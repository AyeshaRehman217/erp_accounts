package tuf.webscaf.app.dbContext.master.dto;


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
public class ProfitAndLossStatementDto {
    private UUID accountUUID;
    private List<ProfitAndLossStatementDto> childList;
    private Double total;
}
