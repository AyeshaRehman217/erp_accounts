package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobGroupWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherMapper;

import java.util.UUID;

public class SlaveCustomJobGroupWithVoucherRepositoryImpl implements SlaveCustomJobGroupWithVoucherRepository {
    private DatabaseClient client;
    private SlaveVoucherEntity slaveVoucherEntity;

    @Autowired
    public SlaveCustomJobGroupWithVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstJobGroup(UUID jobGroupUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_job_group_pvt \n" +
                "on vouchers.uuid = voucher_job_group_pvt.voucher_uuid\n" +
                "where voucher_job_group_pvt.job_group_uuid = '" + jobGroupUUID +
                "' and vouchers.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
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
    public Flux<SlaveVoucherEntity> showMappedVouchersWithStatusAgainstJobGroup(UUID jobGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_job_group_pvt \n" +
                "on vouchers.uuid = voucher_job_group_pvt.voucher_uuid\n" +
                "where voucher_job_group_pvt.job_group_uuid = '" + jobGroupUUID +
                "' and vouchers.status = " + status +
                " and vouchers.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
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
