package tuf.webscaf.app.dbContext.master.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupAccountPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.JobGroupJobPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupJobPvtEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface JobGroupJobPvtRepository extends ReactiveCrudRepository<JobGroupJobPvtEntity, Long> {

    Mono<JobGroupJobPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<JobGroupJobPvtEntity> findFirstByJobGroupUUIDAndAllAndDeletedAtIsNull(UUID jobGroupUUID, Boolean all);

    Flux<JobGroupJobPvtEntity> findByJobGroupUUIDAndDeletedAtIsNull(UUID jobGroupUUID);

    Flux<JobGroupJobPvtEntity> findAllByJobGroupUUIDAndDeletedAtIsNull(UUID jobGroupUUID);

    Mono<JobGroupJobPvtEntity> findFirstByJobGroupUUIDAndDeletedAtIsNull(UUID jobGroupUUID);

    Mono<JobGroupJobPvtEntity> findFirstByJobUUIDAndDeletedAtIsNull(UUID jobUUID);

    Flux<JobGroupJobPvtEntity> findAllByJobGroupUUIDAndJobUUIDInAndDeletedAtIsNull(UUID voucherUUID, List<UUID> accountGroupUUID);

    Mono<JobGroupJobPvtEntity> findFirstByJobGroupUUIDAndJobUUIDAndDeletedAtIsNull(UUID jobGroupUUID, UUID jobUUID);
}
