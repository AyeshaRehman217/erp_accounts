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
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.DocumentNatureGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherDocumentNatureGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveDocumentNatureGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherDocumentNatureGroupPvtRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Component
@Tag(name = "documentNatureGroupHandler")
public class DocumentNatureGroupHandler {
    @Autowired
    DocumentNatureGroupRepository documentNatureGroupRepository;

    @Autowired
    SlaveDocumentNatureGroupRepository slaveDocumentNatureGroupRepository;

    @Autowired
    SlaveVoucherDocumentNatureGroupPvtRepository slaveVoucherDocumentNatureGroupPvtRepository;

    @Autowired
    VoucherDocumentNatureGroupPvtRepository voucherDocumentNatureGroupPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
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


        //      Return All Document Nature Group
        if (!status.isEmpty()) {
            Flux<SlaveDocumentNatureGroupEntity> slaveDocumentNatureGroupEntityFluxOfStatus = slaveDocumentNatureGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveDocumentNatureGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveDocumentNatureGroupEntity ->
                            slaveDocumentNatureGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveDocumentNatureGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveDocumentNatureGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

        }
//      Return All DocumentNatureGroup according to given value
        else {
            Flux<SlaveDocumentNatureGroupEntity> slaveDocumentNatureGroupEntityFlux = slaveDocumentNatureGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveDocumentNatureGroupEntityFlux
                    .collectList()
                    .flatMap(slaveDocumentNatureGroupEntity ->
                            slaveDocumentNatureGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveDocumentNatureGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveDocumentNatureGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact to developer"));
        }
    }

    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        final long documentNatureGroupId = Long.parseLong(serverRequest.pathVariable("id"));

        return slaveDocumentNatureGroupRepository.findByIdAndDeletedAtIsNull(documentNatureGroupId)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    public Mono<ServerResponse> showByUuid(ServerRequest serverRequest) {
        UUID documentNatureGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveDocumentNatureGroupRepository.findByUuidAndDeletedAtIsNull(documentNatureGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
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

                    DocumentNatureGroupEntity documentNatureGroupEntity = DocumentNatureGroupEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .createdBy(UUID.fromString(userId))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    return documentNatureGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(documentNatureGroupEntity.getName())
                            .flatMap(key -> responseInfoMsg("Name already exist"))
                            .switchIfEmpty(Mono.defer(() -> documentNatureGroupRepository.save(documentNatureGroupEntity)
                                    .flatMap(configSave -> responseSuccessMsg("Record stored successfully.", documentNatureGroupEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please Try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID documentNatureGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                .flatMap(value -> documentNatureGroupRepository.findByUuidAndDeletedAtIsNull(documentNatureGroupUUID)
                        .flatMap(previousDocumentNatureGroupEntity -> {

                            DocumentNatureGroupEntity updatedDocumentNatureGroupEntity = DocumentNatureGroupEntity.builder()
                                    .uuid(previousDocumentNatureGroupEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousDocumentNatureGroupEntity.getCreatedAt())
                                    .createdBy(previousDocumentNatureGroupEntity.getCreatedBy())
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedBy(UUID.fromString(userId))
                                    .reqCreatedIP(previousDocumentNatureGroupEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousDocumentNatureGroupEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousDocumentNatureGroupEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousDocumentNatureGroupEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousDocumentNatureGroupEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousDocumentNatureGroupEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            return documentNatureGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedDocumentNatureGroupEntity.getName(), documentNatureGroupUUID)
                                    .flatMap(entity -> responseInfoMsg("Name Already Exist"))
                                    .switchIfEmpty(Mono.defer(() -> documentNatureGroupRepository.save(previousDocumentNatureGroupEntity)
                                            .then(documentNatureGroupRepository.save(updatedDocumentNatureGroupEntity))
                                            .flatMap(documentNatureGroupEntityDB -> responseSuccessMsg("Record updated Successfully.", documentNatureGroupEntityDB))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record .Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request.Please Contact Developer."));

    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID documentNatureGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        //check if Voucher Group Exists
        return documentNatureGroupRepository.findByUuidAndDeletedAtIsNull(documentNatureGroupUUID)
                //check if Cost Group Exists in Voucher Pvt Table
                .flatMap(documentNatureGroupEntity -> voucherDocumentNatureGroupPvtRepository.findFirstByDocumentNatureGroupUUIDAndDeletedAtIsNull(documentNatureGroupEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        //deleting Voucher Group Record
                        .switchIfEmpty(Mono.defer(() -> {

                                    documentNatureGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    documentNatureGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    documentNatureGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    documentNatureGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    documentNatureGroupEntity.setReqDeletedIP(reqIp);
                                    documentNatureGroupEntity.setReqDeletedPort(reqPort);
                                    documentNatureGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    documentNatureGroupEntity.setReqDeletedOS(reqOs);
                                    documentNatureGroupEntity.setReqDeletedDevice(reqDevice);
                                    documentNatureGroupEntity.setReqDeletedReferer(reqReferer);

                                    return documentNatureGroupRepository.save(documentNatureGroupEntity)
                                            .flatMap(saveEntity -> responseSuccessMsg("Record deleted successfully", saveEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                                })
                        )).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }


    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID documentNatureGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return documentNatureGroupRepository.findByUuidAndDeletedAtIsNull(documentNatureGroupUUID)
                            .flatMap(previousDocumentNatureGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousDocumentNatureGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                DocumentNatureGroupEntity updatedDocumentNatureGroupEntity = DocumentNatureGroupEntity.builder()
                                        .uuid(previousDocumentNatureGroupEntity.getUuid())
                                        .name(value.getFirst("name").trim())
                                        .description(value.getFirst("description").trim())
                                        .status(status == true ? true : false)
                                        .createdAt(previousDocumentNatureGroupEntity.getCreatedAt())
                                        .createdBy(previousDocumentNatureGroupEntity.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userId))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousDocumentNatureGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousDocumentNatureGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousDocumentNatureGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousDocumentNatureGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousDocumentNatureGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousDocumentNatureGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousDocumentNatureGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousDocumentNatureGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousDocumentNatureGroupEntity.setReqDeletedIP(reqIp);
                                previousDocumentNatureGroupEntity.setReqDeletedPort(reqPort);
                                previousDocumentNatureGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousDocumentNatureGroupEntity.setReqDeletedOS(reqOs);
                                previousDocumentNatureGroupEntity.setReqDeletedDevice(reqDevice);
                                previousDocumentNatureGroupEntity.setReqDeletedReferer(reqReferer);

                                return documentNatureGroupRepository.save(previousDocumentNatureGroupEntity)
                                        .then(documentNatureGroupRepository.save(updatedDocumentNatureGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
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
}
