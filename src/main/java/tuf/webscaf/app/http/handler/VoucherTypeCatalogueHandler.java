package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherTypeCatalogueEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherTypeCatalogueRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherTypeCatalogueEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherTypeCatalogueRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "voucherTypeCatalogueHandler")
public class VoucherTypeCatalogueHandler {

    @Autowired
    VoucherTypeCatalogueRepository voucherTypeCataloguesRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SlaveVoucherTypeCatalogueRepository slaveVoucherTypeCataloguesRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Autowired
    SlugifyHelper slugifyHelper;

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveVoucherTypeCatalogueEntity> slaveVoucherTypeCatalogueEntityFlux = slaveVoucherTypeCataloguesRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveVoucherTypeCatalogueEntityFlux
                    .collectList()
                    .flatMap(slaveVoucherTypeCatalogueEntity -> slaveVoucherTypeCataloguesRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveVoucherTypeCatalogueEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveVoucherTypeCatalogueEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveVoucherTypeCatalogueEntity> slaveVoucherTypeCatalogueEntityFlux = slaveVoucherTypeCataloguesRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveVoucherTypeCatalogueEntityFlux
                    .collectList()
                    .flatMap(slaveVoucherTypeCatalogueEntity -> slaveVoucherTypeCataloguesRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (slaveVoucherTypeCatalogueEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveVoucherTypeCatalogueEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveVoucherTypeCatalogueEntity> slaveVoucherTypeCatalogueEntityFlux = slaveVoucherTypeCataloguesRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return slaveVoucherTypeCatalogueEntityFlux
                .collectList()
                .flatMap(slaveVoucherTypeCatalogueEntity -> slaveVoucherTypeCataloguesRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                            if (slaveVoucherTypeCatalogueEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", slaveVoucherTypeCatalogueEntity, count);
                            }
                        })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID voucherTypeCatalogueUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveVoucherTypeCataloguesRepository.findByUuidAndDeletedAtIsNull(voucherTypeCatalogueUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_store")
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

                    VoucherTypeCatalogueEntity voucherTypeCataloguesEntity = VoucherTypeCatalogueEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                            .description(value.getFirst("description").trim())
                            .subAccountGroup(Boolean.valueOf(value.getFirst("subAccountGroup")))
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

                    // check if name is unique
                    return voucherTypeCataloguesRepository.findByNameIgnoreCaseAndDeletedAtIsNull(voucherTypeCataloguesEntity.getName())
                            .flatMap(typeName -> responseInfoMsg("Name Already Exists"))
                            // check if voucher Type slug is unique
                            .switchIfEmpty(Mono.defer(() -> voucherTypeCataloguesRepository.findFirstBySlugAndDeletedAtIsNull(voucherTypeCataloguesEntity.getSlug())
                                    .flatMap(checkSlugMsg -> responseInfoMsg("Slug Already Exists"))))
                            .switchIfEmpty(Mono.defer(() -> voucherTypeCataloguesRepository.save(voucherTypeCataloguesEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                            )).switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record.There is something wrong please try again.")))
                            .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID voucherTypeCatalogueUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> voucherTypeCataloguesRepository.findByUuidAndDeletedAtIsNull(voucherTypeCatalogueUUID)
                        .flatMap(previousVoucherTypeCatalogue -> {

                            VoucherTypeCatalogueEntity updatedVoucherTypeCatalogueEntity = VoucherTypeCatalogueEntity
                                    .builder()
                                    .uuid(previousVoucherTypeCatalogue.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                                    .description(value.getFirst("description").trim())
                                    .subAccountGroup(Boolean.valueOf(value.getFirst("subAccountGroup")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousVoucherTypeCatalogue.getCreatedAt())
                                    .createdBy(previousVoucherTypeCatalogue.getCreatedBy())
                                    .updatedBy(UUID.fromString(userId))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousVoucherTypeCatalogue.getReqCreatedIP())
                                    .reqCreatedPort(previousVoucherTypeCatalogue.getReqCreatedPort())
                                    .reqCreatedBrowser(previousVoucherTypeCatalogue.getReqCreatedBrowser())
                                    .reqCreatedOS(previousVoucherTypeCatalogue.getReqCreatedOS())
                                    .reqCreatedDevice(previousVoucherTypeCatalogue.getReqCreatedDevice())
                                    .reqCreatedReferer(previousVoucherTypeCatalogue.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousVoucherTypeCatalogue.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousVoucherTypeCatalogue.setDeletedBy(UUID.fromString(userId));
                            previousVoucherTypeCatalogue.setReqDeletedIP(reqIp);
                            previousVoucherTypeCatalogue.setReqDeletedPort(reqPort);
                            previousVoucherTypeCatalogue.setReqDeletedBrowser(reqBrowser);
                            previousVoucherTypeCatalogue.setReqDeletedOS(reqOs);
                            previousVoucherTypeCatalogue.setReqDeletedDevice(reqDevice);
                            previousVoucherTypeCatalogue.setReqDeletedReferer(reqReferer);

                            // check if name is unique
                            return voucherTypeCataloguesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedVoucherTypeCatalogueEntity.getName(), voucherTypeCatalogueUUID)
                                    .flatMap(name -> responseInfoMsg("Name Already Exist."))
                                    // check if slug is unique
                                    .switchIfEmpty(Mono.defer(() -> voucherTypeCataloguesRepository.findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(updatedVoucherTypeCatalogueEntity.getSlug(), voucherTypeCatalogueUUID)
                                            .flatMap(checkSlugMsg -> responseInfoMsg("Slug Already Exists"))))
                                    .switchIfEmpty(Mono.defer(() -> voucherTypeCataloguesRepository.save(previousVoucherTypeCatalogue)
                                            .then(voucherTypeCataloguesRepository.save(updatedVoucherTypeCatalogueEntity))
                                            .flatMap(entity -> responseSuccessMsg("Record Updated Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist."))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID voucherTypeCatalogueUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
        return voucherTypeCataloguesRepository.findByUuidAndDeletedAtIsNull(voucherTypeCatalogueUUID)
                .flatMap(voucherTypeCatalogueEntity -> voucherRepository.findFirstByVoucherTypeCatalogueUUIDAndDeletedAtIsNull(voucherTypeCatalogueEntity.getUuid())
                        .flatMap(voucherEntity -> responseInfoMsg("Unable to Delete Record as the Reference exists."))
                        .switchIfEmpty(Mono.defer(() -> {

                            voucherTypeCatalogueEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            voucherTypeCatalogueEntity.setDeletedBy(UUID.fromString(userId));
                            voucherTypeCatalogueEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            voucherTypeCatalogueEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            voucherTypeCatalogueEntity.setReqDeletedIP(reqIp);
                            voucherTypeCatalogueEntity.setReqDeletedPort(reqPort);
                            voucherTypeCatalogueEntity.setReqDeletedBrowser(reqBrowser);
                            voucherTypeCatalogueEntity.setReqDeletedOS(reqOs);
                            voucherTypeCatalogueEntity.setReqDeletedDevice(reqDevice);
                            voucherTypeCatalogueEntity.setReqDeletedReferer(reqReferer);

                            return voucherTypeCataloguesRepository.save(voucherTypeCatalogueEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                        }))
                ).onErrorResume(err -> responseInfoMsg("Record does not exist"))
                .switchIfEmpty(responseInfoMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-type-catalogues_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID voucherTypeCatalogueUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return voucherTypeCataloguesRepository.findByUuidAndDeletedAtIsNull(voucherTypeCatalogueUUID)
                            .flatMap(previousVoucherTypeCatalogueEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousVoucherTypeCatalogueEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                VoucherTypeCatalogueEntity updatedVoucherTypeCatalogueEntity = VoucherTypeCatalogueEntity.builder()
                                        .uuid(previousVoucherTypeCatalogueEntity.getUuid())
                                        .name(previousVoucherTypeCatalogueEntity.getName())
                                        .slug(previousVoucherTypeCatalogueEntity.getSlug())
                                        .description(previousVoucherTypeCatalogueEntity.getDescription())
                                        .subAccountGroup(previousVoucherTypeCatalogueEntity.getSubAccountGroup())
                                        .status(status == true ? true : false)
                                        .createdAt(previousVoucherTypeCatalogueEntity.getCreatedAt())
                                        .createdBy(previousVoucherTypeCatalogueEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousVoucherTypeCatalogueEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousVoucherTypeCatalogueEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousVoucherTypeCatalogueEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousVoucherTypeCatalogueEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousVoucherTypeCatalogueEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousVoucherTypeCatalogueEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousVoucherTypeCatalogueEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousVoucherTypeCatalogueEntity.setDeletedBy(UUID.fromString(userId));
                                previousVoucherTypeCatalogueEntity.setReqDeletedIP(reqIp);
                                previousVoucherTypeCatalogueEntity.setReqDeletedPort(reqPort);
                                previousVoucherTypeCatalogueEntity.setReqDeletedBrowser(reqBrowser);
                                previousVoucherTypeCatalogueEntity.setReqDeletedOS(reqOs);
                                previousVoucherTypeCatalogueEntity.setReqDeletedDevice(reqDevice);
                                previousVoucherTypeCatalogueEntity.setReqDeletedReferer(reqReferer);

                                return voucherTypeCataloguesRepository.save(previousVoucherTypeCatalogueEntity)
                                        .then(voucherTypeCataloguesRepository.save(updatedVoucherTypeCatalogueEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please contact developer."));
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
