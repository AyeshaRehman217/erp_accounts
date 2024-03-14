package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CashFlowLineEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowLineEntity;

import java.util.UUID;

@Repository
public interface SlaveCashFlowLineRepository extends ReactiveCrudRepository<SlaveCashFlowLineEntity, Long> {
    Flux<SlaveCashFlowLineEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<SlaveCashFlowLineEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCashFlowLineEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
