package tuf.webscaf.app.dbContext.slave.repository.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;

import java.util.UUID;

// This interface wil extends in Slave Calendar Group Calendar Repository
public interface SlaveCustomCalendarGroupCalendarPvtRepository {

    Flux<SlaveCalendarEntity> indexCalendar(String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarEntity> indexCalendarWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarEntity> showMappedCalendarsAgainstCalendarGroup(UUID calendarGroupUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCalendarEntity> showMappedCalendarsAgainstCalendarGroupWithFilter(UUID calendarGroupUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    //This Function is used to check the unMapped Calendar List Against Calendar Group UUID
    Flux<SlaveCalendarEntity> showUnMappedCalendarList(UUID calendarGroupUUID, String name, String dp, String d, Integer size, Long page);

    //This Function is used to check the unMapped Calendar List Against Calendar Group UUID
    Flux<SlaveCalendarEntity> showUnMappedCalendarListWithStatusFilter(UUID calendarGroupUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

}
