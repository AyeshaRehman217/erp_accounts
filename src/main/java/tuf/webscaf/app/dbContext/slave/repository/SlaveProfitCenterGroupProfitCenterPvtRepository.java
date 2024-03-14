package tuf.webscaf.app.dbContext.slave.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupProfitCenterPvtEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupProfitCenterPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomProfitCenterGroupProfitCenterPvtRepository;

import java.util.UUID;


@Repository
public interface SlaveProfitCenterGroupProfitCenterPvtRepository extends ReactiveCrudRepository<SlaveProfitCenterGroupProfitCenterPvtEntity, Long>, SlaveCustomProfitCenterGroupProfitCenterPvtRepository {

    Mono<SlaveProfitCenterGroupProfitCenterPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveProfitCenterGroupProfitCenterPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveProfitCenterGroupProfitCenterPvtEntity> findFirstByProfitCenterGroupUUIDAndDeletedAtIsNull(UUID profitCenterGroupUUID);

    Mono<SlaveProfitCenterGroupProfitCenterPvtEntity> findByProfitCenterGroupUUIDAndAllAndDeletedAtIsNull(UUID profitCenterGroupUUID, Boolean all);
 }
