package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarWithCalendarGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCalendarGroupMapper;

import java.util.UUID;


public class SlaveCustomCalendarWithCalendarGroupRepositoryImpl implements SlaveCustomCalendarWithCalendarGroupRepository {
    private DatabaseClient client;
    SlaveCalendarGroupEntity slaveCalendarGroupEntity;

    @Autowired
    public SlaveCustomCalendarWithCalendarGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    public Flux<SlaveCalendarGroupEntity> listOfCalendarGroupsAgainstCalendar(UUID calendarUUID, String name, Integer size, Long page, String dp, String d) {

        String query = "select calendar_groups.* from calendar_groups " +
                "join calendar_group_calendar_pvt on calendar_groups.uuid = calendar_group_calendar_pvt.calendar_group_uuid " +
                "join calendars on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid " +
                "where calendars.deleted_at is null " +
                "and calendar_groups.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendars.uuid ='" + calendarUUID +
                "' and calendar_groups.name ilike  '%" + name + "%'" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarGroupEntity> listOfCalendarGroupsWithStatusFilterAgainstCalendar(UUID calendarUUID, String name, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select calendar_groups.* from calendar_groups " +
                "join calendar_group_calendar_pvt on calendar_groups.uuid = calendar_group_calendar_pvt.calendar_group_uuid " +
                "join calendars on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid " +
                "where calendars.deleted_at is null " +
                "and calendar_groups.deleted_at is null " +
                "and calendar_group_calendar_pvt.deleted_at is null " +
                "and calendar_groups.status = :status " +
                "and calendars.uuid ='" + calendarUUID +
                "' and calendar_groups.name ilike  '%" + name + "%'" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();
    }

}

