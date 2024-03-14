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
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureFileExtensionEntity;
import tuf.webscaf.app.dbContext.master.repository.DocumentNatureFileExtensionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureFileExtensionEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveDocumentNatureFileExtensionRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;

@Component
@Tag(name = "documentNatureFileExtensionHandler")
public class DocumentNatureFileExtensionHandler {
    @Autowired
    DocumentNatureFileExtensionRepository documentNatureFileExtensionRepository;

    @Autowired
    SlaveDocumentNatureFileExtensionRepository slaveDocumentNatureFileExtensionRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    public Mono<ServerResponse> index(ServerRequest serverRequest) {

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        Flux<SlaveDocumentNatureFileExtensionEntity> slaveDocumentExtensionEntityFlux = slaveDocumentNatureFileExtensionRepository
                .findAllByExtensionContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
        return slaveDocumentExtensionEntityFlux
                .collectList()
                .flatMap(cashFlowAdjustmentEntityList -> slaveDocumentNatureFileExtensionRepository
                        .countByExtensionContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                        .flatMap(count -> {
                            if (cashFlowAdjustmentEntityList.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records fetched successfully", cashFlowAdjustmentEntityList, count);
                            }

                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long accountGroupId = Long.parseLong(serverRequest.pathVariable("id"));

        return slaveDocumentNatureFileExtensionRepository.findByIdAndDeletedAtIsNull(accountGroupId)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }

    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
        UUID accountGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveDocumentNatureFileExtensionRepository.findByUuidAndDeletedAtIsNull(accountGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
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
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> {
                    DocumentNatureFileExtensionEntity documentNatureEntity = DocumentNatureFileExtensionEntity.builder()
                            .uuid(UUID.randomUUID())
                            .extension(value.getFirst("extension"))
                            .description(value.getFirst("description"))
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
                    return documentNatureFileExtensionRepository
                            .save(documentNatureEntity)
                            .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                            .switchIfEmpty(responseErrorMsg("Unable to store record.There is something wrong please try again."))
                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
}

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final long documentNatureId = Long.parseLong(serverRequest.pathVariable("id"));
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
                .flatMap(documentNatureExtensionEntity -> documentNatureFileExtensionRepository.findByIdAndDeletedAtIsNull(documentNatureId)
                        .flatMap(documentNatureEntityDB -> {
                            documentNatureEntityDB.setExtension(documentNatureExtensionEntity.getFirst("extension"));
                            documentNatureEntityDB.setDescription(documentNatureExtensionEntity.getFirst("description"));
                            documentNatureEntityDB.setUpdatedBy(UUID.fromString(userId));
                            documentNatureEntityDB.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
                            documentNatureEntityDB.setReqCreatedIP(documentNatureExtensionEntity.getFirst("reqCreatedIP"));
                            documentNatureEntityDB.setReqCreatedPort(documentNatureExtensionEntity.getFirst("ReqCreatedPort"));
                            documentNatureEntityDB.setReqCreatedBrowser(documentNatureExtensionEntity.getFirst("reqCreatedBrowser"));
                            documentNatureEntityDB.setReqCreatedOS(documentNatureExtensionEntity.getFirst("reqCreatedOS"));
                            documentNatureEntityDB.setReqCreatedDevice(documentNatureExtensionEntity.getFirst("reqCreatedDevice"));
                            documentNatureEntityDB.setReqCreatedReferer(documentNatureExtensionEntity.getFirst("reqCreatedReferer"));
                            documentNatureEntityDB.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            documentNatureEntityDB.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            documentNatureEntityDB.setReqUpdatedIP(reqIp);
                            documentNatureEntityDB.setReqUpdatedPort(reqPort);
                            documentNatureEntityDB.setReqUpdatedBrowser(reqBrowser);
                            documentNatureEntityDB.setReqUpdatedOS(reqOs);
                            documentNatureEntityDB.setReqUpdatedDevice(reqDevice);
                            documentNatureEntityDB.setReqUpdatedReferer(reqReferer);
                            return documentNatureFileExtensionRepository.save(documentNatureEntityDB)
                                    .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully", value1))
                                    .switchIfEmpty(responseErrorMsg("Unable to update record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to update record.Please Contact Developer."));
                        }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                        .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."))
                ).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final long docFileExtensionId = Long.parseLong(serverRequest.pathVariable("id"));
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

        return documentNatureFileExtensionRepository.findByIdAndDeletedAtIsNull(docFileExtensionId)
                .flatMap(fileExtEntity -> {
                    fileExtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    fileExtEntity.setDeletedBy(UUID.fromString(userId));
                    fileExtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    fileExtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    fileExtEntity.setReqDeletedIP(reqIp);
                    fileExtEntity.setReqDeletedPort(reqPort);
                    fileExtEntity.setReqDeletedBrowser(reqBrowser);
                    fileExtEntity.setReqDeletedOS(reqOs);
                    fileExtEntity.setReqDeletedDevice(reqDevice);
                    fileExtEntity.setReqDeletedReferer(reqReferer);
                    return documentNatureFileExtensionRepository.save(fileExtEntity)
                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                }).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }


    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final long id = Long.parseLong(serverRequest.pathVariable("id"));
        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
        return formData.flatMap(value -> {
                    boolean status = Boolean.parseBoolean(value.getFirst("status"));
                    return documentNatureFileExtensionRepository.findByIdAndDeletedAtIsNull(id)
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
                                return documentNatureFileExtensionRepository.save(val)
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again"))
                                        .onErrorResume(err -> responseInfoMsg("Unable to update the status. Please Contact Developer."));
                            }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                            .onErrorResume(err -> responseInfoMsg("Record does not exist. Please Contact Developer."));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please Contact Developer."));
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
