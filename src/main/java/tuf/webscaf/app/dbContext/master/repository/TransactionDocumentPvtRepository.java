package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TransactionDocumentPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.VoucherBranchPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionDocumentPvtRepository extends ReactiveCrudRepository<TransactionDocumentPvtEntity, Long> {

    Mono<TransactionDocumentPvtEntity> findFirstByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

    Flux<TransactionDocumentPvtEntity> findByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

    Flux<TransactionDocumentPvtEntity> findAllByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

    Mono<TransactionDocumentPvtEntity> findFirstByTransactionUUIDAndDocumentUUIDAndDeletedAtIsNull(UUID transactionUUID, UUID documentUUID);

    Mono<TransactionDocumentPvtEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<TransactionDocumentPvtEntity> findAllByTransactionUUIDAndDocumentUUIDInAndDeletedAtIsNull(UUID transactionUUID,List<UUID> documentUUID);

    Flux<TransactionDocumentPvtEntity> findAllByTransactionUUIDAndDocumentUUIDNotInAndDeletedAtIsNull(UUID transactionUUID,List<UUID> documentUUID);

}
