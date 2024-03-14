package tuf.webscaf.app.dbContext.master.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupVoucherPvtEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface VoucherGroupVoucherPvtRepository extends ReactiveCrudRepository<VoucherGroupVoucherPvtEntity, Long> {

    Mono<VoucherGroupVoucherPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherGroupVoucherPvtEntity> findAllByVoucherGroupUUIDAndDeletedAtIsNull(UUID voucherGroupUUID);

    Mono<VoucherGroupVoucherPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);
//    Flux<VoucherCostCenterGroupPvtEntity> findAllByVoucherUUIDAndAccountGroupUUIDInAndDeletedAtIsNull(long voucherUUID,List<UUID> accountGroupUUID);

    Flux<VoucherGroupVoucherPvtEntity> findAllByVoucherGroupUUIDAndVoucherUUIDInAndDeletedAtIsNull(UUID voucherGroupUUID, List<UUID> voucherUUID);

    Mono<VoucherGroupVoucherPvtEntity> findFirstByVoucherGroupUUIDAndVoucherUUIDAndDeletedAtIsNull(UUID voucherGroupUUID, UUID voucherUUID);

    Flux<VoucherGroupVoucherPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherGroupVoucherPvtEntity> findByVoucherGroupUUIDAndDeletedAtIsNull(UUID voucherGroupUUID);

    Mono<VoucherGroupVoucherPvtEntity> findFirstByVoucherGroupUUIDAndDeletedAtIsNull(UUID voucherGroupUUID);
}
