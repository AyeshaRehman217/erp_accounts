package tuf.webscaf.app.dbContext.master.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FinancialControlCodeDto {

    private String controlCode;

    private Long level;

}
