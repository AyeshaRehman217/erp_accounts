package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherGroupVoucherPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherMapper;

import java.util.UUID;

public class SlaveCustomVoucherGroupVoucherPvtRepositoryImpl implements SlaveCustomVoucherGroupVoucherPvtRepository {
    private DatabaseClient client;
    private SlaveVoucherEntity slaveVoucherEntity;

    @Autowired
    public SlaveCustomVoucherGroupVoucherPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the existing Vouchers List Against Vouchers Group List
    @Override
    public Flux<SlaveVoucherEntity> showUnMappedVoucherListAgainstVoucherGroup(UUID voucherGroupUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "SELECT vouchers.* FROM vouchers\n" +
                "WHERE vouchers.uuid NOT IN(\n" +
                "SELECT vouchers.uuid FROM vouchers\n" +
                "LEFT JOIN voucher_group_voucher_pvt\n" +
                "ON voucher_group_voucher_pvt.voucher_uuid = vouchers.uuid\n" +
                "WHERE voucher_group_voucher_pvt.voucher_group_uuid = '" + voucherGroupUUID +
                "' AND voucher_group_voucher_pvt.deleted_at IS NULL\n" +
                "AND vouchers.deleted_at IS NULL)\n" +
                "AND (vouchers.name ILIKE '%" + name + "%' or vouchers.description ILIKE '%" + description + "%' )\n" +
                "AND vouchers.deleted_at IS NULL " +
                "ORDER BY vouchers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherEntity> showUnMappedVoucherListAgainstVoucherGroupWithStatus(UUID voucherGroupUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT vouchers.* FROM vouchers\n" +
                "WHERE vouchers.uuid NOT IN(\n" +
                "SELECT vouchers.uuid FROM vouchers\n" +
                "LEFT JOIN voucher_group_voucher_pvt\n" +
                "ON voucher_group_voucher_pvt.voucher_uuid = vouchers.uuid\n" +
                "WHERE voucher_group_voucher_pvt.voucher_group_uuid = '" + voucherGroupUUID +
                "' AND voucher_group_voucher_pvt.deleted_at IS NULL\n" +
                "AND vouchers.deleted_at IS NULL)\n" +
                "AND (vouchers.name ILIKE '%" + name + "%' or vouchers.description ILIKE '%" + description + "%' )\n" +
                "AND vouchers.deleted_at IS NULL " +
                "AND vouchers.status= " + status +
                " ORDER BY vouchers." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherEntity> showVoucherListAgainstVoucherGroup(UUID voucherGroupUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_group_voucher_pvt \n" +
                "on vouchers.uuid = voucher_group_voucher_pvt.voucher_uuid\n" +
                "where voucher_group_voucher_pvt.voucher_group_uuid = '" + voucherGroupUUID +
                "' and vouchers.deleted_at is null\n" +
                "and voucher_group_voucher_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;

    }

    @Override
    public Flux<SlaveVoucherEntity> showVoucherListWithStatusAgainstVoucherGroup(UUID voucherGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {

        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_group_voucher_pvt \n" +
                "on vouchers.uuid = voucher_group_voucher_pvt.voucher_uuid\n" +
                "where voucher_group_voucher_pvt.voucher_group_uuid = '" + voucherGroupUUID +
                "' and vouchers.status = " + status +
                " and vouchers.deleted_at is null\n" +
                "and voucher_group_voucher_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }
}
