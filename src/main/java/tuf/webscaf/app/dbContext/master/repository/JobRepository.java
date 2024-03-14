package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.JobEntity;
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;

import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends ReactiveSortingRepository<JobEntity, Long> {
    Mono<JobEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<JobEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<JobEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<JobEntity> findAllByIdInAndNameContainingAndDeletedAtIsNull(List<Long> id, String name, Pageable pageable);

    Mono<JobEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Flux<JobEntity> findAllByDeletedAtIsNull();

    Mono<JobEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<JobEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
