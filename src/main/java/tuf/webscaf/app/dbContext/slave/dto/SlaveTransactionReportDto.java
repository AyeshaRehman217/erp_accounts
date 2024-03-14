package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import tuf.webscaf.app.dbContext.master.dto.VoucherDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTransactionReportDto {
    private Long id;
    private UUID transaction_id;
    private SlaveTransactionStatusDto transaction_status;
    private VoucherDto voucher;
    private SlaveTransactionDataDto transaction_data;
    private List<String> attachments;
}
