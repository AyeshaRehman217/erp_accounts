package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithProfitCenterRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomProfitCenterMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithProfitCenterRepositoryImpl implements SlaveCustomVoucherWithProfitCenterRepository {
    private DatabaseClient client;
    private SlaveProfitCenterEntity slaveProfitCenterEntity;

    @Autowired
    public SlaveCustomVoucherWithProfitCenterRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveProfitCenterEntity> indexProfitCenter(String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT profit_centers.* FROM profit_centers\n" +
                "WHERE profit_centers.deleted_at IS NULL\n" +
                "AND (profit_centers.name ILIKE  '%" + name + "%' " +
                "OR profit_centers.description ILIKE  '%" + description + "%' )\n" +
                "ORDER BY profit_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> indexProfitCenterWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT profit_centers.* FROM profit_centers\n" +
                "WHERE profit_centers.deleted_at IS NULL\n" +
                "AND (profit_centers.name ILIKE  '%" + name + "%' " +
                "OR profit_centers.description ILIKE  '%" + description + "%' )\n" +
                "AND profit_centers.status=" + status +
                " ORDER BY profit_centers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and ( profit_centers.name ILIKE  '%" + name + "%' OR " +
                " profit_centers.description ILIKE '%" + description + "%' ) " +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.company_uuid = '" + companyUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and (profit_centers.name ILIKE  '%" + name + "%' " +
                "or profit_centers.description ILIKE  '%" + description + "%' )" +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.branch_uuid = '" + branchUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and (profit_centers.name ILIKE  '%" + name + "%' " +
                "or profit_centers.description ILIKE  '%" + description + "%' )" +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and profit_centers.status=" + status +
                " and ( profit_centers.name ILIKE  '%" + name + "%' OR " +
                " profit_centers.description ILIKE '%" + description + "%' ) " +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.company_uuid = '" + companyUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and profit_centers.status=" + status +
                " and (profit_centers.name ILIKE  '%" + name + "%' " +
                "or profit_centers.description ILIKE  '%" + description + "%' )" +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveProfitCenterEntity> showProfitCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct profit_centers.* from profit_centers\n" +
                "left join profit_center_group_profit_center_pvt\n" +
                "on profit_centers.uuid = profit_center_group_profit_center_pvt.profit_center_uuid\n" +
                "left join voucher_profit_center_group_pvt\n" +
                "on profit_center_group_profit_center_pvt.profit_center_group_uuid = voucher_profit_center_group_pvt.profit_center_group_uuid\n" +
                "where voucher_profit_center_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and profit_centers.branch_uuid = '" + branchUUID +
                "' and profit_centers.deleted_at is null\n" +
                "and profit_center_group_profit_center_pvt.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and profit_centers.status=" + status +
                " and (profit_centers.name ILIKE  '%" + name + "%' " +
                "or profit_centers.description ILIKE  '%" + description + "%' )" +
                "order by profit_centers." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomProfitCenterMapper mapper = new SlaveCustomProfitCenterMapper();

        Flux<SlaveProfitCenterEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveProfitCenterEntity))
                .all();

        return result;
    }
}
