package tuf.webscaf.app.dbContext.slave.dto;

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
public class SlaveTrialBalanceReportDto {
    private UUID uuid;
    private UUID parentAccountUUID;
    private String accountCode;
    private String level;
    private String accountName;
    private String debit;
    private String credit;
    private List<SlaveTrialBalanceReportDto> childAccounts;
}
