package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherGroupEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherGroupRepository extends ReactiveCrudRepository<VoucherGroupEntity, Long> {

    Mono<VoucherGroupEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<VoucherGroupEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<VoucherGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<VoucherGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<VoucherGroupEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<VoucherGroupEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);
}
