package tuf.webscaf.app.dbContext.slave.repository.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.SlaveDocumentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionEntity;

import java.util.*;
import java.util.function.BiFunction;

public class SlaveCustomDocumentMapper implements BiFunction<Row, Object, MultiValueMap<UUID, SlaveDocumentAttachmentDto>> {
    MultiValueMap<UUID, SlaveDocumentAttachmentDto> documentMap = new LinkedMultiValueMap<UUID, SlaveDocumentAttachmentDto>();


    @Override
    public MultiValueMap<UUID, SlaveDocumentAttachmentDto> apply(Row source, Object o) {
        SlaveDocumentAttachmentDto documentDto = null;

        if (source.get("documentUUID", UUID.class) != null) {
            try {
                documentDto = SlaveDocumentAttachmentDto.builder()
                        .doc_id(source.get("documentUUID", UUID.class))
                        .doc_bucket_uuid(source.get("docBucketUUID", UUID.class))
                        .build();
            } catch (IllegalArgumentException i) {
            }
        }

        SlaveTransactionEntity slaveTransactionEntity = null;
        try {
            slaveTransactionEntity = SlaveTransactionEntity.builder()
                    .uuid(source.get("uuid", UUID.class))
                    .build();
        } catch (IllegalArgumentException i) {
        }


        MultiValueMap<UUID, SlaveDocumentAttachmentDto> documentRow = new LinkedMultiValueMap<>();
        if (slaveTransactionEntity != null) {

            documentMap.add(slaveTransactionEntity.getUuid(), documentDto);

            documentRow.addAll(slaveTransactionEntity.getUuid(), documentMap.get(slaveTransactionEntity.getUuid()));
        }

        return documentRow;
    }
}
