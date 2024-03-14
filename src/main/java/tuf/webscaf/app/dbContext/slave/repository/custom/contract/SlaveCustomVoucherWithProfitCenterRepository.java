package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;

import java.util.UUID;

public interface SlaveCustomVoucherWithProfitCenterRepository {

    Flux<SlaveProfitCenterEntity> indexProfitCenter(String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> indexProfitCenterWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterWithBranch(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterWithCompanyWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveProfitCenterEntity> showProfitCenterWithBranchWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);
}
