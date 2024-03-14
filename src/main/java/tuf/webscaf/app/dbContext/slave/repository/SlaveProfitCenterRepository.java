package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithProfitCenterRepository;

import java.util.UUID;

@Repository
public interface SlaveProfitCenterRepository extends ReactiveCrudRepository<SlaveProfitCenterEntity, Long>, SlaveCustomProfitCenterGroupProfitCenterPvtRepository, SlaveCustomVoucherWithProfitCenterRepository {
    Flux<SlaveProfitCenterEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records based on Status Filter
    Flux<SlaveProfitCenterEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    //Count All Records based on Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    Mono<SlaveProfitCenterEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveProfitCenterEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveProfitCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveProfitCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    //Find By Branch id In Config Module
    Mono<SlaveProfitCenterEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Mono<SlaveProfitCenterEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    //query for getting count of Profit centers for a given Profit center group
    @Query("select count(*) from profit_centers \n" +
            "join profit_center_group_profit_center_pvt " +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid \n" +
            "join profit_center_groups " +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
            "where profit_centers.deleted_at is null \n" +
            "and profit_center_groups.deleted_at is null \n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and profit_center_groups.uuid = :profitCenterGroupUUID\n" +
            "AND (profit_centers.name ILIKE concat('%',:name,'%') " +
            "OR profit_centers.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countMappedProfitCenterAgainstProfitCenterGroup(UUID profitCenterGroupUUID, String name, String description);

    @Query("select count(*) from profit_centers \n" +
            "join profit_center_group_profit_center_pvt " +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid \n" +
            "join profit_center_groups " +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
            "where profit_centers.deleted_at is null \n" +
            "and profit_center_groups.deleted_at is null \n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and profit_center_groups.uuid = :profitCenterGroupUUID\n" +
            "AND profit_centers.status= :status \n" +
            "AND (profit_centers.name ILIKE concat('%',:name,'%') " +
            "OR profit_centers.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countMappedProfitCenterAgainstProfitCenterGroupWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status);

    //query for getting count of profit Centers for a given voucher
    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%') OR " +
            "profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countRecordsWithSearchFilter(UUID voucherUUID, String name, String description);

    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and profit_centers.status= :status \n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%') OR " +
            "profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countRecordsWithSearchFilterWithStatus(UUID voucherUUID, String name, String description, Boolean status);

    //query for getting count of profit centers for a voucher with given companyId
    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.company_uuid = :companyUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%')\n" +
            "or profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countProfitCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description);

    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.company_uuid = :companyUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and profit_centers.status= :status \n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%')\n" +
            "or profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countProfitCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status);

    //query for getting count of profit centers for a voucher with given branchId
    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.branch_uuid = :branchUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%')\n" +
            "or profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countProfitCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description);

    @Query("select count(distinct profit_centers.uuid) from profit_centers\n" +
            "left join profit_center_group_profit_center_pvt\n" +
            "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_centers.branch_uuid = :branchUUID\n" +
            "and profit_centers.deleted_at is null\n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and profit_centers.status= :status \n" +
            "and (profit_centers.name ILIKE concat('%',:name,'%')\n" +
            "or profit_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countProfitCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status);

    //query used in pvt mapping handler in profit Center with Profit Center Groups
    @Query("SELECT count(*) FROM profit_centers\n" +
            "WHERE profit_centers.uuid NOT IN(\n" +
            "SELECT profit_centers.uuid FROM profit_centers\n" +
            "LEFT JOIN profit_center_group_profit_center_pvt\n" +
            "ON profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid \n" +
            "WHERE profit_center_group_profit_center_pvt.profit_center_group_uuid = :profitCenterGroupUUID\n" +
            "AND profit_center_group_profit_center_pvt.deleted_at IS NULL\n" +
            "AND profit_centers.deleted_at IS NULL )\n" +
            "AND profit_centers.deleted_at IS NULL \n" +
            "AND (profit_centers.name ILIKE concat('%',:name,'%') OR " +
            "profit_centers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedProfitCenterAgainstProfitCenterGroup(UUID profitCenterGroupUUID, String name, String description);

    @Query("SELECT count(*) FROM profit_centers\n" +
            "WHERE profit_centers.uuid NOT IN(\n" +
            "SELECT profit_centers.uuid FROM profit_centers\n" +
            "LEFT JOIN profit_center_group_profit_center_pvt\n" +
            "ON profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid \n" +
            "WHERE profit_center_group_profit_center_pvt.profit_center_group_uuid = :profitCenterGroupUUID\n" +
            "AND profit_center_group_profit_center_pvt.deleted_at IS NULL\n" +
            "AND profit_centers.deleted_at IS NULL )\n" +
            "AND profit_centers.deleted_at IS NULL \n" +
            "AND profit_centers.status= :status \n" +
            "AND (profit_centers.name ILIKE concat('%',:name,'%') OR " +
            "profit_centers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedProfitCenterAgainstProfitCenterGroupWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status);
}
