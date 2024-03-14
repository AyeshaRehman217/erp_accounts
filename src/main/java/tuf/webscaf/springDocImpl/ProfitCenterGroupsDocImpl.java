package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProfitCenterGroupsDocImpl {
    //Get Profit Center Group Id
    private List<UUID> profitCenterUUID;

    private Boolean all;
}
