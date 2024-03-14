package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;

import java.util.UUID;

// This interface wil extends in Slave Account Group Account Repository
public interface SlaveCustomAccountGroupAccountPvtRepository {

    //This Function is used to check the existing Account List Against Account Group Id
    Flux<SlaveAccountEntity> showUnMappedAccountList(UUID accountGroupUUID, String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> showUnMappedAccountListWithStatus(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> showMappedAccountAgainstAccountGroup(UUID accountGroupUUID, String name, String description, String code, String controlCode, Integer size, Long page, String dp, String d);

    Flux<SlaveAccountEntity> showMappedAccountAgainstAccountGroupWithStatus(UUID accountGroupUUID, String name, String description, String code, String controlCode, Boolean status, Integer size, Long page, String dp, String d);

}
