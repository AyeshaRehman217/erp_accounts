package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterGroupRepository extends ReactiveCrudRepository<CostCenterGroupEntity, Long> {

    Mono<CostCenterGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CostCenterGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<CostCenterGroupEntity> findAllByDeletedAtIsNull();

    Mono<CostCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CostCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<CostCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<CostCenterGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<CostCenterGroupEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);
}
