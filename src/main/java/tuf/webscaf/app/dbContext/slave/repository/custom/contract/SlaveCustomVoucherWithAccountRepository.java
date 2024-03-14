package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;

import java.util.UUID;

//This Interface is Extends in Slave Account Repository
public interface SlaveCustomVoucherWithAccountRepository {

    Flux<SlaveAccountEntity> indexAccount(String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> indexAccountWithStatus(String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> indexAccountWithEntryAllowed(String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page);

    Flux<SlaveAccountEntity> indexAccountWithStatusAndEntryAllowed(String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page);

    //This Function is used to Show Account List With "Account Code Filter" for A given Voucher
    Flux<SlaveAccountEntity> showAccountListWithAccountNameFilter(UUID voucherUUID, String name, String dp, String d, Integer size, Long page);

    //This Function is used to Show Account List With "Account Code Filter and Status" for A given Voucher
    Flux<SlaveAccountEntity> showAccountListWithAccountNameAndStatusFilter(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page);

    //This Function Is used to Show Accounts with "Given Name" Against Voucher
    Flux<SlaveAccountEntity> showAccountList(UUID voucherUUID, String name, String description, String code, String controlCode, String dp, String d, Integer size, Long page);

    //This Function Is used to Show Accounts with "Given Name Or Status Filter" Against Voucher
    Flux<SlaveAccountEntity> showAccountListWithStatusFilter(UUID voucherUUID, String name, String description, String code, String controlCode, Boolean status, String dp, String d, Integer size, Long page);

    //This Function is used to Show Account List With "Account Code Filter" for A given Voucher
    Flux<SlaveAccountEntity> showAccountListWithAccountCodeFilter(UUID voucherUUID, String accountCode, String dp, String d, Integer size, Long page);

    //This Function is used to Show Account List With "Account Code Filter and Status" for A given Voucher
    Flux<SlaveAccountEntity> showAccountListWithAccountCodeAndStatusFilter(UUID voucherUUID, String accountCode, Boolean status, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given company
    Flux<SlaveAccountEntity> showAccountListWithCompany(UUID voucherUUID, UUID companyUUID, String name, String description, String code, String dp, String d, Integer size, Long page);

    //show accounts for a voucher with given company and Status Filter
    Flux<SlaveAccountEntity> showAccountListWithCompanyAndStatusFilter(UUID voucherUUID, Boolean status, UUID companyUUID, String name, String description, String code, String dp, String d, Integer size, Long page);
}
