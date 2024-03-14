package tuf.webscaf.seeder.model;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    String name;
    String code;
    String description;
    String accountTypeUUID;
    String parentAccountUUID;
    String companyUUID;
    String branchUUID;
    String status;
    String isEntryAllowed;
}
