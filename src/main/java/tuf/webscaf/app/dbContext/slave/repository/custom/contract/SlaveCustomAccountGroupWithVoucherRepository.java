package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

/**
 * This Custom Repository will extend in Slave Voucher Repository
 **/
public interface SlaveCustomAccountGroupWithVoucherRepository {
    Flux<SlaveVoucherEntity> showMappedVouchersAgainstAccountGroup(UUID accountGroupUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstAccountGroupWithStatus(UUID accountGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
