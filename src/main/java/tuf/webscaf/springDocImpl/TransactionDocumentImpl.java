package tuf.webscaf.springDocImpl;

import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDocumentImpl {

    private Long documentId;
}
