package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CostCenterDocImpl {
    //get cost Center UUID
    private UUID costCenterUUID;

    private Boolean all;
}
