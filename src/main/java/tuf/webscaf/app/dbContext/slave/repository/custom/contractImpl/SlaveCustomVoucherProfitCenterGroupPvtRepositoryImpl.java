package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherProfitCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomProfitCenterGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherProfitCenterGroupPvtRepositoryImpl implements SlaveCustomVoucherProfitCenterGroupPvtRepository {
    private DatabaseClient client;
    private SlaveProfitCenterGroupEntity slaveProfitCenterGroupEntity;

    @Autowired
    public SlaveCustomVoucherProfitCenterGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveProfitCenterGroupEntity> showUnMappedProfitCenterGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT profit_center_groups.* FROM profit_center_groups\n" +
                "WHERE profit_center_groups.uuid NOT IN(\n" +
                "SELECT profit_center_groups.uuid FROM profit_center_groups\n" +
                "LEFT JOIN voucher_profit_center_group_pvt\n" +
                "ON voucher_profit_center_group_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
                "WHERE voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_profit_center_group_pvt.deleted_at IS NULL\n" +
                "AND profit_center_groups.deleted_at IS NULL) \n" +
                "AND profit_center_groups.name ILIKE '%" + name + "%'" +
                "AND profit_center_groups.deleted_at IS NULL " +
                "ORDER BY profit_center_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        Flux<SlaveProfitCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterGroupEntity> showUnMappedProfitCenterGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT profit_center_groups.* FROM profit_center_groups\n" +
                "WHERE profit_center_groups.uuid NOT IN(\n" +
                "SELECT profit_center_groups.uuid FROM profit_center_groups\n" +
                "LEFT JOIN voucher_profit_center_group_pvt\n" +
                "ON voucher_profit_center_group_pvt.profit_center_group_uuid = profit_center_groups.uuid\n" +
                "WHERE voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_profit_center_group_pvt.deleted_at IS NULL\n" +
                "AND profit_center_groups.deleted_at IS NULL) \n" +
                "AND profit_center_groups.name ILIKE '%" + name + "%'" +
                "AND profit_center_groups.deleted_at IS NULL " +
                "AND profit_center_groups.status=" + status +
                " ORDER BY profit_center_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        Flux<SlaveProfitCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterGroupEntity> showMappedProfitCenterGroups(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select profit_center_groups.* from profit_center_groups\n" +
                "left join voucher_profit_center_group_pvt \n" +
                "on profit_center_groups.uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_center_groups.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and profit_center_groups.name ILIKE  '%" + name + "%' " +
                "order by profit_center_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        Flux<SlaveProfitCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterGroupEntity> showMappedProfitCenterGroupsWithStatus(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select profit_center_groups.* from profit_center_groups\n" +
                "left join voucher_profit_center_group_pvt \n" +
                "on profit_center_groups.uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_center_groups.status = " + status +
                " and profit_center_groups.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and profit_center_groups.name ILIKE  '%" + name + "%' " +
                "order by profit_center_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        Flux<SlaveProfitCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();

        return result;
    }
//
//    @Override
//    public Flux<SlaveProfitCenterGroupEntity> countExistingProfitCenterGroupList(Long voucherId, String name, String dp, String d) {
//        String query = "SELECT profit_center_groups.* FROM profit_center_groups\n" +
//                "WHERE profit_center_groups.id NOT IN(\n" +
//                "SELECT profit_center_groups.id FROM profit_center_groups\n" +
//                "LEFT JOIN voucher_profit_center_group_pvt\n" +
//                "ON voucher_profit_center_group_pvt.profit_center_group_id = profit_center_groups.id\n" +
//                "WHERE voucher_profit_center_group_pvt.voucher_id = " + voucherId +
//                " AND voucher_profit_center_group_pvt.deleted_at IS NULL\n" +
//                "AND profit_center_groups.deleted_at IS NULL)\n" +
//                "AND profit_center_groups.name ILIKE '%" + name + "%'" +
//                "AND profit_center_groups.deleted_at IS NULL " +
//                "ORDER BY profit_center_groups." + dp + " " + d;
//
//        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();
//
//        Flux<SlaveProfitCenterGroupEntity> result = client.sql(query)
//                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
//                .all();
//
//        return result;
//    }
}
