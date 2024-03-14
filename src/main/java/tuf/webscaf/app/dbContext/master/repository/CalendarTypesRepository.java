package tuf.webscaf.app.dbContext.master.repository;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;


import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;
import tuf.webscaf.app.dbContext.master.entity.CalendarTypesEntity;

import java.util.UUID;

@Repository
public interface CalendarTypesRepository extends ReactiveCrudRepository<CalendarTypesEntity, Long> {

    Mono<CalendarTypesEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarTypesEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarTypesEntity> findFirstByNameIgnoreCaseAndPeriodsAndDeletedAtIsNull(String name, Integer periods);

    Mono<CalendarTypesEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CalendarTypesEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

//    Mono<CalendarTypesEntity> findFirstByNameIgnoreCaseAndPeriodsAndDeletedAtIsNullAndUuidIsNot(String name, Integer periods, UUID uuid);

}
