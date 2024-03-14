package tuf.webscaf.app.dbContext.slave.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveLedgerSummaryReportDto {
    //Before the Given Starting Date
    private String balanceBroughtForward;

    //Keeps difference of the two dates
    private String balanceCarriedForward;

    private List<SlaveReportingLedgerEntriesDto> ledgerEntrySummaryList;
}
