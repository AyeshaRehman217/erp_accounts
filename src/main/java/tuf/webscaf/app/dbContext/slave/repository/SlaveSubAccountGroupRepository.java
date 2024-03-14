package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherSubAccountGroupPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveSubAccountGroupRepository extends ReactiveCrudRepository<SlaveSubAccountGroupEntity, Long>, SlaveCustomVoucherSubAccountGroupPvtRepository {

    Flux<SlaveSubAccountGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<SlaveSubAccountGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveSubAccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    //Fetch All Records With Status Filter
    Flux<SlaveSubAccountGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status);

    //Count All Records With Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //query for getting count of sub account groups for a given voucher
    @Query("select count(*) from sub_account_groups\n" +
            "left join voucher_sub_account_groups_pvt\n" +
            "on sub_account_groups.uuid = voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
            "where voucher_sub_account_groups_pvt.voucher_uuid = :voucherUUID\n" +
            "and sub_account_groups.deleted_at is null\n" +
            "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
            "and (sub_account_groups.name ILIKE concat('%',:name,'%')\n" +
            "or sub_account_groups.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedSubAccountGroupList(UUID voucherUUID, String name, String description);

    //query for getting count of account groups for a given voucher
    @Query("select count(*) from sub_account_groups\n" +
            "left join voucher_sub_account_groups_pvt\n" +
            "on sub_account_groups.uuid = voucher_sub_account_groups_pvt.sub_account_group_uuid\n" +
            "where voucher_sub_account_groups_pvt.voucher_uuid = :voucherUUID\n" +
            "and sub_account_groups.status = :status " +
            "and sub_account_groups.deleted_at is null\n" +
            "and voucher_sub_account_groups_pvt.deleted_at is null\n" +
            "and (sub_account_groups.name ILIKE concat('%',:name,'%')\n" +
            "or sub_account_groups.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countMappedSubAccountGroupListWithStatus(UUID voucherUUID, String name, String description, Boolean status);

    //used in the pvt mapping handler
    @Query("SELECT count(*) FROM sub_account_groups\n" +
            "WHERE sub_account_groups.uuid NOT IN(\n" +
            "SELECT sub_account_groups.uuid FROM sub_account_groups\n" +
            "LEFT JOIN voucher_sub_account_groups_pvt\n" +
            "ON voucher_sub_account_groups_pvt.sub_account_group_uuid = sub_account_groups.uuid\n" +
            "WHERE voucher_sub_account_groups_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_sub_account_groups_pvt.deleted_at IS NULL\n" +
            "AND sub_account_groups.deleted_at IS NULL) " +
            "AND sub_account_groups.deleted_at IS NULL \n" +
            "AND (sub_account_groups.name ILIKE concat('%',:name,'%')\n" +
            "OR sub_account_groups.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedSubAccountGroupRecords(UUID voucherUUID, String name, String description);

    @Query("SELECT count(*) FROM sub_account_groups\n" +
            "WHERE sub_account_groups.uuid NOT IN(\n" +
            "SELECT sub_account_groups.uuid FROM sub_account_groups\n" +
            "LEFT JOIN voucher_sub_account_groups_pvt\n" +
            "ON voucher_sub_account_groups_pvt.sub_account_group_uuid = sub_account_groups.uuid\n" +
            "WHERE voucher_sub_account_groups_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_sub_account_groups_pvt.deleted_at IS NULL\n" +
            "AND sub_account_groups.deleted_at IS NULL) " +
            "AND sub_account_groups.deleted_at IS NULL \n" +
            "AND sub_account_groups.status= :status \n" +
            "AND (sub_account_groups.name ILIKE concat('%',:name,'%')\n" +
            "OR sub_account_groups.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedSubAccountGroupRecordsWithStatus(UUID voucherUUID, String name, String description, Boolean status);

}
