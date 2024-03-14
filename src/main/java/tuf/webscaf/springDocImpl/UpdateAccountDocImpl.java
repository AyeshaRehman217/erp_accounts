package tuf.webscaf.springDocImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDocImpl {

    private String name;

    private String description;

    @Schema(required = true)
    private String code;

    private Boolean isEntryAllowed;

    @Schema(required = true)
    private UUID accountTypeUUID;

    private UUID parentAccountUUID;

    private Boolean status;

}
