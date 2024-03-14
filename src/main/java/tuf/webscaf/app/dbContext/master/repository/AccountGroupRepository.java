package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountGroupRepository extends ReactiveCrudRepository<AccountGroupEntity, Long> {

    Mono<AccountGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AccountGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<AccountGroupEntity> findAllByDeletedAtIsNull();

    Flux<AccountGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Mono<AccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
