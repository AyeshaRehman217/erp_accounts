package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CostCenterGroupsDocImpl {
    //Get Cost Center Group Id
    private List<UUID> costCenterUUID;

    private Boolean all;
}
