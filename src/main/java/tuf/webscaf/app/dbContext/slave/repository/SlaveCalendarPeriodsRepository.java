package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarPeriodEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarPeriodEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SlaveCalendarPeriodsRepository extends ReactiveCrudRepository<SlaveCalendarPeriodEntity, Long> {

    Flux<SlaveCalendarPeriodEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveCalendarPeriodEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveCalendarPeriodEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCalendarPeriodEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    @Query("SELECT * FROM public.calendar_periods \n" +
            "WHERE calendar_periods.start_date <= :transactionDate \n" +
            "AND calendar_periods.end_date >= :transactionDate \n" +
            "AND calendar_periods.adjustments IS FALSE \n" +
            "FETCH FIRST ROW ONLY ")
    Mono<SlaveCalendarPeriodEntity> showRecordWithTransactionDate(LocalDateTime transactionDate);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records With is Open Status Filter
    Flux<SlaveCalendarPeriodEntity> findAllByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndIsOpenAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //Fetch All Records With is Open Status Filter
    Flux<SlaveCalendarPeriodEntity> findAllByNameContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID calendarUUID, String description, UUID calendarUUID2);

    Mono<Long> countByNameContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndDeletedAtIsNull(String name, UUID calendarUUID, String description, UUID calendarUUID2);

    //Fetch All Records With Calendar And is Open Status Filter
    Flux<SlaveCalendarPeriodEntity> findAllByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(Pageable pageable, String name, UUID calendarUUID, Boolean status1, String description, UUID calendarUUID2, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarUUIDAndIsOpenAndDeletedAtIsNull(String name, UUID calendarUUID, Boolean status1, String description, UUID calendarUUID2, Boolean status2);

}
