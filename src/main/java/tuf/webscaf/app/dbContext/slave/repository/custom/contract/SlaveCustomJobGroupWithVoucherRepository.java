package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

public interface SlaveCustomJobGroupWithVoucherRepository {
    Flux<SlaveVoucherEntity> showMappedVouchersAgainstJobGroup(UUID jobGroupUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersWithStatusAgainstJobGroup(UUID jobGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
