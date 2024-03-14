package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCostCenterWithCostCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCostCenterGroupPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveCostCenterGroupRepository extends ReactiveCrudRepository<SlaveCostCenterGroupEntity, Long>, SlaveCustomCostCenterWithCostCenterGroupRepository, SlaveCustomVoucherCostCenterGroupPvtRepository {
    Flux<SlaveCostCenterGroupEntity> findAllByDeletedAtIsNullAndNameContaining(String name, Sort sort);

    Flux<SlaveCostCenterGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveCostCenterGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<SlaveCostCenterGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCostCenterGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //query for getting count of cost center groups for a given cost center
    @Query("select count(*) from cost_center_groups\n" +
            "join cost_center_group_cost_center_pvt on cost_center_groups.uuid = cost_center_group_cost_center_pvt.cost_center_group_uuid\n" +
            "join cost_centers on cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid\n" +
            "where cost_centers.deleted_at is null \n" +
            "and cost_center_groups.deleted_at is null \n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and cost_centers.uuid = :costCenterUUID\n" +
            "and cost_center_groups.name ilike concat('%',:name,'%')")
    Mono<Long> countCostCenterGroupAgainstCostCenter(UUID costCenterUUID, String name);

    @Query("select count(*) from cost_center_groups\n" +
            "join cost_center_group_cost_center_pvt on cost_center_groups.uuid = cost_center_group_cost_center_pvt.cost_center_group_uuid\n" +
            "join cost_centers on cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid\n" +
            "where cost_centers.deleted_at is null \n" +
            "and cost_center_groups.deleted_at is null \n" +
            "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
            "and cost_centers.uuid = :costCenterUUID\n" +
            "and cost_center_groups.status = :status\n" +
            "and cost_center_groups.name ilike concat('%',:name,'%')")
    Mono<Long> countCostCenterGroupAgainstCostCenterWithStatus(UUID costCenterUUID, String name, Boolean status);

    //query for getting count of cost center groups for a given voucher
    @Query("select count(*) from cost_center_groups\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_groups.uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_center_groups.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and cost_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedCostCenterGroups(UUID voucherUUID, String name);

    //query for getting count of cost center groups for a given voucher with status
    @Query("select count(*) from cost_center_groups\n" +
            "left join voucher_cost_center_group_pvt\n" +
            "on cost_center_groups.uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
            "where voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and cost_center_groups.status = :status " +
            "and cost_center_groups.deleted_at is null\n" +
            "and voucher_cost_center_group_pvt.deleted_at is null\n" +
            "and cost_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedCostCenterGroupsWithStatus(UUID voucherUUID, String name, Boolean status);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM cost_center_groups\n" +
            "WHERE cost_center_groups.uuid NOT IN(\n" +
            "SELECT cost_center_groups.uuid FROM cost_center_groups\n" +
            "LEFT JOIN voucher_cost_center_group_pvt\n" +
            "ON voucher_cost_center_group_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
            "WHERE voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_cost_center_group_pvt.deleted_at IS NULL\n" +
            "AND cost_center_groups.deleted_at IS NULL) \n" +
            "AND cost_center_groups.deleted_at IS NULL \n" +
            "AND cost_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedCostCenterGroupRecords(UUID voucherUUID, String name);

    @Query("SELECT count(*) FROM cost_center_groups\n" +
            "WHERE cost_center_groups.uuid NOT IN(\n" +
            "SELECT cost_center_groups.uuid FROM cost_center_groups\n" +
            "LEFT JOIN voucher_cost_center_group_pvt\n" +
            "ON voucher_cost_center_group_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
            "WHERE voucher_cost_center_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_cost_center_group_pvt.deleted_at IS NULL\n" +
            "AND cost_center_groups.deleted_at IS NULL) \n" +
            "AND cost_center_groups.deleted_at IS NULL \n" +
            "AND cost_center_groups.status= :status \n" +
            "AND cost_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedCostCenterGroupRecordsWithStatus(UUID voucherUUID, String name, Boolean status);
}
