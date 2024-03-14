package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentNatureGroupRepository extends ReactiveSortingRepository<DocumentNatureGroupEntity, Long> {
    Mono<DocumentNatureGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<DocumentNatureGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<DocumentNatureGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<DocumentNatureGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<DocumentNatureGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<DocumentNatureGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

}
