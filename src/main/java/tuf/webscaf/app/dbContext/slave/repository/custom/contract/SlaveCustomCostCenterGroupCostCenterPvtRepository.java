package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;

import java.util.UUID;

// This interface wil extends in SlaveCostCenterGroupCostCenterPvt Repository
public interface SlaveCustomCostCenterGroupCostCenterPvtRepository {

    //This Function is used to check
    Flux<SlaveCostCenterEntity> showUnMappedCostCenterRecords(UUID costCenterGroupUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showUnMappedCostCenterRecordsWithStatus(UUID costCenterGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showMappedCostCenterRecords(UUID costCenterGroupUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCostCenterEntity> showMappedCostCenterRecordsWithStatus(UUID costCenterGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

}
