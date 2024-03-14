package tuf.webscaf.springDocImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocImpl {

    private String name;

    private String description;

    @Schema(required = true)
    private String code;

    private Boolean isEntryAllowed;

    @Schema(required = true)
    private UUID accountTypeUUID;

    private UUID parentAccountUUID;

    private Boolean status;

    private Boolean isOpeningBalance;

    private BigDecimal openingBalance;

    private String openingBalanceDescription;

    private Boolean debit;

}
