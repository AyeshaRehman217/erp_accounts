package tuf.webscaf.app.dbContext.slave.dto;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountChildDto {
    private String id;
    private String accountUUID;
    private String formula;
}
