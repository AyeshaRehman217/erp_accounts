package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.IncomeSummaryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveIncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveIncomeSummaryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterGroupEntity;

import java.util.UUID;

@Repository
public interface SlaveIncomeSummaryRepository extends ReactiveCrudRepository<SlaveIncomeSummaryEntity, Long> {
    Mono<SlaveIncomeSummaryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveIncomeSummaryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveIncomeSummaryEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
