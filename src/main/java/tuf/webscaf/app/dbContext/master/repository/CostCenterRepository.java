package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostCenterRepository extends ReactiveCrudRepository<CostCenterEntity, Long> {

    Mono<CostCenterEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CostCenterEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<CostCenterEntity> findAllByDeletedAtIsNull();

    Flux<CostCenterEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Flux<CostCenterEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<CostCenterEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    Mono<CostCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CostCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<CostCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
