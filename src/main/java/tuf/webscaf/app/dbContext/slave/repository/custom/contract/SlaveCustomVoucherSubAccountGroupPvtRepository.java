package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAccountGroupEntity;

import java.util.UUID;

public interface SlaveCustomVoucherSubAccountGroupPvtRepository {
    Flux<SlaveSubAccountGroupEntity> showUnMappedSubAccountGroupList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveSubAccountGroupEntity> showUnMappedSubAccountGroupListWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveSubAccountGroupEntity> showMappedSubAccountGroups(UUID voucherUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveSubAccountGroupEntity> showMappedSubAccountGroupsWithStatus(UUID voucherUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

}
