package tuf.webscaf.app.dbContext.slave.repository.custom.contract;

import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.*;

import java.util.List;
import java.util.UUID;


public interface SlaveCustomTransactionRepository {

    public Mono<SlaveTransactionDto> showAllTransactions(UUID transactionUUID);

    public Mono<SlaveTransactionDto> showTransactionWithVoucherType(UUID transactionUUID, String voucherType);

    public Flux<SlaveLedgerRowDto> showAllLedgerRows(UUID transactionUUID);

    public Flux<SlaveDocumentAttachmentDto> showAllDocumentAttachments(UUID transactionUUID);

    //Used in Index Function
    public Flux<SlaveTransactionDto> listAllTransactions(String dp, String d, Integer size, Long page);

    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachments(String dp, String d, Integer size, Long page);

    public Flux<SlaveTransactionRecordDto> listOfTransactionLedgerRows(String dp, String d, Integer size, Long page);

    //Used in Index Function
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page);

    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page);

    public Flux<SlaveTransactionRecordDto> listOfTransactionLedgerRowsWithVoucherFilter(UUID voucherUUID, String dp, String d, Integer size, Long page);

    // These queries will be used with the static transaction vouchers
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

    public Flux<SlaveTransactionRecordDto> showTransactionLedgerRowsWithVoucherTypeFilter(UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

    // These queries will be used with the static transaction vouchers
    public Flux<SlaveTransactionDto> listAllTransactionsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

    public Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> listAllDocumentAttachmentsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

    public Flux<SlaveTransactionRecordDto> showTransactionLedgerRowsWithVoucherAndVoucherTypeFilter(UUID voucherUUID, UUID voucherTypeUUID, String dp, String d, Integer size, Long page);

}
