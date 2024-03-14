package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TransactionEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherAccountGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherCostCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCostCenterGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherCostCenterGroupPvtRepository extends ReactiveCrudRepository<VoucherCostCenterGroupPvtEntity, Long> {

    Mono<VoucherCostCenterGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherCostCenterGroupPvtEntity> findAllByVoucherUUIDAndCostCenterGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> costCenterGroupUUID);

    Flux<VoucherCostCenterGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherCostCenterGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCostCenterGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCostCenterGroupPvtEntity> findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    Mono<VoucherCostCenterGroupPvtEntity> findFirstByVoucherUUIDAndCostCenterGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID costCenterGroupUUID);
}
