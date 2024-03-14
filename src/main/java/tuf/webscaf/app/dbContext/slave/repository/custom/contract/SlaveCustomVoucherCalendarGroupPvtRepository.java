package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;

import java.util.UUID;

public interface SlaveCustomVoucherCalendarGroupPvtRepository {
    Flux<SlaveCalendarGroupEntity> showUnMappedCalendarGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveCalendarGroupEntity> showUnMappedCalendarGroupListAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveCalendarGroupEntity> showMappedCalendarGroupsAgainstVoucher(UUID voucherUUID, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarGroupEntity> showMappedCalendarGroupsWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, Integer size, Long page, String dp, String d);
}
