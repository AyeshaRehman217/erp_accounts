package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureDocumentNatureFileExtensionPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureDocumentNatureFileExtensionPvtRepository extends ReactiveSortingRepository<SlaveDocumentNatureDocumentNatureFileExtensionPvtEntity, Long> {
    Flux<SlaveDocumentNatureDocumentNatureFileExtensionPvtEntity> findAllBy(Pageable pageable);

    Mono<SlaveDocumentNatureDocumentNatureFileExtensionPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
