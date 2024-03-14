package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveTrialBalanceDto {
    private UUID accountUUID;
    private UUID accountTypeUUID;
    private String accountTypeName;
    private String accountName;
    private String accountCode;
    private Double debit;
    private Double credit;
    private Double netBalance;
    private Double balanceBroughtForward;
}
