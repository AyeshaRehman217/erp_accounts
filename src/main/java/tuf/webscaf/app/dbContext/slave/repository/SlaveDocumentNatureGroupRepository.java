package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithDocumentNatureGroupRepository;

import java.util.UUID;

@Repository
public interface SlaveDocumentNatureGroupRepository extends ReactiveSortingRepository<SlaveDocumentNatureGroupEntity, Long>, SlaveCustomVoucherWithDocumentNatureGroupRepository {
    Mono<SlaveDocumentNatureGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveDocumentNatureGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveDocumentNatureGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveDocumentNatureGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveDocumentNatureGroupEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    /**
     * Count All the Mapped Document Nature Groups Against Voucher UUID.
     **/
    @Query("select count(*) from document_nature_groups\n" +
            "left join voucher_document_nature_group_pvt\n" +
            "on document_nature_groups.uuid = voucher_document_nature_group_pvt.document_nature_group_uuid\n" +
            "where voucher_document_nature_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and document_nature_groups.deleted_at is null\n" +
            "and voucher_document_nature_group_pvt.deleted_at is null\n" +
            "and document_nature_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedDocumentNatureGroupAgainstVoucher(UUID voucherUUID, String name);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);
}
