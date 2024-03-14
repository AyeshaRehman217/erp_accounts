package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class LedgerRowDto {
    private LedgerRowAccountDto account;
    private String description;
    private LedgerRowProfitCenterDto profit_center;
    private LedgerRowCostCenterDto cost_center;
    private BigDecimal cr;
    private BigDecimal dr;
}
