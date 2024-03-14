package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.JobGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobGroupRepository extends ReactiveSortingRepository<JobGroupEntity, Long> {
    Mono<JobGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<JobGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<JobGroupEntity> findAllByDeletedAtIsNull();

    Mono<JobGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Flux<JobGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> id);

    Mono<JobGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
