package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;

import java.util.UUID;

public interface SlaveCustomVoucherProfitCenterGroupPvtRepository {
    Flux<SlaveProfitCenterGroupEntity> showUnMappedProfitCenterGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterGroupEntity> showUnMappedProfitCenterGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterGroupEntity> showMappedProfitCenterGroups(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterGroupEntity> showMappedProfitCenterGroupsWithStatus(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page);

}
