package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;

import java.util.UUID;


public interface SlaveCustomCompanyWithVoucherRepository {
    Flux<SlaveVoucherEntity> showMappedVouchersAgainstCompany(UUID companyUUID, String name, String description, String dp, String d, Integer size, Long page);

    Flux<SlaveVoucherEntity> showMappedVouchersAgainstCompanyWithStatus(UUID companyUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page);
}
