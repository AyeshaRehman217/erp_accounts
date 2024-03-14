package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;

import java.util.UUID;


public interface SlaveCustomProfitCenterWithProfitCenterGroupRepository {

    Flux<SlaveProfitCenterGroupEntity> listOfProfitCenterGroups(UUID profitCenterUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveProfitCenterGroupEntity> listOfProfitCenterGroupsWithStatus(UUID profitCenterUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

}
