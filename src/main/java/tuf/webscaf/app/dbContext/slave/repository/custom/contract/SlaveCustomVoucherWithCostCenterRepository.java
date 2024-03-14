package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;

import java.util.UUID;

public interface SlaveCustomVoucherWithCostCenterRepository {
    Flux<SlaveCostCenterEntity> indexCostCenter(String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> indexCostCenterWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCostCenterEntity> showCostCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);
}
