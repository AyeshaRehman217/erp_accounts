package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroupDocImpl {
    //get accountGroupId
    private UUID accountGroupUUID;
}
