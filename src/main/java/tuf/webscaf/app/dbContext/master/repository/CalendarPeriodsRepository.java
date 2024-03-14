package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;
import tuf.webscaf.app.dbContext.master.entity.CalendarPeriodEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CalendarPeriodsRepository extends ReactiveCrudRepository<CalendarPeriodEntity, Long> {

    Mono<CalendarPeriodEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarPeriodEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarPeriodEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CalendarPeriodEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Flux<CalendarPeriodEntity> findAllByCalendarUUIDAndDeletedAtIsNull(UUID CalendarUUID);

    Flux<CalendarPeriodEntity> findAllByCalendarUUIDAndDeletedAtIsNullAndIdIsNot(UUID CalendarUUID, Long id);

    Flux<CalendarPeriodEntity> findAllByCalendarUUIDAndDeletedAtIsNullAndUuidIsNot(UUID CalendarUUID, UUID uuid);

    Mono<CalendarPeriodEntity> findFirstByCalendarUUIDAndDeletedAtIsNull(UUID CalendarUUID);

    //This query checks whether calendar start date and end date already exist or not
    @Query(" SELECT * FROM calendar_periods " +
            "WHERE ((:startDate BETWEEN start_date AND end_date) or (:endDate BETWEEN start_date AND end_date)) " +
            "AND calendar_periods.deleted_at IS NULL" +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUnique(LocalDateTime startDate, LocalDateTime endDate);
//
    @Query(" SELECT * FROM calendar_periods " +
            "WHERE ((:startDate BETWEEN start_date AND end_date) or (:endDate BETWEEN start_date AND end_date)) and uuid !=:uuid " +
            "AND calendar_periods.deleted_at IS NULL" +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUniqueAndUuidIsNot(LocalDateTime startDate, LocalDateTime endDate, UUID uuid);

    //This query checks whether calendar start date and end date already exist or not if Adjustment Status is True
    @Query(" SELECT * FROM calendar_periods " +
            "WHERE ((:startDate BETWEEN start_date AND end_date) or (:endDate BETWEEN start_date AND end_date)) " +
            "AND calendar_periods.deleted_at IS NULL " +
            "AND calendar_periods.adjustments IS TRUE" +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUniqueAndAdjustmentIsTrue(LocalDateTime startDate, LocalDateTime endDate);

    //This query checks whether calendar start date and end date already exist or not if Adjustment Status is True Used By Update Function
    @Query(" SELECT * FROM calendar_periods " +
            "WHERE ((:startDate BETWEEN start_date AND end_date) or (:endDate BETWEEN start_date AND end_date)) and uuid !=:uuid " +
            "AND calendar_periods.deleted_at IS NULL " +
            "AND calendar_periods.adjustments IS TRUE" +
            " fetch first row only")
    Mono<CalendarEntity> findStartDateAndEndDateIsUniqueAndUuidIsNotAndAdjustmentIsTrue(LocalDateTime startDate, LocalDateTime endDate, UUID uuid);

    @Query("SELECT string_agg(calendar_periods.uuid::text, ',') " +
            "AS calendarPeriodUUID FROM calendar_periods " +
            "JOIN calendars ON calendar_periods.calendar_uuid = calendars.uuid  " +
            "JOIN calendar_group_calendar_pvt ON  calendars.uuid = calendar_group_calendar_pvt.calendar_uuid  " +
            "JOIN voucher_calendar_group_pvt " +
            "ON calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid  " +
            "WHERE voucher_calendar_group_pvt.deleted_at IS NULL " +
            "AND calendars.deleted_at IS NULL " +
            "AND calendar_group_calendar_pvt.deleted_at IS NULL " +
            "AND calendars.deleted_at IS NULL " +
            "AND voucher_calendar_group_pvt.voucher_uuid = :voucherUUID")
    Mono<String> getAllCalendarPeriodsAgainstVoucher(UUID voucherUUID);
}
