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
import tuf.webscaf.app.dbContext.master.entity.JobGroupJobPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.JobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.JobGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.JobRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountGroupAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveJobGroupJobDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "jobGroupJobHandler")
public class JobGroupJobHandler {

    @Autowired
    JobGroupJobPvtRepository jobGroupJobPvtRepository;

    @Autowired
    SlaveJobGroupJobPvtRepository slaveJobGroupJobPvtRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    SlaveJobRepository slaveJobRepository;

    @Autowired
    JobGroupRepository jobGroupRepository;

    @Autowired
    SlaveJobGroupRepository slaveJobGroupRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_job-group-job_un-mapped_show")
    public Mono<ServerResponse> showUnMappedJobsAgainstJobGroups(ServerRequest serverRequest) {

        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("jobGroupUUID").trim());

        // Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveJobEntity> jobEntityFlux = slaveJobGroupJobPvtRepository
                    .showUnMappedJobListAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return jobEntityFlux
                    .collectList()
                    .flatMap(jobEntityDB -> slaveJobRepository.countUnMappedJobAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (jobEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully.", jobEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveJobEntity> jobEntityFlux = slaveJobGroupJobPvtRepository
                    .showUnMappedJobListAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return jobEntityFlux
                    .collectList()
                    .flatMap(jobEntityDB -> slaveJobRepository.countUnMappedJobAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (jobEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully.", jobEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_job-group-job_mapped_show")
    //show Jobs for Job Group UUID
    public Mono<ServerResponse> showMappedJobsAgainstJobGroups(ServerRequest serverRequest) {
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("jobGroupUUID").trim());

        // Optional Query Parameter Based of Status
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

        return slaveJobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                // if job group is mapped with all
                .flatMap(jobGroupEntity -> slaveJobGroupJobPvtRepository.findByJobGroupUUIDAndAllAndDeletedAtIsNull(jobGroupUUID, true)
                        .flatMap(allJobsMapped -> {

                            SlaveJobGroupJobDto slaveJobGroupJobDto = SlaveJobGroupJobDto.builder()
                                    .id(allJobsMapped.getId())
                                    .version(allJobsMapped.getVersion())
                                    .uuid(allJobsMapped.getUuid())
                                    .name("all")
                                    .jobGroupName(jobGroupEntity.getName())
                                    .jobGroupUUID(allJobsMapped.getJobGroupUUID())
                                    .all(allJobsMapped.getAll())
                                    .createdAt(allJobsMapped.getCreatedAt())
                                    .createdBy(allJobsMapped.getCreatedBy())
                                    .updatedBy(allJobsMapped.getUpdatedBy())
                                    .updatedAt(allJobsMapped.getUpdatedAt())
                                    .reqCompanyUUID(allJobsMapped.getReqCompanyUUID())
                                    .reqBranchUUID(allJobsMapped.getReqBranchUUID())
                                    .reqCreatedIP(allJobsMapped.getReqCreatedIP())
                                    .reqCreatedPort(allJobsMapped.getReqCreatedPort())
                                    .reqCreatedBrowser(allJobsMapped.getReqCreatedBrowser())
                                    .reqCreatedOS(allJobsMapped.getReqCreatedOS())
                                    .reqCreatedDevice(allJobsMapped.getReqCreatedDevice())
                                    .reqCreatedReferer(allJobsMapped.getReqCreatedReferer())
                                    .reqUpdatedIP(allJobsMapped.getReqUpdatedIP())
                                    .reqUpdatedPort(allJobsMapped.getReqUpdatedPort())
                                    .reqUpdatedBrowser(allJobsMapped.getReqUpdatedBrowser())
                                    .reqUpdatedOS(allJobsMapped.getReqUpdatedOS())
                                    .reqUpdatedDevice(allJobsMapped.getReqUpdatedDevice())
                                    .reqUpdatedReferer(allJobsMapped.getReqUpdatedReferer())
                                    .build();

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                return slaveJobRepository
                                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveJobGroupJobDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                return slaveJobRepository
                                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveJobGroupJobDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }
                        })

                        // get only mapped records from pvt
                        .switchIfEmpty(Mono.defer(() -> {

                            // if status is present in query parameter
                            if (!status.isEmpty()) {
                                Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                                        .showMappedJobListAgainstJobGroupWithStatus(jobGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveJobEntityFlux
                                        .collectList()
                                        .flatMap(jobEntity -> slaveJobRepository.countMappedJobsAgainstJobGroupWithStatus(jobGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                                .flatMap(count -> {

                                                    if (jobEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records fetched successfully!", jobEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                Flux<SlaveJobEntity> slaveJobEntityFlux = slaveJobRepository
                                        .showMappedJobListAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveJobEntityFlux
                                        .collectList()
                                        .flatMap(jobEntity -> slaveJobRepository.countMappedJobsAgainstJobGroup(jobGroupUUID, searchKeyWord, searchKeyWord)
                                                .flatMap(count -> {

                                                    if (jobEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records fetched successfully!", jobEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }
                        }))
                ).switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-group-job_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("jobGroupUUID").trim());
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
                                .flatMap(jobGroupEntity -> {

//                            // if given job group is inactive
//                            if (!jobGroupEntity.getStatus()) {
//                                return responseInfoMsg("Job Group status is inactive");
//                            }

                                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                                    // mapping with all records
                                    if (all) {

                                        JobGroupJobPvtEntity jobGroupJobPvtEntity = JobGroupJobPvtEntity.builder()
                                                .uuid(UUID.randomUUID())
                                                .jobGroupUUID(jobGroupUUID)
                                                .all(all)
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

                                        return jobGroupJobPvtRepository.findFirstByJobGroupUUIDAndAllAndDeletedAtIsNull(jobGroupUUID, true)
                                                .flatMap(recordAlreadyExists -> responseInfoMsg("Record Already Exists"))
                                                .switchIfEmpty(Mono.defer(() -> jobGroupJobPvtRepository.findAllByJobGroupUUIDAndDeletedAtIsNull(jobGroupUUID)
                                                        .collectList()
                                                        .flatMap(previouslyMappedJobs -> {

                                                            for (JobGroupJobPvtEntity pvtEntity : previouslyMappedJobs) {
                                                                pvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                pvtEntity.setDeletedBy(UUID.fromString(userId));
                                                                pvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                pvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                pvtEntity.setReqDeletedIP(reqIp);
                                                                pvtEntity.setReqDeletedPort(reqPort);
                                                                pvtEntity.setReqDeletedBrowser(reqBrowser);
                                                                pvtEntity.setReqDeletedOS(reqOs);
                                                                pvtEntity.setReqDeletedDevice(reqDevice);
                                                                pvtEntity.setReqDeletedReferer(reqReferer);
                                                            }

                                                            return jobGroupJobPvtRepository.saveAll(previouslyMappedJobs)
                                                                    .then(jobGroupJobPvtRepository.save(jobGroupJobPvtEntity))
                                                                    .flatMap(allJobsMapped -> responseSuccessMsg("All Jobs Are Mapped Successfully With Given Job Group", all))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                        })
                                                ));
                                    }

                                    // if all is not selected
                                    else {

                                        //getting List of Jobs From Front
                                        List<String> listOfJobUUID = value.get("jobUUID");

                                        listOfJobUUID.removeIf(s -> s.equals(""));

                                        List<UUID> l_list = new ArrayList<>();
                                        for (String getJobUUID : listOfJobUUID) {
                                            l_list.add(UUID.fromString(getJobUUID));
                                        }

                                        if (!l_list.isEmpty()) {
                                            return jobRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList()
                                                    .flatMap(existingJobs -> {
                                                        // Job UUID List
                                                        List<UUID> jobList = new ArrayList<>();

                                                        for (JobEntity job : existingJobs) {
                                                            jobList.add(job.getUuid());
                                                        }

                                                        if (!jobList.isEmpty()) {

                                                            // job uuid list to show in response
                                                            List<UUID> jobRecords = new ArrayList<>(jobList);

                                                            List<JobGroupJobPvtEntity> listPvt = new ArrayList<>();

                                                            return jobGroupJobPvtRepository.findAllByJobGroupUUIDAndJobUUIDInAndDeletedAtIsNull(jobGroupUUID, jobList)
                                                                    .collectList()
                                                                    .flatMap(jobGroupPvtEntity -> {
                                                                        for (JobGroupJobPvtEntity pvtEntity : jobGroupPvtEntity) {
                                                                            //Removing Existing Job UUID in Job Final List to be saved that does not contain already mapped values
                                                                            jobList.remove(pvtEntity.getJobUUID());
                                                                        }

                                                                        // iterate Job UUIDs for given Job Group
                                                                        for (UUID jobUUID : jobList) {
                                                                            JobGroupJobPvtEntity jobGroupJobPvtEntity = JobGroupJobPvtEntity
                                                                                    .builder()
                                                                                    .jobUUID(jobUUID)
                                                                                    .uuid(UUID.randomUUID())
                                                                                    .jobGroupUUID(jobGroupUUID)
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
                                                                            listPvt.add(jobGroupJobPvtEntity);
                                                                        }

                                                                        return jobGroupJobPvtRepository.saveAll(listPvt)
                                                                                .collectList()
                                                                                .flatMap(groupList -> {

                                                                                    if (!jobList.isEmpty()) {
                                                                                        return responseSuccessMsg("Record Stored Successfully", jobRecords)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                    } else {
                                                                                        return responseInfoMsg("Record Already Exists", jobRecords);
                                                                                    }

                                                                                }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("Job Record does not exist");
                                                        }
                                                    }).switchIfEmpty(responseInfoMsg("The Entered Job Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("The Entered Job Does not exist.Please Contact Developer."));
                                        } else {
                                            return responseInfoMsg("Select Job First");
                                        }

                                    }
                                }).switchIfEmpty(responseInfoMsg("Job Group Record does not exist"))
                                .onErrorResume(err -> responseInfoMsg("Job Group Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    @AuthHasPermission(value = "account_api_v1_job-group-job_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID jobGroupUUID = UUID.fromString(serverRequest.pathVariable("jobGroupUUID").trim());
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

                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                    // if all is given
                    if (all) {
                        return jobGroupRepository.findByUuidAndDeletedAtIsNull(jobGroupUUID)
                                .flatMap(jobGroupEntity -> jobGroupJobPvtRepository.findFirstByJobGroupUUIDAndAllAndDeletedAtIsNull(jobGroupUUID, true)
                                        .flatMap(jobGroupJobPvtEntity -> {

                                            SlaveJobGroupJobDto slaveJobGroupJobDto = SlaveJobGroupJobDto.builder()
                                                    .id(jobGroupJobPvtEntity.getId())
                                                    .version(jobGroupJobPvtEntity.getVersion())
                                                    .uuid(jobGroupJobPvtEntity.getUuid())
                                                    .name("all")
                                                    .jobGroupName(jobGroupEntity.getName())
                                                    .jobGroupUUID(jobGroupJobPvtEntity.getJobGroupUUID())
                                                    .all(jobGroupJobPvtEntity.getAll())
                                                    .createdAt(jobGroupJobPvtEntity.getCreatedAt())
                                                    .createdBy(jobGroupJobPvtEntity.getCreatedBy())
                                                    .updatedBy(jobGroupJobPvtEntity.getUpdatedBy())
                                                    .updatedAt(jobGroupJobPvtEntity.getUpdatedAt())
                                                    .reqCompanyUUID(jobGroupJobPvtEntity.getReqCompanyUUID())
                                                    .reqBranchUUID(jobGroupJobPvtEntity.getReqBranchUUID())
                                                    .reqCreatedIP(jobGroupJobPvtEntity.getReqCreatedIP())
                                                    .reqCreatedPort(jobGroupJobPvtEntity.getReqCreatedPort())
                                                    .reqCreatedBrowser(jobGroupJobPvtEntity.getReqCreatedBrowser())
                                                    .reqCreatedOS(jobGroupJobPvtEntity.getReqCreatedOS())
                                                    .reqCreatedDevice(jobGroupJobPvtEntity.getReqCreatedDevice())
                                                    .reqCreatedReferer(jobGroupJobPvtEntity.getReqCreatedReferer())
                                                    .reqUpdatedIP(jobGroupJobPvtEntity.getReqUpdatedIP())
                                                    .reqUpdatedPort(jobGroupJobPvtEntity.getReqUpdatedPort())
                                                    .reqUpdatedBrowser(jobGroupJobPvtEntity.getReqUpdatedBrowser())
                                                    .reqUpdatedOS(jobGroupJobPvtEntity.getReqUpdatedOS())
                                                    .reqUpdatedDevice(jobGroupJobPvtEntity.getReqUpdatedDevice())
                                                    .reqUpdatedReferer(jobGroupJobPvtEntity.getReqUpdatedReferer())
                                                    .build();

                                            jobGroupJobPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            jobGroupJobPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            jobGroupJobPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            jobGroupJobPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            jobGroupJobPvtEntity.setReqDeletedIP(reqIp);
                                            jobGroupJobPvtEntity.setReqDeletedPort(reqPort);
                                            jobGroupJobPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            jobGroupJobPvtEntity.setReqDeletedOS(reqOs);
                                            jobGroupJobPvtEntity.setReqDeletedDevice(reqDevice);
                                            jobGroupJobPvtEntity.setReqDeletedReferer(reqReferer);

                                            return jobGroupJobPvtRepository.save(jobGroupJobPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", slaveJobGroupJobDto))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                    // if all is not given
                    else {
                        return jobRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("jobUUID").trim()))
                                .flatMap(jobEntity -> jobGroupJobPvtRepository.findFirstByJobGroupUUIDAndJobUUIDAndDeletedAtIsNull(jobGroupUUID, jobEntity.getUuid())
                                        .flatMap(jobGroupJobPvtEntity -> {

                                            jobGroupJobPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            jobGroupJobPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            jobGroupJobPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            jobGroupJobPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            jobGroupJobPvtEntity.setReqDeletedIP(reqIp);
                                            jobGroupJobPvtEntity.setReqDeletedPort(reqPort);
                                            jobGroupJobPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            jobGroupJobPvtEntity.setReqDeletedOS(reqOs);
                                            jobGroupJobPvtEntity.setReqDeletedDevice(reqDevice);
                                            jobGroupJobPvtEntity.setReqDeletedReferer(reqReferer);

                                            return jobGroupJobPvtRepository.save(jobGroupJobPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", jobEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again!"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                }).switchIfEmpty(responseInfoMsg("Unable to read request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));

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

    public Mono<ServerResponse> responseInfoMsg(String msg, Object entity) {
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
}
