package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.FlowLineTypeEntity;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryEntity;

@Repository
public interface IncomeSummaryDetailRepository extends ReactiveSortingRepository<IncomeSummaryDetailEntity, Long> {
    Mono<IncomeSummaryDetailEntity> findByIdAndDeletedAtIsNull(Long id);
}
