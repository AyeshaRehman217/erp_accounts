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
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupVoucherPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.VoucherGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherGroupVoucherPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherGroupRepository;
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
@Tag(name = "voucherGroupHandler")
public class VoucherGroupHandler {
    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    VoucherGroupRepository voucherGroupRepository;

    @Autowired
    SlaveVoucherGroupRepository slaveVoucherGroupRepository;

    @Autowired
    VoucherGroupVoucherPvtRepository voucherGroupVoucherPvtRepository;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-groups_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
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

        //      Return All Voucher Group
        if (!status.isEmpty()) {
            Flux<SlaveVoucherGroupEntity> slaveVoucherGroupEntityFluxOfStatus = slaveVoucherGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveVoucherGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveVoucherGroupEntity -> slaveVoucherGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveVoucherGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", slaveVoucherGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

        }

//      Return All Voucher Group according to given value
        else {

            Flux<SlaveVoucherGroupEntity> slaveVoucherGroupEntityFlux = slaveVoucherGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveVoucherGroupEntityFlux
                    .collectList()
                    .flatMap(slaveVoucherGroupEntity -> slaveVoucherGroupRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (slaveVoucherGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", slaveVoucherGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact to developer"));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
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


        Flux<SlaveVoucherGroupEntity> slaveVoucherGroupEntityFluxOfStatus = slaveVoucherGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveVoucherGroupEntityFluxOfStatus
                .collectList()
                .flatMap(slaveVoucherGroupEntity -> slaveVoucherGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                            if (slaveVoucherGroupEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);

                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully.", slaveVoucherGroupEntity, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveVoucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_voucher_mapped_show")
    public Mono<ServerResponse> showVoucherWithVoucherGroups(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

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
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveVoucherGroupEntity> slaveVoucherGroupEntityFlux = slaveVoucherGroupRepository
                    .showVoucherGroupListWithStatusAgainstVoucher(voucherUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherGroupEntityFlux
                    .collectList()
                    .flatMap(voucherGroupEntity -> slaveVoucherGroupRepository
                            .countVoucherGroupListWithStatusAgainstVoucher(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (voucherGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", voucherGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherGroupEntity> slaveVoucherGroupEntityFlux = slaveVoucherGroupRepository
                    .showVoucherGroupListAgainstVoucher(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherGroupEntityFlux
                    .collectList()
                    .flatMap(voucherGroupEntity -> slaveVoucherGroupRepository
                            .countVoucherGroupListAgainstVoucher(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (voucherGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", voucherGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_store")
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
                    VoucherGroupEntity voucherGroupEntity = VoucherGroupEntity.builder()
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

                    return voucherGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(voucherGroupEntity.getName())
                            .flatMap(key -> responseInfoMsg("Name Already exist"))
                            .switchIfEmpty(Mono.defer(() -> voucherGroupRepository.save(voucherGroupEntity)
                                    .flatMap(voucherGroupEntity1 -> responseSuccessMsg("Record stored successfully.", voucherGroupEntity1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
                .flatMap(value -> voucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                        .flatMap(previousVoucherGroup -> {

                            VoucherGroupEntity updatedVoucherGroupEntity = VoucherGroupEntity.builder()
                                    .uuid(previousVoucherGroup.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousVoucherGroup.getCreatedAt())
                                    .createdBy(previousVoucherGroup.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousVoucherGroup.getReqCreatedIP())
                                    .reqCreatedPort(previousVoucherGroup.getReqCreatedPort())
                                    .reqCreatedBrowser(previousVoucherGroup.getReqCreatedBrowser())
                                    .reqCreatedOS(previousVoucherGroup.getReqCreatedOS())
                                    .reqCreatedDevice(previousVoucherGroup.getReqCreatedDevice())
                                    .reqCreatedReferer(previousVoucherGroup.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousVoucherGroup.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousVoucherGroup.setDeletedBy(UUID.fromString(userId));
                            previousVoucherGroup.setReqDeletedIP(reqIp);
                            previousVoucherGroup.setReqDeletedPort(reqPort);
                            previousVoucherGroup.setReqDeletedBrowser(reqBrowser);
                            previousVoucherGroup.setReqDeletedOS(reqOs);
                            previousVoucherGroup.setReqDeletedDevice(reqDevice);
                            previousVoucherGroup.setReqDeletedReferer(reqReferer);

                            return voucherGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedVoucherGroupEntity.getName(), updatedVoucherGroupEntity.getUuid())
                                    .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> voucherGroupRepository.save(previousVoucherGroup)
                                            .then(voucherGroupRepository.save(updatedVoucherGroupEntity))
                                            .flatMap(groupEntity -> responseSuccessMsg("Record Updated Successfully.", groupEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Requested Voucher Group Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
        return voucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                //check if Voucher Group Exists in Voucher Pvt Table
                .flatMap(voucherGroupEntity -> voucherGroupVoucherPvtRepository.findFirstByVoucherGroupUUIDAndDeletedAtIsNull(voucherGroupEntity.getUuid())
                        .flatMap(pvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                        .switchIfEmpty(Mono.defer(() -> {

                            voucherGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            voucherGroupEntity.setDeletedBy(UUID.fromString(userId));
                            voucherGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            voucherGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            voucherGroupEntity.setReqDeletedIP(reqIp);
                            voucherGroupEntity.setReqDeletedPort(reqPort);
                            voucherGroupEntity.setReqDeletedBrowser(reqBrowser);
                            voucherGroupEntity.setReqDeletedOS(reqOs);
                            voucherGroupEntity.setReqDeletedDevice(reqDevice);
                            voucherGroupEntity.setReqDeletedReferer(reqReferer);

                            return voucherGroupRepository.save(voucherGroupEntity)
                                    .flatMap(saveEntity -> responseSuccessMsg("Record deleted successfully", saveEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));

                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-groups_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID voucherGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                    return voucherGroupRepository.findByUuidAndDeletedAtIsNull(voucherGroupUUID)
                            .flatMap(previousVoucherGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousVoucherGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                VoucherGroupEntity updatedVoucherGroupEntity = VoucherGroupEntity
                                        .builder()
                                        .uuid(previousVoucherGroupEntity.getUuid())
                                        .name(previousVoucherGroupEntity.getName())
                                        .description(previousVoucherGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousVoucherGroupEntity.getCreatedAt())
                                        .createdBy(previousVoucherGroupEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousVoucherGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousVoucherGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousVoucherGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousVoucherGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousVoucherGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousVoucherGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousVoucherGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousVoucherGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousVoucherGroupEntity.setReqDeletedIP(reqIp);
                                previousVoucherGroupEntity.setReqDeletedPort(reqPort);
                                previousVoucherGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousVoucherGroupEntity.setReqDeletedOS(reqOs);
                                previousVoucherGroupEntity.setReqDeletedDevice(reqDevice);
                                previousVoucherGroupEntity.setReqDeletedReferer(reqReferer);

                                return voucherGroupRepository.save(previousVoucherGroupEntity)
                                        .then(voucherGroupRepository.save(updatedVoucherGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please Contact Developer."));
                            }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    //    ---------------  Custom Response Functions----------------

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
                        msg)
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

    //    ---------------  Custom Response Functions----------------

}
