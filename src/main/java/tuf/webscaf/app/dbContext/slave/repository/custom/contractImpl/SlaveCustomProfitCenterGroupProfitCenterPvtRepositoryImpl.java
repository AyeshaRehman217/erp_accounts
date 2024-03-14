package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterGroupProfitCenterPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomProfitCenterMapper;

import java.util.UUID;

public class SlaveCustomProfitCenterGroupProfitCenterPvtRepositoryImpl implements SlaveCustomProfitCenterGroupProfitCenterPvtRepository {
    private DatabaseClient client;
    private SlaveProfitCenterEntity slaveProfitCenterEntity;

    @Autowired
    public SlaveCustomProfitCenterGroupProfitCenterPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Profit Centers List Against Profit Centers Group List
    @Override
    public Flux<SlaveProfitCenterEntity> showUnMappedProfitCenterRecords(UUID profitCenterGroupUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "SELECT profit_centers.* FROM profit_centers\n" +
                "WHERE profit_centers.uuid NOT IN(\n" +
                "SELECT profit_centers.uuid FROM profit_centers\n" +
                "LEFT JOIN profit_center_group_profit_center_pvt\n" +
                "ON profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid\n" +
                "WHERE profit_center_group_profit_center_pvt.profit_center_group_uuid = '" + profitCenterGroupUUID +
                "' AND profit_center_group_profit_center_pvt.deleted_at IS NULL\n" +
                "AND profit_centers.deleted_at IS NULL)\n" +
                "AND profit_centers.deleted_at IS NULL " +
                "AND (profit_centers.name ILIKE '%" + name + "%' " +
                "OR profit_centers.description ILIKE '%" + description + "%') \n" +
                "ORDER BY profit_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showUnMappedProfitCenterRecordsWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT profit_centers.* FROM profit_centers\n" +
                "WHERE profit_centers.uuid NOT IN(\n" +
                "SELECT profit_centers.uuid FROM profit_centers\n" +
                "LEFT JOIN profit_center_group_profit_center_pvt\n" +
                "ON profit_center_group_profit_center_pvt.profit_center_uuid = profit_centers.uuid\n" +
                "WHERE profit_center_group_profit_center_pvt.profit_center_group_uuid = '" + profitCenterGroupUUID +
                "' AND profit_center_group_profit_center_pvt.deleted_at IS NULL\n" +
                "AND profit_centers.deleted_at IS NULL)\n" +
                "AND profit_centers.deleted_at IS NULL " +
                "AND profit_centers.status=" + status +
                " AND (profit_centers.name ILIKE '%" + name + "%' " +
                "OR profit_centers.description ILIKE '%" + description + "%') \n" +
                "ORDER BY profit_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showMappedProfitCenterRecords(UUID profitCenterGroupUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select profit_centers.* from profit_centers " +
                "join profit_center_group_profit_center_pvt " +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid " +
                "join profit_center_groups " +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = profit_center_groups.uuid " +
                "where profit_centers.deleted_at is null " +
                "and profit_center_groups.deleted_at is null " +
                "and profit_center_group_profit_center_pvt.deleted_at is null " +
                "and profit_center_groups.uuid ='" + profitCenterGroupUUID +
                "' and (profit_centers.name ilike  '%" + name + "%' OR " +
                "profit_centers.description ilike  '%" + description + "%') " +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showMappedProfitCenterRecordsWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select profit_centers.* from profit_centers " +
                "join profit_center_group_profit_center_pvt " +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid " +
                "join profit_center_groups " +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = profit_center_groups.uuid " +
                "where profit_centers.deleted_at is null " +
                "and profit_center_groups.deleted_at is null " +
                "and profit_center_group_profit_center_pvt.deleted_at is null " +
                "and profit_centers.status=" + status +
                " and profit_center_groups.uuid ='" + profitCenterGroupUUID +
                "' and (profit_centers.name ilike  '%" + name + "%' OR " +
                "profit_centers.description ilike  '%" + description + "%') " +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();
    }

}
