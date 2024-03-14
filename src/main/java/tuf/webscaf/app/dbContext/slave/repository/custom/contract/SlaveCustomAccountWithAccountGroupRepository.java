package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;

import java.util.UUID;

// This interface wil extends in slaveAccountGroupRepo
public interface SlaveCustomAccountWithAccountGroupRepository {

    Flux<SlaveAccountGroupEntity> listOfAccountGroups(UUID uuid, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveAccountGroupEntity> listOfAccountGroupsWithStatus(UUID uuid, String name, Boolean status, Integer size, Long page, String dp, String d);
}
