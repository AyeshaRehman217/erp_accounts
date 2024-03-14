package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCostCenterGroupDocImpl {

    //Get Cost Center Group Id
    private List<UUID> costCenterGroupUUID;
}
