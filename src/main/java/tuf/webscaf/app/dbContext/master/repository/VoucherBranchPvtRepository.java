package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherBranchPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherCompanyPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherBranchPvtRepository extends ReactiveCrudRepository<VoucherBranchPvtEntity, Long> {
    Flux<VoucherBranchPvtEntity> findAllByVoucherUUIDAndBranchUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> branchUUID);

    Flux<VoucherBranchPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherBranchPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherBranchPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherBranchPvtEntity> findByBranchUUIDInAndDeletedAtIsNull(List<UUID> branchUUID);

    Mono<VoucherBranchPvtEntity> findFirstByVoucherUUIDAndBranchUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID branchUUID);
}
