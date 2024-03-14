package tuf.webscaf.springDocImpl;

import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.AccountGroupEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDocImpl {
    //get companyId
    Long companyId;
}
