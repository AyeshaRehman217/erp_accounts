package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarGroupRepository extends ReactiveCrudRepository<CalendarGroupEntity, Long> {

    Mono<CalendarGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<CalendarGroupEntity> findAllByDeletedAtIsNull();

    Flux<CalendarGroupEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Flux<CalendarGroupEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    Flux<CalendarGroupEntity> findAllByUuidInAndStatusAndDeletedAtIsNull(List<UUID> uuids, Boolean status);

    Mono<CalendarGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CalendarGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<CalendarGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
