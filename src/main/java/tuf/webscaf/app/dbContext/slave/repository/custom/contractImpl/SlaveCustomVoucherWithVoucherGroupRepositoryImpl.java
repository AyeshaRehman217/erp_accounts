package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithVoucherGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithVoucherGroupRepositoryImpl implements SlaveCustomVoucherWithVoucherGroupRepository {
    private DatabaseClient client;
    private SlaveVoucherGroupEntity slaveVoucherEntity;

    @Autowired
    public SlaveCustomVoucherWithVoucherGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherGroupEntity> showVoucherGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {
        String query = "select voucher_groups.* from voucher_groups\n" +
                "left join voucher_group_voucher_pvt \n" +
                "on voucher_groups.uuid = voucher_group_voucher_pvt.voucher_group_uuid\n" +
                "where voucher_group_voucher_pvt.voucher_uuid = '" + voucherUUID +
                "' and voucher_groups.deleted_at is null\n" +
                "and voucher_group_voucher_pvt.deleted_at is null\n" +
                "and voucher_groups.name ILIKE '%" + name + "%' " +
                "order by voucher_groups." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherGroupMapper mapper = new SlaveCustomVoucherGroupMapper();

        Flux<SlaveVoucherGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherGroupEntity> showVoucherGroupListWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select voucher_groups.* from voucher_groups\n" +
                "left join voucher_group_voucher_pvt \n" +
                "on voucher_groups.uuid = voucher_group_voucher_pvt.voucher_group_uuid\n" +
                "where voucher_group_voucher_pvt.voucher_uuid = '" + voucherUUID +
                "' and voucher_groups.status =" + status +
                " and voucher_groups.deleted_at is null\n" +
                "and voucher_group_voucher_pvt.deleted_at is null\n" +
                "and voucher_groups.name ILIKE '%" + name + "%' " +
                "order by voucher_groups." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherGroupMapper mapper = new SlaveCustomVoucherGroupMapper();

        Flux<SlaveVoucherGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }
}
