package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupAccountPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomAccountGroupAccountPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveAccountGroupAccountPvtRepository extends ReactiveCrudRepository<SlaveAccountGroupAccountPvtEntity, Long>, SlaveCustomAccountGroupAccountPvtRepository {
//    @Query("select * from account_account_group_pvt where deleted_at is null")
    Flux<SlaveAccountGroupAccountPvtEntity> findAllBy(Pageable pageable);

    Mono<SlaveAccountGroupAccountPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveAccountGroupAccountPvtEntity> findFirstByAccountGroupUUIDAndAllAndDeletedAtIsNull(UUID accountGroupUUID, Boolean all);
}
