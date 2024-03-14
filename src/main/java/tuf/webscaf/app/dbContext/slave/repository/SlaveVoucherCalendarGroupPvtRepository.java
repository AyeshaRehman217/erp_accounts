package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCalendarGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherCalendarGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherCalendarGroupPvtEntity, Long> {

    Mono<SlaveVoucherCalendarGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherCalendarGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherCalendarGroupPvtEntity> findFirstByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    @Query("SELECT EXISTS(\n" +
            "SELECT cgapvt.uuid\n" +
            "FROM voucher_calendar_group_pvt AS vcgp\n" +
            "LEFT JOIN calendar_group_calendar_pvt AS cgapvt " +
            "ON vcgp.calendar_group_uuid= cgapvt.calendar_group_uuid\n" +
            "WHERE vcgp.voucher_uuid = :voucherUUID\n" +
            "AND cgapvt .all_mapped= true\n" +
            "AND vcgp.deleted_at IS NULL \n" +
            "AND cgapvt.deleted_at IS NULL )")
    Mono<Boolean> calendarGroupAllMappingExists(UUID voucherUUID);
 }
