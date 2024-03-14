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
public class ProfitAndLossRequestDto {
    private String id;
    private Long rowNum;
    private String columnNum;
    private Long indent;
    private CellAttributeDto cellAttributes;
    private UUID accountType;
    private UUID accountUUID;
    private UUID parentAccountUUID;
    private Long level;
    private String path;
    private String calculationFormula;
    private List<ProfitAndLossRequestDto> childList;
}
