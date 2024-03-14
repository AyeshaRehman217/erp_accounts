package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.FlowLineTypeEntity;

@Repository
public interface FlowLineTypeRepository extends ReactiveCrudRepository<FlowLineTypeEntity, Long> {
    Mono<FlowLineTypeEntity> findByIdAndDeletedAtIsNull(Long id);
}
