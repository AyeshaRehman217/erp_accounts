package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;

import java.util.UUID;

public interface SlaveCustomVoucherJobGroupPvtRepository {
    Flux<SlaveJobGroupEntity> showUnMappedJobGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveJobGroupEntity> showUnMappedJobGroupListAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveJobGroupEntity> showMappedJobGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveJobGroupEntity> showMappedJobGroupListWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, String dp, String d, Integer size, Long page);
}
