package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveOpeningBalanceAccountDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;

import java.util.UUID;

public interface SlaveCustomAccountRepository {
    // These methods will be used in accounts index
    Flux<SlaveAccountDto> showAllAccounts(String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountDto> showAllAccountsWithCompany(UUID companyUUID, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page);

    //Fetch All Accounts that are mapped with Sub Account groups and Sub Account Groups that are mapped with Vouchers
    Flux<SlaveAccountEntity> indexAllAccountsAgainstVoucherAndSubAccountGroupWithStatus(UUID voucherUUID, Boolean status, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> indexAllAccountsAgainstVoucherAndSubAccountGroupWithoutStatus(UUID voucherUUID, String name, String code, String description, String controlCode, String dp, String d, Integer size, Long page);

    Mono<SlaveOpeningBalanceAccountDto> showWithUuid(UUID uuid);
}
