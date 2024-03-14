package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaDataEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;

@Repository
public interface DocumentNatureMetaDataRepository extends ReactiveCrudRepository<DocumentNatureMetaDataEntity, Long> {
    Mono<DocumentNatureMetaDataEntity> findByIdAndDeletedAtIsNull(Long id);
}
