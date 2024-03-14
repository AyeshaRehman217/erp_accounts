package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.AccountTypeEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountTypeEntity;

import java.util.UUID;

@Repository
public interface AccountTypesRepository extends ReactiveCrudRepository<AccountTypeEntity, Long> {

    Mono<AccountTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AccountTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AccountTypeEntity> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AccountTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AccountTypeEntity> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    Mono<AccountTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<AccountTypeEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String code, UUID uuid);

    Mono<AccountTypeEntity> findFirstBySlugAndDeletedAtIsNull(String slug);

    Mono<AccountTypeEntity> findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(String slug, UUID uuid);


}
