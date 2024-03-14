package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomTransactionRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.*;

import java.util.UUID;


public class SlaveCustomTransactionRepositoryImpl implements SlaveCustomTransactionRepository {
    private DatabaseClient client;
    private SlaveTransactionRecordDto slaveTransactionRecordDto;
    private SlaveTransactionDto slaveTransactionDto;
    private SlaveLedgerRowDto ledgerRows;
    private SlaveDocumentAttachmentDto documentAttachment;
    private SlaveTransactionDocumentListDto documentListDto;

    @Autowired
    public SlaveCustomTransactionRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Mono<SlaveTransactionDto> showAllTransactions(UUID transactionUUID) {

        String query = "Select *, \n" +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit\n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "WHERE transaction.uuid= '" + transactionUUID +
                "' AND transaction.deleted_at is null " +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_statuses.id as transactionStatusId, " +
                " transaction_statuses.uuid as transStatusUUID, transaction_statuses.name as transactionName\n" +
                "   FROM   transaction_statuses \n" +
                "   WHERE  \n" +
                "    transaction_statuses.deleted_at is null \n" +
                "   ) AS transactionStatus ON transactionData.transaction_status_uuid=transactionStatus.transStatusUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT vouchers.uuid as voucherUUID, " +
                "   vouchers.name as voucherName, voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "   voucher_type_catalogues.name as voucherTypeName,\n" +
                "   voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "   FROM   vouchers " +
                "   JOIN voucher_type_catalogues " +
                "   ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid\n" +
                "   WHERE  \n" +
                "   vouchers.deleted_at is null \n" +
                "   AND voucher_type_catalogues.deleted_at is null\n" +
                "   ) AS transactionVoucher ON transactionData.voucher_uuid=transactionVoucher.voucherUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName " +
                "   FROM   jobs \n" +
                "   WHERE  \n" +
                "    jobs.deleted_at is null \n" +
                "   ) AS jobs ON transactionData.job_uuid=jobs.jobUUID \n" +
                " )";

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Mono<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .one();

        return result;
    }

    @Override
    public Mono<SlaveTransactionDto> showTransactionWithVoucherType(UUID transactionUUID, String voucherType) {
        String query = "Select *, \n" +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit\n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "WHERE transaction.uuid= '" + transactionUUID +
                "' AND transaction.deleted_at is null " +
                ") AS transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_statuses.id as transactionStatusId, " +
                " transaction_statuses.uuid as transStatusUUID, transaction_statuses.name as transactionName\n" +
                "   FROM   transaction_statuses \n" +
                "   WHERE  \n" +
                "    transaction_statuses.deleted_at is null \n" +
                "   ) AS transactionStatus ON transactionData.transaction_status_uuid=transactionStatus.transStatusUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT uuid as voucherUUID, name as voucherName, voucher_type_catalogue_uuid as voucherTypeCatalogue" +
                "   FROM vouchers \n" +
                "   WHERE  \n" +
                "    vouchers.deleted_at is null \n" +
                "   ) AS voucher on transactionData.voucher_uuid=voucher.voucherUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT id as voucherTypeId,uuid as voucherTypeUUID,name as voucherTypeName, slug as voucherTypeSlug" +
                "   FROM   voucher_type_catalogues \n" +
                "   WHERE  \n" +
                "    voucher_type_catalogues.deleted_at is null \n" +
                "   ) AS voucherType on voucher.voucherTypeCatalogue=voucherType.voucherTypeUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName " +
                "   FROM   jobs \n" +
                "   WHERE  \n" +
                "    jobs.deleted_at is null \n" +
                "   ) AS jobs ON transactionData.job_uuid=jobs.jobUUID\n" +
                " ) WHERE voucherType.voucherTypeSlug = '" + voucherType + "'\n";

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Mono<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .one();

        return result;
    }

    @Override
    public Flux<SlaveLedgerRowDto> showAllLedgerRows(UUID transactionUUID) {
        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "where transaction.uuid= '" + transactionUUID +
                "' AND transaction.deleted_at is null" +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID,dr_amount as drAmount,description as ledgerDescription, " +
                " cr_amount as crAmount, account_uuid as accountUUID, cost_center_uuid as costCenterUUID, " +
                "profit_center_uuid as profitCenterUUID\n" +
                "   FROM   ledger_entries \n" +
                "   WHERE  \n" +
                "    ledger_entries.deleted_at is null \n" +
                "   ) AS ledgerPvt on transactionData.uuid = ledgerPvt.transactionUUID \n" +
                "LEFT  JOIN (\n" +
                "SELECT id as accountId,uuid as accountUUID,code as accountCode, name as accountName, control_code as controlCode" +
                "   FROM   accounts \n" +
                "   WHERE  \n" +
                "    accounts.deleted_at is null \n" +
                "   ) AS accountData on ledgerPvt.accountUUID = accountData.accountUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as profitId,uuid as profitUUID,name as profitCenterName" +
                "   FROM   profit_centers \n" +
                "   WHERE  \n" +
                "    profit_centers.deleted_at is null \n" +
                "   ) AS profitCenterData on ledgerPvt.profitCenterUUID = profitCenterData.profitUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as costId,uuid as costUUID,name as costCenterName" +
                "   FROM   cost_centers \n" +
                "   WHERE  \n" +
                "    cost_centers.deleted_at is null \n" +
                "   ) AS costCenterData on ledgerPvt.costCenterUUID = costCenterData.costUUID )" +
                "";

        SlaveCustomLedgerEntryMapper mapper = new SlaveCustomLedgerEntryMapper();

        Flux<SlaveLedgerRowDto> result = client.sql(query)
                .map(row -> mapper.apply(row, ledgerRows))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveDocumentAttachmentDto> showAllDocumentAttachments(UUID transactionUUID) {

        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "WHERE transaction.uuid= '" + transactionUUID +
                "' AND transaction.deleted_at is null " +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID, " +
                " document_uuid as documentUUID, bucket_uuid as docBucketUUID\n" +
                "   FROM   transaction_document_pvt \n" +
                "   WHERE  \n" +
                "    transaction_document_pvt.deleted_at is null \n" +
                "   ) AS transactionDocumentPvt on transactionData.uuid = transactionDocumentPvt.transactionUUID\n" +
                " )";

        SlaveCustomTransactionDocumentMapper mapper = new SlaveCustomTransactionDocumentMapper();

        Flux<SlaveDocumentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, documentAttachment))
                .all();

        return result;
    }


    @Override
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page) {
        String query = "Select transactions.*, " +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit,\n" +
                "transaction_statuses.id as transactionStatusId," +
                "transaction_statuses.uuid as transStatusUUID,transaction_statuses.name as transactionName," +
                "jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName,vouchers.uuid as voucherUUID," +
                "vouchers.name as voucherName, voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "voucher_type_catalogues.name as voucherTypeName, voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "from transactions \n" +
                "LEFT JOIN transaction_statuses ON transactions.transaction_status_uuid=transaction_statuses.uuid \n" +
                "LEFT JOIN jobs ON jobs.uuid=transactions.job_uuid \n" +
                "LEFT JOIN vouchers ON transactions.voucher_uuid=vouchers.uuid \n" +
                "LEFT JOIN voucher_type_catalogues ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid \n" +
                "WHERE transactions.deleted_at is null\n" +
                "AND transaction_statuses.deleted_at is null\n" +
                "AND jobs.deleted_at is null\n" +
                "AND vouchers.deleted_at is null\n" +
                "AND voucher_type_catalogues.deleted_at is null\n" +
                "AND transactions.voucher_uuid = '" + voucherUUID +
                "' ORDER BY transactions." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Flux<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .all();

        return result;
    }

    @Override
    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page) {
        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                " WHERE transaction.deleted_at is null " +
                " AND transaction.voucher_uuid = '" + voucherUUID +
                "' order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID, " +
                " document_uuid as documentUUID, bucket_uuid as docBucketUUID\n" +
                "   FROM   transaction_document_pvt \n" +
                "   WHERE  \n" +
                "    transaction_document_pvt.deleted_at is null \n" +
                "   ) AS transactionDocumentPvt on  transactionData.uuid = transactionDocumentPvt.transactionUUID\n" +
                " ) " +
                "order by " + dp + " " + d + " ";

        SlaveCustomDocumentMapper mapper = new SlaveCustomDocumentMapper();

        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> result = client.sql(query)
                .map(row -> mapper.apply(row, documentListDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTransactionRecordDto> listOfTransactionLedgerRowsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page) {
        String query = "Select *, " +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit\n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                " WHERE transaction.deleted_at is null " +
                " AND transaction.voucher_uuid = '" + voucherUUID +
                "' order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT id as jobId,uuid as jobUUID,name as jobCenterName" +
                "   FROM   jobs \n" +
                "   WHERE  \n" +
                "    jobs.deleted_at is null \n" +
                "   ) AS jobCenter on transactionData.job_uuid = jobCenter.jobUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT id as transactionStatusId,uuid as transactionStatusUUID,name as transactionStatusName" +
                "   FROM   transaction_statuses \n" +
                "   WHERE  \n" +
                "    transaction_statuses.deleted_at is null \n" +
                "   ) AS transactionStatus on transactionData.transaction_status_uuid=transactionStatus.transactionStatusUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT vouchers.uuid as voucherUUID, " +
                "   vouchers.name as voucherName, voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "   voucher_type_catalogues.name as voucherTypeName,voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "   FROM   vouchers " +
                "   JOIN voucher_type_catalogues " +
                "   ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid \n" +
                "   WHERE  \n" +
                "   vouchers.deleted_at is null \n" +
                "   AND voucher_type_catalogues.deleted_at is null\n" +
                "   ) AS transactionVoucher ON transactionData.voucher_uuid=transactionVoucher.voucherUUID\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID,dr_amount as drAmount,description as ledgerDescription, " +
                " cr_amount as crAmount, account_uuid as accountUUID, cost_center_uuid as costCenterUUID, " +
                " profit_center_uuid as profitCenterUUID\n" +
                "   FROM   ledger_entries \n" +
                "   WHERE  \n" +
                "    ledger_entries.deleted_at is null \n" +
                "   ) AS ledgerPvt on transactionData.uuid=ledgerPvt.transactionUUID \n" +
                "LEFT  JOIN (\n" +
                "SELECT id as accountId,uuid as accUUID,code as accountCode, name as accountName, control_code as controlCode" +
                "   FROM   accounts \n" +
                "   WHERE  \n" +
                "    accounts.deleted_at is null \n" +
                "   ) AS accountData on ledgerPvt.accountUUID = accountData.accUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as profitId,uuid as profitUUID,name as profitCenterName" +
                "   FROM   profit_centers \n" +
                "   WHERE  \n" +
                "    profit_centers.deleted_at is null \n" +
                "   ) AS profitCenterData on ledgerPvt.profitCenterUUID = profitCenterData.profitUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as costId,uuid as costUUID,name as costCenterName" +
                "   FROM   cost_centers \n" +
                "   WHERE  \n" +
                "    cost_centers.deleted_at is null \n" +
                "   ) AS costCenterData on ledgerPvt.costCenterUUID = costCenterData.costUUID )" +
                "order by " + dp + " " + d;

        SlaveCustomTransactionRecordMapper mapper = new SlaveCustomTransactionRecordMapper();

        Flux<SlaveTransactionRecordDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionRecordDto))
                .all();

        return result;
    }


    @Override
    public Flux<SlaveTransactionDto> listAllTransactions(String dp, String d, Integer size, Long page) {
        String query = "Select transactions.*, " +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit,\n" +
                "transaction_statuses.id as transactionStatusId,\n" +
                "transaction_statuses.uuid as transStatusUUID,transaction_statuses.name as transactionName,\n" +
                "jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName,vouchers.uuid as voucherUUID,\n" +
                "vouchers.name as voucherName,voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "voucher_type_catalogues.name as voucherTypeName, voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "from transactions \n" +
                "LEFT JOIN transaction_statuses ON transactions.transaction_status_uuid=transaction_statuses.uuid \n" +
                "LEFT JOIN jobs ON transactions.job_uuid=jobs.uuid\n" +
                "LEFT JOIN vouchers ON transactions.voucher_uuid=vouchers.uuid \n" +
                "LEFT JOIN voucher_type_catalogues ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid \n" +
                "WHERE transactions.deleted_at is null\n" +
                "AND transaction_statuses.deleted_at is null\n" +
                "AND jobs.deleted_at is null\n" +
                "AND vouchers.deleted_at is null\n" +
                "AND voucher_type_catalogues.deleted_at is null\n" +
                " ORDER BY transactions." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Flux<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .all();

        return result;
    }

    @Override
    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachments(String dp, String d, Integer size, Long page) {
        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                " WHERE transaction.deleted_at is null " +
                " order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID, " +
                " document_uuid as documentUUID, bucket_uuid as docBucketUUID\n" +
                "   FROM   transaction_document_pvt \n" +
                "   WHERE  \n" +
                "    transaction_document_pvt.deleted_at is null \n" +
                "   ) AS transactionDocumentPvt on transactionData.uuid = transactionDocumentPvt.transactionUUID\n" +
                " ) " +
                "order by " + dp + " " + d + " ";

        SlaveCustomDocumentMapper mapper = new SlaveCustomDocumentMapper();

        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> result = client.sql(query)
                .map(row -> mapper.apply(row, documentListDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTransactionRecordDto> listOfTransactionLedgerRows(String dp, String d, Integer size, Long page) {
        String query = "Select *, \n" +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit\n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                " WHERE transaction.deleted_at is null " +
                " order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT id as jobId,uuid as jobUUID,name as jobCenterName" +
                "   FROM   jobs \n" +
                "   WHERE  \n" +
                "    jobs.deleted_at is null \n" +
                "   ) AS jobCenter on transactionData.job_uuid=jobCenter.jobUUID\n" +
                "LEFT  JOIN (\n" +
                "   SELECT id as transactionStatusId,uuid as transactionStatusUUID,name as transactionStatusName" +
                "   FROM   transaction_statuses \n" +
                "   WHERE  \n" +
                "    transaction_statuses.deleted_at is null \n" +
                "   ) AS transactionStatus on transactionData.transaction_status_uuid=transactionStatus.transactionStatusUUID\n" +
                "LEFT  JOIN (\n" +
                "   SELECT vouchers.uuid as voucherUUID, " +
                "   vouchers.name as voucherName, voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "   voucher_type_catalogues.name as voucherTypeName, voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "   FROM   vouchers " +
                "   JOIN voucher_type_catalogues " +
                "   ON vouchers.voucher_type_catalogue_uuid = voucher_type_catalogues.uuid \n" +
                "   WHERE  \n" +
                "   vouchers.deleted_at is null \n" +
                "   AND voucher_type_catalogues.deleted_at is null\n" +
                "   ) AS transactionVoucher ON transactionData.voucher_uuid=transactionVoucher.voucherUUID \n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID,dr_amount as drAmount,description as ledgerDescription, " +
                " cr_amount as crAmount, account_uuid as accountUUID, cost_center_uuid as costCenterUUID, " +
                "profit_center_uuid as profitCenterUUID\n" +
                "   FROM   ledger_entries \n" +
                "   WHERE  \n" +
                "    ledger_entries.deleted_at is null \n" +
                "   ) AS ledgerPvt on transactionData.uuid = ledgerPvt.transactionUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as accountId,uuid as accUUID,code as accountCode, name as accountName, control_code as controlCode" +
                "   FROM   accounts \n" +
                "   WHERE  \n" +
                "    accounts.deleted_at is null \n" +
                "   ) AS accountData on ledgerPvt.accountUUID = accountData.accUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as profitId,uuid as profitUUID,name as profitCenterName" +
                "   FROM   profit_centers \n" +
                "   WHERE  \n" +
                "    profit_centers.deleted_at is null \n" +
                "   ) AS profitCenterData on ledgerPvt.profitCenterUUID = profitCenterData.profitUUID\n" +
                "LEFT  JOIN (\n" +
                "SELECT id as costId,uuid as costUUID,name as costCenterName" +
                "   FROM   cost_centers \n" +
                "   WHERE  \n" +
                "    cost_centers.deleted_at is null \n" +
                "   ) AS costCenterData on ledgerPvt.costCenterUUID = costCenterData.costUUID )" +
                "order by " + dp + " " + d;

        SlaveCustomTransactionRecordMapper mapper = new SlaveCustomTransactionRecordMapper();

        Flux<SlaveTransactionRecordDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionRecordDto))
                .all();

        return result;
    }


    @Override
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "Select transactions.*, " +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit,\n" +
                "transaction_statuses.id as transactionStatusId,\n" +
                "transaction_statuses.uuid as transStatusUUID,transaction_statuses.name as transactionName,\n" +
                "jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName,vouchers.uuid as voucherUUID,\n" +
                "vouchers.name as voucherName,voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "voucher_type_catalogues.name as voucherTypeName, voucher_type_catalogues.slug as voucherTypeSlug \n" +
                "from transactions \n" +
                "LEFT JOIN transaction_statuses ON transactions.transaction_status_uuid=transaction_statuses.uuid " +
                "LEFT JOIN jobs ON transactions.job_uuid=jobs.uuid " +
                "LEFT JOIN vouchers ON transactions.voucher_uuid=vouchers.uuid " +
                "LEFT JOIN voucher_type_catalogues ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid " +
                "WHERE transactions.deleted_at is null\n" +
                "AND transaction_statuses.deleted_at IS NULL\n" +
                "AND jobs.deleted_at IS NULL\n" +
                "AND vouchers.deleted_at IS NULL\n" +
                "AND voucher_type_catalogues.deleted_at IS NULL\n" +
                "AND vouchers.voucher_type_catalogue_uuid = '" + voucherTypeUUID +
                "' ORDER BY transactions." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Flux<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .all();

        return result;
    }

    @Override
    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "LEFT JOIN (\n" +
                "   SELECT vouchers.uuid as voucherUUID, " +
                "   vouchers.voucher_type_catalogue_uuid as voucherTypeCatalogue" +
                "   FROM vouchers) AS voucher ON transaction.voucher_uuid = voucher.voucherUUID \n" +
                "WHERE voucher.voucherTypeCatalogue = '" + voucherTypeUUID + "'\n" +
                "AND transaction.deleted_at is null " +
                "order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID, " +
                " document_uuid as documentUUID, bucket_uuid as docBucketUUID\n" +
                "   FROM   transaction_document_pvt \n" +
                "   WHERE  \n" +
                "    transaction_document_pvt.deleted_at is null \n" +
                "   ) AS transactionDocumentPvt on transactionData.uuid = transactionDocumentPvt.transactionUUID " +
                " )\n" +
                "order by " + dp + " " + d + " ";

        SlaveCustomDocumentMapper mapper = new SlaveCustomDocumentMapper();

        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> result = client.sql(query)
                .map(row -> mapper.apply(row, documentListDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTransactionRecordDto> showTransactionLedgerRowsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "SELECT *, \n" +
                "    (SELECT SUM(dr_amount) FROM ledger_entries WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "    AND ledger_entries.deleted_at IS NULL\n" +
                "    GROUP BY transaction_uuid) as debit,\n" +
                "    (SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "    WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "    AND ledger_entries.deleted_at IS NULL\n" +
                "    GROUP BY transaction_uuid) as credit\n" +
                "FROM (\n" +
                "    SELECT *\n" +
                "    FROM transactions as transaction\n" +
                "    LEFT JOIN (\n" +
                "        SELECT\n" +
                "            vouchers.uuid as voucherUUID,\n" +
                "            voucher_type_catalogue_uuid as voucherTypeUUID,\n" +
                "            vouchers.name as voucherName,\n" +
                "            voucher_type_catalogues.name as voucherTypeName,\n" +
                "            voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "        FROM\n" +
                "            vouchers\n" +
                "        JOIN voucher_type_catalogues ON voucher_type_catalogues.uuid = vouchers.voucher_type_catalogue_uuid\n" +
                "        WHERE\n" +
                "            vouchers.deleted_at IS NULL \n" +
                "            AND voucher_type_catalogues.deleted_at IS NULL\n" +
                "    ) AS transactionVoucher ON transactionVoucher.voucherUUID = transaction.voucher_uuid \n" +
                "    WHERE transaction.deleted_at IS NULL \n" +
                "    AND transactionVoucher.voucherTypeUUID = '" + voucherTypeUUID + "'\n"+
                "    ORDER BY " + dp + " " + d + " " +
                "    LIMIT " + size + "  OFFSET " + page + "\n" +
                ") as transactionData\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as jobId,\n" +
                "        uuid as jobUUID,\n" +
                "        name as jobCenterName\n" +
                "    FROM\n" +
                "        jobs \n" +
                "    WHERE  \n" +
                "        jobs.deleted_at IS NULL \n" +
                ") AS jobCenter ON jobCenter.jobUUID = transactionData.job_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as transactionStatusId,\n" +
                "        uuid as transactionStatusUUID,\n" +
                "        name as transactionStatusName   \n" +
                "    FROM\n" +
                "        transaction_statuses \n" +
                "    WHERE  \n" +
                "        transaction_statuses.deleted_at IS NULL \n" +
                ") AS transactionStatus ON transactionStatus.transactionStatusUUID = transactionData.transaction_status_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        transaction_uuid as transactionUUID,\n" +
                "        dr_amount as drAmount,\n" +
                "        description as ledgerDescription,\n" +
                "        cr_amount as crAmount,\n" +
                "        account_uuid as accountUUID,\n" +
                "        cost_center_uuid as costCenterUUID,\n" +
                "        profit_center_uuid as profitCenterUUID\n" +
                "    FROM\n" +
                "        ledger_entries \n" +
                "    WHERE\n" +
                "        ledger_entries.deleted_at IS NULL \n" +
                ") AS ledgerPvt ON ledgerPvt.transactionUUID = transactionData.uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as accountId,\n" +
                "        uuid as accUUID,\n" +
                "        code as accountCode,\n" +
                "        name as accountName,\n" +
                "        control_code as controlCode  \n" +
                "    FROM\n" +
                "        accounts \n" +
                "    WHERE\n" +
                "        accounts.deleted_at IS NULL \n" +
                ") AS accountData ON ledgerPvt.accountUUID = accountData.accUUID\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as profitId,\n" +
                "        uuid as profitUUID,\n" +
                "        name as profitCenterName\n" +
                "    FROM\n" +
                "        profit_centers \n" +
                "    WHERE\n" +
                "        profit_centers.deleted_at IS NULL \n" +
                ") AS profitCenterData ON ledgerPvt.profitCenterUUID = profitCenterData.profitUUID\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as costId,\n" +
                "        uuid as costUUID,\n" +
                "        name as costCenterName\n" +
                "    FROM\n" +
                "        cost_centers \n" +
                "    WHERE\n" +
                "        cost_centers.deleted_at IS NULL \n" +
                ") AS costCenterData ON ledgerPvt.costCenterUUID = costCenterData.costUUID\n" +
                "ORDER BY " + dp + " " + d;

        SlaveCustomTransactionRecordMapper mapper = new SlaveCustomTransactionRecordMapper();

        Flux<SlaveTransactionRecordDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionRecordDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "Select transactions.*, " +
                "(SELECT SUM(dr_amount) FROM ledger_entries " +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as debit," +
                "\n" +
                "(SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "WHERE ledger_entries.transaction_uuid = transactions.uuid\n" +
                "AND ledger_entries.deleted_at IS NULL\n" +
                "GROUP BY transaction_uuid) as credit,\n" +
                "transaction_statuses.id as transactionStatusId,\n" +
                "transaction_statuses.uuid as transStatusUUID,transaction_statuses.name as transactionName,\n" +
                "jobs.id as jobCenterId,jobs.uuid as jobUUID,jobs.name as jobCenterName,vouchers.uuid as voucherUUID,\n" +
                "vouchers.name as voucherName,voucher_type_catalogues.uuid as voucherTypeUUID,\n" +
                "voucher_type_catalogues.name as voucherTypeName, voucher_type_catalogues.slug as voucherTypeSlug \n" +
                "from transactions \n" +
                "LEFT JOIN transaction_statuses ON transactions.transaction_status_uuid=transaction_statuses.uuid \n" +
                "LEFT JOIN jobs ON transactions.job_uuid=jobs.uuid \n" +
                "LEFT JOIN vouchers ON transactions.voucher_uuid=vouchers.uuid \n" +
                "LEFT JOIN voucher_type_catalogues ON vouchers.voucher_type_catalogue_uuid=voucher_type_catalogues.uuid \n" +
                "WHERE transactions.deleted_at is null\n" +
                "AND transaction_statuses.deleted_at IS NULL\n" +
                "AND jobs.deleted_at IS NULL\n" +
                "AND vouchers.deleted_at IS NULL\n" +
                "AND voucher_type_catalogues.deleted_at IS NULL\n" +
                "AND vouchers.voucher_type_catalogue_uuid = '" + voucherTypeUUID +
                "' AND transactions.voucher_uuid = '" + voucherUUID +
                "' ORDER BY transactions." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTransactionMapper mapper = new SlaveCustomTransactionMapper();

        Flux<SlaveTransactionDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionDto))
                .all();

        return result;
    }

    @Override
    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "Select * \n" +
                "from (\n" +
                "(Select transaction.*\n" +
                "from transactions as transaction\n" +
                "LEFT JOIN (\n" +
                "   SELECT vouchers.uuid as voucherUUID, " +
                "   vouchers.voucher_type_catalogue_uuid as voucherTypeCatalogue" +
                "   FROM vouchers) AS voucher ON transaction.voucher_uuid = voucher.voucherUUID\n" +
                "WHERE transaction.deleted_at is null " +
                "AND voucher.voucherTypeCatalogue = '" + voucherTypeUUID + "'\n"+
                "AND transaction.voucher_uuid = '" + voucherUUID + "'\n"+
                "order by " + dp + " " + d + " " +
                "limit " + size + " offset " + page +
                ") as transactionData\n" +
                "LEFT  JOIN (\n" +
                "   SELECT transaction_uuid as transactionUUID, " +
                " document_uuid as documentUUID, bucket_uuid as docBucketUUID\n" +
                "   FROM   transaction_document_pvt \n" +
                "   WHERE  \n" +
                "    transaction_document_pvt.deleted_at is null \n" +
                "   ) AS transactionDocumentPvt on transactionData.uuid = transactionDocumentPvt.transactionUUID\n" +
                ")\n"+
                "order by " + dp + " " + d + " ";

        SlaveCustomDocumentMapper mapper = new SlaveCustomDocumentMapper();

        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> result = client.sql(query)
                .map(row -> mapper.apply(row, documentListDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTransactionRecordDto> showTransactionLedgerRowsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page) {
        String query = "SELECT *, \n" +
                "    (SELECT SUM(dr_amount) FROM ledger_entries WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "    AND ledger_entries.deleted_at IS NULL\n" +
                "    GROUP BY transaction_uuid) as debit,\n" +
                "    (SELECT SUM(cr_amount) FROM ledger_entries\n" +
                "    WHERE ledger_entries.transaction_uuid = transactionData.uuid\n" +
                "    AND ledger_entries.deleted_at IS NULL\n" +
                "    GROUP BY transaction_uuid) as credit\n" +
                "FROM (\n" +
                "    SELECT *\n" +
                "    FROM transactions as transaction\n" +
                "    LEFT JOIN (\n" +
                "        SELECT\n" +
                "            vouchers.uuid as voucherUUID,\n" +
                "            voucher_type_catalogue_uuid as voucherTypeUUID,\n" +
                "            vouchers.name as voucherName,\n" +
                "            voucher_type_catalogues.name as voucherTypeName,\n" +
                "            voucher_type_catalogues.slug as voucherTypeSlug\n" +
                "        FROM\n" +
                "            vouchers\n" +
                "        JOIN voucher_type_catalogues ON voucher_type_catalogues.uuid = vouchers.voucher_type_catalogue_uuid\n" +
                "        WHERE\n" +
                "            vouchers.deleted_at IS NULL \n" +
                "            AND voucher_type_catalogues.deleted_at IS NULL\n" +
                "    ) AS transactionVoucher ON transactionVoucher.voucherUUID = transaction.voucher_uuid \n" +
                "    WHERE transaction.deleted_at IS NULL \n" +
                "    AND transaction.voucher_uuid = '" + voucherUUID + "'\n"+
                "    ORDER BY " + dp + " " + d + " " +
                "    LIMIT " + size + "  OFFSET " + page + "\n" +
                ") as transactionData\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as jobId,\n" +
                "        uuid as jobUUID,\n" +
                "        name as jobCenterName\n" +
                "    FROM\n" +
                "        jobs \n" +
                "    WHERE  \n" +
                "        jobs.deleted_at IS NULL \n" +
                ") AS jobCenter ON jobCenter.jobUUID = transactionData.job_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as transactionStatusId,\n" +
                "        uuid as transactionStatusUUID,\n" +
                "        name as transactionStatusName   \n" +
                "    FROM\n" +
                "        transaction_statuses \n" +
                "    WHERE  \n" +
                "        transaction_statuses.deleted_at IS NULL \n" +
                ") AS transactionStatus ON transactionStatus.transactionStatusUUID = transactionData.transaction_status_uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        transaction_uuid as transactionUUID,\n" +
                "        dr_amount as drAmount,\n" +
                "        description as ledgerDescription,\n" +
                "        cr_amount as crAmount,\n" +
                "        account_uuid as accountUUID,\n" +
                "        cost_center_uuid as costCenterUUID,\n" +
                "        profit_center_uuid as profitCenterUUID\n" +
                "    FROM\n" +
                "        ledger_entries \n" +
                "    WHERE\n" +
                "        ledger_entries.deleted_at IS NULL \n" +
                ") AS ledgerPvt ON ledgerPvt.transactionUUID = transactionData.uuid\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as accountId,\n" +
                "        uuid as accUUID,\n" +
                "        code as accountCode,\n" +
                "        name as accountName,\n" +
                "        control_code as controlCode  \n" +
                "    FROM\n" +
                "        accounts \n" +
                "    WHERE\n" +
                "        accounts.deleted_at IS NULL \n" +
                ") AS accountData ON ledgerPvt.accountUUID = accountData.accUUID\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as profitId,\n" +
                "        uuid as profitUUID,\n" +
                "        name as profitCenterName\n" +
                "    FROM\n" +
                "        profit_centers \n" +
                "    WHERE\n" +
                "        profit_centers.deleted_at IS NULL \n" +
                ") AS profitCenterData ON ledgerPvt.profitCenterUUID = profitCenterData.profitUUID\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        id as costId,\n" +
                "        uuid as costUUID,\n" +
                "        name as costCenterName\n" +
                "    FROM\n" +
                "        cost_centers \n" +
                "    WHERE\n" +
                "        cost_centers.deleted_at IS NULL \n" +
                ") AS costCenterData ON ledgerPvt.costCenterUUID = costCenterData.costUUID\n" +
                "WHERE\n" +
                "    transactionData.voucherTypeUUID = '" + voucherTypeUUID + "'\n"+
                "ORDER BY " + dp + " " + d;

        SlaveCustomTransactionRecordMapper mapper = new SlaveCustomTransactionRecordMapper();

        Flux<SlaveTransactionRecordDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTransactionRecordDto))
                .all();

        return result;
    }

}

