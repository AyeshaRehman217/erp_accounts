package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.FlowLineTypeEntity;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;

@Repository
public interface IncomeSummaryRepository extends ReactiveCrudRepository<IncomeSummaryEntity, Long> {
    Mono<IncomeSummaryEntity> findByIdAndDeletedAtIsNull(Long id);
}
