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
//import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeLineEntity;
//import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
//import tuf.webscaf.app.dbContext.master.repository.BalanceAndIncomeLineRepository;
//import tuf.webscaf.app.dbContext.master.repository.BalanceAndIncomeReportRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeLineEntity;
//import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
//import tuf.webscaf.app.dbContext.slave.repository.SlaveBalanceAndIncomeLineRepository;
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
//@Tag(name = "balanceAndIncomeLineHandler")
//public class BalanceAndIncomeLineHandler {
//
//    @Autowired
//    BalanceAndIncomeLineRepository balanceAndIncomeLineRepository;
//
//    @Autowired
//    BalanceAndIncomeReportRepository balanceAndIncomeReportRepository;
//
//    @Autowired
//    AccountRepository accountRepository;
//
//    @Autowired
//    SlaveBalanceAndIncomeLineRepository slaveBalanceAndIncomeLineRepository;
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
//        if (page < 0) {
//            return responseErrorMsg("Invalid Page No");
//        }
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
//        Flux<SlaveBalanceAndIncomeLineEntity> slaveBalanceAndIncomeLineEntityFlux = slaveBalanceAndIncomeLineRepository
//                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//
//        return slaveBalanceAndIncomeLineEntityFlux
//                .collectList()
//                .flatMap(slaveBalanceAndIncomeEntity -> slaveBalanceAndIncomeLineRepository
//                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
//                        .flatMap(count -> {
//
//                            if (slaveBalanceAndIncomeEntity.isEmpty()) {
//                                return responseIndexInfoMsg("Record does not exist", count);
//                            } else {
//                                return responseIndexSuccessMsg("All Records fetched successfully", slaveBalanceAndIncomeEntity, count);
//                            }
//                        }));
//    }
//
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        final long balanceAndIncomeLineId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        return slaveBalanceAndIncomeLineRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeLineId)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .switchIfEmpty(responseInfoMsg("Record does not exist"))
//                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
//        UUID balanceAndIncomeLineUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return slaveBalanceAndIncomeLineRepository.findByUuidAndDeletedAtIsNull(balanceAndIncomeLineUUID)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .switchIfEmpty(responseInfoMsg("Record does not exist"))
//                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseInfoMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseInfoMsg("Unknown user");
//
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    BalanceAndIncomeLineEntity balanceAndIncomeLineEntity = BalanceAndIncomeLineEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name").trim())
//                            .description(value.getFirst("description").trim())
//                            .lineType(value.getFirst("lineType"))
//                            .linePosition(value.getFirst("linePosition"))
//                            .lineValueType(value.getFirst("lineValueType"))
//                            .lineIndentation(value.getFirst("lineIndentation"))
//                            .lineSide(value.getFirst("lineSide"))
//                            .visiblePositionIndex(Integer.valueOf(value.getFirst("visiblePositionIndex")))
//                            .lineTextShow(value.getFirst("lineTextShow"))
//                            .printedNo(value.getFirst("printedNo"))
//                            .totalOfLinePositions(value.getFirst("totalOfLinePositions"))
//                            .balanceIncomeReportId(Long.valueOf(value.getFirst("balanceIncomeReportId")))
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userId))
//                            .build();
//
//                    return balanceAndIncomeLineRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(balanceAndIncomeLineEntity.getName())
//                            .flatMap(checkName -> responseInfoMsg("The Entered Name already exists"))
//                            .switchIfEmpty(Mono.defer(() -> balanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeLineEntity.getBalanceIncomeReportId())
//                                    .flatMap(value1 -> balanceAndIncomeLineRepository.save(balanceAndIncomeLineEntity)
//                                            .flatMap(balanceLineDB -> responseSuccessMsg("Record stored successfully!", balanceLineDB))
//                                            .switchIfEmpty(responseErrorMsg("There is something wrong. Please Try Again!"))
//                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.There is something wrong.Please Try Again!"))
//                                    ).switchIfEmpty(responseErrorMsg("Balance Income Report Does not exist"))
//                                    .onErrorResume(ex -> responseErrorMsg("Create Balance Income Report First"))
//                            ));
//                }).onErrorResume(err -> responseErrorMsg("Unable to read the request!"))
//                .switchIfEmpty(responseErrorMsg("Unable to read the request!"));
//    }
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        final long balanceAndIncomeLineId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseInfoMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseInfoMsg("Unknown user");
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> balanceAndIncomeLineRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeLineId)
//                        .flatMap(entity -> {
//
//                            entity.setName(value.getFirst("name").trim());
//                            entity.setDescription(value.getFirst("description").trim());
//                            entity.setVisiblePositionIndex(Integer.valueOf(value.getFirst("visiblePositionIndex")));
//                            entity.setLineTextShow(value.getFirst("lineTextShow"));
//                            entity.setPrintedNo(value.getFirst("printedNo"));
//                            entity.setLineType(value.getFirst("lineType"));
//                            entity.setLinePosition(value.getFirst("linePosition"));
//                            entity.setLineValueType(value.getFirst("lineValueType"));
//                            entity.setLineIndentation(value.getFirst("lineIndentation"));
//                            entity.setLineSide(value.getFirst("lineSide"));
//                            entity.setTotalOfLinePositions(value.getFirst("totalOfLinePositions"));
//                            entity.setBalanceIncomeReportId(Long.valueOf(value.getFirst("balanceIncomeReportId")));
//                            entity.setStatus(Boolean.valueOf(value.getFirst("status")));
//                            entity.setUpdatedBy(UUID.fromString(userId));
//                            entity.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                            return balanceAndIncomeLineRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(entity.getName(), balanceAndIncomeLineId)
//                                    .flatMap(checkName -> responseInfoMsg("The Entered Name already exists"))
//                                    .switchIfEmpty(Mono.defer(() -> balanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(entity.getBalanceIncomeReportId())
//                                            .flatMap(value1 -> balanceAndIncomeLineRepository.save(entity)
//                                                    .flatMap(balanceLineDB -> responseSuccessMsg("Record Updated successfully!", balanceLineDB))
//                                                    .switchIfEmpty(responseErrorMsg("There is something wrong.Please Try Again!"))
//                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.There is something wrong.Please Try Again!"))
//                                            ).switchIfEmpty(responseErrorMsg("Balance Income Report Does not exist"))
//                                            .onErrorResume(ex -> responseErrorMsg("Create Balance Income Report First"))
//                                    ));
//                        }).onErrorResume(err -> responseErrorMsg("Record does not exist!"))
//                        .switchIfEmpty(responseInfoMsg("Record does not exist!"))
//                ).onErrorResume(err -> responseErrorMsg("Unable to read the request!"))
//                .switchIfEmpty(responseInfoMsg("Unable to read the request!"));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        final long balanceAndIncomeLineId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//        if (userId == null) {
//            return responseInfoMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseInfoMsg("Unknown user");
//        }
//
//        return balanceAndIncomeLineRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeLineId)
//                .flatMap(balanceAndIncomeLineEntity -> {
//                    balanceAndIncomeLineEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    balanceAndIncomeLineEntity.setDeletedBy(UUID.fromString(userId));
//                    return balanceAndIncomeLineRepository.save(balanceAndIncomeLineEntity)
//                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
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
//                    return balanceAndIncomeLineRepository.findByIdAndDeletedAtIsNull(id)
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
//                                return balanceAndIncomeLineRepository.save(val)
//                                        .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
//                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status!"))
//                                        .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to update the status!")));
//                            }).onErrorResume(err -> responseErrorMsg("Record does not exist!"))
//                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist!")));
//                }).onErrorResume(err -> responseErrorMsg("Unable to read the request!"))
//                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request!")));
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
//
//}
