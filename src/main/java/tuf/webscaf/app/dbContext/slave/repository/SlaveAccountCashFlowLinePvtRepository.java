package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountCashFlowLinePvtEntity;

import java.util.UUID;

@Repository
public interface SlaveAccountCashFlowLinePvtRepository extends ReactiveCrudRepository<SlaveAccountCashFlowLinePvtEntity, Long> {
    Mono<SlaveAccountCashFlowLinePvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
