package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

/**
 * This Custom Repository Extends in Slave Voucher Repository
 **/
public interface SlaveCustomProfitCenterGroupWithVoucherRepository {
    Flux<SlaveVoucherEntity> showMappedVouchersAgainstProfitCenterGroup(UUID profitCenterGroupUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstProfitCenterGroupWithStatus(UUID profitCenterGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
