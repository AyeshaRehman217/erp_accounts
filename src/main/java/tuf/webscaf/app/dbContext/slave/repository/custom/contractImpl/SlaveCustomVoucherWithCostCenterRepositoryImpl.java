package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithCostCenterRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCostCenterMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithCostCenterRepositoryImpl implements SlaveCustomVoucherWithCostCenterRepository {
    private DatabaseClient client;
    private SlaveCostCenterEntity slaveCostCenterEntity;

    @Autowired
    public SlaveCustomVoucherWithCostCenterRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCostCenterEntity> indexCostCenter(String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT cost_centers.* FROM cost_centers\n" +
                "WHERE cost_centers.deleted_at IS NULL\n" +
                "AND (cost_centers.name ILIKE  '%" + name + "%' " +
                "OR cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "ORDER BY cost_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> indexCostCenterWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT cost_centers.* FROM cost_centers\n" +
                "WHERE cost_centers.deleted_at IS NULL\n" +
                "AND (cost_centers.name ILIKE  '%" + name + "%' " +
                "OR cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "AND cost_centers.status=" + status +
                " ORDER BY cost_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.company_uuid = '" + companyUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.branch_uuid = '" + branchUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "and cost_centers.status=" + status +
                " order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.company_uuid = '" + companyUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "and cost_centers.status=" + status +
                " order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showCostCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "select distinct cost_centers.* from cost_centers\n" +
                "left join cost_center_group_cost_center_pvt\n" +
                "on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid\n" +
                "left join voucher_cost_center_group_pvt\n" +
                "on cost_center_group_cost_center_pvt.cost_center_group_uuid = voucher_cost_center_group_pvt.cost_center_group_uuid\n" +
                "where voucher_cost_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and cost_centers.branch_uuid = '" + branchUUID +
                "' and cost_centers.deleted_at is null\n" +
                "and cost_center_group_cost_center_pvt.deleted_at is null\n" +
                "and voucher_cost_center_group_pvt.deleted_at is null\n" +
                "and (cost_centers.name ILIKE  '%" + name + "%' " +
                "or cost_centers.description ILIKE  '%" + description + "%' )\n" +
                "and cost_centers.status=" + status +
                " order by cost_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }
}
