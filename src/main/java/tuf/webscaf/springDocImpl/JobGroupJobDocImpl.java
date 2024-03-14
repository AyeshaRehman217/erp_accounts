package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JobGroupJobDocImpl {
    //get jobId
    private UUID jobUUID;

    private Boolean all;
}
