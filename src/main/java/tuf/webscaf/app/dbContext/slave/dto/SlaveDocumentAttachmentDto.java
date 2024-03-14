package tuf.webscaf.app.dbContext.slave.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveDocumentAttachmentDto {
    private UUID doc_id;
    private UUID doc_bucket_uuid;
}
