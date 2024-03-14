package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveVoucherDto;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomVoucherDtoMapper;

import java.util.UUID;


public class SlaveCustomVoucherRepositoryImpl implements SlaveCustomVoucherRepository {
    private DatabaseClient client;
    private SlaveVoucherDto slaveVoucherDto;

    @Autowired
    public SlaveCustomVoucherRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveVoucherDto> showAllVoucherRecords(String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT vouchers.*, voucher_type_catalogues.slug  \n" +
                "FROM vouchers \n" +
                "JOIN voucher_type_catalogues ON voucher_type_catalogues.uuid = vouchers.voucher_type_catalogue_uuid \n" +
                "WHERE voucher_type_catalogues.deleted_at IS NULL \n" +
                "AND vouchers.deleted_at IS NULL \n";


        SlaveCustomVoucherDtoMapper mapper = new SlaveCustomVoucherDtoMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherDto))
                .all();
    }

    @Override
    public Flux<SlaveVoucherDto> showAllVoucherRecordsWithStatus(Boolean status, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT vouchers.*, voucher_type_catalogues.slug  \n" +
                "FROM vouchers \n" +
                "JOIN voucher_type_catalogues ON voucher_type_catalogues.uuid = vouchers.voucher_type_catalogue_uuid \n" +
                "WHERE voucher_type_catalogues.deleted_at IS NULL \n" +
                "AND vouchers.deleted_at IS NULL \n" +
                "AND vouchers.status = '" + status +"'";


        SlaveCustomVoucherDtoMapper mapper = new SlaveCustomVoucherDtoMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherDto))
                .all();
    }

    @Override
    public Mono<SlaveVoucherDto> showVoucherWithUUID(UUID voucherUUID) {
        String query = "SELECT vouchers.*, voucher_type_catalogues.slug  \n" +
                "FROM vouchers \n" +
                "JOIN voucher_type_catalogues ON voucher_type_catalogues.uuid = vouchers.voucher_type_catalogue_uuid \n" +
                "WHERE voucher_type_catalogues.deleted_at IS NULL \n" +
                "AND vouchers.deleted_at IS NULL \n" +
                "AND vouchers.uuid = '" +voucherUUID +"'";


        SlaveCustomVoucherDtoMapper mapper = new SlaveCustomVoucherDtoMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveVoucherDto))
                .one();
    }

}

