package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaFieldEntity;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureMetaFieldRepository extends ReactiveSortingRepository<SlaveDocumentNatureMetaFieldEntity, Long> {
    Mono<SlaveDocumentNatureMetaFieldEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveDocumentNatureMetaFieldEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveDocumentNatureMetaFieldEntity> findAllByKeyContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String key);

    Mono<Long> countByKeyContainingIgnoreCaseAndDeletedAtIsNull(String key);
}
