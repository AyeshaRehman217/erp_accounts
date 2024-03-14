package tuf.webscaf.app.dbContext.master.dto;

import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountsWithAccountGroupDto {

    private Long id;
    private Long version;
    private String name;
    private String description;
    private Long account_type_id;
    private Long parent_account_id;
    private Long balance_and_income_line_id;
    private Long search_code_id;
//    private List<AccountGroupEntity> accountGroupEntities;
    private UUID createdBy;
    private UUID updatedBy;
    private UUID deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean deletable;
    private Boolean archived;
    private Boolean editable;
}
