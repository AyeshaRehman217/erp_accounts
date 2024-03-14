package tuf.webscaf.app.dbContext.slave.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupCostCenterPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupCostCenterPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCostCenterGroupCostCenterPvtRepository;

import java.util.UUID;


@Repository
public interface SlaveCostCenterGroupCostCenterPvtRepository extends ReactiveCrudRepository<SlaveCostCenterGroupCostCenterPvtEntity, Long>, SlaveCustomCostCenterGroupCostCenterPvtRepository {

    Mono<SlaveCostCenterGroupCostCenterPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCostCenterGroupCostCenterPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCostCenterGroupCostCenterPvtEntity> findFirstByCostCenterGroupUUIDAndDeletedAtIsNull(UUID costCenterGroupUUID);

    Mono<SlaveCostCenterGroupCostCenterPvtEntity> findByCostCenterGroupUUIDAndAllAndDeletedAtIsNull(UUID costCenterGroupUUID, Boolean all);
}
