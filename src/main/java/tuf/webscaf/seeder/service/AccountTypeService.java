package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountTypeEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountTypesRepository;
import tuf.webscaf.seeder.model.AccountType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.ArrayList;


@Service
public class AccountTypeService {

    @Autowired
    AccountTypesRepository accountTypesRepository;

    @Value("${server.zone}")
    private String zone;

    public Mono<String> saveAllAccountTypes(){

        ArrayList<AccountType> accountTypeList = new ArrayList<>();

        AccountType accountTypeA = AccountType.builder()
                .uuid("3dbab946-2827-403e-af14-cafc88d02859")
                .name("Income")
                .desc("Income")
                .code("1")
                .build();

        AccountType accountTypeB = AccountType.builder()
                .uuid("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .name("Expense")
                .desc("Expense")
                .code("2")
                .build();

        AccountType accountTypeC = AccountType.builder()
                .uuid("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")
                .name("Asset")
                .desc("Asset")
                .code("3")
                .build();

        AccountType accountTypeD = AccountType.builder()
                .uuid("722c6478-e459-4a64-a724-81bd90ee4f74")
                .name("Liability")
                .desc("Liability")
                .code("4")
                .build();

        AccountType accountTypeE = AccountType.builder()
                .uuid("762e81a6-15b9-4073-8485-e4d24bc834ce")
                .name("Equity")
                .desc("Equity")
                .code("5")
                .build();

        accountTypeList.add(accountTypeA);
        accountTypeList.add(accountTypeB);
        accountTypeList.add(accountTypeC);
        accountTypeList.add(accountTypeD);
        accountTypeList.add(accountTypeE);

        Flux<String> fres = Flux.just("");


        for (int i=0; i<accountTypeList.size(); i++){
            AccountType act = accountTypeList.get(i);
            Mono<String> res = checkAccountType(act);
            fres = fres.concatWith(res);
        }

        return fres.last();
    }

    public Mono<String> checkAccountType(AccountType accountType){
        return accountTypesRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(accountType.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveAccountType(accountType));
    }

    private Mono<String> saveAccountType(AccountType accountType){

        AccountTypeEntity accountTypeEntity = AccountTypeEntity.builder()
                .uuid(UUID.fromString(accountType.getUuid()))
                .name(accountType.getName())
                .slug(accountType.getName().toLowerCase())
                .description(accountType.getDesc())
                .code(accountType.getCode())
                .status(true)
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .createdBy(UUID.fromString("4d54bb9c-37c9-49c3-9566-1f77b554d218"))
                .build();
       return accountTypesRepository.save(accountTypeEntity)
                .flatMap(accountTypeEntity1 -> {
                    return Mono.just("");
        });
    }
}