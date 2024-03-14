package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveAccountRepository extends ReactiveCrudRepository<SlaveAccountEntity, Long>, SlaveCustomAccountGroupAccountPvtRepository, SlaveCustomVoucherWithAccountRepository, SlaveCustomChildParentAccountRepository, SlaveCustomFinancialReportingRepository, SlaveCustomAccountRepository, SlaveCustomSubAccountGroupAccountPvtRepository {

    Flux<SlaveAccountEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> accountList);

    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String code, String description, String controlCode);

    //Find All Records with Status Filter
    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String code, Boolean status2, String description, Boolean status3, String controlCode, Boolean status4);

    //Count All Accounts
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String code, Boolean Status2, String description, Boolean Status3, String controlCode, Boolean status4);

    //Count All Accounts WithOut Status Check
    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndDeletedAtIsNull(String name, String code, String description, String controlCode);

    //Find All Records with Company ID
    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID companyId, String code, UUID companyId2, String description, UUID companyId3, String controlCode, UUID companyId4);

    //Count All Account With Name,Description Company UUID With Status Check
    Mono<Long> countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNull(String name, UUID companyId, String code, UUID companyId2, String description, UUID companyId3, String controlCode, UUID companyId4);

    //Find All Records with Company UUID and Status Filter
    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, UUID companyId, String code, Boolean status2, UUID companyId2, String description, Boolean status3, UUID companyId3, String controlCode, Boolean status4, UUID companyId4);

    //Count All Account With Company id
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndStatusAndCompanyUUIDAndDeletedAtIsNull(String name, Boolean status, UUID companyId, String code, Boolean status2, UUID companyId2, String description, Boolean status3, UUID companyId3, String controlCode, Boolean status4, UUID companyId4);

    Mono<SlaveAccountEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAccountEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveAccountEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    Mono<SlaveAccountEntity> findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(String controlCode);

    Mono<SlaveAccountEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyId);

    Flux<SlaveAccountEntity> findAllByParentAccountUUIDAndDeletedAtIsNull(UUID parentAccountUUID, Sort sort);

    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean isEntryAllowed, Boolean status, String code, Boolean isEntryAllowed2, Boolean status2, String description, Boolean isEntryAllowed3, Boolean status3, String controlCode, Boolean isEntryAllowed4, Boolean status4);

    Mono<Long> countByNameContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndStatusAndDeletedAtIsNull(String name, Boolean isEntryAllowed, Boolean status, String code, Boolean isEntryAllowed2, Boolean status2, String description, Boolean isEntryAllowed3, Boolean status3, String controlCode, Boolean isEntryAllowed4, Boolean status4);

    Mono<Long> countByNameContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndIsEntryAllowedAndDeletedAtIsNull(String name, Boolean isEntryAllowed, String code, Boolean isEntryAllowed2, String description, Boolean isEntryAllowed3, String controlCode, Boolean isEntryAllowed4);

    Flux<SlaveAccountEntity> findAllByNameContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID accountTypeUUID, Boolean status1, String code, UUID accountTypeUUID2, Boolean status2, String description, UUID accountTypeUUID3, Boolean status3, String controlCode, UUID accountTypeUUID4, Boolean status4);

    Mono<Long> countByNameContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNullOrControlCodeContainingIgnoreCaseAndAccountTypeUUIDAndStatusAndDeletedAtIsNull(String name, UUID accountTypeUUID, Boolean status1, String code, UUID accountTypeUUID2, Boolean status2, String description, UUID accountTypeUUID3, Boolean status3, String controlCode, UUID accountTypeUUID4, Boolean status4);


    /**
     * To View Count and Accounts Against a given Account Group
     * These Routes are Used to Find Accounts Based on Name,Description, And Status
     **/
    //query for getting count of accounts for a given account group based on status
    @Query("select count(*) from accounts \n" +
            "join account_group_account_pvt on accounts.uuid = account_group_account_pvt.account_uuid \n" +
            "where accounts.deleted_at is null \n" +
            "and account_group_account_pvt.deleted_at is null \n" +
            "and account_group_account_pvt.account_group_uuid = :accountGroupUUID\n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%'))" +
            " AND accounts.status= :status")
    Mono<Long> countAccountAgainstAccountGroupsWithStatusFilter(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status);

    //query for getting count of accounts for a given account group based on status
    @Query("select count(*) from accounts \n" +
            "join account_group_account_pvt on accounts.uuid = account_group_account_pvt.account_uuid \n" +
            "where accounts.deleted_at is null \n" +
            "and account_group_account_pvt.deleted_at is null \n" +
            "and account_group_account_pvt.account_group_uuid = :accountGroupUUID\n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countAccountAgainstAccountGroups(UUID accountGroupUUID, String name, String description, String code, String controlCode);


    /**
     * To View Count and Accounts Against a Given Voucher These Two Routes are Used
     * These Routes are Used to Search Accounts Based on Name And Status
     **/

    //query for getting count of accounts for a given voucher with search filter on name
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.deleted_at IS NULL " +
            "AND account_group_account_pvt.deleted_at IS NULL " +
            "AND voucher_account_group_pvt.deleted_at IS NULL " +
            "AND accounts.name ilike concat('%',:name,'%')")
    Mono<Long> countAccountWithWithNameFilterAgainstVoucher(UUID voucherUUID, String name);

    //query for getting count of accounts for a given voucher with search filter on name and Status
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.deleted_at IS NULL " +
            "AND account_group_account_pvt.deleted_at IS NULL " +
            "AND voucher_account_group_pvt.deleted_at IS NULL " +
            "AND accounts.name ilike concat('%',:name,'%')" +
            "AND accounts.status= :status")
    Mono<Long> countAccountWithWithNameAndStatusFilterAgainstVoucher(UUID voucherUUID, String name, Boolean status);

    /**
     * To View Count and Accounts Against a Given Voucher These Two Routes are Used
     * These Routes are Used to Search Accounts Based on Name, Description, Code, Control Code And Status
     **/
    //query for getting count of accounts for a given voucher with search filter on name
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.is_entry_allowed = true\n" +
            "AND accounts.deleted_at IS NULL\n" +
            "AND account_group_account_pvt.deleted_at IS NULL\n" +
            "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
            "AND (accounts.name ilike concat('%',:name,'%')\n" +
            "OR accounts.description ilike concat('%',:description,'%')\n" +
            "OR accounts.code ilike concat('%',:code,'%')\n" +
            "OR accounts.control_code ilike concat('%',:controlCode,'%') )")
    Mono<Long> countAccountWithAgainstVoucher(UUID voucherUUID, String name, String description, String code, String controlCode);

    //query for getting count of accounts for a given voucher with search filter on name and Status
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.is_entry_allowed = true\n" +
            "AND accounts.deleted_at IS NULL\n" +
            "AND account_group_account_pvt.deleted_at IS NULL\n" +
            "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
            "AND (accounts.name ilike concat('%',:name,'%')\n" +
            "OR accounts.description ilike concat('%',:description,'%')\n" +
            "OR accounts.code ilike concat('%',:code,'%')\n" +
            "OR accounts.control_code ilike concat('%',:controlCode,'%') )\n" +
            "AND accounts.status= :status")
    Mono<Long> countAccountWithStatusFilterAgainstVoucher(UUID voucherUUID, String name, String description, String code, String controlCode, Boolean status);

    /**
     * To View Count and Accounts Against a Given Voucher These Two Routes are Used
     * These Routes are Used to Search Accounts Based on Code And Status
     **/
    //query for getting count of accounts for a given voucher with search filter on code
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.deleted_at IS NULL " +
            "AND account_group_account_pvt.deleted_at IS NULL " +
            "AND voucher_account_group_pvt.deleted_at IS NULL " +
            "AND accounts.code ilike concat('%',:code,'%')")
    Mono<Long> countWithAccountCodeFilter(UUID voucherUUID, String code);

    //query for getting count of accounts for a given voucher with search filter on code and Status Field
    @Query("SELECT COUNT(distinct accounts.uuid) FROM public.accounts " +
            "LEFT JOIN account_group_account_pvt " +
            "ON accounts.uuid = account_group_account_pvt.account_uuid " +
            "LEFT JOIN voucher_account_group_pvt " +
            "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid " +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID " +
            "AND accounts.deleted_at IS NULL " +
            "AND account_group_account_pvt.deleted_at IS NULL " +
            "AND voucher_account_group_pvt.deleted_at IS NULL " +
            "AND accounts.code ILIKE concat('%',:code,'%')" +
            "AND accounts.status= :status")
    Mono<Long> countWithAccountCodeAndStatusFilter(UUID voucherUUID, String code, Boolean status);


    /**
     * To View Count and Accounts Against a Given Voucher These Two Routes are Used
     * These Routes are Used to Search Accounts Based on Company And Status
     **/
    //query for getting count of accounts for a voucher with given companyUUID
    @Query("select count(distinct accounts.uuid) from accounts\n" +
            "left join account_group_account_pvt\n" +
            "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
            "left join voucher_account_group_pvt\n" +
            "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
            "where voucher_account_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and accounts.company_uuid = :companyUUID\n" +
            "and accounts.deleted_at is null\n" +
            "and account_group_account_pvt.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and (accounts.name ILIKE concat('%',:name,'%')\n" +
            "or accounts.description ILIKE concat('%',:description,'%')" +
            "or accounts.code ILIKE concat('%',:code,'%') )")
    Mono<Long> countWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String code);

    //query for getting count of accounts for a voucher with given companyId Based on Status Filter and Other Filters
    @Query("select count(distinct accounts.uuid) from accounts\n" +
            "left join account_group_account_pvt\n" +
            "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
            "left join voucher_account_group_pvt\n" +
            "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
            "where voucher_account_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and accounts.company_uuid = :companyUUID\n" +
            "and accounts.deleted_at is null\n" +
            "and account_group_account_pvt.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and (accounts.name ILIKE concat('%',:name,'%')\n" +
            "or accounts.description ILIKE concat('%',:description,'%')" +
            "or accounts.code ILIKE concat('%',:code,'%') )" +
            "AND accounts.status= :status")
    Mono<Long> countAccountsAgainstCompanyIdBasedWithStatusFilter(UUID voucherUUID, Boolean status, UUID companyUUID, String name, String description, String code);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM accounts\n" +
            "WHERE accounts.uuid NOT IN(\n" +
            "SELECT accounts.uuid FROM accounts\n" +
            "LEFT JOIN account_group_account_pvt\n" +
            "ON account_group_account_pvt.account_uuid = accounts.uuid \n" +
            "WHERE account_group_account_pvt.account_group_uuid = :accountGroupUUID\n" +
            "AND account_group_account_pvt.deleted_at IS NULL\n" +
            "AND accounts.deleted_at IS NULL )\n" +
            "AND accounts.deleted_at IS NULL " +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countUnMappedAccountRecords(UUID accountGroupUUID, String name, String description, String code, String controlCode);

    @Query("SELECT count(*) FROM accounts\n" +
            "WHERE accounts.uuid NOT IN(\n" +
            "SELECT accounts.uuid FROM accounts\n" +
            "LEFT JOIN account_group_account_pvt\n" +
            "ON account_group_account_pvt.account_uuid = accounts.uuid \n" +
            "WHERE account_group_account_pvt.account_group_uuid = :accountGroupUUID\n" +
            "AND account_group_account_pvt.deleted_at IS NULL\n" +
            "AND accounts.deleted_at IS NULL )\n" +
            "AND accounts.deleted_at IS NULL " +
            "AND accounts.status= :status " +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countUnMappedAccountRecordsWithStatus(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status);

    /**
     * Financial Chart of Accounts Count from Child to Parent reporting
     **/
    @Query(" WITH RECURSIVE uplines(uuid,control_code,parent_account_uuid, level) AS (\n" +
            "  SELECT tree.uuid,  tree.control_code,tree.parent_account_uuid, level \n" +
            "  FROM accounts AS tree\n" +
            "  WHERE tree.deleted_at is null\n" +
            "  UNION ALL \n" +
            "  SELECT tree.uuid,  tree.control_code,tree.parent_account_uuid, a.level + 1\n" +
            "  FROM accounts AS tree JOIN uplines AS a ON tree.uuid = a.parent_account_uuid\n" +
            "  WHERE \n" +
            " tree.deleted_at is null\n" +
            " and a.level < tree.level\n" +
            ") \n" +
            "SELECT count(distinct uuid)\n" +
            "FROM uplines ")
    Mono<Long> countFinancialChartOfAccounts();


    /**
     * Financial Chart of Accounts Count from Child to Parent reporting with PAGINATION
     **/
    @Query("WITH RECURSIVE rec (uuid,name, control_code,parent_account_uuid, level, path) as\n" +
            "(\n" +
            "  SELECT tree.uuid,  tree.name, tree.control_code,tree.parent_account_uuid, tree.level, \n" +
            "    ROW_NUMBER() \n" +
            " OVER (PARTITION BY tree.parent_account_uuid ORDER BY tree.name):: text as path\n" +
            "    from accounts as tree\n" +
            " where tree.parent_account_uuid is null\n" +
            " and tree.deleted_at is null\n" +
            "\n" +
            "  UNION ALL\n" +
            "  SELECT accounts.uuid,  accounts.name, accounts.control_code,accounts.parent_account_uuid, accounts.level,\n" +
            "    concat(\n" +
            " rec.path, '-', \n" +
            "ROW_NUMBER() \n" +
            "   OVER (PARTITION BY accounts.parent_account_uuid ORDER BY accounts.name):: text\n" +
            " ) as path\n" +
            "    from rec, \n" +
            " accounts\n" +
            " where accounts.parent_account_uuid = rec.uuid\n" +
            " and accounts.deleted_at is null\n" +
            ")\n" +
            "select count (distinct uuid) " +
            "from rec")
    Mono<Long> countFinancialChartOfAccountWithPagination();


    /**
     * Count child to parent hierarchy records
     **/
    @Query(" WITH RECURSIVE childs (uuid,parent_account_uuid,control_code,level) as\n" +
            "(\n" +
            "  SELECT  accounts.uuid,accounts.parent_account_uuid,accounts.control_code,accounts.level  \n" +
            "    from accounts where accounts.uuid = :parentAccountUUID\n" +
            "    and accounts.deleted_at is null \n" +
            "\t\n" +
            "     UNION ALL\n" +
            "\t\n" +
            "   SELECT accounts.uuid,accounts.parent_account_uuid,accounts.control_code,accounts.level\n" +
            "\tfrom childs,\n" +
            "\taccounts where accounts.uuid = childs.parent_account_uuid\n" +
            "   and accounts.deleted_at is null \n" +
            ") \n" +
            " SELECT  count(*) FROM childs")
    Mono<Long> countChildToParentRecords(UUID parentAccountUUID);


    /**
     * Count parent to child hierarchy records
     **/
    @Query(" WITH RECURSIVE rec (uuid,parent_account_uuid,control_code,level) as\n" +
            "( \n" +
            " SELECT tree.uuid, tree.parent_account_uuid,tree.control_code,tree.level \n" +
            " from accounts as tree\n" +
            "  where tree.uuid = :accountUUID\n" +
            "\tand tree.deleted_at is null\n" +
            "  \tUNION ALL \n" +
            " SELECT tree.uuid, tree.parent_account_uuid,tree.control_code,tree.level\n" +
            "\tfrom rec, \n" +
            "  \taccounts as tree where tree.parent_account_uuid = rec.uuid\n" +
            "     and tree.deleted_at is null\n" +
            " ) \n" +
            "SELECT count(*) FROM rec ")
    Mono<Long> countParentToChildRecords(UUID accountUUID);

    /**
     * Count Trial Balance Records based on the Aggregate/Sum of Accounts their Debit and Credit Amount
     **/
    @Query("select \n" +
            "count (*)\n" +
            "from\n" +
            "(select accounts.code as accountCode,accounts.name as accountName,accounts.uuid as accountUUID,\n" +
            "accounts.account_type_uuid as accountTypeUUID,account_types.name as accountTypeName," +
            "transactions.transaction_date as transactionDate,\n" +
            "CASE \n" +
            "WHEN transactions.transaction_date \n" +
            "BETWEEN :startDate  AND :endDate \n" +
            "THEN \n" +
            "sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) \n" +
            "ELSE 0.0\n" +
            "END as netBalance,\n" +
            "CASE \n" +
            "WHEN transactions.transaction_date > :startDate \n" +
            "THEN \n" +
            "sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) \n" +
            "ELSE 0.0\n" +
            "END as balanceBroughtFoward,\n" +
            "CASE \n" +
            "WHEN sum(ledger_entries.dr_amount) is not null \n" +
            "THEN sum(ledger_entries.dr_amount)\n" +
            "ELSE \n" +
            "0.0\n" +
            "END as debitAmount,\n" +
            "CASE \n" +
            "WHEN sum(ledger_entries.cr_amount) is not null \n" +
            "THEN sum(ledger_entries.cr_amount)\n" +
            "ELSE \n" +
            "0.0\n" +
            "END as creditAmount\n" +
            "from accounts\n" +
            "left join account_types " +
            "on accounts.account_type_uuid=account_types.uuid \n" +
            "left join ledger_entries \n" +
            "on accounts.uuid=ledger_entries.account_uuid\n" +
            "left join transactions\n" +
            "on transactions.uuid=ledger_entries.transaction_uuid\n" +
            "where ledger_entries.deleted_at is null\n" +
            "and transactions.deleted_at is null\n" +
            "and accounts.deleted_at is null\n" +
            "and account_types.deleted_at is null\n" +
            "GROUP BY (accountUUID,accountCode,accountName,accountTypeUUID,transactionDate,accountTypeName) \n" +
            ") as accounts")
    Mono<Long> countTrialBalanceRecords(LocalDateTime startDate, LocalDateTime endDate);
//
//    @Query("select count(*) from\n" +
//            "(" +
//            "select sum(ledger_entries.dr_amount) as debitAccount,\n" +
//            "sum(ledger_entries.cr_amount) as creditAccount,\n" +
//            "accounts.code as accountCode,accounts.name as accountName,accounts.uuid as accountUUID,\n" +
//            " sum(ledger_entries.dr_amount) - sum(ledger_entries.cr_amount) as netBalance \n" +
//            "from ledger_entries\n" +
//            "left join accounts \n" +
//            "on accounts.uuid=ledger_entries.account_uuid\n" +
//            "left join transactions\n" +
//            "on transactions.uuid=ledger_entries.transaction_uuid\n" +
//            "where ledger_entries.deleted_at is null\n" +
//            "and transactions.deleted_at is null\n" +
//            "and accounts.deleted_at is null\n" +
//            "and transactions.transaction_date BETWEEN :startDate AND :endDate \n" +
//            "GROUP BY (accountUUID,accountCode,accountName)\n" +
//            ") as accounts")
//    Mono<Long> countTrialBalanceRecords(LocalDateTime startDate,LocalDateTime endDate);


    /**
     * To View Count and Accounts Against a given Sub Account Group
     * These Routes are Used to Find Accounts Based on Name, Code, Description, Control Code And Status
     **/
    //query for getting count of accounts for a given sub account group based on status
    @Query("select count(*) from accounts \n" +
            "join sub_account_group_accounts_pvt \n" +
            "on accounts.uuid = sub_account_group_accounts_pvt.account_uuid \n" +
            "join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid = sub_account_groups.uuid \n" +
            "and accounts.deleted_at is null " +
            "and accounts.status= :status " +
            "and sub_account_groups.deleted_at is null \n" +
            "and sub_account_group_accounts_pvt.deleted_at is null \n" +
            "and sub_account_group_accounts_pvt.sub_account_group_uuid = :subAccountGroupUUID\n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countAccountAgainstSubAccountGroupsWithStatusFilter(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, Boolean status);

    //query for getting count of accounts for a given sub account group
    @Query("select count(*) from accounts \n" +
            "join sub_account_group_accounts_pvt \n" +
            "on accounts.uuid = sub_account_group_accounts_pvt.account_uuid \n" +
            "join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid = sub_account_groups.uuid \n" +
            "and accounts.deleted_at is null " +
            "and sub_account_groups.deleted_at is null \n" +
            "and sub_account_group_accounts_pvt.deleted_at is null \n" +
            "and sub_account_group_accounts_pvt.sub_account_group_uuid = :subAccountGroupUUID\n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countAccountAgainstSubAccountGroups(UUID subAccountGroupUUID, String name, String description, String code, String controlCode);


    //query used in sub account group accounts pvt mapping handler
    @Query("SELECT count(*) FROM accounts\n" +
            "WHERE accounts.uuid NOT IN(\n" +
            "SELECT accounts.uuid FROM accounts\n" +
            "LEFT JOIN sub_account_group_accounts_pvt\n" +
            "ON sub_account_group_accounts_pvt.account_uuid = accounts.uuid \n" +
            "WHERE sub_account_group_accounts_pvt.sub_account_group_uuid = :subAccountGroupUUID\n" +
            "AND sub_account_group_accounts_pvt.deleted_at IS NULL\n" +
            "AND accounts.deleted_at IS NULL )\n" +
            "AND accounts.deleted_at IS NULL " +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countUnMappedRecordsAgainstSubAccountGroup(UUID subAccountGroupUUID, String name, String description, String code, String controlCode);

    @Query("SELECT count(*) FROM accounts\n" +
            "WHERE accounts.uuid NOT IN(\n" +
            "SELECT accounts.uuid FROM accounts\n" +
            "LEFT JOIN sub_account_group_accounts_pvt\n" +
            "ON sub_account_group_accounts_pvt.account_uuid = accounts.uuid \n" +
            "WHERE sub_account_group_accounts_pvt.sub_account_group_uuid = :subAccountGroupUUID\n" +
            "AND sub_account_group_accounts_pvt.deleted_at IS NULL\n" +
            "AND accounts.deleted_at IS NULL )\n" +
            "AND accounts.deleted_at IS NULL " +
            "AND accounts.status= :status " +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countUnMappedRecordsAgainstSubAccountGroupWithStatus(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, Boolean status);

    /**
     * Fetch Accounts List that are mapped with sub account groups and Vouchers
     * These Routes are Used to Find Accounts Based on Name,Description,Control Control And Status
     **/

    @Query("select count(*) \n" +
            "from accounts \n" +
            "left join sub_account_group_accounts_pvt on accounts.uuid=sub_account_group_accounts_pvt.account_uuid\n" +
            "left join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid=sub_account_groups.uuid\n" +
            "left join voucher_sub_account_groups_pvt on sub_account_groups.uuid=voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
            "where accounts.deleted_at is null\n" +
            " and accounts.status=:status \n" +
            " and sub_account_group_accounts_pvt.deleted_at is null\n" +
            "and sub_account_groups.deleted_at is null\n" +
            "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
            "and voucher_uuid= :voucherUUID \n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countAccountAgainstSubAccountGroupsAndVoucherWithStatusFilter(UUID voucherUUID, Boolean status, String name, String description, String code, String controlCode);

    @Query("select count(*) \n" +
            "from accounts \n" +
            "left join sub_account_group_accounts_pvt on accounts.uuid=sub_account_group_accounts_pvt.account_uuid\n" +
            "left join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid=sub_account_groups.uuid\n" +
            "left join voucher_sub_account_groups_pvt on sub_account_groups.uuid=voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
            "where accounts.deleted_at is null\n" +
            "and sub_account_group_accounts_pvt.deleted_at is null\n" +
            "and sub_account_groups.deleted_at is null\n" +
            "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
            "and voucher_uuid= :voucherUUID \n" +
            "AND (accounts.name ILIKE concat('%',:name,'%')" +
            "OR accounts.description ILIKE concat('%',:description,'%')" +
            "OR accounts.code ILIKE concat('%',:code,'%')" +
            "OR accounts.control_code ILIKE  concat('%',:controlCode,'%')) ")
    Mono<Long> countAccountAgainstSubAccountGroupsAndVoucherWithoutStatusFilter(UUID voucherUUID, String name, String description, String code, String controlCode);
}
