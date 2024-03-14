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
import tuf.webscaf.app.dbContext.master.repository.TransactionRepository;
import tuf.webscaf.app.dbContext.master.repository.TransactionStatusRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionStatusEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveTransactionStatusRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "transactionStatusHandler")
public class TransactionStatusHandler {
    @Autowired
    TransactionStatusRepository transactionStatusRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SlaveTransactionStatusRepository slaveTransactionStatusRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_transaction-status_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
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
        if (!status.isEmpty()) {
            Flux<SlaveTransactionStatusEntity> transactionEntityFlux = slaveTransactionStatusRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), pageable);
            return transactionEntityFlux
                    .collectList()
                    .flatMap(transactionStatusDB -> slaveTransactionStatusRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (transactionStatusDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", transactionStatusDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveTransactionStatusEntity> transactionEntityFlux = slaveTransactionStatusRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord, pageable);
            return transactionEntityFlux
                    .collectList()
                    .flatMap(transactionStatusDB -> slaveTransactionStatusRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (transactionStatusDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", transactionStatusDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_active_index")
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


            Flux<SlaveTransactionStatusEntity> transactionEntityFlux = slaveTransactionStatusRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, pageable);
            return transactionEntityFlux
                    .collectList()
                    .flatMap(transactionStatusDB -> slaveTransactionStatusRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                            .flatMap(count -> {
                                        if (transactionStatusDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", transactionStatusDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID transactionStatusUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveTransactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionStatusUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_store")
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

                    TransactionStatusEntity statusEntity = TransactionStatusEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
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

                    return transactionStatusRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(statusEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exists"))
                            .switchIfEmpty(Mono.defer(() -> transactionStatusRepository.findFirstBySlugAndDeletedAtIsNull(statusEntity.getSlug())
                                    .flatMap(checkSlug -> responseInfoMsg("Slug already Exist."))))
                            .switchIfEmpty(Mono.defer(() -> transactionStatusRepository.save(statusEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully!", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again!"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID transactionStatusUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionStatusUUID)
                        .flatMap(previousTransactionStatus -> {

                            TransactionStatusEntity updatedStatusEntity = TransactionStatusEntity.builder()
                                    .uuid(previousTransactionStatus.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousTransactionStatus.getCreatedBy())
                                    .createdAt(previousTransactionStatus.getCreatedAt())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousTransactionStatus.getReqCreatedIP())
                                    .reqCreatedPort(previousTransactionStatus.getReqCreatedPort())
                                    .reqCreatedBrowser(previousTransactionStatus.getReqCreatedBrowser())
                                    .reqCreatedOS(previousTransactionStatus.getReqCreatedOS())
                                    .reqCreatedDevice(previousTransactionStatus.getReqCreatedDevice())
                                    .reqCreatedReferer(previousTransactionStatus.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousTransactionStatus.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousTransactionStatus.setDeletedBy(UUID.fromString(userId));
                            previousTransactionStatus.setReqDeletedIP(reqIp);
                            previousTransactionStatus.setReqDeletedPort(reqPort);
                            previousTransactionStatus.setReqDeletedBrowser(reqBrowser);
                            previousTransactionStatus.setReqDeletedOS(reqOs);
                            previousTransactionStatus.setReqDeletedDevice(reqDevice);
                            previousTransactionStatus.setReqDeletedReferer(reqReferer);

                            return transactionStatusRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedStatusEntity.getName(), transactionStatusUUID)
                                    .flatMap(checkName -> responseInfoMsg("Name already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> transactionStatusRepository.findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(updatedStatusEntity.getSlug(), transactionStatusUUID)
                                            .flatMap(checkSlug -> responseInfoMsg("Slug already Exists"))))
                                    .switchIfEmpty(Mono.defer(() ->
                                            transactionStatusRepository.save(previousTransactionStatus)
                                                    .then(transactionStatusRepository.save(updatedStatusEntity))
                                                    .flatMap(transactionStatusEntity1 -> responseSuccessMsg("Record Updated Successfully!", transactionStatusEntity1))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again!"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Transaction Status Record Does not exist."))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID transactionStatusUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

        return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionStatusUUID)
                .flatMap(transactionStatusEntity -> transactionRepository.findFirstByTransactionStatusUUIDAndDeletedAtIsNull(transactionStatusEntity.getUuid())
                        .flatMap(value2 -> responseInfoMsg("Unable to Delete Transaction Status as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> {

                            transactionStatusEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            transactionStatusEntity.setDeletedBy(UUID.fromString(userId));
                            transactionStatusEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            transactionStatusEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            transactionStatusEntity.setReqDeletedIP(reqIp);
                            transactionStatusEntity.setReqDeletedPort(reqPort);
                            transactionStatusEntity.setReqDeletedBrowser(reqBrowser);
                            transactionStatusEntity.setReqDeletedOS(reqOs);
                            transactionStatusEntity.setReqDeletedDevice(reqDevice);
                            transactionStatusEntity.setReqDeletedReferer(reqReferer);

                            return transactionStatusRepository.save(transactionStatusEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_transaction-status_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID transactionStatusUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                    return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionStatusUUID)
                            .flatMap(previousTransactionStatus -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousTransactionStatus.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                TransactionStatusEntity updatedStatusEntity = TransactionStatusEntity
                                        .builder()
                                        .uuid(previousTransactionStatus.getUuid())
                                        .name(previousTransactionStatus.getName())
                                        .slug(previousTransactionStatus.getSlug())
                                        .description(previousTransactionStatus.getDescription())
                                        .status(status == true ? true : false)
                                        .createdBy(previousTransactionStatus.getCreatedBy())
                                        .createdAt(previousTransactionStatus.getCreatedAt())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousTransactionStatus.getReqCreatedIP())
                                        .reqCreatedPort(previousTransactionStatus.getReqCreatedPort())
                                        .reqCreatedBrowser(previousTransactionStatus.getReqCreatedBrowser())
                                        .reqCreatedOS(previousTransactionStatus.getReqCreatedOS())
                                        .reqCreatedDevice(previousTransactionStatus.getReqCreatedDevice())
                                        .reqCreatedReferer(previousTransactionStatus.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousTransactionStatus.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousTransactionStatus.setDeletedBy(UUID.fromString(userId));
                                previousTransactionStatus.setReqDeletedIP(reqIp);
                                previousTransactionStatus.setReqDeletedPort(reqPort);
                                previousTransactionStatus.setReqDeletedBrowser(reqBrowser);
                                previousTransactionStatus.setReqDeletedOS(reqOs);
                                previousTransactionStatus.setReqDeletedDevice(reqDevice);
                                previousTransactionStatus.setReqDeletedReferer(reqReferer);

                                return transactionStatusRepository.save(previousTransactionStatus)
                                        .then(transactionStatusRepository.save(updatedStatusEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
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
