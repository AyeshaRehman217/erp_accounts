package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.*;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomTransactionRepository;

import java.util.UUID;

@Repository
public interface SlaveTransactionRepository extends ReactiveSortingRepository<SlaveTransactionEntity, Long>, SlaveCustomTransactionRepository {
    Mono<SlaveTransactionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveTransactionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String description);

    //Find By Branch id In Config Module
    Mono<SlaveTransactionEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchId);

    Mono<SlaveTransactionEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyId);

    //Count All Records based on Status Filter
    Mono<Long> countAllByDeletedAtIsNull();

    //Count All Records based on Status Filter
    Mono<Long> countAllByVoucherUUIDAndDeletedAtIsNull(UUID voucherNo);

    //Count All Records based on Voucher Type Filter
    @Query("SELECT COUNT(*) from transactions AS trst\n" +
            "LEFT JOIN vouchers ON trst.voucher_uuid = vouchers.uuid\n" +
            "WHERE trst.deleted_at IS NULL \n" +
            "AND vouchers.deleted_at IS NULL\n" +
            "AND vouchers.voucher_type_catalogue_uuid = :voucherTypeCatalogueUUID")
    Mono<Long> countRecordsWithVoucherTypeCatalogue(UUID voucherTypeCatalogueUUID);


    //Count All Records based on Voucher Type Filter
    @Query("SELECT COUNT(*) from transactions AS trst\n" +
            "LEFT JOIN vouchers ON trst.voucher_uuid = vouchers.uuid\n" +
            "WHERE trst.deleted_at IS NULL \n" +
            "AND vouchers.deleted_at IS NULL\n" +
            "AND vouchers.voucher_type_catalogue_uuid = :voucherTypeCatalogueUUID\n" +
            "AND trst.voucher_uuid = :voucherUUID")
    Mono<Long> countRecordsWithVoucherAndVoucherTypeCatalogue(UUID voucherTypeCatalogueUUID, UUID voucherUUID);

    Flux<SlaveTransactionEntity> findAllByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String description, Boolean status1);
}
