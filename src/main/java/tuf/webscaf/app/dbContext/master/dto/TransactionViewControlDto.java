package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class TransactionViewControlDto {
    private Boolean ledger_description;
    private Boolean description;
    private Boolean job_center;
    private Boolean profit_center;
    private Boolean cost_center;
    private Boolean account_code;
    private Boolean account_name;
    private Boolean printable;
    private Boolean edit;
    private String entry_mode;
    private String entry_type;
}
