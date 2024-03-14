package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherCompanyPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherCompanyPvtRepository extends ReactiveCrudRepository<VoucherCompanyPvtEntity, Long> {
    Flux<VoucherCompanyPvtEntity> findAllByVoucherUUIDAndCompanyUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> companyUUID);

    Flux<VoucherCompanyPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCompanyPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCompanyPvtEntity> findFirstByVoucherUUIDAndCompanyUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID  companyUUID);
}
