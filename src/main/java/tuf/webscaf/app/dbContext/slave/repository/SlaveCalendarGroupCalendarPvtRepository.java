package tuf.webscaf.app.dbContext.slave.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupCalendarPvtEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarGroupCalendarPvtRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveCalendarGroupCalendarPvtRepository extends ReactiveCrudRepository<SlaveCalendarGroupCalendarPvtEntity, Long> {
    
    Mono<SlaveCalendarGroupCalendarPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCalendarGroupCalendarPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCalendarGroupCalendarPvtEntity> findByCalendarGroupUUIDAndAllAndDeletedAtIsNull(UUID calendarGroupUUID, Boolean all);

    //check on deleted at Function
    Mono<SlaveCalendarGroupCalendarPvtEntity> findFirstByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    Flux<SlaveCalendarGroupCalendarPvtEntity> findAllByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);

    Mono<SlaveCalendarGroupCalendarPvtEntity> findFirstByCalendarUUIDAndDeletedAtIsNull(UUID calendarUUID);

    Flux<SlaveCalendarGroupCalendarPvtEntity> findByCalendarGroupUUIDAndDeletedAtIsNull(UUID calendarGroupUUID);
    
    Flux<SlaveCalendarGroupCalendarPvtEntity> findAllByCalendarGroupUUIDAndCalendarUUIDInAndDeletedAtIsNull(UUID calendarGroupUUID, List<UUID> calendarUUID);

    Mono<SlaveCalendarGroupCalendarPvtEntity> findFirstByCalendarGroupUUIDAndCalendarUUIDAndDeletedAtIsNull(UUID calendarGroupUUID, UUID calendarUUID);
 }
