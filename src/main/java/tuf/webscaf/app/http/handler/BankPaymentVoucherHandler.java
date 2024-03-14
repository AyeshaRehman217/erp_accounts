package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.DocumentAttachmentDto;
import tuf.webscaf.app.dbContext.master.dto.LedgerRowDto;
import tuf.webscaf.app.dbContext.master.dto.TransactionDataDto;
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repository.*;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.repository.*;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Tag(name = "bankPaymentVoucherHandler")
public class BankPaymentVoucherHandler {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    TransactionStatusRepository transactionStatusRepository;

    @Autowired
    TransactionDocumentPvtRepository transactionDocumentPvtRepository;

    @Autowired
    SlaveTransactionDocumentPvtRepository slaveTransactionDocumentPvtRepository;

    @Autowired
    SlaveCashFlowAdjustmentRepository slaveCashFlowAdjustmentRepository;

    @Autowired
    CashFlowAdjustmentRepository bankFlowAdjustmentRepository;

    @Autowired
    LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    VoucherRepository voucherRepository;

    @Autowired
    CalendarPeriodsRepository calendarPeriodsRepository;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    SlaveTransactionRepository slaveTransactionRepository;

    //Ledger Entries Repositories
    @Autowired
    SlaveCostCenterRepository slaveCostCenterRepository;

    @Autowired
    SlaveProfitCenterRepository slaveProfitCenterRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    //Ledger Entries Repositories
    @Autowired
    CostCenterRepository costCenterRepository;

    @Autowired
    ProfitCenterRepository profitCenterRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    SubAccountGroupRepository subAccountGroupRepository;

    @Autowired
    VoucherTypeCatalogueRepository voucherTypeCatalogueRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_account_module.uri}")
    private String accountBaseURI;

    @Value("${server.erp_auth_module.uri}")
    private String authURI;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        String voucherUUID = serverRequest.queryParam("voucherUUID").map(String::toString).orElse("").trim();

        return voucherTypeCatalogueRepository.findFirstBySlugAndDeletedAtIsNull("bank-payment-voucher")
                .flatMap(voucherTypeCatalogueEntity -> {

                    // if voucher uuid is given
                    if (!voucherUUID.isEmpty()) {

                        //This Function Fetch All the Transaction WithOut Join with ledger and Document
                        Flux<SlaveTransactionDto> transactionEntityFlux = slaveTransactionRepository
                                .listAllTransactionsWithVoucherAndVoucherTypeFilter(UUID.fromString(voucherUUID), voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                        return transactionEntityFlux
                                .collectList()
                                .flatMap(transactionFlux -> {

                                    //Getting All the transaction UUID and Adding in List to pass it as argument to other
                                    List<UUID> transactionRecordUUID = new ArrayList<>();

                                    for (SlaveTransactionDto transactionUUID : transactionFlux) {
                                        // Adding Transaction UUID in list
                                        transactionRecordUUID.add(transactionUUID.getTransaction_id());
                                    }

                                    //Creating an Empty String for Transaction List
                                    String transactionList = "";
                                    //Iterating Over The Transaction List
                                    for (UUID val : transactionRecordUUID) {
                                        //Getting the last index of list size
                                        if (transactionRecordUUID.indexOf(val) == transactionRecordUUID.size() - 1) {
                                            //Adding '' around each value of list
                                            transactionList = transactionList + "'" + val + "'";
                                        } else {
                                            //Separating the list values with comma
                                            transactionList = transactionList + "'" + val + "' ,";
                                        }
                                    }

                                    //The Query Fetch All the Transactions With Ledger Join
                                    Flux<SlaveTransactionRecordDto> ledgerEntryEntityFlux = slaveTransactionRepository
                                            .showTransactionLedgerRowsWithVoucherAndVoucherTypeFilter(UUID.fromString(voucherUUID), voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                                    //The Query Fetch Document Map from Transaction and Document Join
                                    Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> documentAttachmentFlux = slaveTransactionRepository
                                            .listAllDocumentAttachmentsWithVoucherAndVoucherTypeFilter(UUID.fromString(voucherUUID), voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                                    return documentAttachmentFlux
                                            .collectList()
                                            .flatMap(documentList -> ledgerEntryEntityFlux
                                                    .collectList()
                                                    .flatMap(ledgerList -> {

                                                        //Removing Duplicates from Document MultiValued Map
                                                        List<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> finalDocList = removeDuplicateFromDocumentAttachmentList(documentList);

                                                        //Removing Duplicates from Ledger List
                                                        ledgerList = removeDuplicateInList(ledgerList);

                                                        //Creating and Empty List to Return Final Response
                                                        List<SlaveTransactionRecordDto> listRecord = new ArrayList<>();

                                                        //Iterating Over the Ledger List of Type Slave Transaction Dto
                                                        for (SlaveTransactionRecordDto transaction : ledgerList) {
                                                            //Setting Values
                                                            transaction.setTransaction_data(transaction.getTransaction_data());

                                                            List<SlaveDocumentAttachmentDto> attachments = new ArrayList<>(finalDocList.get(ledgerList.indexOf(transaction)).get(transaction.getTransaction_id()));
                                                            attachments.removeAll(Collections.singleton(null));

                                                            transaction.setAttachments(attachments);

                                                            //Adding in Final List
                                                            listRecord.add(transaction);
                                                        }

                                                        //Count All the Transaction Records
                                                        return slaveTransactionRepository.countRecordsWithVoucherAndVoucherTypeCatalogue(voucherTypeCatalogueEntity.getUuid(), UUID.fromString(voucherUUID))
                                                                .flatMap(count -> {
                                                                    if (listRecord.isEmpty()) {
                                                                        return responseIndexInfoMsg("Record does not exist", count);
                                                                    } else {
                                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", listRecord, count);
                                                                    }
                                                                });
                                                    })
                                            );
                                });
                    }

                    // if voucher uuid is not given
                    else {

                        //This Function Fetch All the Transaction WithOut Join with ledger and Document
                        Flux<SlaveTransactionDto> transactionEntityFlux = slaveTransactionRepository
                                .listAllTransactionsWithVoucherTypeFilter(voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());
                        return transactionEntityFlux
                                .collectList()
                                .flatMap(transactionFlux -> {

                                    //Getting All the transaction UUID and Adding in List to pass it as argument to other
                                    List<UUID> transactionRecordUUID = new ArrayList<>();

                                    for (SlaveTransactionDto transactionUUID : transactionFlux) {
                                        // Adding Transaction UUID in list
                                        transactionRecordUUID.add(transactionUUID.getTransaction_id());
                                    }

                                    //Creating an Empty String for Transaction List
                                    String transactionList = "";
                                    //Iterating Over The Transaction List
                                    for (UUID val : transactionRecordUUID) {
                                        //Getting the last index of list size
                                        if (transactionRecordUUID.indexOf(val) == transactionRecordUUID.size() - 1) {
                                            //Adding '' around each value of list
                                            transactionList = transactionList + "'" + val + "'";
                                        } else {
                                            //Separating the list values with comma
                                            transactionList = transactionList + "'" + val + "' ,";
                                        }
                                    }

                                    //The Query Fetch All the Transactions With Ledger Join
                                    Flux<SlaveTransactionRecordDto> ledgerEntryEntityFlux = slaveTransactionRepository
                                            .showTransactionLedgerRowsWithVoucherTypeFilter(voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                                    //The Query Fetch Document Map from Transaction and Document Join
                                    Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> documentAttachmentFlux = slaveTransactionRepository
                                            .listAllDocumentAttachmentsWithVoucherTypeFilter(voucherTypeCatalogueEntity.getUuid(), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                                    return documentAttachmentFlux
                                            .collectList()
                                            .flatMap(documentList -> ledgerEntryEntityFlux
                                                    .collectList()
                                                    .flatMap(ledgerList -> {

                                                        //Removing Duplicates from Document MultiValued Map
                                                        List<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> finalDocList = removeDuplicateFromDocumentAttachmentList(documentList);

                                                        //Removing Duplicates from Ledger List
                                                        ledgerList = removeDuplicateInList(ledgerList);

                                                        //Creating and Empty List to Return Final Response
                                                        List<SlaveTransactionRecordDto> listRecord = new ArrayList<>();

                                                        //Iterating Over the Ledger List of Type Slave Transaction Dto
                                                        for (SlaveTransactionRecordDto transaction : ledgerList) {
                                                            //Setting Values
                                                            transaction.setTransaction_data(transaction.getTransaction_data());

                                                            List<SlaveDocumentAttachmentDto> attachments = new ArrayList<>(finalDocList.get(ledgerList.indexOf(transaction)).get(transaction.getTransaction_id()));
                                                            attachments.removeAll(Collections.singleton(null));

                                                            transaction.setAttachments(attachments);

                                                            //Adding in Final List
                                                            listRecord.add(transaction);
                                                        }

                                                        //Count All the Transaction Records
                                                        return slaveTransactionRepository.countRecordsWithVoucherTypeCatalogue(voucherTypeCatalogueEntity.getUuid())
                                                                .flatMap(count -> {
                                                                    if (listRecord.isEmpty()) {
                                                                        return responseIndexInfoMsg("Record does not exist", count);
                                                                    } else {
                                                                        return responseIndexSuccessMsg("All Records Fetched Successfully", listRecord, count);
                                                                    }
                                                                });
                                                    })
                                            );
                                });
                    }
                }).switchIfEmpty(responseInfoMsg("Voucher Type Does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Voucher Type does not exist.Please Contact Developer."));

    }


    //This Function is used to Remove Duplicates from List
    public List<SlaveTransactionRecordDto> removeDuplicateInList(List<SlaveTransactionRecordDto> list) {

        //Creating a Refined List with No Duplicates
        List<SlaveTransactionRecordDto> newList = new LinkedList<>();
        List<UUID> transactionUUID = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            //if no Element Exists in list then add element in list
            if (!transactionUUID.contains(list.get(i).getTransaction_id())) {
                //Adding Transaction UUID in New List if No Element Exist
                newList.add(list.get(i));
                transactionUUID.add(list.get(i).getTransaction_id());
            } else {
                for (int j = 0; j < newList.size(); j++) {
                    //If the list element already exist than set the previous value
                    if (Objects.equals(newList.get(j).getTransaction_id(), list.get(i).getTransaction_id())) {
                        newList.set(j, list.get(i));
                    }

                }
            }
        }
        return newList;
    }

    public List<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> removeDuplicateFromDocumentAttachmentList(List<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> list) {

        //Creating and Empty MultiValues Map that returns without Duplicates
        List<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> newList = new LinkedList<>();
        List<UUID> transactionUUID = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            //Getting UUID from Multivalued Map Key Set
            UUID transUUID = UUID.fromString(list.get(i).keySet().toArray()[0].toString());

            //if Transaction UUID list does not already contain UUID
            if (!transactionUUID.contains(transUUID)) {
                //Adding if Transaction UUID does not exist
                newList.add(list.get(i));
                transactionUUID.add(transUUID);
            } else {
                for (int j = 0; j < newList.size(); j++) {

                    if (Objects.equals(UUID.fromString(list.get(j).keySet().toArray()[0].toString()), transUUID)) {
                        newList.set(j, list.get(i));
                    }

                }
            }
        }
        return newList;
    }

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
                .flatMap(transactionEntity -> slaveTransactionRepository.showTransactionWithVoucherType(transactionUUID, "bank-payment-voucher")
                        .flatMap(transaction -> slaveTransactionRepository.showAllLedgerRows(transactionUUID)
                                .collectList()
                                .flatMap(ledgerFlux -> slaveTransactionRepository.showAllDocumentAttachments(transactionUUID)
                                        .collectList()
                                        .flatMap(documentData -> {

                                            SlaveTransactionDataDto transactionDataDto = SlaveTransactionDataDto
                                                    .builder()
                                                    .rows(ledgerFlux)
                                                    .debit(transaction.getDebit())
                                                    .credit(transaction.getCredit())
                                                    .calendar_period_uuid(transaction.getCalendar_period_uuid())
                                                    .date(transaction.getDate())
                                                    .company_uuid(transaction.getCompany_uuid())
                                                    .branch_uuid(transaction.getBranch_uuid())
                                                    .transaction_description(transaction.getTransaction_description())
                                                    .job_center(transaction.getJob_center())
                                                    .build();


                                            List<SlaveDocumentAttachmentDto> attachments = new LinkedList<>();

                                            SlaveTransactionRecordDto finalDto = SlaveTransactionRecordDto
                                                    .builder()
                                                    .id(transaction.getId())
                                                    .transaction_id(transaction.getTransaction_id())
                                                    .transaction_status(transaction.getTransaction_status())
                                                    .voucher(transaction.getVoucher())
                                                    .transaction_data(transactionDataDto)
                                                    .build();

                                            for (SlaveDocumentAttachmentDto docs : documentData) {
                                                if (docs.getDoc_id() != null) {
                                                    attachments.add(docs);
                                                }
                                            }
                                            finalDto.setAttachments(attachments);
                                            return responseSuccessMsg("Record Fetched Successfully", finalDto)
                                                    .switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
                                        })
                                )
                        ).switchIfEmpty(responseInfoMsg("Record Does not Exist for Bank Payment Voucher."))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Record Does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_report")
    public Mono<ServerResponse> bankPaymentVoucherReport(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        UUID bankPaymentVoucherUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return apiCallService.getUserName(authURI + "api/v1/users/show/" + userId)
                .flatMap(email -> apiCallService.getVoucherResponse(accountBaseURI + "api/v1/bank-payment-vouchers/show/", bankPaymentVoucherUUID)
                                .flatMap(jsonNode -> {
                                    SlaveTransactionReportDto transactionReportDto = apiCallService.getTransactionData(jsonNode);
                                    return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionReportDto.getTransaction_data().getCalendar_period_uuid())
                                            .flatMap(calendarPeriodEntity -> calendarRepository.findByUuidAndDeletedAtIsNull(calendarPeriodEntity.getCalendarUUID())
                                                            .flatMap(calendar -> apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", transactionReportDto.getTransaction_data().getCompany_uuid())
                                                                            .flatMap(companyJson -> apiCallService.getCompanyEntity(companyJson)
                                                                                    .flatMap(company -> apiCallService.getDataWithUUID(configUri + "api/v1/branches/show/", transactionReportDto.getTransaction_data().getBranch_uuid())
                                                                                                    .flatMap(branchJson -> apiCallService.getBranchEntity(branchJson)
                                                                                                            .flatMap(branch -> {
                                                                                                                try {
                                                                                                                    List<SlaveCostCenterReportDto> costCenterEntityList = new ArrayList<>();

                                                                                                                    for (int i = 0; i < transactionReportDto.getTransaction_data().getRows().size(); i++) {

                                                                                                                        SlaveCostCenterReportDto costCenterReportDto = SlaveCostCenterReportDto.builder()
                                                                                                                                .id(i + 1)
                                                                                                                                .name(transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_name())
                                                                                                                                .description(transactionReportDto.getTransaction_data().getRows().get(i).getDescription())
                                                                                                                                .costCenter(transactionReportDto.getTransaction_data().getRows().get(i).getCost_center().getName())
                                                                                                                                .profitCenter(transactionReportDto.getTransaction_data().getRows().get(i).getProfit_center().getName())
                                                                                                                                .code(transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_code())
                                                                                                                                .debit(transactionReportDto.getTransaction_data().getRows().get(i).getDr().toString())
                                                                                                                                .credit(transactionReportDto.getTransaction_data().getRows().get(i).getCr().toString())
                                                                                                                                .build();
                                                                                                                        costCenterEntityList.add(costCenterReportDto);
                                                                                                                    }

                                                                                                                    String getTransactionDate = String.valueOf(transactionReportDto.getTransaction_data().getDate());

                                                                                                                    Date printDate = new Date();
                                                                                                                    DateFormat formattedPrintDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

                                                                                                                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
                                                                                                                    DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                                                                                                                    Date formatTransactionDate = inputFormat.parse(getTransactionDate);
                                                                                                                    String transactionDate = outputFormat.format(formatTransactionDate);

                                                                                                                    //dynamic parameters required for report
                                                                                                                    Map<String, Object> voucherParams = new HashMap<String, Object>();
                                                                                                                    voucherParams.put("CompanyName", company.getName().equals("null") ? "" : company.getName());
                                                                                                                    voucherParams.put("BranchName", branch.getName().equals("null") ? "" : branch.getName());
                                                                                                                    voucherParams.put("VoucherCatalogue", transactionReportDto.getVoucher().getVoucherType().getName().equals("null") ? "" : transactionReportDto.getVoucher().getVoucherType().getName());
                                                                                                                    voucherParams.put("VoucherName", transactionReportDto.getVoucher().getName().equals("null") ? "" : transactionReportDto.getVoucher().getName());
                                                                                                                    voucherParams.put("Voucher#", transactionReportDto.getTransaction_id().toString());
                                                                                                                    voucherParams.put("Date", transactionDate);
                                                                                                                    voucherParams.put("Cald", calendar.getName().equals("null") ? "" : calendar.getName());
                                                                                                                    voucherParams.put("Job", transactionReportDto.getTransaction_data().getJob_center().getName().equals("null") ? "" : transactionReportDto.getTransaction_data().getJob_center().getName());
                                                                                                                    voucherParams.put("Perd", calendarPeriodEntity.getPeriodNo().toString());
                                                                                                                    voucherParams.put("Qua", calendarPeriodEntity.getQuarter().toString());
                                                                                                                    voucherParams.put("TransactionDescription", transactionReportDto.getTransaction_data().getTransaction_description().equals("null") ? "" : transactionReportDto.getTransaction_data().getTransaction_description());
                                                                                                                    voucherParams.put("pDate", formattedPrintDate.format(printDate.getTime()));
                                                                                                                    voucherParams.put("email", email);
                                                                                                                    voucherParams.put("totalCredit", transactionReportDto.getTransaction_data().getCredit());
                                                                                                                    voucherParams.put("totalDebit", transactionReportDto.getTransaction_data().getDebit());
                                                                                                                    voucherParams.put("ip", serverRequest.exchange().getRequest().getRemoteAddress().getAddress().getHostAddress().equals("null") ? "" : serverRequest.exchange().getRequest().getRemoteAddress().getAddress().getHostAddress());
                                                                                                                    voucherParams.put("os", apiCallService.getClientOS(serverRequest).equals("null") ? "" : apiCallService.getClientOS(serverRequest));
                                                                                                                    voucherParams.put("browser", apiCallService.getClientBrowser(serverRequest).equals("null") ? "" : apiCallService.getClientBrowser(serverRequest));
                                                                                                                    voucherParams.put("voucherData", new JRBeanCollectionDataSource(costCenterEntityList));

                                                                                                                    String fileName = transactionReportDto.getTransaction_id().toString()+".pdf";

                                                                                                                    // Load the compiled Jasper file
                                                                                                                    JasperReport jasperReport = JasperCompileManager.compileReport(ResourceUtils.getFile("classpath:voucher.jrxml").getAbsolutePath());

                                                                                                                    //compile report in PDF format
                                                                                                                    JasperPrint voucherReport = JasperFillManager.fillReport(jasperReport, voucherParams, new JREmptyDataSource());

                                                                                                                    // Create a ResponseEntity with the PDF byte array and content type
                                                                                                                    return ServerResponse.ok()
                                                                                                                            .contentType(MediaType.APPLICATION_PDF)
                                                                                                                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                                                                                                                            .body(BodyInserters.fromValue(JasperExportManager.exportReportToPdf(voucherReport)));


//                                                                                                                    //create the report in PDF format
//                                                                                                                    return ServerResponse.ok()
//                                                                                                                            .contentType(APPLICATION_OCTET_STREAM)
//                                                                                                                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
//                                                                                                                            .body(BodyInserters.fromValue(JasperExportManager.exportReportToPdf(voucherReport)));

                                                                                                                } catch (Exception e) {
                                                                                                                    return responseErrorMsg("" + e.getMessage());
                                                                                                                }
                                                                                                            })).switchIfEmpty(responseInfoMsg("Branch doesn't Exit"))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Branch doesn't Exit. Please contact Developer"))
                                                                                    )).switchIfEmpty(responseInfoMsg("Company doesn't Exit"))
                                                                            .onErrorResume(ex -> responseErrorMsg("Company doesn't Exit. Please contact Developer"))
                                                            ).switchIfEmpty(responseInfoMsg("Calendar Against Calendar Period doesn't Exit"))
                                                            .onErrorResume(ex -> responseErrorMsg("Calendar Against Calendar Period doesn't Exit. Please contact Developer"))
                                            ).switchIfEmpty(responseInfoMsg("Calendar Period doesn't Exit"))
                                            .onErrorResume(ex -> responseErrorMsg("Calendar Period doesn't Exit. Please contact Developer"));
                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact Developer"))
                ).switchIfEmpty(responseInfoMsg("Unauthenticated user"))
                .onErrorResume(ex -> responseErrorMsg("Unauthenticated user. Please contact Developer"));
    }    

    //This Function is to Read Chunk of Json request
    public Mono<TransactionDataDto> transactionRequestBody(JsonNode transactionJsonNode) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        final JsonNode arrNode = transactionJsonNode.get("transaction_data");

        ObjectReader reader = objectMapper.readerFor(new TypeReference<TransactionDataDto>() {
        });

        TransactionDataDto transactionRowDto = null;

        try {
            transactionRowDto = reader.readValue(arrNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Mono.just(transactionRowDto);
    }

    //This Function is to Read Chunk of Json request
    public Mono<List<DocumentAttachmentDto>> transactionRecordRequestBody(JsonNode transactionRecordJsonNode) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        final JsonNode arrNode = transactionRecordJsonNode.get("attachments");

        ObjectReader reader = objectMapper.readerFor(new TypeReference<List<DocumentAttachmentDto>>() {
        });

        List<DocumentAttachmentDto> docDto = new ArrayList<>();

        try {
            docDto = reader.readValue(arrNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Mono.just(docDto);

    }

    public Mono<List<DocumentAttachmentDto>> storeDocument(TransactionEntity transactionEntity, List<DocumentAttachmentDto> transactionDto, String userId) {

        //List of Ledger Entry Entity
        List<TransactionDocumentPvtEntity> listOfTransactionDocumentPvt = new ArrayList<>();

        //Getting Attachment Object from Transaction Dto and Adding in List of Type Document Attachment Dto
        List<DocumentAttachmentDto> documentAttachmentDto = new ArrayList<>(transactionDto);

        //Creating Empty List for Doc Ids
        List<UUID> listOfDocument = new ArrayList<>();

        for (DocumentAttachmentDto documentRow : documentAttachmentDto) {
            listOfDocument.add(documentRow.getDoc_id());
        }


        //Empty List for Doc Ids from Json Request user Enters
        List<String> listOfDocumentsWithTransactions = new ArrayList<>();

        listOfDocument.forEach(uuid -> {
            if (uuid != null) {
                listOfDocumentsWithTransactions.add(uuid.toString());
            }
        });


        //Sending Doc Ids in Form data to check if doc Ids exist
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
        for (String listOfValues : listOfDocumentsWithTransactions) {
            formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
        }


        //posting Doc Ids in Drive Module Document Handler to get Only that doc Ids that exists
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
                .flatMap(jsonNode2 -> {
                    //Reading the Response "Data" Object from Json Node
                    final JsonNode arrNode2 = jsonNode2.get("data");

                    Map<String, String> documentMap = new HashMap<String, String>();

                    List<DocumentAttachmentDto> responseAttachments = new LinkedList<>();

                    if (arrNode2.isArray()) {
                        for (final JsonNode objNode : arrNode2) {
                            for (UUID documentIdData : listOfDocument) {
                                JsonNode key = objNode.get(String.valueOf(documentIdData));
                                if (key != null) {

                                    DocumentAttachmentDto documentAttachments = DocumentAttachmentDto.builder()
                                            .doc_id(UUID.fromString(key.get("docId").toString().replaceAll("\"", "")))
                                            .doc_name(key.get("filename").toString().replaceAll("\"", ""))
                                            .doc_bucket_uuid(UUID.fromString(key.get("docBucketUUID").toString().replaceAll("\"", "")))
                                            .build();

                                    documentMap.put(documentAttachments.getDoc_id().toString(), documentAttachments.getDoc_bucket_uuid().toString());

                                    responseAttachments.add(documentAttachments);
                                }
                            }
                        }
                    }


                    listOfDocument.removeAll(Arrays.asList("", null));

                    return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDocumentUUIDInAndDeletedAtIsNull(transactionEntity.getUuid(), listOfDocument)
                            .collectList()
                            .flatMap(removeList -> {


                                for (TransactionDocumentPvtEntity pvtEntity : removeList) {
                                    listOfDocument.remove(pvtEntity.getDocumentUUID());
                                    documentMap.remove(pvtEntity.getDocumentUUID().toString());
                                }


                                //List of Document ids to Store in Transaction Document Pvt Table
                                List<TransactionDocumentPvtEntity> listPvt = new ArrayList<TransactionDocumentPvtEntity>();

                                //iterating Over the Map Key "Doc Id" and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {
                                    TransactionDocumentPvtEntity transactionDocumentPvtEntity = TransactionDocumentPvtEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .transactionUUID(transactionEntity.getUuid())
                                            .createdBy(UUID.fromString(userId))
                                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCompanyUUID(transactionEntity.getReqCompanyUUID())
                                            .reqBranchUUID(transactionEntity.getReqBranchUUID())
                                            .reqCreatedIP(transactionEntity.getReqCreatedIP())
                                            .reqCreatedPort(transactionEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(transactionEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(transactionEntity.getReqCreatedOS())
                                            .reqCreatedDevice(transactionEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(transactionEntity.getReqCreatedReferer())
                                            .build();
                                    listPvt.add(transactionDocumentPvtEntity);
                                }


                                //Saving all Pvt Entries in Transaction Document Pvt Table
                                return transactionDocumentPvtRepository.saveAll(listPvt)
                                        .collectList()
                                        .flatMap(tranDocPvt -> {
                                            //Creating Final Document List to Update the Status
                                            List<UUID> finalDocumentList = new ArrayList<>();
                                            for (TransactionDocumentPvtEntity pvtData : tranDocPvt) {
                                                finalDocumentList.add(pvtData.getDocumentUUID());
                                            }


                                            //Empty List for Doc Ids from Json Request user Enters
                                            List<String> listOfDoc = new ArrayList<>();

                                            finalDocumentList.forEach(uuid -> {
                                                if (uuid != null) {
                                                    listOfDoc.add(uuid.toString());
                                                }
                                            });


                                            //Sending Doc Ids in Form data to check if doc Ids exist
                                            MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                            for (String listOfDocumentUUID : listOfDoc) {
                                                sendFormData.add("docId", listOfDocumentUUID);//iterating over multiple values and then adding in list
                                            }

                                            return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
                                                    .flatMap(document -> Mono.just(responseAttachments));
                                        })
                                        .flatMap(attach -> Mono.just(responseAttachments));
                            });
                });
    }

    public Mono<ServerResponse> storeLedgerEntryAndUploadDocument(TransactionDataDto transactionDto, TransactionEntity transactionRecord, String
            msg, String userId, JsonNode jsonValue) {

        //Creating Empty List of Ledger Entry Entity
        List<LedgerEntryEntity> listOfLedgerEntry = new ArrayList<>();

        //Getting row Object From Transaction Dto
        List<LedgerRowDto> ledgerRow = new ArrayList<>(transactionDto.getRows());

        if (ledgerRow.size() < 1) {
            return transactionRepository.delete(transactionRecord)
                    .flatMap(transactionDeleteEntity -> responseInfoMsg("Unable to Store Transaction.There is Something wrong please try again."))
                    .switchIfEmpty(responseInfoMsg("Add Ledger Entry First."))
                    .onErrorResume(ex -> responseErrorMsg("Unable to store transaction record.Please Contact Developer."));
        } else {
            //Creating Empty List for Cost Center,Profit Center and List of Accounts
            List<UUID> listOfCostCenter = new ArrayList<>();
            List<UUID> listOfProfitCenter = new ArrayList<>();
            List<UUID> listOfAccount = new ArrayList<>();

            // BigDecimal value for 0.0
            BigDecimal amount = new BigDecimal("0.0");

            BigDecimal sumOfDebitAmount = amount;
            BigDecimal sumOfCreditAmount = amount;

            List<UUID> bankAccount = new ArrayList<>();

            //Looping Over the Ledger Row and Setting Values in Ledger Entry
            for (LedgerRowDto ledger : ledgerRow) {

                LedgerEntryEntity ledgerData = LedgerEntryEntity.builder()
                        .uuid(UUID.randomUUID())
                        .drAmount(ledger.getDr())
                        .crAmount(ledger.getCr())
                        .description(ledger.getDescription().trim())
                        .profitCenterUUID(ledger.getProfit_center().getUuid())
                        .costCenterUUID(ledger.getCost_center().getUuid())
                        .accountUUID(ledger.getAccount().getUuid())
                        .transactionUUID(transactionRecord.getUuid())
                        .createdBy(UUID.fromString(userId))
                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                        .reqCompanyUUID(transactionRecord.getReqCompanyUUID())
                        .reqBranchUUID(transactionRecord.getReqBranchUUID())
                        .reqCreatedIP(transactionRecord.getReqCreatedIP())
                        .reqCreatedPort(transactionRecord.getReqCreatedPort())
                        .reqCreatedBrowser(transactionRecord.getReqCreatedBrowser())
                        .reqCreatedOS(transactionRecord.getReqCreatedOS())
                        .reqCreatedDevice(transactionRecord.getReqCreatedDevice())
                        .reqCreatedReferer(transactionRecord.getReqCreatedReferer())
                        .build();

                if (ledgerData.getDrAmount() == null) {
                    ledgerData.setDrAmount(amount);
                }

                if (ledgerData.getCrAmount() == null) {
                    ledgerData.setCrAmount(amount);
                }

                if (ledgerData.getDrAmount().compareTo(amount) != 0) {
                    //getting Sum of All the Debit Amount
                    sumOfDebitAmount = sumOfDebitAmount.add(ledgerData.getDrAmount());
                }


                if (ledgerData.getCrAmount().compareTo(amount) != 0) {
                    //getting Sum of All the Credit Amount
                    sumOfCreditAmount = sumOfCreditAmount.add(ledgerData.getCrAmount());


                    // add accounts that are on credit side
                    if (!bankAccount.contains(ledgerData.getAccountUUID())) {
                        bankAccount.add(ledgerData.getAccountUUID());
                    }
                }

                if (ledgerData.getCostCenterUUID() != null) {
                    listOfCostCenter.add(ledgerData.getCostCenterUUID());
                }

                if (ledgerData.getProfitCenterUUID() != null) {
                    listOfProfitCenter.add(ledgerData.getProfitCenterUUID());
                }

                if (ledgerData.getAccountUUID() != null) {
                    listOfAccount.add(ledgerData.getAccountUUID());
                }
                listOfLedgerEntry.add(ledgerData);

            }

            //Getting Distinct Values Fom the List of Cost center
            listOfCostCenter = listOfCostCenter.stream()
                    .distinct()
                    .collect(Collectors.toList());

            //Getting Distinct Values Fom the List of Profit center
            listOfProfitCenter = listOfProfitCenter.stream()
                    .distinct()
                    .collect(Collectors.toList());

            //Getting Distinct Values Fom the List of Accounts
            listOfAccount = listOfAccount.stream()
                    .distinct()
                    .collect(Collectors.toList());

            List<UUID> finalListOfProfitCenter = listOfProfitCenter;
            List<UUID> finalListOfAccount = listOfAccount;
            List<UUID> finalListOfCostCenter = listOfCostCenter;

            // bank Account in Debit
            UUID bankAccountUUID = bankAccount.get(0);


            if (!sumOfCreditAmount.equals(sumOfDebitAmount)) {
                return transactionRepository.delete(transactionRecord)
                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Unable to Store Transaction.There is Something wrong please try again."))
                        .switchIfEmpty(responseInfoMsg("Total Balance of Credit and Debit is not equal."))
                        .onErrorResume(ex -> responseErrorMsg("Unable to store transaction Record.Please Contact Developer."));
            }


            return accountRepository.findByUuidAndDeletedAtIsNull(bankAccountUUID)
                    //Check if The List of Account Ids Exist or not
                    .flatMap(bankAccountEntity -> accountRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfAccount)
                                    .collectList()
                                    .flatMap(accountEntityList -> costCenterRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfCostCenter)
                                                    .collectList()
                                                    .flatMap(costCenterEntityList -> profitCenterRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfProfitCenter)
                                                                    .collectList()
                                                                    .flatMap(profitCenterEntityList -> {

                                                                        // check if status is active in accounts
                                                                        for (AccountEntity accountEntity : accountEntityList) {
                                                                            // if given account is inactive
                                                                            if (!accountEntity.getStatus()) {
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Account status is inactive in " + accountEntity.getName()))
                                                                                        .switchIfEmpty(responseInfoMsg("Account status is inactive in " + accountEntity.getName()))
                                                                                        .onErrorResume(err -> responseErrorMsg("Account status is inactive in " + accountEntity.getName() + " .Please Contact Developer."));
                                                                            }

                                                                            // if entry is not allowed for given account
                                                                            if (!accountEntity.getIsEntryAllowed()) {
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Entry is not allowed for " + accountEntity.getName()))
                                                                                        .switchIfEmpty(responseInfoMsg("Entry is not allowed for " + accountEntity.getName()))
                                                                                        .onErrorResume(err -> responseErrorMsg("Entry is not allowed for " + accountEntity.getName() + " .Please Contact Developer."));
                                                                            }
                                                                        }

                                                                        // check if status is active in cost centers
                                                                        for (CostCenterEntity costCenterEntity : costCenterEntityList) {
                                                                            // if given cost center is inactive
                                                                            if (!costCenterEntity.getStatus()) {
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Cost Center status is inactive in " + costCenterEntity.getName()))
                                                                                        .switchIfEmpty(responseInfoMsg("Cost Center status is inactive in " + costCenterEntity.getName()))
                                                                                        .onErrorResume(err -> responseErrorMsg("Cost Center status is inactive in " + costCenterEntity.getName() + " .Please Contact Developer."));
                                                                            }
                                                                        }

                                                                        // check if status is active in profit centers
                                                                        for (ProfitCenterEntity profitCenterEntity : profitCenterEntityList) {
                                                                            // if given profit center is inactive
                                                                            if (!profitCenterEntity.getStatus()) {
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Profit Center status is inactive in " + profitCenterEntity.getName()))
                                                                                        .switchIfEmpty(responseInfoMsg("Profit Center status is inactive in " + profitCenterEntity.getName()))
                                                                                        .onErrorResume(err -> responseErrorMsg("Profit Center status is inactive in " + profitCenterEntity.getName() + " .Please Contact Developer."));
                                                                            }
                                                                        }

                                                                        //If the Provided List of Account Size is Greater than the Account Entity List Size
                                                                        if (finalListOfAccount.size() != accountEntityList.size()) {
                                                                            //Hard Delete Transaction Entity
                                                                            return transactionRepository.delete(transactionRecord)
                                                                                    .flatMap(delMsg -> responseInfoMsg("Account does not exist."))
                                                                                    .switchIfEmpty(responseInfoMsg("Account does not exist"))
                                                                                    .onErrorResume(ex -> responseErrorMsg("The Requested Account does not exist.Please Contact Developer."));

                                                                        }

                                                                        if (!finalListOfCostCenter.isEmpty()) {

                                                                            if (finalListOfCostCenter.size() != costCenterEntityList.size()) {
//
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(delMsg -> responseInfoMsg("Cost Center Does not exist"))
                                                                                        .switchIfEmpty(responseInfoMsg("Cost Center Does not exist"))
                                                                                        .onErrorResume(ex -> responseErrorMsg("Cost Center Does not exist.Please Contact Developer."));
                                                                            }
                                                                        }

                                                                        if (!finalListOfProfitCenter.isEmpty()) {

                                                                            if (finalListOfProfitCenter.size() != profitCenterEntityList.size()) {
                                                                                return transactionRepository.delete(transactionRecord)
                                                                                        .flatMap(delMsg -> responseInfoMsg("Profit Center Does not exist."))
                                                                                        .switchIfEmpty(responseInfoMsg("Profit Center Does not exist."))
                                                                                        .onErrorResume(ex -> responseErrorMsg("Profit Center Does not exist.Please Contact Developer."));
                                                                            }
                                                                        }

                                                                        if (bankAccount.size() > 1) {
                                                                            return transactionRepository.delete(transactionRecord)
                                                                                    .flatMap(delMsg -> responseInfoMsg("Transaction must have single Bank account in Credit"))
                                                                                    .switchIfEmpty(responseInfoMsg("Transaction must have single Bank account in Credit."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Transaction must have single Bank account in Credit.Please Contact Developer."));
                                                                        }

                                                                        //Adding the Two Values in Json Value Object Node
                                                                        ObjectNode transactionObjectNode = (ObjectNode) jsonValue;
                                                                        transactionObjectNode.put("id", transactionRecord.getId());
                                                                        transactionObjectNode.put("transaction_id", transactionRecord.getUuid().toString());
                                                                        transactionObjectNode.put("company_uuid", transactionRecord.getCompanyUUID().toString());
                                                                        transactionObjectNode.put("branch_uuid", transactionRecord.getBranchUUID().toString());

                                                                        return ledgerEntryRepository.saveAll(listOfLedgerEntry)
                                                                                .collectList()
                                                                                .flatMap(ledgerStore -> transactionRecordRequestBody(jsonValue)
                                                                                        .flatMap(docStore -> storeDocument(transactionRecord, docStore, userId)
                                                                                                .flatMap(attachmentData -> {

                                                                                                            //Creating Object Mapper
                                                                                                            ObjectMapper mapper = new ObjectMapper();
                                                                                                            //Adding list to Mapper
                                                                                                            ArrayNode array = mapper.valueToTree(attachmentData);
                                                                                                            //Put List to Object Node
                                                                                                            transactionObjectNode.putArray("attachments").addAll(array);

                                                                                                            return responseSuccessMsg(msg, transactionObjectNode)
                                                                                                                    .switchIfEmpty(responseSuccessMsg(msg, transactionObjectNode));
                                                                                                        }
                                                                                                )
                                                                                        ).switchIfEmpty(responseSuccessMsg(msg, transactionObjectNode))
                                                                                );
                                                                    }).switchIfEmpty(responseInfoMsg("Profit Center Record does not exist."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Profit Center Record does not exist.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("Cost Center Record does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("Cost Center Record does not exist.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Account Record does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Account Record does not exist.Please Contact Developer."))
                    ).switchIfEmpty(responseInfoMsg("Account Record does not exist."))
                    .onErrorResume(ex -> responseErrorMsg("Account Record does not exist.Please Contact Developer."));
        }

    }

    //This Function is used to read Json Node and Convert it to Json Object Node
    public ObjectNode readJsonNode(JsonNode jsonNode) {

        //Adding the Two Values in Json Value Object Node
        ObjectNode transactionObjectNode = (ObjectNode) jsonNode;

        // empty string with null value
        String emptyString = null;

        //Reading the Array Node from the json Node
        ArrayNode transactionDataRows = (ArrayNode) transactionObjectNode.get("transaction_data").get("rows");
        ArrayNode transactionsRows = new ArrayNode(JsonNodeFactory.instance);

        // transaction status uuid
        String transactionStatusUUID = transactionObjectNode.get("transaction_status").get("uuid").toString().replaceAll("\"", "");


        if (transactionStatusUUID.isEmpty()) {
            //parsing transaction status as Object Node
            ObjectNode transactionStatus = (ObjectNode) transactionObjectNode.get("transaction_status");

            transactionStatus.put("uuid", emptyString);
            transactionObjectNode.set("transaction_status", transactionStatus);
        }

        //iterating over the transaction data Ledger Rows
        transactionDataRows.forEach(row -> {

            //parsing row as Object Node
            ObjectNode objectRow = (ObjectNode) row;

            // cost center uuid
            String costCenterUUID = row.get("cost_center").get("uuid").toString().replaceAll("\"", "");

            //check if Cost Center UUID is equals to ""
            if (costCenterUUID.isEmpty()) {
                ObjectNode costCenter = (ObjectNode) objectRow.get("cost_center");
                //assigning uuid equals to null
                costCenter.put("uuid", emptyString);
                //setting cost Center Object back to Transaction Data
                objectRow.set("cost_center", costCenter);
            }

            // profit center uuid
            String profitCenterUUID = row.get("profit_center").get("uuid").toString().replaceAll("\"", "");

            if (profitCenterUUID.isEmpty()) {
                ObjectNode profitCenter = (ObjectNode) objectRow.get("profit_center");
                profitCenter.put("uuid", emptyString);
                objectRow.set("profit_center", profitCenter);
            }

            // cr amount
            String crAmount = row.get("cr").toString().replaceAll("\"", "");

            // if cr amount is null in response
            if (crAmount.isEmpty()) {
                objectRow.put("cr", emptyString);
            }

            // dr amount
            String drAmount = row.get("dr").toString().replaceAll("\"", "");

            // if dr amount is null in response
            if (drAmount.isEmpty()) {
                objectRow.put("dr", emptyString);
            }

            //adding modified cost center and profit center to ledger rows
            transactionsRows.add(objectRow);
        });

        ObjectNode transactionData = (ObjectNode) transactionObjectNode.get("transaction_data");


        // job center uuid
        String jobCenterUUID = transactionData.get("job_center").get("uuid").toString().replaceAll("\"", "");

        if (jobCenterUUID.isEmpty()) {
            ObjectNode jobCenter = (ObjectNode) transactionData.get("job_center");
            jobCenter.put("uuid", emptyString);
            transactionData.set("job_center", jobCenter);
        }

        transactionData.set("rows", transactionsRows);
        transactionObjectNode.set("transaction_data", transactionData);

        //Reading the Array Node from the json Node
        ArrayNode attachmentRows = (ArrayNode) transactionObjectNode.get("attachments");
        ArrayNode attachmentsList = new ArrayNode(JsonNodeFactory.instance);

        //iterating over the transaction data Ledger Rows
        attachmentRows.forEach(attachment -> {

            //parsing row as Object Node
            ObjectNode objectRow1 = (ObjectNode) attachment;

            String docId = attachment.get("doc_id").toString().replaceAll("\"", "");

            if (docId.isBlank()) {
                objectRow1.put("doc_id", emptyString);
            }

            String docBucketUUID = attachment.get("doc_bucket_uuid").toString().replaceAll("\"", "");

            if (docBucketUUID.isBlank()) {
                objectRow1.put("doc_bucket_uuid", emptyString);
            }
            //adding modified cost center and profit center to ledger rows
            attachmentsList.add(objectRow1);
        });

        //setting modified attachments back to object Node
        transactionObjectNode.set("attachments", attachmentsList);

        // return the object node of request body
        return transactionObjectNode;
    }


    //This Function is used to return Json Node in response and replace all null values in Json Object Node
    public ObjectNode updateJsonNode(JsonNode jsonNode) {

        //Adding the Two Values in Json Value Object Node
        ObjectNode transactionObjectNode = (ObjectNode) jsonNode;

        // empty string to replace with null value
        String emptyString = "";

        //Reading the Array Node from the json Node
        ArrayNode transactionDataRows = (ArrayNode) transactionObjectNode.get("transaction_data").get("rows");
        ArrayNode transactionsRows = new ArrayNode(JsonNodeFactory.instance);

        //iterating over the transaction data Ledger Rows
        transactionDataRows.forEach(row -> {

            //parsing row as Object Node
            ObjectNode objectRow = (ObjectNode) row;

            //check if Cost Center UUID is equals to null
            if (row.get("cost_center").get("uuid").isNull()) {
                ObjectNode costCenter = (ObjectNode) objectRow.get("cost_center");
                //assigning empty string to uuid
                costCenter.put("uuid", emptyString);
                //setting cost Center Object back to Transaction Data
                objectRow.set("cost_center", costCenter);
            }

            if (row.get("profit_center").get("uuid").isNull()) {
                ObjectNode profitCenter = (ObjectNode) objectRow.get("profit_center");
                profitCenter.put("uuid", emptyString);
                objectRow.set("profit_center", profitCenter);
            }

            // if cr amount is null in response
            if (row.get("cr").isNull()) {
                objectRow.put("cr", emptyString);
            }

            // if dr amount is null in response
            if (row.get("dr").isNull()) {
                objectRow.put("dr", emptyString);
            }

            //adding modified cost center and profit center to ledger rows
            transactionsRows.add(objectRow);
        });

        ObjectNode transactionData = (ObjectNode) transactionObjectNode.get("transaction_data");

        if (transactionData.get("job_center").get("uuid").isNull()) {
            ObjectNode jobCenter = (ObjectNode) transactionData.get("job_center");
            jobCenter.put("uuid", emptyString);
            transactionData.set("job_center", jobCenter);
        }

        transactionData.set("rows", transactionsRows);
        transactionObjectNode.set("transaction_data", transactionData);

        //Reading the Array Node from the json Node
        ArrayNode attachmentRows = (ArrayNode) transactionObjectNode.get("attachments");
        ArrayNode attachmentsList = new ArrayNode(JsonNodeFactory.instance);

        //iterating over the transaction data Ledger Rows
        attachmentRows.forEach(attachment -> {

            //parsing row as Object Node
            ObjectNode objectRow1 = (ObjectNode) attachment;

            String docId = attachment.get("doc_id").toString().replaceAll("\"", "");

            if (docId.isBlank()) {
                objectRow1.put("doc_id", emptyString);
            }

            String docBucketUUID = attachment.get("doc_bucket_uuid").toString().replaceAll("\"", "");

            if (docBucketUUID.isBlank()) {
                objectRow1.put("doc_bucket_uuid", emptyString);
            }
            //adding modified cost center and profit center to ledger rows
            attachmentsList.add(objectRow1);
        });

        //setting modified attachments back to object Node
        transactionObjectNode.set("attachments", attachmentsList);

        // return the object node of request body
        return transactionObjectNode;
    }

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");
        }

        return serverRequest.bodyToMono(JsonNode.class)
                .flatMap(jsonValue -> {

                    ObjectNode jsonNode = readJsonNode(jsonValue);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    JsonNode transactionJsonNode = null;
                    try {
                        transactionJsonNode = objectMapper.readTree(jsonNode.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    JsonNode finalTransactionJsonNode = transactionJsonNode;
                    return transactionRequestBody(transactionJsonNode)
                            .flatMap(entity -> {

                                UUID transactionStatusUUID = null;
                                if (!jsonNode.get("transaction_status").get("uuid").isNull()) {
                                    transactionStatusUUID = UUID.fromString(jsonNode.get("transaction_status").get("uuid").toString().replaceAll("\"", ""));
                                }

                                //Building Transaction Entity
                                TransactionEntity transactionEntity = TransactionEntity.builder()
                                        .transactionDate(entity.getDate())
                                        .uuid(UUID.randomUUID())
                                        .description(entity.getTransaction_description().trim())
                                        .module("account")
                                        .calendarPeriodUUID(entity.getCalendar_period_uuid())
                                        .branchUUID(UUID.fromString(reqBranchUUID))
                                        .companyUUID(UUID.fromString(reqCompanyUUID))
                                        .transactionStatusUUID(transactionStatusUUID)
                                        .jobUUID(entity.getJob_center().getUuid())
                                        .voucherUUID(UUID.fromString(jsonNode.get("voucher").get("uuid").toString().replaceAll("\"", "")))
                                        .createdBy(UUID.fromString(userId))
                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(reqIp)
                                        .reqCreatedPort(reqPort)
                                        .reqCreatedBrowser(reqBrowser)
                                        .reqCreatedOS(reqOs)
                                        .reqCreatedDevice(reqDevice)
                                        .reqCreatedReferer(reqReferer)
                                        .build();


                                // check if calendar period uuid exists
                                return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getCalendarPeriodUUID())
                                        //check if voucher uuid exists
                                        .flatMap(calendarPeriodEntity -> voucherRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getVoucherUUID())
                                                .flatMap(voucherEntity -> voucherTypeCatalogueRepository.findByUuidAndDeletedAtIsNull(voucherEntity.getVoucherTypeCatalogueUUID())
                                                        .flatMap(voucherTypeCatalogueEntity -> {

                                                            // if given calendar period is inactive
                                                            if (!calendarPeriodEntity.getIsOpen()) {
                                                                return responseInfoMsg("Calendar Period status is inactive");
                                                            }
                                                            // if given Voucher Type is inactive
                                                            if (!voucherTypeCatalogueEntity.getStatus()) {
                                                                return responseInfoMsg("Voucher Type status is inactive");
                                                            }

                                                            // if given voucher type is not bank payment voucher
                                                            if (!voucherTypeCatalogueEntity.getSlug().equals("bank-payment-voucher")) {
                                                                return responseInfoMsg("Voucher Type must be Bank Payment for this Transaction Voucher");
                                                            }

                                                            // if given voucher is inactive
                                                            if (!voucherEntity.getStatus()) {
                                                                return responseInfoMsg("Voucher status is inactive");
                                                            }

                                                            if (transactionEntity.getTransactionDate().isBefore(calendarPeriodEntity.getStartDate())) {
                                                                return responseInfoMsg("Transaction Entries are not opened for given Calendar Period");
                                                            }

                                                            if (transactionEntity.getTransactionDate().isAfter(calendarPeriodEntity.getEndDate())) {
                                                                return responseInfoMsg("Transaction Entries are closed for given Calendar Period");
                                                            }

                                                            //check If Transaction Status and Job uuid Exists
                                                            if (transactionEntity.getJobUUID() != null && transactionEntity.getTransactionStatusUUID() != null) {

                                                                // check if transaction status uuid exists
                                                                return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getTransactionStatusUUID())
                                                                        // check if job uuid exists
                                                                        .flatMap(transactionStatusEntity -> jobRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getJobUUID())
                                                                                .flatMap(jobEntity -> {

                                                                                    // if given transaction status is inactive
                                                                                    if (!transactionStatusEntity.getStatus()) {
                                                                                        return responseInfoMsg("Transaction Status's status is inactive");
                                                                                    }
                                                                                    // if given job is inactive
                                                                                    if (!jobEntity.getStatus()) {
                                                                                        return responseInfoMsg("Job status is inactive");
                                                                                    }

                                                                                    transactionEntity.setJobUUID(jobEntity.getUuid());
                                                                                    return transactionRepository.save(transactionEntity)
                                                                                            .flatMap(transactionDB -> storeLedgerEntryAndUploadDocument(entity, transactionDB, "Record Stored Successfully", userId, finalTransactionJsonNode)
                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Transactions.There is something wrong please try again."))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."))
                                                                                            );
                                                                                }).switchIfEmpty(responseInfoMsg("The Requested Job Does not exist"))
                                                                                .onErrorResume(ex -> responseErrorMsg("The Requested Job Does not exist.Please Contact Developer."))
                                                                        ).switchIfEmpty(responseInfoMsg("The Requested Transaction Status Does not Exist"))
                                                                        .onErrorResume(ex -> responseErrorMsg("Create Transaction Status First.Please Contact Developer."));
                                                            }

                                                            //check If Transaction Status uuid Exists
                                                            else if (transactionEntity.getTransactionStatusUUID() != null) {
                                                                // check if transaction status uuid exists
                                                                return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getTransactionStatusUUID())
                                                                        .flatMap(transactionStatusEntity -> {

                                                                            // if given transaction status is inactive
                                                                            if (!transactionStatusEntity.getStatus()) {
                                                                                return responseInfoMsg("Transaction Status's status is inactive");
                                                                            }

                                                                            return transactionRepository.save(transactionEntity)
                                                                                    .flatMap(transactionDB -> storeLedgerEntryAndUploadDocument(entity, transactionDB, "Record Stored Successfully", userId, finalTransactionJsonNode)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Transactions.There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Save Transactions.Please Contact Developer."))
                                                                                    );
                                                                        }).switchIfEmpty(responseInfoMsg("The Requested Transaction Status Does not Exist"))
                                                                        .onErrorResume(ex -> responseErrorMsg("Create Transaction Status First.Please Contact Developer."));
                                                            }

                                                            //check If Job uuid Exists
                                                            else if (transactionEntity.getJobUUID() != null) {
                                                                return jobRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getJobUUID())
                                                                        .flatMap(jobEntity -> {

                                                                            // if given job is inactive
                                                                            if (!jobEntity.getStatus()) {
                                                                                return responseInfoMsg("Job status is inactive");
                                                                            }

                                                                            transactionEntity.setJobUUID(jobEntity.getUuid());
                                                                            return transactionRepository.save(transactionEntity)
                                                                                    .flatMap(transactionDB -> storeLedgerEntryAndUploadDocument(entity, transactionDB, "Record Stored Successfully", userId, finalTransactionJsonNode)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Transactions.There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."))
                                                                                    );
                                                                        }).switchIfEmpty(responseInfoMsg("The Requested Job Does not exist"))
                                                                        .onErrorResume(ex -> responseErrorMsg("The Requested Job Does not exist.Please Contact Developer."));
                                                            }

                                                            // else store the transaction
                                                            else {
                                                                return transactionRepository.save(transactionEntity)
                                                                        .flatMap(transactionDB -> storeLedgerEntryAndUploadDocument(entity, transactionDB, "Record Stored Successfully", userId, finalTransactionJsonNode)
                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Transactions.There is something wrong please try again."))
                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Save Transactions.Please Contact Developer."))
                                                                        );
                                                            }

                                                        }).switchIfEmpty(responseInfoMsg("The Requested Voucher Type Does not Exist"))
                                                        .onErrorResume(ex -> responseErrorMsg("Create Voucher Type First.Please Contact Developer."))
                                                ).switchIfEmpty(responseInfoMsg("The Requested Voucher No Does not Exist"))
                                                .onErrorResume(ex -> responseErrorMsg("Create Voucher No First.Please Contact Developer."))
                                        ).switchIfEmpty(responseInfoMsg("The Requested Calendar Period Does not Exist"))
                                        .onErrorResume(ex -> responseErrorMsg("Create Calendar Period First.Please Contact Developer."));
                            });
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request."))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    public Mono<List<DocumentAttachmentDto>> updateDocument(TransactionEntity transactionEntity, List<DocumentAttachmentDto> transactionDto, String userId) {

        //Getting Attachment Object from Transaction Dto and Adding in List of Type Document Attachment Dto
        List<DocumentAttachmentDto> documentAttachmentDto = new ArrayList<>(transactionDto);

        //Creating Empty List for Doc Ids
        List<UUID> listOfDocument = new ArrayList<>();

        for (DocumentAttachmentDto documentRow : documentAttachmentDto) {
            // add doc Ids from front into doc Ids list
            listOfDocument.add(documentRow.getDoc_id());

        }

        //Empty List for Doc Ids from Json Request user Enters
        List<String> listOfDocumentsWithTransactions = new ArrayList<>();

        listOfDocument.forEach(uuid -> {
            if (uuid != null) {
                listOfDocumentsWithTransactions.add(uuid.toString());
            }
        });

        //Sending Doc Ids in Form data to check if doc Ids exist
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
        for (String listOfValues : listOfDocumentsWithTransactions) {
            formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
        }

        //posting Doc Ids in Drive Module Document Handler to get only those doc Ids that exists
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
                .flatMap(jsonNode2 -> {
                    //Reading the Response "Data" Object from Json Node
                    final JsonNode arrNode2 = jsonNode2.get("data");

                    Map<String, String> documentMap = new HashMap<String, String>();

                    List<DocumentAttachmentDto> responseAttachments = new LinkedList<>();

                    if (arrNode2.isArray()) {
                        for (final JsonNode objNode : arrNode2) {
                            for (UUID documentIdData : listOfDocument) {
                                JsonNode key = objNode.get(String.valueOf(documentIdData));
                                if (key != null) {

                                    DocumentAttachmentDto documentAttachments = DocumentAttachmentDto.builder()
                                            .doc_id(UUID.fromString(key.get("docId").toString().replaceAll("\"", "")))
                                            .doc_name(key.get("filename").toString().replaceAll("\"", ""))
                                            .doc_bucket_uuid(UUID.fromString(key.get("docBucketUUID").toString().replaceAll("\"", "")))
                                            .build();

                                    documentMap.put(documentAttachments.getDoc_id().toString(), documentAttachments.getDoc_bucket_uuid().toString());

                                    responseAttachments.add(documentAttachments);
                                }
                            }
                        }
                    }

                    // This list is used to delete that are mapped previously, but does not exist in current list
                    List<UUID> docIdList = new ArrayList<>();

                    listOfDocument.removeAll(Arrays.asList("", null));

                    return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDocumentUUIDInAndDeletedAtIsNull(transactionEntity.getUuid(), listOfDocument)
                            .collectList()
                            .flatMap(removeList -> {

                                for (TransactionDocumentPvtEntity pvtEntity : removeList) {

                                    // remove already mapped doc Ids from current list
                                    listOfDocument.remove(pvtEntity.getDocumentUUID());

                                    documentMap.remove(pvtEntity.getDocumentUUID().toString());

                                    // add already mapped records, that also exist in current list
                                    docIdList.add(pvtEntity.getDocumentUUID());
                                }


                                //List of Document ids to Store in Transaction Document Pvt Table
                                List<TransactionDocumentPvtEntity> listPvt = new ArrayList<TransactionDocumentPvtEntity>();

                                //iterating Over the Map Key Document Ids and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {
                                    TransactionDocumentPvtEntity transactionDocumentPvtEntity = TransactionDocumentPvtEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .transactionUUID(transactionEntity.getUuid())
                                            .createdBy(transactionEntity.getCreatedBy())
                                            .createdAt(transactionEntity.getCreatedAt())
                                            .updatedBy(UUID.fromString(userId))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(transactionEntity.getReqCreatedIP())
                                            .reqCreatedPort(transactionEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(transactionEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(transactionEntity.getReqCreatedOS())
                                            .reqCreatedDevice(transactionEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(transactionEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(transactionEntity.getReqCompanyUUID())
                                            .reqBranchUUID(transactionEntity.getReqBranchUUID())
                                            .reqCreatedIP(transactionEntity.getReqCreatedIP())
                                            .reqCreatedPort(transactionEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(transactionEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(transactionEntity.getReqCreatedOS())
                                            .reqCreatedDevice(transactionEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(transactionEntity.getReqCreatedReferer())
                                            .reqUpdatedIP(transactionEntity.getReqUpdatedIP())
                                            .reqUpdatedPort(transactionEntity.getReqUpdatedPort())
                                            .reqUpdatedBrowser(transactionEntity.getReqUpdatedBrowser())
                                            .reqUpdatedOS(transactionEntity.getReqUpdatedOS())
                                            .reqUpdatedDevice(transactionEntity.getReqUpdatedDevice())
                                            .reqUpdatedReferer(transactionEntity.getReqUpdatedReferer())
                                            .build();

                                    listPvt.add(transactionDocumentPvtEntity);
                                }

                                docIdList.removeAll(Arrays.asList("", null));

                                return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDocumentUUIDNotInAndDeletedAtIsNull(transactionEntity.getUuid(), docIdList)
                                        .collectList()
                                        .flatMap(transactionDocPvt -> {
                                            for (TransactionDocumentPvtEntity transDocumentPvt : transactionDocPvt) {
                                                transDocumentPvt.setDeletedBy(UUID.fromString(userId));
                                                transDocumentPvt.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            }
                                            //Saving all Pvt Entries in Transaction Document Pvt Table
                                            return transactionDocumentPvtRepository.saveAll(transactionDocPvt)
                                                    .then(transactionDocumentPvtRepository.saveAll(listPvt)
                                                            .collectList()
                                                            .flatMap(tranDocPvt -> {
                                                                //Creating Final Document List to Update the Status
                                                                List<UUID> finalDocumentList = new ArrayList<>();
                                                                for (TransactionDocumentPvtEntity pvtData : tranDocPvt) {
                                                                    finalDocumentList.add(pvtData.getDocumentUUID());
                                                                }
                                                                //Empty List for Doc Ids from Json Request user Enters
                                                                List<String> listOfDoc = new ArrayList<>();

                                                                finalDocumentList.forEach(uuid -> {
                                                                    if (uuid != null) {
                                                                        listOfDoc.add(uuid.toString());
                                                                    }
                                                                });


                                                                //Sending Doc Ids in Form data to check if doc Ids exist
                                                                MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                                                for (String listOfDocumentUUID : listOfDoc) {
                                                                    sendFormData.add("docId", listOfDocumentUUID);   //iterating over multiple values and then adding in list
                                                                }
                                                                return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
                                                                        .flatMap(document -> Mono.just(responseAttachments));
                                                            })
                                                            .flatMap(test -> Mono.just(responseAttachments))
                                                    );
                                        });
                            });
                });

    }

    public Mono<ServerResponse> updateLedgerEntryAndUploadDocument(TransactionDataDto transactionDto, TransactionEntity transactionEntity, TransactionEntity updatedTransactionEntity, String
            msg, String userId, JsonNode jsonValue) {

        //Creating Empty List of Ledger Entry Entity
        List<LedgerEntryEntity> listOfLedgerEntry = new ArrayList<>();

        //Getting row Object From Transaction Dto
        List<LedgerRowDto> ledgerRow = new ArrayList<>(transactionDto.getRows());
        if (ledgerRow.size() < 1) {
            return transactionRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getUuid())
                    .flatMap(deleteTransaction -> transactionRepository.delete(deleteTransaction)
                            .flatMap(transactionDeleteEntity -> responseInfoMsg("Unable to Update Transaction.There is Something wrong please try again."))
                            .switchIfEmpty(responseInfoMsg("Add Ledger Entry First."))
                    ).switchIfEmpty(responseInfoMsg("Transaction Does not Exist."))
                    .onErrorResume(ex -> responseErrorMsg("Transaction Does not exist.Please Contact Developer."));
        } else {
            //Creating Empty List for Cost Center,Profit Center and List of Accounts
            List<UUID> listOfCostCenter = new ArrayList<>();
            List<UUID> listOfProfitCenter = new ArrayList<>();
            List<UUID> listOfAccount = new ArrayList<>();

            // BigDecimal value for 0.0
            BigDecimal amount = new BigDecimal("0.0");

            BigDecimal sumOfDebitAmount = amount;
            BigDecimal sumOfCreditAmount = amount;
            //Looping Over the Ledger Row and Setting Values in Ledger Entry

            List<UUID> bankAccount = new ArrayList<>();

            for (LedgerRowDto ledger : ledgerRow) {

                if (ledger.getCost_center().getUuid() != null) {
                    listOfCostCenter.add(ledger.getCost_center().getUuid());
                }

                if (ledger.getProfit_center().getUuid() != null) {
                    listOfProfitCenter.add(ledger.getProfit_center().getUuid());
                }

                if (ledger.getAccount().getUuid() != null) {
                    listOfAccount.add(ledger.getAccount().getUuid());
                }

                LedgerEntryEntity ledgerData = LedgerEntryEntity.builder()
                        .uuid(UUID.randomUUID())
                        .drAmount(ledger.getDr())
                        .crAmount(ledger.getCr())
                        .description(ledger.getDescription().trim())
                        .profitCenterUUID(ledger.getProfit_center().getUuid())
                        .costCenterUUID(ledger.getCost_center().getUuid())
                        .accountUUID(ledger.getAccount().getUuid())
                        .transactionUUID(transactionEntity.getUuid())
                        .createdBy(transactionEntity.getCreatedBy())
                        .createdAt(transactionEntity.getCreatedAt())
                        .updatedBy(UUID.fromString(userId))
                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                        .reqCompanyUUID(transactionEntity.getReqCompanyUUID())
                        .reqBranchUUID(transactionEntity.getReqBranchUUID())
                        .reqCreatedIP(transactionEntity.getReqCreatedIP())
                        .reqCreatedPort(transactionEntity.getReqCreatedPort())
                        .reqCreatedBrowser(transactionEntity.getReqCreatedBrowser())
                        .reqCreatedOS(transactionEntity.getReqCreatedOS())
                        .reqCreatedDevice(transactionEntity.getReqCreatedDevice())
                        .reqCreatedReferer(transactionEntity.getReqCreatedReferer())
                        .reqUpdatedIP(transactionEntity.getReqUpdatedIP())
                        .reqUpdatedPort(transactionEntity.getReqUpdatedPort())
                        .reqUpdatedBrowser(transactionEntity.getReqUpdatedBrowser())
                        .reqUpdatedOS(transactionEntity.getReqUpdatedOS())
                        .reqUpdatedDevice(transactionEntity.getReqUpdatedDevice())
                        .reqUpdatedReferer(transactionEntity.getReqUpdatedReferer())
                        .build();

                listOfLedgerEntry.add(ledgerData);

                if (ledgerData.getDrAmount() == null) {
                    ledgerData.setDrAmount(amount);
                }

                if (ledgerData.getCrAmount() == null) {
                    ledgerData.setCrAmount(amount);
                }


                if (ledgerData.getDrAmount().compareTo(amount) != 0) {
                    //getting Sum of All the Debit Amount
                    sumOfDebitAmount = sumOfDebitAmount.add(ledgerData.getDrAmount());
                }


                if (ledgerData.getCrAmount().compareTo(amount) != 0) {
                    //getting Sum of All the Credit Amount
                    sumOfCreditAmount = sumOfCreditAmount.add(ledgerData.getCrAmount());


                    // add accounts that are on credit side
                    if (!bankAccount.contains(ledger.getAccount().getUuid())) {
                        bankAccount.add(ledger.getAccount().getUuid());
                    }
                }

            }
            //Getting Distinct Values Fom the List of Cost center
            listOfCostCenter = listOfCostCenter.stream()
                    .distinct()
                    .collect(Collectors.toList());

            //Getting Distinct Values Fom the List of Profit center
            listOfProfitCenter = listOfProfitCenter.stream()
                    .distinct()
                    .collect(Collectors.toList());

            //Getting Distinct Values Fom the List of Accounts
            listOfAccount = listOfAccount.stream()
                    .distinct()
                    .collect(Collectors.toList());

            List<UUID> finalListOfProfitCenter = listOfProfitCenter;
            List<UUID> finalListOfAccount = listOfAccount;
            List<UUID> finalListOfCostCenter = listOfCostCenter;

            if (!sumOfCreditAmount.equals(sumOfDebitAmount)) {
                return responseInfoMsg("Total Balance of Credit and Debit is not equal.");
            }

            UUID bankAccountUUID = bankAccount.get(0);

            return accountRepository.findByUuidAndDeletedAtIsNull(bankAccountUUID)
                    .flatMap(bankAccountEntity -> ledgerEntryRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
                            .collectList()
                            .flatMap(previousLedgerEntry -> accountRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfAccount)
                                    .collectList()
                                    .flatMap(accountEntityList -> costCenterRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfCostCenter)
                                            .collectList()
                                            .flatMap(costCenterEntityList -> profitCenterRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfProfitCenter)
                                                    .collectList()
                                                    .flatMap(profitCenterList -> {

                                                        // check if status is active in accounts
                                                        for (AccountEntity accountEntity : accountEntityList) {
                                                            // if given account is inactive
                                                            if (!accountEntity.getStatus()) {
                                                                return responseInfoMsg("Account status is inactive in " + accountEntity.getName());
                                                            }

                                                            // if entry is not allowed for given account
                                                            if (!accountEntity.getIsEntryAllowed()) {
                                                                return responseInfoMsg("Entry is not allowed for " + accountEntity.getName());
                                                            }
                                                        }

                                                        // check if status is active in cost centers
                                                        for (CostCenterEntity costCenterEntity : costCenterEntityList) {
                                                            // if given cost center is inactive
                                                            if (!costCenterEntity.getStatus()) {
                                                                return responseInfoMsg("Cost Center status is inactive in " + costCenterEntity.getName());
                                                            }
                                                        }

                                                        // check if status is active in profit centers
                                                        for (ProfitCenterEntity profitCenterEntity : profitCenterList) {
                                                            // if given profit center is inactive
                                                            if (!profitCenterEntity.getStatus()) {
                                                                return responseInfoMsg("Profit Center status is inactive in " + profitCenterEntity.getName());
                                                            }
                                                        }

                                                        //If the Provided List of Account Size is Greater than the Account Entity List Size
                                                        if (finalListOfAccount.size() != accountEntityList.size()) {
                                                            return responseInfoMsg("Account Does not exist.");

                                                        }

                                                        if (!finalListOfCostCenter.isEmpty()) {
                                                            if (finalListOfCostCenter.size() != costCenterEntityList.size()) {
                                                                return responseInfoMsg("Cost Center Does not exist");
                                                            }
                                                        }

                                                        if (finalListOfProfitCenter.size() != profitCenterList.size()) {
                                                            return responseInfoMsg("Profit Center Does not exist.");
                                                        }

                                                        if (bankAccount.size() > 1) {
                                                            return responseInfoMsg("Transaction must have single Bank account in Credit");
                                                        }

                                                        //Adding the Two Values in Json Value Object Node
                                                        ObjectNode transactionObjectNode = (ObjectNode) jsonValue;
                                                        transactionObjectNode.put("id", transactionEntity.getId());
                                                        transactionObjectNode.put("transaction_id", transactionEntity.getUuid().toString());
                                                        transactionObjectNode.put("company_uuid", transactionEntity.getCompanyUUID().toString());
                                                        transactionObjectNode.put("branch_uuid", transactionEntity.getBranchUUID().toString());

                                                        //Removing previous Ledger Entry
                                                        for (LedgerEntryEntity previousLedgerRow : previousLedgerEntry) {
                                                            previousLedgerRow.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                            previousLedgerRow.setDeletedBy(UUID.fromString(userId));
                                                        }

                                                        //Save Previous Ledger Entry
                                                        return ledgerEntryRepository.saveAll(previousLedgerEntry)
                                                                //Then Save Updated Ledger Entry
                                                                .then(ledgerEntryRepository.saveAll(listOfLedgerEntry)
                                                                        .collectList()
                                                                        .flatMap(ledgerStore -> transactionRecordRequestBody(jsonValue)
                                                                                .flatMap(docStore -> updateDocument(transactionEntity, docStore, userId)
                                                                                        .flatMap(attachedDocs -> transactionRepository.save(transactionEntity)
                                                                                                .then(transactionRepository.save(updatedTransactionEntity))
                                                                                                .flatMap(transactionUpdated -> {
                                                                                                            //Creating Object Mapper
                                                                                                            ObjectMapper mapper = new ObjectMapper();
                                                                                                            //Adding list to Mapper
                                                                                                            ArrayNode array = mapper.valueToTree(attachedDocs);
                                                                                                            //Put List to Object Node
                                                                                                            transactionObjectNode.putArray("attachments").addAll(array);

                                                                                                            return responseSuccessMsg(msg, transactionObjectNode)
                                                                                                                    .switchIfEmpty(responseSuccessMsg(msg, transactionObjectNode));
                                                                                                        }
                                                                                                ))
                                                                                )
                                                                        )
                                                                );
                                                    }).switchIfEmpty(responseInfoMsg("Profit Center Record does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("Profit Center Record does not exist.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("Cost Center Record does not exist."))
                                            .onErrorResume(ex -> responseErrorMsg("Cost Center Record does not exist.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Account Record does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Account Record does not exist.Please Contact Developer."))
                            )
                    ).switchIfEmpty(responseInfoMsg("Account Record does not exist."))
                    .onErrorResume(ex -> responseErrorMsg("Account Record does not exist.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return serverRequest.bodyToMono(JsonNode.class)
                .flatMap(jsonValue -> {

                    //reading Json Value as Object Node
                    ObjectNode jsonNode = readJsonNode(jsonValue);

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    JsonNode transactionJsonNode = null;
                    try {
                        transactionJsonNode = objectMapper.readTree(jsonNode.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    JsonNode finalTransactionJsonNode = transactionJsonNode;
                    return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
                            .flatMap(previousTransactionEntity -> transactionRequestBody(finalTransactionJsonNode)
                                    .flatMap(entity -> {

                                        UUID transactionStatusUUID = null;
                                        if (!jsonNode.get("transaction_status").get("uuid").isNull()) {
                                            transactionStatusUUID = UUID.fromString(jsonNode.get("transaction_status").get("uuid").toString().replaceAll("\"", ""));
                                        }

                                        //Building Transaction Entity
                                        TransactionEntity transactionEntity = TransactionEntity.builder()
                                                .transactionDate(entity.getDate())
                                                .uuid(previousTransactionEntity.getUuid())
                                                .description(entity.getTransaction_description().trim())
                                                .module("account")
                                                .calendarPeriodUUID(entity.getCalendar_period_uuid())
                                                .branchUUID(UUID.fromString(reqBranchUUID))
                                                .companyUUID(UUID.fromString(reqCompanyUUID))
                                                .transactionStatusUUID(transactionStatusUUID)
                                                .jobUUID(entity.getJob_center().getUuid())
                                                .voucherUUID(UUID.fromString(jsonNode.get("voucher").get("uuid").toString().replaceAll("\"", "")))
                                                .createdBy(previousTransactionEntity.getCreatedBy())
                                                .createdAt(previousTransactionEntity.getCreatedAt())
                                                .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                                .updatedBy(UUID.fromString(userId))
                                                .reqCreatedIP(previousTransactionEntity.getReqCreatedIP())
                                                .reqCreatedPort(previousTransactionEntity.getReqCreatedPort())
                                                .reqCreatedBrowser(previousTransactionEntity.getReqCreatedBrowser())
                                                .reqCreatedOS(previousTransactionEntity.getReqCreatedOS())
                                                .reqCreatedDevice(previousTransactionEntity.getReqCreatedDevice())
                                                .reqCreatedReferer(previousTransactionEntity.getReqCreatedReferer())
                                                .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                                .reqUpdatedIP(reqIp)
                                                .reqUpdatedPort(reqPort)
                                                .reqUpdatedBrowser(reqBrowser)
                                                .reqUpdatedOS(reqOs)
                                                .reqUpdatedDevice(reqDevice)
                                                .reqUpdatedReferer(reqReferer)
                                                .build();

                                        previousTransactionEntity.setDeletedBy(UUID.fromString(userId));
                                        previousTransactionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                        previousTransactionEntity.setReqDeletedIP(reqIp);
                                        previousTransactionEntity.setReqDeletedPort(reqPort);
                                        previousTransactionEntity.setReqDeletedBrowser(reqBrowser);
                                        previousTransactionEntity.setReqDeletedOS(reqOs);
                                        previousTransactionEntity.setReqDeletedDevice(reqDevice);
                                        previousTransactionEntity.setReqDeletedReferer(reqReferer);


                                        return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getCalendarPeriodUUID())
                                                //check Transaction Status
                                                .flatMap(calendarPeriodEntity -> voucherRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getVoucherUUID())
                                                        .flatMap(voucherEntity -> voucherTypeCatalogueRepository.findByUuidAndDeletedAtIsNull(voucherEntity.getVoucherTypeCatalogueUUID())
                                                                .flatMap(voucherTypeCatalogueEntity -> {

                                                                    // if given calendar period is inactive
                                                                    if (!calendarPeriodEntity.getIsOpen()) {
                                                                        return responseInfoMsg("Calendar Period status is inactive");
                                                                    }
                                                                    // if given Voucher Type is inactive
                                                                    if (!voucherTypeCatalogueEntity.getStatus()) {
                                                                        return responseInfoMsg("Voucher Type status is inactive");
                                                                    }

                                                                    // if given voucher type is not bank payment voucher
                                                                    if (!voucherTypeCatalogueEntity.getSlug().equals("bank-payment-voucher")) {
                                                                        return responseInfoMsg("Voucher Type must be Bank Payment for this Transaction Voucher");
                                                                    }

                                                                    // if given voucher is inactive
                                                                    if (!voucherEntity.getStatus()) {
                                                                        return responseInfoMsg("Voucher status is inactive");
                                                                    }


                                                                    if (transactionEntity.getTransactionDate().isBefore(calendarPeriodEntity.getStartDate())) {
                                                                        return responseInfoMsg("Transaction Entries are not opened for given Calendar Period");
                                                                    }

                                                                    if (transactionEntity.getTransactionDate().isAfter(calendarPeriodEntity.getEndDate())) {
                                                                        return responseInfoMsg("Transaction Entries are closed for given Calendar Period");
                                                                    }


                                                                    //check If Transaction Status And Job id Exists
                                                                    if (transactionEntity.getJobUUID() != null && transactionEntity.getTransactionStatusUUID() != null) {
                                                                        return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getTransactionStatusUUID())
                                                                                //check if job uuid exists
                                                                                .flatMap(transactionStatusEntity -> jobRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getJobUUID())
                                                                                        .flatMap(jobEntity -> {

                                                                                            // if given transaction status is inactive
                                                                                            if (!transactionStatusEntity.getStatus()) {
                                                                                                return responseInfoMsg("Transaction Status's status is inactive");
                                                                                            }

                                                                                            // if given job is inactive
                                                                                            if (!jobEntity.getStatus()) {
                                                                                                return responseInfoMsg("Job status is inactive");
                                                                                            }

                                                                                            transactionEntity.setJobUUID(jobEntity.getUuid());
                                                                                            return updateLedgerEntryAndUploadDocument(entity, previousTransactionEntity, transactionEntity, "Record Updated Successfully", userId, finalTransactionJsonNode)
                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Transactions.There is something wrong please try again."))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."));
                                                                                        }).switchIfEmpty(responseInfoMsg("The Requested Job Does not exist"))
                                                                                        .onErrorResume(ex -> responseErrorMsg("The Requested Job Does not exist.Please Contact Developer."))
                                                                                ).switchIfEmpty(responseInfoMsg("The Requested Transaction Status Does not Exist"))
                                                                                .onErrorResume(ex -> responseErrorMsg("Create Transaction Status First.Please Contact Developer."));
                                                                    }

                                                                    //check If Transaction Status uuid Exists
                                                                    else if (transactionEntity.getTransactionStatusUUID() != null) {
                                                                        return transactionStatusRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getTransactionStatusUUID())
                                                                                .flatMap(transactionStatusEntity -> {

                                                                                    // if given transaction status is inactive
                                                                                    if (!transactionStatusEntity.getStatus()) {
                                                                                        return responseInfoMsg("Transaction Status's status is inactive");
                                                                                    }

                                                                                    return updateLedgerEntryAndUploadDocument(entity, previousTransactionEntity, transactionEntity, "Record Updated Successfully", userId, finalTransactionJsonNode)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Update Transactions.There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."));
                                                                                }).switchIfEmpty(responseInfoMsg("The Requested Transaction Status Does not Exist"))
                                                                                .onErrorResume(ex -> responseErrorMsg("Create Transaction Status First.Please Contact Developer."));
                                                                    }

                                                                    //check If Job id Exists
                                                                    else if (transactionEntity.getJobUUID() != null) {
                                                                        return jobRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getJobUUID())
                                                                                .flatMap(jobEntity -> {

                                                                                    // if given job is inactive
                                                                                    if (!jobEntity.getStatus()) {
                                                                                        return responseInfoMsg("Job status is inactive");
                                                                                    }

                                                                                    transactionEntity.setJobUUID(jobEntity.getUuid());
                                                                                    return updateLedgerEntryAndUploadDocument(entity, previousTransactionEntity, transactionEntity, "Record Updated Successfully", userId, finalTransactionJsonNode)
                                                                                            .switchIfEmpty(responseInfoMsg("Unable to Update Transactions.There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."));
                                                                                }).switchIfEmpty(responseInfoMsg("The Requested Job Does not exist"))
                                                                                .onErrorResume(ex -> responseErrorMsg("The Requested Job Does not exist.Please Contact Developer."));
                                                                    }

                                                                    // else update the record
                                                                    else {
                                                                        return updateLedgerEntryAndUploadDocument(entity, previousTransactionEntity, transactionEntity, "Record Updated Successfully", userId, finalTransactionJsonNode)
                                                                                .switchIfEmpty(responseInfoMsg("Unable to Update Transactions.There is something wrong please try again."))
                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."));
                                                                    }

                                                                }).switchIfEmpty(responseInfoMsg("The Requested Voucher Type Does not Exist"))
                                                                .onErrorResume(ex -> responseErrorMsg("Create Voucher Type First.Please Contact Developer."))
                                                        ).switchIfEmpty(responseInfoMsg("The Requested Voucher No Does not Exist"))
                                                        .onErrorResume(ex -> responseErrorMsg("Create Voucher No First.Please Contact Developer."))
                                                ).switchIfEmpty(responseInfoMsg("The Requested Calendar Period Does not Exist"))
                                                .onErrorResume(ex -> responseErrorMsg("Create Calendar Period First.Please Contact Developer."));
                                    }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("Requested Transaction Does not Exist"))
                            .onErrorResume(ex -> responseErrorMsg("Requested Transaction Does not Exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_bank-payment-vouchers_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        String userId = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
                .flatMap(transactionEntity -> slaveTransactionRepository.showTransactionWithVoucherType(transactionUUID, "bank-payment-voucher")
                        .flatMap(transaction -> slaveTransactionRepository.showAllLedgerRows(transactionUUID)
                                .collectList()
                                .flatMap(ledgerFlux -> slaveTransactionRepository.showAllDocumentAttachments(transactionUUID)
                                        .collectList()
                                        .flatMap(documentData -> {

                                            SlaveTransactionDataDto transactionDataDto = SlaveTransactionDataDto
                                                    .builder()
                                                    .rows(ledgerFlux)
                                                    .debit(transaction.getDebit())
                                                    .credit(transaction.getCredit())
                                                    .calendar_period_uuid(transaction.getCalendar_period_uuid())
                                                    .date(transaction.getDate())
                                                    .company_uuid(transaction.getCompany_uuid())
                                                    .branch_uuid(transaction.getBranch_uuid())
                                                    .transaction_description(transaction.getTransaction_description())
                                                    .job_center(transaction.getJob_center())
                                                    .build();

                                            List<SlaveDocumentAttachmentDto> attachments = new LinkedList<>();

                                            SlaveTransactionRecordDto finalDto = SlaveTransactionRecordDto
                                                    .builder()
                                                    .id(transaction.getId())
                                                    .transaction_id(transaction.getTransaction_id())
                                                    .transaction_status(transaction.getTransaction_status())
                                                    .voucher(transaction.getVoucher())
                                                    .transaction_data(transactionDataDto)
                                                    .build();

                                            for (SlaveDocumentAttachmentDto docs : documentData) {
                                                if (docs.getDoc_id() != null) {
                                                    attachments.add(docs);
                                                }
                                            }
                                            finalDto.setAttachments(attachments);
                                            return ledgerEntryRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionUUID)
                                                    .collectList()
                                                    .flatMap(previousLedgerEntry -> transactionDocumentPvtRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionUUID)
                                                            .collectList()
                                                            .flatMap(previousTransactionDocumentPvtRecords -> {

                                                                for (LedgerEntryEntity ledgerEntryEntity : previousLedgerEntry) {
                                                                    ledgerEntryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                    ledgerEntryEntity.setDeletedBy(UUID.fromString(userId));
                                                                    ledgerEntryEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                    ledgerEntryEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                    ledgerEntryEntity.setReqDeletedIP(reqIp);
                                                                    ledgerEntryEntity.setReqDeletedPort(reqPort);
                                                                    ledgerEntryEntity.setReqDeletedBrowser(reqBrowser);
                                                                    ledgerEntryEntity.setReqDeletedOS(reqOs);
                                                                    ledgerEntryEntity.setReqDeletedDevice(reqDevice);
                                                                    ledgerEntryEntity.setReqDeletedReferer(reqReferer);
                                                                }

                                                                for (TransactionDocumentPvtEntity transactionDocumentPvtEntity : previousTransactionDocumentPvtRecords) {
                                                                    transactionDocumentPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                    transactionDocumentPvtEntity.setDeletedBy(UUID.fromString(userId));
                                                                    transactionDocumentPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                    transactionDocumentPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                    transactionDocumentPvtEntity.setReqDeletedIP(reqIp);
                                                                    transactionDocumentPvtEntity.setReqDeletedPort(reqPort);
                                                                    transactionDocumentPvtEntity.setReqDeletedBrowser(reqBrowser);
                                                                    transactionDocumentPvtEntity.setReqDeletedOS(reqOs);
                                                                    transactionDocumentPvtEntity.setReqDeletedDevice(reqDevice);
                                                                    transactionDocumentPvtEntity.setReqDeletedReferer(reqReferer);
                                                                }

                                                                transactionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                transactionEntity.setDeletedBy(UUID.fromString(userId));
                                                                transactionEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                transactionEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                transactionEntity.setReqDeletedIP(reqIp);
                                                                transactionEntity.setReqDeletedPort(reqPort);
                                                                transactionEntity.setReqDeletedBrowser(reqBrowser);
                                                                transactionEntity.setReqDeletedOS(reqOs);
                                                                transactionEntity.setReqDeletedDevice(reqDevice);
                                                                transactionEntity.setReqDeletedReferer(reqReferer);

                                                                return ledgerEntryRepository.saveAll(previousLedgerEntry)
                                                                        .thenMany(transactionDocumentPvtRepository.saveAll(previousTransactionDocumentPvtRecords))
                                                                        .then(transactionRepository.save(transactionEntity))
                                                                        .flatMap(recordsDeleted -> responseSuccessMsg("Record Deleted Successfully", finalDto)
                                                                                .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to delete record.Please Contact Developer.")));
                                                            }));
                                        })
                                )
                        ).switchIfEmpty(responseInfoMsg("Record Does not Exist for Bank Payment Voucher."))
                        .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Record Does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."));
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }


    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }


    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }
}
