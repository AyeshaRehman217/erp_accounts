package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import tuf.webscaf.app.dbContext.master.dto.LedgerRowAccountDto;
import tuf.webscaf.app.dbContext.master.dto.LedgerRowCostCenterDto;
import tuf.webscaf.app.dbContext.master.dto.LedgerRowProfitCenterDto;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveLedgerRowDto {
    private SlaveLedgerRowAccountDto account;
    private String description;
    private SlaveLedgerRowProfitCenterDto profit_center;
    private SlaveLedgerRowCostCenterDto cost_center;
    private Double cr;
    private Double dr;
}
