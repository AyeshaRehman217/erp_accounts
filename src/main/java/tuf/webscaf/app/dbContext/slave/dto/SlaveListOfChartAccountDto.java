package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveListOfChartAccountDto {

    private UUID uuid;

    private Long level;

    private UUID parentAccountUUID;

    private String controlCode;
//
    private List<SlaveListOfChartAccountDto> childAccount;
}
