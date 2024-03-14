package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;

import java.util.UUID;


public interface SlaveCustomCostCenterWithCostCenterGroupRepository {

    Flux<SlaveCostCenterGroupEntity> listOfCostCenterGroupsAgainstCostCenter(UUID costCenterUUID, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveCostCenterGroupEntity> listOfCostCenterGroupsAgainstCostCenterWithStatus(UUID costCenterUUID, String name, Boolean status, Integer size, Long page, String dp, String d);

}
