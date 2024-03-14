package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomFinancialReportingRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class SlaveCustomFinancialReportingRepositoryImpl implements SlaveCustomFinancialReportingRepository {

    private DatabaseClient client;
    private SlaveReportingLedgerEntriesDto slaveLedgerEntryDto;
    private SlaveChartOfAccountDto listOfCharts;
    private SlaveTrialBalanceDto trialBalanceDto;
    private SlaveProfitAndLossStatementDto profitLossDto;
    private SlaveLedgerReportDto ledgerReportDto;
    private SlaveTrialBalanceReportDto trialBalanceReportDto;

    @Autowired
    public SlaveCustomFinancialReportingRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveReportingLedgerEntriesDto> showFinancialLedgerReporting(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate) {
        String query = "select ledger_entries.*,accounts.uuid as accountUUID,accounts.code as accountCode, \n" +
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
                "where transactions.transaction_date BETWEEN '" + startDate + "' AND '" + endDate + "' \n" +
                "and  ledger_entries.account_uuid = '" + accountUUID +
                "'and ledger_entries.deleted_at is null \n" +
                "and transactions.deleted_at is null \n" +
                "and cost_centers.deleted_at is null \n" +
                "and profit_centers.deleted_at is null \n" +
                "and accounts.deleted_at is null " +
                "order by transactions.transaction_date";

        SlaveCustomLedgerEntryReportingMapper mapper = new SlaveCustomLedgerEntryReportingMapper();

        Flux<SlaveReportingLedgerEntriesDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveLedgerEntryDto))
                .all();

        return result;
    }

    // This function is being used for generating ledger reports
    @Override
    public Flux<SlaveLedgerReportDto> showLedgerReport(UUID accountUUID, LocalDateTime startDate, LocalDateTime endDate, Integer size, Long page) {
        String query = "WITH ledger_report_cte AS (\n" +
                "    SELECT\n" +
                "        ROW_NUMBER() OVER (ORDER BY transaction_date) AS row,\n" +
                "        transaction.*,\n" +
                "        CASE\n" +
                "            WHEN slug IN ('liability', 'equity', 'income') THEN SUM(\"cr_amount\" - \"dr_amount\") OVER (ORDER BY transaction_date, created_at)\n" +
                "            WHEN slug IN ('asset', 'expense') THEN SUM(\"dr_amount\" - \"cr_amount\") OVER (ORDER BY transaction_date, created_at)\n" +
                "            ELSE 0.0\n" +
                "        END AS netBalance\n" +
                "    FROM (\n" +
                "        SELECT\n" +
                "            trst.transaction_date,\n" +
                "            le.transaction_uuid,\n" +
                "            le.uuid AS ledgerUUID,\n" +
                "            le.description,\n" +
                "            le.dr_amount,\n" +
                "            le.cr_amount,\n" +
                "            acc.account_type_uuid,\n" +
                "            acc.name,\n" +
                "            act.name AS accountTypeName,\n" +
                "            act.slug,\n" +
                "            cstc.name AS costCenter,\n" +
                "            pftc.name AS profitCenter,\n" +
                "            jobs.name AS job,\n" +
                "            trst.created_at\n" +
                "        FROM transactions AS trst\n" +
                "        LEFT JOIN ledger_entries AS le ON le.transaction_uuid = trst.uuid\n" +
                "        LEFT JOIN accounts AS acc ON le.account_uuid = acc.uuid\n" +
                "        LEFT JOIN account_types AS act ON acc.account_type_uuid = act.uuid\n" +
                "        LEFT JOIN cost_centers AS cstc ON le.cost_center_uuid = cstc.uuid\n" +
                "        LEFT JOIN profit_centers AS pftc ON le.profit_center_uuid = pftc.uuid\n" +
                "        LEFT JOIN public.jobs ON trst.job_uuid = jobs.uuid\n" +
                "        WHERE\n" +
                "            trst.deleted_at IS NULL\n" +
                "            AND le.deleted_at IS NULL\n" +
                "            AND acc.deleted_at IS NULL\n" +
                "            AND act.deleted_at IS NULL\n" +
                "            AND cstc.deleted_at IS NULL\n" +
                "            AND pftc.deleted_at IS NULL\n" +
                "            AND jobs.deleted_at IS NULL\n" +
                "            AND (\n" +
                "                acc.uuid = '" + accountUUID + "'\n" +
                "                OR le.account_uuid = '" + accountUUID + "'\n" +
                "            )\n" +
                "    ) AS transaction\n" +
                "),\n" +
                "balance_brought_forward AS (\n" +
                "SELECT \n" +
                "    '' as rowId,\n" +
                "    '' as transactionUUID, \n" +
                "    TIMESTAMP '" + startDate + "' AS transaction_date, \n" +
                "    '' AS account, \n" +
                "    0 AS ledgerAmount,\n" +
                "    '' AS ledgerDescription, \n" +
                "    'balance B/F' AS description, \n" +
                "    0 AS dr_amount, \n" +
                "    0 AS cr_amount, \n" +
                "    '' AS costCenter, \n" +
                "    '' AS profitCenter, \n" +
                "    '' AS job, \n" +
                "    netBalance \n" +
                "FROM \n" +
                "    ledger_report_cte AS ledger\n" +
                "WHERE \n" +
                "    ledger.transaction_date < '" + startDate + "'\n" +
                "ORDER BY row DESC\n" +
                "LIMIT 1\n" +
                "),\n" +
                "transaction_data AS(\n" +
                "SELECT \n" +
                "    row :: text AS rowId,\n" +
                "    ledger.transaction_uuid :: text AS transactionUUID, \n" +
                "    transaction_date, \n" +
                "    COALESCE(le.accountName, '') AS account, \n" +
                "    CASE \n" +
                "         WHEN ledger.dr_amount !=0\n" +
                "             THEN\n" +
                "                 CASE\n" +
                "                 WHEN le.dr_amount !=0 THEN le.dr_amount\n" +
                "                 ELSE le.cr_amount * -1\n" +
                "                 END\n" +
                "         ELSE\n" +
                "                 CASE\n" +
                "                 WHEN le.dr_amount !=0 THEN le.dr_amount * -1\n" +
                "                 ELSE le.cr_amount\n" +
                "         END\n" +
                "    END as ledgerAmount,\n" +
                "    le.description as ledgerDescription,\n" +
                "    ledger.description, \n" +
                "    ledger.dr_amount, \n" +
                "    ledger.cr_amount, \n" +
                "    costCenter, \n" +
                "    profitCenter, \n" +
                "    job, \n" +
                "    netBalance \n" +
                "FROM \n" +
                "    (SELECT *\n" +
                "    FROM \n" +
                "        ledger_report_cte\n" +
                "    WHERE \n" +
                "        transaction_date <= '" + endDate + "') \n" +
                "AS ledger \n" +
                "LEFT JOIN \n" +
                "    (SELECT tle.*, acc.name as accountName \n" +
                "    FROM ledger_entries as tle\n" +
                "    LEFT JOIN accounts AS acc \n" +
                "    ON tle.account_uuid = acc.uuid\n" +
                "    WHERE tle.deleted_at IS NULL\n" +
                "    AND acc.deleted_at IS NULL) AS le \n" +
                "    ON ledger.transaction_uuid = le.transaction_uuid\n" +
                "    AND le.uuid != ledger.ledgerUUID\n" +
                "WHERE \n" +
                "    ledger.transaction_date >= '" + startDate + "' \n" +
                "    AND ledger.transaction_date <= '" + endDate + "' \n" +
                "     AND le.deleted_at IS NULL \n" +
                "ORDER BY row\n" +
                "),\n" +
                "balance_carried_forward AS(\n" +
                "    SELECT \n" +
                "    '' as rowId,\n" +
                "    '' as transactionUUID, \n" +
                "    TIMESTAMP '" + endDate + "' AS transaction_date, \n" +
                "    '' AS account, \n" +
                "    0 AS ledgerAmount,\n" +
                "    '' AS ledgerDescription, \n" +
                "    'balance C/F' AS description, \n" +
                "    0 AS dr_amount, \n" +
                "    0 AS cr_amount, \n" +
                "    '' AS costCenter, \n" +
                "    '' AS profitCenter, \n" +
                "    '' AS job, \n" +
                "    netBalance \n" +
                "FROM \n" +
                "    ledger_report_cte AS ledger\n" +
                "WHERE \n" +
                "    ledger.transaction_date <= '" + endDate + "'\n" +
                "ORDER BY row DESC\n" +
                "LIMIT 1\n" +
                ")\n" +
                "SELECT * FROM (\n" +
                "    SELECT * FROM balance_brought_forward    \n" +
                "    UNION ALL\n" +
                "    SELECT * FROM transaction_data\n" +
                "    UNION ALL\n" +
                "    SELECT * FROM balance_carried_forward\n" +
                ") AS ledgreport \n" +
                "LIMIT " + size + " OFFSET "+ page;


        SlaveCustomLedgerReportDtoMapper mapper = new SlaveCustomLedgerReportDtoMapper();

        Flux<SlaveLedgerReportDto> result = client.sql(query)
                .map(row -> mapper.apply(row, ledgerReportDto))
                .all();

        return result;
    }

//    @Override
//    public Flux<SlaveChartOfAccountDto> fetchingParentOfAllChild() {
//        String query = "WITH RECURSIVE uplines AS (\n" +
//                "  SELECT tree.uuid, tree.name, tree.control_code,tree.parent_account_uuid, tree.level, " +
//                "  tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed \n" +
//                "  FROM accounts AS tree\n" +
//                "  JOIN account_types ON account_types.uuid = tree.account_type_uuid\n" +
//                "  WHERE tree.deleted_at is null\n" +
//                "  UNION ALL \n" +
//                "  SELECT tree.uuid, tree.name, tree.control_code,tree.parent_account_uuid, a.level + 1, " +
//                "  tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed \n" +
//                "  FROM accounts AS tree JOIN uplines AS a ON tree.uuid = a.parent_account_uuid\n" +
//                "  JOIN account_types ON account_types.uuid = tree.account_type_uuid\n" +
//                "  WHERE \n" +
//                " tree.deleted_at is null\n" +
//                " and a.level < tree.level\n" +
//                ") \n" +
//                "SELECT distinct *\n" +
//                "FROM uplines";
//
//        SlaveCustomChartOfAccountMapper mapper = new SlaveCustomChartOfAccountMapper();
//
//        Flux<SlaveChartOfAccountDto> result = client.sql(query)
//                .map(row -> mapper.apply(row, listOfCharts))
//                .all();
//
//        return result;
//    }


    // This function is being used for generating Chart of Accounts
    @Override
    public Flux<SlaveChartOfAccountDto> chartOfAccounts() {
        String query = "WITH RECURSIVE rec AS\n" +
                "(\n" +
                "  SELECT tree.uuid, tree.name, tree.control_code,tree.parent_account_uuid, tree.level, " +
                "  tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed,\n" +
                "    ROW_NUMBER() \n" +
                " OVER (PARTITION BY tree.parent_account_uuid ORDER BY tree.control_code):: text as path\n" +
                "    from accounts as tree\n" +
                " join account_types on account_types.uuid = tree.account_type_uuid " +
                " where tree.parent_account_uuid is null  \n" +
                " and tree.deleted_at is null \n" +
                " and account_types.deleted_at is null\n" +
                "\n" +
                "  UNION ALL\n" +
                "    \n" +
                "  SELECT accounts.uuid, accounts.name, accounts.control_code,accounts.parent_account_uuid, accounts.level,\n" +
                "  accounts.account_type_uuid, account_types.name as account_type_name, accounts.is_entry_allowed,\n" +
                "    concat(\n" +
                " rec.path, '-', \n" +
                " ROW_NUMBER() OVER (\n" +
                " PARTITION BY accounts.parent_account_uuid ORDER BY accounts.name):: text\n" +
                ") as path\n" +
                "    from rec, \n" +
                " accounts \n" +
                " join account_types on account_types.uuid = accounts.account_type_uuid " +
                " where accounts.parent_account_uuid = rec.uuid \n" +
                " and accounts.deleted_at is null\n " +
                " and account_types.deleted_at is null" +
                " ) " +
                "SELECT distinct *\n" +
                "FROM rec " +
                "ORDER BY path";

        SlaveCustomChartOfAccountMapper mapper = new SlaveCustomChartOfAccountMapper();

        Flux<SlaveChartOfAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, listOfCharts))
                .all();

        return result;
    }

    // This function is being used for generating Chart of Accounts with pagination
    @Override
    public Flux<SlaveChartOfAccountDto> chartOfAccountsWithPagination(Integer size, Long page) {
        String query = "WITH RECURSIVE rec AS\n" +
                "(\n" +
                "  SELECT tree.uuid, tree.name, tree.control_code,tree.parent_account_uuid, tree.level, " +
                "  tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed,\n" +
                "    ROW_NUMBER() \n" +
                " OVER (PARTITION BY tree.parent_account_uuid ORDER BY tree.control_code):: text as path\n" +
                "    from accounts as tree\n" +
                " join account_types on account_types.uuid = tree.account_type_uuid " +
                " where tree.parent_account_uuid is null  \n" +
                " and tree.deleted_at is null \n " +
                " and account_types.deleted_at is null\n" +
                "\n" +
                "  UNION ALL\n" +
                "    \n" +
                "  SELECT accounts.uuid, accounts.name, accounts.control_code,accounts.parent_account_uuid, accounts.level,\n" +
                "  accounts.account_type_uuid, account_types.name as account_type_name, accounts.is_entry_allowed,\n" +
                "    concat(\n" +
                " rec.path, '-', \n" +
                " ROW_NUMBER() OVER (\n" +
                " PARTITION BY accounts.parent_account_uuid ORDER BY accounts.name):: text\n" +
                " ) as path\n" +
                "    from rec, \n" +
                " accounts \n" +
                " join account_types on account_types.uuid = accounts.account_type_uuid " +
                " where accounts.parent_account_uuid = rec.uuid \n" +
                " and accounts.deleted_at is null\n " +
                " and account_types.deleted_at is null\n" +
                " ) " +
                "SELECT distinct *\n" +
                "FROM rec " +
                "order by path limit " + size + " offset " + page;

        SlaveCustomChartOfAccountMapper mapper = new SlaveCustomChartOfAccountMapper();

        Flux<SlaveChartOfAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, listOfCharts))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveChartOfAccountDto> fetchChildBasedOnParentAccount(UUID uuid) {
        String query = "WITH RECURSIVE rec AS\n" +
                "( \n" +
                "  SELECT tree.uuid, tree.name, tree.parent_account_uuid,tree.control_code,tree.level, \n" +
                "    tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed \n" +
                "     from accounts as tree\n" +
                "     join account_types on account_types.uuid = tree.account_type_uuid " +
                "  where tree.uuid = '" + uuid +
                "' and tree.deleted_at is null\n " +
                "  and account_types.deleted_at is null\n" +
                "  UNION ALL \n" +
                "  SELECT tree.uuid, tree.name, tree.parent_account_uuid,tree.control_code,tree.level,\n" +
                "    tree.account_type_uuid, account_types.name as account_type_name, tree.is_entry_allowed \n" +
                "     from rec, \n" +
                "     accounts as tree " +
                "   join account_types on account_types.uuid = tree.account_type_uuid " +
                "   where tree.parent_account_uuid = rec.uuid\n" +
                "   and tree.deleted_at is null\n " +
                "   and account_types.deleted_at is null\n" +
                ") \n" +
                "SELECT * \n" +
                "FROM rec ";

        SlaveCustomChartOfAccountMapper mapper = new SlaveCustomChartOfAccountMapper();

        Flux<SlaveChartOfAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, listOfCharts))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveChartOfAccountDto> fetchParentBasedOnChildParent(UUID parentAccountUUID) {
        String query = "WITH RECURSIVE childs AS \n" +
                "(\n" +
                "  SELECT  accounts.uuid, accounts.name, accounts.parent_account_uuid,accounts.control_code,accounts.level, \n" +
                "    accounts.account_type_uuid, account_types.name as account_type_name, accounts.is_entry_allowed \n" +
                "    from accounts " +
                "    join account_types on account_types.uuid = accounts.account_type_uuid\n" +
                "    where accounts.uuid= '" + parentAccountUUID +
                "'    and accounts.deleted_at is null\n " +
                "     and account_types.deleted_at is null\n" +
                "    \n" +
                "     UNION ALL\n" +
                "    \n" +
                "   SELECT accounts.uuid, accounts.name, accounts.parent_account_uuid,accounts.control_code,accounts.level, \n" +
                "    accounts.account_type_uuid, account_types.name as account_type_name, accounts.is_entry_allowed \n" +
                "    from childs, accounts\n" +
                "     join account_types on account_types.uuid = accounts.account_type_uuid\n" +
                "   where accounts.uuid = childs.parent_account_uuid\n" +
                "   and accounts.deleted_at is null \n " +
                "   and account_types.deleted_at is null\n" +
                ") \n" +
                " SELECT  *  FROM childs";

        SlaveCustomChartOfAccountMapper mapper = new SlaveCustomChartOfAccountMapper();

        Flux<SlaveChartOfAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, listOfCharts))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTrialBalanceDto> trialBalance(LocalDateTime startDate, LocalDateTime endDate, String dp, String d, Integer size, Long page) {
        String query = "select accounts.code as accountCode,accounts.name as accountName,accounts.uuid as accountUUID,\n" +
                "accounts.account_type_uuid as accountTypeUUID,account_types.name as accountTypeName," +
                "transactions.transaction_date as transactionDate,\n" +
                "CASE \n" +
                "WHEN transactions.transaction_date \n" +
                "BETWEEN '" + startDate + "' AND '" + endDate + "' \n" +
                "THEN \n" +
                "sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) \n" +
                "ELSE 0.0\n" +
                "END as netBalance,\n" +
                "CASE \n" +
                "WHEN transactions.transaction_date > '" + startDate + "' \n" +
                "THEN \n" +
                "sum(ledger_entries.dr_amount)-sum(ledger_entries.cr_amount) \n" +
                "ELSE 0.0 \n" +
                "END as balanceBroughtForward,\n" +
                "CASE \n" +
                "WHEN sum(ledger_entries.dr_amount) is not null \n" +
                "THEN sum(ledger_entries.dr_amount)\n" +
                "ELSE \n" +
                "0.0 \n" +
                "END as debitAmount,\n" +
                "CASE \n" +
                "WHEN sum(ledger_entries.cr_amount) is not null \n" +
                "THEN sum(ledger_entries.cr_amount)\n" +
                "ELSE \n" +
                "0.0 \n" +
                "END as creditAmount\n" +
                "from accounts" +
                " left join account_types " +
                "on accounts.account_type_uuid=account_types.uuid \n" +
                "left join ledger_entries \n" +
                "on accounts.uuid=ledger_entries.account_uuid\n" +
                "left join transactions\n" +
                "on transactions.uuid=ledger_entries.transaction_uuid\n" +
                "where ledger_entries.deleted_at is null\n" +
                "and transactions.deleted_at is null\n" +
                "and accounts.deleted_at is null" +
                " and account_types.deleted_at is null\n" +
                "GROUP BY (accountUUID,accountCode,accountName,accountTypeUUID,transactionDate,accountTypeName)" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomTrialBalanceMapper mapper = new SlaveCustomTrialBalanceMapper();

        Flux<SlaveTrialBalanceDto> result = client.sql(query)
                .map(row -> mapper.apply(row, trialBalanceDto))
                .all();

        return result;
    }


    // This function is being used for generating trial balance report
    @Override
    public Flux<SlaveTrialBalanceReportDto> trialBalanceReport(LocalDateTime startDate, LocalDateTime endDate, String dp, String d, Integer size, Long page) {

        String query = "WITH recursive rec AS (\n" +
                "\n" +
                "  SELECT t.uuid, t.name, t.control_code, t.parent_account_uuid, t.level, t.account_type_uuid, at.slug AS account_type_slug, t.is_entry_allowed,\n" +
                "         ROW_NUMBER() OVER (PARTITION BY t.parent_account_uuid ORDER BY t.control_code)::text AS path\n" +
                "  FROM accounts AS t\n" +
                "  JOIN account_types AS at ON at.uuid = t.account_type_uuid \n" +
                "  WHERE t.parent_account_uuid IS NULL " +
                "  AND t.deleted_at IS NULL " +
                "  AND at.deleted_at IS NULL\n" +
                "  \n" +
                "  UNION ALL\n" +
                "  \n" +
                "  SELECT a.uuid, a.name, a.control_code, a.parent_account_uuid, a.level, a.account_type_uuid, at.slug AS account_type_slug, a.is_entry_allowed,\n" +
                "         concat(rec.path, '-', ROW_NUMBER() OVER (PARTITION BY a.parent_account_uuid ORDER BY a."+ dp + " " + d + ")::text) AS path\n" +
                "  FROM rec \n" +
                "  JOIN accounts AS a ON a.parent_account_uuid = rec.uuid\n " +
                "  JOIN account_types AS at ON at.uuid = a.account_type_uuid\n" +
                "  WHERE a.deleted_at IS NULL\n " +
                "  AND at.deleted_at IS NULL\n" +
                ")\n" +
                "\n" +
                "SELECT rec.uuid, \n" +
                "rec.parent_account_uuid, \n" +
                "rec.name, \n" +
                "rec.control_code,\n" +
                "rec.level,\n" +
                "CASE\n" +
                "  WHEN trail.balance !=0 AND rec.account_type_slug= ANY(ARRAY['asset', 'expense']) \n" +
                "  THEN trail.balance\n" +
                "  ELSE 0.0\n" +
                "  END as debit,\n" +
                "CASE\n" +
                "  WHEN trail.balance !=0 AND rec.account_type_slug = ANY(ARRAY['liability','equity','income']) \n" +
                "  THEN trail.balance\n" +
                "  ELSE 0.0\n" +
                "  END as credit\n" +
                "FROM rec\n" +
                "LEFT JOIN\n" +
                "  (SELECT sact.uuid, \n" +
                "   CASE\n" +
                "      WHEN sact.account_type_slug = ANY(ARRAY['liability','equity','income'])\n" +
                "      THEN SUM(sle.cr_amount) - SUM(sle.dr_amount)\n" +
                "      WHEN sact.account_type_slug = ANY(ARRAY['asset', 'expense'])\n" +
                "      THEN SUM(sle.dr_amount) - SUM(sle.cr_amount)\n" +
                "      ELSE 0.0\n" +
                "   END AS balance\n" +
                "  FROM rec AS sact\n" +
                "        LEFT JOIN ledger_entries AS sle ON sact.uuid = sle.account_uuid\n" +
                "        LEFT JOIN transactions AS str ON  sle.transaction_uuid = str.uuid\n" +
                "        WHERE str.deleted_at is null\n" +
                "        AND sle.deleted_at is null\n" +
                "        AND str.transaction_date <= '" + endDate + "'\n" +
                "        GROUP BY (sact.uuid, sact.account_type_slug)\n" +
                "  ) as trail ON rec.uuid = trail.uuid\n" +
                "ORDER BY path\n" +
                "LIMIT " + size + " OFFSET " + page;

        SlaveCustomTrialBalanceReportMapper mapper = new SlaveCustomTrialBalanceReportMapper();

        Flux<SlaveTrialBalanceReportDto> result = client.sql(query)
                .map(row -> mapper.apply(row, trialBalanceReportDto))
                .all();

        return result;
    }


//    @Override
//    public Flux<SlaveTrialBalanceDto> trialBalance(LocalDateTime startDate, LocalDateTime endDate, String dp, String d, Integer size, Long page) {
//        String query = "select sum(ledger_entries.dr_amount) as debitAccount,\n" +
//                "sum(ledger_entries.cr_amount) as creditAccount,\n" +
//                "accounts.code as accountCode,accounts.name as accountName,accounts.uuid as accountUUID,\n" +
//                " sum(ledger_entries.dr_amount) - sum(ledger_entries.cr_amount) as netBalance \n" +
//                "from ledger_entries\n" +
//                "join accounts \n" +
//                "on accounts.uuid=ledger_entries.account_uuid\n" +
//                "join transactions\n" +
//                "on transactions.uuid=ledger_entries.transaction_uuid\n" +
//                "where ledger_entries.deleted_at is null\n" +
//                "and transactions.deleted_at is null\n" +
//                "and accounts.deleted_at is null\n" +
//                "and transactions.transaction_date " +
//                "BETWEEN '" + startDate + "' AND '" + endDate + "' \n" +
//                "GROUP BY (accountUUID,accountCode,accountName)\n" +
//                "order by accounts." + dp + " \n" + d +
//                " limit " + size + " offset " + page;
//
//        SlaveCustomTrialBalanceMapper mapper = new SlaveCustomTrialBalanceMapper();
//
//        Flux<SlaveTrialBalanceDto> result = client.sql(query)
//                .map(row -> mapper.apply(row, trialBalanceDto))
//                .all();
//
//        return result;
//    }

    @Override
    public Flux<SlaveProfitAndLossStatementDto> profitAndLossStatement(String accountUUID, LocalDateTime startDate) {
        String query = "select ledger_entries.account_uuid,\n" +
                "CASE \n" +
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
                "where transactions.transaction_date < '" + startDate + "' \n" +
                "and  ledger_entries.account_uuid IN (" + accountUUID + ") \n" +
                "and ledger_entries.deleted_at is null \n" +
                "and transactions.deleted_at is null \n" +
                "and accounts.deleted_at is null\n" +
                "GROUP BY (ledger_entries.account_uuid)";

        SlaveProfitAndLossStatementMapper mapper = new SlaveProfitAndLossStatementMapper();

        Flux<SlaveProfitAndLossStatementDto> result = client.sql(query)
                .map(row -> mapper.apply(row, profitLossDto))
                .all();

        return result;
    }
}
