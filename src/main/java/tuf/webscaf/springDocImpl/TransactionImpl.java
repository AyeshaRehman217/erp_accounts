package tuf.webscaf.springDocImpl;

import lombok.*;
import tuf.webscaf.app.dbContext.master.entity.LedgerEntryEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransactionImpl {

    private Boolean status;
    private LocalDateTime transactionDate;
    private String description;
    private Long voucherNo;
    private Long branchId;
    private Long companyId;
    private Long calendarPeriodId;
    private Long jobId;
    private Long transactionStatusId;
    //Ledger Entries Fields
    private List<LedgerEntryEntity> ledgerEntryEntityList;
//    private List<Long> drAmount;
//    private List<Long> crAmount;
//    private List<Long> costCenterId;
//    private List<Long> profitCenterId;
//    //Get Document Entity
//    private List<Long> documentId;
//    private List<Long> accountId;
}
