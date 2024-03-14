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
//
//
//import tuf.webscaf.app.dbContext.master.entity.CashFlowAdjustmentEntity;
//
//import tuf.webscaf.app.dbContext.master.repository.CashFlowAdjustmentRepository;
//
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowAdjustmentEntity;
//import tuf.webscaf.app.dbContext.slave.repository.SlaveCashFlowAdjustmentRepository;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.UUID;
//import java.util.List;
//
//@Component
//@Tag(name = "cashFlowAdjustmentHandler")
//public class CashFlowAdjustmentHandler {
//    @Autowired
//    CashFlowAdjustmentRepository cashFlowAdjustmentRepository;
//
//    @Autowired
//    SlaveCashFlowAdjustmentRepository slaveRepository;
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
//        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
//        Flux<SlaveCashFlowAdjustmentEntity> slaveCashFlowAdjustmentEntityFlux = slaveRepository
//                .findAllByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord);
//        return slaveCashFlowAdjustmentEntityFlux
//                .collectList()
//                .flatMap(cashFlowAdjustmentEntityList -> slaveRepository
//                        .countByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord)
//                        .flatMap(count -> {
//                            if (cashFlowAdjustmentEntityList.isEmpty()) {
//                                return responseIndexInfoMsg("Record does not exist", count);
//                            } else {
//                                return responseIndexSuccessMsg("All Records fetched successfully", cashFlowAdjustmentEntityList, count);
//                            }
//
//                        }));
//    }
//
//
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        final long cashFlowAdjustmentId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        return slaveRepository.findByIdAndDeletedAtIsNull(cashFlowAdjustmentId)
//                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
//        UUID cashFlowAdjustmentUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return slaveRepository.findByUuidAndDeletedAtIsNull(cashFlowAdjustmentUUID)
//                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
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
//                    CashFlowAdjustmentEntity cashFlowAdjustmentEntity = CashFlowAdjustmentEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .drAmount(Long.valueOf(value.getFirst("drAmount")))
//                            .crAmount(Long.valueOf(value.getFirst("crAmount")))
//                            .description(value.getFirst("description").trim())
//                            .accountId(Long.valueOf(value.getFirst("accountId")))
//                            .transactionId(Long.valueOf(value.getFirst("transactionId")))
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userId))
//                            .build();
//
//                    return cashFlowAdjustmentRepository.save(cashFlowAdjustmentEntity)
//                            .flatMap(value1 -> responseSuccessMsg("Record stored successfully", value1))
//                            .switchIfEmpty(responseErrorMsg("Unable to store record.There is something wrong please try again."))
//                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
//                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        final long Id = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> cashFlowAdjustmentRepository.findByIdAndDeletedAtIsNull(Id)
//                        .flatMap(entity -> {
//                            entity.setDrAmount(Long.valueOf(value.getFirst("drAmount")));
//                            entity.setCrAmount(Long.valueOf(value.getFirst("crAmount")));
//                            entity.setDescription(value.getFirst("description").trim());
//                            entity.setAccountId(Long.valueOf(value.getFirst("accountId")));
//                            entity.setTransactionId(Long.valueOf(value.getFirst("transactionId")));
//                            entity.setStatus(Boolean.valueOf(value.getFirst("status")));
//                            entity.setUpdatedBy(UUID.fromString(userId));
//                            entity.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                            return cashFlowAdjustmentRepository.save(entity)
//                                    .flatMap(cashEntityDB -> responseSuccessMsg("Record updated successfully!", cashEntityDB))
//                                    .switchIfEmpty(responseErrorMsg("Unable to Update Record.There is something wrong please try again."))
//                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."));
//
//                        }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                        .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."))
//
//                ).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        final long cashFlowAdjustmentId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return cashFlowAdjustmentRepository.findByIdAndDeletedAtIsNull(cashFlowAdjustmentId)
//                .flatMap(cashFlowAdjustmentEntity -> {
//                    cashFlowAdjustmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    cashFlowAdjustmentEntity.setDeletedBy(UUID.fromString(userId));
//                    return cashFlowAdjustmentRepository.save(cashFlowAdjustmentEntity)
//                            .flatMap(value1 -> responseSuccessMsg("Record deleted successfully", value1))
//                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
//                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
//                }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        final long id = Long.parseLong(serverRequest.pathVariable("id"));
//        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
//        return formData.flatMap(value -> {
//                    Boolean status = Boolean.parseBoolean(value.getFirst("status"));
//                    return cashFlowAdjustmentRepository.findByIdAndDeletedAtIsNull(id)
//                            .flatMap(val -> {
//                                // If status is not Boolean value
//                                if (status != false && status != true) {
//                                    return responseErrorMsg("Status must be Active or InActive");
//                                }
//
//                                // If already same status exist in database.
//                                if (((val.getStatus() ? true : false) == status)) {
//                                    return responseWarningMsg("Record already exist with same status");
//                                }
//
//                                // update status
//                                val.setStatus(status == true ? true : false);
//                                return cashFlowAdjustmentRepository.save(val)
//                                        .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
//                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again"))
//                                        .onErrorResume(err -> responseInfoMsg("Unable to update the status. Please Contact Developer."));
//                            }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                            .onErrorResume(err -> responseInfoMsg("Record does not exist. Please Contact Developer."));
//                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please Contact Developer."));
//    }
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
//}
