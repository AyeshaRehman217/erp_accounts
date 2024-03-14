package tuf.webscaf.app.dbContext.master.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import tuf.webscaf.app.dbContext.master.entity.AccountCashFlowLinePvtEntity;

@Repository
public interface AccountCashFlowLinePvtRepository extends ReactiveCrudRepository<AccountCashFlowLinePvtEntity, Long> {
}
