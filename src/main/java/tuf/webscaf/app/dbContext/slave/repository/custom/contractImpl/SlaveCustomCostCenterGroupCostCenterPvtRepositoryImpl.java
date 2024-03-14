package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCostCenterGroupCostCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCostCenterMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomProfitCenterMapper;

import java.util.UUID;

public class SlaveCustomCostCenterGroupCostCenterPvtRepositoryImpl implements SlaveCustomCostCenterGroupCostCenterPvtRepository {
    private DatabaseClient client;
    private SlaveCostCenterEntity slaveCostCenterEntity;

    @Autowired
    public SlaveCustomCostCenterGroupCostCenterPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Cost Centers List Against Cost Center Groups UUID
    @Override
    public Flux<SlaveCostCenterEntity> showUnMappedCostCenterRecords(UUID costCenterGroupUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "SELECT cost_centers.* FROM cost_centers\n" +
                "WHERE cost_centers.uuid NOT IN(\n" +
                "SELECT cost_centers.uuid FROM cost_centers\n" +
                "LEFT JOIN cost_center_group_cost_center_pvt\n" +
                "ON cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid\n" +
                "WHERE cost_center_group_cost_center_pvt.cost_center_group_uuid = '" + costCenterGroupUUID +
                "' AND cost_center_group_cost_center_pvt.deleted_at IS NULL\n" +
                "AND cost_centers.deleted_at IS NULL)\n" +
                "AND cost_centers.deleted_at IS NULL " +
                "AND (cost_centers.name ILIKE '%" + name + "%' \n" +
                "OR cost_centers.description ILIKE '%" + description + "%' ) \n" +
                "ORDER BY cost_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showUnMappedCostCenterRecordsWithStatus(UUID costCenterGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT cost_centers.* FROM cost_centers\n" +
                "WHERE cost_centers.uuid NOT IN(\n" +
                "SELECT cost_centers.uuid FROM cost_centers\n" +
                "LEFT JOIN cost_center_group_cost_center_pvt\n" +
                "ON cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid\n" +
                "WHERE cost_center_group_cost_center_pvt.cost_center_group_uuid = '" + costCenterGroupUUID +
                "' AND cost_center_group_cost_center_pvt.deleted_at IS NULL\n" +
                "AND cost_centers.deleted_at IS NULL)\n" +
                "AND cost_centers.deleted_at IS NULL " +
                "AND cost_centers.status=" + status +
                " AND (cost_centers.name ILIKE '%" + name + "%' \n" +
                "OR cost_centers.description ILIKE '%" + description + "%' ) \n" +
                "ORDER BY cost_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        Flux<SlaveCostCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCostCenterEntity> showMappedCostCenterRecords(UUID costCenterGroupUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select cost_centers.* from cost_centers " +
                "join cost_center_group_cost_center_pvt on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid " +
                "join cost_center_groups on cost_center_group_cost_center_pvt.cost_center_group_uuid = cost_center_groups.uuid " +
                "where cost_centers.deleted_at is null " +
                "and cost_center_groups.deleted_at is null " +
                "and cost_center_group_cost_center_pvt.deleted_at is null " +
                "and cost_center_groups.uuid ='" + costCenterGroupUUID +
                "' and (cost_centers.name ilike  '%" + name + "%'" +
                "or cost_centers.description ilike  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();
    }

    @Override
    public Flux<SlaveCostCenterEntity> showMappedCostCenterRecordsWithStatus(UUID costCenterGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select cost_centers.* from cost_centers " +
                "join cost_center_group_cost_center_pvt on cost_centers.uuid = cost_center_group_cost_center_pvt.cost_center_uuid " +
                "join cost_center_groups on cost_center_group_cost_center_pvt.cost_center_group_uuid = cost_center_groups.uuid " +
                "where cost_centers.deleted_at is null " +
                "and cost_center_groups.deleted_at is null " +
                "and cost_center_group_cost_center_pvt.deleted_at is null " +
                "and cost_centers.status=" + status +
                " and cost_center_groups.uuid ='" + costCenterGroupUUID +
                "' and (cost_centers.name ilike  '%" + name + "%'" +
                "or cost_centers.description ilike  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCostCenterMapper mapper = new SlaveCustomCostCenterMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterEntity))
                .all();
    }
}
