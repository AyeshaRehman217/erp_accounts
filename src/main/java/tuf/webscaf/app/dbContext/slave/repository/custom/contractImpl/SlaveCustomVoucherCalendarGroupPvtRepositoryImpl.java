package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherCalendarGroupPvtRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomCalendarGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherCalendarGroupPvtRepositoryImpl implements SlaveCustomVoucherCalendarGroupPvtRepository {
    private DatabaseClient client;
    private SlaveCalendarGroupEntity slaveCalendarGroupEntity;

    @Autowired
    public SlaveCustomVoucherCalendarGroupPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCalendarGroupEntity> showUnMappedCalendarGroupListAgainstVoucher(UUID voucherUUID, String name, String dp, String d, Integer size, Long page) {

        String query = "SELECT calendar_groups.* FROM calendar_groups\n" +
                "WHERE calendar_groups.uuid NOT IN(\n" +
                "SELECT calendar_groups.uuid FROM calendar_groups\n" +
                "LEFT JOIN voucher_calendar_group_pvt\n" +
                "ON voucher_calendar_group_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
                "WHERE voucher_calendar_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_calendar_group_pvt.deleted_at IS NULL\n" +
                "AND calendar_groups.deleted_at IS NULL)\n" +
                "AND calendar_groups.name ILIKE '%" + name + "%'" +
                "AND calendar_groups.deleted_at IS NULL " +
                "ORDER BY calendar_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        Flux<SlaveCalendarGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCalendarGroupEntity> showUnMappedCalendarGroupListAgainstVoucherWithStatus(UUID voucherUUID, String name, Boolean status, String dp, String d, Integer size, Long page) {

        String query = "SELECT calendar_groups.* FROM calendar_groups\n" +
                "WHERE calendar_groups.uuid NOT IN(\n" +
                "SELECT calendar_groups.uuid FROM calendar_groups\n" +
                "LEFT JOIN voucher_calendar_group_pvt\n" +
                "ON voucher_calendar_group_pvt.calendar_group_uuid = calendar_groups.uuid\n" +
                "WHERE voucher_calendar_group_pvt.voucher_uuid = '" + voucherUUID +
                "' AND voucher_calendar_group_pvt.deleted_at IS NULL\n" +
                "AND calendar_groups.deleted_at IS NULL)\n" +
                "AND calendar_groups.name ILIKE '%" + name + "%'" +
                "AND calendar_groups.status= " + status +
                " AND calendar_groups.deleted_at IS NULL " +
                "ORDER BY calendar_groups." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        Flux<SlaveCalendarGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCalendarGroupEntity> showMappedCalendarGroupsAgainstVoucher(UUID voucherUUID, String name, Integer size, Long page, String dp, String d) {

        String query = "select calendar_groups.* from calendar_groups\n" +
                "left join voucher_calendar_group_pvt \n" +
                "on calendar_groups.uuid = voucher_calendar_group_pvt.calendar_group_uuid\n" +
                "where voucher_calendar_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and calendar_groups.deleted_at is null\n" +
                "and voucher_calendar_group_pvt.deleted_at is null\n" +
                "and calendar_groups.name ILIKE  '%" + name + "%' " +
                "order by calendar_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();
    }

    @Override
    public Flux<SlaveCalendarGroupEntity> showMappedCalendarGroupsWithStatusAgainstVoucher(UUID voucherUUID, Boolean status, String name, Integer size, Long page, String dp, String d) {

        String query = "select calendar_groups.* from calendar_groups\n" +
                "left join voucher_calendar_group_pvt \n" +
                "on calendar_groups.uuid = voucher_calendar_group_pvt.calendar_group_uuid\n" +
                "where voucher_calendar_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and calendar_groups.status = " + status +
                " and calendar_groups.deleted_at is null\n" +
                "and voucher_calendar_group_pvt.deleted_at is null\n" +
                "and calendar_groups.name ILIKE  '%" + name + "%' " +
                "order by calendar_groups." + dp + " \n" + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarGroupMapper mapper = new SlaveCustomCalendarGroupMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarGroupEntity))
                .all();
    }
}
