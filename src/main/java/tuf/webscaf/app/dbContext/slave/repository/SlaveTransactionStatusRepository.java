package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionStatusEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

@Repository
public interface SlaveTransactionStatusRepository extends ReactiveCrudRepository<SlaveTransactionStatusEntity, Long> {
    Mono<SlaveTransactionStatusEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveTransactionStatusEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveTransactionStatusEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndDeletedAtIsNull(String name, String description, String slug, Pageable pageable);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndDeletedAtIsNull(String name, String description, String slug);

    //Fetch all Record with Status Filter
    Flux<SlaveTransactionStatusEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2, String slug, Boolean status3, Pageable pageable);

    //Count all Record with Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrSlugContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2, String slug, Boolean status3);

    Mono<Long> countByDeletedAtIsNullAndNameContaining(String name);
}
