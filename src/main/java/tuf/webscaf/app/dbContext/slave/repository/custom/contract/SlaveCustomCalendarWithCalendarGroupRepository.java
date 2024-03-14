package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;

import java.util.UUID;

// This interface wil extends in slaveCalendarGroupRepo
public interface SlaveCustomCalendarWithCalendarGroupRepository {

    Flux<SlaveCalendarGroupEntity> listOfCalendarGroupsAgainstCalendar(UUID calendarUUID, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarGroupEntity> listOfCalendarGroupsWithStatusFilterAgainstCalendar(UUID calendarUUID, String name,Boolean status,  Integer size, Long page, String dp, String d);

}
