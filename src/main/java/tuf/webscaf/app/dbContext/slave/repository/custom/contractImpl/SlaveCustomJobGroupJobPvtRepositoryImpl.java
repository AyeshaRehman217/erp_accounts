package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomJobMapper;

import java.util.UUID;

public class SlaveCustomJobGroupJobPvtRepositoryImpl implements SlaveCustomJobGroupJobPvtRepository {
    private DatabaseClient client;
    private SlaveJobEntity slaveJobEntity;

    @Autowired
    public SlaveCustomJobGroupJobPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Jobs List Against Job Group List
    @Override
    public Flux<SlaveJobEntity> showUnMappedJobListAgainstJobGroup(UUID jobGroupUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "SELECT jobs.* FROM jobs\n" +
                "WHERE jobs.uuid NOT IN(\n" +
                "SELECT jobs.uuid FROM jobs\n" +
                "LEFT JOIN job_group_job_pvt\n" +
                "ON job_group_job_pvt.job_uuid = jobs.uuid\n" +
                "WHERE job_group_job_pvt.job_group_uuid = '" + jobGroupUUID +
                "' AND job_group_job_pvt.deleted_at IS NULL\n" +
                "AND jobs.deleted_at IS NULL)\n" +
                "AND jobs.deleted_at IS NULL " +
                "AND (jobs.name ILIKE '%" + name + "%' \n" +
                "AND jobs.description ILIKE '%" + description + "%' ) \n" +
                "ORDER BY jobs." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showUnMappedJobListAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT jobs.* FROM jobs\n" +
                "WHERE jobs.uuid NOT IN(\n" +
                "SELECT jobs.uuid FROM jobs\n" +
                "LEFT JOIN job_group_job_pvt\n" +
                "ON job_group_job_pvt.job_uuid = jobs.uuid\n" +
                "WHERE job_group_job_pvt.job_group_uuid = '" + jobGroupUUID +
                "' AND job_group_job_pvt.deleted_at IS NULL\n" +
                "AND jobs.deleted_at IS NULL)\n" +
                "AND jobs.deleted_at IS NULL " +
                "AND jobs.status= " + status +
                " AND (jobs.name ILIKE '%" + name + "%' \n" +
                "AND jobs.description ILIKE '%" + description + "%' ) \n" +
                "ORDER BY jobs." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        Flux<SlaveJobEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveJobEntity> showMappedJobListAgainstJobGroup(UUID jobGroupUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select jobs.* from jobs " +
                "join job_group_job_pvt on jobs.uuid = job_group_job_pvt.job_uuid " +
                "join job_groups on job_group_job_pvt.job_group_uuid = job_groups.uuid " +
                "where jobs.deleted_at is null " +
                "and job_groups.deleted_at is null " +
                "and job_group_job_pvt.deleted_at is null " +
                "and job_groups.uuid ='" + jobGroupUUID +
                "' and (jobs.name ilike  '%" + name + "%' " +
                "and jobs.description ilike  '%" + description + "%') " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();
    }

    @Override
    public Flux<SlaveJobEntity> showMappedJobListAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select jobs.* from jobs " +
                "join job_group_job_pvt on jobs.uuid = job_group_job_pvt.job_uuid " +
                "join job_groups on job_group_job_pvt.job_group_uuid = job_groups.uuid " +
                "where jobs.deleted_at is null " +
                "and job_groups.deleted_at is null " +
                "and job_group_job_pvt.deleted_at is null " +
                "and jobs.status=" + status +
                " and job_groups.uuid ='" + jobGroupUUID +
                "' and (jobs.name ilike  '%" + name + "%' " +
                "and jobs.description ilike  '%" + description + "%') " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobMapper mapper = new SlaveCustomJobMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveJobEntity))
                .all();
    }
}
