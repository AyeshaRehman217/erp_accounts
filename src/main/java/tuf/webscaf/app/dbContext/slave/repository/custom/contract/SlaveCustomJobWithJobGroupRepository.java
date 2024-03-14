package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;

import java.util.UUID;


public interface SlaveCustomJobWithJobGroupRepository {

    Flux<SlaveJobGroupEntity> listOfJobGroupsAgainstJob(UUID jobUUID, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveJobGroupEntity> listOfJobGroupsAgainstJobWithStatus(UUID jobUUID, String name, Boolean status, Integer size, Long page, String dp, String d);
}
