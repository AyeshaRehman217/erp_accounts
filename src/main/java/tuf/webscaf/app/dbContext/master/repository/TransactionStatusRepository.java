package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;

import java.util.UUID;

@Repository
public interface TransactionStatusRepository extends ReactiveCrudRepository<TransactionStatusEntity, Long> {
    Mono<TransactionStatusEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<TransactionStatusEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<TransactionStatusEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<TransactionStatusEntity> findFirstBySlugAndDeletedAtIsNull(String slug);

    Mono<TransactionStatusEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<TransactionStatusEntity> findFirstBySlugAndDeletedAtIsNullAndIdIsNot(String slug, Long id);

    Mono<TransactionStatusEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<TransactionStatusEntity> findFirstBySlugAndDeletedAtIsNullAndUuidIsNot(String slug, UUID uuid);

}
