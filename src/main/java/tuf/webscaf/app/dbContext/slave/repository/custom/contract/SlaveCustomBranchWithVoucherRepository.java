package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;

public interface SlaveCustomBranchWithVoucherRepository {

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstBranch(UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstBranchWithStatus(UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

}
