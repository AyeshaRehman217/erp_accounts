package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;

import java.util.UUID;

public interface SlaveCustomVoucherAccountGroupPvtRepository {
    Flux<SlaveAccountGroupEntity> showUnMappedAccountGroupList(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountGroupEntity> showUnMappedAccountGroupListWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountGroupEntity> showMappedAccountGroups(UUID voucherUUID, String name, Integer size, Long page, String dp, String d);

    Flux<SlaveAccountGroupEntity> showMappedAccountGroupsWithStatus(UUID voucherUUID, Boolean status, String name, Integer size, Long page, String dp, String d);

}
