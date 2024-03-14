package tuf.webscaf.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChildParentAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveParentAccountDto;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountControlCodeService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    public Mono<AccountEntity> settingParentControlCode(AccountEntity updatedAccount) {

        return slaveAccountRepository.showParentAgainstChild(updatedAccount.getParentAccountUUID())
                .collectList()
                .flatMap(parent -> {

                    //Getting All the Parent of the Given Child
                    String childCode = "";

                    //Get Count of All the Levels from child to parent
                    Long level = Long.valueOf(parent.size());

                    //Looping through all the child to Given Parent Node
                    for (SlaveChildParentAccountDto account : parent) {
                        //if the Parent Account is at index 0 do not add hyphen between the codes

                        if (parent.indexOf(account) == 0) {
                            childCode = account.getCode();
                        } else {
                            //Adding Hyphen between two codes
                            childCode = account.getCode() + "-" + childCode;
                        }
                    }

                    // append code generated with control code
                    childCode= childCode + "-" + updatedAccount.getCode();

                    //Setting Control Code
                    updatedAccount.setControlCode(childCode);
                    //Setting Level
                    updatedAccount.setLevel(level);

                    return Mono.just(updatedAccount);
                });
    }

    //Getting Child of All the given parent UUID
    public Mono<List<UUID>> gettingChildUUIDList(UUID accountUUID) {

        return slaveAccountRepository.showAllChildAgainstParent(accountUUID)
                .collectList()
                .flatMap(childAccounts -> {
                    List<UUID> childAccountUUIDList = new ArrayList<>();
                    for (SlaveParentAccountDto child : childAccounts) {
                        childAccountUUIDList.add(child.getParentAccountUUID());
                    }

                    return Mono.just(childAccountUUIDList);
                });
    }

    //This Function get All Parents Of the Child and set the Control Code
    public Mono<String> setControlCodes(UUID accountUUID, String controlCode) {

        //get All the Parent Account
        return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                .flatMap(childAccountEntity ->
                                //get Child Account Related to parent
                                accountRepository.findFirstByUuidAndDeletedAtIsNull(childAccountEntity.getParentAccountUUID())
                                        .flatMap(parentAccountEntity -> {
//                            parent.setControlCode(parent.getCode());
                                            String finalControlCode = controlCode;
                                            //if Counter Flag is False
//                                            if (!childAccountEntity.getCounterFlag()) {
                                                //get Parent Account Code and Concat with Child Account Code
                                                finalControlCode = parentAccountEntity.getCode() + "-" + finalControlCode;
//                                            }
                                            //return Control Code
                                            return Mono.just(finalControlCode)
                                                    .then(setControlCodes(parentAccountEntity.getUuid(), finalControlCode));
                                            //if parent Account is not present Set Code
                                        }).switchIfEmpty(Mono.just(controlCode))
                ).switchIfEmpty(Mono.just(controlCode));
    }


    public Mono<AccountEntity> getControlCodes(UUID accountUUID) {

        //Find Parent of Child Again
        return accountRepository.findByUuidAndDeletedAtIsNull(accountUUID)
                //Recursively Calling Function
                .flatMap(childAccountEntity -> setControlCodes(accountUUID, childAccountEntity.getCode())
                        .flatMap(controlCode -> {
                            childAccountEntity.setControlCode(controlCode);
                            long dashes = childAccountEntity.getControlCode().split("-").length - 1;
                            childAccountEntity.setLevel(dashes);
                            return accountRepository.save(childAccountEntity)
                                    .flatMap(accountEntity -> Mono.just(accountEntity));
                        })
                );
    }
}
