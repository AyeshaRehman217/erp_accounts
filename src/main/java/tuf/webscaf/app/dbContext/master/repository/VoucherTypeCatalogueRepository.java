package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherTypeCatalogueEntity;

import java.util.UUID;

@Repository
public interface VoucherTypeCatalogueRepository extends ReactiveCrudRepository<VoucherTypeCatalogueEntity, Long> {

    Mono<VoucherTypeCatalogueEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<VoucherTypeCatalogueEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<VoucherTypeCatalogueEntity> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<VoucherTypeCatalogueEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<VoucherTypeCatalogueEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<VoucherTypeCatalogueEntity> findFirstBySlugAndDeletedAtIsNull(String slug);

    Mono<VoucherTypeCatalogueEntity> findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(String slug, UUID uuid);


}
