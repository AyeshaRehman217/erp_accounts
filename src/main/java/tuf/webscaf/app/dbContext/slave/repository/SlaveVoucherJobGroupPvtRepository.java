package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherJobGroupPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCostCenterGroupPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherJobGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherJobGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherJobGroupPvtEntity, Long> {

    Mono<SlaveVoucherJobGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherJobGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherJobGroupPvtEntity> findFirstByJobGroupIdAndDeletedAtIsNull(Long jobGroupId);

    @Query("SELECT EXISTS(\n" +
            "SELECT jgjpvt.uuid\n" +
            "FROM voucher_job_group_pvt AS vjgp\n" +
            "LEFT JOIN job_group_job_pvt AS jgjpvt " +
            "ON vjgp.job_group_uuid= jgjpvt.job_group_uuid\n" +
            "WHERE vjgp.voucher_uuid = :voucherUUID\n" +
            "AND jgjpvt .all_mapped= true\n" +
            "AND vjgp.deleted_at IS NULL \n" +
            "AND jgjpvt.deleted_at IS NULL )")
    Mono<Boolean> jobGroupAllMappingExists(UUID voucherUUID);
}
