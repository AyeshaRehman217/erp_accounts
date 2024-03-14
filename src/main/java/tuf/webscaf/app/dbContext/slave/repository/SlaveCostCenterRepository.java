package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithCostCenterRepository;

import java.util.UUID;

@Repository
public interface SlaveCostCenterRepository extends ReactiveCrudRepository<SlaveCostCenterEntity, Long>, SlaveCustomVoucherWithCostCenterRepository, SlaveCustomCostCenterGroupCostCenterPvtRepository {

    Flux<SlaveCostCenterEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records based on Status Filter
    Flux<SlaveCostCenterEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    //Count All Records based on Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveCostCenterEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveCostCenterEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCostCenterEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCostCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveCostCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    //Find By Branch id In Config Module
    Mono<SlaveCostCenterEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Mono<SlaveCostCenterEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<Long> countByDeletedAtIsNullAndNameContaining(String name);

    //query for getting count of cost centers for a given cost center group
    @Query("select count(*) from cost_centers \n" +
            "join cost_center_group_cost_center_pvt " +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid \n" +
            "join cost_center_groups " +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
            "where cost_centers.deleted_at is null \n" +
            "and cost_center_groups.deleted_at is null \n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and cost_center_groups.uuid = :costCenterGroupUUID\n" +
            "and (cost_centers.name ilike concat('%',:name,'%') " +
            "or cost_centers.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedCostCenterAgainstCostCenterGroup(UUID costCenterGroupUUID, String name, String description);

    @Query("select count(*) from cost_centers \n" +
            "join cost_center_group_cost_center_pvt " +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid \n" +
            "join cost_center_groups " +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
            "where cost_centers.deleted_at is null \n" +
            "and cost_center_groups.deleted_at is null \n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and cost_centers.status = :status \n" +
            "and cost_center_groups.uuid = :costCenterGroupUUID\n" +
            "and (cost_centers.name ilike concat('%',:name,'%') " +
            "or cost_centers.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedCostCenterAgainstCostCenterGroupWithStatus(UUID costCenterGroupUUID, String name, String description, Boolean status);

    //query for getting count of cost centers for a given voucher
    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countRecordsWithSearchFilter(UUID voucherUUID, String name, String description);

    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and cost_centers.status= :status \n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countRecordsWithSearchFilterWithStatus(UUID voucherUUID, String name, String description, Boolean status);

    //query for getting count of cost centers for a voucher with given companyId
    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.company_uuid = :companyUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCostCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description);

    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.company_uuid = :companyUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and cost_centers.status= :status \n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCostCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status);

    //query for getting count of cost centers for a voucher with given branchUUID
    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid \n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.branch_uuid = :branchUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCostCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description);

    @Query("select count(distinct cost_centers.uuid) from cost_centers\n" +
            "left join cost_center_group_cost_center_pvt\n" +
            "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid \n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_centers.branch_uuid = :branchUUID\n" +
            "and cost_centers.deleted_at is null\n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and cost_centers.status= :status \n" +
            "and (cost_centers.name ILIKE concat('%',:name,'%')\n" +
            "or cost_centers.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCostCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM cost_centers\n" +
            "WHERE cost_centers.uuid NOT IN(\n" +
            "SELECT cost_centers.uuid FROM cost_centers\n" +
            "LEFT JOIN cost_center_group_cost_center_pvt\n" +
            "ON cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid \n" +
            "WHERE cost_center_group_cost_center_pvt.cost_center_group_uuid = :costCenterGroupUUID\n" +
            "AND cost_center_group_cost_center_pvt.deleted_at IS NULL\n" +
            "AND cost_centers.deleted_at IS NULL )\n" +
            "AND cost_centers.deleted_at IS NULL \n" +
            "AND (cost_centers.name ILIKE concat('%',:name,'%') " +
            "OR cost_centers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedCostCentersRecords(UUID costCenterGroupUUID, String name, String description);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM cost_centers\n" +
            "WHERE cost_centers.uuid NOT IN(\n" +
            "SELECT cost_centers.uuid FROM cost_centers\n" +
            "LEFT JOIN cost_center_group_cost_center_pvt\n" +
            "ON cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid \n" +
            "WHERE cost_center_group_cost_center_pvt.cost_center_group_uuid = :costCenterGroupUUID\n" +
            "AND cost_center_group_cost_center_pvt.deleted_at IS NULL\n" +
            "AND cost_centers.deleted_at IS NULL )\n" +
            "AND cost_centers.deleted_at IS NULL \n" +
            "AND cost_centers.status = :status \n" +
            "AND (cost_centers.name ILIKE concat('%',:name,'%') " +
            "OR cost_centers.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedCostCentersRecordsWithStatusFilter(UUID costCenterGroupUUID, String name, String description, Boolean status);
}
