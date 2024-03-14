package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveLedgerAccountDto {
    private String name;
    private String amount;
    private String description;
}
