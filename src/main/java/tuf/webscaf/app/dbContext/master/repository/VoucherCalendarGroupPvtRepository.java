package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherCalendarGroupPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherCalendarGroupPvtRepository extends ReactiveCrudRepository<VoucherCalendarGroupPvtEntity, Long> {

    Mono<VoucherCalendarGroupPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<VoucherCalendarGroupPvtEntity> findAllByCalendarGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Flux<VoucherCalendarGroupPvtEntity> findAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Flux<VoucherCalendarGroupPvtEntity> findAllByVoucherUUIDAndCalendarGroupUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> accountGroupUUID);

    Flux<VoucherCalendarGroupPvtEntity> findByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCalendarGroupPvtEntity> findFirstByVoucherUUIDAndDeletedAtIsNull(UUID voucherUUID);

    Mono<VoucherCalendarGroupPvtEntity> findFirstByCalendarGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Mono<VoucherCalendarGroupPvtEntity> findFirstByVoucherUUIDAndCalendarGroupUUIDAndDeletedAtIsNull(UUID voucherUUID, UUID accountGroupUUID);
}
