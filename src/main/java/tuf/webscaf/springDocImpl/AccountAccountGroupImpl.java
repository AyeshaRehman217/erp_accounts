package tuf.webscaf.springDocImpl;

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
public class AccountAccountGroupImpl {

    private String name;
    private Integer code;
    private String description;
    private UUID accountTypeUUID;
    private UUID parentAccountUUID;
    private UUID balanceIncomeLineUUID;
//    private Long searchCodeId;
    //Config module
    private UUID branchUUID;
    private UUID companyUUID;

    private Boolean status;
    private Boolean isEntryAllowed;

//    private List<Long> accountGroupId;
}
