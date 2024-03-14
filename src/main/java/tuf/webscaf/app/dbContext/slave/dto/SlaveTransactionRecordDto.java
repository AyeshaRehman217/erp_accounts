package tuf.webscaf.app.dbContext.slave.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tuf.webscaf.app.dbContext.master.dto.VoucherDto;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTransactionRecordDto {
    private Long id;
    private UUID transaction_id;
    private SlaveTransactionStatusDto transaction_status;
    private VoucherDto voucher;
    private SlaveTransactionDataDto transaction_data;
    private List<SlaveDocumentAttachmentDto> attachments;
}
