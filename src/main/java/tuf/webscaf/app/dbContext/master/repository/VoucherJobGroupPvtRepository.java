package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherJobGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherJobGroupPvtRepository extends ReactiveCrudRepository<VoucherJobGroupPvtEntity, Long> {

    Mono<VoucherJobGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<VoucherJobGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherJobGroupPvtEntity> findAllByVoucherUUIDAndJobGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> costCenterGroupUUID);

    Flux<VoucherJobGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherJobGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherJobGroupPvtEntity> findFirstByJobGroupUUIDAndDeletedAtIsNull(UUID jobGroupUUID);

    Mono<VoucherJobGroupPvtEntity> findFirstByVoucherUUIDAndJobGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID jobGroupUUID);
}
