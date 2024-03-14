package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveJobEntity;

import java.util.UUID;

public interface SlaveCustomVoucherWithJobRepository {
    Flux<SlaveJobEntity> indexJob(String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveJobEntity> indexJobWithStatus(String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveJobEntity> showJobListDataAgainstVoucher(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given company UUID
    Flux<SlaveJobEntity> showJobListWithCompanyAgainstVoucher(UUID voucherUUID, UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given branch UUID
    Flux<SlaveJobEntity> showJobListWithBranchAgainstVoucher(UUID voucherUUID, UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveJobEntity> showJobListDataAgainstVoucherWithStatus(UUID voucherUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given company UUID
    Flux<SlaveJobEntity> showJobListWithCompanyAgainstVoucherWithStatus(UUID voucherUUID, UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given branch UUID
    Flux<SlaveJobEntity> showJobListWithBranchAgainstVoucherWithStatus(UUID voucherUUID, UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);
}
