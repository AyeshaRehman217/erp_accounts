package tuf.webscaf.springDocImpl;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VoucherImpl {

    private Boolean status;
    private String name;
    private String description;
    private Long voucherTypeId;

    //Get Document Nature Group Id Entity
    private List<Long> documentNatureGroupId;

    //Get Account Group Id
    private List<Long> accountGroupId;

    //Get Profit Center Group Id
    private List<Long> profitCenterGroupId;

    //Get Cost Center Group Id
    private List<Long> costCenterGroupId;

    //Get Job Group Id
    private List<Long> jobGroupId;

    //Get Voucher Group Id
    private List<Long> voucherGroupId;
}
