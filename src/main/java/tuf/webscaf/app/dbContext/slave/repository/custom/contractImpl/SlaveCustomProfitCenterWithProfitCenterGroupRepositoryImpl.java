package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterWithProfitCenterGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomProfitCenterGroupMapper;

import java.util.UUID;


public class SlaveCustomProfitCenterWithProfitCenterGroupRepositoryImpl implements SlaveCustomProfitCenterWithProfitCenterGroupRepository {
    SlaveProfitCenterGroupEntity slaveProfitCenterGroupEntity;
    private DatabaseClient client;

    @Autowired
    public SlaveCustomProfitCenterWithProfitCenterGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    @Override
    public Flux<SlaveProfitCenterGroupEntity> listOfProfitCenterGroups(UUID profitCenterUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select profit_center_groups.* from profit_center_groups " +
                "join profit_center_group_profit_center_pvt " +
                "on profit_center_groups.uuid = profit_center_group_profit_center_pvt.profit_center_group_uuid " +
                "join profit_centers on profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid " +
                "where profit_centers.deleted_at is null " +
                "and profit_center_groups.deleted_at is null " +
                "and profit_center_group_profit_center_pvt.deleted_at is null " +
                "and profit_centers.uuid ='" + profitCenterUUID +
                "' and (profit_center_groups.name ilike  '%" + name + "%' " +
                "or profit_center_groups.description ilike '%" + description + "%') " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveProfitCenterGroupEntity> listOfProfitCenterGroupsWithStatus(UUID profitCenterUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select profit_center_groups.* from profit_center_groups " +
                "join profit_center_group_profit_center_pvt " +
                "on profit_center_groups.uuid = profit_center_group_profit_center_pvt.profit_center_group_uuid " +
                "join profit_centers on profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid " +
                "where profit_centers.deleted_at is null " +
                "and profit_center_groups.deleted_at is null " +
                "and profit_center_group_profit_center_pvt.deleted_at is null " +
                "and profit_center_groups.status=" + status +
                " and profit_centers.uuid ='" + profitCenterUUID +
                "' and (profit_center_groups.name ilike  '%" + name + "%' " +
                "or profit_center_groups.description ilike '%" + description + "%') " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterGroupMapper mapper = new SlaveCustomProfitCenterGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterGroupEntity))
                .all();
    }


}

