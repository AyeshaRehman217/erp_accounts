package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.SubAccountGroupAccountPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubAccountGroupAccountPvtRepository extends ReactiveCrudRepository<SubAccountGroupAccountPvtEntity, Long> {

    Mono<SubAccountGroupAccountPvtEntity> findFirstBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    Mono<SubAccountGroupAccountPvtEntity> findFirstBySubAccountGroupUUIDAndAllAndDeletedAtIsNull(UUID subAccountGroupUUID, Boolean all);

    Flux<SubAccountGroupAccountPvtEntity> findAllBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    Mono<SubAccountGroupAccountPvtEntity> findFirstByAccountUUIDAndDeletedAtIsNull(UUID accountUUID);

    Flux<SubAccountGroupAccountPvtEntity> findBySubAccountGroupUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID);

    Flux<SubAccountGroupAccountPvtEntity> findAllBySubAccountGroupUUIDAndAccountUUIDInAndDeletedAtIsNull(UUID subAccountGroupUUID, List<UUID> accountUUID);

    Mono<SubAccountGroupAccountPvtEntity> findFirstBySubAccountGroupUUIDAndAccountUUIDAndDeletedAtIsNull(UUID subAccountGroupUUID, UUID accountUUID);
 }
