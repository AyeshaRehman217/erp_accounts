package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherAccountGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherAccountGroupPvtRepositoryImpl implements SlaveCustomVoucherAccountGroupPvtRepository {
    private DatabaseClient client;
    private SlaveAccountGroupEntity slaveAccountGroupEntity;

    @Autowired
    public SlaveCustomVoucherAccountGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveAccountGroupEntity> showUnMappedAccountGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT account_groups.* FROM account_groups\n" +
                "WHERE account_groups.uuid NOT IN(\n" +
                "SELECT account_groups.uuid FROM account_groups\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON voucher_account_group_pvt.account_group_uuid = account_groups.uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND account_groups.deleted_at IS NULL)\n" +
                "AND account_groups.name ILIKE '%" + name + "%'" +
                "AND account_groups.deleted_at IS NULL " +
                "ORDER BY account_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        Flux<SlaveAccountGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountGroupEntity> showUnMappedAccountGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT account_groups.* FROM account_groups\n" +
                "WHERE account_groups.uuid NOT IN(\n" +
                "SELECT account_groups.uuid FROM account_groups\n" +
                "LEFT JOIN voucher_account_group_pvt\n" +
                "ON voucher_account_group_pvt.account_group_uuid = account_groups.uuid\n" +
                "WHERE voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_account_group_pvt.deleted_at IS NULL\n" +
                "AND account_groups.deleted_at IS NULL)\n" +
                "AND account_groups.name ILIKE '%" + name + "%'" +
                "AND account_groups.status= " + status +
                " AND account_groups.deleted_at IS NULL " +
                "ORDER BY account_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        Flux<SlaveAccountGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAccountGroupEntity> showMappedAccountGroups(UUID voucherUUID, String name, Integer size, Long page, String dp, String d) {

        String query = "select account_groups.* from account_groups\n" +
                "left join voucher_account_group_pvt \n" +
                "on account_groups.uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and account_groups.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and account_groups.name ILIKE  '%" + name + "%' " +
                "order by account_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveAccountGroupEntity> showMappedAccountGroupsWithStatus(UUID voucherUUID, Boolean status, String name, Integer size, Long page, String dp, String d) {

        String query = "select account_groups.* from account_groups\n" +
                "left join voucher_account_group_pvt \n" +
                "on account_groups.uuid = voucher_account_group_pvt.account_group_uuid\n" +
                "where voucher_account_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and account_groups.status = " + status +
                " and account_groups.deleted_at is null\n" +
                "and voucher_account_group_pvt.deleted_at is null\n" +
                "and account_groups.name ILIKE  '%" + name + "%' " +
                "order by account_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();
    }

}
