package tuf.webscaf.app.dbContext.master.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class TransactionExtraCalendarDto {
    private Long calendar_id;
    private String calendar_name;
    private LocalDateTime date;
    private Integer period_no;
    private Long period_id;
    private Integer quarter;
    private Boolean status;
}
