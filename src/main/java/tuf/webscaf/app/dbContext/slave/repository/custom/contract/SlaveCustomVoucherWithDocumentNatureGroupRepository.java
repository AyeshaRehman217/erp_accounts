package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;

import java.util.UUID;

/**
 * This Custom Repository extends in Slave Document Nature Group Repository
 **/
public interface SlaveCustomVoucherWithDocumentNatureGroupRepository {
    Flux<SlaveDocumentNatureGroupEntity> showDocumentNatureGroupList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page);
}
