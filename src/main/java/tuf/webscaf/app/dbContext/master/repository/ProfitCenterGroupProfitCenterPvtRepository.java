package tuf.webscaf.app.dbContext.master.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupProfitCenterPvtEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface ProfitCenterGroupProfitCenterPvtRepository extends ReactiveCrudRepository<ProfitCenterGroupProfitCenterPvtEntity, Long> {

    Mono<ProfitCenterGroupProfitCenterPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<ProfitCenterGroupProfitCenterPvtEntity> findFirstByProfitCenterGroupUUIDAndAllAndDeletedAtIsNull(UUID profitCenterGroupUUID, Boolean all);

    Flux<ProfitCenterGroupProfitCenterPvtEntity> findAllByProfitCenterGroupUUIDAndProfitCenterUUIDInAndDeletedAtIsNull(UUID profitCenterGroupId, List<UUID> ids);

    Flux<ProfitCenterGroupProfitCenterPvtEntity> findByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    Flux<ProfitCenterGroupProfitCenterPvtEntity> findAllByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    Mono<ProfitCenterGroupProfitCenterPvtEntity> findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    Mono<ProfitCenterGroupProfitCenterPvtEntity> findFirstByProfitCenterUUIDAndDeletedAtIsNull(UUID profitCenterUUID);

    Mono<ProfitCenterGroupProfitCenterPvtEntity> findFirstByProfitCenterGroupUUIDAndProfitCenterUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID, UUID profitCenterUUID);
}
