package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowAdjustmentEntity;

import java.util.UUID;

@Repository
public interface SlaveCashFlowAdjustmentRepository extends ReactiveCrudRepository<SlaveCashFlowAdjustmentEntity, Long> {

    Flux<SlaveCashFlowAdjustmentEntity> findAllByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String description);

    Mono<SlaveCashFlowAdjustmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCashFlowAdjustmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCashFlowAdjustmentEntity> findByTransactionIdAndDeletedAtIsNull(Long transactionId);

    Mono<Long> countByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String description);
}
