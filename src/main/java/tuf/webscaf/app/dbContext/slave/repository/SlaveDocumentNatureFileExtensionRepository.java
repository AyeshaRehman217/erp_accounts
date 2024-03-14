package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowReportEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureFileExtensionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureFileExtensionRepository extends ReactiveSortingRepository<SlaveDocumentNatureFileExtensionEntity, Long> {
    Mono<SlaveDocumentNatureFileExtensionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveDocumentNatureFileExtensionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveDocumentNatureFileExtensionEntity> findAllByExtensionContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String extension, String description);

    Mono<Long> countByExtensionContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String extension, String description);
}
