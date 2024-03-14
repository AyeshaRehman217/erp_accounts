package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCalendarMapper;

import java.util.UUID;

public class SlaveCustomCalendarGroupCalendarPvtRepositoryImpl implements SlaveCustomCalendarGroupCalendarPvtRepository {
    private DatabaseClient client;
    private SlaveCalendarEntity slaveCalendarEntity;

    @Autowired
    public SlaveCustomCalendarGroupCalendarPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCalendarEntity> indexCalendar(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.deleted_at IS NULL\n" +
                "AND (calendars.name ILIKE  '%" + name + "%' " +
                "OR calendars.description ILIKE  '%" + description + "%' )\n" +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarEntity> indexCalendarWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.deleted_at IS NULL\n" +
                "AND calendars.status = " + status + "\n" +
                "AND (calendars.name ILIKE  '%" + name + "%' " +
                "OR calendars.description ILIKE  '%" + description + "%' )\n" +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarEntity> showMappedCalendarsAgainstCalendarGroup(UUID calendarGroupUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select calendars.* from calendars " +
                "join calendar_group_calendar_pvt on calendars.uuid = calendar_group_calendar_pvt.calendar_uuid " +
                "join calendar_groups on calendar_group_calendar_pvt.calendar_group_uuid = calendar_groups.uuid " +
                " and calendars.deleted_at is null " +
                "and calendar_groups.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendar_groups.uuid ='" + calendarGroupUUID +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                "or calendars.description ILIKE  '%" + description + "%' )" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarEntity> showMappedCalendarsAgainstCalendarGroupWithFilter(UUID calendarGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select calendars.* from calendars " +
                "join calendar_group_calendar_pvt on calendars.uuid = calendar_group_calendar_pvt.calendar_uuid " +
                "join calendar_groups on calendar_group_calendar_pvt.calendar_group_uuid = calendar_groups.uuid " +
                "where calendars.status = " + status +
                " and calendars.deleted_at is null " +
                "and calendar_groups.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendar_groups.uuid ='" + calendarGroupUUID +
                "' and (calendars.name ILIKE  '%" + name + "%' " +
                "or calendars.description ILIKE  '%" + description + "%' )" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();
    }

    //This Function is used to check the existing Calendar List Against Calendar Group UUID
    @Override
    public Flux<SlaveCalendarEntity> showUnMappedCalendarList(UUID calendarGroupUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.uuid NOT IN(\n" +
                "SELECT calendars.uuid FROM calendars\n" +
                "LEFT JOIN calendar_group_calendar_pvt\n" +
                "ON calendar_group_calendar_pvt.calendar_uuid = calendars.uuid\n" +
                "WHERE calendar_group_calendar_pvt.calendar_group_uuid = '" + calendarGroupUUID +
                "' AND calendar_group_calendar_pvt.deleted_at IS NULL\n" +
                "AND calendars.deleted_at IS NULL) \n" +
                "AND calendars.deleted_at IS NULL " +
                "AND calendars.name ILIKE '%" + name + "%' \n" +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        Flux<SlaveCalendarEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCalendarEntity> showUnMappedCalendarListWithStatusFilter(UUID calendarGroupUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT calendars.* FROM calendars\n" +
                "WHERE calendars.uuid NOT IN(\n" +
                "SELECT calendars.uuid FROM calendars\n" +
                "LEFT JOIN calendar_group_calendar_pvt\n" +
                "ON calendar_group_calendar_pvt.calendar_uuid = calendars.uuid\n" +
                "WHERE calendar_group_calendar_pvt.calendar_group_uuid = '" + calendarGroupUUID +
                "' AND calendar_group_calendar_pvt.deleted_at IS NULL\n" +
                "AND calendars.deleted_at IS NULL) \n" +
                "AND calendars.deleted_at IS NULL " +
                " and calendars.status = " + status +
                " and calendars.name ILIKE '%" + name + "%' \n" +
                "ORDER BY calendars." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarMapper mapper = new SlaveCustomCalendarMapper();

        Flux<SlaveCalendarEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarEntity))
                .all();

        return result;
    }

}
