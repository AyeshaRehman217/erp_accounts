package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomJobGroupJobPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithJobRepository;

import java.util.UUID;

@Repository
public interface SlaveJobRepository extends ReactiveSortingRepository<SlaveJobEntity, Long>, SlaveCustomJobGroupJobPvtRepository, SlaveCustomVoucherWithJobRepository {
    Mono<SlaveJobEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveJobEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    //Find By Branch id In Config Module
    Mono<SlaveJobEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Mono<SlaveJobEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Flux<SlaveJobEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records based on Status Filter
    Flux<SlaveJobEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    //Count All Records based on Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

    //query for getting count of jobs for a given job group
    @Query("select count(*) from jobs \n" +
            "join job_group_job_pvt on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "join job_groups on job_group_job_pvt.job_group_uuid = job_groups.uuid\n" +
            "where jobs.deleted_at is null \n" +
            "and job_groups.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and job_groups.uuid = :jobGroupUUID\n" +
            "and (jobs.name ilike concat('%',:name,'%') " +
            "or jobs.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedJobsAgainstJobGroup(UUID jobGroupUUID, String name, String description);

    @Query("select count(*) from jobs \n" +
            "join job_group_job_pvt on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "join job_groups on job_group_job_pvt.job_group_uuid = job_groups.uuid\n" +
            "where jobs.deleted_at is null \n" +
            "and job_groups.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and jobs.status= :status\n" +
            "and job_groups.uuid = :jobGroupUUID\n" +
            "and (jobs.name ilike concat('%',:name,'%') " +
            "or jobs.description ilike concat('%',:description,'%') )")
    Mono<Long> countMappedJobsAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status);

    //    //query for getting count of jobs for a given voucher
    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt \n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt \n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid \n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID \n" +
            " and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and (jobs.name ILIKE concat('%',:name,'%') " +
            "or jobs.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countJobsDataWithVoucher(UUID voucherUUID, String name, String description);

    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt \n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt \n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid \n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID \n" +
            " and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and jobs.status= :status \n" +
            "and (jobs.name ILIKE concat('%',:name,'%') " +
            "or jobs.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countJobsDataWithVoucherWithStatus(UUID voucherUUID, String name, String description, Boolean status);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM jobs\n" +
            "WHERE jobs.uuid NOT IN(\n" +
            "SELECT jobs.uuid FROM jobs\n" +
            "LEFT JOIN job_group_job_pvt\n" +
            "ON job_group_job_pvt.job_uuid = jobs.uuid \n" +
            "WHERE job_group_job_pvt.job_group_uuid = :jobGroupUUID\n" +
            "AND job_group_job_pvt.deleted_at IS NULL\n" +
            "AND jobs.deleted_at IS NULL )\n" +
            "AND jobs.deleted_at IS NULL \n" +
            "AND (jobs.name ILIKE concat('%',:name,'%') " +
            "OR jobs.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedJobAgainstJobGroup(UUID jobGroupUUID, String name, String description);

    @Query("SELECT count(*) FROM jobs\n" +
            "WHERE jobs.uuid NOT IN(\n" +
            "SELECT jobs.uuid FROM jobs\n" +
            "LEFT JOIN job_group_job_pvt\n" +
            "ON job_group_job_pvt.job_uuid = jobs.uuid \n" +
            "WHERE job_group_job_pvt.job_group_uuid = :jobGroupUUID\n" +
            "AND job_group_job_pvt.deleted_at IS NULL\n" +
            "AND jobs.deleted_at IS NULL )\n" +
            "AND jobs.deleted_at IS NULL \n" +
            "AND (jobs.name ILIKE concat('%',:name,'%') " +
            "OR jobs.description ILIKE concat('%',:description,'%') )\n")
    Mono<Long> countUnMappedJobAgainstJobGroupWithStatus(UUID jobGroupUUID, String name, String description, Boolean status);

    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt\n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and jobs.company_uuid = :companyUUID\n" +
            "and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and (jobs.name ILIKE concat('%',:name,'%')\n" +
            "or jobs.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countWithCompanyAgainstVoucher(UUID voucherUUID, UUID companyUUID, String name, String description);

    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt\n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and jobs.company_uuid = :companyUUID\n" +
            "and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and jobs.status= :status \n" +
            "and (jobs.name ILIKE concat('%',:name,'%')\n" +
            "or jobs.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countWithCompanyAgainstVoucherWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status);

    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt\n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and jobs.branch_uuid = :branchId\n" +
            "and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and (jobs.name ILIKE concat('%',:name,'%')\n" +
            "or jobs.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countWithBranchAgainstVoucher(UUID voucherUUID, UUID branchUUID, String name, String description);

    @Query("select count(distinct jobs.uuid) from jobs\n" +
            "left join job_group_job_pvt\n" +
            "on jobs.uuid = job_group_job_pvt.job_uuid\n" +
            "left join voucher_job_group_pvt\n" +
            "on job_group_job_pvt.job_group_uuid = voucher_job_group_pvt.job_group_uuid\n" +
            "where voucher_job_group_pvt.voucher_uuid = :voucherUUID\n" +
            "and jobs.branch_uuid = :branchId\n" +
            "and jobs.deleted_at is null\n" +
            "and job_group_job_pvt.deleted_at is null\n" +
            "and voucher_job_group_pvt.deleted_at is null\n" +
            "and jobs.status= :status \n" +
            "and (jobs.name ILIKE concat('%',:name,'%')\n" +
            "or jobs.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countWithBranchAgainstVoucherWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status);
}
