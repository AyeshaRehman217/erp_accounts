package tuf.webscaf.app.dbContext.master.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class CashReceiptVoucherDto {
    private Long id;
    private UUID transaction_id;
    private TransactionStatusDto transaction_status;
    private VoucherDto voucher;
    private TransactionDataDto transaction_data;
    private List<DocumentAttachmentDto> attachments;
    private TransactionViewControlDto view_controls;
    private TransactionExtraDto extra;
}
