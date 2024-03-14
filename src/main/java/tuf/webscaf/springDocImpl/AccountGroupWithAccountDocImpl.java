package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroupWithAccountDocImpl {

    private List<UUID> accountUUID;

    private Boolean all;
}
