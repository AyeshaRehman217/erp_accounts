package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.*;

import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomTransactionDocumentMapper implements BiFunction<Row, Object, SlaveDocumentAttachmentDto> {

    @Override
    public SlaveDocumentAttachmentDto apply(Row source, Object o) {


        return SlaveDocumentAttachmentDto.builder()
                .doc_id(source.get("documentUUID", UUID.class))
                .doc_bucket_uuid(source.get("docBucketUUID", UUID.class))
                .build();
    }

}
