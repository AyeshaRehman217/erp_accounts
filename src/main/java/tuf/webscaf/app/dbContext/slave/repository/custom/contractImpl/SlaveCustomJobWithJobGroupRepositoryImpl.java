package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.master.entity.JobEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobWithJobGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomJobGroupMapper;

import java.util.UUID;


public class SlaveCustomJobWithJobGroupRepositoryImpl implements SlaveCustomJobWithJobGroupRepository {
    SlaveAccountEntity slaveAccountEntity;
    JobEntity jobEntity;
    private DatabaseClient client;

    @Autowired
    public SlaveCustomJobWithJobGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    @Override
    public Flux<SlaveJobGroupEntity> listOfJobGroupsAgainstJob(UUID jobUUID, String name, Integer size, Long page, String dp, String d) {
        String query = "select job_groups.* from job_groups " +
                "join job_group_job_pvt on job_groups.uuid = job_group_job_pvt.job_group_uuid " +
                "join jobs on job_group_job_pvt.job_uuid = jobs.uuid " +
                "where jobs.deleted_at is null " +
                "and job_groups.deleted_at is null " +
                "and job_group_job_pvt.deleted_at is null " +
                "and jobs.uuid ='" + jobUUID +
                "' and jobs.name ilike  '%" + name + "%' " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, jobEntity))
                .all();
    }

    @Override
    public Flux<SlaveJobGroupEntity> listOfJobGroupsAgainstJobWithStatus(UUID jobUUID, String name, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select job_groups.* from job_groups " +
                "join job_group_job_pvt on job_groups.uuid = job_group_job_pvt.job_group_uuid " +
                "join jobs on job_group_job_pvt.job_uuid = jobs.uuid " +
                "where jobs.deleted_at is null " +
                "and job_groups.deleted_at is null " +
                "and job_group_job_pvt.deleted_at is null " +
                "and jobs.uuid ='" + jobUUID +
                "' and jobs.status= "+ status +
                " and jobs.name ilike  '%" + name + "%' " +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomJobGroupMapper mapper = new SlaveCustomJobGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, jobEntity))
                .all();
    }
}

