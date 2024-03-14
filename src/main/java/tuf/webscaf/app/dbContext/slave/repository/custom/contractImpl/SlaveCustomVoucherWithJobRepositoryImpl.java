package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithJobRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomJobMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithJobRepositoryImpl implements SlaveCustomVoucherWithJobRepository {
    private DatabaseClient client;
    private SlaveJobEntity slaveJobEntity;

    @Autowired
    public SlaveCustomVoucherWithJobRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveJobEntity> indexJob(String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT jobs.* FROM jobs\n" +
                "WHERE jobs.deleted_at IS NULL\n" +
                "AND (jobs.name ILIKE  '%" + name + "%' " +
                "OR jobs.description ILIKE  '%" + description + "%' )\n" +
                "ORDER BY jobs." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> indexJobWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT jobs.* FROM jobs\n" +
                "WHERE jobs.deleted_at IS NULL\n" +
                "AND (jobs.name ILIKE  '%" + name + "%' " +
                "OR jobs.description ILIKE  '%" + description + "%' )\n" +
                "AND jobs.status=" + status +
                " ORDER BY jobs." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showJobListDataAgainstVoucher(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt \n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt \n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid \n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveJobEntity> showJobListWithCompanyAgainstVoucher(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt\n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt\n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.company_uuid = '" + companyUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showJobListWithBranchAgainstVoucher(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt\n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt\n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.branch_uuid = '" + branchUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showJobListDataAgainstVoucherWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt \n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt \n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid \n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and jobs.status=" + status +
                " and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showJobListWithCompanyAgainstVoucherWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt\n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt\n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.company_uuid = '" + companyUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and jobs.status=" + status +
                " and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showJobListWithBranchAgainstVoucherWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select distinct jobs.* from jobs\n" +
                "left join job_group_job_pvt\n" +
                "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
                "left join voucher_job_group_pvt\n" +
                "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
                "where voucher_job_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and jobs.branch_uuid = '" + branchUUID +
                "' and jobs.deleted_at is null\n" +
                "and job_group_job_pvt.deleted_at is null\n" +
                "and voucher_job_group_pvt.deleted_at is null\n" +
                "and jobs.status=" + status +
                " and (jobs.name ILIKE  '%" + name + "%' " +
                "or jobs.description ILIKE  '%" + description + "%' )" +
                "order by jobs." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

}
