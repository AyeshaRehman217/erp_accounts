package tuf.webscaf.app.dbContext.slave.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupJobPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupVoucherPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobGroupJobPvtRepository;

import java.util.UUID;


@Repository
public interface SlaveJobGroupJobPvtRepository extends ReactiveCrudRepository<SlaveJobGroupJobPvtEntity, Long>, SlaveCustomJobGroupJobPvtRepository {

    Mono<SlaveJobGroupJobPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveJobGroupJobPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveJobGroupJobPvtEntity> findFirstByJobGroupUUIDAndDeletedAtIsNull(UUID jobGroupUUID);

    Mono<SlaveJobGroupJobPvtEntity> findByJobGroupUUIDAndAllAndDeletedAtIsNull(UUID jobGroupUUID, Boolean all);

//    Flux<JobCostCenterGroupPvtEntity> findAllByJobUUIDAndAccountGroupUUIDInAndDeletedAtIsNull(long voucherUUID,List<Long> accountGroupUUID);
 }
