package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

// This interface wil extends in Voucher Group Voucher Repository
public interface SlaveCustomVoucherGroupVoucherPvtRepository {

    //This Function is used to check existing/UnMapped vouchers UUID
    Flux<SlaveVoucherEntity> showUnMappedVoucherListAgainstVoucherGroup(UUID voucherGroupUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showUnMappedVoucherListAgainstVoucherGroupWithStatus(UUID voucherGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showVoucherListAgainstVoucherGroup(UUID voucherGroupUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showVoucherListWithStatusAgainstVoucherGroup(UUID voucherGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
