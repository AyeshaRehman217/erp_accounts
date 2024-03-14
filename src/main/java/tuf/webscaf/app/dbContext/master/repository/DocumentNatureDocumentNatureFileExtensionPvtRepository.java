package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureDocumentNatureFileExtensionPvtEntity;

@Repository
public interface DocumentNatureDocumentNatureFileExtensionPvtRepository extends ReactiveSortingRepository<DocumentNatureDocumentNatureFileExtensionPvtEntity, Long> {

}
