package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerRowDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class TransactionDataDto {
    private List<LedgerRowDto> rows;
    private Double debit;
    private Double credit;
    private UUID calendar_period_uuid;
    private LocalDateTime date;
    private UUID company_uuid;
    private UUID branch_uuid;
    private String transaction_description;
    private TransactionDataJobCenterDto job_center;
}
