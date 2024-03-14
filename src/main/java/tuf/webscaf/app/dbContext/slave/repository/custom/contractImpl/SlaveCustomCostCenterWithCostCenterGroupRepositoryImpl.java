package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCostCenterWithCostCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCostCenterGroupMapper;

import java.util.UUID;


public class SlaveCustomCostCenterWithCostCenterGroupRepositoryImpl implements SlaveCustomCostCenterWithCostCenterGroupRepository {
    SlaveCostCenterGroupEntity slaveCostCenterGroupEntity;
    private DatabaseClient client;

    @Autowired
    public SlaveCustomCostCenterWithCostCenterGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> listOfCostCenterGroupsAgainstCostCenter(UUID costCenterUUID, String name, Integer size, Long page, String dp, String d) {
        String query = "select cost_center_groups.* from cost_center_groups " +
                "join cost_center_group_cost_center_pvt on cost_center_groups.uuid = cost_center_group_cost_center_pvt.cost_center_group_uuid " +
                "join cost_centers on cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid " +
                "where cost_centers.deleted_at is null " +
                "and cost_center_groups.deleted_at is null " +
                "and cost_center_group_cost_center_pvt.deleted_at is null " +
                "and cost_centers.uuid ='" + costCenterUUID +
                "' and cost_center_groups.name ilike  '%" + name + "%' " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveCostCenterGroupEntity> listOfCostCenterGroupsAgainstCostCenterWithStatus(UUID costCenterUUID, String name, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select cost_center_groups.* from cost_center_groups " +
                "join cost_center_group_cost_center_pvt on cost_center_groups.uuid = cost_center_group_cost_center_pvt.cost_center_group_uuid " +
                "join cost_centers on cost_center_group_cost_center_pvt.cost_center_uuid = cost_centers.uuid " +
                "where cost_centers.deleted_at is null " +
                "and cost_center_groups.deleted_at is null " +
                "and cost_center_group_cost_center_pvt.deleted_at is null " +
                "and cost_center_groups.status= " + status +
                " and cost_centers.uuid ='" + costCenterUUID +
                "' and cost_center_groups.name ilike  '%" + name + "%' " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCostCenterGroupMapper mapper = new SlaveCustomCostCenterGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCostCenterGroupEntity))
                .all();
    }

}

