package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomAccountWithAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherAccountGroupPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveAccountGroupRepository extends ReactiveCrudRepository<SlaveAccountGroupEntity, Long>, SlaveCustomAccountWithAccountGroupRepository, SlaveCustomVoucherAccountGroupPvtRepository {

    Flux<SlaveAccountGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveAccountGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveAccountGroupEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveAccountGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAccountGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveAccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveAccountGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    @Query("select count(*) from account_groups " +
            "join account_group_account_pvt " +
            "on account_groups.uuid = account_group_account_pvt.account_group_uuid " +
            "join accounts on account_group_account_pvt.account_uuid = accounts.uuid " +
            "where accounts.deleted_at is null " +
            "and account_group_account_pvt.deleted_at is null \n" +
            "and accounts.uuid = :accountUUID \n" +
            "and account_groups.name ilike concat('%', :name ,'%')")
    Mono<Long> countMappedAccountGroupAgainstAccount(UUID accountUUID, String name);

    @Query("select count(*) from account_groups " +
            "join account_group_account_pvt " +
            "on account_groups.uuid = account_group_account_pvt.account_group_uuid " +
            "join accounts on account_group_account_pvt.account_uuid = accounts.uuid " +
            "where accounts.deleted_at is null " +
            "and account_group_account_pvt.deleted_at is null \n" +
            "and accounts.uuid = :accountUUID \n" +
            "and account_groups.status = :status \n" +
            "and account_groups.name ilike concat('%', :name ,'%')")
    Mono<Long> countMappedAccountGroupAgainstAccountWithStatus(UUID accountUUID, String name, Boolean status);

    //query for getting count of account groups for a given voucher
    @Query("select count(*) from account_groups\n" +
            "left join voucher_account_group_pvt\n" +
            "on account_groups.uuid = voucher_account_group_pvt.account_group_uuid\n" +
            "where voucher_account_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and account_groups.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and account_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedAccountGroupList(UUID voucherUUID, String name);

    //query for getting count of account groups for a given voucher
    @Query("select count(*) from account_groups\n" +
            "left join voucher_account_group_pvt\n" +
            "on account_groups.uuid = voucher_account_group_pvt.account_group_uuid\n" +
            "where voucher_account_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and account_groups.status = :status " +
            "and account_groups.deleted_at is null\n" +
            "and voucher_account_group_pvt.deleted_at is null\n" +
            "and account_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedAccountGroupListWithStatus(UUID voucherUUID, String name, Boolean status);

    //used in the pvt mapping handler
    @Query("SELECT count(*) FROM account_groups\n" +
            "WHERE account_groups.uuid NOT IN(\n" +
            "SELECT account_groups.uuid FROM account_groups\n" +
            "LEFT JOIN voucher_account_group_pvt\n" +
            "ON voucher_account_group_pvt.account_group_uuid = account_groups.uuid\n" +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_account_group_pvt.deleted_at IS NULL\n" +
            "AND account_groups.deleted_at IS NULL) " +
            "AND account_groups.deleted_at IS NULL \n" +
            "AND account_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedAccountGroupRecords(UUID voucherUUID, String name);

    @Query("SELECT count(*) FROM account_groups\n" +
            "WHERE account_groups.uuid NOT IN(\n" +
            "SELECT account_groups.uuid FROM account_groups\n" +
            "LEFT JOIN voucher_account_group_pvt\n" +
            "ON voucher_account_group_pvt.account_group_uuid = account_groups.uuid\n" +
            "WHERE voucher_account_group_pvt.voucher_uuid = :voucherUUID" +
            " AND voucher_account_group_pvt.deleted_at IS NULL\n" +
            "AND account_groups.deleted_at IS NULL) " +
            "AND account_groups.deleted_at IS NULL \n" +
            "AND account_groups.status= :status \n" +
            "AND account_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedAccountGroupRecordsWithStatus(UUID voucherUUID, String name, Boolean status);
}
