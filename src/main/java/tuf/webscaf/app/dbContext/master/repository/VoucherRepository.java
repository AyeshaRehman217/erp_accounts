package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherRepository extends ReactiveSortingRepository<VoucherEntity, Long> {
    Mono<VoucherEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<VoucherEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<VoucherEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<VoucherEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    Flux<VoucherEntity> findAllByDeletedAtIsNull();

    Mono<VoucherEntity> findFirstByVoucherTypeCatalogueUUIDAndDeletedAtIsNull(UUID voucherTypeCatalogueUUID);

    Mono<VoucherEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<VoucherEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
