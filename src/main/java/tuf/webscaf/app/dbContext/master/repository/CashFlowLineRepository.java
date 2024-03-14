package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.CashFlowLineEntity;

@Repository
public interface CashFlowLineRepository extends ReactiveCrudRepository<CashFlowLineEntity, Long> {
    Mono<CashFlowLineEntity> findByIdAndDeletedAtIsNull(Long id);
}
