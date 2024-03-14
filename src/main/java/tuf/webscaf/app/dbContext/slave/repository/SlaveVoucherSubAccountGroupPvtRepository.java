package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherSubAccountGroupPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherSubAccountGroupPvtRepository extends ReactiveCrudRepository<SlaveVoucherSubAccountGroupPvtEntity, Long> {

    Mono<SlaveVoucherSubAccountGroupPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherSubAccountGroupPvtEntity> findFirstBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    @Query("SELECT EXISTS(\n" +
            "SELECT sagapvt.uuid\n" +
            "FROM voucher_sub_account_groups_pvt AS vsagp\n" +
            "LEFT JOIN sub_account_group_accounts_pvt AS sagapvt " +
            "ON vsagp.sub_account_group_uuid= sagapvt.sub_account_group_uuid\n" +
            "WHERE vsagp.voucher_uuid = :voucherUUID\n" +
            "AND sagapvt .all_mapped= true\n" +
            "AND vsagp.deleted_at IS NULL \n" +
            "AND sagapvt.deleted_at IS NULL )")
    Mono<Boolean> subAccountGroupAllMappingExists(UUID voucherUUID);
 }
