package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherBranchPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherBranchPvtRepository extends ReactiveCrudRepository<SlaveVoucherBranchPvtEntity, Long> {
    Mono<SlaveVoucherBranchPvtEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Mono<SlaveVoucherBranchPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    @Query("SELECT string_agg(branch_uuid::text, ',') " +
            "as ids FROM voucher_branch_pvt " +
            "WHERE voucher_branch_pvt.deleted_at IS NULL " +
            "AND voucher_branch_pvt.voucher_uuid = :voucherUUID")
    Mono<String> getAllIds(UUID voucherUUID);
}
