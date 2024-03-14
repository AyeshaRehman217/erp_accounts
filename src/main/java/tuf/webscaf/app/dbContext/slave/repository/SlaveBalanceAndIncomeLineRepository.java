package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeLineEntity;

import java.util.UUID;

@Repository
public interface SlaveBalanceAndIncomeLineRepository extends ReactiveCrudRepository<SlaveBalanceAndIncomeLineEntity, Long> {
    Flux<SlaveBalanceAndIncomeLineEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveBalanceAndIncomeLineEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveBalanceAndIncomeLineEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveBalanceAndIncomeLineEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
