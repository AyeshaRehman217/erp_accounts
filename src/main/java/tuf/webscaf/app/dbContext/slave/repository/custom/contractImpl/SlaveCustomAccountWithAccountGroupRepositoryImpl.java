package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerAccountDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomAccountWithAccountGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomAccountGroupMapper;

import java.util.UUID;


public class SlaveCustomAccountWithAccountGroupRepositoryImpl implements SlaveCustomAccountWithAccountGroupRepository {
    private DatabaseClient client;
    private SlaveLedgerAccountDto slaveLedgerAccountDto;
    SlaveAccountGroupEntity slaveAccountGroupEntity;

    @Autowired
    public SlaveCustomAccountWithAccountGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    public Flux<SlaveAccountGroupEntity> listOfAccountGroups(UUID uuid, String name, Integer size, Long page, String dp, String d) {

        String query = "select account_groups.* from account_groups " +
                "join account_group_account_pvt on account_groups.uuid = account_group_account_pvt.account_group_uuid " +
                "join accounts on account_group_account_pvt.account_uuid = accounts.uuid " +
                "where accounts.deleted_at is null " +
                "and account_groups.deleted_at is null " +
                "and account_group_account_pvt.deleted_at is null " +
                "and accounts.uuid ='" + uuid +
                "' and account_groups.name ilike  '%" + name + "%'" +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveAccountGroupEntity> listOfAccountGroupsWithStatus(UUID uuid, String name, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select account_groups.* from account_groups " +
                "join account_group_account_pvt on account_groups.uuid = account_group_account_pvt.account_group_uuid " +
                "join accounts on account_group_account_pvt.account_uuid = accounts.uuid " +
                "where accounts.deleted_at is null " +
                "and account_groups.deleted_at is null " +
                "and account_group_account_pvt.deleted_at is null " +
                "and account_groups.status " + status +
                " and accounts.uuid ='" + uuid +
                "' and account_groups.name ilike  '%" + name + "%'" +
                "order by " + dp + " " + d +
                " limit " + size + " offset " + page;

        SlaveCustomAccountGroupMapper mapper = new SlaveCustomAccountGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveAccountGroupEntity))
                .all();
    }


}

