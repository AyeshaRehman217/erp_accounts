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
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherSubAccountGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.SubAccountGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherSubAccountGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveSubAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherSubAccountGroupPvtRepository;
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
@Tag(name = "voucherSubAccountGroupHandler")
public class VoucherSubAccountGroupHandler {
    @Autowired
    VoucherSubAccountGroupPvtRepository voucherSubAccountGroupPvtRepository;

    @Autowired
    SlaveVoucherSubAccountGroupPvtRepository slaveVoucherSubAccountGroupPvtRepository;

    @Autowired
    SubAccountGroupRepository subAccountGroupRepository;

    @Autowired
    SlaveSubAccountGroupRepository slaveSubAccountGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-sub-account-groups_un-mapped_show")
    public Mono<ServerResponse> showUnMappedSubAccountGroupsAgainstVoucher(ServerRequest serverRequest) {
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


        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupRepository
                    .showUnMappedSubAccountGroupListWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(subAccountGroupEntity -> slaveSubAccountGroupRepository
                            .countUnMappedSubAccountGroupRecordsWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (subAccountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subAccountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupRepository
                    .showUnMappedSubAccountGroupList(voucherUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(subAccountGroupEntity -> slaveSubAccountGroupRepository
                            .countUnMappedSubAccountGroupRecords(voucherUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (subAccountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subAccountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    //Show Mapped Account Groups for given Voucher UUID
    @AuthHasPermission(value = "account_api_v1_voucher-sub-account-groups_mapped_show")
    public Mono<ServerResponse> showMappedSubAccountGroupsAgainstVoucher(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size);
        if (!status.isEmpty()) {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupRepository
                    .showMappedSubAccountGroupsWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(subAccountGroupEntity -> slaveSubAccountGroupRepository.countMappedSubAccountGroupListWithStatus(voucherUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (subAccountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subAccountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        } else {
            Flux<SlaveSubAccountGroupEntity> slaveSubAccountGroupEntityFlux = slaveSubAccountGroupRepository
                    .showMappedSubAccountGroups(voucherUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveSubAccountGroupEntityFlux
                    .collectList()
                    .flatMap(subAccountGroupEntity -> slaveSubAccountGroupRepository.countMappedSubAccountGroupList(voucherUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (subAccountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subAccountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-sub-account-groups_store")
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

                            // if given voucher is inactive
                            if (!voucherEntity.getStatus()) {
                                return responseInfoMsg("Voucher status is inactive");
                            }

                            //getting List of Sub Account Group uuids From Front
                            List<String> listSubAccountGroup = value.get("subAccountGroupUUID");

                            //removing any empty String from the Front List
                            listSubAccountGroup.removeIf(s -> s.equals(""));

                            //Creating an Empty List to add all the UUID from Front
                            List<UUID> l_list = new ArrayList<>();
                            //Looping Through all the Sub Account Group List and add in Empty List
                            for (String subAccountGroupUUID : listSubAccountGroup) {
                                l_list.add(UUID.fromString(subAccountGroupUUID));
                            }

                            //If the List is not empty
                            if (!l_list.isEmpty()) {
                                //Check if Sub Account Group Records exist
                                return subAccountGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                        .collectList()
                                        .flatMap(existingSubAccountGroups -> {
                                            // Sub Account Group UUID List
                                            List<UUID> subAccountGroupList = new ArrayList<>();
                                            //If the Sub Account Group UUID exists fetch and save it in another list
                                            for (SubAccountGroupEntity subAccountGroups : existingSubAccountGroups) {

                                                // if sub account group status is inactive
                                                if (!subAccountGroups.getStatus()) {
                                                    return responseInfoMsg("Sub Account group status is inactive in " + subAccountGroups.getName());
                                                }

                                                // add uuid in sub account groups list
                                                subAccountGroupList.add(subAccountGroups.getUuid());
                                            }

                                            //check if Final Sub Account Group list is not empty
                                            if (!subAccountGroupList.isEmpty()) {

                                                // sub account group uuid list to show in response
                                                List<UUID> returningSubAccountGroupRecords = new ArrayList<>(subAccountGroupList);

                                                List<VoucherSubAccountGroupPvtEntity> listPvt = new ArrayList<>();
                                                //All the existing records that exist in Pvt Table
                                                return voucherSubAccountGroupPvtRepository.findAllByVoucherUUIDAndSubAccountGroupUUIDInAndDeletedAtIsNull(voucherUUID, subAccountGroupList)
                                                        .collectList()
                                                        .flatMap(removelist -> {
                                                            for (VoucherSubAccountGroupPvtEntity pvtEntity : removelist) {
                                                                //removing records from the List from front that contain already mapped uuids
                                                                subAccountGroupList.remove(pvtEntity.getSubAccountGroupUUID());
                                                            }

                                                            for (UUID subAccountGroup : subAccountGroupList) {
                                                                VoucherSubAccountGroupPvtEntity subAccountGroupWithGroupEntity = VoucherSubAccountGroupPvtEntity
                                                                        .builder()
                                                                        .voucherUUID(voucherUUID)
                                                                        .subAccountGroupUUID(subAccountGroup)
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

                                                                listPvt.add(subAccountGroupWithGroupEntity);
                                                            }

                                                            return voucherSubAccountGroupPvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(groupList -> voucherSubAccountGroupPvtRepository.findByVoucherUUIDAndDeletedAtIsNull(voucherUUID)
                                                                            .collectList()
                                                                            .flatMap(mappedRecords -> {
                                                                                List<UUID> resultList = new ArrayList<>();
                                                                                for (VoucherSubAccountGroupPvtEntity entity : mappedRecords) {
                                                                                    resultList.add(entity.getSubAccountGroupUUID());
                                                                                }

                                                                                return subAccountGroupRepository.findAllByUuidInAndDeletedAtIsNull(resultList)
                                                                                        .collectList()
                                                                                        .flatMap(subAccountGroupRecords -> {
                                                                                            if (!subAccountGroupList.isEmpty()) {
                                                                                                return responseSuccessMsg("Record Stored Successfully", returningSubAccountGroupRecords);
                                                                                            } else {
                                                                                                return responseSuccessMsg("Record Already exists", returningSubAccountGroupRecords);
                                                                                            }
                                                                                        });
                                                                            })
                                                                    );
                                                        }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                            } else {
                                                return responseInfoMsg("Account Group Record does not exist");
                                            }
                                        }).switchIfEmpty(responseInfoMsg("The Entered Account Group Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("The Entered Account Group Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Account Groups First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-sub-account-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.queryParam("subAccountGroupUUID").map(String::toString).orElse(""));
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
                .flatMap(value -> subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                        .flatMap(subAccountGroupEntity -> voucherSubAccountGroupPvtRepository
                                .findFirstByVoucherUUIDAndSubAccountGroupUUIDAndDeletedAtIsNull(voucherUUID, subAccountGroupEntity.getUuid())
                                .flatMap(voucherSubAccountGroupPvtEntity -> {

                                    voucherSubAccountGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    voucherSubAccountGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                    voucherSubAccountGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    voucherSubAccountGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    voucherSubAccountGroupPvtEntity.setReqDeletedIP(reqIp);
                                    voucherSubAccountGroupPvtEntity.setReqDeletedPort(reqPort);
                                    voucherSubAccountGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                    voucherSubAccountGroupPvtEntity.setReqDeletedOS(reqOs);
                                    voucherSubAccountGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                    voucherSubAccountGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                    return voucherSubAccountGroupPvtRepository.save(voucherSubAccountGroupPvtEntity)
                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", subAccountGroupEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
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
