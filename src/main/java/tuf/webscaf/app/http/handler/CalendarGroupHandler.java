package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherCalendarGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarGroupRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "calendarGroupHandler")
public class CalendarGroupHandler {

    @Autowired
    CalendarGroupRepository calendarGroupRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    CalendarGroupCalendarPvtRepository calendarGroupCalendarPvtRepository;

    @Autowired
    VoucherCalendarGroupPvtRepository voucherCalendarGroupPvtRepository;

    @Autowired
    SlaveCalendarGroupRepository slaveCalendarGroupRepository;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialUri;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_calendar-group_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        boolean getStatus = false;
        if (!status.isEmpty()) {
            getStatus = Boolean.parseBoolean(status);

        }


//      Return All Calendar Group
        if (!status.isEmpty()) {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFluxOfStatus = slaveCalendarGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, getStatus, searchKeyWord, getStatus);


            return slaveCalendarGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveCalendarGroupEntity ->
                            slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveCalendarGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", slaveCalendarGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

//      Return All Calendar Group according to given value
        else {

            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(slaveCalendarGroupEntity ->
                            slaveCalendarGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveCalendarGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", slaveCalendarGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

        }
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFluxWithStatus = slaveCalendarGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveCalendarGroupEntityFluxWithStatus
                .collectList()
                .flatMap(slaveCalendarGroupEntity ->
                        slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                .flatMap(count -> {
                                    if (slaveCalendarGroupEntity.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", slaveCalendarGroupEntity, count);
                                    }
                                })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }


    @AuthHasPermission(value = "account_api_v1_calendar-group_existing_list_show")
    //This Function Is used By Student Financial Module to Check if Calendar Group UUID exists in Financial Voucher Mapping
    public Mono<ServerResponse> showCalendarGroupListInStudentFinancial(ServerRequest serverRequest) {

        List<String> uuids = serverRequest.queryParams().get("uuid");

        //This is Calendar Group List to paas in the query
        List<UUID> calendarGroupList = new ArrayList<>();
        if (uuids != null) {
            for (String calendarGroup : uuids) {
                calendarGroupList.add(UUID.fromString(calendarGroup));
            }
        }

        // Used to Show Existing Calendar Group UUIDs in Response
        List<UUID> finalList = new ArrayList<>();

        return calendarGroupRepository.findAllByUuidInAndDeletedAtIsNull(calendarGroupList)
                .collectList()
                .flatMap(calendarGroupEntities -> {
                    for (CalendarGroupEntity entity : calendarGroupEntities) {
                        finalList.add(entity.getUuid());
                    }
                    return responseSuccessMsg("Records Fetched Successfully", finalList);
                });
    }

    //This Function Is used By Student Financial Module in Financial Voucher Calendar Group Pvt Handler
//    public Mono<ServerResponse> getCalendarGroupList(ServerRequest serverRequest) {
//        List<String> uuids = serverRequest.queryParams().get("uuid");
//
//        //This is Calendar Group List to paas in the query
//        List<UUID> calendarGroupList = new ArrayList<>();
//        if (uuids != null) {
//            for (String calendarGroup : uuids) {
//                calendarGroupList.add(UUID.fromString(calendarGroup));
//            }
//        }
//        return slaveCalendarGroupRepository.findAllByUuidInAndDeletedAtIsNull(calendarGroupList)
//                .collectList()
//                .flatMap(value -> responseSuccessMsg("Record Fetched Successfully", value))
//                .switchIfEmpty(responseErrorMsg("Record Does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
//    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_financial-voucher_mapped_show")
    //    Show Mapped Calendar Groups for Financial Voucher UUID
    public Mono<ServerResponse> listOfMappedCalendarGroupsAgainstFinancialVoucher(ServerRequest serverRequest) {

        UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(studentFinancialUri + "api/v1/financial-voucher-calendar-groups/list/show/", financialVoucherUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getListUUID(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveCalendarGroupEntityFlux
                                .collectList()
                                .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (calendarGroupEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", calendarGroupEntity, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

                    } else {
                        Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveCalendarGroupEntityFlux
                                .collectList()
                                .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (calendarGroupEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", calendarGroupEntity, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                    }
                });
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_financial-voucher_un-mapped_show")
    //    Show Unmapped Calendar Groups for Financial Voucher UUID
    public Mono<ServerResponse> listOfExistingCalendarGroupsAgainstFinancialVoucher(ServerRequest serverRequest) {

        UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(studentFinancialUri + "api/v1/financial-voucher-calendar-groups/list/show/", financialVoucherUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getListUUID(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveCalendarGroupEntityFlux
                                .collectList()
                                .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (calendarGroupEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", calendarGroupEntity, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

                    } else {
                        Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveCalendarGroupEntityFlux
                                .collectList()
                                .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (calendarGroupEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", calendarGroupEntity, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
                    }
                });
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_calendar_mapped_show")
    //    Show Calendar Groups for Calendar Id
    public Mono<ServerResponse> listOfCalendarGroups(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("calendarUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);


        if (!status.isEmpty()) {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFluxWithStatus = slaveCalendarGroupRepository
                    .listOfCalendarGroupsWithStatusFilterAgainstCalendar(calendarUUID, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCalendarGroupEntityFluxWithStatus
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countMappedCalendarGroupsWithStatusFilter(calendarUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .listOfCalendarGroupsAgainstCalendar(calendarUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countMappedCalendarGroups(calendarUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }


    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }
        return serverRequest.formData()
                .flatMap(value -> {

                    CalendarGroupEntity calendarGroupsEntity = CalendarGroupEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .createdBy(UUID.fromString(userId))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    return calendarGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(calendarGroupsEntity.getName())
                            .flatMap(typeName -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> calendarGroupRepository.save(calendarGroupsEntity)
                                    .flatMap(calendarGroupSave -> responseSuccessMsg("Record Stored Successfully.", calendarGroupSave))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please Try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> calendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                        .flatMap(previousCalendarGroup -> {

                            CalendarGroupEntity calendarGroupsEntity = CalendarGroupEntity.builder()
                                    .uuid(previousCalendarGroup.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousCalendarGroup.getCreatedAt())
                                    .createdBy(previousCalendarGroup.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCalendarGroup.getReqCreatedIP())
                                    .reqCreatedPort(previousCalendarGroup.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCalendarGroup.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCalendarGroup.getReqCreatedOS())
                                    .reqCreatedDevice(previousCalendarGroup.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCalendarGroup.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCalendarGroup.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCalendarGroup.setDeletedBy(UUID.fromString(userId));
                            previousCalendarGroup.setReqDeletedIP(reqIp);
                            previousCalendarGroup.setReqDeletedPort(reqPort);
                            previousCalendarGroup.setReqDeletedBrowser(reqBrowser);
                            previousCalendarGroup.setReqDeletedOS(reqOs);
                            previousCalendarGroup.setReqDeletedDevice(reqDevice);
                            previousCalendarGroup.setReqDeletedReferer(reqReferer);

                            return calendarGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(calendarGroupsEntity.getName(), calendarGroupUUID)
                                    //Checks if name already exists
                                    .flatMap(entity -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> calendarGroupRepository.save(previousCalendarGroup)
                                            .then(calendarGroupRepository.save(calendarGroupsEntity))
                                            .flatMap(calendarGroupEntity -> responseSuccessMsg("Record Updated Successfully", calendarGroupEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again"))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Calendar Group Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Calendar Group Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        //check if Calendar Group Exists
        return calendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                .flatMap(calendarGroupEntity -> voucherCalendarGroupPvtRepository.findFirstByCalendarGroupUUIDAndDeletedAtIsNull(calendarGroupEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> calendarGroupCalendarPvtRepository.findFirstByCalendarGroupUUIDAndDeletedAtIsNull(calendarGroupEntity.getUuid())
                                .flatMap(pvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        .switchIfEmpty(Mono.defer(() -> {

                            calendarGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarGroupEntity.setDeletedBy(UUID.fromString(userId));
                            calendarGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarGroupEntity.setReqDeletedIP(reqIp);
                            calendarGroupEntity.setReqDeletedPort(reqPort);
                            calendarGroupEntity.setReqDeletedBrowser(reqBrowser);
                            calendarGroupEntity.setReqDeletedOS(reqOs);
                            calendarGroupEntity.setReqDeletedDevice(reqDevice);
                            calendarGroupEntity.setReqDeletedReferer(reqReferer);

                            return calendarGroupRepository.save(calendarGroupEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_calendar-group_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID calendarGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User!");
            }
        }
        return serverRequest.formData()
                .flatMap(value -> {

                    boolean status = Boolean.parseBoolean(value.getFirst("status"));

                    return calendarGroupRepository.findByUuidAndDeletedAtIsNull(calendarGroupUUID)
                            .flatMap(previousCalendarGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCalendarGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CalendarGroupEntity updatedCalendarGroupEntity = CalendarGroupEntity.builder()
                                        .uuid(previousCalendarGroupEntity.getUuid())
                                        .name(previousCalendarGroupEntity.getName())
                                        .description(previousCalendarGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousCalendarGroupEntity.getCreatedAt())
                                        .createdBy(previousCalendarGroupEntity.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userId))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCalendarGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCalendarGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCalendarGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCalendarGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCalendarGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCalendarGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCalendarGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCalendarGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousCalendarGroupEntity.setReqDeletedIP(reqIp);
                                previousCalendarGroupEntity.setReqDeletedPort(reqPort);
                                previousCalendarGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousCalendarGroupEntity.setReqDeletedOS(reqOs);
                                previousCalendarGroupEntity.setReqDeletedDevice(reqDevice);
                                previousCalendarGroupEntity.setReqDeletedReferer(reqReferer);

                                return calendarGroupRepository.save(previousCalendarGroupEntity)
                                        .then(calendarGroupRepository.save(updatedCalendarGroupEntity))
                                        .flatMap(calendarGroupEntity -> responseSuccessMsg("Status updated successfully", calendarGroupEntity))
                                        .switchIfEmpty(responseInfoMsg("Unable to Update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to Update the status.Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }
}
