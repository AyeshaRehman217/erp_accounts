package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterGroupWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherMapper;

import java.util.UUID;

public class SlaveCustomProfitCenterGroupWithVoucherRepositoryImpl implements SlaveCustomProfitCenterGroupWithVoucherRepository {
    private DatabaseClient client;
    private SlaveVoucherEntity slaveVoucherEntity;

    @Autowired
    public SlaveCustomProfitCenterGroupWithVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstProfitCenterGroup(UUID profitCenterGroupUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_profit_center_group_pvt \n" +
                "on vouchers.uuid = voucher_profit_center_group_pvt.voucher_uuid\n" +
                "where voucher_profit_center_group_pvt.profit_center_group_uuid = " + profitCenterGroupUUID +
                " and vouchers.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE  '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;
        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstProfitCenterGroupWithStatus(UUID profitCenterGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_profit_center_group_pvt \n" +
                "on vouchers.uuid = voucher_profit_center_group_pvt.voucher_uuid\n" +
                "where voucher_profit_center_group_pvt.profit_center_group_uuid = " + profitCenterGroupUUID +
                " and vouchers.status = " + status +
                " and vouchers.deleted_at is null\n" +
                "and voucher_profit_center_group_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE  '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;
        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }
}
