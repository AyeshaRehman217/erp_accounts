package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;

import java.util.UUID;

/**
 * This Custom Repository will extend in Slave Cost center Group Repository
 **/
public interface SlaveCustomVoucherCostCenterGroupPvtRepository {
    Flux<SlaveCostCenterGroupEntity> showUnMappedCostCenterGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterGroupEntity> showUnMappedCostCenterGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterGroupEntity> showMappedCostCenterGroups(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterGroupEntity> showMappedCostCenterGroupsWithStatus(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
