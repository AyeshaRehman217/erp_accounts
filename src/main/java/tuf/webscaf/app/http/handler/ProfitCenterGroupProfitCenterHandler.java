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
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupProfitCenterPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveProfitCenterGroupProfitCenterDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveProfitCenterGroupProfitCenterDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterRepository;
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
@Tag(name = "profitCenterGroupProfitCenterHandler")
public class ProfitCenterGroupProfitCenterHandler {

    @Autowired
    ProfitCenterGroupProfitCenterPvtRepository profitCenterGroupProfitCenterPvtRepository;

    @Autowired
    SlaveProfitCenterGroupProfitCenterPvtRepository slaveProfitCenterGroupProfitCenterPvtRepository;

    @Autowired
    ProfitCenterRepository profitCenterRepository;

    @Autowired
    SlaveProfitCenterRepository slaveProfitCenterRepository;

    @Autowired
    ProfitCenterGroupRepository profitCenterGroupRepository;

    @Autowired
    SlaveProfitCenterGroupRepository slaveProfitCenterGroupRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_profit-center-group-profit-center_un-mapped_show")
    public Mono<ServerResponse> showUnMappedProfitCenterAgainstProfitCenterGroup(ServerRequest serverRequest) {

        UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("profitCenterGroupUUID").trim());

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

        // if status is given in query parameter
        if (!status.isEmpty()) {

            Flux<SlaveProfitCenterEntity> profitCenterEntityFlux = slaveProfitCenterGroupProfitCenterPvtRepository
                    .showUnMappedProfitCenterRecordsWithStatus(profitCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return profitCenterEntityFlux
                    .collectList()
                    .flatMap(profitCenterEntityDB -> slaveProfitCenterRepository.countUnMappedProfitCenterAgainstProfitCenterGroupWithStatus(profitCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (profitCenterEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", profitCenterEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not given
        else {

            Flux<SlaveProfitCenterEntity> profitCenterEntityFlux = slaveProfitCenterGroupProfitCenterPvtRepository
                    .showUnMappedProfitCenterRecords(profitCenterGroupUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return profitCenterEntityFlux
                    .collectList()
                    .flatMap(profitCenterEntityDB -> slaveProfitCenterRepository.countUnMappedProfitCenterAgainstProfitCenterGroup(profitCenterGroupUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (profitCenterEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records fetched successfully!", profitCenterEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-group-profit-center_mapped_show")
    public Mono<ServerResponse> showMappedProfitCenterAgainstProfitCenterGroup(ServerRequest serverRequest) {
        UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("profitCenterGroupUUID").trim());

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

        return slaveProfitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                // if profit center group is mapped with all
                .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupProfitCenterPvtRepository.findByProfitCenterGroupUUIDAndAllAndDeletedAtIsNull(profitCenterGroupUUID, true)
                        .flatMap(allProfitCentersMapped -> {
                            
                            SlaveProfitCenterGroupProfitCenterDto slaveProfitCenterGroupProfitCenterDto = SlaveProfitCenterGroupProfitCenterDto.builder()
                                    .id(allProfitCentersMapped.getId())
                                    .version(allProfitCentersMapped.getVersion())
                                    .uuid(allProfitCentersMapped.getUuid())
                                    .name("all")
                                    .profitCenterGroupName(profitCenterGroupEntity.getName())
                                    .profitCenterGroupUUID(allProfitCentersMapped.getProfitCenterGroupUUID())
                                    .all(allProfitCentersMapped.getAll())
                                    .createdAt(allProfitCentersMapped.getCreatedAt())
                                    .createdBy(allProfitCentersMapped.getCreatedBy())
                                    .updatedBy(allProfitCentersMapped.getUpdatedBy())
                                    .updatedAt(allProfitCentersMapped.getUpdatedAt())
                                    .reqCompanyUUID(allProfitCentersMapped.getReqCompanyUUID())
                                    .reqBranchUUID(allProfitCentersMapped.getReqBranchUUID())
                                    .reqCreatedIP(allProfitCentersMapped.getReqCreatedIP())
                                    .reqCreatedPort(allProfitCentersMapped.getReqCreatedPort())
                                    .reqCreatedBrowser(allProfitCentersMapped.getReqCreatedBrowser())
                                    .reqCreatedOS(allProfitCentersMapped.getReqCreatedOS())
                                    .reqCreatedDevice(allProfitCentersMapped.getReqCreatedDevice())
                                    .reqCreatedReferer(allProfitCentersMapped.getReqCreatedReferer())
                                    .reqUpdatedIP(allProfitCentersMapped.getReqUpdatedIP())
                                    .reqUpdatedPort(allProfitCentersMapped.getReqUpdatedPort())
                                    .reqUpdatedBrowser(allProfitCentersMapped.getReqUpdatedBrowser())
                                    .reqUpdatedOS(allProfitCentersMapped.getReqUpdatedOS())
                                    .reqUpdatedDevice(allProfitCentersMapped.getReqUpdatedDevice())
                                    .reqUpdatedReferer(allProfitCentersMapped.getReqUpdatedReferer())
                                    .build();

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                return slaveProfitCenterRepository
                                        .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveProfitCenterGroupProfitCenterDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                return slaveProfitCenterRepository
                                        .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveProfitCenterGroupProfitCenterDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }
                        })

                        // get only mapped records from pvt
                        .switchIfEmpty(Mono.defer(() -> {

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                        .showMappedProfitCenterRecordsWithStatus(profitCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveProfitCenterEntityFlux
                                        .collectList()
                                        .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countMappedProfitCenterAgainstProfitCenterGroupWithStatus(profitCenterGroupUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                                .flatMap(count -> {

                                                    if (profitCenterEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                Flux<SlaveProfitCenterEntity> slaveProfitCenterEntityFlux = slaveProfitCenterRepository
                                        .showMappedProfitCenterRecords(profitCenterGroupUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveProfitCenterEntityFlux
                                        .collectList()
                                        .flatMap(profitCenterEntity -> slaveProfitCenterRepository.countMappedProfitCenterAgainstProfitCenterGroup(profitCenterGroupUUID, searchKeyWord, searchKeyWord)
                                                .flatMap(count -> {

                                                    if (profitCenterEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", profitCenterEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }
                        }))
                ).switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-group-profit-center_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("profitCenterGroupUUID").trim());
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
                .flatMap(value -> profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                                .flatMap(profitCenterGroupEntity -> {

//                            // if given profit center group is inactive
//                            if (!profitCenterGroupEntity.getStatus()) {
//                                return responseInfoMsg("Profit Center Group status is inactive");
//                            }

                                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                                    // mapping with all records
                                    if (all) {

                                        ProfitCenterGroupProfitCenterPvtEntity profitCenterGroupProfitCenterPvtEntity = ProfitCenterGroupProfitCenterPvtEntity
                                                .builder()
                                                .uuid(UUID.randomUUID())
                                                .profitCenterGroupUUID(profitCenterGroupUUID)
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

                                        return profitCenterGroupProfitCenterPvtRepository.findFirstByProfitCenterGroupUUIDAndAllAndDeletedAtIsNull(profitCenterGroupUUID, true)
                                                .flatMap(recordAlreadyExists -> responseInfoMsg("Record Already Exists"))
                                                .switchIfEmpty(Mono.defer(() -> profitCenterGroupProfitCenterPvtRepository.findAllByProfitCenterGroupUUIDAndDeletedAtIsNull(profitCenterGroupUUID)
                                                        .collectList()
                                                        .flatMap(previouslyMappedProfitCenters -> {

                                                            for (ProfitCenterGroupProfitCenterPvtEntity pvtEntity : previouslyMappedProfitCenters) {
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

                                                            return profitCenterGroupProfitCenterPvtRepository.saveAll(previouslyMappedProfitCenters)
                                                                    .then(profitCenterGroupProfitCenterPvtRepository.save(profitCenterGroupProfitCenterPvtEntity))
                                                                    .flatMap(allProfitCentersMapped -> responseSuccessMsg("All Profit Centers Are Mapped Successfully With Given Profit Center Group", all))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                        })
                                                ));
                                    }


                                    // if all is not selected
                                    else {

                                        //getting List of Profit Centers From Front
                                        List<String> listOfProfitCenterUUID = value.get("profitCenterUUID");

                                        listOfProfitCenterUUID.removeIf(s -> s.equals(""));

                                        List<UUID> l_list = new ArrayList<>();
                                        for (String profitCenterUUID : listOfProfitCenterUUID) {
                                            l_list.add(UUID.fromString(profitCenterUUID));
                                        }

                                        if (!l_list.isEmpty()) {
                                            return profitCenterRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList()
                                                    .flatMap(existingProfitCenters -> {

                                                        // Profit Center UUID List
                                                        List<UUID> profitCenterList = new ArrayList<>();

                                                        for (ProfitCenterEntity profitCenter : existingProfitCenters) {
                                                            profitCenterList.add(profitCenter.getUuid());
                                                        }

                                                        if (!profitCenterList.isEmpty()) {

                                                            // profit center uuid list to show in response
                                                            List<UUID> profitCenterRecords = new ArrayList<>(profitCenterList);

                                                            List<ProfitCenterGroupProfitCenterPvtEntity> listPvt = new ArrayList<>();

                                                            return profitCenterGroupProfitCenterPvtRepository.findAllByProfitCenterGroupUUIDAndProfitCenterUUIDInAndDeletedAtIsNull(profitCenterGroupUUID, profitCenterList)
                                                                    .collectList()
                                                                    .flatMap(profitCenterGroupPvtEntity -> {
                                                                        for (ProfitCenterGroupProfitCenterPvtEntity pvtEntity : profitCenterGroupPvtEntity) {
                                                                            //Removing Existing Profit Center UUID in Profit Center Final List to be saved that does not contain already mapped values
                                                                            profitCenterList.remove(pvtEntity.getProfitCenterUUID());
                                                                        }

                                                                        // iterate Profit Center UUIDs for given Profit Center Group
                                                                        for (UUID profitCenterUUID : profitCenterList) {
                                                                            ProfitCenterGroupProfitCenterPvtEntity profitCenterGroupProfitCenterPvtEntity = ProfitCenterGroupProfitCenterPvtEntity
                                                                                    .builder()
                                                                                    .profitCenterUUID(profitCenterUUID)
                                                                                    .uuid(UUID.randomUUID())
                                                                                    .profitCenterGroupUUID(profitCenterGroupUUID)
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
                                                                            listPvt.add(profitCenterGroupProfitCenterPvtEntity);
                                                                        }

                                                                        return profitCenterGroupProfitCenterPvtRepository.saveAll(listPvt)
                                                                                .collectList()
                                                                                .flatMap(groupList -> {

                                                                                    if (!profitCenterList.isEmpty()) {
                                                                                        return responseSuccessMsg("Record Stored Successfully", profitCenterRecords)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                    } else {
                                                                                        return responseInfoMsg("Record Already Exists", profitCenterRecords);
                                                                                    }

                                                                                }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("Profit Center Record does not exist");
                                                        }
                                                    }).switchIfEmpty(responseInfoMsg("The Entered Profit Center Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("The Entered Profit Center Does not exist.Please Contact Developer."));
                                        } else {
                                            return responseInfoMsg("Select Profit Center First");
                                        }

                                    }
                                }).switchIfEmpty(responseInfoMsg("Profit Center Group does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Profit Center Group does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_profit-center-group-profit-center_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID profitCenterGroupUUID = UUID.fromString(serverRequest.pathVariable("profitCenterGroupUUID").trim());
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
                        return profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(profitCenterGroupUUID)
                                .flatMap(profitCenterGroupEntity -> profitCenterGroupProfitCenterPvtRepository.findFirstByProfitCenterGroupUUIDAndAllAndDeletedAtIsNull(profitCenterGroupUUID, true)
                                        .flatMap(profitCenterGroupProfitCenterPvtEntity -> {

                                            SlaveProfitCenterGroupProfitCenterDto slaveProfitCenterGroupProfitCenterDto = SlaveProfitCenterGroupProfitCenterDto.builder()
                                                    .id(profitCenterGroupProfitCenterPvtEntity.getId())
                                                    .version(profitCenterGroupProfitCenterPvtEntity.getVersion())
                                                    .uuid(profitCenterGroupProfitCenterPvtEntity.getUuid())
                                                    .name("all")
                                                    .profitCenterGroupName(profitCenterGroupEntity.getName())
                                                    .profitCenterGroupUUID(profitCenterGroupProfitCenterPvtEntity.getProfitCenterGroupUUID())
                                                    .all(profitCenterGroupProfitCenterPvtEntity.getAll())
                                                    .createdAt(profitCenterGroupProfitCenterPvtEntity.getCreatedAt())
                                                    .createdBy(profitCenterGroupProfitCenterPvtEntity.getCreatedBy())
                                                    .updatedBy(profitCenterGroupProfitCenterPvtEntity.getUpdatedBy())
                                                    .updatedAt(profitCenterGroupProfitCenterPvtEntity.getUpdatedAt())
                                                    .reqCompanyUUID(profitCenterGroupProfitCenterPvtEntity.getReqCompanyUUID())
                                                    .reqBranchUUID(profitCenterGroupProfitCenterPvtEntity.getReqBranchUUID())
                                                    .reqCreatedIP(profitCenterGroupProfitCenterPvtEntity.getReqCreatedIP())
                                                    .reqCreatedPort(profitCenterGroupProfitCenterPvtEntity.getReqCreatedPort())
                                                    .reqCreatedBrowser(profitCenterGroupProfitCenterPvtEntity.getReqCreatedBrowser())
                                                    .reqCreatedOS(profitCenterGroupProfitCenterPvtEntity.getReqCreatedOS())
                                                    .reqCreatedDevice(profitCenterGroupProfitCenterPvtEntity.getReqCreatedDevice())
                                                    .reqCreatedReferer(profitCenterGroupProfitCenterPvtEntity.getReqCreatedReferer())
                                                    .reqUpdatedIP(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedIP())
                                                    .reqUpdatedPort(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedPort())
                                                    .reqUpdatedBrowser(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedBrowser())
                                                    .reqUpdatedOS(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedOS())
                                                    .reqUpdatedDevice(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedDevice())
                                                    .reqUpdatedReferer(profitCenterGroupProfitCenterPvtEntity.getReqUpdatedReferer())
                                                    .build();

                                            profitCenterGroupProfitCenterPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            profitCenterGroupProfitCenterPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            profitCenterGroupProfitCenterPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            profitCenterGroupProfitCenterPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedIP(reqIp);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedPort(reqPort);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedOS(reqOs);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedDevice(reqDevice);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedReferer(reqReferer);

                                            return profitCenterGroupProfitCenterPvtRepository.save(profitCenterGroupProfitCenterPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", slaveProfitCenterGroupProfitCenterDto))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                    // if all is not given
                    else {
                        return profitCenterRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("profitCenterUUID").trim()))
                                .flatMap(profitCenterEntity -> profitCenterGroupProfitCenterPvtRepository.findFirstByProfitCenterGroupUUIDAndProfitCenterUUIDAndDeletedAtIsNull(profitCenterGroupUUID, profitCenterEntity.getUuid())
                                        .flatMap(profitCenterGroupProfitCenterPvtEntity -> {

                                            profitCenterGroupProfitCenterPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            profitCenterGroupProfitCenterPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            profitCenterGroupProfitCenterPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            profitCenterGroupProfitCenterPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedIP(reqIp);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedPort(reqPort);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedOS(reqOs);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedDevice(reqDevice);
                                            profitCenterGroupProfitCenterPvtEntity.setReqDeletedReferer(reqReferer);

                                            return profitCenterGroupProfitCenterPvtRepository.save(profitCenterGroupProfitCenterPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", profitCenterEntity))
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
