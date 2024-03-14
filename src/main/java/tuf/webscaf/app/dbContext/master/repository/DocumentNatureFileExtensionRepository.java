package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CashFlowReportEntity;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureFileExtensionEntity;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryDetailEntity;

@Repository
public interface DocumentNatureFileExtensionRepository extends ReactiveSortingRepository<DocumentNatureFileExtensionEntity, Long> {
    Mono<DocumentNatureFileExtensionEntity> findByIdAndDeletedAtIsNull(Long id);
}
