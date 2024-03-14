package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherTypeCatalogueEntity;

import java.util.UUID;

@Repository
public interface SlaveVoucherTypeCatalogueRepository extends ReactiveCrudRepository<SlaveVoucherTypeCatalogueEntity, Long> {

    Flux<SlaveVoucherTypeCatalogueEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<SlaveVoucherTypeCatalogueEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveVoucherTypeCatalogueEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    //Fetch All Records With Status Filter
    Flux<SlaveVoucherTypeCatalogueEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status);

    //Count All Records With Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

}
