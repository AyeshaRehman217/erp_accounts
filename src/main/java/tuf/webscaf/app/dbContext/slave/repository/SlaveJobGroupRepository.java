package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobWithJobGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherJobGroupPvtRepository;

import java.util.UUID;


@Repository
public interface SlaveJobGroupRepository extends ReactiveSortingRepository<SlaveJobGroupEntity, Long>, SlaveCustomJobWithJobGroupRepository, SlaveCustomVoucherJobGroupPvtRepository {
    Mono<SlaveJobGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveJobGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveJobGroupEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveJobGroupEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Flux<SlaveJobGroupEntity> findAllByDeletedAtIsNullAndNameContaining(String name, Sort sort);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //query for getting count of job groups for a given job
    @Query("select count(*) from job_groups \n" +
            "join job_group_job_pvt on job_groups.uuid = job_group_job_pvt.job_group_uuid \n" +
            "join jobs on job_group_job_pvt.job_uuid = jobs.uuid\n" +
            "where jobs.deleted_at is null\n" +
            "and job_groups.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and jobs.uuid = :jobUUID \n" +
            "and jobs.name ilike concat('%',:name,'%')")
    Mono<Long> countJobGroupsAgainstJob(UUID jobUUID, String name);

    @Query("select count(*) from job_groups \n" +
            "join job_group_job_pvt on job_groups.uuid = job_group_job_pvt.job_group_uuid \n" +
            "join jobs on job_group_job_pvt.job_uuid = jobs.uuid\n" +
            "where jobs.deleted_at is null\n" +
            "and job_groups.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and jobs.uuid = :jobUUID \n" +
            "and job_groups.status= :status \n" +
            "and jobs.name ilike concat('%',:name,'%')")
    Mono<Long> countJobGroupsAgainstJobWithStatus(UUID jobUUID, String name, Boolean status);

    //query for getting count of job groups for a given voucher
    @Query("select count(*) from job_groups\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_groups.uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and job_groups.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and job_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedJobGroupListAgainstVoucher(UUID voucherUUID, String name);

    //query for getting count of job groups for a given voucher with status
    @Query("select count(*) from job_groups\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_groups.uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and job_groups.status = :status " +
            "and job_groups.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and job_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMappedJobGroupListWithStatusAgainstVoucher(UUID voucherUUID, String name, Boolean status);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM job_groups\n" +
            "WHERE job_groups.uuid NOT IN(\n" +
            "SELECT job_groups.uuid FROM job_groups\n" +
            "LEFT JOIN voucher_job_group_pvt\n" +
            "ON voucher_job_group_pvt.job_group_uuid = job_groups.uuid\n" +
            "WHERE voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "AND voucher_job_group_pvt.deleted_at IS NULL\n" +
            "AND job_groups.deleted_at IS NULL) \n" +
            "AND job_groups.deleted_at IS NULL \n" +
            "AND job_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedJobGroupsAgainstVoucher(UUID voucherUUID, String name);

    @Query("SELECT count(*) FROM job_groups\n" +
            "WHERE job_groups.uuid NOT IN(\n" +
            "SELECT job_groups.uuid FROM job_groups\n" +
            "LEFT JOIN voucher_job_group_pvt\n" +
            "ON voucher_job_group_pvt.job_group_uuid = job_groups.uuid\n" +
            "WHERE voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "AND voucher_job_group_pvt.deleted_at IS NULL\n" +
            "AND job_groups.deleted_at IS NULL) \n" +
            "AND job_groups.deleted_at IS NULL \n" +
            "AND job_groups.status= :status\n" +
            "AND job_groups.name ILIKE concat('%',:name,'%')")
    Mono<Long> countUnMappedJobGroupsAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status);
}
