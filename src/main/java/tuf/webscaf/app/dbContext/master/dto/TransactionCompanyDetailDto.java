package tuf.webscaf.app.dbContext.master.dto;


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
public class TransactionCompanyDetailDto {
    private String company_name;
    private Long company_id;
    private UUID company_logo_id;
    private String branch_name;
    private Long branch_id;
}
