package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

/**
 * This Custom Repository Extends in Slave Voucher Repository
 **/
public interface SlaveCustomCostCenterGroupWithVoucherRepository {
    Flux<SlaveVoucherEntity> showMappedVouchersAgainstCostCenterGroup(UUID costCenterGroupUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstCostCenterGroupWithStatus(UUID costCenterGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
