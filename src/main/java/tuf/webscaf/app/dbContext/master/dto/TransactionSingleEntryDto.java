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
public class TransactionSingleEntryDto {
    private Boolean status;
    private Long account_id;
    private String drcr;
}
