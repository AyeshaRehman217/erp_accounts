package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CashFlowLineEntity;
import tuf.webscaf.app.dbContext.master.entity.CashFlowReportEntity;

@Repository
public interface CashFlowReportRepository extends ReactiveCrudRepository<CashFlowReportEntity, Long> {
    Mono<CashFlowReportEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CashFlowReportEntity> findFirstBySlugAndDeletedAtIsNull(String slug);
}
