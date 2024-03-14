package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCalendarPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCalendarMapper;


public class SlaveCustomVoucherCalendarPvtRepositoryImpl implements SlaveCustomVoucherCalendarPvtRepository {
    private DatabaseClient client;
    private SlaveCalendarEntity slaveCalendarEntity;

    @Autowired
    public SlaveCustomVoucherCalendarPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCalendarEntity> showUnmappedCalendarListAgainstVoucher(Long voucherId, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.id NOT IN(\n" +
                "SELECT calendars.id FROM calendars\n" +
                "LEFT JOIN voucher_calendar_pvt\n" +
                "ON voucher_calendar_pvt.calendar_id = calendars.id\n" +
                "WHERE voucher_calendar_pvt.voucher_id = " + voucherId +
                " AND voucher_calendar_pvt.deleted_at IS NULL\n" +
                "AND calendars.deleted_at IS NULL) \n" +
                "AND calendars.name ILIKE '%" + name + "%'" +
                "AND calendars.deleted_at IS NULL " +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        Flux<SlaveCalendarEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCalendarEntity> showUnmappedCalendarListAgainstVoucherWithStatus(Long voucherId, Boolean status, String name, String dp, String d, Integer size, Long page) {
        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.id NOT IN(\n" +
                "SELECT calendars.id FROM calendars\n" +
                "LEFT JOIN voucher_calendar_pvt\n" +
                "ON voucher_calendar_pvt.calendar_id = calendars.id\n" +
                "WHERE voucher_calendar_pvt.voucher_id = " + voucherId +
                " AND voucher_calendar_pvt.deleted_at IS NULL\n" +
                "AND calendars.deleted_at IS NULL) \n" +
                "AND calendars.name ILIKE '%" + name + "%'" +
                "AND calendars.status= " + status +
                " AND calendars.deleted_at IS NULL " +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        Flux<SlaveCalendarEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();

        return result;
    }
}
