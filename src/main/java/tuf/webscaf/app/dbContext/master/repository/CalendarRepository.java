package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;


@Repository
public interface CalendarRepository extends ReactiveCrudRepository<CalendarEntity, Long> {

    Mono<CalendarEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarEntity> findFirstByCalendarTypeUUIDAndDeletedAtIsNull(UUID calendarTypeUUID);

    Mono<CalendarEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CalendarEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<CalendarEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<CalendarEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    //This query checks whether calendar start date and end date already exist or not
    @Query(" SELECT * FROM calendars " +
            "WHERE (:startDate BETWEEN calendar_from AND calendar_to) or (:endDate BETWEEN calendar_from AND calendar_to) " +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUnique(LocalDateTime startDate, LocalDateTime endDate);

    @Query(" SELECT * FROM calendars " +
            "WHERE ((:startDate BETWEEN calendar_from AND calendar_to) or (:endDate BETWEEN calendar_from AND calendar_to)) and uuid !=:uuid" +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUniqueAndUuidIsNot(LocalDateTime startDate, LocalDateTime endDate, UUID uuid);

}