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
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repository.AccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.AccountGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherAccountGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountGroupRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "accountGroupHandler")
public class AccountGroupHandler {

    @Autowired
    AccountGroupRepository accountGroupRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountGroupAccountPvtRepository accountGroupAccountPvtRepository;

    @Autowired
    VoucherAccountGroupPvtRepository voucherAccountGroupPvtRepository;

    @Autowired
    SlaveAccountGroupRepository slaveAccountGroupRepository;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_account-group_index")
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


//      Return All Account Group
        if (!status.isEmpty()) {
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFluxOfStatus = slaveAccountGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, getStatus, searchKeyWord, getStatus);


            return slaveAccountGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveAccountGroupEntity ->
                            slaveAccountGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveAccountGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", slaveAccountGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

//      Return All Account Group according to given value
        else {

            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(slaveAccountGroupEntity ->
                            slaveAccountGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveAccountGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", slaveAccountGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

        }
    }
    @AuthHasPermission(value = "account_api_v1_account-group_active_index")
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

        Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFluxOfStatus = slaveAccountGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);


        return slaveAccountGroupEntityFluxOfStatus
                .collectList()
                .flatMap(slaveAccountGroupEntity ->
                        slaveAccountGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                .flatMap(count -> {
                                    if (slaveAccountGroupEntity.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", slaveAccountGroupEntity, count);
                                    }
                                })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-group_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveAccountGroupRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-group_account_mapped_show")
    //Show Account Groups for Account UUID
    public Mono<ServerResponse> listOfAccountGroups(ServerRequest serverRequest) {
        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("accountUUID").trim());

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        // if status is given in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .listOfAccountGroupsWithStatus(accountUUID, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository.countMappedAccountGroupAgainstAccountWithStatus(accountUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .listOfAccountGroups(accountUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository.countMappedAccountGroupAgainstAccount(accountUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_account-group_store")
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

                    AccountGroupEntity accountGroupsEntity = AccountGroupEntity
                            .builder()
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

                    return accountGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(accountGroupsEntity.getName())
                            .flatMap(typeName -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> accountGroupRepository.save(accountGroupsEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-group_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                .flatMap(value -> accountGroupRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
                        .flatMap(previousAccountGroupEntity -> {

                                    AccountGroupEntity updatedEntity = AccountGroupEntity
                                            .builder()
                                            .uuid(previousAccountGroupEntity.getUuid())
                                            .name(value.getFirst("name").trim())
                                            .description(value.getFirst("description").trim())
                                            .status(Boolean.valueOf(value.getFirst("status")))
                                            .createdAt(previousAccountGroupEntity.getCreatedAt())
                                            .createdBy(previousAccountGroupEntity.getCreatedBy())
                                            .updatedBy(UUID.fromString(userId))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(previousAccountGroupEntity.getReqCreatedIP())
                                            .reqCreatedPort(previousAccountGroupEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(previousAccountGroupEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(previousAccountGroupEntity.getReqCreatedOS())
                                            .reqCreatedDevice(previousAccountGroupEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(previousAccountGroupEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                            .reqUpdatedIP(reqIp)
                                            .reqUpdatedPort(reqPort)
                                            .reqUpdatedBrowser(reqBrowser)
                                            .reqUpdatedOS(reqOs)
                                            .reqUpdatedDevice(reqDevice)
                                            .reqUpdatedReferer(reqReferer)
                                            .build();

                                    previousAccountGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousAccountGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    previousAccountGroupEntity.setReqDeletedIP(reqIp);
                                    previousAccountGroupEntity.setReqDeletedPort(reqPort);
                                    previousAccountGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    previousAccountGroupEntity.setReqDeletedOS(reqOs);
                                    previousAccountGroupEntity.setReqDeletedDevice(reqDevice);
                                    previousAccountGroupEntity.setReqDeletedReferer(reqReferer);

                                    return accountGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getName(), accountGroupUUID)
                                            //Checks if name already exists
                                            .flatMap(entity -> responseInfoMsg("Name Already Exists."))
                                            .switchIfEmpty(Mono.defer(() -> accountGroupRepository.save(previousAccountGroupEntity)
                                                    .then(accountGroupRepository.save(updatedEntity))
                                                    .flatMap(accountGroupEntity -> responseSuccessMsg("Record Updated Successfully!", accountGroupEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again!"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            ));
                                }
                        ).switchIfEmpty(responseInfoMsg("Account Group Does not exist."))
                        .onErrorResume(ex -> responseErrorMsg("Account Group Does not exist.Please Contact Developer."))
                )
                .switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-group_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        //check if Account Group Exists
        return accountGroupRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
                .flatMap(accountGroupEntity -> voucherAccountGroupPvtRepository.findFirstByAccountGroupUUIDAndDeletedAtIsNull(accountGroupUUID)
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> accountGroupAccountPvtRepository.findFirstByAccountGroupUUIDAndDeletedAtIsNull(accountGroupUUID)
                                .flatMap(pvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        .switchIfEmpty(Mono.defer(() -> {

                            accountGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            accountGroupEntity.setDeletedBy(UUID.fromString(userId));
                            accountGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            accountGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            accountGroupEntity.setReqDeletedIP(reqIp);
                            accountGroupEntity.setReqDeletedPort(reqPort);
                            accountGroupEntity.setReqDeletedBrowser(reqBrowser);
                            accountGroupEntity.setReqDeletedOS(reqOs);
                            accountGroupEntity.setReqDeletedDevice(reqDevice);
                            accountGroupEntity.setReqDeletedReferer(reqReferer);

                            return accountGroupRepository.save(accountGroupEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-group_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return accountGroupRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
                            .flatMap(previousAccountGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousAccountGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                AccountGroupEntity updatedEntity = AccountGroupEntity.builder()
                                        .uuid(previousAccountGroupEntity.getUuid())
                                        .name(previousAccountGroupEntity.getName())
                                        .description(previousAccountGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousAccountGroupEntity.getCreatedAt())
                                        .createdBy(previousAccountGroupEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousAccountGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousAccountGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousAccountGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousAccountGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousAccountGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousAccountGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousAccountGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousAccountGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousAccountGroupEntity.setReqDeletedIP(reqIp);
                                previousAccountGroupEntity.setReqDeletedPort(reqPort);
                                previousAccountGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousAccountGroupEntity.setReqDeletedOS(reqOs);
                                previousAccountGroupEntity.setReqDeletedDevice(reqDevice);
                                previousAccountGroupEntity.setReqDeletedReferer(reqReferer);

                                return accountGroupRepository.save(previousAccountGroupEntity)
                                        .then(accountGroupRepository.save(updatedEntity))
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
