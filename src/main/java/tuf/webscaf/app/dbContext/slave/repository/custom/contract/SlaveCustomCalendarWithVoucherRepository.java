package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCalendarCalendarPeriodDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;

import java.time.LocalDateTime;
import java.util.UUID;


// This interface wil extends in slaveCalendarRepo
public interface SlaveCustomCalendarWithVoucherRepository {
    //This Function is Used to Fetch Calendar Against Calendar Groups
    Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstVoucherAndTransactionDateWithStatusFilter(UUID voucherUUID, LocalDateTime transactionDate, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstVoucherAndTransactionDate(UUID voucherUUID, LocalDateTime transactionDate, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstTransactionDateWithStatus(LocalDateTime transactionDate, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstTransactionDate(LocalDateTime transactionDate, String name, String description, Integer size, Long page, String dp, String d);

}
