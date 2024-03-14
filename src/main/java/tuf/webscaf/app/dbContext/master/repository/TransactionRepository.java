package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.JobEntity;
import tuf.webscaf.app.dbContext.master.entity.JobGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionEntity;

import java.util.UUID;

@Repository
public interface TransactionRepository extends ReactiveSortingRepository<TransactionEntity, Long> {
    Mono<TransactionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<TransactionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<TransactionEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<TransactionEntity> findFirstByTransactionStatusUUIDAndDeletedAtIsNull(UUID transactionStatusUUID);

    Mono<TransactionEntity> findFirstByJobUUIDAndDeletedAtIsNull(UUID jobId);

    Mono<TransactionEntity> findFirstByCalendarPeriodUUIDAndDeletedAtIsNull(UUID calendarPeriodUUID);
}
