package tuf.webscaf.app.dbContext.slave.repository.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureGroupEntity;
import tuf.webscaf.app.dbContext.slave.repository.custom.contract.SlaveCustomVoucherWithDocumentNatureGroupRepository;
import tuf.webscaf.app.dbContext.slave.repository.custom.mapper.SlaveCustomDocumentNatureGroupMapper;

import java.util.UUID;

public class SlaveCustomVoucherWithDocumentNatureGroupRepositoryImpl implements SlaveCustomVoucherWithDocumentNatureGroupRepository {
    private DatabaseClient client;
    private SlaveDocumentNatureGroupEntity slaveDocumentNatureGroupEntity;

    @Autowired
    public SlaveCustomVoucherWithDocumentNatureGroupRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveDocumentNatureGroupEntity> showDocumentNatureGroupList(UUID voucherUUID, String name, String description, String dp, String d, Integer size, Long page) {

        String query = "select document_nature_groups.* from document_nature_groups\n" +
                "left join voucher_document_nature_group_pvt \n" +
                "on document_nature_groups.uuid = voucher_document_nature_group_pvt.document_nature_group_uuid\n" +
                "where voucher_document_nature_group_pvt.voucher_uuid = '" + voucherUUID +
                "' and document_nature_groups.deleted_at is null\n" +
                "and voucher_document_nature_group_pvt.deleted_at is null\n" +
                "and (document_nature_groups.name ILIKE  '%" + name + "%' " +
                "or document_nature_groups.description  ILIKE  '%" + description + "%') \n" +
                "order by document_nature_groups." + dp + " \n" + d +
                " limit " + size + " offset " + page;

        SlaveCustomDocumentNatureGroupMapper mapper = new SlaveCustomDocumentNatureGroupMapper();

        Flux<SlaveDocumentNatureGroupEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveDocumentNatureGroupEntity))
                .all();

        return result;

    }
}
