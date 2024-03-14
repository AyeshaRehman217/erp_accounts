package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChildParentAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveParentAccountDto;

import java.util.UUID;

//This Interface Will extends in Slave Account Repository
public interface SlaveCustomChildParentAccountRepository {

    //This Function is used to Fetch Parent of All the Child Accounts
    Flux<SlaveChildParentAccountDto> showParentAgainstChild(UUID uuid);

    //This Function is used to Fetch All Parent Accounts for given Financial Accounts
    Flux<SlaveChildParentAccountDto> showAllParentsAgainstChild(UUID uuid);

    //This Function is used to Fetch Child of All the Parent Account
    Flux<SlaveParentAccountDto> showAllChildAgainstParent(UUID uuid);
}
