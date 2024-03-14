package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeReportEntity;
import tuf.webscaf.app.dbContext.master.entity.TransactionStatusEntity;

@Repository
public interface BalanceAndIncomeReportRepository extends ReactiveCrudRepository<BalanceAndIncomeReportEntity, Long> {

    Mono<BalanceAndIncomeReportEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<BalanceAndIncomeReportEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<BalanceAndIncomeReportEntity> findFirstBySlugAndDeletedAtIsNull(String slug);

    Mono<BalanceAndIncomeReportEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<BalanceAndIncomeReportEntity> findFirstBySlugAndDeletedAtIsNullAndIdIsNot(String slug, Long id);
}
