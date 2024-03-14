package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarGroupCalendarPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCalendarPvtRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SlaveCalendarRepository extends ReactiveCrudRepository<SlaveCalendarEntity, Long>, SlaveCustomVoucherCalendarPvtRepository, SlaveCustomCalendarWithVoucherRepository,
        SlaveCustomCalendarGroupCalendarPvtRepository {

    Flux<SlaveCalendarEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<SlaveCalendarEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCalendarEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCalendarEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<SlaveCalendarEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveCalendarEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<Long> countAllByDeletedAtIsNull();

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records based on Status Filter
    Flux<SlaveCalendarEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    //Count all records based on status filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //Fetch All Records based on Status Filter
    Flux<SlaveCalendarEntity> findAllByNameContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID calendarTypeUUID, Boolean status1, String description, UUID calendarTypeUUID2, Boolean status2);

    //Count all records based on status filter
    Mono<Long> countByNameContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCalendarTypeUUIDAndStatusAndDeletedAtIsNull(String name, UUID calendarTypeUUID, Boolean status1, String description, UUID calendarTypeUUID2, Boolean status2);

    /**
     * This Function is Used to Check if Calendar Group and Calendars are mapped and check if Calendar Exists against the given Transaction Date
     * with and without status filter
     **/
    //query for getting count of calendars for a given voucher id and transaction date
    @Query("  select count(*) from calendars\n" +
            " join calendar_group_calendar_pvt on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid" +
            " join voucher_calendar_group_pvt on calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid" +
            " join vouchers on voucher_calendar_group_pvt.voucher_uuid = vouchers.uuid" +
            " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
            " where calendars.deleted_at is null " +
            "and vouchers.deleted_at is null " +
            "and voucher_calendar_group_pvt.deleted_at is null " +
            "and calendar_group_calendar_pvt.deleted_at is null " +
            " and vouchers.uuid = :voucherUUID\n" +
            " and calendars.calendar_from <=  :transactionDate and calendars.calendar_to >= :transactionDate " +
            " and calendar_periods.start_date <= :transactionDate and calendar_periods.end_date >= :transactionDate" +
            " and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%'))")
    Mono<Long> countCalendarsAgainstAndTransactionDate(UUID voucherUUID, LocalDateTime transactionDate, String name, String description);


    //query for getting count of calendars for a given voucher and transaction date based on status Filter
    @Query("  select count(*)  from calendars\n" +
            " join calendar_group_calendar_pvt on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid" +
            " join voucher_calendar_group_pvt on calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid" +
            " join vouchers on voucher_calendar_group_pvt.voucher_uuid = vouchers.uuid" +
            " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
            " where calendars.deleted_at is null " +
            "and vouchers.deleted_at is null " +
            "and voucher_calendar_group_pvt.deleted_at is null " +
            "and calendar_group_calendar_pvt.deleted_at is null " +
            " and vouchers.uuid = :voucherUUID \n" +
            " and calendars.calendar_from <=  :transactionDate and calendars.calendar_to >= :transactionDate " +
            " and calendar_periods.start_date <= :transactionDate and calendar_periods.end_date >= :transactionDate" +
            " and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%'))" +
            "AND calendars.status= :status")
    Mono<Long> countCalendarsAgainstVoucherAndTransactionDateWithStatusFilter(UUID voucherUUID, LocalDateTime transactionDate, String name, String description, Boolean status);

    //query for getting count of calendar periods for transaction date
    @Query("  select count(*)  from calendars\n" +
            " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
            " where calendars.deleted_at is null " +
            " and calendars.calendar_from <=  :transactionDate and calendars.calendar_to >= :transactionDate " +
            " and calendar_periods.start_date <= :transactionDate and calendar_periods.end_date >= :transactionDate" +
            " and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%')) ")
    Mono<Long> countCalendarsAgainstTransactionDate(LocalDateTime transactionDate, String name, String description);

    //query for getting count of calendar periods for transaction date
    @Query("  select count(*)  from calendars\n" +
            " join calendar_periods on calendar_periods.calendar_uuid = calendars.uuid" +
            " where calendars.deleted_at is null " +
            " and calendars.calendar_from <=  :transactionDate and calendars.calendar_to >= :transactionDate " +
            " and calendar_periods.start_date <= :transactionDate and calendar_periods.end_date >= :transactionDate" +
            " and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%'))" +
            "AND calendars.status= :status")
    Mono<Long> countCalendarsAgainstTransactionDateWithStatus(LocalDateTime transactionDate, String name, String description, Boolean status);

    //query used in calendar-group calendar pvt handler
    @Query("SELECT count(*) FROM calendars\n" +
            "WHERE calendars.uuid NOT IN(\n" +
            "SELECT calendars.uuid FROM calendars\n" +
            "LEFT JOIN calendar_group_calendar_pvt\n" +
            "ON calendar_group_calendar_pvt.calendar_uuid = calendars.uuid \n" +
            "WHERE calendar_group_calendar_pvt.calendar_group_uuid = :calendarGroupUUID\n" +
            "AND calendar_group_calendar_pvt.deleted_at IS NULL\n" +
            "AND calendars.deleted_at IS NULL )\n" +
            "AND calendars.deleted_at IS NULL " +
            "AND calendars.name ILIKE concat('%',:name,'%') \n")
    Mono<Long> countUnMappedCalendarRecords(UUID calendarGroupUUID, String name);


    //query used in calendar-group calendar pvt handler
    @Query("SELECT count(*) FROM calendars\n" +
            "WHERE calendars.uuid NOT IN(\n" +
            "SELECT calendars.uuid FROM calendars\n" +
            "LEFT JOIN calendar_group_calendar_pvt\n" +
            "ON calendar_group_calendar_pvt.calendar_uuid = calendars.uuid \n" +
            "WHERE calendar_group_calendar_pvt.calendar_group_uuid = :calendarGroupUUID\n" +
            "AND calendar_group_calendar_pvt.deleted_at IS NULL\n" +
            "AND calendars.deleted_at IS NULL )\n" +
            "AND calendars.deleted_at IS NULL " +
            "AND calendars.status= :status " +
            "AND calendars.name ILIKE concat('%',:name,'%') \n")
    Mono<Long> countUnMappedCalendarRecordsWithStatusFilter(UUID calendarGroupUUID, String name, Boolean status);


    //query for getting count of calendars for a given calendar group based on status
    @Query("select count(*) from calendars \n" +
            "join calendar_group_calendar_pvt on calendars.uuid = calendar_group_calendar_pvt.calendar_uuid \n" +
            "join calendar_groups on calendar_group_calendar_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
            "where calendars.deleted_at is null \n" +
            "and calendar_groups.deleted_at is null\n" +
            "and calendar_group_calendar_pvt.deleted_at is null \n" +
            "and calendar_groups.uuid = :calendarGroupUUID\n" +
            "and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%'))" +
            " AND calendars.status= :status")
    Mono<Long> countCalendarsAgainstCalendarGroupWithStatusFilter(UUID calendarGroupUUID, String name, String description, Boolean status);

    //query for getting count of calendars for a given calendar group based on status
    @Query("select count(*) from calendars \n" +
            "join calendar_group_calendar_pvt on calendars.uuid = calendar_group_calendar_pvt.calendar_uuid \n" +
            "join calendar_groups on calendar_group_calendar_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
            "where calendars.deleted_at is null \n" +
            "and calendar_groups.deleted_at is null\n" +
            "and calendar_group_calendar_pvt.deleted_at is null \n" +
            "and calendar_groups.uuid = :calendarGroupUUID\n" +
            "and (calendars.name ilike concat('%',:name,'%')" +
            "or calendars.description ilike concat('%',:description,'%'))")
    Mono<Long> countCalendarsAgainstCalendarGroup(UUID calendarGroupUUID, String name, String description);

    //query for getting count of calendars for a given voucher based on status
    @Query("SELECT COUNT(distinct calendars.uuid) FROM public.calendars " +
            "LEFT JOIN calendar_group_calendar_pvt " +
            "ON calendars.uuid = calendar_group_calendar_pvt.calendar_uuid " +
            "LEFT JOIN voucher_calendar_group_pvt " +
            "ON calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid " +
            "WHERE voucher_calendar_group_pvt.voucher_uuid = :voucherUUID " +
            "AND calendars.deleted_at IS NULL " +
            "AND calendar_group_calendar_pvt.deleted_at IS NULL " +
            "AND voucher_calendar_group_pvt.deleted_at IS NULL " +
            "AND calendars.name ILIKE concat('%',:name,'%')" +
            "AND calendars.status= :status")
    Mono<Long> countCalendarsWithStatusFilterAgainstVoucher(UUID voucherUUID, String name, Boolean status);

    //query for getting count of calendars for a given voucher
    @Query("SELECT COUNT(distinct calendars.uuid) FROM public.calendars " +
            "LEFT JOIN calendar_group_calendar_pvt " +
            "ON calendars.uuid = calendar_group_calendar_pvt.calendar_uuid " +
            "LEFT JOIN voucher_calendar_group_pvt " +
            "ON calendar_group_calendar_pvt.calendar_group_uuid = voucher_calendar_group_pvt.calendar_group_uuid " +
            "WHERE voucher_calendar_group_pvt.voucher_uuid = :voucherUUID " +
            "AND calendars.deleted_at IS NULL " +
            "AND calendar_group_calendar_pvt.deleted_at IS NULL " +
            "AND voucher_calendar_group_pvt.deleted_at IS NULL " +
            "AND calendars.name ILIKE concat('%',:name,'%')")
    Mono<Long> countCalendarsAgainstVoucher(UUID voucherUUID, String name);

}
