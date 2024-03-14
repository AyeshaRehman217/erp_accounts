package tuf.webscaf.app.dbContext.slave.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTransactionDocumentListDto {
//    private UUID transaction_uuid;
    private MultiValueMap<UUID,SlaveDocumentAttachmentDto> attachment;
}
