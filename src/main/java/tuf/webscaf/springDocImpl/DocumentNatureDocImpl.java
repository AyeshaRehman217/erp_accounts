package tuf.webscaf.springDocImpl;

import lombok.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNatureDocImpl {
    private String name;

    private String description;

    private UUID createdBy;

    List<String> documentNatureGroupId;
}
