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
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupAccountPvtEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.SubAccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.SubAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAccountGroupAccountDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveSubAccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveSubAccountGroupRepository;
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
@Tag(name = "subAccountGroupAccountHandler")
public class SubAccountGroupAccountHandler {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    @Autowired
    SubAccountGroupAccountPvtRepository subAccountGroupAccountPvtRepository;

    @Autowired
    SlaveSubAccountGroupAccountPvtRepository slaveSubAccountGroupAccountPvtRepository;

    @Autowired
    SubAccountGroupRepository subAccountGroupRepository;

    @Autowired
    SlaveSubAccountGroupRepository slaveSubAccountGroupRepository;

    @Autowired
    CustomResponse appResponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_sub-account-group-accounts_un-mapped_show")
    public Mono<ServerResponse> showUnMappedAccountListAgainstSubAccountGroup(ServerRequest serverRequest) {

        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("subAccountGroupUUID").trim());

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

        // if status is present in query parameter
        if (!status.isEmpty()) {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showUnMappedAccountAgainstSubAccountGroup(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntityDB -> slaveAccountRepository.countUnMappedRecordsAgainstSubAccountGroupWithStatus(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (accountEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showUnMappedAccountAgainstSubAccountGroup(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntityDB -> slaveAccountRepository.countUnMappedRecordsAgainstSubAccountGroup(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (accountEntityDB.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count);

                                        } else {

                                            return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntityDB, count);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    // show Accounts for Sub Account Group Id
    @AuthHasPermission(value = "account_api_v1_sub-account-group-accounts_mapped_show")
    public Mono<ServerResponse> showMappedAccountListAgainstSubAccountGroup(ServerRequest serverRequest) {
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("subAccountGroupUUID").trim());
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        return slaveSubAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                // if sub account group is mapped with all
                .flatMap(subAccountGroupEntity -> slaveSubAccountGroupAccountPvtRepository.findFirstBySubAccountGroupUUIDAndAllAndDeletedAtIsNull(subAccountGroupUUID, true)
                        .flatMap(allAccountsMapped -> {

                            SlaveSubAccountGroupAccountDto slaveSubAccountGroupAccountDto = SlaveSubAccountGroupAccountDto.builder()
                                    .id(allAccountsMapped.getId())
                                    .version(allAccountsMapped.getVersion())
                                    .uuid(allAccountsMapped.getUuid())
                                    .name("all")
                                    .subAccountGroupName(subAccountGroupEntity.getName())
                                    .subAccountGroupUUID(allAccountsMapped.getSubAccountGroupUUID())
                                    .all(allAccountsMapped.getAll())
                                    .createdAt(allAccountsMapped.getCreatedAt())
                                    .createdBy(allAccountsMapped.getCreatedBy())
                                    .updatedBy(allAccountsMapped.getUpdatedBy())
                                    .updatedAt(allAccountsMapped.getUpdatedAt())
                                    .reqCompanyUUID(allAccountsMapped.getReqCompanyUUID())
                                    .reqBranchUUID(allAccountsMapped.getReqBranchUUID())
                                    .reqCreatedIP(allAccountsMapped.getReqCreatedIP())
                                    .reqCreatedPort(allAccountsMapped.getReqCreatedPort())
                                    .reqCreatedBrowser(allAccountsMapped.getReqCreatedBrowser())
                                    .reqCreatedOS(allAccountsMapped.getReqCreatedOS())
                                    .reqCreatedDevice(allAccountsMapped.getReqCreatedDevice())
                                    .reqCreatedReferer(allAccountsMapped.getReqCreatedReferer())
                                    .reqUpdatedIP(allAccountsMapped.getReqUpdatedIP())
                                    .reqUpdatedPort(allAccountsMapped.getReqUpdatedPort())
                                    .reqUpdatedBrowser(allAccountsMapped.getReqUpdatedBrowser())
                                    .reqUpdatedOS(allAccountsMapped.getReqUpdatedOS())
                                    .reqUpdatedDevice(allAccountsMapped.getReqUpdatedDevice())
                                    .reqUpdatedReferer(allAccountsMapped.getReqUpdatedReferer())
                                    .build();

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                return slaveAccountRepository
                                        .countByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveSubAccountGroupAccountDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }

                            // if status is not given
                            else {
                                return slaveAccountRepository
                                        .countByNameContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                        .flatMap(count -> responseIndexSuccessMsg("All Records Fetched Successfully", slaveSubAccountGroupAccountDto, count))
                                        .switchIfEmpty(responseErrorMsg("Record does not exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                            }
                        })

                        // get only mapped records from pvt
                        .switchIfEmpty(Mono.defer(() -> {

                            // if status is given in query parameter
                            if (!status.isEmpty()) {
                                Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                                        .showMappedAccountAgainstSubAccountGroupWithStatus(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
                                return slaveAccountEntityFlux
                                        .collectList()
                                        .flatMap(accountEntity -> slaveAccountRepository.countAccountAgainstSubAccountGroupsWithStatusFilter(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                                .flatMap(count -> {
                                                    if (accountEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);
                                                    } else {
                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                            }

                            // if status is not given
                            else {

                                Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                                        .showMappedAccountAgainstSubAccountGroup(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                                return slaveAccountEntityFlux
                                        .collectList()
                                        .flatMap(accountEntity -> slaveAccountRepository.countAccountAgainstSubAccountGroups(subAccountGroupUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                                                .flatMap(count -> {
                                                    if (accountEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count);

                                                    } else {

                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                    }
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please Contact Developer"));

                            }
                        }))
                ).switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_sub-account-group-accounts_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("subAccountGroupUUID"));
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                                .flatMap(subAccountGroupEntity -> {

//                                    // if given sub account group is inactive
//                                    if (!subAccountGroupEntity.getStatus()) {
//                                        return responseInfoMsg("Sub Account Group status is inactive");
//                                    }

                                    Boolean all = Boolean.valueOf(value.getFirst("all"));

                                    // mapping with all records
                                    if (all) {

                                        SubAccountGroupAccountPvtEntity subAccountGroupAccountPvtEntity = SubAccountGroupAccountPvtEntity.builder()
                                                .uuid(UUID.randomUUID())
                                                .subAccountGroupUUID(subAccountGroupUUID)
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

                                        return subAccountGroupAccountPvtRepository.findFirstBySubAccountGroupUUIDAndAllAndDeletedAtIsNull(subAccountGroupUUID, true)
                                                .flatMap(recordAlreadyExists -> responseInfoMsg("Record Already Exists"))
                                                .switchIfEmpty(Mono.defer(() -> subAccountGroupAccountPvtRepository.findAllBySubAccountGroupUUIDAndDeletedAtIsNull(subAccountGroupUUID)
                                                        .collectList()
                                                        .flatMap(previouslyMappedAccounts -> {

                                                            for (SubAccountGroupAccountPvtEntity pvtEntity : previouslyMappedAccounts) {
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

                                                            return subAccountGroupAccountPvtRepository.saveAll(previouslyMappedAccounts)
                                                                    .then(subAccountGroupAccountPvtRepository.save(subAccountGroupAccountPvtEntity))
                                                                    .flatMap(allAccountsMapped -> responseSuccessMsg("All Accounts Are Mapped Successfully With Given Sub Account Group", all))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                        })
                                                ));
                                    }


                                    // if all is not selected
                                    else {

                                        //getting List of Accounts From Front
                                        List<String> listOfAccountUUID = value.get("accountUUID");

                                        listOfAccountUUID.removeIf(s -> s.equals(""));

                                        List<UUID> l_list = new ArrayList<>();
                                        for (String getAccountUUID : listOfAccountUUID) {
                                            l_list.add(UUID.fromString(getAccountUUID));
                                        }

                                        if (!l_list.isEmpty()) {
                                            return accountRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList()
                                                    .flatMap(existingAccounts -> {
                                                        // Account UUID List
                                                        List<UUID> accountList = new ArrayList<>();

                                                        for (AccountEntity account : existingAccounts) {
                                                            accountList.add(account.getUuid());
                                                        }

                                                        if (!accountList.isEmpty()) {

                                                            // account uuid list to show in response
                                                            List<UUID> accountRecords = new ArrayList<>(accountList);

                                                            List<SubAccountGroupAccountPvtEntity> listPvt = new ArrayList<>();

                                                            return subAccountGroupAccountPvtRepository.findAllBySubAccountGroupUUIDAndAccountUUIDInAndDeletedAtIsNull(subAccountGroupUUID, accountList)
                                                                    .collectList()
                                                                    .flatMap(subAccountGroupPvtEntity -> {
                                                                        for (SubAccountGroupAccountPvtEntity pvtEntity : subAccountGroupPvtEntity) {
                                                                            //Removing Existing Account UUID in Account Final List to be saved that does not contain already mapped values
                                                                            accountList.remove(pvtEntity.getAccountUUID());
                                                                        }

                                                                        // iterate Account UUIDs for given Account Group
                                                                        for (UUID accountUUID : accountList) {
                                                                            SubAccountGroupAccountPvtEntity subAccountGroupAccountPvtEntity = SubAccountGroupAccountPvtEntity
                                                                                    .builder()
                                                                                    .accountUUID(accountUUID)
                                                                                    .uuid(UUID.randomUUID())
                                                                                    .subAccountGroupUUID(subAccountGroupUUID)
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
                                                                            listPvt.add(subAccountGroupAccountPvtEntity);
                                                                        }

                                                                        return subAccountGroupAccountPvtRepository.saveAll(listPvt)
                                                                                .collectList()
                                                                                .flatMap(groupList -> {

                                                                                    if (!accountList.isEmpty()) {
                                                                                        return responseSuccessMsg("Record Stored Successfully", accountRecords)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                                                    } else {
                                                                                        return responseInfoMsg("Record Already Exists", accountRecords);
                                                                                    }

                                                                                }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("Account Record does not exist");
                                                        }
                                                    }).switchIfEmpty(responseInfoMsg("The Entered Account Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("The Entered Account Does not exist.Please Contact Developer."));
                                        } else {
                                            return responseInfoMsg("Select Account First");
                                        }
                                    }
                                }).switchIfEmpty(responseInfoMsg("Sub Account Group Record does not exist"))
                                .onErrorResume(err -> responseInfoMsg("Sub Account Group Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    @AuthHasPermission(value = "account_api_v1_sub-account-group-accounts_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID subAccountGroupUUID = UUID.fromString(serverRequest.pathVariable("subAccountGroupUUID").trim());
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
                        return subAccountGroupRepository.findByUuidAndDeletedAtIsNull(subAccountGroupUUID)
                                .flatMap(subAccountGroupEntity -> subAccountGroupAccountPvtRepository.findFirstBySubAccountGroupUUIDAndAllAndDeletedAtIsNull(subAccountGroupUUID, true)
                                        .flatMap(accountGroupAccountPvtEntity -> {

                                            SlaveSubAccountGroupAccountDto slaveSubAccountGroupAccountDto = SlaveSubAccountGroupAccountDto.builder()
                                                    .id(accountGroupAccountPvtEntity.getId())
                                                    .version(accountGroupAccountPvtEntity.getVersion())
                                                    .uuid(accountGroupAccountPvtEntity.getUuid())
                                                    .name("all")
                                                    .subAccountGroupName(subAccountGroupEntity.getName())
                                                    .subAccountGroupUUID(accountGroupAccountPvtEntity.getSubAccountGroupUUID())
                                                    .all(accountGroupAccountPvtEntity.getAll())
                                                    .createdAt(accountGroupAccountPvtEntity.getCreatedAt())
                                                    .createdBy(accountGroupAccountPvtEntity.getCreatedBy())
                                                    .updatedBy(accountGroupAccountPvtEntity.getUpdatedBy())
                                                    .updatedAt(accountGroupAccountPvtEntity.getUpdatedAt())
                                                    .reqCompanyUUID(accountGroupAccountPvtEntity.getReqCompanyUUID())
                                                    .reqBranchUUID(accountGroupAccountPvtEntity.getReqBranchUUID())
                                                    .reqCreatedIP(accountGroupAccountPvtEntity.getReqCreatedIP())
                                                    .reqCreatedPort(accountGroupAccountPvtEntity.getReqCreatedPort())
                                                    .reqCreatedBrowser(accountGroupAccountPvtEntity.getReqCreatedBrowser())
                                                    .reqCreatedOS(accountGroupAccountPvtEntity.getReqCreatedOS())
                                                    .reqCreatedDevice(accountGroupAccountPvtEntity.getReqCreatedDevice())
                                                    .reqCreatedReferer(accountGroupAccountPvtEntity.getReqCreatedReferer())
                                                    .reqUpdatedIP(accountGroupAccountPvtEntity.getReqUpdatedIP())
                                                    .reqUpdatedPort(accountGroupAccountPvtEntity.getReqUpdatedPort())
                                                    .reqUpdatedBrowser(accountGroupAccountPvtEntity.getReqUpdatedBrowser())
                                                    .reqUpdatedOS(accountGroupAccountPvtEntity.getReqUpdatedOS())
                                                    .reqUpdatedDevice(accountGroupAccountPvtEntity.getReqUpdatedDevice())
                                                    .reqUpdatedReferer(accountGroupAccountPvtEntity.getReqUpdatedReferer())
                                                    .build();

                                            accountGroupAccountPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            accountGroupAccountPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            accountGroupAccountPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            accountGroupAccountPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            accountGroupAccountPvtEntity.setReqDeletedIP(reqIp);
                                            accountGroupAccountPvtEntity.setReqDeletedPort(reqPort);
                                            accountGroupAccountPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            accountGroupAccountPvtEntity.setReqDeletedOS(reqOs);
                                            accountGroupAccountPvtEntity.setReqDeletedDevice(reqDevice);
                                            accountGroupAccountPvtEntity.setReqDeletedReferer(reqReferer);

                                            return subAccountGroupAccountPvtRepository.save(accountGroupAccountPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", slaveSubAccountGroupAccountDto))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again"))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }

                    // if all is not given
                    else {
                        return accountRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("accountUUID").trim()))
                                .flatMap(accountEntity -> subAccountGroupAccountPvtRepository.findFirstBySubAccountGroupUUIDAndAccountUUIDAndDeletedAtIsNull(subAccountGroupUUID, accountEntity.getUuid())
                                        .flatMap(subAccountGroupAccountPvtEntity -> {

                                            subAccountGroupAccountPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            subAccountGroupAccountPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            subAccountGroupAccountPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            subAccountGroupAccountPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            subAccountGroupAccountPvtEntity.setReqDeletedIP(reqIp);
                                            subAccountGroupAccountPvtEntity.setReqDeletedPort(reqPort);
                                            subAccountGroupAccountPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            subAccountGroupAccountPvtEntity.setReqDeletedOS(reqOs);
                                            subAccountGroupAccountPvtEntity.setReqDeletedDevice(reqDevice);
                                            subAccountGroupAccountPvtEntity.setReqDeletedReferer(reqReferer);

                                            return subAccountGroupAccountPvtRepository.save(subAccountGroupAccountPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", accountEntity))
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

        return appResponse.set(
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

        return appResponse.set(
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

        return appResponse.set(
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

        return appResponse.set(
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


        return appResponse.set(
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

        return appResponse.set(
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


        return appResponse.set(
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


}
