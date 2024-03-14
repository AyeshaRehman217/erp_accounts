package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveChartOfAccountDto {

    private UUID uuid;

    private String name;

    private UUID accountTypeUUID;

    private String accountTypeName;

    private Boolean isEntryAllowed;

    private Long level;

    private UUID parentAccountUUID;

    private String controlCode;
//
    private List<SlaveChartOfAccountDto> childAccount;
}
