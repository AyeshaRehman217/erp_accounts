package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveOpeningBalanceAccountDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountDtoMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomOpeningBalanceAccountDtoMapper;

import java.util.UUID;

public class SlaveCustomAccountRepositoryImpl implements SlaveCustomAccountRepository {
    private DatabaseClient client;
    private SlaveAccountDto slaveAccountDto;
    private SlaveAccountEntity slaveAccountEntity;
    private SlaveOpeningBalanceAccountDto slaveOpeningBalanceAccountDto;

    @Autowired
    public SlaveCustomAccountRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    @Override
    public Flux<SlaveAccountDto> showAllAccounts(String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page) {
        String query = "WITH recursive rec AS (\n" +
                "\n" +
                "  SELECT t.*,\n" +
                "         ROW_NUMBER() OVER (PARTITION BY t.parent_account_uuid ORDER BY t.control_code)::text AS path\n" +
                "  FROM accounts AS t\n" +
                "  WHERE t.parent_account_uuid IS NULL \n" +
                "  AND t.deleted_at IS NULL\n" +
                "  \n" +
                "  UNION ALL\n" +
                "  \n" +
                "  SELECT a.*,\n" +
                "         concat(rec.path, '-', ROW_NUMBER() OVER (PARTITION BY a.parent_account_uuid ORDER BY a." + dp + " " + d + ")::text) AS path\n" +
                "  FROM rec \n" +
                "  JOIN accounts AS a ON a.parent_account_uuid = rec.uuid \n" +
                "  WHERE a.deleted_at IS NULL\n" +
                "  \n" +
                ")\n" +
                "SELECT DISTINCT *\n" +
                "FROM rec \n" +
                "WHERE (rec.name ILIKE  '%" + name + "%' " +
                "OR rec.code ILIKE  '%" + code + "%' " +
                "OR rec.description ILIKE  '%" + description + "%' " +
                "OR rec.control_code ILIKE  '%" + controlCode + "%' )" +
                "ORDER BY path \n" +
                "LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountDtoMapper mapper = new SlaveCustomAccountDtoMapper();

        Flux<SlaveAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountDto> showAllAccountsWithCompany(UUID companyUUID, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page) {

        String query = "WITH recursive rec AS (\n" +
                "\n" +
                "  SELECT t.*,\n" +
                "         ROW_NUMBER() OVER (PARTITION BY t.parent_account_uuid ORDER BY t.control_code)::text AS path\n" +
                "  FROM accounts AS t\n" +
                "  WHERE t.parent_account_uuid IS NULL \n" +
                "  AND t.deleted_at IS NULL\n" +
                "  \n" +
                "  UNION ALL\n" +
                "  \n" +
                "  SELECT a.*,\n" +
                "         concat(rec.path, '-', ROW_NUMBER() OVER (PARTITION BY a.parent_account_uuid ORDER BY a." + dp + " " + d + ")::text) AS path\n" +
                "  FROM rec \n" +
                "  JOIN accounts AS a ON a.parent_account_uuid = rec.uuid \n" +
                "  WHERE a.deleted_at IS NULL\n" +
                "  \n" +
                ")\n" +
                "SELECT DISTINCT *\n" +
                "FROM rec \n" +
                "WHERE (rec.name ILIKE  '%" + name + "%' " +
                "OR rec.code ILIKE  '%" + code + "%' " +
                "OR rec.description ILIKE  '%" + description + "%' " +
                "OR rec.control_code ILIKE  '%" + controlCode + "%' )" +
                "AND rec.company_uuid = '" + companyUUID + "'\n" +
                "ORDER BY path \n" +
                "LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountDtoMapper mapper = new SlaveCustomAccountDtoMapper();

        Flux<SlaveAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> indexAllAccountsAgainstVoucherAndSubAccountGroupWithStatus(UUID voucherUUID, Boolean status, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page) {
        String query = "select DISTINCT accounts.* \n" +
                "from accounts \n" +
                "left join sub_account_group_accounts_pvt on accounts.uuid=sub_account_group_accounts_pvt.account_uuid\n" +
                "left join voucher_sub_account_groups_pvt on sub_account_group_accounts_pvt.sub_account_group_uuid=voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
                "where accounts.deleted_at is null\n" +
                "and accounts.status= " + status + "\n"+
                "and accounts.is_entry_allowed = true\n" +
                "and sub_account_group_accounts_pvt.deleted_at is null\n" +
                "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
                "and voucher_uuid='" + voucherUUID + "' " +
                "and (accounts.name ILIKE  '%" + name + "%' \n" +
                " OR accounts.code ILIKE  '%" + code + "%' \n" +
                " OR accounts.description ILIKE  '%" + description + "%' \n" +
                " OR accounts.control_code ILIKE  '%" + controlCode + "%' ) \n" +
                " ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> indexAllAccountsAgainstVoucherAndSubAccountGroupWithoutStatus(UUID voucherUUID, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page) {
        String query = "select DISTINCT accounts.* \n" +
                "from accounts \n" +
                "left join sub_account_group_accounts_pvt on accounts.uuid=sub_account_group_accounts_pvt.account_uuid\n" +
                "left join voucher_sub_account_groups_pvt on sub_account_group_accounts_pvt.sub_account_group_uuid=voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
                "where accounts.deleted_at is null\n" +
                "and accounts.is_entry_allowed = true\n" +
                " and sub_account_group_accounts_pvt.deleted_at is null\n" +
                "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
                "and voucher_uuid='" + voucherUUID + "' " +
                "and (accounts.name ILIKE  '%" + name + "%' \n" +
                " OR accounts.code ILIKE  '%" + code + "%' \n" +
                " OR accounts.description ILIKE  '%" + description + "%' \n" +
                " OR accounts.control_code ILIKE  '%" + controlCode + "%' ) \n" +
                " ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Mono<SlaveOpeningBalanceAccountDto> showWithUuid(UUID uuid) {
        String query = "SELECT acc.*, openingBalanceTrst.* " +
                "FROM accounts AS acc\n " +
                "LEFT JOIN " +
                "(SELECT transactions.uuid AS openingBalanceUUID, \n" +
                "ledger_entries.description AS openingBalanceDescription, \n" +
                "CASE\n" +
                "    WHEN ledger_entries.dr_amount != 0 THEN true\n" +
                "    ELSE false\n" +
                "END as debit,\n" +
                "CASE\n" +
                "    WHEN ledger_entries.dr_amount != 0 THEN ledger_entries.dr_amount\n" +
                "    ELSE ledger_entries.cr_amount\n" +
                "END as openingBalance\n" +
                "FROM transactions \n" +
                "LEFT JOIN ledger_entries ON transactions.uuid = ledger_entries.transaction_uuid \n" +
                "WHERE transactions.deleted_at IS NULL \n" +
                "AND ledger_entries.deleted_at IS NULL) AS openingBalanceTrst\n" +
                "ON acc.opening_balance_uuid = openingBalanceTrst.openingBalanceUUID\n" +
                "WHERE acc.deleted_at IS NULL\n" +
                "AND acc.uuid = '"+ uuid +"'";

        SlaveCustomOpeningBalanceAccountDtoMapper mapper = new SlaveCustomOpeningBalanceAccountDtoMapper();

        Mono<SlaveOpeningBalanceAccountDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveOpeningBalanceAccountDto))
                .one();

        return result;
    }
}
