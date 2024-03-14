package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherJobGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomJobGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherJobGroupPvtRepositoryImpl implements SlaveCustomVoucherJobGroupPvtRepository {
    private DatabaseClient client;
    private SlaveJobGroupEntity slaveJobGroupEntity;

    @Autowired
    public SlaveCustomVoucherJobGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveJobGroupEntity> showUnMappedJobGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT job_groups.* FROM job_groups\n" +
                "WHERE job_groups.uuid NOT IN(\n" +
                "SELECT job_groups.uuid FROM job_groups\n" +
                "LEFT JOIN voucher_job_group_pvt\n" +
                "ON voucher_job_group_pvt.job_group_uuid = job_groups.uuid\n" +
                "WHERE voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_job_group_pvt.deleted_at IS NULL\n" +
                "AND job_groups.deleted_at IS NULL) \n" +
                "AND job_groups.deleted_at IS NULL " +
                "AND job_groups.name ILIKE '%" + name + "%'" +
                "ORDER BY job_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        Flux<SlaveJobGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobGroupEntity> showUnMappedJobGroupListAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT job_groups.* FROM job_groups\n" +
                "WHERE job_groups.uuid NOT IN(\n" +
                "SELECT job_groups.uuid FROM job_groups\n" +
                "LEFT JOIN voucher_job_group_pvt\n" +
                "ON voucher_job_group_pvt.job_group_uuid = job_groups.uuid\n" +
                "WHERE voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_job_group_pvt.deleted_at IS NULL\n" +
                "AND job_groups.deleted_at IS NULL) \n" +
                "AND job_groups.deleted_at IS NULL " +
                "AND job_groups.status= " + status +
                " AND job_groups.name ILIKE '%" + name + "%'" +
                "ORDER BY job_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        Flux<SlaveJobGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobGroupEntity> showMappedJobGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select job_groups.* from job_groups\n" +
                "left join voucher_job_group_pvt \n" +
                "on job_groups.uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and job_groups.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and job_groups.name ILIKE  '%" + name + "%' " +
                "order by job_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;


        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        Flux<SlaveJobGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobGroupEntity> showMappedJobGroupListWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select job_groups.* from job_groups\n" +
                "left join voucher_job_group_pvt \n" +
                "on job_groups.uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and job_groups.status =" + status +
                " and job_groups.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and job_groups.name ILIKE  '%" + name + "%' " +
                "order by job_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;


        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        Flux<SlaveJobGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobGroupEntity))
                .all();

        return result;
    }
}
