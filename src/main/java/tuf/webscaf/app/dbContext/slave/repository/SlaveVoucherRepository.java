package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.*;

import java.util.UUID;

@Repository
public interface SlaveVoucherRepository extends ReactiveCrudRepository<SlaveVoucherEntity, Long>, SlaveCustomVoucherGroupVoucherPvtRepository,
        SlaveCustomJobGroupWithVoucherRepository, SlaveCustomAccountGroupWithVoucherRepository, SlaveCustomCalendarGroupWithVoucherRepository,
        SlaveCustomCostCenterGroupWithVoucherRepository, SlaveCustomProfitCenterGroupWithVoucherRepository, SlaveCustomBranchWithVoucherRepository,
        SlaveCustomCompanyWithVoucherRepository, SlaveCustomVoucherRepository {

    Mono<SlaveVoucherEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveVoucherEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Flux<SlaveVoucherEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    // query used for count of mapped vouchers for a voucher group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_group_voucher_pvt\n" +
            "on vouchers.uuid = voucher_group_voucher_pvt.voucher_uuid\n" +
            "where voucher_group_voucher_pvt.voucher_group_uuid = :voucherGroupUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_group_voucher_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVoucherAgainstVoucherGroup(UUID voucherGroupUUID, String name);

    // query used for count of mapped vouchers for a voucher group with status
    @Query("select count(*) from vouchers\n" +
            "left join voucher_group_voucher_pvt\n" +
            "on vouchers.uuid = voucher_group_voucher_pvt.voucher_uuid\n" +
            "where voucher_group_voucher_pvt.voucher_group_uuid = :voucherGroupUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_group_voucher_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVoucherListWithStatusAgainstVoucherGroup(UUID voucherGroupUUID, String name, Boolean status);

    /*** Count Un mapped Vouchers Against Voucher Group in Voucher Voucher Group Pvt Handler **/
    @Query("SELECT count(*) FROM vouchers\n" +
            "WHERE vouchers.uuid NOT IN(\n" +
            "SELECT vouchers.uuid FROM vouchers\n" +
            "LEFT JOIN voucher_group_voucher_pvt\n" +
            "ON voucher_group_voucher_pvt.voucher_uuid = vouchers.uuid\n" +
            "WHERE voucher_group_voucher_pvt.voucher_group_uuid = :voucherGroupUUID\n" +
            "AND voucher_group_voucher_pvt.deleted_at IS NULL\n" +
            "AND vouchers.deleted_at IS NULL)\n" +
            "AND vouchers.deleted_at IS NULL \n" +
            "AND (vouchers.name ILIKE concat('%',:name,'%') " +
            "or vouchers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedVoucherAgainstVoucherGroup(UUID voucherGroupUUID, String name, String description);

    @Query("SELECT count(*) FROM vouchers\n" +
            "WHERE vouchers.uuid NOT IN(\n" +
            "SELECT vouchers.uuid FROM vouchers\n" +
            "LEFT JOIN voucher_group_voucher_pvt\n" +
            "ON voucher_group_voucher_pvt.voucher_uuid = vouchers.uuid\n" +
            "WHERE voucher_group_voucher_pvt.voucher_group_uuid = :voucherGroupUUID\n" +
            "AND voucher_group_voucher_pvt.deleted_at IS NULL\n" +
            "AND vouchers.deleted_at IS NULL)\n" +
            "AND vouchers.deleted_at IS NULL \n" +
            "AND vouchers.status= :status \n" +
            "AND (vouchers.name ILIKE concat('%',:name,'%') " +
            "or vouchers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedVoucherAgainstVoucherGroupWithStatus(UUID voucherGroupUUID, String name, String description, Boolean status);

    /***  Count mapped Vouchers Against Job Group in Voucher Job Group Pvt Handler With and Without Status Filter**/
    @Query("select count(*) from vouchers\n" +
            "left join voucher_job_group_pvt\n" +
            "on vouchers.uuid = voucher_job_group_pvt.voucher_uuid\n" +
            "where voucher_job_group_pvt.job_group_uuid = :jobGroupUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstJobGroup(UUID jobGroupUUID, String name);

    // query used for count of mapped vouchers for a job group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_job_group_pvt\n" +
            "on vouchers.uuid = voucher_job_group_pvt.voucher_uuid\n" +
            "where voucher_job_group_pvt.job_group_uuid = :jobGroupUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersWithStatusAgainstJobGroup(UUID jobGroupUUID, String name, Boolean status);

    /**
     * Count Mapped Vouchers Against Account Group With and Without Status Filter
     **/
    @Query("select count(*) from vouchers\n" +
            "left join voucher_account_group_pvt\n" +
            "on vouchers.uuid = voucher_account_group_pvt.voucher_uuid\n" +
            "where voucher_account_group_pvt.account_group_uuid = :accountGroupUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstAccountGroup(UUID accountGroupUUID, String name);

    // query used for count of mapped vouchers for given account group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_account_group_pvt\n" +
            "on vouchers.uuid = voucher_account_group_pvt.voucher_uuid\n" +
            "where voucher_account_group_pvt.account_group_uuid = :accountGroupUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstAccountGroupWithStatus(UUID accountGroupUUID, String name, Boolean status);

    /**
     * Count Mapped Vouchers Against Cost Center Group With and Without Status Filter
     **/
    @Query("select count(*) from vouchers\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on vouchers.uuid = voucher_cost_center_group_pvt.voucher_uuid\n" +
            "where voucher_cost_center_group_pvt.cost_center_group_uuid = :costCenterGroupUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstCostCenterGroup(UUID costCenterGroupUUID, String name);

    // query used for count of mapped vouchers for given cost center group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on vouchers.uuid = voucher_cost_center_group_pvt.voucher_uuid\n" +
            "where voucher_cost_center_group_pvt.cost_center_group_uuid = :costCenterGroupUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstCostCenterGroupWithStatus(UUID costCenterGroupUUID, String name, Boolean status);

    /**
     * Count Mapped Vouchers Against Profit Center Group With and Without Status Filter
     **/
    @Query("select count(*) from vouchers\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on vouchers.uuid = voucher_profit_center_group_pvt.voucher_uuid\n" +
            "where voucher_profit_center_group_pvt.profit_center_group_uuid = :profitCenterGroupUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstProfitCenterGroup(UUID profitCenterGroupUUID, String name);

    // query used for count of mapped vouchers for given profit center group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on vouchers.uuid = voucher_profit_center_group_pvt.voucher_uuid\n" +
            "where voucher_profit_center_group_pvt.profit_center_group_uuid = :profitCenterGroupUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstProfitCenterGroupWithStatus(UUID profitCenterGroupUUID, String name, Boolean status);

    /**
     * Count All the Vouchers Against Company. The second Function for company counts the Vouchers with Status Filter
     **/
    // query used for count of mapped vouchers for given Company
    @Query("select count(*) from vouchers\n" +
            "left join voucher_company_pvt \n" +
            "on vouchers.uuid = voucher_company_pvt.voucher_uuid\n" +
            "where voucher_company_pvt.company_uuid = :companyUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_company_pvt.deleted_at is null\n" +
            "and (vouchers.name ILIKE concat('%',:name,'%')" +
            "or vouchers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedVouchersAgainstCompany(UUID companyUUID, String name, String description);

    // query used for count of mapped vouchers for given Company
    @Query("select count(*) from vouchers\n" +
            "left join voucher_company_pvt\n" +
            "on vouchers.uuid = voucher_company_pvt.voucher_uuid\n" +
            "where voucher_company_pvt.company_uuid = :companyUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_company_pvt.deleted_at is null\n" +
            "and (vouchers.name ILIKE concat('%',:name,'%')" +
            "or vouchers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedVouchersAgainstCompanyWithStatus(UUID companyUUID, String name, String description, Boolean status);

    /**
     * Count All the Mapped Vouchers Against Branch id. The second Function for branch counts the Vouchers with Status Filter
     **/
    // query used for count of mapped vouchers for given branch
    @Query("select count(*) from vouchers\n" +
            "left join voucher_branch_pvt \n" +
            "on vouchers.uuid = voucher_branch_pvt.voucher_uuid\n" +
            "where voucher_branch_pvt.branch_uuid = :branchUUID\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_branch_pvt.deleted_at is null\n" +
            "and (vouchers.name ILIKE concat('%',:name,'%')" +
            "or vouchers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedVouchersAgainstBranch(UUID branchUUID, String name, String description);

    // query used for count of mapped vouchers for given branch
    @Query("select count(*) from vouchers\n" +
            "left join voucher_branch_pvt\n" +
            "on vouchers.uuid = voucher_branch_pvt.voucher_uuid\n" +
            "where voucher_branch_pvt.branch_uuid = :branchUUID\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_branch_pvt.deleted_at is null\n" +
            "and (vouchers.name ILIKE concat('%',:name,'%')" +
            "or vouchers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedVouchersAgainstBranchWithStatus(UUID branchUUID, String name, String description, Boolean status);

    /**
     * Count All the Mapped Vouchers Against Calendar Group UUID. The second Function counts the Vouchers with Status Filter
     **/
    @Query("select count(*) from vouchers\n" +
            "left join voucher_calendar_group_pvt\n" +
            "on vouchers.uuid = voucher_calendar_group_pvt.voucher_uuid\n" +
            "where voucher_calendar_group_pvt.calendar_group_uuid = :calendarGroupId\n" +
            "and vouchers.deleted_at is null\n" +
            "and voucher_calendar_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstCalendarGroup(UUID calendarGroupUUID, String name);

    // query used for count of mapped vouchers for given calendar group
    @Query("select count(*) from vouchers\n" +
            "left join voucher_calendar_group_pvt\n" +
            "on vouchers.uuid = voucher_calendar_group_pvt.voucher_uuid\n" +
            "where voucher_calendar_group_pvt.calendar_group_uuid = :calendarGroupId\n" +
            "and vouchers.status = :status " +
            "and vouchers.deleted_at is null\n" +
            "and voucher_calendar_group_pvt.deleted_at is null\n" +
            "and vouchers.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedVouchersAgainstCalendarGroupWithStatus(UUID calendarGroupUUID, String name, Boolean status);
}
