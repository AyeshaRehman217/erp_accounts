package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountTypeEntity;

import java.util.UUID;

@Repository
public interface SlaveAccountTypesRepository extends ReactiveCrudRepository<SlaveAccountTypeEntity, Long> {

    Flux<SlaveAccountTypeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveAccountTypeEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);

    Mono<SlaveAccountTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAccountTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveAccountTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<SlaveAccountTypeEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String code, Long id);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records With Status Filter
    Flux<SlaveAccountTypeEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name,Boolean status1, String description,Boolean status);
    //Count All Records With Status Filter
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name,Boolean status1, String description,Boolean status2);

}
