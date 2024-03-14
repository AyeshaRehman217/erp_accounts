package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.master.entity.CashFlowAdjustmentEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowAdjustmentEntity;

@Repository
public interface CashFlowAdjustmentRepository extends ReactiveCrudRepository<CashFlowAdjustmentEntity, Long> {

    Mono<CashFlowAdjustmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CashFlowAdjustmentEntity> findByTransactionIdAndDeletedAtIsNull(Long transactionId);

}
