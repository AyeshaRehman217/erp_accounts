package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProfitCenterDocImpl {
    //get profit Center UUID
    private UUID profitCenterUUID;

    private Boolean all;
}
