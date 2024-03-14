//package tuf.webscaf.app.http.handler;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;
//import tuf.webscaf.app.dbContext.master.repository.*;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveLedgerEntryEntity;
//import tuf.webscaf.app.dbContext.slave.repository.*;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.UUID;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//@Tag(name = "ledgerEntryHandler")
//public class LedgerEntryHandler {
//    @Autowired
//    LedgerEntryRepository ledgerEntryRepository;
//
//    @Autowired
//    SlaveLedgerEntryRepository slaveLedgerRepository;
//
//    @Autowired
//    CostCenterRepository costCenterRepository;
//
//    @Autowired
//    ProfitCenterRepository profitCenterRepository;
//
//    @Autowired
//    AccountRepository accountRepository;
//
//    @Autowired
//    TransactionRepository transactionRepository;
//
//    @Autowired
//    CustomResponse appresponse;
//
//    @Value("${server.zone}")
//    private String zone;
//
//    public Mono<ServerResponse> index(ServerRequest serverRequest) {
//
//        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
//
//        //Optional Query Parameter Based of Status
//        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();
//
//        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
//        if (size > 100) {
//            size = 100;
//        }
//        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
//        int page = pageRequest - 1;
//
//        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
//        Sort.Direction direction;
//        switch (d.toLowerCase()) {
//            case "asc":
//                direction = Sort.Direction.ASC;
//                break;
//            case "desc":
//                direction = Sort.Direction.DESC;
//                break;
//            default:
//                direction = Sort.Direction.ASC;
//        }
//
//        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
//
////        if (!status.isEmpty()) {
////            Flux<SlaveLedgerEntryEntity> slaveLedgerEntryEntityFlux = slaveLedgerRepository
////                    .findAllByDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status));
////            return slaveLedgerEntryEntityFlux
////                    .collectList()
////                    .flatMap(ledgerEntryEntity -> slaveLedgerRepository.countByDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status))
////                            .flatMap(count ->
////                            {
////                                if (ledgerEntryEntity.isEmpty()) {
////                                    return responseIndexInfoMsg("Record does not exist", count);
////                                } else {
////                                    return responseIndexSuccessMsg("All Records fetched successfully!", ledgerEntryEntity, count);
////                                }
////                            })
////                    ).switchIfEmpty(responseErrorMsg("Unable to Read Request"))
////                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
////        } else {
//        Flux<SlaveLedgerEntryEntity> slaveLedgerEntryEntityFlux = slaveLedgerRepository
//                .findAllByDeletedAtIsNull(pageable);
//        return slaveLedgerEntryEntityFlux
//                .collectList()
//                .flatMap(ledgerEntryEntity -> slaveLedgerRepository.countByDeletedAtIsNull()
//                        .flatMap(count ->
//                        {
//                            if (ledgerEntryEntity.isEmpty()) {
//                                return responseIndexInfoMsg("Record does not exist", count);
//                            } else {
//                                return responseIndexSuccessMsg("All Records fetched successfully!", ledgerEntryEntity, count);
//                            }
//                        })
//                ).switchIfEmpty(responseErrorMsg("Unable to Read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
////        }
//    }
//
//
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        final long ledgerEntryId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        return slaveLedgerRepository.findByIdAndDeletedAtIsNull(ledgerEntryId)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
//        UUID ledgerEntryUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return slaveLedgerRepository.findByUuidAndDeletedAtIsNull(ledgerEntryUUID)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseErrorMsg("Record does not exist. Please Contact Developer."));
//    }
//
//
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    LedgerEntryEntity ledgerEntryEntity = LedgerEntryEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .drAmount(new BigDecimal(value.getFirst("drAmount")))
//                            .description(value.getFirst("description").trim())
//                            .crAmount(new BigDecimal(value.getFirst("crAmount")))
////                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .costCenterUUID(UUID.fromString(value.getFirst("costCenterUUID").trim()))
//                            .profitCenterUUID(UUID.fromString(value.getFirst("profitCenterUUID").trim()))
//                            .accountUUID(UUID.fromString(value.getFirst("accountUUID").trim()))
//                            .transactionUUID(UUID.fromString(value.getFirst("transactionUUID").trim()))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userId))
//                            .build();
//
//                    return costCenterRepository.findByUuidAndDeletedAtIsNull(ledgerEntryEntity.getCostCenterUUID())
//                            .flatMap(costCenterEntity -> profitCenterRepository.findByUuidAndDeletedAtIsNull(ledgerEntryEntity.getProfitCenterUUID())
//                                    .flatMap(profitCenterEntity -> accountRepository.findByUuidAndDeletedAtIsNull(ledgerEntryEntity.getAccountUUID())
//                                            .flatMap(accountEntity -> transactionRepository.findByUuidAndDeletedAtIsNull(ledgerEntryEntity.getTransactionUUID())
//                                                    .flatMap(transactionEntity -> ledgerEntryRepository.save(ledgerEntryEntity)
//                                                            .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully!", value1))
//                                                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."))
//                                                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to store record.There is something wrong please try again.")))
//                                                    ).onErrorResume(err -> responseErrorMsg("Transaction does not exist.Please Contact Developer."))
//                                                    .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Transaction does not exist!")))
//                                            ).onErrorResume(err -> responseErrorMsg("Account does not exist.Please Contact Developer."))
//                                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Account does not exist!")))
//                                    ).onErrorResume(err -> responseErrorMsg("Profit Center does not exist.Please Contact Developer."))
//                                    .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Profit Center does not exist!")))
//                            ).onErrorResume(err -> responseErrorMsg("Cost Center does not exist.Please Contact Developer."))
//                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Cost Center does not exist!")));
//                }).onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."))
//                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request!")));
//    }
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        final long ledgerEntryId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> ledgerEntryRepository.findByIdAndDeletedAtIsNull(ledgerEntryId)
//                                .flatMap(entity -> costCenterRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("costCenterUUID").trim()))
//                                                .flatMap(costCenterEntity -> profitCenterRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("profitCenterUUID").trim()))
//                                                                .flatMap(profitCenterEntity -> accountRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("accountUUID").trim()))
//                                                                                .flatMap(accountEntity -> transactionRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("transactionUUID").trim()))
//                                                                                                .flatMap(transactionEntity -> {
//                                                                                                    entity.setDrAmount(new BigDecimal(value.getFirst("drAmount")));
//                                                                                                    entity.setCrAmount(new BigDecimal(value.getFirst("crAmount")));
//                                                                                                    entity.setDescription(value.getFirst("description").trim());
////                                                            entity.setStatus(Boolean.valueOf(value.getFirst("status")));
//                                                                                                    entity.setCostCenterUUID(costCenterEntity.getUuid());
//                                                                                                    entity.setProfitCenterUUID(profitCenterEntity.getUuid());
//                                                                                                    entity.setAccountUUID(accountEntity.getUuid());
//                                                                                                    entity.setTransactionUUID(transactionEntity.getUuid());
//                                                                                                    entity.setUpdatedBy(UUID.fromString(userId));
//                                                                                                    entity.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                                                                                    return ledgerEntryRepository.save(entity)
//                                                                                                            .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully!", value1))
//                                                                                                            .onErrorResume(err -> responseErrorMsg("Unable to update record.Please Contact Developer."))
//                                                                                                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to update record!")));
//                                                                                                }).onErrorResume(err -> responseErrorMsg("Transaction does not exist.Please Contact Developer."))
//                                                                                                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Transaction does not exist!")))
//                                                                                ).onErrorResume(err -> responseErrorMsg("Account does not exist.Please Contact Developer."))
//                                                                                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Account does not exist!")))
//                                                                ).onErrorResume(err -> responseErrorMsg("Profit Center does not exist.Please Contact Developer."))
//                                                                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Profit Center does not exist!")))
//                                                ).onErrorResume(err -> responseErrorMsg("Cost Center does not exist.Please Contact Developer."))
//                                                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Cost Center does not exist!")))
//                                ).onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
//                                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist!")))
//                ).onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."))
//                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request!")));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        final long ledgerEntryId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return ledgerEntryRepository.findByIdAndDeletedAtIsNull(ledgerEntryId)
//                .flatMap(profitCenterEntity -> {
//                    profitCenterEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    profitCenterEntity.setDeletedBy(UUID.fromString(userId));
//
//                    return ledgerEntryRepository.save(profitCenterEntity)
//                            .flatMap(value1 -> responseSuccessMsg("Record Deleted Successfully", value1))
//                            .switchIfEmpty(responseErrorMsg("Unable to Delete record.There is something wrong please try again."))
//                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
//                }).switchIfEmpty(responseErrorMsg("Record does not Exist."))
//                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
//    }
//
//
////    public Mono<ServerResponse> status(ServerRequest serverRequest) {
////        final long id = Long.parseLong(serverRequest.pathVariable("id"));
////        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
////        return formData.flatMap(value -> {
////                    Boolean status = Boolean.parseBoolean(value.getFirst("status"));
////                    return ledgerEntryRepository.findByIdAndDeletedAtIsNull(id)
////                            .flatMap(val -> {
////                                // If status is not Boolean value
////                                if (status != false && status != true) {
////                                    return responseInfoMsg("Status must be Active or InActive");
////                                }
////
////                                // If already same status exist in database.
////                                if (((val.getStatus() ? true : false) == status)) {
////                                    return responseWarningMsg("Record already exist with same status");
////                                }
////
////                                // update status
////                                val.setStatus(status == true ? true : false);
////                                return ledgerEntryRepository.save(val)
////                                        .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
////                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please Contact Developer."))
////                                        .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to update the status.There is something wrong Please try again.")));
////                            }).onErrorResume(err -> responseErrorMsg("Record does not exist!"))
////                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist!")));
////                }).onErrorResume(err -> responseErrorMsg("Unable to read the request!"))
////                .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to read the request!")));
////    }
//
//    public Mono<ServerResponse> responseInfoMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.INFO,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.INFO,
//                        msg
//                )
//        );
//
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                totalDataRowsWithFilter,
//                0L,
//                messages,
//                Mono.empty()
//
//        );
//    }
//
//    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg)
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                totalDataRowsWithFilter,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//
//    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg)
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//
//    public Mono<ServerResponse> responseErrorMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.ERROR,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.BAD_REQUEST.value(),
//                HttpStatus.BAD_REQUEST.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//    public Mono<ServerResponse> responseWarningMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.WARNING,
//                        msg)
//        );
//
//
//        return appresponse.set(
//                HttpStatus.UNPROCESSABLE_ENTITY.value(),
//                HttpStatus.UNPROCESSABLE_ENTITY.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//}
