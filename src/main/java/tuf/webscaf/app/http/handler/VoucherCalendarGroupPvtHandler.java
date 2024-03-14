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
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherCalendarGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherCalendarGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherCalendarGroupPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCalendarGroupPvtRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "voucherCalendarGroupPvtHandler")
public class VoucherCalendarGroupPvtHandler {
    @Autowired
    VoucherCalendarGroupPvtRepository voucherCalendarGroupPvtRepository;

    @Autowired
    SlaveVoucherCalendarGroupPvtRepository slaveVoucherCalendarGroupPvtRepository;

    @Autowired
    CalendarGroupRepository calendarGroupRepository;

    @Autowired
    SlaveCalendarGroupRepository slaveCalendarGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-calendar-group_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCalendarGroupsAgainstVoucher(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        //Optional Query Parameter Based of Status
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .showUnMappedCalendarGroupListAgainstVoucher(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository
                            .countUnMappedCalendarGroupsRecords(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request. Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .showUnMappedCalendarGroupListAgainstVoucher(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository
                            .countUnMappedCalendarGroupsRecords(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request. Please Contact Developer."));
        }
    }

    //Show Mapped Calendar Groups for given Voucher Id
    @AuthHasPermission(value = "account_api_v1_voucher-calendar-group_mapped_show")
    public Mono<ServerResponse> showMappedCalendarGroupsAgainstVoucher(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }
        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size);
        if (!status.isEmpty()) {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .showMappedCalendarGroupsWithStatusAgainstVoucher(voucherUUID, Boolean.valueOf(status), searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countMappedCalendarGroupListWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        } else {
            Flux<SlaveCalendarGroupEntity> slaveCalendarGroupEntityFlux = slaveCalendarGroupRepository
                    .showMappedCalendarGroupsAgainstVoucher(voucherUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCalendarGroupEntityFlux
                    .collectList()
                    .flatMap(calendarGroupEntity -> slaveCalendarGroupRepository.countMappedCalendarGroupList(voucherUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (calendarGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", calendarGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-calendar-group_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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
                .flatMap(value -> voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
                                .flatMap(voucherEntity -> {

//                            // if given voucher is inactive
//                            if (!voucherEntity.getStatus()) {
//                                return responseInfoMsg("Voucher status is inactive");
//                            }

                                    //getting List of Calendar Groups From Front
                                    List<String> listOfCalendarGroupUUID = value.get("calendarGroupUUID");

                                    listOfCalendarGroupUUID.removeIf(s -> s.equals(""));

                                    List<UUID> l_list = new ArrayList<>();
                                    for (String calendarGroupUUID : listOfCalendarGroupUUID) {
                                        l_list.add(UUID.fromString(calendarGroupUUID));
                                    }

                                    if (!l_list.isEmpty()) {
                                        return calendarGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                .collectList()
                                                .flatMap(existingCalendarGroups -> {
                                                    // Calendar Group UUID List
                                                    List<UUID> calendarGroupList = new ArrayList<>();

                                                    for (CalendarGroupEntity calendarGroup : existingCalendarGroups) {
                                                        calendarGroupList.add(calendarGroup.getUuid());
                                                    }

                                                    if (!calendarGroupList.isEmpty()) {

                                                        // calendar group uuid list to show in response
                                                        List<UUID> calendarGroupRecords = new ArrayList<>(calendarGroupList);

                                                        List<VoucherCalendarGroupPvtEntity> listPvt = new ArrayList<>();

                                                        return voucherCalendarGroupPvtRepository.findAllByVoucherUUIDAndCalendarGroupUUIDInAndDeletedAtIsNull(voucherUUID, calendarGroupList)
                                                                .collectList()
                                                                .flatMap(voucherPvtEntity -> {
                                                                    for (VoucherCalendarGroupPvtEntity pvtEntity : voucherPvtEntity) {
                                                                        //Removing Existing Calendar Group UUID in Calendar Group Final List to be saved that does not contain already mapped values
                                                                        calendarGroupList.remove(pvtEntity.getCalendarGroupUUID());
                                                                    }

                                                                    // iterate Calendar Group UUIDs for given Voucher
                                                                    for (UUID calendarGroupUUID : calendarGroupList) {
                                                                        VoucherCalendarGroupPvtEntity voucherCalendarGroupPvtEntity = VoucherCalendarGroupPvtEntity
                                                                                .builder()
                                                                                .calendarGroupUUID(calendarGroupUUID)
                                                                                .uuid(UUID.randomUUID())
                                                                                .voucherUUID(voucherUUID)
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
                                                                        listPvt.add(voucherCalendarGroupPvtEntity);
                                                                    }

                                                                    return voucherCalendarGroupPvtRepository.saveAll(listPvt)
                                                                            .collectList()
                                                                            .flatMap(groupList -> {

                                                                                if (!calendarGroupList.isEmpty()) {
                                                                                    return responseSuccessMsg("Record Stored Successfully", calendarGroupRecords)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                } else {
                                                                                    return responseInfoMsg("Record Already Exists", calendarGroupRecords);
                                                                                }

                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                });
                                                    } else {
                                                        return responseInfoMsg("Calendar Group Record does not exist");
                                                    }
                                                }).switchIfEmpty(responseInfoMsg("The Entered Calendar Group Does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("The Entered Calendar Group Does not exist.Please Contact Developer."));
                                    } else {
                                        return responseInfoMsg("Select Calendar Group First");
                                    }
                                }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-calendar-group_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

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
                .flatMap(value -> calendarGroupRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarGroupUUID").trim()))
                        .flatMap(calendarGroupEntity -> voucherCalendarGroupPvtRepository
                                .findFirstByVoucherUUIDAndCalendarGroupUUIDAndDeletedAtIsNull(voucherUUID, calendarGroupEntity.getUuid())
                                .flatMap(voucherCalendarGroupPvtEntity -> {

                                    voucherCalendarGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    voucherCalendarGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                    voucherCalendarGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    voucherCalendarGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    voucherCalendarGroupPvtEntity.setReqDeletedIP(reqIp);
                                    voucherCalendarGroupPvtEntity.setReqDeletedPort(reqPort);
                                    voucherCalendarGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                    voucherCalendarGroupPvtEntity.setReqDeletedOS(reqOs);
                                    voucherCalendarGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                    voucherCalendarGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                    return voucherCalendarGroupPvtRepository.save(voucherCalendarGroupPvtEntity)
                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully!", calendarGroupEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                })
                        ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))

                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
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

    public Mono<ServerResponse> responseInfoMsg(String msg, Object entity) {
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
                Mono.just(entity)
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
}
