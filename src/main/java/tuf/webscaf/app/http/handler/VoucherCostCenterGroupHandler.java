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
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherCostCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherCostCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCostCenterGroupPvtRepository;
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
@Tag(name = "voucherCostCenterGroupHandler")
public class VoucherCostCenterGroupHandler {
    @Autowired
    VoucherCostCenterGroupPvtRepository voucherCostCenterGroupPvtRepository;

    @Autowired
    SlaveVoucherCostCenterGroupPvtRepository slaveVoucherCostCenterGroupPvtRepository;

    @Autowired
    CostCenterGroupRepository costCenterGroupRepository;

    @Autowired
    SlaveCostCenterGroupRepository slaveCostCenterGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-cost-center-group_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCostCenterGroupsAgainstVoucher(ServerRequest serverRequest) {

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
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .showUnMappedCostCenterGroupListWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository
                            .countUnMappedCostCenterGroupRecordsWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", costCenterGroupEntity, count);
                                }
                            })
                    )
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."))
                    .switchIfEmpty(responseInfoMsg("Unable to read request"));
        }

        // if status is not present
        else {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .showUnMappedCostCenterGroupList(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository
                            .countUnMappedCostCenterGroupRecords(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", costCenterGroupEntity, count);
                                }
                            })
                    )
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."))
                    .switchIfEmpty(responseInfoMsg("Unable to read request"));
        }
    }

    //Show Mapped Cost Center Groups for given Voucher Id
    @AuthHasPermission(value = "account_api_v1_voucher-cost-center-group_mapped_show")
    public Mono<ServerResponse> showMappedCostCenterGroupsAgainstVoucher(ServerRequest serverRequest) {
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
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .showMappedCostCenterGroupsWithStatus(voucherUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository.countMappedCostCenterGroupsWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully.", costCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .showMappedCostCenterGroups(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository.countMappedCostCenterGroups(voucherUUID, searchKeyWord)
                            .flatMap(count -> {

                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully.", costCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-cost-center-group_store")
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

                                    //getting List of Cost Center Groups From Front
                                    List<String> listOfCostCenterGroupUUID = value.get("costCenterGroupUUID");

                                    listOfCostCenterGroupUUID.removeIf(s -> s.equals(""));

                                    List<UUID> l_list = new ArrayList<>();
                                    for (String costCenterGroupUUID : listOfCostCenterGroupUUID) {
                                        l_list.add(UUID.fromString(costCenterGroupUUID));
                                    }

                                    if (!l_list.isEmpty()) {
                                        return costCenterGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                .collectList()
                                                .flatMap(existingCostCenterGroups -> {
                                                    // Cost Center Group UUID List
                                                    List<UUID> costCenterGroupList = new ArrayList<>();

                                                    for (CostCenterGroupEntity costCenterGroup : existingCostCenterGroups) {
                                                        costCenterGroupList.add(costCenterGroup.getUuid());
                                                    }

                                                    if (!costCenterGroupList.isEmpty()) {

                                                        // cost center group uuid list to show in response
                                                        List<UUID> costCenterGroupRecords = new ArrayList<>(costCenterGroupList);

                                                        List<VoucherCostCenterGroupPvtEntity> listPvt = new ArrayList<>();

                                                        return voucherCostCenterGroupPvtRepository.findAllByVoucherUUIDAndCostCenterGroupUUIDInAndDeletedAtIsNull(voucherUUID, costCenterGroupList)
                                                                .collectList()
                                                                .flatMap(voucherPvtEntity -> {
                                                                    for (VoucherCostCenterGroupPvtEntity pvtEntity : voucherPvtEntity) {
                                                                        //Removing Existing Cost Center Group UUID in Cost Center Group Final List to be saved that does not contain already mapped values
                                                                        costCenterGroupList.remove(pvtEntity.getCostCenterGroupUUID());
                                                                    }

                                                                    // iterate Cost Center Group UUIDs for given Voucher
                                                                    for (UUID costCenterGroupUUID : costCenterGroupList) {
                                                                        VoucherCostCenterGroupPvtEntity voucherCostCenterGroupPvtEntity = VoucherCostCenterGroupPvtEntity
                                                                                .builder()
                                                                                .costCenterGroupUUID(costCenterGroupUUID)
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
                                                                        listPvt.add(voucherCostCenterGroupPvtEntity);
                                                                    }

                                                                    return voucherCostCenterGroupPvtRepository.saveAll(listPvt)
                                                                            .collectList()
                                                                            .flatMap(groupList -> {

                                                                                if (!costCenterGroupList.isEmpty()) {
                                                                                    return responseSuccessMsg("Record Stored Successfully", costCenterGroupRecords)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                } else {
                                                                                    return responseInfoMsg("Record Already Exists", costCenterGroupRecords);
                                                                                }

                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                });
                                                    } else {
                                                        return responseInfoMsg("Cost Center Group Record does not exist");
                                                    }
                                                }).switchIfEmpty(responseInfoMsg("The Entered Cost Center Group Does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("The Entered Cost Center Group Does not exist.Please Contact Developer."));
                                    } else {
                                        return responseInfoMsg("Select Cost Center Group First");
                                    }
                                }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-cost-center-group_delete")
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
                .flatMap(value -> costCenterGroupRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("costCenterGroupUUID").trim()))
                        .flatMap(costCenterGroupEntity -> voucherCostCenterGroupPvtRepository
                                .findFirstByVoucherUUIDAndCostCenterGroupUUIDAndDeletedAtIsNull(voucherUUID, costCenterGroupEntity.getUuid())
                                .flatMap(voucherCostCenterGroupPvtEntity -> {

                                    voucherCostCenterGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    voucherCostCenterGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                    voucherCostCenterGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    voucherCostCenterGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    voucherCostCenterGroupPvtEntity.setReqDeletedIP(reqIp);
                                    voucherCostCenterGroupPvtEntity.setReqDeletedPort(reqPort);
                                    voucherCostCenterGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                    voucherCostCenterGroupPvtEntity.setReqDeletedOS(reqOs);
                                    voucherCostCenterGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                    voucherCostCenterGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                    return voucherCostCenterGroupPvtRepository.save(voucherCostCenterGroupPvtEntity)
                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", costCenterGroupEntity))
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
