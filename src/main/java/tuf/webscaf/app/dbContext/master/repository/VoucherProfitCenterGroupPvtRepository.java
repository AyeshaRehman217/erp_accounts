package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherAccountGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherProfitCenterGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherProfitCenterGroupPvtRepository extends ReactiveCrudRepository<VoucherProfitCenterGroupPvtEntity, Long> {

    Mono<VoucherProfitCenterGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherProfitCenterGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherProfitCenterGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherProfitCenterGroupPvtEntity> findAllByVoucherUUIDAndProfitCenterGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> profitCenterGroupUUID);

    Flux<VoucherProfitCenterGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherProfitCenterGroupPvtEntity> findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    Mono<VoucherProfitCenterGroupPvtEntity> findFirstByVoucherUUIDAndProfitCenterGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID profitCenterGroupId);
}
