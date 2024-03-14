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
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.SubAccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.SubAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveSubAccountGroupRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "subAccountGroupHandler")
public class SubAccountGroupHandler {

    @Autowired
    SubAccountGroupRepository subAccountGroupRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SlaveSubAccountGroupRepository slaveSubAccountGroupsRepository;

    @Autowired
    SubAccountGroupAccountPvtRepository subAccountGroupAccountPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Autowired
    SlugifyHelper slugifyHelper;

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupsRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(slaveSubAccountGroupEntity -> slaveSubAccountGroupsRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveSubAccountGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveSubAccountGroupEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupsRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(slaveSubAccountGroupEntity -> slaveSubAccountGroupsRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (slaveSubAccountGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveSubAccountGroupEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_active_index")
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

        Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupsRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return slaveSubAccountGroupEntityFlux
                .collectList()
                .flatMap(slaveSubAccountGroupEntity -> slaveSubAccountGroupsRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                            if (slaveSubAccountGroupEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", slaveSubAccountGroupEntity, count);
                            }
                        })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveSubAccountGroupsRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_store")
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

                    SubAccountGroupEntity subAccountGroupsEntity = SubAccountGroupEntity.builder()
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

                    // check if name is unique
                    return subAccountGroupRepository.findByNameIgnoreCaseAndDeletedAtIsNull(subAccountGroupsEntity.getName())
                            .flatMap(typeName -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> subAccountGroupRepository.save(subAccountGroupsEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                            )).switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record.There is something wrong please try again.")))
                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                        .flatMap(previousSubAccountGroup -> {

                            SubAccountGroupEntity updatedSubAccountGroupEntity = SubAccountGroupEntity
                                    .builder()
                                    .uuid(previousSubAccountGroup.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousSubAccountGroup.getCreatedAt())
                                    .createdBy(previousSubAccountGroup.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousSubAccountGroup.getReqCreatedIP())
                                    .reqCreatedPort(previousSubAccountGroup.getReqCreatedPort())
                                    .reqCreatedBrowser(previousSubAccountGroup.getReqCreatedBrowser())
                                    .reqCreatedOS(previousSubAccountGroup.getReqCreatedOS())
                                    .reqCreatedDevice(previousSubAccountGroup.getReqCreatedDevice())
                                    .reqCreatedReferer(previousSubAccountGroup.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousSubAccountGroup.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousSubAccountGroup.setDeletedBy(UUID.fromString(userId));
                            previousSubAccountGroup.setReqDeletedIP(reqIp);
                            previousSubAccountGroup.setReqDeletedPort(reqPort);
                            previousSubAccountGroup.setReqDeletedBrowser(reqBrowser);
                            previousSubAccountGroup.setReqDeletedOS(reqOs);
                            previousSubAccountGroup.setReqDeletedDevice(reqDevice);
                            previousSubAccountGroup.setReqDeletedReferer(reqReferer);

                            // check if name is unique
                            return subAccountGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedSubAccountGroupEntity.getName(), subAccountGroupUUID)
                                    .flatMap(name -> responseInfoMsg("Name Already Exist."))
                                    .switchIfEmpty(Mono.defer(() -> subAccountGroupRepository.save(previousSubAccountGroup)
                                            .then(subAccountGroupRepository.save(updatedSubAccountGroupEntity))
                                            .flatMap(entity -> responseSuccessMsg("Record Updated Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist."))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
        return subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                // Check if Sub Account Type exist in Accounts
                .flatMap(subAccountGroupEntity -> subAccountGroupAccountPvtRepository.findFirstBySubAccountGroupUUIDAndDeletedAtIsNull(subAccountGroupUUID)
                        .flatMap(subAccountGroupAccountPvtEntity -> responseInfoMsg("Unable to Delete Record As Reference of Record Exists"))
                        .switchIfEmpty(Mono.defer(() -> {

                            subAccountGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            subAccountGroupEntity.setDeletedBy(UUID.fromString(userId));
                            subAccountGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            subAccountGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            subAccountGroupEntity.setReqDeletedIP(reqIp);
                            subAccountGroupEntity.setReqDeletedPort(reqPort);
                            subAccountGroupEntity.setReqDeletedBrowser(reqBrowser);
                            subAccountGroupEntity.setReqDeletedOS(reqOs);
                            subAccountGroupEntity.setReqDeletedDevice(reqDevice);
                            subAccountGroupEntity.setReqDeletedReferer(reqReferer);

                            return subAccountGroupRepository.save(subAccountGroupEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-groups_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                            .flatMap(previousSubAccountGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousSubAccountGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                SubAccountGroupEntity updatedSubAccountGroupEntity = SubAccountGroupEntity.builder()
                                        .uuid(previousSubAccountGroupEntity.getUuid())
                                        .name(previousSubAccountGroupEntity.getName())
                                        .description(previousSubAccountGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousSubAccountGroupEntity.getCreatedAt())
                                        .createdBy(previousSubAccountGroupEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousSubAccountGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousSubAccountGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousSubAccountGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousSubAccountGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousSubAccountGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousSubAccountGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousSubAccountGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousSubAccountGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousSubAccountGroupEntity.setReqDeletedIP(reqIp);
                                previousSubAccountGroupEntity.setReqDeletedPort(reqPort);
                                previousSubAccountGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousSubAccountGroupEntity.setReqDeletedOS(reqOs);
                                previousSubAccountGroupEntity.setReqDeletedDevice(reqDevice);
                                previousSubAccountGroupEntity.setReqDeletedReferer(reqReferer);

                                return subAccountGroupRepository.save(previousSubAccountGroupEntity)
                                        .then(subAccountGroupRepository.save(updatedSubAccountGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
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
