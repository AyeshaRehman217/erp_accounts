package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;

//This Custom Repository Extends in Slave Calendar Repository
public interface SlaveCustomVoucherCalendarPvtRepository {
    //to show Unmapped Calendars against Voucher
    Flux<SlaveCalendarEntity> showUnmappedCalendarListAgainstVoucher(Long voucherId, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveCalendarEntity> showUnmappedCalendarListAgainstVoucherWithStatus(Long voucherId,Boolean status, String name, String dp, String d, Integer size, Long page);

}
