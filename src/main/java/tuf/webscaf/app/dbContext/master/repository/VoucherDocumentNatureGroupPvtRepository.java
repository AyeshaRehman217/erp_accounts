package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherCostCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherDocumentNatureGroupPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherJobGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherDocumentNatureGroupPvtRepository extends ReactiveCrudRepository<VoucherDocumentNatureGroupPvtEntity, Long> {

    Mono<VoucherDocumentNatureGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherDocumentNatureGroupPvtEntity> findAllByVoucherUUIDAndDocumentNatureGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> docNatureGroupUUID);

    Flux<VoucherDocumentNatureGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherDocumentNatureGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherDocumentNatureGroupPvtEntity> findFirstByDocumentNatureGroupUUIDAndDeletedAtIsNull(UUID documentNatureGroupUUID);
}
