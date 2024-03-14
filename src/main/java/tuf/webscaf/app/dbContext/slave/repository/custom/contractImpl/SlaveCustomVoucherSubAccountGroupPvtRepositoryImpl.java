package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherSubAccountGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomSubAccountGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherSubAccountGroupPvtRepositoryImpl implements SlaveCustomVoucherSubAccountGroupPvtRepository {
    private DatabaseClient client;
    private SlaveSubAccountGroupEntity slaveSubAccountGroupEntity;

    @Autowired
    public SlaveCustomVoucherSubAccountGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveSubAccountGroupEntity> showUnMappedSubAccountGroupList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "SELECT sub_account_groups.* FROM sub_account_groups\n" +
                "WHERE sub_account_groups.uuid NOT IN(\n" +
                "SELECT sub_account_groups.uuid FROM sub_account_groups\n" +
                "LEFT JOIN voucher_sub_account_groups_pvt\n" +
                "ON voucher_sub_account_groups_pvt.sub_account_group_uuid = sub_account_groups.uuid\n" +
                "WHERE voucher_sub_account_groups_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_sub_account_groups_pvt.deleted_at IS NULL\n" +
                "AND sub_account_groups.deleted_at IS NULL)\n" +
                "AND sub_account_groups.name ILIKE '%" + name + "%'" +
                "AND sub_account_groups.deleted_at IS NULL " +
                "ORDER BY sub_account_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomSubAccountGroupMapper mapper = new SlaveCustomSubAccountGroupMapper();

        Flux<SlaveSubAccountGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAccountGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveSubAccountGroupEntity> showUnMappedSubAccountGroupListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT sub_account_groups.* FROM sub_account_groups\n" +
                "WHERE sub_account_groups.uuid NOT IN(\n" +
                "SELECT sub_account_groups.uuid FROM sub_account_groups\n" +
                "LEFT JOIN voucher_sub_account_groups_pvt\n" +
                "ON voucher_sub_account_groups_pvt.sub_account_group_uuid = sub_account_groups.uuid\n" +
                "WHERE voucher_sub_account_groups_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_sub_account_groups_pvt.deleted_at IS NULL\n" +
                "AND sub_account_groups.deleted_at IS NULL)\n" +
                "AND sub_account_groups.name ILIKE '%" + name + "%'" +
                "AND sub_account_groups.status= " + status +
                " AND sub_account_groups.deleted_at IS NULL " +
                "ORDER BY sub_account_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomSubAccountGroupMapper mapper = new SlaveCustomSubAccountGroupMapper();

        Flux<SlaveSubAccountGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAccountGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveSubAccountGroupEntity> showMappedSubAccountGroups(UUID voucherUUID, String name, String description,Integer size, Long page, String dp, String d) {

        String query = "select sub_account_groups.* from sub_account_groups\n" +
                "left join voucher_sub_account_groups_pvt \n" +
                "on sub_account_groups.uuid = voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
                "where voucher_sub_account_groups_pvt.voucher_uuid = '" + voucherUUID +
                "' and sub_account_groups.deleted_at is null\n" +
                "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
                "and sub_account_groups.name ILIKE  '%" + name + "%' " +
                "order by sub_account_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomSubAccountGroupMapper mapper = new SlaveCustomSubAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAccountGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveSubAccountGroupEntity> showMappedSubAccountGroupsWithStatus(UUID voucherUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {

        String query = "select sub_account_groups.* from sub_account_groups\n" +
                "left join voucher_sub_account_groups_pvt \n" +
                "on sub_account_groups.uuid = voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
                "where voucher_sub_account_groups_pvt.voucher_uuid = '" + voucherUUID +
                "' and sub_account_groups.status = " + status +
                " and sub_account_groups.deleted_at is null\n" +
                "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
                "and sub_account_groups.name ILIKE  '%" + name + "%' " +
                "order by sub_account_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomSubAccountGroupMapper mapper = new SlaveCustomSubAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAccountGroupEntity))
                .all();
    }

}
