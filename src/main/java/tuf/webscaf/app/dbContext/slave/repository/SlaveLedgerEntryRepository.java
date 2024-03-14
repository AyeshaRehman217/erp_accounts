package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveProfitAndLossStatementDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLedgerEntryEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomFinancialReportingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveLedgerEntryRepository extends ReactiveCrudRepository<SlaveLedgerEntryEntity, Long>, SlaveCustomFinancialReportingRepository {

    Flux<SlaveLedgerEntryEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveLedgerEntryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveLedgerEntryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();


    /**
     * Count All Ledger Entries for the given transaction dates
     **/
    @Query("select count (*) \n" +
            "from \n" +
            "(\n" +
            "select ledger_entries.*,accounts.uuid as accountUUID,accounts.code as accountCode, \n" +
            "accounts.name as accountName,\n" +
            "profit_centers.uuid profitUUID,profit_centers.name as profitCenterName,\n" +
            "cost_centers.uuid as costUUID,cost_centers.name as costCenterName,\n" +
            "transactions.transaction_date as transactionDate\n" +
            "from ledger_entries \n" +
            "LEFT join \n" +
            "transactions \n" +
            "ON transactions.uuid=ledger_entries.transaction_uuid \n" +
            "LEFT  JOIN\n" +
            "accounts on accounts.uuid=ledger_entries.account_uuid\n" +
            "LEFT JOIN \n" +
            "profit_centers \n" +
            "on ledger_entries.profit_center_uuid = profit_centers.uuid\n" +
            "LEFT JOIN \n" +
            "cost_centers \n" +
            "on ledger_entries.cost_center_uuid = cost_centers.uuid\n" +
            "where transactions.transaction_date BETWEEN :startDate AND :endDate \n" +
            "and  ledger_entries.account_uuid = :accountUUID " +
            "and ledger_entries.deleted_at is null \n" +
            "and transactions.deleted_at is null \n" +
            "and cost_centers.deleted_at is null \n" +
            "and profit_centers.deleted_at is null \n" +
            "and accounts.deleted_at is null \n" +
            ") as ledgerEntries")
    Mono<Long> countLedgerEntrySummaryRecords(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * This Function Calculates the Cash brought down for the given account
     * before the given period of time
     **/
    @Query("select \n" +
            "CASE\n" +
            "WHEN sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) IS NOT NULL \n" +
            "THEN sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount)\n" +
            "ELSE 0.0\n" +
            "END as carriedForward\n" +
            "from ledger_entries \n" +
            "LEFT join \n" +
            "transactions \n" +
            "ON transactions.uuid=ledger_entries.transaction_uuid \n" +
            "LEFT  JOIN\n" +
            "accounts on accounts.uuid=ledger_entries.account_uuid\n" +
            "where transactions.transaction_date BETWEEN :startDate AND :endDate \n" +
            "and  ledger_entries.account_uuid = :accountUUID \n" +
            "and ledger_entries.deleted_at is null \n" +
            "and transactions.deleted_at is null \n" +
            "and accounts.deleted_at is null")
    Mono<Double> countCarriedForward(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate);


    /**
     * This Function Calculates the Cash forward down for the given account
     * before the given period of time
     **/
    @Query("select CASE \n" +
            "WHEN sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) IS NOT NULL \n" +
            "THEN sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount)\n" +
            "ELSE 0.0 \n" +
            "END as balanceBroughtForward\n" +
            "from ledger_entries \n" +
            "LEFT join \n" +
            "transactions \n" +
            "ON transactions.uuid=ledger_entries.transaction_uuid \n" +
            "LEFT  JOIN\n" +
            "accounts on accounts.uuid=ledger_entries.account_uuid\n" +
            "where transactions.transaction_date < :startDate \n" +
            "and  ledger_entries.account_uuid = :accountUUID \n" +
            "and ledger_entries.deleted_at is null \n" +
            "and transactions.deleted_at is null \n" +
            "and accounts.deleted_at is null")
    Mono<Double> countBroughtForward(UUID accountUUID, LocalDateTime startDate);

}
