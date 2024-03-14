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
import tuf.webscaf.app.dbContext.master.entity.JobGroupEntity;
import tuf.webscaf.app.dbContext.master.repository.JobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.JobGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.JobRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherJobGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherJobGroupPvtRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "jobGroupHandler")
public class JobGroupHandler {
    @Autowired
    JobGroupRepository jobGroupRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    SlaveJobGroupRepository slaveJobGroupRepository;

    @Autowired
    SlaveVoucherJobGroupPvtRepository slaveVoucherJobGroupPvtRepository;

    @Autowired
    VoucherJobGroupPvtRepository voucherJobGroupPvtRepository;

    @Autowired
    JobGroupJobPvtRepository jobGroupJobPvtRepository;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_job-groups_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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


        // Return All Job Group
        if (!status.isEmpty()) {
            Flux<SlaveJobGroupEntity> slaveJobGroupEntityFluxOfStatus = slaveJobGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveJobGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveJobGroupEntity ->
                            slaveJobGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveJobGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveJobGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));
        }

        // Return All Job Group according to given value
        else {
            Flux<SlaveJobGroupEntity> jobGroupEntityFlux = slaveJobGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return jobGroupEntityFlux
                    .collectList()
                    .flatMap(slaveJobGroupEntity ->
                            slaveJobGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveJobGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveJobGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact to developer"));
        }
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_active_index")
    public Mono<ServerResponse> indexWithActiveStatus(ServerRequest serverRequest) {

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        Flux<SlaveJobGroupEntity> slaveJobGroupEntityFluxOfStatus = slaveJobGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveJobGroupEntityFluxOfStatus
                .collectList()
                .flatMap(slaveJobGroupEntity ->
                        slaveJobGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                .flatMap(count -> {
                                    if (slaveJobGroupEntity.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", slaveJobGroupEntity, count);
                                    }
                                })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

    }

    @AuthHasPermission(value = "account_api_v1_job-groups_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveJobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_job_mapped_show")
    //Show Job Groups for Job Id
    public Mono<ServerResponse> listOfJobGroups(ServerRequest serverRequest) {
        UUID jobUUID = UUID.fromString(serverRequest.pathVariable("jobUUID").trim());

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveJobGroupEntity> slaveJobGroupEntityFlux = slaveJobGroupRepository
                    .listOfJobGroupsAgainstJobWithStatus(jobUUID, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveJobGroupEntityFlux
                    .collectList()
                    .flatMap(jobGroupEntity -> slaveJobGroupRepository.countJobGroupsAgainstJobWithStatus(jobUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (jobGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", jobGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveJobGroupEntity> slaveJobGroupEntityFlux = slaveJobGroupRepository
                    .listOfJobGroupsAgainstJob(jobUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveJobGroupEntityFlux
                    .collectList()
                    .flatMap(jobGroupEntity -> slaveJobGroupRepository.countJobGroupsAgainstJob(jobUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (jobGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", jobGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_store")
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

                    JobGroupEntity jobGroupEntity = JobGroupEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
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

                    return jobGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(jobGroupEntity.getName())
                            .flatMap(key -> responseInfoMsg("Name Already Exist"))
                            .switchIfEmpty(Mono.defer(() -> jobGroupRepository.save(jobGroupEntity)
                                    .flatMap(jobGroup -> responseSuccessMsg("Record stored successfully.", jobGroup))
                                    .switchIfEmpty(responseInfoMsg("Record not stored.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> jobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                        .flatMap(previousJobGroupEntity -> {
                                    JobGroupEntity updatedJobGroupEntity = JobGroupEntity.builder()
                                            .uuid(previousJobGroupEntity.getUuid())
                                            .name(value.getFirst("name").trim())
                                            .description(value.getFirst("description").trim())
                                            .status(Boolean.valueOf(value.getFirst("status")))
                                            .createdAt(previousJobGroupEntity.getCreatedAt())
                                            .createdBy(previousJobGroupEntity.getCreatedBy())
                                            .updatedBy(UUID.fromString(userId))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(previousJobGroupEntity.getReqCreatedIP())
                                            .reqCreatedPort(previousJobGroupEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(previousJobGroupEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(previousJobGroupEntity.getReqCreatedOS())
                                            .reqCreatedDevice(previousJobGroupEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(previousJobGroupEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                            .reqUpdatedIP(reqIp)
                                            .reqUpdatedPort(reqPort)
                                            .reqUpdatedBrowser(reqBrowser)
                                            .reqUpdatedOS(reqOs)
                                            .reqUpdatedDevice(reqDevice)
                                            .reqUpdatedReferer(reqReferer)
                                            .build();

                                    previousJobGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousJobGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    previousJobGroupEntity.setReqDeletedIP(reqIp);
                                    previousJobGroupEntity.setReqDeletedPort(reqPort);
                                    previousJobGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    previousJobGroupEntity.setReqDeletedOS(reqOs);
                                    previousJobGroupEntity.setReqDeletedDevice(reqDevice);
                                    previousJobGroupEntity.setReqDeletedReferer(reqReferer);

                                    return jobGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedJobGroupEntity.getName(), jobGroupUUID)
                                            .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exists"))
                                            .switchIfEmpty(Mono.defer(() -> jobGroupRepository.save(previousJobGroupEntity)
                                                    .then(jobGroupRepository.save(updatedJobGroupEntity))
                                                    .flatMap(saveJobGroup -> responseSuccessMsg("Record Updated Successfully.", saveJobGroup))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong Please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            ));
                                }
                        )
                        .switchIfEmpty(responseInfoMsg("Job Center Group Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Job Center Group Does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
        return jobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                //check if Job Group Exists in Voucher Pvt Table
                .flatMap(jobGroupEntity -> voucherJobGroupPvtRepository.findFirstByJobGroupUUIDAndDeletedAtIsNull(jobGroupEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        //check if Job Group Exists in JobJobGroup Pvt Table
                        .switchIfEmpty(Mono.defer(() -> jobGroupJobPvtRepository.findFirstByJobGroupUUIDAndDeletedAtIsNull(jobGroupEntity.getUuid())
                                .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        .switchIfEmpty(Mono.defer(() -> {

                                    jobGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    jobGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    jobGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    jobGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    jobGroupEntity.setReqDeletedIP(reqIp);
                                    jobGroupEntity.setReqDeletedPort(reqPort);
                                    jobGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    jobGroupEntity.setReqDeletedOS(reqOs);
                                    jobGroupEntity.setReqDeletedDevice(reqDevice);
                                    jobGroupEntity.setReqDeletedReferer(reqReferer);

                                    return jobGroupRepository.save(jobGroupEntity)
                                            .flatMap(saveEntity -> responseSuccessMsg("Record Deleted Successfully", saveEntity)
                                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer.")));
                                }
                        ))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-groups_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                    return jobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                            .flatMap(previousJobGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousJobGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                JobGroupEntity updatedJobGroupEntity = JobGroupEntity
                                        .builder()
                                        .uuid(previousJobGroupEntity.getUuid())
                                        .name(previousJobGroupEntity.getName())
                                        .description(previousJobGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousJobGroupEntity.getCreatedAt())
                                        .createdBy(previousJobGroupEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousJobGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousJobGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousJobGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousJobGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousJobGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousJobGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousJobGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousJobGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousJobGroupEntity.setReqDeletedIP(reqIp);
                                previousJobGroupEntity.setReqDeletedPort(reqPort);
                                previousJobGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousJobGroupEntity.setReqDeletedOS(reqOs);
                                previousJobGroupEntity.setReqDeletedDevice(reqDevice);
                                previousJobGroupEntity.setReqDeletedReferer(reqReferer);

                                return jobGroupRepository.save(previousJobGroupEntity)
                                        .then(jobGroupRepository.save(updatedJobGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    //    ---------------  Custom Response Functions----------------

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

    //    ---------------  Custom Response Functions----------------

}
