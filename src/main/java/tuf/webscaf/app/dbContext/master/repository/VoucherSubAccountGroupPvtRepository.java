package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherSubAccountGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherSubAccountGroupPvtRepository extends ReactiveCrudRepository<VoucherSubAccountGroupPvtEntity, Long> {

    Flux<VoucherSubAccountGroupPvtEntity> findAllBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    Flux<VoucherSubAccountGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherSubAccountGroupPvtEntity> findAllByVoucherUUIDAndSubAccountGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> subAccountGroupUUID);

    Flux<VoucherSubAccountGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherSubAccountGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherSubAccountGroupPvtEntity> findFirstBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    Mono<VoucherSubAccountGroupPvtEntity> findFirstByVoucherUUIDAndSubAccountGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID subAccountGroupUUID);
}
