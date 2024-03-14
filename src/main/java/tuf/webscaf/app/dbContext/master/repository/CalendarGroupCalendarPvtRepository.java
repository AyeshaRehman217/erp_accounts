package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupCalendarPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarGroupCalendarPvtRepository extends ReactiveCrudRepository<CalendarGroupCalendarPvtEntity, Long> {

    Mono<CalendarGroupCalendarPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    //check on deleted at Function
    Mono<CalendarGroupCalendarPvtEntity> findFirstByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    Mono<CalendarGroupCalendarPvtEntity> findFirstByCalendarGroupUUIDAndAllAndDeletedAtIsNull(UUID calendarGroupUUID, Boolean all);

    Flux<CalendarGroupCalendarPvtEntity> findAllByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    Mono<CalendarGroupCalendarPvtEntity> findFirstByCalendarUUIDAndDeletedAtIsNull(UUID calendarUUID);

    Flux<CalendarGroupCalendarPvtEntity> findByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    Flux<CalendarGroupCalendarPvtEntity> findAllByCalendarGroupUUIDAndCalendarUUIDInAndDeletedAtIsNull(UUID calendarGroupUUID, List<UUID> calendarUUID);

    Mono<CalendarGroupCalendarPvtEntity> findFirstByCalendarGroupUUIDAndCalendarUUIDAndDeletedAtIsNull(UUID calendarGroupUUID, UUID calendarId);
}
