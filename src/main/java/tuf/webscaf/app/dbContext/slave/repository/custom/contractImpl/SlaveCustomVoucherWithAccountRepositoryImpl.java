package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithAccountRepositoryImpl implements SlaveCustomVoucherWithAccountRepository {
    private DatabaseClient client;
    private SlaveAccountEntity slaveAccountEntity;

    @Autowired
    public SlaveCustomVoucherWithAccountRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveAccountEntity> indexAccount(String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page) {
        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.deleted_at IS NULL\n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> indexAccountWithStatus(String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.deleted_at IS NULL\n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "AND accounts.status=" + status +
                " ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> indexAccountWithEntryAllowed(String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page) {
        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.deleted_at IS NULL\n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "AND accounts.is_entry_allowed = true \n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> indexAccountWithStatusAndEntryAllowed(String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.deleted_at IS NULL\n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "AND accounts.status=" + status +
                " AND accounts.is_entry_allowed = true \n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithAccountNameFilter(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {
        String query = "SELECT DISTINCT accounts.* FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND accounts.deleted_at IS NULL\n" +
                "AND account_group_account_pvt.deleted_at IS NULL\n" +
                "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND accounts.name ILIKE  '%" + name + "%' " +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithAccountNameAndStatusFilter(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT DISTINCT accounts.* FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND accounts.deleted_at IS NULL\n" +
                " AND accounts.status = " + status +
                " AND account_group_account_pvt.deleted_at IS NULL\n" +
                "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND accounts.name ILIKE  '%" + name + "%' " +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountList(UUID voucherUUID, String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page) {

        String query = "SELECT DISTINCT accounts.* FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND accounts.deleted_at IS NULL\n" +
                "AND account_group_account_pvt.deleted_at IS NULL\n" +
                "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND accounts.is_entry_allowed = true \n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithStatusFilter(UUID voucherUUID, String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT DISTINCT accounts.* FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND accounts.deleted_at IS NULL\n" +
                " AND accounts.status = " + status +
                " AND accounts.is_entry_allowed = true \n" +
                " AND account_group_account_pvt.deleted_at IS NULL\n" +
                "AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND (accounts.name ILIKE  '%" + name + "%' " +
                "OR accounts.description ILIKE  '%" + description + "%' " +
                "OR accounts.code ILIKE  '%" + code + "%' " +
                "OR accounts.control_code ILIKE  '%" + controlCode + "%' )\n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;

    }


    @Override
    public Flux<SlaveAccountEntity> showAccountListWithAccountCodeFilter(UUID voucherUUID, String accountCode, String dp, String d, Integer size, Long page) {
        String query = "select distinct accounts.* from accounts\n" +
                "left join account_group_account_pvt\n" +
                "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "left join voucher_account_group_pvt\n" +
                "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and accounts.deleted_at is null\n" +
                "and account_group_account_pvt.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and accounts.code ILIKE  '%" + accountCode + "%' " +
                "order by accounts." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithAccountCodeAndStatusFilter(UUID voucherUUID, String accountCode, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct accounts.* from accounts\n" +
                "left join account_group_account_pvt\n" +
                "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "left join voucher_account_group_pvt\n" +
                "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and accounts.deleted_at is null\n" +
                " and accounts.status = " + status +
                " and account_group_account_pvt.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and accounts.code ILIKE  '%" + accountCode + "%' " +
                "order by accounts." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String code, String dp, String d, Integer size, Long page) {
        String query = "select distinct accounts.* from accounts\n" +
                "left join account_group_account_pvt\n" +
                "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "left join voucher_account_group_pvt\n" +
                "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and accounts.company_uuid = '" + companyUUID +
                "' and accounts.deleted_at is null\n" +
                "and account_group_account_pvt.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and (accounts.name ILIKE  '%" + name + "%' " +
                "or accounts.description ILIKE  '%" + description + "%' or accounts.code ILIKE '%" + code + "%')" +
                "order by accounts." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showAccountListWithCompanyAndStatusFilter(UUID voucherUUID, Boolean status, UUID companyUUID, String name, String description, String code, String dp, String d, Integer size, Long page) {
        String query = "select distinct accounts.* from accounts\n" +
                "left join account_group_account_pvt\n" +
                "on accounts.uuid = account_group_account_pvt.account_uuid\n" +
                "left join voucher_account_group_pvt\n" +
                "on account_group_account_pvt.account_group_uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and accounts.company_uuid = '" + companyUUID +
                "' and accounts.deleted_at is null\n" +
                " and accounts.status = " + status +
                " and account_group_account_pvt.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and (accounts.name ILIKE  '%" + name + "%' " +
                "or accounts.description ILIKE  '%" + description + "%' or accounts.code ILIKE '%" + code + "%')" +
                "order by accounts." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }
}
