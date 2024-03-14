package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class TransactionDocumentDto {
    private List<DocumentAttachmentDto> attachments;
}
