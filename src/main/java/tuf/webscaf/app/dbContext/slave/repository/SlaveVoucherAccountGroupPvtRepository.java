package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherAccountGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherAccountGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherAccountGroupPvtEntity, Long> {

    Mono<SlaveVoucherAccountGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherAccountGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherAccountGroupPvtEntity> findFirstByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    @Query("SELECT EXISTS(\n" +
            "SELECT agapvt.uuid\n" +
            "FROM voucher_account_group_pvt AS vagp\n" +
            "LEFT JOIN account_group_account_pvt AS agapvt " +
            "ON vagp.account_group_uuid= agapvt.account_group_uuid\n" +
            "WHERE vagp.voucher_uuid = :voucherUUID\n" +
            "AND agapvt .all_mapped= true\n" +
            "AND vagp.deleted_at IS NULL \n" +
            "AND agapvt.deleted_at IS NULL )")
    Mono<Boolean> accountGroupAllMappingExists(UUID voucherUUID);
 }
