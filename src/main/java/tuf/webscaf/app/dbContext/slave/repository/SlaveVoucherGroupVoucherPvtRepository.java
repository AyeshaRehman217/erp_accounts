package tuf.webscaf.app.dbContext.slave.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupVoucherPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherGroupVoucherPvtRepository;

import java.util.UUID;


@Repository
public interface SlaveVoucherGroupVoucherPvtRepository extends ReactiveCrudRepository<SlaveVoucherGroupVoucherPvtEntity, Long>, SlaveCustomVoucherGroupVoucherPvtRepository {

    Mono<SlaveVoucherGroupVoucherPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherGroupVoucherPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherGroupVoucherPvtEntity> findFirstByVoucherGroupUUIDAndDeletedAtIsNull(UUID voucherGroupUUID);

}
