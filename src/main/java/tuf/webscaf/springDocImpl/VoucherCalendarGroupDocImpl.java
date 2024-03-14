package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VoucherCalendarGroupDocImpl {
    //Get Calendar Group Id
    private List<UUID> calendarGroupUUID;
}
