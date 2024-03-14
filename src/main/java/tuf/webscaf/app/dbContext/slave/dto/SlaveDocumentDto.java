package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveDocumentDto {
    private Long documentId;
    private UUID docId;
    private Long docBucketId;
}
