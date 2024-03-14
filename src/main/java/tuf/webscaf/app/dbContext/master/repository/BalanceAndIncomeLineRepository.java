package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeLineEntity;
import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeReportEntity;

import java.util.UUID;

@Repository
public interface BalanceAndIncomeLineRepository extends ReactiveCrudRepository<BalanceAndIncomeLineEntity, Long> {

    Mono<BalanceAndIncomeLineEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<BalanceAndIncomeLineEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<BalanceAndIncomeLineEntity> findFirstByBalanceIncomeReportIdAndDeletedAtIsNullAndDeletedAtIsNull(Long balanceReportId);

    Mono<BalanceAndIncomeLineEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<BalanceAndIncomeLineEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

}
