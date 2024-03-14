package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherCompanyPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherCompanyPvtRepository extends ReactiveCrudRepository<SlaveVoucherCompanyPvtEntity, Long> {
    Mono<SlaveVoucherCompanyPvtEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<SlaveVoucherCompanyPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    @Query("SELECT string_agg(company_uuid::text, ',') " +
            "as ids FROM voucher_company_pvt " +
            "WHERE voucher_company_pvt.deleted_at IS NULL " +
            "AND voucher_company_pvt.voucher_uuid = :voucherUUID")
    Mono<String> getAllIds(UUID voucherUUID);
}
