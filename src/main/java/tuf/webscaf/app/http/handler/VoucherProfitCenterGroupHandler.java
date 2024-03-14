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
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherProfitCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherProfitCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.ProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "voucherProfitCenterGroupHandler")
public class VoucherProfitCenterGroupHandler {
    @Autowired
    VoucherProfitCenterGroupPvtRepository voucherProfitCenterGroupPvtRepository;

    @Autowired
    SlaveVoucherProfitCenterGroupPvtRepository slaveVoucherProfitCenterGroupPvtRepository;

    @Autowired
    ProfitCenterGroupRepository profitCenterGroupRepository;

    @Autowired
    SlaveProfitCenterGroupRepository slaveProfitCenterGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-profit-center-group_un-mapped_show")
    public Mono<ServerResponse> showUnMappedProfitCenterGroupAgainstVoucher(ServerRequest serverRequest) {
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
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showUnMappedProfitCenterGroupListWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository
                            .countUnMappedProfitCenterGroupAgainstVoucherWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseErrorMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showUnMappedProfitCenterGroupList(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository
                            .countUnMappedProfitCenterGroupAgainstVoucher(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseErrorMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-profit-center-group_mapped_show")
    public Mono<ServerResponse> showMappedProfitCenterGroupAgainstVoucher(ServerRequest serverRequest) {
        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("");

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
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showMappedProfitCenterGroupsWithStatus(voucherUUID, Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroupsWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveProfitCenterGroupEntity> slaveProfitCenterGroupEntityFlux = slaveProfitCenterGroupRepository
                    .showMappedProfitCenterGroups(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveProfitCenterGroupEntityFlux
                    .collectList()
                    .flatMap(profitCenterGroupEntity -> slaveProfitCenterGroupRepository.countMappedProfitCenterGroups(voucherUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (profitCenterGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", profitCenterGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-profit-center-group_store")
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

                                    //getting List of Profit Center Groups From Front
                                    List<String> listOfProfitCenterGroupUUID = value.get("profitCenterGroupUUID");

                                    listOfProfitCenterGroupUUID.removeIf(s -> s.equals(""));

                                    List<UUID> l_list = new ArrayList<>();
                                    for (String profitCenterGroupUUID : listOfProfitCenterGroupUUID) {
                                        l_list.add(UUID.fromString(profitCenterGroupUUID));
                                    }

                                    if (!l_list.isEmpty()) {
                                        return profitCenterGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                .collectList()
                                                .flatMap(existingProfitCenterGroups -> {
                                                    // Profit Center Group UUID List
                                                    List<UUID> profitCenterGroupList = new ArrayList<>();

                                                    for (ProfitCenterGroupEntity profitCenterGroup : existingProfitCenterGroups) {
                                                        profitCenterGroupList.add(profitCenterGroup.getUuid());
                                                    }

                                                    if (!profitCenterGroupList.isEmpty()) {

                                                        // profit center group uuid list to show in response
                                                        List<UUID> profitCenterGroupRecords = new ArrayList<>(profitCenterGroupList);

                                                        List<VoucherProfitCenterGroupPvtEntity> listPvt = new ArrayList<>();

                                                        return voucherProfitCenterGroupPvtRepository.findAllByVoucherUUIDAndProfitCenterGroupUUIDInAndDeletedAtIsNull(voucherUUID, profitCenterGroupList)
                                                                .collectList()
                                                                .flatMap(voucherPvtEntity -> {
                                                                    for (VoucherProfitCenterGroupPvtEntity pvtEntity : voucherPvtEntity) {
                                                                        //Removing Existing Profit Center Group UUID in Profit Center Group Final List to be saved that does not contain already mapped values
                                                                        profitCenterGroupList.remove(pvtEntity.getProfitCenterGroupUUID());
                                                                    }

                                                                    // iterate Profit Center Group UUIDs for given Voucher
                                                                    for (UUID profitCenterGroupUUID : profitCenterGroupList) {
                                                                        VoucherProfitCenterGroupPvtEntity voucherProfitCenterGroupPvtEntity = VoucherProfitCenterGroupPvtEntity
                                                                                .builder()
                                                                                .profitCenterGroupUUID(profitCenterGroupUUID)
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
                                                                        listPvt.add(voucherProfitCenterGroupPvtEntity);
                                                                    }

                                                                    return voucherProfitCenterGroupPvtRepository.saveAll(listPvt)
                                                                            .collectList()
                                                                            .flatMap(groupList -> {

                                                                                if (!profitCenterGroupList.isEmpty()) {
                                                                                    return responseSuccessMsg("Record Stored Successfully", profitCenterGroupRecords)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                } else {
                                                                                    return responseInfoMsg("Record Already Exists", profitCenterGroupRecords);
                                                                                }

                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                });
                                                    } else {
                                                        return responseInfoMsg("Profit Center Group Record does not exist");
                                                    }
                                                }).switchIfEmpty(responseInfoMsg("The Entered Profit Center Group Does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("The Entered Profit Center Group Does not exist.Please Contact Developer."));
                                    } else {
                                        return responseInfoMsg("Select Profit Center Group First");
                                    }
                                }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-profit-center-group_delete")
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
                .flatMap(value -> profitCenterGroupRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("profitCenterGroupUUID").trim()))
                                .flatMap(profitCenterGroupEntity -> voucherProfitCenterGroupPvtRepository
                                        .findFirstByVoucherUUIDAndProfitCenterGroupUUIDAndDeletedAtIsNull(voucherUUID, profitCenterGroupEntity.getUuid())
                                        .flatMap(voucherProfitCenterGroupPvtEntity -> {

                                            voucherProfitCenterGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            voucherProfitCenterGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            voucherProfitCenterGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            voucherProfitCenterGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedIP(reqIp);
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedPort(reqPort);
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedOS(reqOs);
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                            voucherProfitCenterGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                            return voucherProfitCenterGroupPvtRepository.save(voucherProfitCenterGroupPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", profitCenterGroupEntity))
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
