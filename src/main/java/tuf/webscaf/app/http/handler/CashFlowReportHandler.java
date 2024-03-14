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
//import tuf.webscaf.app.dbContext.master.entity.CashFlowLineEntity;
//import tuf.webscaf.app.dbContext.master.entity.CashFlowReportEntity;
//import tuf.webscaf.app.dbContext.master.entity.FlowLineTypeEntity;
//import tuf.webscaf.app.dbContext.master.repository.CashFlowReportRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowReportEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveFlowLineTypeEntity;
//import tuf.webscaf.app.dbContext.slave.repository.SlaveCashFlowReportRepository;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//import tuf.webscaf.helper.SlugifyHelper;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.UUID;
//import java.util.List;
//import java.util.regex.Pattern;
//
//@Component
//@Tag(name = "cashFlowReportHandler")
//public class CashFlowReportHandler {
//    @Autowired
//    CashFlowReportRepository cashFlowReportRepository;
//
//    @Autowired
//    SlaveCashFlowReportRepository slaveCashFlowReportRepository;
//
//    @Autowired
//    SlugifyHelper slugifyHelper;
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
//        Flux<SlaveCashFlowReportEntity> slaveCashReportFlux = slaveCashFlowReportRepository
//                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//        return slaveCashReportFlux
//                .collectList()
//                .flatMap(cashFlowAdjustmentEntityList -> slaveCashFlowReportRepository
//                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
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
//        final long accountGroupId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        return slaveCashFlowReportRepository.findByIdAndDeletedAtIsNull(accountGroupId)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
//        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return slaveCashFlowReportRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
//                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
//                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
//    }
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
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    CashFlowReportEntity cashFlowReportEntity = CashFlowReportEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name"))
//                            .description(value.getFirst("description"))
//                            .slug(slugifyHelper.slugify(value.getFirst("name")))
//                            .createdBy(UUID.fromString(userId))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .build();
//
//                    return cashFlowReportRepository.save(cashFlowReportEntity)
//                            .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
//                            .switchIfEmpty(responseErrorMsg("Unable to store record.There is something wrong please try again."))
//                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
//                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//
//
//    }
//
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        final long cashFlowReportId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//        return serverRequest.formData()
//                .flatMap(value -> cashFlowReportRepository.findByIdAndDeletedAtIsNull(cashFlowReportId)
//                        .flatMap(cashFlowReport -> {
//                            cashFlowReport.setName(value.getFirst("name"));
//                            cashFlowReport.setUpdatedBy(UUID.fromString(userId));
//                            cashFlowReport.setDescription(value.getFirst("description"));
//                            cashFlowReport.setSlug(slugifyHelper.slugify(value.getFirst("name")));
//                            cashFlowReport.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
//                            return cashFlowReportRepository.save(cashFlowReport)
//                                    .flatMap(entity -> responseSuccessMsg("Record Updated Successfully", entity))
//                                    .switchIfEmpty(responseErrorMsg("Unable to Update Record.There is something wrong please try again"))
//                                    .onErrorResume(ex -> responseInfoMsg("Unable to Update Record.Please Contact Developer."));
//                        }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
//                        .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."))
//                ).switchIfEmpty(responseErrorMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        final long cashFlowReportId = Long.parseLong(serverRequest.pathVariable("id"));
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//        return cashFlowReportRepository.findByIdAndDeletedAtIsNull(cashFlowReportId)
//                .flatMap(cashFlowReportEntity -> {
//                    cashFlowReportEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    cashFlowReportEntity.setDeletedBy(UUID.fromString(userId));
//                    return cashFlowReportRepository.save(cashFlowReportEntity)
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
//                    return cashFlowReportRepository.findByIdAndDeletedAtIsNull(id)
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
//                                return cashFlowReportRepository.save(val)
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
//
//    public Mono<ServerResponse> findSlug(ServerRequest serverRequest) {
//
//        String searchSlug = serverRequest.queryParam("slug").map(String::toString).orElse("");
//        return cashFlowReportRepository.findFirstBySlugAndDeletedAtIsNull(searchSlug)
//                .flatMap(value1 -> responseWarningMsg("Slug Already Exists"))
//                .switchIfEmpty(Mono.defer(() -> {
//                    String msg = "Unique Slug";
//                    if (!slugifyHelper.validateSlug(searchSlug)) {
//                        msg = "Invalid Slug";
//                    }
//                    return responseInfoMsg(msg);
//                }));
//    }
//
//}
