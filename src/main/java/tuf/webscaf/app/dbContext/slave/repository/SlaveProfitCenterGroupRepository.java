package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterWithProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherProfitCenterGroupPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveProfitCenterGroupRepository extends ReactiveCrudRepository<SlaveProfitCenterGroupEntity, Long>, SlaveCustomProfitCenterWithProfitCenterGroupRepository, SlaveCustomVoucherProfitCenterGroupPvtRepository {

    Flux<SlaveProfitCenterGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveProfitCenterGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<SlaveProfitCenterGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveProfitCenterGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    //Check If Name Already Exists
    Mono<SlaveProfitCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    //Check If Name Already Exists in Update Function
    Mono<SlaveProfitCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<Long> countByDeletedAtIsNullAndNameContaining(String name);

    //query used in pvt mapping handler profit Center groups with vouchers
    @Query("SELECT count(*) FROM profit_center_groups\n" +
            "WHERE profit_center_groups.uuid NOT IN(\n" +
            "SELECT profit_center_groups.uuid FROM profit_center_groups\n" +
            "LEFT JOIN voucher_profit_center_group_pvt\n" +
            "ON voucher_profit_center_group_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
            "WHERE voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_profit_center_group_pvt.deleted_at IS NULL\n" +
            "AND profit_center_groups.deleted_at IS NULL) \n" +
            "AND profit_center_groups.deleted_at IS NULL \n" +
            "AND profit_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedProfitCenterGroupAgainstVoucher(UUID voucherUUID, String name);

    @Query("SELECT count(*) FROM profit_center_groups\n" +
            "WHERE profit_center_groups.uuid NOT IN(\n" +
            "SELECT profit_center_groups.uuid FROM profit_center_groups\n" +
            "LEFT JOIN voucher_profit_center_group_pvt\n" +
            "ON voucher_profit_center_group_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
            "WHERE voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_profit_center_group_pvt.deleted_at IS NULL\n" +
            "AND profit_center_groups.deleted_at IS NULL) \n" +
            "AND profit_center_groups.deleted_at IS NULL \n" +
            "AND profit_center_groups.status= :status \n" +
            "AND profit_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedProfitCenterGroupAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status);

    //query for getting count of profit Center Groups for a given profit center
    @Query("select count(*) from profit_center_groups\n" +
            "join profit_center_group_profit_center_pvt " +
            "on profit_center_groups.uuid = profit_center_group_profit_center_pvt.profit_center_group_uuid\n" +
            "join profit_centers on profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid\n" +
            "where profit_centers.deleted_at is null \n" +
            "and profit_center_groups.deleted_at is null \n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null \n" +
            "and profit_centers.uuid = :profitCenterUUID\n" +
            "and ( profit_center_groups.name ilike concat('%',:name,'%')" +
            " or profit_center_groups.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedProfitCenterGroups(UUID profitCenterUUID, String name, String description);

    @Query("select count(*) from profit_center_groups\n" +
            "join profit_center_group_profit_center_pvt " +
            "on profit_center_groups.uuid = profit_center_group_profit_center_pvt.profit_center_group_uuid\n" +
            "join profit_centers on profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid\n" +
            "where profit_centers.deleted_at is null \n" +
            "and profit_center_groups.deleted_at is null \n" +
            "and profit_center_group_profit_center_pvt.deleted_at is null \n" +
            "and profit_centers.uuid = :profitCenterUUID\n" +
            "and profit_center_groups.status= :status \n" +
            "and ( profit_center_groups.name ilike concat('%',:name,'%')" +
            " or profit_center_groups.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedProfitCenterGroupsWithStatus(UUID profitCenterUUID, String name, String description, Boolean status);

    //query for getting count of profit center groups for a given voucher
    @Query("select count(*) from profit_center_groups\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_groups.uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_center_groups.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and profit_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedProfitCenterGroups(UUID voucherUUID, String name);

    //query for getting count of profit center groups for a given voucher with status
    @Query("select count(*) from profit_center_groups\n" +
            "left join voucher_profit_center_group_pvt\n" +
            "on profit_center_groups.uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
            "where voucher_profit_center_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and profit_center_groups.status = :status " +
            "and profit_center_groups.deleted_at is null\n" +
            "and voucher_profit_center_group_pvt.deleted_at is null\n" +
            "and profit_center_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedProfitCenterGroupsWithStatus(UUID voucherUUID, String name, Boolean status);
}
