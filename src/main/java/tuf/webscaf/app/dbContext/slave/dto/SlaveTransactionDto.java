package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import tuf.webscaf.app.dbContext.master.dto.VoucherDto;
import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTransactionDto {
    private Long id;
    private UUID transaction_id;
    private SlaveTransactionStatusDto transaction_status;
    private VoucherDto voucher;
    private UUID calendar_period_uuid;
    private LocalDateTime date;
    private UUID company_uuid;
    private UUID branch_uuid;
    private String transaction_description;
    private SlaveTransactionDataJobCenterDto job_center;
    private Double debit;
    private Double credit;
}
