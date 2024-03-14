package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveCostCenterReportDto {

    private int id;

    private String code;

    private String name;

    private String description;

    private String costCenter;

    private String profitCenter;

    private String debit;

    private String credit;
}
