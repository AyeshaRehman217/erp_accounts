package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveAccountModuleDto {
    private Long moduleId;
    private UUID moduleUUID;
    private String baseUrl;
    private String infoUrl;
    private String hostAddress;
}
