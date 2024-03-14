package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveLedgerReportDto {
    private String rowId;
    private String transactionUUID;
    private LocalDateTime date;
    private List<SlaveLedgerAccountDto> account;
    private String description;
    private String credit;
    private String debit;
    private String costCenter;
    private String profitCenter;
    private String job;
    private String netBalance;
}
