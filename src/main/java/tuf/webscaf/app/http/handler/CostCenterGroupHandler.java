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
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.CostCenterRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherCostCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherCostCenterGroupPvtRepository;
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
@Tag(name = "costCenterGroupHandler")
public class CostCenterGroupHandler {
    @Autowired
    CostCenterGroupRepository costCenterGroupRepository;

    @Autowired
    SlaveCostCenterGroupRepository slaveCostCenterGroupRepository;

    @Autowired
    SlaveVoucherCostCenterGroupPvtRepository slaveVoucherCostCenterGroupPvtRepository;

    @Autowired
    VoucherCostCenterGroupPvtRepository voucherCostCenterGroupPvtRepository;

    @Autowired
    CostCenterGroupCostCenterPvtRepository costCenterGroupCostCenterPvtRepository;

    @Autowired
    CostCenterRepository costCenterRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_index")
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        //      Return All Cost Center Group
        if (!status.isEmpty()) {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFluxOfStatus = slaveCostCenterGroupRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveCostCenterGroupEntityFluxOfStatus
                    .collectList()
                    .flatMap(slaveCostCenterGroupEntity ->
                            slaveCostCenterGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (slaveCostCenterGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveCostCenterGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

        }
//      Return All Cost Center Group according to given value
        else {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(slaveCostCenterGroupEntity ->
                            slaveCostCenterGroupRepository
                                    .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (slaveCostCenterGroupEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully.", slaveCostCenterGroupEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact to developer"));
        }
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_active_index")
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


        Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFluxOfStatus = slaveCostCenterGroupRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE);

        return slaveCostCenterGroupEntityFluxOfStatus
                .collectList()
                .flatMap(slaveCostCenterGroupEntity -> slaveCostCenterGroupRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                        .flatMap(count -> {
                            if (slaveCostCenterGroupEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);

                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", slaveCostCenterGroupEntity, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact to developer"));

    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCostCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_cost-center_mapped_show")
    //Show Cost Center Groups for Cost Center Id
    public Mono<ServerResponse> listOfCostCenterGroups(ServerRequest serverRequest) {
        UUID costCenterUUID = UUID.fromString(serverRequest.pathVariable("costCenterUUID").trim());

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .listOfCostCenterGroupsAgainstCostCenterWithStatus(costCenterUUID, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository.countCostCenterGroupAgainstCostCenterWithStatus(costCenterUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", costCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveCostCenterGroupEntity> slaveCostCenterGroupEntityFlux = slaveCostCenterGroupRepository
                    .listOfCostCenterGroupsAgainstCostCenter(costCenterUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveCostCenterGroupEntityFlux
                    .collectList()
                    .flatMap(costCenterGroupEntity -> slaveCostCenterGroupRepository.countCostCenterGroupAgainstCostCenter(costCenterUUID, searchKeyWord)
                            .flatMap(count -> {

                                if (costCenterGroupEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully.", costCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_store")
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

                    CostCenterGroupEntity costCenterGroupEntity = CostCenterGroupEntity.builder()
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

                    return costCenterGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(costCenterGroupEntity.getName())
                            .flatMap(key -> responseInfoMsg("Name already exist"))
                            .switchIfEmpty(Mono.defer(() -> costCenterGroupRepository.save(costCenterGroupEntity)
                                    .flatMap(configSave -> responseSuccessMsg("Record Stored Successfully.", costCenterGroupEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please Try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                .flatMap(value -> costCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                        .flatMap(previousCostCenterGroupEntity -> {

                            CostCenterGroupEntity updatedCostCenterGroupEntity = CostCenterGroupEntity.builder()
                                    .uuid(previousCostCenterGroupEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousCostCenterGroupEntity.getCreatedAt())
                                    .createdBy(previousCostCenterGroupEntity.getCreatedBy())
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedBy(UUID.fromString(userId))
                                    .reqCreatedIP(previousCostCenterGroupEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousCostCenterGroupEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCostCenterGroupEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCostCenterGroupEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousCostCenterGroupEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCostCenterGroupEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCostCenterGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCostCenterGroupEntity.setDeletedBy(UUID.fromString(userId));
                            previousCostCenterGroupEntity.setReqDeletedIP(reqIp);
                            previousCostCenterGroupEntity.setReqDeletedPort(reqPort);
                            previousCostCenterGroupEntity.setReqDeletedBrowser(reqBrowser);
                            previousCostCenterGroupEntity.setReqDeletedOS(reqOs);
                            previousCostCenterGroupEntity.setReqDeletedDevice(reqDevice);
                            previousCostCenterGroupEntity.setReqDeletedReferer(reqReferer);

                            return costCenterGroupRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCostCenterGroupEntity.getName(), costCenterGroupUUID)
                                    .flatMap(entity -> responseInfoMsg("Name Already Exist"))
                                    .switchIfEmpty(Mono.defer(() -> costCenterGroupRepository.save(previousCostCenterGroupEntity)
                                            .then(costCenterGroupRepository.save(updatedCostCenterGroupEntity))
                                            .flatMap(costCenterGroupEntityDB -> responseSuccessMsg("Record Updated Successfully.", costCenterGroupEntityDB))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record .Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_cost-center-groups_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
        return costCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                //check if Cost Group Exists in Voucher Pvt Table
                .flatMap(costCenterGroupEntity -> voucherCostCenterGroupPvtRepository.findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(costCenterGroupEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> costCenterGroupCostCenterPvtRepository.findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(costCenterGroupEntity.getUuid())
                                .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        //deleting Voucher Group Record
                        .switchIfEmpty(Mono.defer(() -> {

                                    costCenterGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    costCenterGroupEntity.setDeletedBy(UUID.fromString(userId));
                                    costCenterGroupEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    costCenterGroupEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    costCenterGroupEntity.setReqDeletedIP(reqIp);
                                    costCenterGroupEntity.setReqDeletedPort(reqPort);
                                    costCenterGroupEntity.setReqDeletedBrowser(reqBrowser);
                                    costCenterGroupEntity.setReqDeletedOS(reqOs);
                                    costCenterGroupEntity.setReqDeletedDevice(reqDevice);
                                    costCenterGroupEntity.setReqDeletedReferer(reqReferer);

                                    return costCenterGroupRepository.save(costCenterGroupEntity)
                                            .flatMap(saveEntity -> responseSuccessMsg("Record deleted successfully", saveEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                                })
                        )).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }


    @AuthHasPermission(value = "account_api_v1_cost-center-groups_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return costCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                            .flatMap(previousCostCenterGroupEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCostCenterGroupEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CostCenterGroupEntity updatedCostCenterGroupEntity = CostCenterGroupEntity.builder()
                                        .uuid(previousCostCenterGroupEntity.getUuid())
                                        .name(previousCostCenterGroupEntity.getName())
                                        .description(previousCostCenterGroupEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdAt(previousCostCenterGroupEntity.getCreatedAt())
                                        .createdBy(previousCostCenterGroupEntity.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userId))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCostCenterGroupEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCostCenterGroupEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCostCenterGroupEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCostCenterGroupEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCostCenterGroupEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCostCenterGroupEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCostCenterGroupEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCostCenterGroupEntity.setDeletedBy(UUID.fromString(userId));
                                previousCostCenterGroupEntity.setReqDeletedIP(reqIp);
                                previousCostCenterGroupEntity.setReqDeletedPort(reqPort);
                                previousCostCenterGroupEntity.setReqDeletedBrowser(reqBrowser);
                                previousCostCenterGroupEntity.setReqDeletedOS(reqOs);
                                previousCostCenterGroupEntity.setReqDeletedDevice(reqDevice);
                                previousCostCenterGroupEntity.setReqDeletedReferer(reqReferer);

                                return costCenterGroupRepository.save(previousCostCenterGroupEntity)
                                        .then(costCenterGroupRepository.save(updatedCostCenterGroupEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to Update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please contact developer."));
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
