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
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupCostCenterPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.CostCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.CostCenterRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCostCenterGroupCostCenterDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCostCenterRepository;
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
@Tag(name = "costCenterGroupCostCenterHandler")
public class CostCenterGroupCostCenterHandler {
    @Autowired
    CostCenterGroupCostCenterPvtRepository costCenterGroupCostCenterPvtRepository;

    @Autowired
    SlaveCostCenterGroupCostCenterPvtRepository slaveCostCenterGroupCostCenterPvtRepository;

    @Autowired
    CostCenterRepository costCenterRepository;

    @Autowired
    SlaveCostCenterRepository slaveCostCenterRepository;

    @Autowired
    CostCenterGroupRepository costCenterGroupRepository;

    @Autowired
    SlaveCostCenterGroupRepository slaveCostCenterGroupRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_cost-center-group-cost-center_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCostCentersAgainstCostCenterGroup(ServerRequest serverRequest) {

        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("costCenterGroupUUID").trim());

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

        // if status is present
        if (!status.isEmpty()) {

            Flux<SlaveCostCenterEntity> costCenterEntityFlux = slaveCostCenterGroupCostCenterPvtRepository
                    .showUnMappedCostCenterRecordsWithStatus(costCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return costCenterEntityFlux
                    .collectList()
                    .flatMap(costCenterEntityDB -> slaveCostCenterRepository.countUnMappedCostCentersRecordsWithStatusFilter(costCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (costCenterEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", costCenterEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {

            Flux<SlaveCostCenterEntity> costCenterEntityFlux = slaveCostCenterGroupCostCenterPvtRepository
                    .showUnMappedCostCenterRecords(costCenterGroupUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return costCenterEntityFlux
                    .collectList()
                    .flatMap(costCenterEntityDB -> slaveCostCenterRepository.countUnMappedCostCentersRecords(costCenterGroupUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (costCenterEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", costCenterEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-group-cost-center_mapped_show")
    //Show Cost Centers for Cost Center Group UUID
    public Mono<ServerResponse> showMappedCostCentersAgainstCostCenterGroup(ServerRequest serverRequest) {
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("costCenterGroupUUID"));

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

        return slaveCostCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                // if cost center group is mapped with all
                .flatMap(costCenterGroupEntity -> slaveCostCenterGroupCostCenterPvtRepository.findByCostCenterGroupUUIDAndAllAndDeletedAtIsNull(costCenterGroupUUID, true)
                        .flatMap(allCostCentersMapped -> {

                            SlaveCostCenterGroupCostCenterDto slaveCostCenterGroupCostCenterDto = SlaveCostCenterGroupCostCenterDto.builder()
                                    .id(allCostCentersMapped.getId())
                                    .version(allCostCentersMapped.getVersion())
                                    .uuid(allCostCentersMapped.getUuid())
                                    .name("all")
                                    .costCenterGroupName(costCenterGroupEntity.getName())
                                    .costCenterGroupUUID(allCostCentersMapped.getCostCenterGroupUUID())
                                    .all(allCostCentersMapped.getAll())
                                    .createdAt(allCostCentersMapped.getCreatedAt())
                                    .createdBy(allCostCentersMapped.getCreatedBy())
                                    .updatedBy(allCostCentersMapped.getUpdatedBy())
                                    .updatedAt(allCostCentersMapped.getUpdatedAt())
                                    .reqCompanyUUID(allCostCentersMapped.getReqCompanyUUID())
                                    .reqBranchUUID(allCostCentersMapped.getReqBranchUUID())
                                    .reqCreatedIP(allCostCentersMapped.getReqCreatedIP())
                                    .reqCreatedPort(allCostCentersMapped.getReqCreatedPort())
                                    .reqCreatedBrowser(allCostCentersMapped.getReqCreatedBrowser())
                                    .reqCreatedOS(allCostCentersMapped.getReqCreatedOS())
                                    .reqCreatedDevice(allCostCentersMapped.getReqCreatedDevice())
                                    .reqCreatedReferer(allCostCentersMapped.getReqCreatedReferer())
                                    .reqUpdatedIP(allCostCentersMapped.getReqUpdatedIP())
                                    .reqUpdatedPort(allCostCentersMapped.getReqUpdatedPort())
                                    .reqUpdatedBrowser(allCostCentersMapped.getReqUpdatedBrowser())
                                    .reqUpdatedOS(allCostCentersMapped.getReqUpdatedOS())
                                    .reqUpdatedDevice(allCostCentersMapped.getReqUpdatedDevice())
                                    .reqUpdatedReferer(allCostCentersMapped.getReqUpdatedReferer())
                                    .build();

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                return slaveCostCenterRepository
                                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveCostCenterGroupCostCenterDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                return slaveCostCenterRepository
                                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveCostCenterGroupCostCenterDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }
                        })

                        // get only mapped records from pvt
                        .switchIfEmpty(Mono.defer(() -> {

                            if (!status.isEmpty()) {
                                Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                        .showMappedCostCenterRecordsWithStatus(costCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveCostCenterEntityFlux
                                        .collectList()
                                        .flatMap(costCenterEntity -> slaveCostCenterRepository.countMappedCostCenterAgainstCostCenterGroupWithStatus(costCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                                .flatMap(count -> {

                                                    if (costCenterEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            } else {
                                Flux<SlaveCostCenterEntity> slaveCostCenterEntityFlux = slaveCostCenterRepository
                                        .showMappedCostCenterRecords(costCenterGroupUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveCostCenterEntityFlux
                                        .collectList()
                                        .flatMap(costCenterEntity -> slaveCostCenterRepository.countMappedCostCenterAgainstCostCenterGroup(costCenterGroupUUID, searchKeyWord, searchKeyWord)
                                                .flatMap(count -> {

                                                    if (costCenterEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records fetched successfully", costCenterEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }
                        }))
                ).switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-group-cost-center_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("costCenterGroupUUID").trim());
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
                                .flatMap(costCenterGroupEntity -> {

//                            // if given cost center group is inactive
//                            if (!costCenterGroupEntity.getStatus()) {
//                                return responseInfoMsg("Cost Center Group status is inactive");
//                            }

                                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                                    // mapping with all records
                                    if (all) {

                                        CostCenterGroupCostCenterPvtEntity costCenterGroupCostCenterPvtEntity = CostCenterGroupCostCenterPvtEntity
                                                .builder()
                                                .uuid(UUID.randomUUID())
                                                .costCenterGroupUUID(costCenterGroupUUID)
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

                                        return costCenterGroupCostCenterPvtRepository.findFirstByCostCenterGroupUUIDAndAllAndDeletedAtIsNull(costCenterGroupUUID, true)
                                                .flatMap(recordAlreadyExists -> responseInfoMsg("Record Already Exists"))
                                                .switchIfEmpty(Mono.defer(() -> costCenterGroupCostCenterPvtRepository.findAllByCostCenterGroupUUIDAndDeletedAtIsNull(costCenterGroupUUID)
                                                        .collectList()
                                                        .flatMap(previouslyMappedCostCenters -> {

                                                            for (CostCenterGroupCostCenterPvtEntity pvtEntity : previouslyMappedCostCenters) {
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

                                                            return costCenterGroupCostCenterPvtRepository.saveAll(previouslyMappedCostCenters)
                                                                    .then(costCenterGroupCostCenterPvtRepository.save(costCenterGroupCostCenterPvtEntity))
                                                                    .flatMap(allCostCentersMapped -> responseSuccessMsg("All Cost Centers Are Mapped Successfully With Given Cost Center Group", all))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                        })
                                                ));
                                    }


                                    // if all is not selected
                                    else {

                                        //getting List of Cost Centers From Front
                                        List<String> listOfCostCenterUUID = value.get("costCenterUUID");

                                        listOfCostCenterUUID.removeIf(s -> s.equals(""));

                                        List<UUID> l_list = new ArrayList<>();
                                        for (String costCenterUUID : listOfCostCenterUUID) {
                                            l_list.add(UUID.fromString(costCenterUUID));
                                        }

                                        if (!l_list.isEmpty()) {
                                            return costCenterRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList()
                                                    .flatMap(existingCostCenters -> {

                                                        // Cost Center UUID List
                                                        List<UUID> costCenterList = new ArrayList<>();

                                                        for (CostCenterEntity costCenter : existingCostCenters) {
                                                            costCenterList.add(costCenter.getUuid());
                                                        }

                                                        if (!costCenterList.isEmpty()) {

                                                            // cost center uuid list to show in response
                                                            List<UUID> costCenterRecords = new ArrayList<>(costCenterList);

                                                            List<CostCenterGroupCostCenterPvtEntity> listPvt = new ArrayList<>();

                                                            return costCenterGroupCostCenterPvtRepository.findAllByCostCenterGroupUUIDAndCostCenterUUIDInAndDeletedAtIsNull(costCenterGroupUUID, costCenterList)
                                                                    .collectList()
                                                                    .flatMap(costCenterGroupPvtEntity -> {
                                                                        for (CostCenterGroupCostCenterPvtEntity pvtEntity : costCenterGroupPvtEntity) {
                                                                            //Removing Existing Cost Center UUID in Cost Center Final List to be saved that does not contain already mapped values
                                                                            costCenterList.remove(pvtEntity.getCostCenterUUID());
                                                                        }

                                                                        // iterate Cost Center UUIDs for given Cost Center Group
                                                                        for (UUID costCenterUUID : costCenterList) {
                                                                            CostCenterGroupCostCenterPvtEntity costCenterGroupCostCenterPvtEntity = CostCenterGroupCostCenterPvtEntity
                                                                                    .builder()
                                                                                    .costCenterUUID(costCenterUUID)
                                                                                    .uuid(UUID.randomUUID())
                                                                                    .costCenterGroupUUID(costCenterGroupUUID)
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
                                                                            listPvt.add(costCenterGroupCostCenterPvtEntity);
                                                                        }

                                                                        return costCenterGroupCostCenterPvtRepository.saveAll(listPvt)
                                                                                .collectList()
                                                                                .flatMap(groupList -> {

                                                                                    if (!costCenterList.isEmpty()) {
                                                                                        return responseSuccessMsg("Record Stored Successfully", costCenterRecords)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                    } else {
                                                                                        return responseInfoMsg("Record Already Exists", costCenterRecords);
                                                                                    }

                                                                                }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("Cost Center Record does not exist");
                                                        }
                                                    }).switchIfEmpty(responseInfoMsg("The Entered Cost Center Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("The Entered Cost Center Does not exist.Please Contact Developer."));
                                        } else {
                                            return responseInfoMsg("Select Cost Center First");
                                        }

                                    }
                                }).switchIfEmpty(responseInfoMsg("Cost Center Group does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Cost Center Group does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_cost-center-group-cost-center_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID costCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("costCenterGroupUUID").trim());
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
                        return costCenterGroupRepository.findByUuidAndDeletedAtIsNull(costCenterGroupUUID)
                                .flatMap(costCenterGroupEntity -> costCenterGroupCostCenterPvtRepository.findFirstByCostCenterGroupUUIDAndAllAndDeletedAtIsNull(costCenterGroupUUID, true)
                                        .flatMap(costCenterGroupCostCenterPvtEntity -> {

                                            SlaveCostCenterGroupCostCenterDto slaveCostCenterGroupCostCenterDto = SlaveCostCenterGroupCostCenterDto.builder()
                                                    .id(costCenterGroupCostCenterPvtEntity.getId())
                                                    .version(costCenterGroupCostCenterPvtEntity.getVersion())
                                                    .uuid(costCenterGroupCostCenterPvtEntity.getUuid())
                                                    .name("all")
                                                    .costCenterGroupName(costCenterGroupEntity.getName())
                                                    .costCenterGroupUUID(costCenterGroupCostCenterPvtEntity.getCostCenterGroupUUID())
                                                    .all(costCenterGroupCostCenterPvtEntity.getAll())
                                                    .createdAt(costCenterGroupCostCenterPvtEntity.getCreatedAt())
                                                    .createdBy(costCenterGroupCostCenterPvtEntity.getCreatedBy())
                                                    .updatedBy(costCenterGroupCostCenterPvtEntity.getUpdatedBy())
                                                    .updatedAt(costCenterGroupCostCenterPvtEntity.getUpdatedAt())
                                                    .reqCompanyUUID(costCenterGroupCostCenterPvtEntity.getReqCompanyUUID())
                                                    .reqBranchUUID(costCenterGroupCostCenterPvtEntity.getReqBranchUUID())
                                                    .reqCreatedIP(costCenterGroupCostCenterPvtEntity.getReqCreatedIP())
                                                    .reqCreatedPort(costCenterGroupCostCenterPvtEntity.getReqCreatedPort())
                                                    .reqCreatedBrowser(costCenterGroupCostCenterPvtEntity.getReqCreatedBrowser())
                                                    .reqCreatedOS(costCenterGroupCostCenterPvtEntity.getReqCreatedOS())
                                                    .reqCreatedDevice(costCenterGroupCostCenterPvtEntity.getReqCreatedDevice())
                                                    .reqCreatedReferer(costCenterGroupCostCenterPvtEntity.getReqCreatedReferer())
                                                    .reqUpdatedIP(costCenterGroupCostCenterPvtEntity.getReqUpdatedIP())
                                                    .reqUpdatedPort(costCenterGroupCostCenterPvtEntity.getReqUpdatedPort())
                                                    .reqUpdatedBrowser(costCenterGroupCostCenterPvtEntity.getReqUpdatedBrowser())
                                                    .reqUpdatedOS(costCenterGroupCostCenterPvtEntity.getReqUpdatedOS())
                                                    .reqUpdatedDevice(costCenterGroupCostCenterPvtEntity.getReqUpdatedDevice())
                                                    .reqUpdatedReferer(costCenterGroupCostCenterPvtEntity.getReqUpdatedReferer())
                                                    .build();

                                            costCenterGroupCostCenterPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            costCenterGroupCostCenterPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            costCenterGroupCostCenterPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            costCenterGroupCostCenterPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedIP(reqIp);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedPort(reqPort);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedOS(reqOs);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedDevice(reqDevice);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedReferer(reqReferer);

                                            return costCenterGroupCostCenterPvtRepository.save(costCenterGroupCostCenterPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", slaveCostCenterGroupCostCenterDto))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                    // if all is not given
                    else {
                        return costCenterRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("costCenterUUID").trim()))
                                .flatMap(costCenterEntity -> costCenterGroupCostCenterPvtRepository.findFirstByCostCenterGroupUUIDAndCostCenterUUIDAndDeletedAtIsNull(costCenterGroupUUID, costCenterEntity.getUuid())
                                        .flatMap(costCenterGroupCostCenterPvtEntity -> {

                                            costCenterGroupCostCenterPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            costCenterGroupCostCenterPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            costCenterGroupCostCenterPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            costCenterGroupCostCenterPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedIP(reqIp);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedPort(reqPort);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedOS(reqOs);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedDevice(reqDevice);
                                            costCenterGroupCostCenterPvtEntity.setReqDeletedReferer(reqReferer);

                                            return costCenterGroupCostCenterPvtRepository.save(costCenterGroupCostCenterPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", costCenterEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again!"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                }).switchIfEmpty(responseInfoMsg("Unable to read request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
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
}
