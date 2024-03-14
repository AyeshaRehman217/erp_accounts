package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherProfitCenterGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherProfitCenterGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherProfitCenterGroupPvtEntity, Long> {

    Mono<SlaveVoucherProfitCenterGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherProfitCenterGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherProfitCenterGroupPvtEntity> findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    @Query("SELECT EXISTS(\n" +
            "SELECT pcgpcpvt.uuid\n" +
            "FROM voucher_profit_center_group_pvt AS vpcgp\n" +
            "LEFT JOIN profit_center_group_profit_center_pvt AS pcgpcpvt " +
            "ON vpcgp.profit_center_group_uuid= pcgpcpvt.profit_center_group_uuid\n" +
            "WHERE vpcgp.voucher_uuid = :voucherUUID\n" +
            "AND pcgpcpvt .all_mapped= true\n" +
            "AND vpcgp.deleted_at IS NULL \n" +
            "AND pcgpcpvt.deleted_at IS NULL )")
    Mono<Boolean> profitCenterGroupAllMappingExists(UUID voucherUUID);

 }
