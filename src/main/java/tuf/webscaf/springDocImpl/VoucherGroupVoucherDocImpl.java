package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VoucherGroupVoucherDocImpl {
    //get voucherId
    private UUID voucherUUID;
}
