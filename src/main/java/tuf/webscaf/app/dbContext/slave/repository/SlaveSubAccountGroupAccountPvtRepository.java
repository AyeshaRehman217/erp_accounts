package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupAccountPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveSubAccountGroupAccountPvtRepository extends ReactiveCrudRepository<SlaveSubAccountGroupAccountPvtEntity, Long> {

    Mono<SlaveSubAccountGroupAccountPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveSubAccountGroupAccountPvtEntity> findFirstBySubAccountGroupUUIDAndAllAndDeletedAtIsNull(UUID subAccountGroupUUID, Boolean all);
}
