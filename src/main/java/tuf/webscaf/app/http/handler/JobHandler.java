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
import tuf.webscaf.app.dbContext.master.entity.JobEntity;
import tuf.webscaf.app.dbContext.master.repository.JobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.JobRepository;
import tuf.webscaf.app.dbContext.master.repository.TransactionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherJobGroupPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Tag(name = "jobHandler")
public class JobHandler {
    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobGroupJobPvtRepository jobGroupJobPvtRepository;

    @Autowired
    SlaveVoucherJobGroupPvtRepository slaveVoucherJobGroupPvtRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SlaveJobRepository slaveJobRepository;

    @Autowired
    ApiCallService apiCallService;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_jobs_index")
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
            Flux<SlaveJobEntity> jobEntityFlux = slaveJobRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return jobEntityFlux
                    .collectList()
                    .flatMap(jobEntityDB -> slaveJobRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (jobEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", jobEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveJobEntity> jobEntityFlux = slaveJobRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return jobEntityFlux
                    .collectList()
                    .flatMap(jobEntityDB -> slaveJobRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (jobEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", jobEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_jobs_active_index")
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

        Flux<SlaveJobEntity> jobEntityFlux = slaveJobRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);
        return jobEntityFlux
                .collectList()
                .flatMap(jobEntityDB -> slaveJobRepository
                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                                    if (jobEntityDB.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);

                                    } else {

                                        return responseIndexSuccessMsg("All Records fetched successfully!", jobEntityDB, count);
                                    }
                                }
                        )
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_jobs_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID jobUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveJobRepository.findByUuidAndDeletedAtIsNull(jobUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    //Check Branch id In Config Module
    @AuthHasPermission(value = "account_api_v1_jobs_branch_show")
    public Mono<ServerResponse> getBranchUUID(ServerRequest serverRequest) {
        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveJobRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //Check Company id In Config Module
    @AuthHasPermission(value = "account_api_v1_jobs_company_show")
    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveJobRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //Show Jobs for Voucher UUID
    @AuthHasPermission(value = "account_api_v1_jobs_voucher_show")
    public Mono<ServerResponse> showVoucherWithJobs(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();


        return slaveVoucherJobGroupPvtRepository.jobGroupAllMappingExists(voucherUUID)
                .flatMap(all -> {

                    // if voucher is mapped with job group of all jobs
                    if(all){

                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveJobEntity> jobEntityFlux = slaveJobRepository
                                    .indexJobWithStatus(searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return jobEntityFlux
                                    .collectList()
                                    .flatMap(jobEntityDB -> slaveJobRepository
                                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count -> {
                                                        if (jobEntityDB.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count);

                                                        } else {

                                                            return responseIndexSuccessMsg("All Records Fetched Successfully", jobEntityDB, count);
                                                        }
                                                    }
                                            )
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveJobEntity> jobEntityFlux = slaveJobRepository
                                    .indexJob(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return jobEntityFlux
                                    .collectList()
                                    .flatMap(jobEntityDB -> slaveJobRepository
                                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                            .flatMap(count -> {
                                                        if (jobEntityDB.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count);

                                                        } else {

                                                            return responseIndexSuccessMsg("All Records Fetched Successfully", jobEntityDB, count);
                                                        }
                                                    }
                                            )
                                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                        }
                    }


                    // if voucher is not mapped with job group of all jobs
                    else {
                        // if status is present in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                                    .showJobListDataAgainstVoucherWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveJobEntityFlux
                                    .collectList()
                                    .flatMap(jobEntity -> slaveJobRepository
                                            .countJobsDataWithVoucherWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count ->
                                            {
                                                if (jobEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", jobEntity, count);
                                                }
                                            })).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }

                        // if status is not present
                        else {
                            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                                    .showJobListDataAgainstVoucher(voucherUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveJobEntityFlux
                                    .collectList()
                                    .flatMap(jobEntity -> slaveJobRepository
                                            .countJobsDataWithVoucher(voucherUUID, searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (jobEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", jobEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
                        }
                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }


    //show Accounts for a Voucher with given Company UUID
    @AuthHasPermission(value = "account_api_v1_jobs_voucher_company_show")
    public Mono<ServerResponse> showJobsWithCompany(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        UUID companyUUID = UUID.fromString(serverRequest.queryParam("companyUUID").map(String::toString).orElse(""));

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                    .showJobListWithCompanyAgainstVoucherWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveJobRepository
                            .countWithCompanyAgainstVoucherWithStatus(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                    .showJobListWithCompanyAgainstVoucher(voucherUUID, companyUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveJobRepository
                            .countWithCompanyAgainstVoucher(voucherUUID, companyUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    //show Accounts for a Voucher with given Company Id
    @AuthHasPermission(value = "account_api_v1_jobs_voucher_branch_show")
    public Mono<ServerResponse> showJobsWithBranch(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        UUID branchUUID = UUID.fromString(serverRequest.queryParam("branchUUID").map(String::toString).orElse(""));

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                    .showJobListWithBranchAgainstVoucherWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveJobRepository
                            .countWithBranchAgainstVoucherWithStatus(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));

        }

        // if status is not present
        else {
            Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                    .showJobListWithBranchAgainstVoucher(voucherUUID, branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveJobRepository
                            .countWithBranchAgainstVoucher(voucherUUID, branchUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_jobs_store")
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

                    JobEntity jobEntity = JobEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .companyUUID(UUID.fromString(reqCompanyUUID))
                            .branchUUID(UUID.fromString(reqBranchUUID))
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

                    return jobRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(jobEntity.getName())
                            .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> jobRepository.save(jobEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is Something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_jobs_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID jobUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> jobRepository.findByUuidAndDeletedAtIsNull(jobUUID)
                        .flatMap(previousJobEntity -> {

                            JobEntity jobEntity = JobEntity.builder()
                                    .uuid(previousJobEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .companyUUID(UUID.fromString(reqCompanyUUID))
                                    .branchUUID(UUID.fromString(reqBranchUUID))
                                    .createdBy(previousJobEntity.getCreatedBy())
                                    .createdAt(previousJobEntity.getCreatedAt())
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedBy(UUID.fromString(userId))
                                    .reqCreatedIP(previousJobEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousJobEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousJobEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousJobEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousJobEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousJobEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousJobEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousJobEntity.setDeletedBy(UUID.fromString(userId));
                            previousJobEntity.setReqDeletedIP(reqIp);
                            previousJobEntity.setReqDeletedPort(reqPort);
                            previousJobEntity.setReqDeletedBrowser(reqBrowser);
                            previousJobEntity.setReqDeletedOS(reqOs);
                            previousJobEntity.setReqDeletedDevice(reqDevice);
                            previousJobEntity.setReqDeletedReferer(reqReferer);

                            return jobRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(jobEntity.getName(), jobUUID)
                                    .flatMap(nameAlreadyExists -> responseInfoMsg("Name Already Exists"))
                                    .switchIfEmpty(Mono.defer(() -> jobRepository.save(previousJobEntity)
                                            .then(jobRepository.save(jobEntity))
                                            .flatMap(value1 -> responseSuccessMsg("Record Updated Successfully", value1))
                                            .switchIfEmpty(responseInfoMsg("Unable to update record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to Update record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_jobs_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID jobUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

        return jobRepository.findByUuidAndDeletedAtIsNull(jobUUID)
                .flatMap(jobEntity -> transactionRepository.findFirstByJobUUIDAndDeletedAtIsNull(jobEntity.getUuid())
                        .flatMap(val -> responseInfoMsg("Unable to Delete Record as Reference Exists."))
                        .switchIfEmpty(Mono.defer(() -> jobGroupJobPvtRepository.findFirstByJobUUIDAndDeletedAtIsNull(jobEntity.getUuid())
                                .flatMap(val1 -> responseInfoMsg("Unable to Delete Record as Reference Exists."))))
                        .switchIfEmpty(Mono.defer(() -> {
                            jobEntity.setDeletedBy(UUID.fromString(userId));
                            jobEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            jobEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            jobEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            jobEntity.setReqDeletedIP(reqIp);
                            jobEntity.setReqDeletedPort(reqPort);
                            jobEntity.setReqDeletedBrowser(reqBrowser);
                            jobEntity.setReqDeletedOS(reqOs);
                            jobEntity.setReqDeletedDevice(reqDevice);
                            jobEntity.setReqDeletedReferer(reqReferer);

                            return jobRepository.save(jobEntity)
                                    .flatMap(jobEntity1 -> responseSuccessMsg("Record Deleted Successfully!", jobEntity1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record.Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record Does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_jobs_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID jobUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                    return jobRepository.findByUuidAndDeletedAtIsNull(jobUUID)
                            .flatMap(previousJobEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousJobEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                JobEntity updatedJobEntity = JobEntity.builder()
                                        .uuid(previousJobEntity.getUuid())
                                        .name(previousJobEntity.getName())
                                        .description(previousJobEntity.getDescription())
                                        .companyUUID(previousJobEntity.getCompanyUUID())
                                        .branchUUID(previousJobEntity.getBranchUUID())
                                        .status(status == true ? true : false)
                                        .createdAt(previousJobEntity.getCreatedAt())
                                        .createdBy(previousJobEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousJobEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousJobEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousJobEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousJobEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousJobEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousJobEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                // update status
                                previousJobEntity.setDeletedBy(UUID.fromString(userId));
                                previousJobEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousJobEntity.setReqDeletedIP(reqIp);
                                previousJobEntity.setReqDeletedPort(reqPort);
                                previousJobEntity.setReqDeletedBrowser(reqBrowser);
                                previousJobEntity.setReqDeletedOS(reqOs);
                                previousJobEntity.setReqDeletedDevice(reqDevice);
                                previousJobEntity.setReqDeletedReferer(reqReferer);

                                return jobRepository.save(previousJobEntity)
                                        .then(jobRepository.save(updatedJobEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
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
