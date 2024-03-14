package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveFlowLineTypeEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveIncomeSummaryDetailEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveIncomeSummaryEntity;

import java.util.UUID;

@Repository
public interface SlaveIncomeSummaryDetailRepository extends ReactiveSortingRepository<SlaveIncomeSummaryDetailEntity, Long> {
    Mono<SlaveIncomeSummaryDetailEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveIncomeSummaryDetailEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

//    @Query("SELECT income_summary_details.* FROM income_summary_details WHERE CAST(dr_amount AS text) ILIKE concat('%',:drAmount,'%') AND deleted_at IS NULL")
    Flux<SlaveIncomeSummaryDetailEntity> findAllByDeletedAtIsNull(Pageable pageable, String drAmount);

//    @Query("SELECT COUNT(*) FROM income_summary_details WHERE CAST(dr_amount AS text) ILIKE concat('%',:drAmount,'%') AND deleted_at IS NULL")
    Mono<Long> countByDeletedAtIsNull(String drAmount);
}
