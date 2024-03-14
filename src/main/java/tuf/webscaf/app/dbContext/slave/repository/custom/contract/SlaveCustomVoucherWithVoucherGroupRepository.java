package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupEntity;

import java.util.UUID;

/**
 * This Custom Repository Extends in Slave Voucher Group Repository
 **/
public interface SlaveCustomVoucherWithVoucherGroupRepository {
    Flux<SlaveVoucherGroupEntity> showVoucherGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherGroupEntity> showVoucherGroupListWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
