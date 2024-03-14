package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCalendarCalendarPeriodDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCalendarCalendarPeriodMapper;

import java.time.LocalDateTime;
import java.util.UUID;


// This interface wil extends in slaveCalendarRepo
public class SlaveCustomCalendarWithVoucherRepositoryImpl implements SlaveCustomCalendarWithVoucherRepository {
    SlaveCalendarEntity slaveCalendarEntity;
    private DatabaseClient client;

    @Autowired
    public SlaveCustomCalendarWithVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstVoucherAndTransactionDateWithStatusFilter(UUID voucherUUID, LocalDateTime transactionDate, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {

        String query = "select calendars.*,calendar_periods.quarter,calendar_periods.uuid as calendarPeriodUUID, " +
                "calendar_periods.period_no,calendar_periods.is_open ,calendar_periods.adjustments from calendars" +
                " join calendar_group_calendar_pvt on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid" +
                " join voucher_calendar_group_pvt on calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid" +
                " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
                " where calendars.deleted_at is null " +
                "and voucher_calendar_group_pvt.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendar_periods.deleted_at is null " +
                " and calendars.status = " + status +
                " and voucher_calendar_group_pvt.voucher_uuid ='" + voucherUUID +
                "' and calendars.calendar_from <= '" + transactionDate + "' and calendars.calendar_to >= '" + transactionDate +
                "' and calendar_periods.start_date <= '" + transactionDate + "' and calendar_periods.end_date >= '" + transactionDate +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                " or calendars.description ILIKE  '%" + description + "%' )" +
                " order by calendars." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarCalendarPeriodMapper mapper = new SlaveCustomCalendarCalendarPeriodMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstVoucherAndTransactionDate(UUID voucherUUID, LocalDateTime transactionDate, String name, String description, Integer size, Long page, String dp, String d) {

        String query = "select calendars.*,calendar_periods.quarter,calendar_periods.uuid as calendarPeriodUUID, " +
                "calendar_periods.period_no,calendar_periods.is_open ,calendar_periods.adjustments from calendars" +
                " join calendar_group_calendar_pvt on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid" +
                " join voucher_calendar_group_pvt on calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid" +
                " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
                " where calendars.deleted_at is null " +
                "and voucher_calendar_group_pvt.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendar_periods.deleted_at is null " +
                " and voucher_calendar_group_pvt.voucher_uuid ='" + voucherUUID +
                "' and calendars.calendar_from <= '" + transactionDate + "' and calendars.calendar_to >= '" + transactionDate +
                "' and calendar_periods.start_date <= '" + transactionDate + "' and calendar_periods.end_date >= '" + transactionDate +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                " or calendars.description ILIKE  '%" + description + "%' )" +
                " order by calendars." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarCalendarPeriodMapper mapper = new SlaveCustomCalendarCalendarPeriodMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstTransactionDateWithStatus(LocalDateTime transactionDate, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select calendars.*,calendar_periods.quarter,calendar_periods.uuid as calendarPeriodUUID, " +
                "calendar_periods.period_no,calendar_periods.is_open ,calendar_periods.adjustments from calendars" +
                " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
                " where calendars.deleted_at is null " +
                "and calendar_periods.deleted_at is null " +
                " and calendars.status = " + status + "\n"+
                " and calendars.calendar_from <= '" + transactionDate + "' and calendars.calendar_to >= '" + transactionDate +
                "' and calendar_periods.start_date <= '" + transactionDate + "' and calendar_periods.end_date >= '" + transactionDate +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                " or calendars.description ILIKE  '%" + description + "%' )" +
                " order by calendars." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCalendarCalendarPeriodMapper mapper = new SlaveCustomCalendarCalendarPeriodMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarCalendarPeriodDto> listOfCalendarsAgainstTransactionDate(LocalDateTime transactionDate, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select calendars.*,calendar_periods.quarter,calendar_periods.uuid as calendarPeriodUUID, " +
                "calendar_periods.period_no,calendar_periods.is_open ,calendar_periods.adjustments from calendars" +
                " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
                " where calendars.deleted_at is null " +
                "and calendar_periods.deleted_at is null " +
                " and calendars.calendar_from <= '" + transactionDate + "' and calendars.calendar_to >= '" + transactionDate +
                "' and calendar_periods.start_date <= '" + transactionDate + "' and calendar_periods.end_date >= '" + transactionDate +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                " or calendars.description ILIKE  '%" + description + "%' )" +
                " order by calendars." + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomCalendarCalendarPeriodMapper mapper = new SlaveCustomCalendarCalendarPeriodMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

}
