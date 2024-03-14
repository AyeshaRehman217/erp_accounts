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
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherAccountGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountGroupRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherAccountGroupPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveVoucherAccountGroupPvtRepository;
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
@Tag(name = "voucherAccountGroupHandler")
public class VoucherAccountGroupHandler {
    @Autowired
    VoucherAccountGroupPvtRepository voucherAccountGroupPvtRepository;

    @Autowired
    SlaveVoucherAccountGroupPvtRepository slaveVoucherAccountGroupPvtRepository;

    @Autowired
    AccountGroupRepository accountGroupRepository;

    @Autowired
    SlaveAccountGroupRepository slaveAccountGroupRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_voucher-account-group_un-mapped_show")
    public Mono<ServerResponse> showUnMappedAccountGroupsAgainstVoucher(ServerRequest serverRequest) {
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
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .showUnMappedAccountGroupListWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository
                            .countUnMappedAccountGroupRecordsWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .showUnMappedAccountGroupList(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository
                            .countUnMappedAccountGroupRecords(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }
    }

    //Show Mapped Account Groups for given Voucher Id
    @AuthHasPermission(value = "account_api_v1_voucher-account-group_mapped_show")
    public Mono<ServerResponse> showMappedAccountGroupsAgainstVoucher(ServerRequest serverRequest) {
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
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .showMappedAccountGroupsWithStatus(voucherUUID, Boolean.valueOf(status), searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository.countMappedAccountGroupListWithStatus(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        } else {
            Flux<SlaveAccountGroupEntity> slaveAccountGroupEntityFlux = slaveAccountGroupRepository
                    .showMappedAccountGroups(voucherUUID, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveAccountGroupEntityFlux
                    .collectList()
                    .flatMap(accountGroupEntity -> slaveAccountGroupRepository.countMappedAccountGroupList(voucherUUID, searchKeyWord)
                            .flatMap(count -> {
                                if (accountGroupEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", accountGroupEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_voucher-account-group_store")
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

                            //getting List of Account Groups From Front
                            List<String> listOfAccountGroupUUID = value.get("accountGroupUUID");

                            listOfAccountGroupUUID.removeIf(s -> s.equals(""));

                            List<UUID> l_list = new ArrayList<>();
                            for (String accountGroupUUID : listOfAccountGroupUUID) {
                                l_list.add(UUID.fromString(accountGroupUUID));
                            }

                            if (!l_list.isEmpty()) {
                                return accountGroupRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                        .collectList()
                                        .flatMap(existingAccountGroups -> {
                                            // Account Group UUID List
                                            List<UUID> accountGroupList = new ArrayList<>();

                                            for (AccountGroupEntity accountGroup : existingAccountGroups) {
                                                accountGroupList.add(accountGroup.getUuid());
                                            }

                                            if (!accountGroupList.isEmpty()) {

                                                // account group uuid list to show in response
                                                List<UUID> accountGroupRecords = new ArrayList<>(accountGroupList);

                                                List<VoucherAccountGroupPvtEntity> listPvt = new ArrayList<>();

                                                return voucherAccountGroupPvtRepository.findAllByVoucherUUIDAndAccountGroupUUIDInAndDeletedAtIsNull(voucherUUID, accountGroupList)
                                                        .collectList()
                                                        .flatMap(voucherPvtEntity -> {
                                                            for (VoucherAccountGroupPvtEntity pvtEntity : voucherPvtEntity) {
                                                                //Removing Existing Account Group UUID in Account Group Final List to be saved that does not contain already mapped values
                                                                accountGroupList.remove(pvtEntity.getAccountGroupUUID());
                                                            }

                                                            // iterate Account Group UUIDs for given Voucher
                                                            for (UUID accountGroupUUID : accountGroupList) {
                                                                VoucherAccountGroupPvtEntity voucherAccountGroupPvtEntity = VoucherAccountGroupPvtEntity
                                                                        .builder()
                                                                        .accountGroupUUID(accountGroupUUID)
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
                                                                listPvt.add(voucherAccountGroupPvtEntity);
                                                            }

                                                            return voucherAccountGroupPvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(groupList -> {

                                                                        if (!accountGroupList.isEmpty()) {
                                                                            return responseSuccessMsg("Record Stored Successfully", accountGroupRecords)
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                        } else {
                                                                            return responseInfoMsg("Record Already Exists", accountGroupRecords);
                                                                        }

                                                                    }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                        });
                                            } else {
                                                return responseInfoMsg("Account Group Record does not exist");
                                            }
                                        }).switchIfEmpty(responseInfoMsg("The Entered Account Group Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("The Entered Account Group Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Account Group First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Voucher does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Voucher does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_voucher-account-group_delete")
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
                .flatMap(value -> accountGroupRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("accountGroupUUID").trim()))
                                .flatMap(accountGroupEntity -> voucherAccountGroupPvtRepository
                                        .findFirstByVoucherUUIDAndAccountGroupUUIDAndDeletedAtIsNull(voucherUUID, accountGroupEntity.getUuid())
                                        .flatMap(voucherAccountGroupPvtEntity -> {

                                            voucherAccountGroupPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            voucherAccountGroupPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            voucherAccountGroupPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            voucherAccountGroupPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            voucherAccountGroupPvtEntity.setReqDeletedIP(reqIp);
                                            voucherAccountGroupPvtEntity.setReqDeletedPort(reqPort);
                                            voucherAccountGroupPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            voucherAccountGroupPvtEntity.setReqDeletedOS(reqOs);
                                            voucherAccountGroupPvtEntity.setReqDeletedDevice(reqDevice);
                                            voucherAccountGroupPvtEntity.setReqDeletedReferer(reqReferer);

                                            return voucherAccountGroupPvtRepository.save(voucherAccountGroupPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", accountGroupEntity))
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
