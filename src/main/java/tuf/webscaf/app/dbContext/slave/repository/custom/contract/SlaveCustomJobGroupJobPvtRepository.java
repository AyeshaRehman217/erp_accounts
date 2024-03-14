package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;

import java.util.UUID;

// This interface wil extends in JobGroupJobPvt  Repository
public interface SlaveCustomJobGroupJobPvtRepository {

    //This Function is used to check
    Flux<SlaveJobEntity> showUnMappedJobListAgainstJobGroup(UUID jobGroupUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveJobEntity> showUnMappedJobListAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveJobEntity> showMappedJobListAgainstJobGroup(UUID jobGroupUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveJobEntity> showMappedJobListAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);
}
