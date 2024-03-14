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
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaFieldEntity;
import tuf.webscaf.app.dbContext.master.repository.DocumentNatureMetaFieldRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaFieldEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveDocumentNatureMetaFieldRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;

@Component
@Tag(name = "documentNatureMetaFieldHandler")
public class DocumentNatureMetaFieldHandler {
    @Autowired
    DocumentNatureMetaFieldRepository documentNatureMetaFieldRepository;

    @Autowired
    SlaveDocumentNatureMetaFieldRepository slaveDocumentNatureMetaFieldRepository;

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
        Flux<SlaveDocumentNatureMetaFieldEntity> slaveDocumentNatureMetaFieldEntityFlux = slaveDocumentNatureMetaFieldRepository
                .findAllByKeyContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord);
        return slaveDocumentNatureMetaFieldEntityFlux
                .collectList()
                .flatMap(docNatureMetaFieldDB -> slaveDocumentNatureMetaFieldRepository
                        .countByKeyContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord)
                        .flatMap(count ->
                        {
                            if (docNatureMetaFieldDB.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records fetched successfully!", docNatureMetaFieldDB, count);
                            }
                        }));
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long documentMetaFieldId = Long.parseLong(serverRequest.pathVariable("id"));

        return serverRequest.formData()
                .flatMap(value -> slaveDocumentNatureMetaFieldRepository.findByIdAndDeletedAtIsNull(documentMetaFieldId)
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
                    DocumentNatureMetaFieldEntity documentNatureMetaFieldEntity = DocumentNatureMetaFieldEntity.builder()
                            .key(value.getFirst("key"))
                            .valueType(value.getFirst("valueType"))
//                           .createdBy(Long.valueOf(value.getFirst("createdBy")))
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
                    return documentNatureMetaFieldRepository
                            .save(documentNatureMetaFieldEntity)
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
        final long docMetaId = Long.parseLong(serverRequest.pathVariable("id"));
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
                .flatMap(docMetaFieldEntity -> documentNatureMetaFieldRepository.findByIdAndDeletedAtIsNull(docMetaId)
                        .flatMap(docNatureMetaFieldEntityDB -> {
                            docNatureMetaFieldEntityDB.setKey(docMetaFieldEntity.getFirst("key"));
                            docNatureMetaFieldEntityDB.setValueType(docMetaFieldEntity.getFirst("valueType"));
//                          docNatureMetaFieldEntityDB.setUpdatedBy(Long.valueOf(docMetaFieldEntity.getFirst("updatedBy")));
                            docNatureMetaFieldEntityDB.setUpdatedBy(UUID.fromString(userId));
                            docNatureMetaFieldEntityDB.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
                            docNatureMetaFieldEntityDB.setReqCreatedIP(docMetaFieldEntity.getFirst("reqCreatedIP"));
                            docNatureMetaFieldEntityDB.setReqCreatedPort(docMetaFieldEntity.getFirst("ReqCreatedPort"));
                            docNatureMetaFieldEntityDB.setReqCreatedBrowser(docMetaFieldEntity.getFirst("reqCreatedBrowser"));
                            docNatureMetaFieldEntityDB.setReqCreatedOS(docMetaFieldEntity.getFirst("reqCreatedOS"));
                            docNatureMetaFieldEntityDB.setReqCreatedDevice(docMetaFieldEntity.getFirst("reqCreatedDevice"));
                            docNatureMetaFieldEntityDB.setReqCreatedReferer(docMetaFieldEntity.getFirst("reqCreatedReferer"));
                            docNatureMetaFieldEntityDB.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            docNatureMetaFieldEntityDB.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            docNatureMetaFieldEntityDB.setReqUpdatedIP(reqIp);
                            docNatureMetaFieldEntityDB.setReqUpdatedPort(reqPort);
                            docNatureMetaFieldEntityDB.setReqUpdatedBrowser(reqBrowser);
                            docNatureMetaFieldEntityDB.setReqUpdatedOS(reqOs);
                            docNatureMetaFieldEntityDB.setReqUpdatedDevice(reqDevice);
                            docNatureMetaFieldEntityDB.setReqUpdatedReferer(reqReferer);
                            return documentNatureMetaFieldRepository.save(docNatureMetaFieldEntityDB);
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
        final long documentNatureMetaId = Long.parseLong(serverRequest.pathVariable("id"));
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
                .flatMap(value -> documentNatureMetaFieldRepository.findByIdAndDeletedAtIsNull(documentNatureMetaId)
                        .flatMap(docMetaFieldEntity -> {
//                          docMetaFieldEntity.setDeletedBy(Long.valueOf(value.getFirst("deletedBy")));
                            docMetaFieldEntity.setDeletedBy(UUID.fromString(userId));
                            docMetaFieldEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            docMetaFieldEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            docMetaFieldEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            docMetaFieldEntity.setReqDeletedIP(reqIp);
                            docMetaFieldEntity.setReqDeletedPort(reqPort);
                            docMetaFieldEntity.setReqDeletedBrowser(reqBrowser);
                            docMetaFieldEntity.setReqDeletedOS(reqOs);
                            docMetaFieldEntity.setReqDeletedDevice(reqDevice);
                            docMetaFieldEntity.setReqDeletedReferer(reqReferer);
                            return documentNatureMetaFieldRepository.save(docMetaFieldEntity)
                                    .thenReturn(docMetaFieldEntity);
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
            Boolean status = Boolean.parseBoolean(value.getFirst("status"));

            return documentNatureMetaFieldRepository.findByIdAndDeletedAtIsNull(id)
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
                                documentNatureMetaFieldRepository.save(val)
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
