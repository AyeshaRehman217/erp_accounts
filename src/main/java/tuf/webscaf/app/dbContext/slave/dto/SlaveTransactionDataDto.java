package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tuf.webscaf.app.dbContext.master.dto.LedgerRowDto;
import tuf.webscaf.app.dbContext.master.dto.TransactionDataJobCenterDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTransactionDataDto {
    private List<SlaveLedgerRowDto> rows;
    private Double debit;
    private Double credit;
    private UUID calendar_period_uuid;
    private LocalDateTime date;
    private UUID company_uuid;
    private UUID branch_uuid;
    private String transaction_description;
    private SlaveTransactionDataJobCenterDto job_center;
}
