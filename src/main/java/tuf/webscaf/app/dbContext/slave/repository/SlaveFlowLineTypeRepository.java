package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveFlowLineTypeEntity;

import java.util.UUID;

@Repository
public interface SlaveFlowLineTypeRepository extends ReactiveCrudRepository<SlaveFlowLineTypeEntity, Long> {
    Mono<SlaveFlowLineTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveFlowLineTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveFlowLineTypeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
