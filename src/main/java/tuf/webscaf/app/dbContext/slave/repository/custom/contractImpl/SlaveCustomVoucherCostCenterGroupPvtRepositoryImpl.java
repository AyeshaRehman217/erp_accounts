package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCostCenterGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCostCenterGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherCostCenterGroupPvtRepositoryImpl implements SlaveCustomVoucherCostCenterGroupPvtRepository {
    private DatabaseClient client;
    private SlaveCostCenterGroupEntity slaveCostCenterGroupEntity;

    @Autowired
    public SlaveCustomVoucherCostCenterGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> showUnMappedCostCenterGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT cost_center_groups.* FROM cost_center_groups\n" +
                "WHERE cost_center_groups.uuid NOT IN(\n" +
                "SELECT cost_center_groups.uuid FROM cost_center_groups\n" +
                "LEFT JOIN voucher_cost_center_group_pvt\n" +
                "ON voucher_cost_center_group_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
                "WHERE voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_cost_center_group_pvt.deleted_at IS NULL\n" +
                "AND cost_center_groups.deleted_at IS NULL)\n" +
                "AND cost_center_groups.name ILIKE '%" + name + "%'" +
                "AND cost_center_groups.deleted_at IS NULL " +
                "ORDER BY cost_center_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        Flux<SlaveCostCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> showUnMappedCostCenterGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT cost_center_groups.* FROM cost_center_groups\n" +
                "WHERE cost_center_groups.uuid NOT IN(\n" +
                "SELECT cost_center_groups.uuid FROM cost_center_groups\n" +
                "LEFT JOIN voucher_cost_center_group_pvt\n" +
                "ON voucher_cost_center_group_pvt.cost_center_group_uuid = cost_center_groups.uuid\n" +
                "WHERE voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_cost_center_group_pvt.deleted_at IS NULL\n" +
                "AND cost_center_groups.deleted_at IS NULL)\n" +
                "AND cost_center_groups.name ILIKE '%" + name + "%'" +
                "AND cost_center_groups.deleted_at IS NULL " +
                "AND cost_center_groups.status= " + status +
                " ORDER BY cost_center_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        Flux<SlaveCostCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> showMappedCostCenterGroups(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select cost_center_groups.* from cost_center_groups\n" +
                "left join voucher_cost_center_group_pvt \n" +
                "on cost_center_groups.uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_center_groups.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and cost_center_groups.name ILIKE  '%" + name + "%' " +
                "order by cost_center_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        Flux<SlaveCostCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> showMappedCostCenterGroupsWithStatus(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select cost_center_groups.* from cost_center_groups\n" +
                "left join voucher_cost_center_group_pvt \n" +
                "on cost_center_groups.uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_center_groups.status = " + status +
                " and cost_center_groups.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and cost_center_groups.name ILIKE  '%" + name + "%' " +
                "order by cost_center_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        Flux<SlaveCostCenterGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();

        return result;
    }
}
