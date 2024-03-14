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
import tuf.webscaf.app.dbContext.master.entity.AccountTypeEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.AccountTypesRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountTypeEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountTypesRepository;
import tuf.webscaf.app.service.ApiCallService;
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
@Tag(name = "accountTypeHandler")
public class AccountTypeHandler {

    @Autowired
    AccountTypesRepository accountTypesRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SlaveAccountTypesRepository slaveAccountTypesRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialModuleUri;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.erp_emp_financial_module.uri}")
    private String empFinancialModuleUri;

    @AuthHasPermission(value = "account_api_v1_account-type_index")
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
            Flux<SlaveAccountTypeEntity> slaveAccountTypeEntityFlux = slaveAccountTypesRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveAccountTypeEntityFlux
                    .collectList()
                    .flatMap(slaveAccountTypeEntity -> slaveAccountTypesRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveAccountTypeEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveAccountTypeEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveAccountTypeEntity> slaveAccountTypeEntityFlux = slaveAccountTypesRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveAccountTypeEntityFlux
                    .collectList()
                    .flatMap(slaveAccountTypeEntity -> slaveAccountTypesRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (slaveAccountTypeEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveAccountTypeEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_account-type_active_index")
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

            Flux<SlaveAccountTypeEntity> slaveAccountTypeEntityFlux = slaveAccountTypesRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
            return slaveAccountTypeEntityFlux
                    .collectList()
                    .flatMap(slaveAccountTypeEntity -> slaveAccountTypesRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                            .flatMap(count -> {
                                if (slaveAccountTypeEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveAccountTypeEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-type_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID accountTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveAccountTypesRepository.findByUuidAndDeletedAtIsNull(accountTypeUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-type_store")
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

                    AccountTypeEntity accountTypesEntity = AccountTypeEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                            .description(value.getFirst("description").trim())
                            .code(value.getFirst("code").trim())
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

                    return accountTypesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(accountTypesEntity.getName())
                            .flatMap(typeName -> responseInfoMsg("Name Already Exists"))
                            // check if account Type slug is unique
                            .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findFirstBySlugAndDeletedAtIsNull(accountTypesEntity.getSlug())
                                    .flatMap(checkSlugMsg -> responseInfoMsg("Slug Already Exists"))))
                            .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findByCodeIgnoreCaseAndDeletedAtIsNull(accountTypesEntity.getCode())
                                    .flatMap(varcharCode -> responseInfoMsg("Code Already Exists"))))
                            .switchIfEmpty(Mono.defer(() -> accountTypesRepository.save(accountTypesEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfull", value1))
                            )).switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record.There is something wrong please try again.")))
                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer"));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer"));
    }

    @AuthHasPermission(value = "account_api_v1_account-type_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID accountTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> accountTypesRepository.findByUuidAndDeletedAtIsNull(accountTypeUUID)
                        .flatMap(previousAccountType -> {

                            AccountTypeEntity updatedAccountTypeEntity = AccountTypeEntity
                                    .builder()
                                    .uuid(previousAccountType.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                                    .description(value.getFirst("description").trim())
                                    .code(value.getFirst("code").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousAccountType.getCreatedAt())
                                    .createdBy(previousAccountType.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousAccountType.getReqCreatedIP())
                                    .reqCreatedPort(previousAccountType.getReqCreatedPort())
                                    .reqCreatedBrowser(previousAccountType.getReqCreatedBrowser())
                                    .reqCreatedOS(previousAccountType.getReqCreatedOS())
                                    .reqCreatedDevice(previousAccountType.getReqCreatedDevice())
                                    .reqCreatedReferer(previousAccountType.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousAccountType.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousAccountType.setDeletedBy(UUID.fromString(userId));
                            previousAccountType.setReqDeletedIP(reqIp);
                            previousAccountType.setReqDeletedPort(reqPort);
                            previousAccountType.setReqDeletedBrowser(reqBrowser);
                            previousAccountType.setReqDeletedOS(reqOs);
                            previousAccountType.setReqDeletedDevice(reqDevice);
                            previousAccountType.setReqDeletedReferer(reqReferer);

                            return accountTypesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedAccountTypeEntity.getName(), accountTypeUUID)
                                    .flatMap(name -> responseInfoMsg("Name Already Exist."))
                                    // check if slug is unique
                                    .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(updatedAccountTypeEntity.getSlug(), accountTypeUUID)
                                            .flatMap(checkSlugMsg -> responseInfoMsg("Slug Already Exists"))))
                                    //Checks if code already exists
                                    .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(value.getFirst("code"), accountTypeUUID))
                                            .flatMap(code -> responseInfoMsg("Code Already Exist"))
                                    ).switchIfEmpty(Mono.defer(() -> accountTypesRepository.save(previousAccountType)
                                            .then(accountTypesRepository.save(updatedAccountTypeEntity))
                                            .flatMap(entity -> responseSuccessMsg("Record Updated Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        })
                        .switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-type_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID accountTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
        return accountTypesRepository.findByUuidAndDeletedAtIsNull(accountTypeUUID)
//                        Account Type exist in Accounts
                .flatMap(accountTypeEntity -> accountRepository.findFirstByAccountTypeUUIDAndDeletedAtIsNull(accountTypeUUID)
                        .flatMap(accountEntity -> responseInfoMsg("Unable to Delete Record as the Reference exists"))
                        //Checks if Account Type Reference exists in Financial Accounts in Emp Financial Module
                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(empFinancialModuleUri + "api/v1/financial-accounts/account-type/show/", accountTypeEntity.getUuid())
                                .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                        .flatMap(checkAccountTypeApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
//                              check if Account Type reference exists in financial accounts in student financial module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-accounts/account-type/show/", accountTypeEntity.getUuid())
                                        .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                                .flatMap(checkBranchUUIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))

                        .switchIfEmpty(Mono.defer(() -> {

                            accountTypeEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            accountTypeEntity.setDeletedBy(UUID.fromString(userId));
                            accountTypeEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            accountTypeEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            accountTypeEntity.setReqDeletedIP(reqIp);
                            accountTypeEntity.setReqDeletedPort(reqPort);
                            accountTypeEntity.setReqDeletedBrowser(reqBrowser);
                            accountTypeEntity.setReqDeletedOS(reqOs);
                            accountTypeEntity.setReqDeletedDevice(reqDevice);
                            accountTypeEntity.setReqDeletedReferer(reqReferer);

                            return accountTypesRepository.save(accountTypeEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_account-type_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID accountTypeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return accountTypesRepository.findByUuidAndDeletedAtIsNull(accountTypeUUID)
                            .flatMap(previousAccountTypeEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousAccountTypeEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                AccountTypeEntity updatedAccountTypeEntity = AccountTypeEntity.builder()
                                        .uuid(previousAccountTypeEntity.getUuid())
                                        .name(previousAccountTypeEntity.getName())
                                        .slug(previousAccountTypeEntity.getSlug())
                                        .description(previousAccountTypeEntity.getDescription())
                                        .code(previousAccountTypeEntity.getCode())
                                        .status(status == true ? true : false)
                                        .createdAt(previousAccountTypeEntity.getCreatedAt())
                                        .createdBy(previousAccountTypeEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousAccountTypeEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousAccountTypeEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousAccountTypeEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousAccountTypeEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousAccountTypeEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousAccountTypeEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousAccountTypeEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousAccountTypeEntity.setDeletedBy(UUID.fromString(userId));
                                previousAccountTypeEntity.setReqDeletedIP(reqIp);
                                previousAccountTypeEntity.setReqDeletedPort(reqPort);
                                previousAccountTypeEntity.setReqDeletedBrowser(reqBrowser);
                                previousAccountTypeEntity.setReqDeletedOS(reqOs);
                                previousAccountTypeEntity.setReqDeletedDevice(reqDevice);
                                previousAccountTypeEntity.setReqDeletedReferer(reqReferer);

                                return accountTypesRepository.save(previousAccountTypeEntity)
                                        .then(accountTypesRepository.save(updatedAccountTypeEntity))
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
