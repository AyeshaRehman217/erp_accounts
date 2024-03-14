package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomSubAccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountMapper;

import java.util.UUID;

public class SlaveCustomSubAccountGroupAccountPvtRepositoryImpl implements SlaveCustomSubAccountGroupAccountPvtRepository {
    private DatabaseClient client;
    private SlaveAccountEntity slaveAccountEntity;

    @Autowired
    public SlaveCustomSubAccountGroupAccountPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Account List Against Account Group Id
    @Override
    public Flux<SlaveAccountEntity> showUnMappedAccountAgainstSubAccountGroup(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page) {

        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.uuid NOT IN(\n" +
                "SELECT accounts.uuid FROM accounts\n" +
                "LEFT JOIN sub_account_group_accounts_pvt\n" +
                "ON sub_account_group_accounts_pvt.account_uuid = accounts.uuid\n" +
                "WHERE sub_account_group_accounts_pvt.sub_account_group_uuid = '" + subAccountGroupUUID +
                "' AND sub_account_group_accounts_pvt.deleted_at IS NULL\n" +
                "AND accounts.deleted_at IS NULL) \n" +
                "AND accounts.deleted_at IS NULL " +
                "AND (accounts.name ILIKE '%" + name + "%' " +
                "OR accounts.description ILIKE '%" + description + "%' " +
                "OR accounts.code ILIKE '%" + code + "%' " +
                "OR accounts.control_code ILIKE '%" + controlCode + "%') \n" +
                "ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showUnMappedAccountAgainstSubAccountGroup(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.uuid NOT IN(\n" +
                "SELECT accounts.uuid FROM accounts\n" +
                "LEFT JOIN sub_account_group_accounts_pvt\n" +
                "ON sub_account_group_accounts_pvt.account_uuid = accounts.uuid\n" +
                "WHERE sub_account_group_accounts_pvt.sub_account_group_uuid = '" + subAccountGroupUUID +
                "' AND sub_account_group_accounts_pvt.deleted_at IS NULL\n" +
                "AND accounts.deleted_at IS NULL) \n" +
                "AND accounts.deleted_at IS NULL " +
                "AND (accounts.name ILIKE '%" + name + "%' " +
                "OR accounts.description ILIKE '%" + description + "%' " +
                "OR accounts.code ILIKE '%" + code + "%' " +
                "OR accounts.control_code ILIKE '%" + controlCode + "%') \n" +
                "AND accounts.status =" + status +
                " ORDER BY accounts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        Flux<SlaveAccountEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountEntity> showMappedAccountAgainstSubAccountGroup(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, Integer size, Long page, String dp, String d) {

        String query = "select accounts.* from accounts " +
                "join sub_account_group_accounts_pvt on accounts.uuid = sub_account_group_accounts_pvt.account_uuid " +
                "join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid = sub_account_groups.uuid " +
                " and accounts.deleted_at is null " +
                "and sub_account_groups.deleted_at is null " +
                "and sub_account_group_accounts_pvt.deleted_at is null " +
                "and sub_account_groups.uuid ='" + subAccountGroupUUID +
                "' AND (accounts.name ILIKE '%" + name + "%' " +
                "OR accounts.description ILIKE '%" + description + "%' " +
                "OR accounts.code ILIKE '%" + code + "%' " +
                "OR accounts.control_code ILIKE '%" + controlCode + "%') \n" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();
    }

    @Override
    public Flux<SlaveAccountEntity> showMappedAccountAgainstSubAccountGroupWithStatus(UUID subAccountGroupUUID, String name, String description, String code, String controlCode, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select accounts.* from accounts " +
                "join sub_account_group_accounts_pvt on accounts.uuid = sub_account_group_accounts_pvt.account_uuid " +
                "join sub_account_groups on sub_account_group_accounts_pvt.sub_account_group_uuid = sub_account_groups.uuid " +
                "where accounts.status = " + status +
                " and accounts.deleted_at is null " +
                "and sub_account_groups.deleted_at is null " +
                "and sub_account_group_accounts_pvt.deleted_at is null " +
                "and sub_account_groups.uuid ='" + subAccountGroupUUID +
                "' AND (accounts.name ILIKE '%" + name + "%' " +
                "OR accounts.description ILIKE '%" + description + "%' " +
                "OR accounts.code ILIKE '%" + code + "%' " +
                "OR accounts.control_code ILIKE '%" + controlCode + "%') \n" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomAccountMapper mapper = new SlaveCustomAccountMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountEntity))
                .all();
    }


}
