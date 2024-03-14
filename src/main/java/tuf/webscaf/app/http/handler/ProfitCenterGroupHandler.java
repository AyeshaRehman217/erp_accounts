package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.JobGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupProfitCenterPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterGroupRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "profitCenterGroupHandler")
public class ProfitCenterGroupHandler {
    @Autowired
    ProfitCenterGroupRepository profitCenterGroupRepository;

    @Autowired
    SlaveProfitCenterGroupRepository slaveProfitCenterGroupRepository;

    @Autowired
    VoucherProfitCenterGroupPvtRepository voucherProfitCenterGroupPvtRepository;

    @Autowired
    ProfitCenterRepository profitCenterRepository;

    @Autowired
    ProfitCenterGroupProfitCenterPvtRepository profitCenterGroupProfitCenterPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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


        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        // Return All Profit Center Group
        if (!status.isEmpty()) {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFluxOfStatus = slaveProfitCenterGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveProfitCenterGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveProfitCenterGroupEntity ->
                            slaveProfitCenterGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveProfitCenterGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveProfitCenterGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

        }
//      Return All Profit Center Group according to given value
        else {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(slaveProfitCenterGroupEntity ->
                            slaveProfitCenterGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveProfitCenterGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveProfitCenterGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact to developer"));
        }
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

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

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFluxOfStatus = slaveProfitCenterGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveProfitCenterGroupEntityFluxOfStatus
                .collectList()
                .flatMap(slaveProfitCenterGroupEntity ->
                        slaveProfitCenterGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                .flatMap(count -> {
                                    if (slaveProfitCenterGroupEntity.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", slaveProfitCenterGroupEntity, count);
                                    }
                                })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveProfitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_profit-center_mapped_show")
    public Mono<ServerResponse> listOfProfitCenterGroups(ServerRequest serverRequest) {
        UUID profitCenterUUID = UUID.fromString(serverRequest.pathVariable("profitCenterUUID").trim());

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("");

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        // if status is not present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .listOfProfitCenterGroupsWithStatus(profitCenterUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroupsWithStatus(profitCenterUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseInfoMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .listOfProfitCenterGroups(profitCenterUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroups(profitCenterUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseInfoMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    public Mono<ServerResponse> listOfProfitCenterGroupsAgainstVoucher(ServerRequest serverRequest) {
        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("");

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
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showMappedProfitCenterGroupsWithStatus(voucherUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroupsWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showMappedProfitCenterGroups(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroups(voucherUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_store")
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
            return responseInfoMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseInfoMsg("Unknown user");
        }

        return serverRequest.formData()
                //Check If the Name Already Exists
                .flatMap(value -> {

                    ProfitCenterGroupEntity profitCenterGroupEntity = ProfitCenterGroupEntity.builder()
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

                    return profitCenterGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(profitCenterGroupEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> profitCenterGroupRepository.save(profitCenterGroupEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please Try Again!"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.There is something wrong please try again."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
            return responseInfoMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseInfoMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                        .flatMap(previousProfitCenter -> {

                            ProfitCenterGroupEntity profitCenterGroupEntity = ProfitCenterGroupEntity.builder()
                                    .uuid(previousProfitCenter.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousProfitCenter.getCreatedAt())
                                    .createdBy(previousProfitCenter.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousProfitCenter.getReqCreatedIP())
                                    .reqCreatedPort(previousProfitCenter.getReqCreatedPort())
                                    .reqCreatedBrowser(previousProfitCenter.getReqCreatedBrowser())
                                    .reqCreatedOS(previousProfitCenter.getReqCreatedOS())
                                    .reqCreatedDevice(previousProfitCenter.getReqCreatedDevice())
                                    .reqCreatedReferer(previousProfitCenter.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousProfitCenter.setDeletedBy(UUID.fromString(userId));
                            previousProfitCenter.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousProfitCenter.setReqDeletedIP(reqIp);
                            previousProfitCenter.setReqDeletedPort(reqPort);
                            previousProfitCenter.setReqDeletedBrowser(reqBrowser);
                            previousProfitCenter.setReqDeletedOS(reqOs);
                            previousProfitCenter.setReqDeletedDevice(reqDevice);
                            previousProfitCenter.setReqDeletedReferer(reqReferer);

                            return profitCenterGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(profitCenterGroupEntity.getName(), profitCenterGroupUUID)
                                    .flatMap(checkName -> responseInfoMsg("Name Already Exists."))
                                    .switchIfEmpty(Mono.defer(() -> profitCenterGroupRepository.save(previousProfitCenter)
                                            .then(profitCenterGroupRepository.save(profitCenterGroupEntity))
                                            .flatMap(saveProfitGroup -> responseSuccessMsg("Record Updated Successfully.", saveProfitGroup))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong Please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Profit Center Group Does not exist."))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

        //check if Voucher Group Exists
        return profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                //check if profit Group Exists in Voucher Pvt Table
                .flatMap(profitCenterGroupEntity -> voucherProfitCenterGroupPvtRepository.findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(profitCenterGroupEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        //check if Profit Center Group Exists in Profit Center Pvt Table
                        .switchIfEmpty(Mono.defer(() -> profitCenterGroupProfitCenterPvtRepository.findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(profitCenterGroupEntity.getUuid())
                                .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        .switchIfEmpty(Mono.defer(() -> {

                                    profitCenterGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    profitCenterGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    profitCenterGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    profitCenterGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    profitCenterGroupEntity.setReqDeletedIP(reqIp);
                                    profitCenterGroupEntity.setReqDeletedPort(reqPort);
                                    profitCenterGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    profitCenterGroupEntity.setReqDeletedOS(reqOs);
                                    profitCenterGroupEntity.setReqDeletedDevice(reqDevice);
                                    profitCenterGroupEntity.setReqDeletedReferer(reqReferer);

                                    return profitCenterGroupRepository.save(profitCenterGroupEntity)
                                            .flatMap(saveEntity -> responseSuccessMsg("Record deleted successfully", saveEntity)
                                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer.")));
                                }
                        ))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_profit-center-groups_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                            .flatMap(previousProfitCenterGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousProfitCenterGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                ProfitCenterGroupEntity updatedProfitCenterGroupEntity = ProfitCenterGroupEntity.builder()
                                        .uuid(previousProfitCenterGroupEntity.getUuid())
                                        .name(previousProfitCenterGroupEntity.getName())
                                        .description(previousProfitCenterGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousProfitCenterGroupEntity.getCreatedAt())
                                        .createdBy(previousProfitCenterGroupEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousProfitCenterGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousProfitCenterGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousProfitCenterGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousProfitCenterGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousProfitCenterGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousProfitCenterGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousProfitCenterGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousProfitCenterGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousProfitCenterGroupEntity.setReqDeletedIP(reqIp);
                                previousProfitCenterGroupEntity.setReqDeletedPort(reqPort);
                                previousProfitCenterGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousProfitCenterGroupEntity.setReqDeletedOS(reqOs);
                                previousProfitCenterGroupEntity.setReqDeletedDevice(reqDevice);
                                previousProfitCenterGroupEntity.setReqDeletedReferer(reqReferer);

                                return profitCenterGroupRepository.save(previousProfitCenterGroupEntity)
                                        .then(profitCenterGroupRepository.save(updatedProfitCenterGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
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



