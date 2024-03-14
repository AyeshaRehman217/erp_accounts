package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveOpeningBalanceAccountDto {
    private Long id;

    private Long version;

    private UUID uuid;

    private Boolean status;

    private String name;

    private String code;

    private String description;

    private Boolean isEntryAllowed;

    private UUID accountTypeUUID;

    private UUID parentAccountUUID;

    private Boolean isOpeningBalance;

    private UUID openingBalanceUUID;

    private BigDecimal openingBalance;

    private String openingBalanceDescription;

    private Boolean debit;

    private UUID companyUUID;

    private String controlCode;

    private Long level;

    private UUID createdBy;

    private LocalDateTime createdAt;

    private UUID updatedBy;

    private LocalDateTime updatedAt;

    private UUID deletedBy;

    private LocalDateTime deletedAt;

    private UUID reqCompanyUUID;

    private UUID reqBranchUUID;

    private String reqCreatedBrowser;

    private String reqCreatedIP;

    private String reqCreatedPort;

    private String reqCreatedOS;

    private String reqCreatedDevice;

    private String reqCreatedReferer;

    private String reqUpdatedBrowser;

    private String reqUpdatedIP;

    private String reqUpdatedPort;

    private String reqUpdatedOS;

    private String reqUpdatedDevice;

    private String reqUpdatedReferer;

    private String reqDeletedBrowser;

    private String reqDeletedIP;

    private String reqDeletedPort;

    private String reqDeletedOS;

    private String reqDeletedDevice;

    private String reqDeletedReferer;

    private Boolean editable;

    private Boolean deletable;

    private Boolean archived;

    private List<SlaveOpeningBalanceAccountDto> childAccounts;
}
