package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowLineEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowReportEntity;

import java.util.UUID;

@Repository
public interface SlaveCashFlowReportRepository extends ReactiveCrudRepository<SlaveCashFlowReportEntity, Long> {
    Mono<SlaveCashFlowReportEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCashFlowReportEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveCashFlowReportEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    //Checks the validation of Slug
    Mono<SlaveCashFlowReportEntity> findBySlugAndDeletedAtIsNull(String slug);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
}
