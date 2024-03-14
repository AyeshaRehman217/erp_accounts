package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;

import java.util.UUID;

// This interface wil extends in SlaveProfitCenterGroupProfitCenterPvt Repository
public interface SlaveCustomProfitCenterGroupProfitCenterPvtRepository {

    //This Function is used to check
    Flux<SlaveProfitCenterEntity> showUnMappedProfitCenterRecords(UUID profitCenterGroupUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showUnMappedProfitCenterRecordsWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showMappedProfitCenterRecords(UUID profitCenterGroupUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveProfitCenterEntity> showMappedProfitCenterRecordsWithStatus(UUID profitCenterGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);
}
