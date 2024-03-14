package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLedgerEntryEntity;

import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends ReactiveCrudRepository<LedgerEntryEntity, Long> {
        Mono<LedgerEntryEntity> findByIdAndDeletedAtIsNull(Long id);
        //Check In Transaction Update Function
        Mono<LedgerEntryEntity> findFirstByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

        Flux<LedgerEntryEntity> findAllByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

        Mono<LedgerEntryEntity> findByCostCenterUUIDAndDeletedAtIsNull(UUID id);

        Mono<LedgerEntryEntity> findByProfitCenterUUIDAndDeletedAtIsNull(UUID profitCenterId);

        Mono<LedgerEntryEntity> findFirstByCostCenterUUIDAndDeletedAtIsNull(UUID costCenterId);

        Mono<LedgerEntryEntity> findFirstByProfitCenterUUIDAndDeletedAtIsNull(UUID profitCenterId);
        //Check in Account Handler if Record Exists
        Mono<LedgerEntryEntity> findFirstByAccountUUIDAndDeletedAtIsNull(UUID accountId);

        @Query("SELECT * \n" +
                "FROM ledger_entries AS le \n" +
                "LEFT JOIN accounts AS act ON le.account_uuid= act.uuid \n" +
                "WHERE le.transaction_uuid != act.opening_balance_uuid \n" +
                "AND le.deleted_at IS NULL \n" +
                "AND act.deleted_at IS NULL \n" +
                "AND le.account_uuid = :accountUUID\n" +
                "FETCH FIRST ROW ONLY")
        Mono<LedgerEntryEntity> checkAccountReferenceInLedger(UUID accountUUID);
}
