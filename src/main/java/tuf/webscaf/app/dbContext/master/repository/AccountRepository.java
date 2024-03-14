package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, Long> {
    Mono<AccountEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AccountEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AccountEntity> findFirstByParentAccountUUIDAndDeletedAtIsNull(UUID uuid);

    Flux<AccountEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<AccountEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuid, Boolean status);

    Flux<AccountEntity> findAllByDeletedAtIsNull();

    Mono<AccountEntity> findFirstByUuidAndDeletedAtIsNull(UUID parentAccountUUID);

    Mono<AccountEntity> findFirstByAccountTypeUUIDAndDeletedAtIsNull(UUID accountTypeUUID);

    Mono<AccountEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<AccountEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<AccountEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AccountEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    Mono<AccountEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String code, UUID uuid);

    Mono<AccountEntity> findFirstByControlCodeIgnoreCaseAndDeletedAtIsNull(String controlCode);

    Mono<AccountEntity> findFirstByControlCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String controlCode, UUID uuid);
}
