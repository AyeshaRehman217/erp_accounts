package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureDocumentNatureGroupPvtEntity;

@Repository
public interface DocumentNatureDocumentNatureGroupPvtRepository extends ReactiveSortingRepository<DocumentNatureDocumentNatureGroupPvtEntity, Long> {
}
