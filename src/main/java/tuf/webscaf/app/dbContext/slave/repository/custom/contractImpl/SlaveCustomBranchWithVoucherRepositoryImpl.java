package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomBranchWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherMapper;

import java.util.UUID;

public class SlaveCustomBranchWithVoucherRepositoryImpl implements SlaveCustomBranchWithVoucherRepository {
    private DatabaseClient client;
    private SlaveVoucherEntity slaveVoucherEntity;


    @Autowired
    public SlaveCustomBranchWithVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstBranch(UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_branch_pvt \n" +
                "on vouchers.uuid = voucher_branch_pvt.voucher_uuid\n" +
                "where voucher_branch_pvt.branch_uuid ='" + branchUUID +
                "' and vouchers.deleted_at is null\n" +
                "and voucher_branch_pvt.deleted_at is null\n" +
                "and (vouchers.name ILIKE  '%" + name + "%'" +
                "OR vouchers.description ILIKE '%" + description + "%') " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstBranchWithStatus(UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_branch_pvt \n" +
                "on vouchers.uuid = voucher_branch_pvt.voucher_uuid \n" +
                "where voucher_branch_pvt.branch_uuid ='" + branchUUID +
                "' and vouchers.status = " + status +
                " and vouchers.deleted_at is null \n" +
                "and voucher_branch_pvt.deleted_at is null \n" +
                "and (vouchers.name ILIKE  '%" + name + "%'" +
                "OR vouchers.description ILIKE '%" + description + "%') " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }
}
