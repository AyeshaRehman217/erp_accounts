package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChildParentAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveParentAccountDto;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomChildParentAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomChildToParentAccountMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomParentAccountMapper;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomParentToChildAccountMapper;

import java.util.UUID;

public class SlaveCustomChildParentAccountRepositoryImpl implements SlaveCustomChildParentAccountRepository {
    private DatabaseClient client;
    private SlaveChildParentAccountDto slaveChildParentAccountDto;
    private SlaveParentAccountDto parentToChildDto;

    @Autowired
    public SlaveCustomChildParentAccountRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveChildParentAccountDto> showParentAgainstChild(UUID uuid) {
        String query = "WITH RECURSIVE childs (parent_account_uuid) as\n" +
                "( " +
                "  SELECT  accounts.parent_account_uuid,accounts.code  " +
                "from accounts where accounts.uuid= '" + uuid +
                "'\n" +
                " and accounts.deleted_at is null  \n" +
                "  UNION ALL\n" +
                "  SELECT accounts.parent_account_uuid,accounts.code from childs, accounts where accounts.uuid = childs.parent_account_uuid\n" +
                " and accounts.deleted_at is null \n" +
                "  ) \n" +
                "SELECT  * \n" +
                "FROM childs ";

        SlaveCustomChildToParentAccountMapper mapper = new SlaveCustomChildToParentAccountMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveChildParentAccountDto))
                .all();
    }


    // This query is used to get all parent accounts for a given financial account
    @Override
    public Flux<SlaveChildParentAccountDto> showAllParentsAgainstChild(UUID uuid) {
        String query = "WITH RECURSIVE rec (uuid) as\n" +
                "(\n" +
                "  SELECT accounts.uuid, accounts.code, accounts.parent_account_uuid from accounts where " +
                " accounts.deleted_at is null and uuid = '" + uuid +
                "'\n" +
                "  UNION ALL\n" +
                "\n" +
                "  SELECT accounts.uuid, accounts.code, accounts.parent_account_uuid from rec, accounts where accounts.deleted_at is null" +
                " and  accounts.uuid = rec.parent_account_uuid\n" +
                "  )\n" +
                "SELECT uuid, code\n" +
                "FROM rec";

        // This mapper is used for student financial account to get parent accounts
        SlaveCustomParentAccountMapper mapper = new SlaveCustomParentAccountMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveChildParentAccountDto))
                .all();
    }


    @Override
    public Flux<SlaveParentAccountDto> showAllChildAgainstParent(UUID uuid) {
        String query = "WITH RECURSIVE account (uuid) as\n" +
                "(\n" +
                "  SELECT accounts.uuid, accounts.name, accounts.parent_account_uuid from accounts \n" +
                " where accounts.parent_account_uuid= '" + uuid +
                "'    and accounts.deleted_at is null \n" +
                "  UNION ALL\n" +
                "  SELECT accounts.uuid, accounts.name, accounts.parent_account_uuid from account, accounts \n" +
                " where accounts.parent_account_uuid = account.uuid\n" +
                "    and accounts.deleted_at is null \n" +
                "  )\n" +
                "SELECT  name,uuid\n" +
                "FROM account";

        SlaveCustomParentToChildAccountMapper mapper = new SlaveCustomParentToChildAccountMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, parentToChildDto))
                .all();
    }
}
