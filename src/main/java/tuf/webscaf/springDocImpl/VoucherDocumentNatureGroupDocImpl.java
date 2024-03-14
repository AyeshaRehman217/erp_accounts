package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDocumentNatureGroupDocImpl {
    //Get Document Nature Group Id Entity
    private List<Long> documentNatureGroup;
}
