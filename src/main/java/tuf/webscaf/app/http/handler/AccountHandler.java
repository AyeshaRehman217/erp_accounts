package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.FinancialAccountDto;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionEntity;
import tuf.webscaf.app.dbContext.master.repository.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAccountListDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.*;
import tuf.webscaf.app.service.AccountControlCodeService;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helper.StringVerifyHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Tag(name = "accountHandler")
@Slf4j
public class AccountHandler {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountTypesRepository accountTypesRepository;

    @Autowired
    AccountGroupRepository accountGroupRepository;

    @Autowired
    BalanceAndIncomeLineRepository balanceAndIncomeLineRepository;

    @Autowired
    AccountGroupAccountPvtRepository accountGroupAccountPvtRepository;

    @Autowired
    SubAccountGroupAccountPvtRepository subAccountGroupAccountPvtRepository;

    @Autowired
    SlaveVoucherAccountGroupPvtRepository slaveVoucherAccountGroupPvtRepository;

    @Autowired
    SlaveVoucherSubAccountGroupPvtRepository slaveVoucherSubAccountGroupPvtRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    @Autowired
    SubAccountGroupRepository subAccountGroupRepository;

    @Autowired
    SlaveAccountGroupRepository slaveAccountGroupRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SlaveCalendarPeriodsRepository slaveCalendarPeriodRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Autowired
    AccountControlCodeService accountControlCodeService;

    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialUri;

    @Value("${server.erp_emp_financial_module.uri}")
    private String empFinancialModuleUri;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialModuleUri;

    @AuthHasPermission(value = "account_api_v1_accounts_mapped-sub-account-groups-voucher_index")
    public Mono<ServerResponse> indexAccountsAgainstVoucherAndSubAccountGroup(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Voucher UUID
        String voucherUUID = serverRequest.queryParam("voucherUUID").map(String::toString).orElse("").trim();

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

        return slaveVoucherSubAccountGroupPvtRepository.subAccountGroupAllMappingExists(UUID.fromString(voucherUUID))
                .flatMap(all -> {

                    // if voucher is not mapped with sub account group of all accounts
                    if (all) {

                        // if status is given in query parameter
                        if (!status.isEmpty()) {

                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAccountWithStatusAndEntryAllowed(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            }))
                                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAccountWithEntryAllowed(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countByNameContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            }))
                                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    }

                    // if voucher is not mapped with sub account group of all accounts
                    else {

                        if (!status.isEmpty()) {
                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAllAccountsAgainstVoucherAndSubAccountGroupWithStatus(UUID.fromString(voucherUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countAccountAgainstSubAccountGroupsAndVoucherWithStatusFilter(UUID.fromString(voucherUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            }))
                                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        } else {
                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAllAccountsAgainstVoucherAndSubAccountGroupWithoutStatus(UUID.fromString(voucherUUID), searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countAccountAgainstSubAccountGroupsAndVoucherWithoutStatusFilter(UUID.fromString(voucherUUID), searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

//    public Mono<ServerResponse> fetchAllChildAccountAgainstParentAccount(ServerRequest serverRequest) {
//
//        final UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
//                .flatMap(accountEntity -> getChildAccounts(accountUUID)
//                        .flatMap(accountRecord -> responseSuccessMsg("Record Fetched Successfully", accountRecord))
//                        .switchIfEmpty(responseInfoMsg("Record does not exist."))
//                        .onErrorResume(ex -> responseErrorMsg("Record does not Exist.Please Contact Developer."))
//                ).switchIfEmpty(responseInfoMsg("Record does not exist."))
//                .onErrorResume(ex -> responseErrorMsg("Record does not Exist.Please Contact Developer."));
//
//    }

    public Mono<SlaveSubAccountListDto> getChildAccounts(UUID accountUUID) {

        return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                .flatMap(parentAccount -> slaveAccountRepository.findAllByParentAccountUUIDAndDeletedAtIsNull(accountUUID, Sort.by("createdAt"))
                        .flatMap(childAccounts -> getChildAccounts(childAccounts.getUuid()))
                        .collectList()
                        .flatMap(childAccountsList -> {

                            SlaveSubAccountListDto subAccountDtoList = SlaveSubAccountListDto
                                    .builder()
                                    .id(parentAccount.getId())
                                    .version(parentAccount.getVersion())
                                    .uuid(parentAccount.getUuid())
                                    .name(parentAccount.getName())
                                    .code(parentAccount.getCode())
                                    .description(parentAccount.getDescription())
                                    .isEntryAllowed(parentAccount.getIsEntryAllowed())
                                    .accountTypeUUID(parentAccount.getAccountTypeUUID())
                                    .parentAccountUUID(parentAccount.getParentAccountUUID())
                                    .controlCode(parentAccount.getControlCode())
                                    .companyUUID(parentAccount.getCompanyUUID())
                                    .childAccounts(childAccountsList)
                                    .status(parentAccount.getStatus())
                                    .createdAt(parentAccount.getCreatedAt())
                                    .createdBy(parentAccount.getCreatedBy())
                                    .updatedAt(parentAccount.getUpdatedAt())
                                    .updatedBy(parentAccount.getUpdatedBy())
                                    .build();

                            return Mono.just(subAccountDtoList);
                        }));
    }


    //fetch all accounts based on company id
    public Mono<ServerResponse> indexAccountsWithCompanyWithStatusFilter(UUID companyUUID, Boolean status, String searchKeyWord, Pageable pageable) {

        Flux<SlaveAccountEntity> accountEntityWithCompanyFlux = slaveAccountRepository
                .findAllByNameContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNull(pageable, searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID);

        //check if Company id exists in config
        return apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", companyUUID)
                .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                        .flatMap(company -> accountEntityWithCompanyFlux
                                .collectList()
                                .flatMap(accountWithCompanyFlux -> slaveAccountRepository.countByNameContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNull(searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID, searchKeyWord, status, companyUUID)
                                        .flatMap(count -> {
                                            if (accountWithCompanyFlux.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);
                                            } else {
                                                return responseIndexSuccessMsg("All Records Fetched Successfully", accountWithCompanyFlux, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."))
                        ).switchIfEmpty(responseInfoMsg("Company Does not exist."))
                        .onErrorResume(ex -> responseErrorMsg("Company Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }


    //fetch All Accounts in Index Function with Company UUID check
    public Mono<ServerResponse> indexAccountsWithCompanyWithOutStatusFilter(UUID companyUUID, String searchKeyWord, Pageable pageable) {

        Flux<SlaveAccountEntity> accountEntityWithBranchFlux = slaveAccountRepository
                .findAllByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNull(pageable, searchKeyWord, companyUUID, searchKeyWord, companyUUID, searchKeyWord, companyUUID, searchKeyWord, companyUUID);


        return apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", companyUUID)
                .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                        .flatMap(company -> accountEntityWithBranchFlux
                                .collectList()
                                .flatMap(accountWithBranchFlux -> slaveAccountRepository.countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNull(searchKeyWord, companyUUID, searchKeyWord, companyUUID, searchKeyWord, companyUUID, searchKeyWord, companyUUID)
                                        .flatMap(count -> {
                                            if (accountWithBranchFlux.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count);
                                            } else {
                                                return responseIndexSuccessMsg("All Records Fetched Successfully", accountWithBranchFlux, count);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."))
                        ).switchIfEmpty(responseInfoMsg("Company Does not exist."))
                        .onErrorResume(ex -> responseErrorMsg("Company Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_accounts_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company uuid
        String companyUUID = serverRequest.queryParam("companyUUID").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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

        //if Company uuid and Status is present in request
        if (!companyUUID.isEmpty() && !status.isEmpty()) {
            return indexAccountsWithCompanyWithStatusFilter(UUID.fromString(companyUUID), Boolean.valueOf(status), searchKeyWord, pageable);
        }

        //if Company UUID is present in request
        else if (!companyUUID.isEmpty()) {
            return indexAccountsWithCompanyWithOutStatusFilter(UUID.fromString(companyUUID), searchKeyWord, pageable);
        }

        //if Status is present
        else if (!status.isEmpty()) {
            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return accountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            }))
                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
        //if no query parameter is present in request
        else {
            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord);

            return accountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                }
                            }))
                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    public Mono<ServerResponse> indexWithChildAccounts(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company UUID
        String companyUUID = serverRequest.queryParam("companyUUID").map(String::toString).orElse("").trim();

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


        if (!companyUUID.isEmpty()) {
            Flux<SlaveAccountDto> accountDtoFlux = slaveAccountRepository
                    .showAllAccountsWithCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return accountDtoFlux
                    .collectList()
                    .flatMap(accountsDtoList -> {

                        //Map where parent Account UUID is not Null
                        MultiValueMap<UUID, SlaveAccountDto> innerChildMap = new LinkedMultiValueMap<>();

                        //creating List of Main Parent Accounts
                        List<SlaveAccountDto> mainParent = new ArrayList<>();


                        //creating List of Accounts Where Parent Account UUID is null
                        List<SlaveAccountDto> mainParentWhereNull = new ArrayList<>();

                        //creating List of parent account uuid where not null
                        List<UUID> accountUUID = new ArrayList<>();

                        //Map where parent Account UUID is not Null
                        MultiValueMap<UUID, SlaveAccountDto> paginatingMap = new LinkedMultiValueMap<>();

                        //iterating over the loop from query
                        for (SlaveAccountDto accountsList : accountsDtoList) {

                            // Add all records to map with uuid as key
                            paginatingMap.add(accountsList.getUuid(), accountsList);

                            //check where parent Account UUID is null then add in the Main Parent Account List
                            if (accountsList.getParentAccountUUID() == null) {
                                mainParentWhereNull.add(accountsList);

                            } else {
                                // add records to map with parent account uuid as key
                                innerChildMap.add(accountsList.getParentAccountUUID(), accountsList);

                                if (!accountUUID.contains(accountsList.getParentAccountUUID())) {
                                    accountUUID.add(accountsList.getParentAccountUUID());
                                }
                            }
                        }

                        // iterating over parent uuids
                        for (UUID parentUUID : accountUUID) {
                            // check if all records with pagination map does not contain parent account uuid
                            if (!paginatingMap.containsKey(parentUUID)) {
                                mainParent.addAll(innerChildMap.get(parentUUID));
                            }
                        }

                        // add all root parents to Main Parent List
                        mainParent.addAll(mainParentWhereNull);

                        //iterating over the Main Parent List
                        for (SlaveAccountDto parentWhereNull : mainParent) {
                            // Calling Recursive Function to check
                            parentWhereNull.setChildAccounts(setChildAccounts(innerChildMap, parentWhereNull));
                        }

                        return slaveAccountRepository
                                .countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNull(searchKeyWord, UUID.fromString(companyUUID), searchKeyWord, UUID.fromString(companyUUID), searchKeyWord, UUID.fromString(companyUUID), searchKeyWord, UUID.fromString(companyUUID))
                                .flatMap(count ->
                                {
                                    if (mainParent.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);
                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", mainParent, count);
                                    }
                                });
                    })
                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

        //if no query parameter is present in request
        else {
            Flux<SlaveAccountDto> accountDtoFlux = slaveAccountRepository
                    .showAllAccounts(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return accountDtoFlux
                    .collectList()
                    .flatMap(accountsDtoList -> {

                        //Map where parent Account UUID is not Null
                        MultiValueMap<UUID, SlaveAccountDto> innerChildMap = new LinkedMultiValueMap<>();

                        //creating List of Main Parent Accounts
                        List<SlaveAccountDto> mainParent = new ArrayList<>();


                        //creating List of Accounts Where Parent Account UUID is null
                        List<SlaveAccountDto> mainParentWhereNull = new ArrayList<>();

                        //creating List of parent account uuid where not null
                        List<UUID> accountUUID = new ArrayList<>();

                        //Map where parent Account UUID is not Null
                        MultiValueMap<UUID, SlaveAccountDto> paginatingMap = new LinkedMultiValueMap<>();

                        //iterating over the loop from query
                        for (SlaveAccountDto accountsList : accountsDtoList) {

                            // Add all records to map with uuid as key
                            paginatingMap.add(accountsList.getUuid(), accountsList);

                            //check where parent Account UUID is null then add in the Main Parent Account List
                            if (accountsList.getParentAccountUUID() == null) {
                                mainParentWhereNull.add(accountsList);

                            } else {
                                // add records to map with parent account uuid as key
                                innerChildMap.add(accountsList.getParentAccountUUID(), accountsList);

                                if (!accountUUID.contains(accountsList.getParentAccountUUID())) {
                                    accountUUID.add(accountsList.getParentAccountUUID());
                                }
                            }
                        }

                        // iterating over parent uuids
                        for (UUID parentUUID : accountUUID) {
                            // check if all records with pagination map does not contain parent account uuid
                            if (!paginatingMap.containsKey(parentUUID)) {
                                mainParent.addAll(innerChildMap.get(parentUUID));
                            }
                        }

                        // add all root parents to Main Parent List
                        mainParent.addAll(mainParentWhereNull);

                        //iterating over the Main Parent List
                        for (SlaveAccountDto parentWhereNull : mainParent) {
                            // Calling Recursive Function to check
                            parentWhereNull.setChildAccounts(setChildAccounts(innerChildMap, parentWhereNull));
                        }

                        return slaveAccountRepository
                                .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                                .flatMap(count ->
                                {
                                    if (mainParent.isEmpty()) {
                                        return responseIndexInfoMsg("Record does not exist", count);
                                    } else {
                                        return responseIndexSuccessMsg("All Records Fetched Successfully", mainParent, count);
                                    }
                                });
                    }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }


    //Setting Inner Child Accounts
    public List<SlaveAccountDto> setChildAccounts(MultiValueMap<UUID, SlaveAccountDto> childMap, SlaveAccountDto accountsDto) {

        //Creating Accounts List for returning records
        List<SlaveAccountDto> accountList = new ArrayList<>();

        //check where map key is not null
        if (childMap.get(accountsDto.getUuid()) != null) {
            //iterating over the map based on UUID key
            for (SlaveAccountDto chart : childMap.get(accountsDto.getUuid())) {
                //Setting Inner Child Accounts
                chart.setChildAccounts(setChildAccounts(childMap, chart));
                //Adding in Returning List
                accountList.add(chart);
            }
        }
        return accountList;
    }


    public Mono<ServerResponse> indexWithEntryAllowed(ServerRequest serverRequest) {

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


        Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                .findAllByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE);

        return accountEntityFlux
                .collectList()
                .flatMap(accountEntity -> slaveAccountRepository
                        .countByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE)
                        .flatMap(count ->
                        {
                            if (accountEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                            }
                        }))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }


    @AuthHasPermission(value = "account_api_v1_accounts_account-type_index")
    public Mono<ServerResponse> indexWithAccountType(ServerRequest serverRequest) {

        UUID accountTypeUUID = UUID.fromString(serverRequest.queryParam("accountTypeUUID").map(String::toString).orElse(""));

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


        Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                .findAllByNameContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE);

        return accountEntityFlux
                .collectList()
                .flatMap(accountEntity -> slaveAccountRepository
                        .countByNameContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNull(searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE, searchKeyWord, accountTypeUUID, Boolean.TRUE)
                        .flatMap(count ->
                        {
                            if (accountEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                            }
                        }))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


//    public ArrayList<SlaveAccountDto> accountMapping(ArrayList<SlaveAccountDto> list) {
//
//        ArrayList<SlaveAccountDto> newList = new ArrayList<>();
//        ArrayList<Long> accountId = new ArrayList<>();
//        for (int i = 0; i < list.size(); i++) {
//            if (!accountId.contains(list.get(i).getId())) {
//                newList.add(list.get(i));
//                accountId.add(list.get(i).getId());
//            } else {
//                for (int j = 0; j < newList.size(); j++) {
//
//                    if (Objects.equals(newList.get(j).getId(), list.get(i).getId())) {
//                        newList.set(j, list.get(i));
//                    }
//
//                }
//            }
//        }
//        return newList;
//    }

    @AuthHasPermission(value = "account_api_v1_accounts_voucher_show")
    //show Accounts for Voucher With Search Filter on Name Field
    public Mono<ServerResponse> showMappedAccountsAgainstVoucher(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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
        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();


        return slaveVoucherAccountGroupPvtRepository.accountGroupAllMappingExists(voucherUUID)
                .flatMap(all -> {

                    // if voucher is mapped with all account group
                    if (all) {
                        // if status is given in query parameter
                        if (!status.isEmpty()) {

                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAccountWithStatusAndEntryAllowed(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE, searchKeyWord, Boolean.TRUE, Boolean.TRUE)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            }))
                                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveAccountEntity> accountEntityFlux = slaveAccountRepository
                                    .indexAccountWithEntryAllowed(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                            return accountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countByNameContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNull(searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE, searchKeyWord, Boolean.TRUE)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            }))
                                    .switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    }

                    // if voucher is not mapped with all account group
                    else {
                        // if status is given in query parameter
                        if (!status.isEmpty()) {
                            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                                    .showAccountListWithStatusFilter(voucherUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveAccountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countAccountWithStatusFilterAgainstVoucher(voucherUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if status is not given
                        else {
                            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                                    .showAccountList(voucherUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                            return slaveAccountEntityFlux
                                    .collectList()
                                    .flatMap(accountEntity -> slaveAccountRepository
                                            .countAccountWithAgainstVoucher(voucherUUID, searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                                            .flatMap(count ->
                                            {
                                                if (accountEntity.isEmpty()) {
                                                    return responseInfoMsg("Record does not exist");
                                                } else {
                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                                }
                                            })).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    }
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_accounts_voucher_name_show")
    //show Accounts for Voucher With Search Filter on Name Field
    public Mono<ServerResponse> showVoucherWithAccounts(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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
        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithAccountNameAndStatusFilter(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countAccountWithWithNameAndStatusFilterAgainstVoucher(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        } else {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithAccountNameFilter(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countAccountWithWithNameFilterAgainstVoucher(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_accounts_voucher_code_show")
    //show Accounts for Voucher By Adding Filter on Code and Status Field
    public Mono<ServerResponse> showAccountsWithAccountCodeFilter(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithAccountCodeAndStatusFilter(voucherUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countWithAccountCodeAndStatusFilter(voucherUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        } else {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithAccountCodeFilter(voucherUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity -> slaveAccountRepository
                            .countWithAccountCodeFilter(voucherUUID, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (accountEntity.isEmpty()) {
                                    return responseInfoMsg("Record does not exist");
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_accounts_voucher_company_show")
    //show Accounts for a Voucher with given Company
    public Mono<ServerResponse> showAccountsWithCompanyAndVoucher(ServerRequest serverRequest) {

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());
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

        String company = serverRequest.queryParam("companyUUID").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        if (!status.isEmpty()) {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithCompanyAndStatusFilter(voucherUUID, Boolean.valueOf(status), UUID.fromString(company), searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity ->
                            slaveAccountRepository
                                    .countAccountsAgainstCompanyIdBasedWithStatusFilter(voucherUUID, Boolean.valueOf(status), UUID.fromString(company), searchKeyWord, searchKeyWord, searchKeyWord)
                                    .flatMap(count ->
                                    {
                                        if (accountEntity.isEmpty()) {
                                            return responseInfoMsg("Record does not exist");
                                        } else {
                                            return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }

        // if status is not present
        else {
            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
                    .showAccountListWithCompany(voucherUUID, UUID.fromString(company), searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAccountEntityFlux
                    .collectList()
                    .flatMap(accountEntity ->
                            slaveAccountRepository
                                    .countWithCompany(voucherUUID, UUID.fromString(company), searchKeyWord, searchKeyWord, searchKeyWord)
                                    .flatMap(count ->
                                    {
                                        if (accountEntity.isEmpty()) {
                                            return responseInfoMsg("Record does not exist");
                                        } else {
                                            return responseIndexSuccessMsg("All Records Fetched Successfully", accountEntity, count);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read the request"))
                    .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_accounts_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveAccountRepository.showWithUuid(accountUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_accounts_control-code_show")
    public Mono<ServerResponse> showByControlCode(ServerRequest serverRequest) {
        String controlCode = serverRequest.pathVariable("controlCode");

        return slaveAccountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(controlCode)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));

    }

    @AuthHasPermission(value = "account_api_v1_accounts_company_show")
    //Check Company uuid In Config Module
    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveAccountRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_accounts_existing_list_show")
    //This Function Is used By Student Financial Module to Check if Account UUIDs exists
    public Mono<ServerResponse> showExistingAccountsListInStudentFinancial(ServerRequest serverRequest) {

        // List of accounts uuids from query params
        List<String> uuids = serverRequest.queryParams().get("uuid");

        //This is Account List to paas as parameter in the query
        List<UUID> accountList = new ArrayList<>();
        if (uuids != null) {
            for (String account : uuids) {
                accountList.add(UUID.fromString(account));
            }
        }

        // Used to Show Existing Account UUIDs in Response
        List<UUID> finalList = new ArrayList<>();

        return slaveAccountRepository.findAllByUuidInAndDeletedAtIsNull(accountList)
                .collectList()
                .flatMap(accountEntities -> {
                    for (SlaveAccountEntity entity : accountEntities) {
                        finalList.add(entity.getUuid());
                    }
                    return responseSuccessMsg("Records Fetched Successfully", finalList)
                            .switchIfEmpty(responseInfoMsg("Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
                });
    }

    //If All Search Code & Parent Account Exists
    public Mono<ServerResponse> checkParentAccountOnly(AccountEntity accountEntity) {

        return accountRepository.findByUuidAndDeletedAtIsNull(accountEntity.getParentAccountUUID())
                .flatMap(parentAccount -> {

                    // if given parent account is inactive
                    if (!parentAccount.getStatus()) {
                        return responseInfoMsg("Parent Account status is inactive");
                    }

                    //check if parent Account Type and Child Account Type are same
                    if (!parentAccount.getAccountTypeUUID().equals(accountEntity.getAccountTypeUUID())) {
                        return responseInfoMsg("The Given Parent Account Type Does not Matches with Current Account Type");
                    }

                    //set parent account
                    accountEntity.setParentAccountUUID(accountEntity.getParentAccountUUID());

                    return accountControlCodeService.settingParentControlCode(accountEntity)
                            .flatMap(accountMapping -> accountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(accountMapping.getControlCode())
                                    .flatMap(controlCodeAlreadyExists -> responseInfoMsg("Control Code Already Exists"))
                                    .switchIfEmpty(accountRepository.save(accountMapping)
                                            .flatMap(accountRecord -> responseSuccessMsg("Record Stored Successfully", accountRecord)))
                            ).switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something Wrong Please try again"))
                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Parent Account Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Parent Account Does not exist.Please Contact Developer."));
    }


    // used to set opening balance
    public Mono<AccountEntity> setOpeningBalance(AccountEntity accountEntity, BigDecimal openingBalance, String description, Boolean debit) {

        // Transaction Entry for opening balance
        TransactionEntity transactionEntity = TransactionEntity.builder()
                .uuid(UUID.randomUUID())
                .transactionDate(accountEntity.getCreatedAt())
                .description("This is opening balance of " + accountEntity.getName())
                .module("account")
                .companyUUID(accountEntity.getCompanyUUID())
                .createdAt(accountEntity.getCreatedAt())
                .createdBy(accountEntity.getCreatedBy())
                .reqCompanyUUID(accountEntity.getReqCompanyUUID())
                .reqBranchUUID(accountEntity.getReqBranchUUID())
                .reqCreatedIP(accountEntity.getReqCreatedIP())
                .reqCreatedPort(accountEntity.getReqCreatedPort())
                .reqCreatedBrowser(accountEntity.getReqCreatedBrowser())
                .reqCreatedOS(accountEntity.getReqCreatedOS())
                .reqCreatedDevice(accountEntity.getReqCreatedDevice())
                .reqCreatedReferer(accountEntity.getReqCreatedReferer())
                .build();

        // Ledger Row for opening balance
        LedgerEntryEntity ledgerEntryEntity = LedgerEntryEntity.builder()
                .uuid(UUID.randomUUID())
                .description(description)
                .accountUUID(accountEntity.getUuid())
                .transactionUUID(transactionEntity.getUuid())
                .createdAt(accountEntity.getCreatedAt())
                .createdBy(accountEntity.getCreatedBy())
                .reqCompanyUUID(accountEntity.getReqCompanyUUID())
                .reqBranchUUID(accountEntity.getReqBranchUUID())
                .reqCreatedIP(accountEntity.getReqCreatedIP())
                .reqCreatedPort(accountEntity.getReqCreatedPort())
                .reqCreatedBrowser(accountEntity.getReqCreatedBrowser())
                .reqCreatedOS(accountEntity.getReqCreatedOS())
                .reqCreatedDevice(accountEntity.getReqCreatedDevice())
                .reqCreatedReferer(accountEntity.getReqCreatedReferer())
                .build();

        // if amount has debit effect
        if (debit) {
            ledgerEntryEntity.setDrAmount(openingBalance);
            ledgerEntryEntity.setCrAmount(BigDecimal.ZERO);
        }

        // if amount has credit effect
        else {
            ledgerEntryEntity.setDrAmount(BigDecimal.ZERO);
            ledgerEntryEntity.setCrAmount(openingBalance);
        }


        return slaveCalendarPeriodRepository.showRecordWithTransactionDate(transactionEntity.getTransactionDate())
                .flatMap(calendarPeriodEntity -> {

                    // set the calendar period uuid
                    transactionEntity.setCalendarPeriodUUID(calendarPeriodEntity.getUuid());
                    accountEntity.setOpeningBalanceUUID(transactionEntity.getUuid());

                    return transactionRepository.save(transactionEntity)
                            .then(ledgerEntryRepository.save(ledgerEntryEntity)
                                    .flatMap(ledgerRow -> Mono.just(accountEntity))
                            );
                });

    }

    @AuthHasPermission(value = "account_api_v1_accounts_store")
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

                    UUID parentAccountUUID = null;

                    if ((value.containsKey("parentAccountUUID") && (!Objects.equals(value.getFirst("parentAccountUUID").trim(), "")))) {
                        parentAccountUUID = UUID.fromString(value.getFirst("parentAccountUUID").trim());
                    }

                    BigDecimal openingBalance = BigDecimal.ZERO;

                    if (!StringVerifyHelper.isNullEmptyOrBlank(value.getFirst("openingBalance"))) {
                        openingBalance = new BigDecimal(value.getFirst("openingBalance"));
                    }

                    // description for opening balance transaction entry
                    String description = value.getFirst("openingBalanceDescription");

                    // debit or credit opening balance amount
                    Boolean debit = Boolean.valueOf(value.getFirst("debit"));

                    AccountEntity accountEntity = AccountEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .parentAccountUUID(parentAccountUUID)
                            .accountTypeUUID(UUID.fromString(value.getFirst("accountTypeUUID").trim()))
                            .companyUUID(UUID.fromString(reqCompanyUUID))
                            .code(value.getFirst("code").trim())
                            .isEntryAllowed(Boolean.valueOf(value.getFirst("isEntryAllowed")))
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .isOpeningBalance(Boolean.valueOf(value.getFirst("isOpeningBalance")))
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

                    BigDecimal finalOpeningBalance = openingBalance;
                    return accountRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(accountEntity.getName())
                            .flatMap(nameMsg -> responseInfoMsg("Name Already Exists."))
                            .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findByUuidAndDeletedAtIsNull(accountEntity.getAccountTypeUUID())
                                    .flatMap(accountType -> {

                                        // if given account type is inactive
                                        if (!accountType.getStatus()) {
                                            return responseInfoMsg("Account Type status is inactive");
                                        }

                                        // Check if parent Account Exists
                                        else if (accountEntity.getParentAccountUUID() != null) {

                                            // if isOpeningBalance is true
                                            if (accountEntity.getIsOpeningBalance()) {

                                                // if opening balance amount is given
                                                if (finalOpeningBalance.compareTo(BigDecimal.ZERO) > 0) {
                                                    return setOpeningBalance(accountEntity, finalOpeningBalance, description, debit)
                                                            .flatMap(this::checkParentAccountOnly)
                                                            .switchIfEmpty(responseInfoMsg("Unable to set the opening balance."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to set the opening balance.Please Contact Developer."));
                                                }

                                                // if opening balance is zero
                                                else {
                                                    return responseInfoMsg("Opening Balance Amount is Required");
                                                }
                                            }


                                            // if isOpeningBalance is false
                                            else {

                                                // if opening balance amount is given, don't allow storing
                                                if (finalOpeningBalance.compareTo(BigDecimal.ZERO) > 0) {
                                                    return responseInfoMsg("Opening Balance Amount is not allowed when Opening Balance status is inactive");
                                                }


                                                // else store the record
                                                else {
                                                    return checkParentAccountOnly(accountEntity);
                                                }
                                            }

                                        }

                                        // else store the record
                                        else {

                                            // if isOpeningBalance is true
                                            if (accountEntity.getIsOpeningBalance()) {

                                                // if opening balance amount is given
                                                if (finalOpeningBalance.compareTo(BigDecimal.ZERO) > 0) {
                                                    accountEntity.setControlCode(accountEntity.getCode());
                                                    accountEntity.setLevel(0L);

                                                    return accountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(accountEntity.getControlCode())
                                                            .flatMap(controlCodeAlreadyExists -> responseInfoMsg("Control Code Already Exists"))
                                                            .switchIfEmpty(Mono.defer(() -> setOpeningBalance(accountEntity, finalOpeningBalance, description, debit)
                                                                    .flatMap(accountRecord -> accountRepository.save(accountRecord)
                                                                            .flatMap(accountMapping -> responseSuccessMsg("Record Stored Successfully", accountEntity))
                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store record.There is something Wrong Please try again!"))
                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store record.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Unable to set the opening balance."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to set the opening balance.Please Contact Developer."))
                                                            ));
                                                }

                                                // if opening balance is zero
                                                else {
                                                    return responseInfoMsg("Opening Balance Amount is Required");
                                                }
                                            }

                                            // if isOpeningBalance is false
                                            else {

                                                // if opening balance amount is given, don't allow storing
                                                if (finalOpeningBalance.compareTo(BigDecimal.ZERO) > 0) {
                                                    return responseInfoMsg("Opening Balance Amount is not allowed when Opening Balance status is inactive");
                                                }


                                                // else store the record
                                                else {
                                                    accountEntity.setControlCode(accountEntity.getCode());
                                                    accountEntity.setLevel(0L);

                                                    return accountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(accountEntity.getControlCode())
                                                            .flatMap(controlCodeAlreadyExists -> responseInfoMsg("Control Code Already Exists"))
                                                            .switchIfEmpty(Mono.defer(() -> accountRepository.save(accountEntity)
                                                                    .flatMap(accountMapping -> responseSuccessMsg("Record Stored Successfully", accountEntity))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store record.There is something Wrong Please try again!"))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store record.Please Contact Developer."))
                                                            ));
                                                }
                                            }

                                        }
                                    }).switchIfEmpty(responseInfoMsg("Requested Account Type does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Account Type does not exist.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    //If Parent Account Exists
    public Mono<ServerResponse> updateParentAccountOnly(AccountEntity accountEntity, AccountEntity previousAccountEntity) {

        //check parent Account
        return accountRepository.findByUuidAndDeletedAtIsNull(accountEntity.getParentAccountUUID())
                .flatMap(parentAccountEntity -> accountControlCodeService.gettingChildUUIDList(previousAccountEntity.getUuid())
                        .flatMap(childList -> {

                            // if given parent account is inactive
                            if (!parentAccountEntity.getStatus()) {
                                return responseInfoMsg("Parent Account status is inactive");
                            }

                            // if child of given account is given as parent account
                            else if (childList.contains(accountEntity.getParentAccountUUID())) {
                                return responseInfoMsg("The Given Parent Account is Already the child of given parent");
                            }

                            // if parent account type is different from given account
                            else if (!parentAccountEntity.getAccountTypeUUID().equals(accountEntity.getAccountTypeUUID())) {
                                return responseInfoMsg("The Given Parent Account Type Does not Matches with Current Account Type");
                            }

                            // else update the record
                            else {

                                return accountControlCodeService.settingParentControlCode(accountEntity)
                                        .flatMap(updatedEntity -> accountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getControlCode(), updatedEntity.getUuid())
                                                .flatMap(controlCodeAlreadyExists -> responseInfoMsg("Control Code Already Exists"))
                                                .switchIfEmpty(accountRepository.save(previousAccountEntity)
                                                        .then(accountRepository.save(updatedEntity))
                                                        .flatMap(accountRecord -> responseSuccessMsg("Record Updated successfully", accountRecord))
                                                )
                                        ).switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something Wrong Please try again"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer"));

                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Requested Parent Account Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested Parent Account Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_accounts_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
                .flatMap(value -> accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                        .flatMap(previousAccountEntity -> {

                            UUID parentAccountUUID = null;

                            if ((value.containsKey("parentAccountUUID") && (!Objects.equals(value.getFirst("parentAccountUUID").trim(), "")))) {
                                parentAccountUUID = UUID.fromString(value.getFirst("parentAccountUUID").trim());
                            }

                            AccountEntity accountEntity = AccountEntity.builder()
                                    .uuid(previousAccountEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .parentAccountUUID(parentAccountUUID)
                                    .accountTypeUUID(UUID.fromString(value.getFirst("accountTypeUUID").trim()))
                                    .companyUUID(UUID.fromString(reqCompanyUUID))
                                    .code(value.getFirst("code").trim())
                                    .isEntryAllowed(Boolean.valueOf(value.getFirst("isEntryAllowed")))
                                    .isOpeningBalance(Boolean.valueOf(value.getFirst("isOpeningBalance")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousAccountEntity.getCreatedAt())
                                    .createdBy(previousAccountEntity.getCreatedBy())
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedBy(UUID.fromString(userId))
                                    .reqCreatedIP(previousAccountEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousAccountEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousAccountEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousAccountEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousAccountEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousAccountEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousAccountEntity.setDeletedBy(UUID.fromString(userId));
                            previousAccountEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousAccountEntity.setReqDeletedIP(reqIp);
                            previousAccountEntity.setReqDeletedPort(reqPort);
                            previousAccountEntity.setReqDeletedBrowser(reqBrowser);
                            previousAccountEntity.setReqDeletedOS(reqOs);
                            previousAccountEntity.setReqDeletedDevice(reqDevice);
                            previousAccountEntity.setReqDeletedReferer(reqReferer);

                            return accountRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(accountEntity.getName(), accountEntity.getUuid())
                                    .flatMap(nameMsg -> responseInfoMsg("Name Already Exists."))
                                    .switchIfEmpty(Mono.defer(() -> accountTypesRepository.findByUuidAndDeletedAtIsNull(accountEntity.getAccountTypeUUID())
                                            .flatMap(accountTypeEntity -> {

                                                // if given account type is inactive
                                                if (!accountTypeEntity.getStatus()) {
                                                    return responseInfoMsg("Account Type status is inactive");
                                                }


                                                // Check if parent Account Exists
                                                if (accountEntity.getParentAccountUUID() != null) {

                                                    if (accountEntity.getUuid().equals(accountEntity.getParentAccountUUID())) {
                                                        return responseInfoMsg("Account can't be Parent Account of itself");
                                                    }

                                                    // update the record
                                                    else {
                                                        return updateParentAccountOnly(accountEntity, previousAccountEntity);

                                                    }

                                                }

                                                // else update the record
                                                else {
                                                    accountEntity.setControlCode(accountEntity.getCode());
                                                    accountEntity.setLevel(0L);

                                                    return accountRepository.findFirstByControlCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(accountEntity.getControlCode(), accountEntity.getUuid())
                                                            .flatMap(controlCodeAlreadyExists -> responseInfoMsg("Control Code Already Exists"))
                                                            .switchIfEmpty(Mono.defer(() -> accountRepository.save(previousAccountEntity)
                                                                    .then(accountRepository.save(accountEntity))
                                                                    .flatMap(accountMapping -> responseSuccessMsg("Record Updated successfully", accountEntity))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something Wrong Please try again"))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                                            ));

                                                }
                                            }).switchIfEmpty(responseInfoMsg("Account Type Does Not Exist"))
                                            .onErrorResume(ex -> responseErrorMsg("Account Type Does not Exist.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Account Does Not Exist"))
                        .onErrorResume(ex -> responseErrorMsg("Account Does Not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

//    @AuthHasPermission(value = "account_api_v1_accounts_financial-account_control-code_show")
//    public Mono<ServerResponse> setControlCodeInStudentFinancial(ServerRequest serverRequest) {
//        final UUID financialAccountUUID = UUID.fromString(serverRequest.pathVariable("financialAccountUUID"));
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//        return apiCallService.getDataWithUUID(studentFinancialUri + "api/v1/financial-accounts/show/", financialAccountUUID)
//                .flatMap(financialAccountJsonNode -> apiCallService.getUUID(financialAccountJsonNode)
//                        .flatMap(financialAccount -> {
//                            FinancialAccountDto financialAccountDto = financialAccountDtoMapper(financialAccountJsonNode);
//
//                            return slaveAccountRepository.showAllParentsAgainstChild(financialAccountDto.getParentAccountUUID())
//                                    .collectList()
//                                    .flatMap(parent -> {
//                                        //Getting All the Parent of the Given Child
//
//                                        String childCode = financialAccountDto.getCode();
//                                        //Get Count of All the Levels from child to parent
//                                        Long level = Long.valueOf(parent.size());
//
//                                        //Looping through all the child to Given Parent Node
//                                        for (SlaveChildParentAccountDto account : parent) {
//                                            //Adding Hyphen between two codes
//                                            childCode = account.getCode() + "-" + childCode;
//
//                                        }
//
//                                        //Setting Control Code
//                                        financialAccountDto.setControlCode(childCode);
//
//                                        //Setting Level
//                                        financialAccountDto.setLevel(level);
//
//                                        return responseSuccessMsg("Record Fetched Successfully", financialAccountDto)
//                                                .switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
//                                    }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                    .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
//                        }).switchIfEmpty(responseInfoMsg("Financial Account does not exist"))
//                        .onErrorResume(err -> responseErrorMsg("Financial Account does not exist. Please contact developer."))
//                );
//    }

//    @AuthHasPermission(value = "account_api_v1_accounts_financial-account_parent-control-code_show")
//    public Mono<ServerResponse> getControlCodeInStudentFinancial(ServerRequest serverRequest) {
//        final UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseWarningMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseWarningMsg("Unknown user");
//
//        }
//
//        //Getting All the Parent of the Given Account
//        return slaveAccountRepository.showAllParentsAgainstChild(accountUUID)
//                .collectList()
//                .flatMap(parent -> {
//
//                    String childCode = "";
//                    //Get Count of All the Levels from child to parent
//                    Long level = Long.valueOf(parent.size());
//
//                    //Looping through all the child to Given Parent Node
//                    for (SlaveChildParentAccountDto account : parent) {
//                        //if the Parent Account is at index 0 do not add hyphen between the codes
//                        if (parent.indexOf(account) == 0) {
//                            childCode = account.getCode();
//                        } else {
//                            //Adding Hyphen between two codes
//                            childCode = account.getCode() + "-" + childCode;
//                        }
//                    }
//
//                    //Setting Control Code And Level
//                    FinancialControlCodeDto financialControlCodeDto = FinancialControlCodeDto
//                            .builder()
//                            .controlCode(childCode)
//                            .level(level)
//                            .build();
//
//                    return responseSuccessMsg("Record Fetched Successfully", financialControlCodeDto)
//                            .switchIfEmpty(responseInfoMsg("Record does not exist"))
//                            .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
//
//    }

    public FinancialAccountDto financialAccountDtoMapper(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<FinancialAccountDto>() {
        });
        FinancialAccountDto financialAccountDto = null;
        if (!jsonNode.get("data").isEmpty()) {
            try {
                financialAccountDto = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return financialAccountDto;
    }

    @AuthHasPermission(value = "account_api_v1_accounts_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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

        //Finding Id if exists in the same table
        return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                //check In Account Group Account  Pvt If Account Exists
                .flatMap(accountEntity -> accountGroupAccountPvtRepository.findFirstByAccountUUIDAndDeletedAtIsNull(accountUUID)
                        .flatMap(checkInPvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        //check if Account Reference Exists in Sub Account Group Account Pvt
                        .switchIfEmpty(Mono.defer(()-> subAccountGroupAccountPvtRepository.findFirstByAccountUUIDAndDeletedAtIsNull(accountUUID)
                                .flatMap(checkInPvtMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        //check In Accounts If Account Exists As Parent Account
                        .switchIfEmpty(Mono.defer(() -> accountRepository.findFirstByParentAccountUUIDAndDeletedAtIsNull(accountEntity.getUuid())
                                .flatMap(checkAccAsParentAccount -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        //check In Ledger Entry If Account Exists
                        .switchIfEmpty(Mono.defer(() -> ledgerEntryRepository.checkAccountReferenceInLedger(accountEntity.getUuid())
                                .flatMap(checkAccInLedger -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))))
                        //Checks if Account Reference exists in Financial Accounts in Emp Financial Module
                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(empFinancialModuleUri + "api/v1/financial-accounts/account/show/", accountEntity.getUuid())
                                .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                        .flatMap(checkAccountApiMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists")))))
                        //check if Account reference exists in financial accounts in student financial module
                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-accounts/account/show/", accountEntity.getUuid())
                                .flatMap(jsonNode -> apiCallService.checkResponse(jsonNode)
                                        .flatMap(checkBranchUUIDApiMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists")))))
                        .switchIfEmpty(Mono.defer(() -> {

                            // if account has opening balance
                            if (accountEntity.getOpeningBalanceUUID() != null) {

                                accountEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                accountEntity.setDeletedBy(UUID.fromString(userId));
                                accountEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                accountEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                accountEntity.setReqDeletedIP(reqIp);
                                accountEntity.setReqDeletedPort(reqPort);
                                accountEntity.setReqDeletedBrowser(reqBrowser);
                                accountEntity.setReqDeletedOS(reqOs);
                                accountEntity.setReqDeletedDevice(reqDevice);
                                accountEntity.setReqDeletedReferer(reqReferer);

                                return transactionRepository.findByUuidAndDeletedAtIsNull(accountEntity.getOpeningBalanceUUID())
                                        .flatMap(openingBalanceTransaction -> ledgerEntryRepository.findFirstByTransactionUUIDAndDeletedAtIsNull(openingBalanceTransaction.getUuid())
                                                .flatMap(openingBalanceLedgerRow -> {
                                                    // delete previous opening balance transaction
                                                    openingBalanceTransaction.setDeletedAt(accountEntity.getUpdatedAt());
                                                    openingBalanceTransaction.setDeletedBy(accountEntity.getUpdatedBy());
                                                    openingBalanceTransaction.setReqDeletedIP(accountEntity.getReqUpdatedIP());
                                                    openingBalanceTransaction.setReqDeletedPort(accountEntity.getReqUpdatedPort());
                                                    openingBalanceTransaction.setReqDeletedBrowser(accountEntity.getReqUpdatedBrowser());
                                                    openingBalanceTransaction.setReqDeletedOS(accountEntity.getReqUpdatedOS());
                                                    openingBalanceTransaction.setReqDeletedDevice(accountEntity.getReqUpdatedDevice());
                                                    openingBalanceTransaction.setReqDeletedReferer(accountEntity.getReqUpdatedReferer());

                                                    // delete previous opening balance transaction ledger entry
                                                    openingBalanceLedgerRow.setDeletedAt(accountEntity.getUpdatedAt());
                                                    openingBalanceLedgerRow.setDeletedBy(accountEntity.getUpdatedBy());
                                                    openingBalanceLedgerRow.setReqDeletedIP(accountEntity.getReqUpdatedIP());
                                                    openingBalanceLedgerRow.setReqDeletedPort(accountEntity.getReqUpdatedPort());
                                                    openingBalanceLedgerRow.setReqDeletedBrowser(accountEntity.getReqUpdatedBrowser());
                                                    openingBalanceLedgerRow.setReqDeletedOS(accountEntity.getReqUpdatedOS());
                                                    openingBalanceLedgerRow.setReqDeletedDevice(accountEntity.getReqUpdatedDevice());
                                                    openingBalanceLedgerRow.setReqDeletedReferer(accountEntity.getReqUpdatedReferer());

                                                    return accountRepository.save(accountEntity)
                                                            .then(transactionRepository.save(openingBalanceTransaction))
                                                            .then(ledgerEntryRepository.save(openingBalanceLedgerRow))
                                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                                                })
                                        ).switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                            }

                            // delete the record
                            else {
                                //If Not Exists in Pvt Then Allow Deleting
                                accountEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                accountEntity.setDeletedBy(UUID.fromString(userId));
                                accountEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                accountEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                accountEntity.setReqDeletedIP(reqIp);
                                accountEntity.setReqDeletedPort(reqPort);
                                accountEntity.setReqDeletedBrowser(reqBrowser);
                                accountEntity.setReqDeletedOS(reqOs);
                                accountEntity.setReqDeletedDevice(reqDevice);
                                accountEntity.setReqDeletedReferer(reqReferer);

                                return accountRepository.save(accountEntity)
                                        .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                        .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again"))
                                        .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record. Please Contact Developer."));
                            }
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseInfoMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_accounts_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

                    return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                            .flatMap(previousAccountEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousAccountEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                AccountEntity updatedAccountEntity = AccountEntity.builder()
                                        .uuid(previousAccountEntity.getUuid())
                                        .name(previousAccountEntity.getName())
                                        .description(previousAccountEntity.getDescription())
                                        .parentAccountUUID(previousAccountEntity.getParentAccountUUID())
                                        .accountTypeUUID(previousAccountEntity.getAccountTypeUUID())
                                        .companyUUID(previousAccountEntity.getCompanyUUID())
                                        .code(previousAccountEntity.getCode())
                                        .controlCode(previousAccountEntity.getControlCode())
                                        .level(previousAccountEntity.getLevel())
                                        .isEntryAllowed(previousAccountEntity.getIsEntryAllowed())
                                        .isOpeningBalance(previousAccountEntity.getIsOpeningBalance())
                                        .openingBalanceUUID(previousAccountEntity.getOpeningBalanceUUID())
                                        .status(status == true ? true : false)
                                        .createdAt(previousAccountEntity.getCreatedAt())
                                        .createdBy(previousAccountEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userId))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousAccountEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousAccountEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousAccountEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousAccountEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousAccountEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousAccountEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousAccountEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousAccountEntity.setDeletedBy(UUID.fromString(userId));
                                previousAccountEntity.setReqDeletedIP(reqIp);
                                previousAccountEntity.setReqDeletedPort(reqPort);
                                previousAccountEntity.setReqDeletedBrowser(reqBrowser);
                                previousAccountEntity.setReqDeletedOS(reqOs);
                                previousAccountEntity.setReqDeletedDevice(reqDevice);
                                previousAccountEntity.setReqDeletedReferer(reqReferer);

                                return accountRepository.save(previousAccountEntity)
                                        .then(accountRepository.save(updatedAccountEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }


//    ------------------------End of Custom Mapping Functions ----------------------------------

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

//
//    //If Balance Income And Search Code
//    public Mono<ServerResponse> updateSearchCodeAndBalanceIncome(ServerRequest serverRequest, AccountEntity accountEntity) {
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    //check search Code and Balance Income
//                    return balanceAndIncomeLineRepository.findByIdAndDeletedAtIsNull(Long.valueOf(value.getFirst("balanceIncomeLineId")))
//                            .flatMap(balanceIncomeLineData -> {
//                                return accountRepository.findFirstBySearchCodeIdAndDeletedAtIsNullAndIdIsNot(Long.valueOf(value.getFirst("searchCodeId")), accountEntity.getId())
//                                        .flatMap(uniqueSearchCode -> responseInfoMsg("The Requested Search Code Already exists!"))
//                                        .switchIfEmpty(Mono.defer(() -> searchCodeRepository.findByIdAndDeletedAtIsNull(Long.valueOf(value.getFirst("searchCodeId")))
//                                                .flatMap(checkSearchCode1 -> {
//                                                    Long parentAccount = null;
//                                                    accountEntity.setParentAccountId(parentAccount);
//                                                    accountEntity.setSearchCodeId(Long.valueOf(value.getFirst("searchCodeId")));
//                                                    accountEntity.setBalanceIncomeLineId(Long.valueOf(value.getFirst("balanceIncomeLineId")));
//                                                    return accountRepository.save(accountEntity)
//                                                            .flatMap(accountMapping -> responseSuccessMsg("Record Updated successfully", accountEntity))
//                                                            .switchIfEmpty(responseErrorMsg("There is something Wrong Please try again!"));
//                                                }).switchIfEmpty(responseInfoMsg("Search Code does not exist")).onErrorResume(ex -> responseInfoMsg("Create Search Code First"))
//                                        ));
//                            }).switchIfEmpty(responseInfoMsg("Balance Income Line does not exist")).onErrorResume(ex -> responseInfoMsg("Create Balance Income Line First"));
//                });
//    }
//
//    //If Search Code Exists Only
//    public Mono<ServerResponse> updateSearchCodeOnly(ServerRequest serverRequest, AccountEntity accountEntity) {
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    //check search Code Only
//                    return accountRepository.findFirstBySearchCodeIdAndDeletedAtIsNullAndIdIsNot(Long.valueOf(value.getFirst("searchCodeId")), accountEntity.getId())
//                            .flatMap(checkOffice -> responseInfoMsg("Search Code Already Exists"))
//                            .switchIfEmpty(Mono.defer(() -> {
//                                return searchCodeRepository.findByIdAndDeletedAtIsNull(Long.valueOf(value.getFirst("searchCodeId")))
//                                        .flatMap(saveAccount -> {
//                                            Long balanceIncomeLine = null;
//                                            accountEntity.setBalanceIncomeLineId(balanceIncomeLine);
//                                            Long parentAccount = null;
//                                            accountEntity.setParentAccountId(parentAccount);
//
//                                            accountEntity.setSearchCodeId(Long.valueOf(value.getFirst("searchCodeId"))); //set parent account
//                                            return accountRepository.save(accountEntity)
//                                                    .flatMap(accountMapping -> responseSuccessMsg("Record Updated successfully", accountEntity))
//                                                    .switchIfEmpty(responseErrorMsg("There is something Wrong Please try again!"));
//                                        })
//                                        .switchIfEmpty(responseInfoMsg("Search Code Does not exists"))
//                                        .onErrorResume(ex -> responseInfoMsg("Create Search Code First.Please Contact Developer."));
//                            }));
//                });
//    }


//    //show Accounts for Voucher With Search Filter on Search code Field
//    public Mono<ServerResponse> showAccountsWithSearchCodeFilter(ServerRequest serverRequest) {
//
//        final long voucherId = Long.parseLong(serverRequest.pathVariable("voucherId"));
//        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
//        if (size > 100) {
//            size = 100;
//        }
//        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
//        int page = pageRequest - 1;
//        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
//        Sort.Direction direction;
//        switch (d.toLowerCase()) {
//            case "asc":
//                direction = Sort.Direction.ASC;
//                break;
//            case "desc":
//                direction = Sort.Direction.DESC;
//                break;
//            default:
//                direction = Sort.Direction.ASC;
//        }
//
//        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
//        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
//
//        //Optional Query Parameter Based of Status
//        Optional<String> status = serverRequest.queryParam("status");
//
//        if (status.isPresent()) {
//            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
//                    .showAccountListWithSearchCodeFilterAndStatusFilter(voucherId, searchKeyWord, Boolean.valueOf(status.get()), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
//            return slaveAccountEntityFlux
//                    .collectList()
//                    .flatMap(accountEntity ->
//                            slaveAccountRepository
//                                    .countWithSearchCodeAndStatusFilterWithVoucher(voucherId, searchKeyWord, Boolean.valueOf(status.get()))
//                                    .flatMap(count ->
//                                    {
//                                        if (accountEntity.isEmpty()) {
//                                            return responseInfoMsg("Record does not exist");
//                                        } else {
//                                            return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
//                                        }
//                                    })).switchIfEmpty(responseErrorMsg("Unable to read the request!"))
//                    .onErrorResume(err -> responseErrorMsg("Unable to read the request!"));
//        } else {
//            Flux<SlaveAccountEntity> slaveAccountEntityFlux = slaveAccountRepository
//                    .showAccountListWithSearchCodeFilter(voucherId, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
//            return slaveAccountEntityFlux
//                    .collectList()
//                    .flatMap(accountEntity ->
//                            slaveAccountRepository
//                                    .countWithSearchCodeFilter(voucherId, searchKeyWord)
//                                    .flatMap(count ->
//                                    {
//                                        if (accountEntity.isEmpty()) {
//                                            return responseInfoMsg("Record does not exist");
//                                        } else {
//                                            return responseIndexSuccessMsg("All Records Fetched Successfully!", accountEntity, count);
//                                        }
//                                    })).switchIfEmpty(responseErrorMsg("Unable to read the request!"))
//                    .onErrorResume(err -> responseErrorMsg("Unable to read the request!"));
//        }
//    }

}
