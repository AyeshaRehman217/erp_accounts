package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface ProfitCenterRepository extends ReactiveCrudRepository<ProfitCenterEntity, Long> {

    Mono<ProfitCenterEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<ProfitCenterEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<ProfitCenterEntity> findAllByDeletedAtIsNull();

    Flux<ProfitCenterEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Flux<ProfitCenterEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<ProfitCenterEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    Mono<ProfitCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<ProfitCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<ProfitCenterEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
