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
//import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
//import tuf.webscaf.app.dbContext.master.entity.CashFlowLineEntity;
//
//import tuf.webscaf.app.dbContext.master.repository.CashFlowLineRepository;
//
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowLineEntity;
//
//import tuf.webscaf.app.dbContext.slave.repository.SlaveCashFlowLineRepository;
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
//@Tag(name = "cashFlowLineHandler")
//public class CashFlowLineHandler {
//    @Autowired
//    CashFlowLineRepository cashFlowLineRepository;
//
//    @Autowired
//    SlaveCashFlowLineRepository slaveCashFlowLineRepository;
//
//    @Value("${server.zone}")
//    private String zone;
//
//    @Autowired
//    CustomResponse appresponse;
//
//
//    public Mono<ServerResponse> index(ServerRequest serverRequest) {
//
//        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
//
//        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
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
//        Flux<SlaveCashFlowLineEntity> slaveCashFlowLineEntityFlux = slaveCashFlowLineRepository
//                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//        return slaveCashFlowLineEntityFlux
//                .collectList()
//                .flatMap(cashFlowAdjustmentEntityList -> slaveCashFlowLineRepository
//                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
//                        .flatMap(count -> {
//                            if (cashFlowAdjustmentEntityList.isEmpty()) {
//                                return responseIndexInfoMsg("Record does not exist", count);
//                            } else {
//                                return responseIndexSuccessMsg("All Records fetched successfully!", cashFlowAdjustmentEntityList, count);
//                            }
//
//                        }));
//    }
//
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        final long accountGroupId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        return slaveCashFlowLineRepository.findByIdAndDeletedAtIsNull(accountGroupId)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
//        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return slaveCashFlowLineRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    CashFlowLineEntity cashFlowLineEntity = CashFlowLineEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name"))
//                            .visiblePositionIndex(Integer.valueOf(value.getFirst("visiblePositionIndex")))
//                            .description(value.getFirst("description"))
//                            .printedNo(value.getFirst("printedNo"))
//                            .lineTextShow(value.getFirst("lineTextShow"))
//                            .totalOfLinePositions(value.getFirst("totalOfLinePositions"))
//                            .cashFlowReportId(Long.valueOf(value.getFirst("cashFlowReportId")))
//                            .linePosition(value.getFirst("linePosition"))
//                            .lineValueType(value.getFirst("lineValueType"))
//                            .lineIntendentation(value.getFirst("lineIntendentation"))
//                            .createdBy(UUID.fromString(userId))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .build();
//                    return cashFlowLineRepository
//                            .save(cashFlowLineEntity)
//                            .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
//                            .switchIfEmpty(responseErrorMsg("Unable to store record.There is something wrong please try again."))
//                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
//
//                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }
//
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        final long cashFlowLineId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//        return serverRequest.formData()
//                .flatMap(value -> cashFlowLineRepository.findByIdAndDeletedAtIsNull(cashFlowLineId)
//                                .flatMap(cashFlowLine -> {
////                                    cashFlowLine.setVisible_position_index(Integer.valueOf(value.getFirst("visible_position_index")));
//                                    cashFlowLine.setName(value.getFirst("name"));
//                                    cashFlowLine.setDescription(value.getFirst("description"));
//                                    cashFlowLine.setPrintedNo(value.getFirst("printedNo"));
//                                    cashFlowLine.setLineTextShow(value.getFirst("lineTextShow"));
//                                    cashFlowLine.setLinePosition(value.getFirst("linePosition"));
//                                    cashFlowLine.setLineValueType(value.getFirst("lineValueType"));
//                                    cashFlowLine.setLineIntendentation(value.getFirst("lineIntendentation"));
//                                    cashFlowLine.setTotalOfLinePositions(value.getFirst("totalOfLinePositions"));
//                                    cashFlowLine.setCashFlowReportId(Long.valueOf(value.getFirst("cashFlowReportId")));
////                                    cashFlowLine.setUpdatedBy(Long.valueOf(value.getFirst("updatedBy")));
//                                    cashFlowLine.setUpdatedBy(UUID.fromString(userId));
//                                    cashFlowLine.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                    return cashFlowLineRepository.save(cashFlowLine)
//                                            .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully", value1))
//                                            .switchIfEmpty(responseErrorMsg("Unable to update record.There is something wrong please try again."))
//                                            .onErrorResume(err -> responseErrorMsg("Unable to update record.Please Contact Developer."));
//                                }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."))
//                ).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//
//        final long cashFlowLineId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//        return cashFlowLineRepository.findByIdAndDeletedAtIsNull(cashFlowLineId)
//                .flatMap(cashFlowEntity -> {
//                    cashFlowEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    cashFlowEntity.setDeletedBy(UUID.fromString(userId));
//                    return cashFlowLineRepository.save(cashFlowEntity)
//                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
//                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
//                }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        final long id = Long.parseLong(serverRequest.pathVariable("id"));
//        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
//        return formData.flatMap(value -> {
//                    Boolean status = Boolean.parseBoolean(value.getFirst("status"));
//                    return cashFlowLineRepository.findByIdAndDeletedAtIsNull(id)
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
//                                return cashFlowLineRepository.save(val)
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
