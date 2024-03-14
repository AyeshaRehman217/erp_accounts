package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherDocumentNatureGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherDocumentNatureGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherDocumentNatureGroupPvtEntity, Long> {

    Mono<SlaveVoucherDocumentNatureGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherDocumentNatureGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherDocumentNatureGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);
 }
