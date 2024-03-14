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
import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeReportEntity;

import tuf.webscaf.app.dbContext.master.repository.BalanceAndIncomeLineRepository;
import tuf.webscaf.app.dbContext.master.repository.BalanceAndIncomeReportRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeReportEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveBalanceAndIncomeReportRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;

@Component
@Tag(name = "balanceAndIncomeReportHandler")
public class BalanceAndIncomeReportHandler {

    @Autowired
    BalanceAndIncomeReportRepository balanceAndIncomeReportRepository;

    @Autowired
    BalanceAndIncomeLineRepository balanceAndIncomeLineRepository;

    @Autowired
    SlaveBalanceAndIncomeReportRepository slaveBalanceAndIncomeReportRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.zone}")
    private String zone;

    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        Flux<SlaveBalanceAndIncomeReportEntity> slaveBalanceAndIncomeReportEntityFlux = slaveBalanceAndIncomeReportRepository
                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

        return slaveBalanceAndIncomeReportEntityFlux
                .collectList()
                .flatMap(slaveBalanceAndIncomeReportEntity -> slaveBalanceAndIncomeReportRepository
                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                        .flatMap(count -> {

                            if (slaveBalanceAndIncomeReportEntity.isEmpty()) {

                                return responseIndexInfoMsg("Record does not exist", count);

                            } else {

                                return responseIndexSuccessMsg("All Records fetched successfully!", slaveBalanceAndIncomeReportEntity, count);
                            }
                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long balanceAndIncomeReportId = Long.parseLong(serverRequest.pathVariable("id"));

        return slaveBalanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeReportId)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
        UUID balanceAndIncomeReportUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveBalanceAndIncomeReportRepository.findByUuidAndDeletedAtIsNull(balanceAndIncomeReportUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

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
            return responseInfoMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseInfoMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> {
                    BalanceAndIncomeReportEntity balanceReportEntityDB = BalanceAndIncomeReportEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .slug(value.getFirst("slug").trim())
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

                    return balanceAndIncomeReportRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(balanceReportEntityDB.getName())
                            .flatMap(checkName -> responseInfoMsg("The Entered Name already exists"))
                            .switchIfEmpty(Mono.defer(() -> balanceAndIncomeReportRepository.findFirstBySlugAndDeletedAtIsNull(balanceReportEntityDB.getSlug())
                                    .flatMap(checkSlug -> responseInfoMsg("The Entered Slug already Exists!"))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        Mono<ServerResponse> serverResponse = Mono.empty();
                                        if (!slugifyHelper.validateSlug(balanceReportEntityDB.getSlug())) {
                                            serverResponse = responseErrorMsg("Invalid Slug!");
                                        }
                                        return serverResponse;
                                    }))
                            )).switchIfEmpty(Mono.defer(() -> balanceAndIncomeReportRepository.save(balanceReportEntityDB)
                                    .flatMap(ReportDB -> responseSuccessMsg("Record stored successfully!", ReportDB))
                                    .switchIfEmpty(Mono.defer(() -> responseErrorMsg("There is something wrong. Please Try Again!")))
                                    .onErrorResume(err -> responseErrorMsg("There is something wrong. Please Try Again!"))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!")).onErrorResume(err -> responseErrorMsg("Unable to read the request!"));
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final long balanceReportId = Long.parseLong(serverRequest.pathVariable("id"));

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
            return responseInfoMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseInfoMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> balanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(balanceReportId)
                        .flatMap(entity -> {

                            entity.setName(value.getFirst("name").trim());
                            entity.setDescription(value.getFirst("description").trim());
                            entity.setSlug(value.getFirst("slug").trim());
                            entity.setStatus((Boolean.valueOf(value.getFirst("status"))));
                            entity.setUpdatedBy(UUID.fromString(userId));
                            entity.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));

                            return balanceAndIncomeReportRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(entity.getName(), balanceReportId)
                                    .flatMap(checkName -> responseInfoMsg("The Entered Name already Exists"))
                                    .switchIfEmpty(Mono.defer(() ->
                                            balanceAndIncomeReportRepository.findFirstBySlugAndDeletedAtIsNullAndIdIsNot(entity.getSlug(), balanceReportId)
                                                    .flatMap(checkSlug -> responseInfoMsg("The Entered Slug already Exists!"))
                                                    .switchIfEmpty(Mono.defer(() -> {
                                                        Mono<ServerResponse> serverResponse = Mono.empty();

                                                        if (!slugifyHelper.validateSlug(value.getFirst("slug"))) {
                                                            serverResponse = responseErrorMsg("Invalid Slug!");
                                                        }

                                                        return serverResponse;
                                                    }))
                                    )).switchIfEmpty(Mono.defer(() ->
                                            balanceAndIncomeReportRepository.save(entity)
                                                    .flatMap(ReportDB -> responseSuccessMsg("Record Updated Successfully!", ReportDB))
                                                    .switchIfEmpty(responseErrorMsg("There is something wrong. Please Try Again!"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.There is something wrong please try again!"))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist!"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist!"))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request!"));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final long balanceAndIncomeReportId = Long.parseLong(serverRequest.pathVariable("id"));

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
            return responseInfoMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseInfoMsg("Unknown user");

        }

        return balanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(balanceAndIncomeReportId)
                .flatMap(balanceAndIncomeReportEntity -> balanceAndIncomeLineRepository.findFirstByBalanceIncomeReportIdAndDeletedAtIsNullAndDeletedAtIsNull(balanceAndIncomeReportId)
                        .flatMap(checkId -> responseInfoMsg("Unable to Delete Record as the Reference Exists!"))
                        .switchIfEmpty(Mono.defer(() -> {
                            balanceAndIncomeReportEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            balanceAndIncomeReportEntity.setDeletedBy(UUID.fromString(userId));
                            balanceAndIncomeReportEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            balanceAndIncomeReportEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            balanceAndIncomeReportEntity.setReqDeletedIP(reqIp);
                            balanceAndIncomeReportEntity.setReqDeletedPort(reqPort);
                            balanceAndIncomeReportEntity.setReqDeletedBrowser(reqBrowser);
                            balanceAndIncomeReportEntity.setReqDeletedOS(reqOs);
                            balanceAndIncomeReportEntity.setReqDeletedDevice(reqDevice);
                            balanceAndIncomeReportEntity.setReqDeletedReferer(reqReferer);

                            return balanceAndIncomeReportRepository.save(balanceAndIncomeReportEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }


    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final long id = Long.parseLong(serverRequest.pathVariable("id"));

        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
        return formData.flatMap(value -> {
                    Boolean status = Boolean.parseBoolean(value.getFirst("status"));
                    return balanceAndIncomeReportRepository.findByIdAndDeletedAtIsNull(id)
                            .flatMap(val -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseErrorMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((val.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                // update status
                                val.setStatus(status == true ? true : false);
                                return balanceAndIncomeReportRepository.save(val)
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status!"))
                                        .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to update the status!")));
                            }).onErrorResume(err -> responseErrorMsg("Record does not exist!"))
                            .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist!")));
                }).onErrorResume(err -> responseErrorMsg("Unable to read the request!"))
                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request!")));
    }

//    public Mono<ServerResponse> findSlug(ServerRequest serverRequest) {
//
//        String searchSlug = serverRequest.queryParam("slug").map(String::toString).orElse("");
//        return serverRequest.formData()
//                .flatMap(value -> slaveBalanceAndIncomeReportRepository.findBySlugAndDeletedAtIsNull(searchSlug)
//                        .flatMap(value1 -> responseWarningMsg("Slug Already Exists!"))
//                        .switchIfEmpty(Mono.defer(() -> {
//                            String msg = "Unique Slug!";
//                            if (!slugifyHelper.validateSlug(searchSlug)) {
//                                msg = "Invalid Slug!";
//                            }
//                            return responseInfoMsg(msg);
//                        }))
//                );
//    }

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
