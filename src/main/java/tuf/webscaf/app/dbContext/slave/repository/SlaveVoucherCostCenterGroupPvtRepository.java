package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherProfitCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherAccountGroupPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCostCenterGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveVoucherCostCenterGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherCostCenterGroupPvtEntity, Long> {

    Mono<SlaveVoucherCostCenterGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherCostCenterGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherCostCenterGroupPvtEntity> findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    @Query("SELECT EXISTS(\n" +
            "SELECT ccgccpvt.uuid\n" +
            "FROM voucher_cost_center_group_pvt AS vccgp\n" +
            "LEFT JOIN cost_center_group_cost_center_pvt AS ccgccpvt " +
            "ON vccgp.cost_center_group_uuid= ccgccpvt.cost_center_group_uuid\n" +
            "WHERE vccgp.voucher_uuid = :voucherUUID\n" +
            "AND ccgccpvt .all_mapped= true\n" +
            "AND vccgp.deleted_at IS NULL \n" +
            "AND ccgccpvt.deleted_at IS NULL )")
    Mono<Boolean> costCenterGroupAllMappingExists(UUID voucherUUID);
 }
