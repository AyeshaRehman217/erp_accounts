package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithVoucherGroupRepository;

import java.util.UUID;

@Repository
public interface SlaveVoucherGroupRepository extends ReactiveCrudRepository<SlaveVoucherGroupEntity, Long>, SlaveCustomVoucherWithVoucherGroupRepository {

    Mono<SlaveVoucherGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveVoucherGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveVoucherGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveVoucherGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveVoucherGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    /**
     * Count Mapped Voucher Groups Against Voucher UUID With and without status Filter
     **/
    //query for getting count of voucher groups for a given voucher
    @Query("select count(*) from voucher_groups\n" +
            "left join voucher_group_voucher_pvt\n" +
            "on voucher_groups.uuid = voucher_group_voucher_pvt.voucher_group_uuid\n" +
            "where voucher_group_voucher_pvt.voucher_uuid = :voucherId \n" +
            "and voucher_groups.deleted_at is null\n" +
            "and voucher_group_voucher_pvt.deleted_at is null\n" +
            "and voucher_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countVoucherGroupListAgainstVoucher(UUID voucherUUID, String name);

    //query for getting count of voucher groups for a given voucher with status
    @Query("select count(*) from voucher_groups\n" +
            "left join voucher_group_voucher_pvt\n" +
            "on voucher_groups.uuid = voucher_group_voucher_pvt.voucher_group_uuid\n" +
            "where voucher_group_voucher_pvt.voucher_uuid = :voucherId \n" +
            "and voucher_groups.status = :status " +
            "and voucher_groups.deleted_at is null\n" +
            "and voucher_group_voucher_pvt.deleted_at is null\n" +
            "and voucher_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countVoucherGroupListWithStatusAgainstVoucher(UUID voucherUUID, String name, Boolean status);
}
