package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfitCenterGroupRepository extends ReactiveCrudRepository<ProfitCenterGroupEntity, Long> {
    //    @Query("select * from profit_center_groups where deleted_at is null and id=:id")
//    Mono<ProfitCenterGroupEntity> findById(Long id);
    Mono<ProfitCenterGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<ProfitCenterGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<ProfitCenterGroupEntity> findAllByDeletedAtIsNull();

    Flux<ProfitCenterGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<ProfitCenterGroupEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    //Check If Name Already Exists
    Mono<ProfitCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    //Check If Name Already Exists in Update Function
    Mono<ProfitCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<ProfitCenterGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
