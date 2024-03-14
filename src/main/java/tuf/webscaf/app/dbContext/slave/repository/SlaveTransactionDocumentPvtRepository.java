package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TransactionDocumentPvtEntity;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionDocumentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionDocumentPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveTransactionDocumentPvtRepository extends ReactiveCrudRepository<SlaveTransactionDocumentPvtEntity, Long> {
    Mono<SlaveTransactionDocumentPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveTransactionDocumentPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveTransactionDocumentPvtEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveTransactionDocumentPvtEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentId);

    Flux<SlaveTransactionDocumentPvtEntity> findAllByTransactionUUIDAndDeletedAtIsNull(UUID transactionUUID);

    //Getting All Documents That exist in Drive Module
    @Query("SELECT \n" +
            "CASE\n" +
            "WHEN string_agg(transaction_document_pvt.document_uuid::text, ',') is not null\n" +
            "THEN string_agg(transaction_document_pvt.document_uuid::text, ',') \n" +
            "ELSE ''\n" +
            "END as documentIds \n" +
            "FROM transaction_document_pvt \n" +
            "WHERE transaction_document_pvt.deleted_at IS NULL \n" +
            "AND transaction_document_pvt.transaction_uuid = :transactionUUID")
    Mono<String> getAllDocumentListAgainstTransaction(UUID transactionId);
}
