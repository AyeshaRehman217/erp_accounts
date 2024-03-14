package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountSearchCodePvtEntity;

@Repository
public interface SlaveAccountSearchCodePvtRepository extends ReactiveCrudRepository<SlaveAccountSearchCodePvtEntity, Long> {
    Flux<SlaveAccountSearchCodePvtEntity> findAllBy(Pageable pageable);
}
