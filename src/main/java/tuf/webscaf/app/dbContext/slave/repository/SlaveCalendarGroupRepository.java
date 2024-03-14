package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarWithCalendarGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCalendarGroupPvtRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveCalendarGroupRepository extends ReactiveCrudRepository<SlaveCalendarGroupEntity, Long>, SlaveCustomCalendarWithCalendarGroupRepository, SlaveCustomVoucherCalendarGroupPvtRepository {

    Mono<SlaveCalendarGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCalendarGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveCalendarGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Flux<SlaveCalendarGroupEntity> findAllByDeletedAtIsNull();

    Flux<SlaveCalendarGroupEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Mono<SlaveCalendarGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveCalendarGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveCalendarGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    @Query("select count(*) from calendar_groups " +
            "join calendar_group_calendar_pvt on calendar_groups.uuid = calendar_group_calendar_pvt.calendar_group_uuid " +
            "join calendars on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid " +
            "where calendars.deleted_at is null and calendar_group_calendar_pvt.deleted_at is null \n" +
            "and calendars.uuid = :calendarUUID \n" +
            "and calendar_groups.name ilike concat('%', :name ,'%')")
    Mono<Long> countMappedCalendarGroups(UUID calendarUUID, String name);

    @Query("select count(*) from calendar_groups " +
            "join calendar_group_calendar_pvt on calendar_groups.uuid = calendar_group_calendar_pvt.calendar_group_uuid " +
            "join calendars on calendar_group_calendar_pvt.calendar_uuid = calendars.uuid " +
            "where calendars.deleted_at is null " +
            "and calendar_group_calendar_pvt.deleted_at is null \n" +
            "and calendar_groups.status = :status " +
            "and calendars.uuid = :calendarUUID \n" +
            "and calendar_groups.name ilike concat('%', :name ,'%')")
    Mono<Long> countMappedCalendarGroupsWithStatusFilter(UUID calendarUUID, String name, Boolean status);

    /**
     * Used in Voucher calendar Group Pvt Handler
     **/
    //used in the pvt mapping handler
    @Query("SELECT count(*) FROM calendar_groups\n" +
            "WHERE calendar_groups.uuid NOT IN(\n" +
            "SELECT calendar_groups.uuid FROM calendar_groups\n" +
            "LEFT JOIN voucher_calendar_group_pvt\n" +
            "ON voucher_calendar_group_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
            "WHERE voucher_calendar_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_calendar_group_pvt.deleted_at IS NULL\n" +
            "AND calendar_groups.deleted_at IS NULL) " +
            "AND calendar_groups.deleted_at IS NULL \n" +
            "AND calendar_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedCalendarGroupsRecords(UUID voucherUUID, String name);

    @Query("SELECT count(*) FROM calendar_groups\n" +
            "WHERE calendar_groups.uuid NOT IN(\n" +
            "SELECT calendar_groups.uuid FROM calendar_groups\n" +
            "LEFT JOIN voucher_calendar_group_pvt\n" +
            "ON voucher_calendar_group_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
            "WHERE voucher_calendar_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_calendar_group_pvt.deleted_at IS NULL\n" +
            "AND calendar_groups.deleted_at IS NULL) " +
            "AND calendar_groups.deleted_at IS NULL \n" +
            "AND calendar_groups.status= :status \n" +
            "AND calendar_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedCalendarGroupsRecordsWithStatus(UUID voucherUUID, String name, Boolean status);

    //query for getting count of calendar groups for a given voucher
    @Query("select count(*) from calendar_groups\n" +
            "left join voucher_calendar_group_pvt\n" +
            "on calendar_groups.uuid = voucher_calendar_group_pvt.calendar_group_uuid\n" +
            "where voucher_calendar_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and calendar_groups.deleted_at is null\n" +
            "and voucher_calendar_group_pvt.deleted_at is null\n" +
            "and calendar_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedCalendarGroupList(UUID voucherUUID, String name);

    //query for getting count of calendar groups for a given voucher
    @Query("select count(*) from calendar_groups\n" +
            "left join voucher_calendar_group_pvt\n" +
            "on calendar_groups.uuid = voucher_calendar_group_pvt.calendar_group_uuid\n" +
            "where voucher_calendar_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and calendar_groups.status = :status " +
            "and calendar_groups.deleted_at is null\n" +
            "and voucher_calendar_group_pvt.deleted_at is null\n" +
            "and calendar_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedCalendarGroupListWithStatus(UUID voucherUUID, String name, Boolean status);
}
