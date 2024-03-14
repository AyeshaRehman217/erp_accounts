package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaFieldEntity;

@Repository
public interface DocumentNatureMetaFieldRepository extends ReactiveSortingRepository<DocumentNatureMetaFieldEntity, Long> {
    Mono<DocumentNatureMetaFieldEntity> findByIdAndDeletedAtIsNull(Long id);
}
