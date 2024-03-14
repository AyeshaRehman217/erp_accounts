package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.*;

import java.time.LocalDateTime;
import java.util.UUID;

//This Custom Repository extends in Slave Ledger Entry Repository
public interface SlaveCustomFinancialReportingRepository {
    //This Function is used to Fetch Ledgers Based on Account UUID and Transaction Start date and End Date
    Flux<SlaveReportingLedgerEntriesDto> showFinancialLedgerReporting(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate);

    Flux<SlaveLedgerReportDto> showLedgerReport(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate, Integer size, Long page);

    //This Function is used to Fetch parent of all the child Accounts
    Flux<SlaveChartOfAccountDto> chartOfAccounts();

    //fetch All parents where parent Account is null
    Flux<SlaveChartOfAccountDto> chartOfAccountsWithPagination(Integer size, Long page);

    //fetch All child based on account uuid
    Flux<SlaveChartOfAccountDto> fetchChildBasedOnParentAccount(UUID uuid);

    //fetch All parent based on child Parent Account uuid
    Flux<SlaveChartOfAccountDto> fetchParentBasedOnChildParent(UUID parentAccountUUID);

    //Trial Balance based on Account UUID, Transactions Start Date and End Date
    Flux<SlaveTrialBalanceDto> trialBalance(LocalDateTime startDate, LocalDateTime endDate, String dp, String d, Integer size, Long page);

    Flux<SlaveTrialBalanceReportDto> trialBalanceReport(LocalDateTime startDate, LocalDateTime endDate, String dp, String d, Integer size, Long page);

    //Profit and Loss Statement Reporting
    Flux<SlaveProfitAndLossStatementDto> profitAndLossStatement(String accountUUID, LocalDateTime startDate);
}
