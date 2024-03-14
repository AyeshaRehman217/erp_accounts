package tuf.webscaf.seeder.model;


import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountType {

    String uuid;
    String name;
    String desc;
    String code;

}
