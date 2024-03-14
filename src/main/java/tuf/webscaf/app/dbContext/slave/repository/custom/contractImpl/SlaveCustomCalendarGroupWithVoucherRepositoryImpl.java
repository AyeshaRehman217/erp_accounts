package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomCalendarGroupWithVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherMapper;

import java.util.UUID;


public class SlaveCustomCalendarGroupWithVoucherRepositoryImpl implements SlaveCustomCalendarGroupWithVoucherRepository {
    private DatabaseClient client;
    private SlaveVoucherEntity slaveVoucherEntity;

    @Autowired
    public SlaveCustomCalendarGroupWithVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstCalendarGroup(UUID calendarGroupUUID, String name, String dp, String d, Integer size, Long page) {
        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_calendar_group_pvt \n" +
                "on vouchers.uuid = voucher_calendar_group_pvt.voucher_uuid\n" +
                "where voucher_calendar_group_pvt.calendar_group_uuid ='" + calendarGroupUUID +
                "' and vouchers.deleted_at is null\n" +
                "and voucher_calendar_group_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE  '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveVoucherEntity> showMappedVouchersAgainstCalendarGroupWithStatus(UUID calendarGroupUUID, Boolean status, String name, String dp, String d, Integer size, Long page) {
        String query = "select vouchers.* from vouchers\n" +
                "left join voucher_calendar_group_pvt \n" +
                "on vouchers.uuid = voucher_calendar_group_pvt.voucher_uuid\n" +
                "where voucher_calendar_group_pvt.calendar_group_uuid ='" + calendarGroupUUID +
                "' and vouchers.status = " + status +
                " and vouchers.deleted_at is null\n" +
                "and voucher_calendar_group_pvt.deleted_at is null\n" +
                "and vouchers.name ILIKE  '%" + name + "%' " +
                "order by vouchers." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomVoucherMapper mapper = new SlaveCustomVoucherMapper();

        Flux<SlaveVoucherEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherEntity))
                .all();

        return result;
    }
}
