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
import tuf.webscaf.app.dbContext.master.entity.VoucherJobGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.JobGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherJobGroupPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveJobGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherJobGroupPvtRepository;
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
@Tag(name = "voucherJobGroupHandler")
public class VoucherJobGroupHandler {
    @Autowired
    VoucherJobGroupPvtRepository voucherJobGroupPvtRepository;

    @Autowired
    SlaveVoucherJobGroupPvtRepository slaveVoucherJobGroupPvtRepository;

    @Autowired
    JobGroupRepository jobGroupRepository;

    @Autowired
    SlaveJobGroupRepository slaveJobGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-job-group_un-mapped_show")
    public Mono<ServerResponse> showUnMappedJobGroupsAgainstVoucher(ServerRequest serverRequest) {
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


        // if status is given in optional query parameter
        if (!status.isEmpty()) {
            Flux<SlaveJobGroupEntity> slaveJobGroupEntityFlux = slaveJobGroupRepository
                    .showUnMappedJobGroupListAgainstVoucherWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobGroupEntityFlux
                    .collectList()
                    .flatMap(jobGroupEntity -> slaveJobGroupRepository
                            .countUnMappedJobGroupsAgainstVoucherWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (jobGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", jobGroupEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not given
        else {
            Flux<SlaveJobGroupEntity> slaveJobGroupEntityFlux = slaveJobGroupRepository
                    .showUnMappedJobGroupListAgainstVoucher(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveJobGroupEntityFlux
                    .collectList()
                    .flatMap(jobGroupEntity -> slaveJobGroupRepository
                            .countUnMappedJobGroupsAgainstVoucher(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (jobGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", jobGroupEntity, count);
                                }
                            })).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-job-group_mapped_show")
    public Mono<ServerResponse> showMappedJobGroupsAgainstVoucher(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("");

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size);

        if (!status.isEmpty()) {
            Flux<SlaveJobGroupEntity> slaveVoucherEntityFlux = slaveJobGroupRepository
                    .showMappedJobGroupListWithStatusAgainstVoucher(voucherUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveJobGroupRepository.countMappedJobGroupListWithStatusAgainstVoucher(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        } else {
            Flux<SlaveJobGroupEntity> slaveVoucherEntityFlux = slaveJobGroupRepository
                    .showMappedJobGroupListAgainstVoucher(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveVoucherEntityFlux
                    .collectList()
                    .flatMap(voucherEntity -> slaveJobGroupRepository.countMappedJobGroupListAgainstVoucher(voucherUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (voucherEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", voucherEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-job-group_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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
                .flatMap(value -> voucherRepository.findByUuidAndDeletedAtIsNull(voucherUUID)
                                .flatMap(voucherEntity -> {

//                            // if given voucher is inactive
//                            if (!voucherEntity.getStatus()) {
//                                return responseInfoMsg("Voucher status is inactive");
//                            }

                                    //getting List of Job Groups From Front
                                    List<String> listOfJobGroupUUID = value.get("jobGroupUUID");

                                    listOfJobGroupUUID.removeIf(s -> s.equals(""));

                                    List<UUID> l_list = new ArrayList<>();
                                    for (String jobGroupUUID : listOfJobGroupUUID) {
                                        l_list.add(UUID.fromString(jobGroupUUID));
                                    }

                                    if (!l_list.isEmpty()) {
                                        return jobGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                .collectList()
                                                .flatMap(existingJobGroups -> {
                                                    // Job Group UUID List
                                                    List<UUID> jobGroupList = new ArrayList<>();

                                                    for (JobGroupEntity jobGroup : existingJobGroups) {
                                                        jobGroupList.add(jobGroup.getUuid());
                                                    }

                                                    if (!jobGroupList.isEmpty()) {

                                                        // job group uuid list to show in response
                                                        List<UUID> jobGroupRecords = new ArrayList<>(jobGroupList);

                                                        List<VoucherJobGroupPvtEntity> listPvt = new ArrayList<>();

                                                        return voucherJobGroupPvtRepository.findAllByVoucherUUIDAndJobGroupUUIDInAndDeletedAtIsNull(voucherUUID, jobGroupList)
                                                                .collectList()
                                                                .flatMap(voucherPvtEntity -> {
                                                                    for (VoucherJobGroupPvtEntity pvtEntity : voucherPvtEntity) {
                                                                        //Removing Existing Job Group UUID in Job Group Final List to be saved that does not contain already mapped values
                                                                        jobGroupList.remove(pvtEntity.getJobGroupUUID());
                                                                    }

                                                                    // iterate Job Group UUIDs for given Voucher
                                                                    for (UUID jobGroupUUID : jobGroupList) {
                                                                        VoucherJobGroupPvtEntity voucherJobGroupPvtEntity = VoucherJobGroupPvtEntity
                                                                                .builder()
                                                                                .jobGroupUUID(jobGroupUUID)
                                                                                .uuid(UUID.randomUUID())
                                                                                .voucherUUID(voucherUUID)
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
                                                                        listPvt.add(voucherJobGroupPvtEntity);
                                                                    }

                                                                    return voucherJobGroupPvtRepository.saveAll(listPvt)
                                                                            .collectList()
                                                                            .flatMap(groupList -> {

                                                                                if (!jobGroupList.isEmpty()) {
                                                                                    return responseSuccessMsg("Record Stored Successfully", jobGroupRecords)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                } else {
                                                                                    return responseInfoMsg("Record Already Exists", jobGroupRecords);
                                                                                }

                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                });
                                                    } else {
                                                        return responseInfoMsg("Job Group Record does not exist");
                                                    }
                                                }).switchIfEmpty(responseInfoMsg("The Entered Job Group Does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("The Entered Job Group Does not exist.Please Contact Developer."));
                                    } else {
                                        return responseInfoMsg("Select Job Group First");
                                    }
                                }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-job-group_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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
                .flatMap(value -> jobGroupRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("jobGroupUUID").trim()))
                        .flatMap(jobGroupEntity -> voucherJobGroupPvtRepository
                                .findFirstByVoucherUUIDAndJobGroupUUIDAndDeletedAtIsNull(voucherUUID, jobGroupEntity.getUuid())
                                .flatMap(voucherJobGroupPvtEntity -> {

                                    voucherJobGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    voucherJobGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                    voucherJobGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    voucherJobGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    voucherJobGroupPvtEntity.setReqDeletedIP(reqIp);
                                    voucherJobGroupPvtEntity.setReqDeletedPort(reqPort);
                                    voucherJobGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                    voucherJobGroupPvtEntity.setReqDeletedOS(reqOs);
                                    voucherJobGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                    voucherJobGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                    return voucherJobGroupPvtRepository.save(voucherJobGroupPvtEntity)
                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully!", jobGroupEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.there is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer"));
                                })
                        ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))

                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
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
