package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureDocumentNatureGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureDocumentNatureGroupPvtRepositorySlave extends ReactiveSortingRepository<SlaveDocumentNatureDocumentNatureGroupPvtEntity, Long> {
    Flux<SlaveDocumentNatureDocumentNatureGroupPvtEntity> findAllBy(Pageable pageable);

    Mono<SlaveDocumentNatureDocumentNatureGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
