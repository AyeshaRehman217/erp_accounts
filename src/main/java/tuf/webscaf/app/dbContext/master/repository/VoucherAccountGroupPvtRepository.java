package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupCostCenterPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherAccountGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherAccountGroupPvtRepository extends ReactiveCrudRepository<VoucherAccountGroupPvtEntity, Long> {

    Mono<VoucherAccountGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherAccountGroupPvtEntity> findAllByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Flux<VoucherAccountGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherAccountGroupPvtEntity> findAllByVoucherUUIDAndAccountGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> accountGroupUUID);

    Flux<VoucherAccountGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherAccountGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherAccountGroupPvtEntity> findFirstByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Mono<VoucherAccountGroupPvtEntity> findFirstByVoucherUUIDAndAccountGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID accountGroupUUID);
}
