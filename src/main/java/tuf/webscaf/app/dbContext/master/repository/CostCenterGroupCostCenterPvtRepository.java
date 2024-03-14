package tuf.webscaf.app.dbContext.master.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupAccountPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupCostCenterPvtEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface CostCenterGroupCostCenterPvtRepository extends ReactiveCrudRepository<CostCenterGroupCostCenterPvtEntity, Long> {

    Mono<CostCenterGroupCostCenterPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<CostCenterGroupCostCenterPvtEntity> findAllByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    Mono<CostCenterGroupCostCenterPvtEntity> findFirstByCostCenterGroupUUIDAndAllAndDeletedAtIsNull(UUID costCenterGroupUUID, Boolean all);

    Flux<CostCenterGroupCostCenterPvtEntity> findAllByCostCenterGroupUUIDAndCostCenterUUIDInAndDeletedAtIsNull(UUID costCenterGroupUUID, List<UUID> costCenterUUIDs);

    Flux<CostCenterGroupCostCenterPvtEntity> findByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    Mono<CostCenterGroupCostCenterPvtEntity> findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    Mono<CostCenterGroupCostCenterPvtEntity> findFirstByCostCenterUUIDAndDeletedAtIsNull(UUID costCenterUUID);

    Mono<CostCenterGroupCostCenterPvtEntity> findFirstByCostCenterGroupUUIDAndCostCenterUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID, UUID costCenterUUID);
}
