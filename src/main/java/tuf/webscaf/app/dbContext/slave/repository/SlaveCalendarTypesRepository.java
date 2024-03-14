package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarTypesEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarTypeEntity;

import java.util.UUID;

@Repository
public interface SlaveCalendarTypesRepository extends ReactiveCrudRepository<SlaveCalendarTypeEntity, Long> {

    Flux<SlaveCalendarTypeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveCalendarTypeEntity> findAllByDeletedAtIsNullAndNameContainingAndDescriptionContaining(Sort sort, String name, String description);


    Mono<SlaveCalendarTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveCalendarTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCalendarTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch All Records in Index Function Based on Status Filter
    Flux<SlaveCalendarTypeEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status1, String description, Boolean status2);

}
