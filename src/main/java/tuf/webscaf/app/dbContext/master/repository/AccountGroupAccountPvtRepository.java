package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupAccountPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountGroupAccountPvtRepository extends ReactiveCrudRepository<AccountGroupAccountPvtEntity, Long> {
//    @Query("select * from account_account_group_pvt where deleted_at is null and id=:id")
//    Mono<AccountGroupAccountPvtEntity> findById(Long id);

    Mono<AccountGroupAccountPvtEntity> findByIdAndDeletedAtIsNull(Long id);
    //check on deleted at Function
    Mono<AccountGroupAccountPvtEntity> findFirstByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Mono<AccountGroupAccountPvtEntity> findFirstByAccountGroupUUIDAndAllAndDeletedAtIsNull(UUID accountGroupUUID, Boolean all);

    Flux<AccountGroupAccountPvtEntity> findAllByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

    Mono<AccountGroupAccountPvtEntity> findFirstByAccountUUIDAndDeletedAtIsNull(UUID accountUUID);

    Flux<AccountGroupAccountPvtEntity> findByAccountGroupUUIDAndDeletedAtIsNull(UUID accountGroupUUID);

//    Flux<AccountGroupAccountPvtEntity> findAllByAccountUUIDAndAccountGroupUUIDInAndDeletedAtIsNull(UUID accountUUID, List<UUID> accountGroupUUID);

//    Flux<AccountGroupAccountPvtEntity> findAllByAccountIdAndAccountGroupIdInAndDeletedAtIsNull(long accountId, List<Long> accountGroupId);
    Flux<AccountGroupAccountPvtEntity> findAllByAccountGroupUUIDAndAccountUUIDInAndDeletedAtIsNull(UUID accountGroupUUID, List<UUID> accountUUID);

    Mono<AccountGroupAccountPvtEntity> findFirstByAccountGroupUUIDAndAccountUUIDAndDeletedAtIsNull(UUID accountGroupUUID, UUID accountUUID);
 }
