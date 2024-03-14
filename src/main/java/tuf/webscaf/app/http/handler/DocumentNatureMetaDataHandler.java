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
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaDataEntity;
import tuf.webscaf.app.dbContext.master.repository.DocumentNatureMetaDataRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaDataEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveDocumentNatureMetaDataRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Tag(name = "documentNatureMetaDataHandler")
public class DocumentNatureMetaDataHandler {
    @Autowired
    DocumentNatureMetaDataRepository documentNatureMetaDataRepository;

    @Autowired
    SlaveDocumentNatureMetaDataRepository slaveDocumentNatureMetaDataRepository;

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
        Flux<SlaveDocumentNatureMetaDataEntity> slaveDocumentNatureMetaDataEntityFlux = slaveDocumentNatureMetaDataRepository
                .findAllByKeyContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord);
        return slaveDocumentNatureMetaDataEntityFlux
                .collectList()
                .flatMap(docNatureMetaData -> slaveDocumentNatureMetaDataRepository
                        .countByKeyContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord)
                        .flatMap(count ->
                        {
                            if (docNatureMetaData.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records fetched successfully!", docNatureMetaData, count);
                            }
                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long documentNatureMetaDataId = Long.parseLong(serverRequest.pathVariable("id"));
        return serverRequest.formData()
                .flatMap(value -> slaveDocumentNatureMetaDataRepository.findByIdAndDeletedAtIsNull(documentNatureMetaDataId)
                        .flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record fetched successfully!"
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
                                    Mono.just(value1)
                            );
                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("Record does not exist"))));
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

        return serverRequest.formData()
                .flatMap(value -> {
                    DocumentNatureMetaDataEntity documentNatureMetaDataEntity = DocumentNatureMetaDataEntity.builder()
                            .key(value.getFirst("key"))
                            .valueChar(value.getFirst("valueChar"))
                            .valueInt(Integer.valueOf(value.getFirst("valueInt")))
                            .valueDate(LocalDateTime.parse((value.getFirst("valueDate")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .valueType(value.getFirst("valueType"))
                            .documentNatureMetaFieldId(Long.valueOf(value.getFirst("documentNatureMetaFieldId")))
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
                    return documentNatureMetaDataRepository
                            .save(documentNatureMetaDataEntity)
                            .flatMap(value1 -> {
                                var messages = List.of(
                                        new AppResponseMessage(
                                                AppResponse.Response.SUCCESS,
                                                "Record stored successfully!"
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
                                        Mono.just(value1)
                                );
                            })
                            .switchIfEmpty(Mono.defer(() -> responseMsg("There is something wrong. Please Try Again!")));
                });
    }


    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final long documentNatureMetaDataId = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        return serverRequest.formData()
                .flatMap(value -> documentNatureMetaDataRepository.findByIdAndDeletedAtIsNull(documentNatureMetaDataId)
                        .flatMap(documentNatureMetaData -> {
                            documentNatureMetaData.setKey(value.getFirst("key"));
                            documentNatureMetaData.setValueChar(value.getFirst("valueChar"));
                            documentNatureMetaData.setValueInt(Integer.valueOf(value.getFirst("valueInt")));
//                            documentNatureMetaData.setDocumentId(Long.valueOf(value.getFirst("documentId")));
//                            documentNatureMetaData.setDocumentNatureId(Long.valueOf(value.getFirst("documentNatureId")));
                            documentNatureMetaData.setValueType(value.getFirst("valueType"));
                            documentNatureMetaData.setDocumentNatureMetaFieldId(Long.valueOf(value.getFirst("documentNatureMetaFieldId")));
//                            documentNatureMetaData.setUpdatedBy(Long.valueOf(value.getFirst("updatedBy")));
                            documentNatureMetaData.setUpdatedBy(UUID.fromString(userId));
                            documentNatureMetaData.setValueDate(LocalDateTime.parse((value.getFirst("valueDate")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            documentNatureMetaData.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
                            documentNatureMetaData.setReqCreatedIP(value.getFirst("reqCreatedIP"));
                            documentNatureMetaData.setReqCreatedPort(value.getFirst("ReqCreatedPort"));
                            documentNatureMetaData.setReqCreatedBrowser(value.getFirst("reqCreatedBrowser"));
                            documentNatureMetaData.setReqCreatedOS(value.getFirst("reqCreatedOS"));
                            documentNatureMetaData.setReqCreatedDevice(value.getFirst("reqCreatedDevice"));
                            documentNatureMetaData.setReqCreatedReferer(value.getFirst("reqCreatedReferer"));
                            documentNatureMetaData.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            documentNatureMetaData.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            documentNatureMetaData.setReqUpdatedIP(reqIp);
                            documentNatureMetaData.setReqUpdatedPort(reqPort);
                            documentNatureMetaData.setReqUpdatedBrowser(reqBrowser);
                            documentNatureMetaData.setReqUpdatedOS(reqOs);
                            documentNatureMetaData.setReqUpdatedDevice(reqDevice);
                            documentNatureMetaData.setReqUpdatedReferer(reqReferer);
                            return documentNatureMetaDataRepository.save(documentNatureMetaData);
                        }).flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record updated successfully!"
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
                                    Mono.just(value1)
                            );

                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("There is something wrong. Please Try Again!"))));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final long documentNatureMetaDataId = Long.parseLong(serverRequest.pathVariable("id"));
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        return serverRequest.formData()
                .flatMap(value -> documentNatureMetaDataRepository.findByIdAndDeletedAtIsNull(documentNatureMetaDataId)
                        .flatMap(natureMetaDataEntity -> {
//                          natureMetaDataEntity.setDeletedBy(Long.valueOf(value.getFirst("deletedBy")));
                            natureMetaDataEntity.setDeletedBy(UUID.fromString(userId));
                            natureMetaDataEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            natureMetaDataEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            natureMetaDataEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            natureMetaDataEntity.setReqDeletedIP(reqIp);
                            natureMetaDataEntity.setReqDeletedPort(reqPort);
                            natureMetaDataEntity.setReqDeletedBrowser(reqBrowser);
                            natureMetaDataEntity.setReqDeletedOS(reqOs);
                            natureMetaDataEntity.setReqDeletedDevice(reqDevice);
                            natureMetaDataEntity.setReqDeletedReferer(reqReferer);
                            return documentNatureMetaDataRepository.save(natureMetaDataEntity)
                                    .thenReturn(natureMetaDataEntity);
                        }).flatMap(value1 -> {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.SUCCESS,
                                            "Record deleted successfully!"
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
                                    Mono.just(value1)
                            );
                        })
                        .switchIfEmpty(Mono.defer(() -> responseMsg("Record does not exist"))));
    }

    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final long id = Long.parseLong(serverRequest.pathVariable("id"));
        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();

        return formData.flatMap(value -> {
            boolean status = Boolean.parseBoolean(value.getFirst("status"));

            return documentNatureMetaDataRepository.findByIdAndDeletedAtIsNull(id)
                    .flatMap(val -> {
                        // If status is not boolean vale
                        if (status != false && status != true) {
                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Status must be 0 or 1"
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

                        // If already same status exist in database.
                        if (((val.getStatus() ? true : false) == status)) {

                            var messages = List.of(
                                    new AppResponseMessage(
                                            AppResponse.Response.WARNING,
                                            "Record already exist with same status"
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
                                    Mono.just(val)
                            );
                        }

                        // Need to update
                        val.setStatus(status == true ? true : false);
                        var messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Status changed successfully"
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
                                documentNatureMetaDataRepository.save(val)
                        );
                    });
        });
    }

    public Mono<ServerResponse> responseMsg(String msg) {
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

}
