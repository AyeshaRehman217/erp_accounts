package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeReportEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowReportEntity;

import java.util.UUID;

@Repository
public interface SlaveBalanceAndIncomeReportRepository extends ReactiveCrudRepository<SlaveBalanceAndIncomeReportEntity, Long> {
    //   @Query("select * from balance_and_income_reports where deleted_at is null")
//    Flux<SlaveBalanceAndIncomeReportEntity> findAllBy(Pageable pageable);
    Flux<SlaveBalanceAndIncomeReportEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveBalanceAndIncomeReportEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveBalanceAndIncomeReportEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveBalanceAndIncomeReportEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    //Checks the validation of Slug
    Mono<SlaveBalanceAndIncomeReportEntity> findBySlugAndDeletedAtIsNull(String slug);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
