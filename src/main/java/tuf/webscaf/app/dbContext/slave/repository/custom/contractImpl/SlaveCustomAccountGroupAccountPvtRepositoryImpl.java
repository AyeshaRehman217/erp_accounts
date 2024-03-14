package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomAccountGroupAccountPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountMapper;

import java.util.UUID;

public class SlaveCustomAccountGroupAccountPvtRepositoryImpl implements SlaveCustomAccountGroupAccountPvtRepository {
    private DatabaseClient client;
    private SlaveAccountEntity slaveAccountEntity;

    @Autowired
    public SlaveCustomAccountGroupAccountPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Account List Against Account Group Id
    @Override
    public Flux<SlaveAccountEntity> showUnMappedAccountList(UUID accountGroupUUID, String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page) {

        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.uuid NOT IN(\n" +
                "SELECT accounts.uuid FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON account_group_account_pvt.account_uuid = accounts.uuid\n" +
                "WHERE account_group_account_pvt.account_group_uuid = '" + accountGroupUUID +
                "' AND account_group_account_pvt.deleted_at IS NULL\n" +
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
    public Flux<SlaveAccountEntity> showUnMappedAccountListWithStatus(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT accounts.* FROM accounts\n" +
                "WHERE accounts.uuid NOT IN(\n" +
                "SELECT accounts.uuid FROM accounts\n" +
                "LEFT JOIN account_group_account_pvt\n" +
                "ON account_group_account_pvt.account_uuid = accounts.uuid\n" +
                "WHERE account_group_account_pvt.account_group_uuid = '" + accountGroupUUID +
                "' AND account_group_account_pvt.deleted_at IS NULL\n" +
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
    public Flux<SlaveAccountEntity> showMappedAccountAgainstAccountGroup(UUID accountGroupUUID, String name, String description, String code, String controlCode, Integer size, Long page, String dp, String d) {

        String query = "select accounts.* from accounts " +
                "join account_group_account_pvt on accounts.uuid = account_group_account_pvt.account_uuid " +
                "join account_groups on account_group_account_pvt.account_group_uuid = account_groups.uuid " +
                " and accounts.deleted_at is null " +
                "and account_groups.deleted_at is null " +
                "and account_group_account_pvt.deleted_at is null " +
                "and account_groups.uuid ='" + accountGroupUUID +
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
    public Flux<SlaveAccountEntity> showMappedAccountAgainstAccountGroupWithStatus(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select accounts.* from accounts " +
                "join account_group_account_pvt on accounts.uuid = account_group_account_pvt.account_uuid " +
                "join account_groups on account_group_account_pvt.account_group_uuid = account_groups.uuid " +
                "where accounts.status = " + status +
                " and accounts.deleted_at is null " +
                "and account_groups.deleted_at is null " +
                "and account_group_account_pvt.deleted_at is null " +
                "and account_groups.uuid ='" + accountGroupUUID +
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
