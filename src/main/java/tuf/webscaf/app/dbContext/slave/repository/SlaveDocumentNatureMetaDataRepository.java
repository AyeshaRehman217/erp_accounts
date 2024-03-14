package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaDataEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaDataEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaFieldEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveFlowLineTypeEntity;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureMetaDataRepository extends ReactiveCrudRepository<SlaveDocumentNatureMetaDataEntity, Long> {
    Mono<SlaveDocumentNatureMetaDataEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveDocumentNatureMetaDataEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveDocumentNatureMetaDataEntity> findAllByKeyContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String key);

    Mono<Long> countByKeyContainingIgnoreCaseAndDeletedAtIsNull(String key);
}
