package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveSubAccountListDto {
    private Long id;

    private Long version;

    private UUID uuid;

    private String name;

    private String code;

    private String description;

    private Boolean isEntryAllowed;

    private UUID accountTypeUUID;

    private UUID parentAccountUUID;

    private UUID balanceIncomeLineUUID;

    private UUID branchUUID;

    private String controlCode;

    private UUID companyUUID;

    private Boolean status;

    private List<SlaveSubAccountListDto> childAccounts;

    private UUID createdBy;
    private UUID updatedBy;
    private UUID deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean archived;
    private Boolean editable;
}
