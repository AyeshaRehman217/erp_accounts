package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubAccountGroupRepository extends ReactiveCrudRepository<SubAccountGroupEntity, Long> {

    Mono<SubAccountGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SubAccountGroupEntity> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SubAccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SubAccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<SubAccountGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);
}
