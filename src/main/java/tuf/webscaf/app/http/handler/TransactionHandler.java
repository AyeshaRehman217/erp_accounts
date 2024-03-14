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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.*;
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repository.*;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLedgerEntryEntity;
import tuf.webscaf.app.dbContext.slave.repository.*;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Tag(name = "transactionHandler")
public class TransactionHandler {
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
    CashFlowAdjustmentRepository cashFlowAdjustmentRepository;

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
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_transactions_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {
        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Voucher UUID
        String voucherUUID = serverRequest.queryParam("voucherUUID").map(String::toString).orElse("").trim();

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

        if (!voucherUUID.isEmpty()) {
            //This Function Fetch All the Transaction WithOut Join with ledger and Document
            Flux<SlaveTransactionDto> transactionEntityFlux = slaveTransactionRepository
                    .listAllTransactionsWithVoucherFilter(UUID.fromString(voucherUUID), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return voucherRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(voucherUUID))
                    .flatMap(voucher -> transactionEntityFlux
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
                                                .listOfTransactionLedgerRowsWithVoucherFilter(UUID.fromString(voucherUUID), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                                        //The Query Fetch Document Map from Transaction and Document Join
                                        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> documentAttachmentFlux = slaveTransactionRepository
                                                .listAllDocumentAttachmentsWithVoucherFilter(UUID.fromString(voucherUUID), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

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
//
                                                                        transaction.setAttachments(attachments);

                                                                        //Adding in Final List
                                                                        listRecord.add(transaction);
                                                                    }

                                                                    //Count All the Transaction Records
                                                                    return slaveTransactionRepository.countAllByVoucherUUIDAndDeletedAtIsNull(UUID.fromString(voucherUUID))
                                                                            .flatMap(count -> {
                                                                                if (listRecord.isEmpty()) {
                                                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                                                } else {
                                                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", listRecord, count);
                                                                                }
                                                                            });
                                                                })
                                                );
                                    }).switchIfEmpty(responseInfoMsg("Unable to read request."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."))
                    ).switchIfEmpty(responseInfoMsg("Voucher Does not exist."))
                    .onErrorResume(ex -> responseErrorMsg("Voucher does not exist.Please Contact Developer."));
        } else {
            //This Function Fetch All the Transaction WithOut Join with ledger and Document
            Flux<SlaveTransactionDto> transactionEntityFlux = slaveTransactionRepository
                    .listAllTransactions(directionProperty, d, pageable.getPageSize(), pageable.getOffset());

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
                                .listOfTransactionLedgerRows(directionProperty, d, pageable.getPageSize(), pageable.getOffset());

                        //The Query Fetch Document Map from Transaction and Document Join
                        Flux<MultiValueMap<UUID, SlaveDocumentAttachmentDto>> documentAttachmentFlux = slaveTransactionRepository
                                .listAllDocumentAttachments(directionProperty, d, pageable.getPageSize(), pageable.getOffset());

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
//
                                                        transaction.setAttachments(attachments);

                                                        //Adding in Final List
                                                        listRecord.add(transaction);
                                                    }

                                                    //Count All the Transaction Records
                                                    return slaveTransactionRepository.countAllByDeletedAtIsNull()
                                                            .flatMap(count -> {
                                                                if (listRecord.isEmpty()) {
                                                                    return responseIndexInfoMsg("Record does not exist", count);
                                                                } else {
                                                                    return responseIndexSuccessMsg("All Records Fetched Successfully", listRecord, count);
                                                                }
                                                            });
                                                })
                                );
                    }).switchIfEmpty(responseInfoMsg("Unable to read request."))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }


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

    @AuthHasPermission(value = "account_api_v1_transactions_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        //This Query fetch all the Transactions
        Mono<SlaveTransactionDto> showTransactionWithIdMono = slaveTransactionRepository
                .showAllTransactions(transactionUUID);

        //This Query fetch all the Transactions with ledger Join
        Flux<SlaveLedgerRowDto> ledgerEntryEntityFlux = slaveTransactionRepository
                .showAllLedgerRows(transactionUUID);

        //This Query fetch all the Transactions with Documents
        Flux<SlaveDocumentAttachmentDto> documentAttachment = slaveTransactionRepository
                .showAllDocumentAttachments(transactionUUID);

        return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
                .flatMap(transactionEntity -> showTransactionWithIdMono
                        .flatMap(transaction -> ledgerEntryEntityFlux.collectList()
                                .flatMap(ledgerFlux -> documentAttachment.collectList()
                                                .flatMap(documentData -> {

                                                    SlaveTransactionDataDto transactionDataDto = SlaveTransactionDataDto
                                                            .builder()
                                                            .rows(ledgerFlux)
                                                            .calendar_period_uuid(transaction.getCalendar_period_uuid())
                                                            .date(transaction.getDate())
                                                            .company_uuid(transaction.getCompany_uuid())
                                                            .branch_uuid(transaction.getBranch_uuid())
                                                            .transaction_description(transaction.getTransaction_description())
                                                            .job_center(transaction.getJob_center())
                                                            .build();
//
//
//                                                    if (transaction.getJob_center().getId() != null) {
//                                                        transactionDataDto.setJob_center(transaction.getJob_center());
//                                                    }

                                                    List<SlaveDocumentAttachmentDto> attachments = new LinkedList<>();

                                                    SlaveTransactionRecordDto finalDto = SlaveTransactionRecordDto
                                                            .builder()
                                                            .id(transaction.getId())
                                                            .transaction_id(transaction.getTransaction_id())
                                                            .transaction_status(transaction.getTransaction_status())
                                                            .voucher(transaction.getVoucher())
                                                            .transaction_data(transactionDataDto)
//                                                            .attachments(documentData)
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
                                )))
                .switchIfEmpty(responseInfoMsg("Record Does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."));
    }

    //Check Branch id In Config Module
    @AuthHasPermission(value = "account_api_v1_transactions_branch_show")
    public Mono<ServerResponse> getBranchUUID(ServerRequest serverRequest) {
        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        return serverRequest.formData()
                .flatMap(value -> slaveTransactionRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //Check Company id In Config Module
    @AuthHasPermission(value = "account_api_v1_transactions_company_show")
    public Mono<ServerResponse> getCompanyUUID(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        return serverRequest.formData()
                .flatMap(value -> slaveTransactionRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    //Check Document UUID In Drive Module in Delete Function to Check Existence
    @AuthHasPermission(value = "account_api_v1_transactions_documents_show")
    public Mono<ServerResponse> getDocumentUUID(ServerRequest serverRequest) {
        UUID documentUUID = UUID.fromString(serverRequest.pathVariable("documentUUID"));

        return serverRequest.formData()
                .flatMap(value -> slaveTransactionDocumentPvtRepository.findFirstByDocumentUUIDAndDeletedAtIsNull(documentUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
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


//    //This Function is to Read Chunk of Json request
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//
//        return serverRequest.bodyToMono(JsonNode.class)
//                .flatMap(jsonNode -> {
//
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    objectMapper.registerModule(new JavaTimeModule());
//
//                    JsonNode transactionJsonNode = null;
//                    try {
//                        transactionJsonNode = objectMapper.readTree(jsonNode.toString());
//                    } catch (JsonProcessingException e) {
//                        e.printStackTrace();
//                    }
//
//                    return responseSuccessMsg("Ok", o);
//                });
//    }

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
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map",userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
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

                                            return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update",userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
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


            if (!sumOfCreditAmount.equals(sumOfDebitAmount)) {
                return transactionRepository.delete(transactionRecord)
                        .flatMap(transactionDeleteEntity -> responseInfoMsg("Unable to Store Transaction.There is Something wrong please try again."))
                        .switchIfEmpty(responseInfoMsg("Total Balance of Credit and Debit is not equal."))
                        .onErrorResume(ex -> responseErrorMsg("Unable to store transaction record.Please Contact Developer."));
            }

            //Check if The List of Account Ids Exist or not
            return accountRepository.findAllByUuidInAndDeletedAtIsNull(finalListOfAccount)
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
                                                                    .flatMap(delMsg -> responseInfoMsg("Account Does not exist."))
                                                                    .switchIfEmpty(responseInfoMsg("Account Does not exist"))
                                                                    .onErrorResume(ex -> responseErrorMsg("The Requested Account Does not exist.Please Contact Developer."));

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


                                                        //Adding the Two Values in Json Value Object Node
                                                        ObjectNode transactionObjectNode = (ObjectNode) jsonValue;
                                                        transactionObjectNode.put("id", transactionRecord.getId());
                                                        transactionObjectNode.put("transaction_id", transactionRecord.getUuid().toString());

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

    @AuthHasPermission(value = "account_api_v1_transactions_store")
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
                                        .branchUUID(entity.getBranch_uuid())
                                        .companyUUID(entity.getCompany_uuid())
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

                                return apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", transactionEntity.getCompanyUUID())
                                        .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                                                        .flatMap(company -> apiCallService.getDataWithUUID(configUri + "api/v1/branches/show/", transactionEntity.getBranchUUID())
                                                                        .flatMap(branchJson -> apiCallService.getUUID(branchJson)
                                                                                .flatMap(branch -> apiCallService.getCompanyUUID(branchJson)
                                                                                                .flatMap(companyID -> {

                                                                                                            // if given company is inactive
                                                                                                            if (!apiCallService.getStatus(companyJson)) {
                                                                                                                return responseInfoMsg("Company status is inactive");
                                                                                                            }

                                                                                                            // if given branch is inactive
                                                                                                            if (!apiCallService.getStatus(branchJson)) {
                                                                                                                return responseInfoMsg("Branch status is inactive");
                                                                                                            }

                                                                                                            if (!companyID.equals(transactionEntity.getCompanyUUID())) {
                                                                                                                return responseInfoMsg("Branch does not exists for this Company");
                                                                                                            }
                                                                                                            return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getCalendarPeriodUUID())
                                                                                                                    //check if voucher uuid exists
                                                                                                                    .flatMap(calendarPeriodEntity -> voucherRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getVoucherUUID())
                                                                                                                                    // check if calendar period uuid exists
                                                                                                                                    .flatMap(voucherEntity -> {
//                                                                                                                                                calendarPeriodsRepository.getAllCalendarPeriodsAgainstVoucher(voucherEntity.getUuid())
//                                                                                                                                                        .flatMap(calendarPeriods -> {
//
//                                                                                                                                                            List<String> listOfCalendarPeriodUuids = Arrays.asList(calendarPeriods.split("\\s*,\\s*"));
//
//                                                                                                                                                            List<UUID> calendarPeriodUUIDs = new ArrayList<>();
//                                                                                                                                                            listOfCalendarPeriodUuids.forEach(uuidString -> {
//                                                                                                                                                                calendarPeriodUUIDs.add(UUID.fromString(uuidString));
//                                                                                                                                                            });

                                                                                                                                        // if given calendar period is inactive
                                                                                                                                        if (!calendarPeriodEntity.getIsOpen()) {
                                                                                                                                            return responseInfoMsg("Calendar Period status is inactive");
                                                                                                                                        }

                                                                                                                                        // if given voucher is inactive
                                                                                                                                        if (!voucherEntity.getStatus()) {
                                                                                                                                            return responseInfoMsg("Voucher status is inactive");
                                                                                                                                        }

//                                                                                                                                        if (!calendarPeriodUUIDs.contains(calendarPeriodEntity.getUuid())) {
//                                                                                                                                            return responseInfoMsg("Transaction Entries for given Voucher cannot be posted in this Calendar Period");
//                                                                                                                                        }

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

//                                                                                                                                        }).switchIfEmpty(responseInfoMsg("Calendar Periods does not exist for given Voucher"))
//                                                                                                                                          .onErrorResume(ex -> responseErrorMsg("Calendar Periods does not exist for given Voucher.Please Contact Developer."))
                                                                                                                                    }).switchIfEmpty(responseInfoMsg("The Requested Voucher No Does not Exist"))
                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Create Voucher No First.Please Contact Developer."))
                                                                                                                    ).switchIfEmpty(responseInfoMsg("The Requested Calendar Period Does not Exist!"))
                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Create Calendar Period First.Please Contact Developer."));
                                                                                                        }
                                                                                                )
                                                                                ).switchIfEmpty(responseInfoMsg("Branch does not exist")))
                                                                        .onErrorResume(err -> responseErrorMsg("Branch does not exist.Please Contact Developer."))
                                                        )
                                        ).switchIfEmpty(responseInfoMsg("Company does not exist"))
                                        .onErrorResume(err -> responseErrorMsg("Company does not exist.Please Contact Developer."));
                            }).switchIfEmpty(responseInfoMsg("Unable to Read Request."));
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
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map",userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
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
                                                                return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update",userId, transactionEntity.getReqCompanyUUID().toString(), transactionEntity.getReqBranchUUID().toString())
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

            return ledgerEntryRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
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

                                                //Adding the Two Values in Json Value Object Node
                                                ObjectNode transactionObjectNode = (ObjectNode) jsonValue;
                                                transactionObjectNode.put("id", transactionEntity.getId());
                                                transactionObjectNode.put("transaction_id", transactionEntity.getUuid().toString());

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
                    );
        }
    }

    @AuthHasPermission(value = "account_api_v1_transactions_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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
                                                .branchUUID(entity.getBranch_uuid())
                                                .companyUUID(entity.getCompany_uuid())
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

                                        return apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", transactionEntity.getCompanyUUID())
                                                .flatMap(companyJson -> apiCallService.getUUID(companyJson)
                                                        .flatMap(company -> apiCallService.getDataWithUUID(configUri + "api/v1/branches/show/", transactionEntity.getBranchUUID())
                                                                .flatMap(branchJson -> apiCallService.getUUID(branchJson)
                                                                        .flatMap(branch -> apiCallService.getCompanyUUID(branchJson)
                                                                                .flatMap(companyID -> {

                                                                                    // if given company is inactive
                                                                                    if (!apiCallService.getStatus(companyJson)) {
                                                                                        return responseInfoMsg("Company status is inactive");
                                                                                    }

                                                                                    // if given branch is inactive
                                                                                    if (!apiCallService.getStatus(branchJson)) {
                                                                                        return responseInfoMsg("Branch status is inactive");
                                                                                    }

                                                                                    if (!companyID.equals(transactionEntity.getCompanyUUID())) {
                                                                                        return responseInfoMsg("Branch does not exists for this Company");
                                                                                    }
                                                                                    return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getCalendarPeriodUUID())
                                                                                            //check Transaction Status
                                                                                            .flatMap(calendarPeriodEntity -> voucherRepository.findByUuidAndDeletedAtIsNull(transactionEntity.getVoucherUUID())
                                                                                                    .flatMap(voucherEntity -> {
//                                                                                                            calendarPeriodsRepository.getAllCalendarPeriodsAgainstVoucher(voucherEntity.getUuid())
//                                                                                                            .flatMap(calendarPeriods -> {
//
//                                                                                                                List<String> listOfCalendarPeriodUuids = Arrays.asList(calendarPeriods.split("\\s*,\\s*"));
//
//                                                                                                                List<UUID> calendarPeriodUUIDs = new ArrayList<>();
//                                                                                                                listOfCalendarPeriodUuids.forEach(uuidString -> {
//                                                                                                                    calendarPeriodUUIDs.add(UUID.fromString(uuidString));
//                                                                                                                });

                                                                                                        // if given calendar period is inactive
                                                                                                        if (!calendarPeriodEntity.getIsOpen()) {
                                                                                                            return responseInfoMsg("Calendar Period status is inactive");
                                                                                                        }

                                                                                                        // if given voucher is inactive
                                                                                                        if (!voucherEntity.getStatus()) {
                                                                                                            return responseInfoMsg("Voucher status is inactive");
                                                                                                        }

//                                                                                                                if (!calendarPeriodUUIDs.contains(calendarPeriodEntity.getUuid())) {
//                                                                                                                    return responseInfoMsg("Transaction Entries for given Voucher cannot be posted in this Calendar Period");
//                                                                                                                }

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

                                                                                                        // if job uuid is not given
                                                                                                        else {
                                                                                                            return updateLedgerEntryAndUploadDocument(entity, previousTransactionEntity, transactionEntity, "Record Updated Successfully", userId, finalTransactionJsonNode)
                                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Transactions.There is something wrong please try again."))
                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Contact Developer."));
                                                                                                        }
//                                                                                                            }).switchIfEmpty(responseInfoMsg("Calendar Periods does not exist for given Voucher"))
//                                                                                                            .onErrorResume(ex -> responseErrorMsg("Calendar Periods does not exist for given Voucher.Please Contact Developer."))
                                                                                                    }).switchIfEmpty(responseInfoMsg("The Requested Voucher No Does not Exist"))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Create Voucher No First.Please Contact Developer."))
                                                                                            ).switchIfEmpty(responseInfoMsg("The Requested Calendar Period Does not Exist!"))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Create Calendar Period First.Please Contact Developer."));
                                                                                })
                                                                        ).switchIfEmpty(responseInfoMsg("Branch does not exist")))
                                                                .onErrorResume(err -> responseErrorMsg("Branch does not exist.Please Contact Developer.")))
                                                ).switchIfEmpty(responseInfoMsg("Company does not exist"))
                                                .onErrorResume(err -> responseErrorMsg("Company does not exist.Please Contact Developer."));
                                    }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                            ).switchIfEmpty(responseInfoMsg("Requested Transaction Does not Exist"))
                            .onErrorResume(ex -> responseErrorMsg("Requested Transaction Does not Exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

//    public Mono<ServerResponse> storeLedgerEntryAndUploadDocument(TransactionDataDto transactionDto, UUID transactionUUID, String
//            msg, Long userId, JsonNode jsonValue) {
//
//        //Creating Empty List of Ledger Entry Entity
//        List<LedgerEntryEntity> listOfLedgerEntry = new ArrayList<>();
//
//        //Getting row Object From Transaction Dto
//        List<LedgerRowDto> ledgerRow = new ArrayList<>(transactionDto.getRows());
//
//        //Creating Empty List for Cost Center,Profit Center and List of Accounts
//        List<Long> listOfCostCenter = new ArrayList<>();
//        List<Long> listOfProfitCenter = new ArrayList<>();
//        List<Long> listOfAccount = new ArrayList<>();
//
//        //Looping Over the Ledger Row and Setting Values in Ledger Entry
//        for (LedgerRowDto ledger : ledgerRow) {
//
//            listOfCostCenter.add(ledger.getCost_center().getId());
//            listOfProfitCenter.add(ledger.getProfit_center().getId());
//            listOfAccount.add(ledger.getAccount().getAccount_id());
//
//            LedgerEntryEntity ledgerData = LedgerEntryEntity.builder()
//                    .uuid(UUID.randomUUID())
//                    .drAmount(ledger.getCr())
//                    .crAmount(ledger.getDr())
//                    .description(ledger.getDescription())
//                    .costCenterId(ledger.getCost_center().getId())
//                    .accountId(ledger.getAccount().getAccount_id())
//                    .transactionUUID(transactionUUID)
//                    .createdBy(userId)
//                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                    .build();
//
//            listOfLedgerEntry.add(ledgerData);
//
//        }
//        //Getting Distinct Values Fom the List of Cost center
//        listOfCostCenter = listOfCostCenter.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//        //Getting Distinct Values Fom the List of Profit center
//        listOfProfitCenter = listOfProfitCenter.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//        //Getting Distinct Values Fom the List of Accounts
//        listOfAccount = listOfAccount.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<Long> finalListOfProfitCenter = listOfProfitCenter;
//        List<Long> finalListOfAccount = listOfAccount;
//        List<Long> finalListOfCostCenter = listOfCostCenter;
//
//        //Check if Transaction Id Exists
//        return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
//                //Check if The List of Account Ids Exist or not
//                .flatMap(transactionEntity1 -> accountRepository.findAllByIdInAndDeletedAtIsNull(finalListOfAccount)
//                                .collectList()
//                                .flatMap(accountEntityList -> {
//
//
//
//                                    //If the Provided List of Account Size is Greater than the Account Entity List Size
//                                    if (finalListOfAccount.size() > accountEntityList.size()) {
//                                        //Hard Delete Transaction Entity
//                                        return transactionRepository.delete(transactionEntity1)
//                                                .flatMap(delMsg -> responseInfoMsg("Account Does not exist."))
//                                                .switchIfEmpty(responseInfoMsg("Account Does not exist"))
//                                                .onErrorResume(ex -> responseErrorMsg("The Requested Account Does not exist.Please Contact Developer."));
//
//                                    }
//
//                                    System.out.println("Hello I am here at Cost Center");
//                                    System.out.println(finalListOfCostCenter);
//
//                                    if (!finalListOfCostCenter.isEmpty()) {
//                                        return costCenterRepository.findAllByIdInAndDeletedAtIsNull(finalListOfCostCenter)
//                                                .collectList()
//                                                .flatMap(costCenterListData -> {
//
//
//                                                    if (finalListOfCostCenter.size() > costCenterListData.size()) {
////
//                                                        return transactionRepository.delete(transactionEntity1)
//                                                                .flatMap(delMsg -> responseInfoMsg("Cost Center Does not exist"))
//                                                                .switchIfEmpty(responseInfoMsg("Cost Center Does not exist"))
//                                                                .onErrorResume(ex -> responseErrorMsg("Cost Center Does not exist.Please Contact Developer."));
//                                                    } else {
//                                                        return responseInfoMsg("Cost Center Does not exist.");
//                                                    }
//                                                });
//                                    }
//
//                                    if (!finalListOfProfitCenter.isEmpty()) {
//                                        return profitCenterRepository.findAllByIdInAndDeletedAtIsNull(finalListOfProfitCenter)
//                                                .collectList()
//                                                .flatMap(profitCenterList -> {
//                                                    if (finalListOfProfitCenter.size() > profitCenterList.size()) {
//                                                        return transactionRepository.delete(transactionEntity1)
//                                                                .flatMap(delMsg -> responseInfoMsg("Profit Center Does not exist."))
//                                                                .switchIfEmpty(responseInfoMsg("Profit Center Does not exist."))
//                                                                .onErrorResume(ex -> responseErrorMsg("Profit Center Does not exist.Please Contact Developer."));
//                                                    } else {
//                                                        return responseInfoMsg("Profit Center Does not exist.");
//                                                    }
//                                                });
//                                    }
//
//                                    //Adding the Two Values in Json Value Object Node
//                                    ObjectNode transactionObjectNode = (ObjectNode) jsonValue;
//                                    transactionObjectNode.put("id", transactionEntity1.getId());
//                                    transactionObjectNode.put("transaction_id", transactionEntity1.getUuid().toString());
//
//                                    return ledgerEntryRepository.saveAll(listOfLedgerEntry)
//                                            .collectList()
//                                            .flatMap(ledgerStore -> transactionRecordRequestBody(jsonValue)
//                                                    .flatMap(docStore -> storeDocument(transactionEntity1.getUuid(), docStore, userId)
//                                                            .flatMap(attachmentData -> {
//                                                                        //Creating Object Mapper
//                                                                        ObjectMapper mapper = new ObjectMapper();
//                                                                        //Adding list to Mapper
//                                                                        ArrayNode array = mapper.valueToTree(attachmentData);
//                                                                        //Put List to Object Node
//                                                                        transactionObjectNode.putArray("attachments").addAll(array);
//
//                                                                        return responseSuccessMsg(msg, transactionObjectNode)
//                                                                                .switchIfEmpty(responseSuccessMsg(msg, transactionObjectNode));
//                                                                    }
//                                                            )
//                                                    )
//                                            );
//
//                                })
//                                .switchIfEmpty(responseInfoMsg("Account Does not Exist."))
//                ).switchIfEmpty(responseInfoMsg("Transaction Does not Exist"));
//    }


//
//    public Mono<Map<Long, LedgerRowAccountDto>> ledgerAccountData(List<Long> accountId) {
//
//        Map<Long, LedgerRowAccountDto> ledgerAccountMap = new HashMap<>();
//        return accountRepository.findAllByIdInAndDeletedAtIsNull(accountId)
//                .collectList()
//                .flatMap(accountEntity -> {
//
//                    for (AccountEntity account : accountEntity) {
//                        LedgerRowAccountDto ledgerAccountRowDto = LedgerRowAccountDto
//                                .builder()
//                                .account_id(account.getId())
//                                .account_name(account.getName())
//                                .account_code(account.getCode())
//                                .build();
//
//                        ledgerAccountMap.put(account.getId(), ledgerAccountRowDto);
//                    }
//                    return Mono.just(ledgerAccountMap);
//                });
//    }
//
//    public Mono<Map<Long, LedgerRowCostCenterDto>> ledgerCostCenterData(List<Long> costCenterId) {
//
//        Map<Long, LedgerRowCostCenterDto> ledgerCostCenterMap = new HashMap<>();
//        return costCenterRepository.findAllByIdInAndDeletedAtIsNull(costCenterId)
//                .collectList()
//                .flatMap(costCenterEntity -> {
//
//                    for (CostCenterEntity costCenter : costCenterEntity) {
//                        LedgerRowCostCenterDto ledgerCostCenterRowDto = LedgerRowCostCenterDto
//                                .builder()
//                                .id(costCenter.getId())
//                                .name(costCenter.getName())
//                                .build();
//
//                        ledgerCostCenterMap.put(costCenter.getId(), ledgerCostCenterRowDto);
//                    }
//                    return Mono.just(ledgerCostCenterMap);
//                });
//    }
//
//    public Mono<Map<Long, LedgerRowProfitCenterDto>> ledgerProfitCenterData(List<Long> profitCenterId) {
//
//        Map<Long, LedgerRowProfitCenterDto> ledgerProfitCenterMap = new HashMap<>();
//        return profitCenterRepository.findAllByIdInAndDeletedAtIsNull(profitCenterId)
//                .collectList()
//                .flatMap(profitCenterEntity -> {
//
//                    for (ProfitCenterEntity profitCenter : profitCenterEntity) {
//                        LedgerRowProfitCenterDto ledgerProfitCenterRowDto = LedgerRowProfitCenterDto
//                                .builder()
//                                .id(profitCenter.getId())
//                                .name(profitCenter.getName())
//                                .build();
//
//                        ledgerProfitCenterMap.put(profitCenter.getId(), ledgerProfitCenterRowDto);
//                    }
//                    return Mono.just(ledgerProfitCenterMap);
//                });
//    }

//    public Mono<TransactionRecordDto> finalTransactionDtoRecordBuilder
//            (List<LedgerEntryEntity> ledgerEntryEntityList, TransactionEntity
//                    transactionEntity, JsonNode jsonValue) {
//
//        List<LedgerRowDto> rows = new LinkedList<>();
//
//        List<Long> accountId = new ArrayList<>();
//
//        List<Long> profitCenterId = new ArrayList<>();
//
//        List<Long> costCenterId = new ArrayList<>();
//
//
//        for (LedgerEntryEntity ledger : ledgerEntryEntityList) {
//            accountId.add(ledger.getAccountId());
//            profitCenterId.add(ledger.getProfitCenterId());
//            costCenterId.add(ledger.getCostCenterId());
//        }
//
//        return ledgerAccountData(accountId)
//                .flatMap(accountDtoList -> ledgerCostCenterData(costCenterId)
//                        .flatMap(costCenterDtoList -> ledgerProfitCenterData(profitCenterId)
//                                .flatMap(profitCenterDtoList -> {
//                                    for (LedgerEntryEntity ledger : ledgerEntryEntityList) {
//
//                                        LedgerRowDto ledgerRow = LedgerRowDto
//                                                .builder()
//                                                .account(accountDtoList.get(ledger.getAccountId()))
//                                                .description(ledger.getDescription())
//                                                .cost_center(costCenterDtoList.get(ledger.getCostCenterId()))
//                                                .profit_center(profitCenterDtoList.get(ledger.getProfitCenterId()))
//                                                .cr(ledger.getCrAmount())
//                                                .dr(ledger.getDrAmount())
//                                                .build();
//
//                                        rows.add(ledgerRow);
//                                    }
//
//                                    TransactionDataJobCenterDto jobCenter = TransactionDataJobCenterDto.builder()
//                                            .id(transactionEntity.getJobId())
//                                            .build();
//
//                                    TransactionDataDto transactionData = TransactionDataDto
//                                            .builder()
//                                            .rows(rows)
//                                            .calendar_period_id(transactionEntity.getCalendarPeriodId())
//                                            .date(transactionEntity.getTransactionDate())
//                                            .branch_id(transactionEntity.getBranchId())
//                                            .company_id(transactionEntity.getCompanyId())
//                                            .transaction_description(transactionEntity.getDescription())
//                                            .job_center(jobCenter)
//                                            .build();
//
//                                    TransactionRecordDto transactionRecordDto = TransactionRecordDto
//                                            .builder()
//                                            .id(transactionEntity.getId())
//                                            .voucher_no(transactionEntity.getVoucherNo())
//                                            .transaction_id(transactionEntity.getUuid())
//                                            .transaction_status(transactionEntity.getTransactionStatusId())
//                                            .transaction_data(transactionData)
//                                            .build();
//
//                                    return Mono.just(transactionRecordDto);
//
//                                }))
//                );
//    }


//    public Mono<TransactionEntity> deleteExistingInUpdate(TransactionEntity transactionEntity, Long userId) {
//        return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
//                .flatMap(deletingFromPvt -> {
//                    deletingFromPvt.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    deletingFromPvt.setDeletedBy(UUID.fromString(userId));
//                    return transactionDocumentPvtRepository.save(deletingFromPvt);
//                }).then(Mono.defer(() -> {
//                    return ledgerEntryRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
//                            .collectList()
//                            .flatMap(ledgerEntryEntityList -> {
//                                for (LedgerEntryEntity ledgerData : ledgerEntryEntityList) {
//                                    ledgerData.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                    ledgerData.setDeletedBy(UUID.fromString(userId));
//                                }
//                                return ledgerEntryRepository.saveAll(ledgerEntryEntityList)
//                                        .then(Mono.just(ledgerEntryEntityList));
//                            });
//                })).then(Mono.defer(() -> {
//                    // Delete Previous Transaction
//                    transactionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    transactionEntity.setDeletedBy(UUID.fromString(userId));
//                    return transactionRepository.save(transactionEntity);
//                }));
//    }

//    public Mono<ServerResponse> updateLedgerEntry(ServerRequest serverRequest, SlaveTransactionDto
//            transactionDto, TransactionEntity transactionEntity) {
//
//        //List of Ledger Entry Entity
//        List<LedgerEntryEntity> listOfLedgerEntry = new ArrayList<>(transactionDto.getLedgerEntryEntityList());
//
//        //Creating Empty List for Cost Center,Profit Center
//        List<Long> listOfCostCenter = new ArrayList<>();
//        List<Long> listOfProfitCenter = new ArrayList<>();
//        List<Long> listOfAccount = new ArrayList<>();
//
//        for (LedgerEntryEntity ledger : listOfLedgerEntry) {
//            listOfCostCenter.add(ledger.getCostCenterId());
//            listOfProfitCenter.add(ledger.getProfitCenterId());
//            listOfAccount.add(ledger.getAccountId());
//
//            ledger.setTransactionId(transactionEntity.getId());
//            ledger.setCreatedAt(transactionEntity.getCreatedAt());
//            ledger.setCreatedBy(Long.valueOf(transactionEntity.getCreatedBy()));
//        }
//        listOfCostCenter = listOfCostCenter.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//        listOfProfitCenter = listOfProfitCenter.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//        listOfAccount = listOfAccount.stream()
//                .distinct()
//                .collect(Collectors.toList());
//
//
//        List<Long> finalListOfProfitCenter = listOfProfitCenter;
//        List<Long> finalListOfAccount = listOfAccount;
//
//        List<Long> finalListOfCostCenter = listOfCostCenter;
//        return costCenterRepository.findAllByIdInAndDeletedAtIsNull(listOfCostCenter)
//                .collectList()
//                //Check Profit Center If Exists
//                .flatMap(costCenterList -> profitCenterRepository.findAllByIdInAndDeletedAtIsNull(finalListOfProfitCenter)
//                        .collectList()
//                        .flatMap(profitCenterList -> accountRepository.findAllByIdInAndDeletedAtIsNull(finalListOfAccount)
//                                .collectList()
//                                .flatMap(accountList -> {
//                                    if (finalListOfAccount.size() > accountList.size()) {
//                                        return transactionRepository.findByIdAndDeletedAtIsNull(transactionEntity.getId())
//                                                .flatMap(delete -> {
//                                                    return transactionRepository.delete(transactionEntity)
//                                                            .flatMap(delMsg -> {
//                                                                return responseInfoMsg("Account Does not exist");
//                                                            }).switchIfEmpty(Mono.defer(() -> {
//                                                                return responseInfoMsg("Account Does not exist");
//                                                            }))
//                                                            .onErrorResume(ex -> responseErrorMsg("Please Contact Developer"));
//                                                });
//                                    } else if (finalListOfProfitCenter.size() > profitCenterList.size()) {
//                                        return transactionRepository.findByIdAndDeletedAtIsNull(transactionEntity.getId())
//                                                .flatMap(delete -> {
//                                                    return transactionRepository.delete(transactionEntity)
//                                                            .flatMap(delMsg -> {
//                                                                return responseInfoMsg("Profit Center Does not exist");
//                                                            }).switchIfEmpty(Mono.defer(() -> {
//                                                                return responseInfoMsg("Profit Center Does not exist");
//                                                            }))
//                                                            .onErrorResume(ex -> responseErrorMsg("Please Contact Developer"));
//                                                });
//                                    } else if (finalListOfCostCenter.size() > costCenterList.size()) {
//                                        return transactionRepository.findByIdAndDeletedAtIsNull(transactionEntity.getId())
//                                                .flatMap(delete -> {
//                                                    return transactionRepository.delete(transactionEntity)
//                                                            .flatMap(delMsg -> {
//                                                                return responseInfoMsg("Cost Center Does not exist");
//                                                            }).switchIfEmpty(Mono.defer(() -> {
//                                                                return responseInfoMsg("Cost Center Does not exist");
//                                                            }))
//                                                            .onErrorResume(ex -> responseErrorMsg("Please Contact Developer"));
//                                                });
//                                    } else {
//                                        return Mono.empty();
//                                    }
//                                })
//                        ));
//    }


//    public Mono<ServerResponse> update1(ServerRequest serverRequest) {
//        final long transactionId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return serverRequest.bodyToMono(SlaveTransactionDto.class)
//                .flatMap(entity -> transactionRepository.findByIdAndDeletedAtIsNull(transactionId)
//                                .flatMap(transaction -> {
//
//                                    //Building Transaction Entity
//                                    TransactionEntity transactionEntity = TransactionEntity.builder()
//                                            .transactionDate(entity.getTransactionDate())
//                                            .uuid(transaction.getUuid())
//                                            .description(entity.getDescription().trim())
//                                            .calendarPeriodId(entity.getCalendarPeriodId())
//                                            .companyId(entity.getCompanyId())
//                                            .branchId(entity.getBranchId())
//                                            .transactionStatusId(entity.getTransactionStatusId())
//                                            .voucherNo(entity.getVoucherNo())
////                                    .status(entity.getStatus())
//                                            .createdBy(transaction.getCreatedBy())
//                                            .createdAt(transaction.getCreatedAt())
//                                            .updatedBy(UUID.fromString(userId))
//                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                            .build();
//
//                                    return apiCallService.getDataWithId(configUri + "api/v1/companies/show//", entity.getCompanyId())
//                                            .flatMap(companyJson -> apiCallService.checkId(companyJson)
//                                                    .flatMap(company -> apiCallService.getDataWithId(configUri + "api/v1/branches/show/", entity.getBranchId())
//                                                            .flatMap(branchJson -> apiCallService.checkId(branchJson)
//                                                                    .flatMap(branch -> {
//                                                                        if (apiCallService.getCompanyId(branchJson) != entity.getCompanyId()) {
//                                                                            return responseInfoMsg("Branch does not exists for this Company");
//                                                                        }
//                                                                        return calendarPeriodsRepository.findByIdAndDeletedAtIsNull(transactionEntity.getCalendarPeriodId())
//                                                                                //check Transaction Status
//                                                                                .flatMap(checkTransaction -> transactionStatusRepository.findByIdAndDeletedAtIsNull(transactionEntity.getTransactionStatusId())
//                                                                                        //check Voucher Id
//                                                                                        .flatMap(checkVoucher -> voucherRepository.findByIdAndDeletedAtIsNull(transactionEntity.getVoucherNo())
//                                                                                                .flatMap(value1 -> {
//
//                                                                                                    //check If Job id Exists
//                                                                                                    if (entity.getJobId() != null) {
//
//                                                                                                        return jobRepository.findByIdAndDeletedAtIsNull(entity.getJobId())
//                                                                                                                .flatMap(jobEntity -> {
//                                                                                                                    transactionEntity.setJobId(jobEntity.getId());
//                                                                                                                    return transactionRepository.save(transactionEntity)
//                                                                                                                            .flatMap(transactionEntity1 -> storeLedgerEntryAndUploadDocument(entity, transactionEntity1.getUuid(), "Record Stored Successfully", Long.valueOf(userId), finalTransactionJsonNode)
//                                                                                                                                    .switchIfEmpty(Mono.defer(() -> ledgerEntryRepository.saveAll(entity.getLedgerEntryEntityList())
//                                                                                                                                            .collectList()
//                                                                                                                                            .flatMap(ledgerEntryEntityList -> deleteExistingInUpdate(transaction, Long.valueOf(userId))
//                                                                                                                                                    .flatMap(saveEntity -> responseSuccessMsg("Record Updated Successfully!", storeDtoTransactionBuilder(ledgerEntryEntityList, transactionEntity)))
//                                                                                                                                            )
//                                                                                                                                            .switchIfEmpty(responseSuccessMsg("Record Updated Successfully!", storeDtoTransactionBuilder(entity.getLedgerEntryEntityList(), transactionEntity))))
//                                                                                                                                    )
//                                                                                                                            ).switchIfEmpty(responseErrorMsg("Unable to Save Transactions.There is something wrong please try again!"))
//                                                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Save Transactions.Please Contact Developer."));
//                                                                                                                }).switchIfEmpty(responseInfoMsg("The Requested Job Does not exist."))
//                                                                                                                .onErrorResume(ex -> responseErrorMsg("The Requested Job Does not exist.Please Contact Developer."));
//                                                                                                    } else {
//                                                                                                        //If Job Does not exist
//                                                                                                        return transactionRepository.save(transactionEntity)
//                                                                                                                .flatMap(transactionEntity1 -> storeLedgerEntryAndUploadDocument(entity, transactionEntity1.getUuid(), "Record Stored Successfully", Long.valueOf(userId), finalTransactionJsonNode)
//                                                                                                                        .switchIfEmpty(Mono.defer(() -> ledgerEntryRepository.saveAll(entity.getLedgerEntryEntityList())
//                                                                                                                                .collectList()
//                                                                                                                                .flatMap(ledgerEntryEntityList -> deleteExistingInUpdate(transaction, Long.valueOf(userId))
//                                                                                                                                        .flatMap(saveEntity -> responseSuccessMsg("Record Updated Successfully!", storeDtoTransactionBuilder(ledgerEntryEntityList, transactionEntity)))
//                                                                                                                                )
//                                                                                                                                .switchIfEmpty(responseSuccessMsg("Record Updated Successfully!", storeDtoTransactionBuilder(entity.getLedgerEntryEntityList(), transactionEntity))))
//                                                                                                                        )
//                                                                                                                ).switchIfEmpty(responseErrorMsg("Unable to Store Transactions.There is something wrong please try again."))
//                                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Transactions.Please Conatct Developer."));
//                                                                                                    }
//
//                                                                                                }).switchIfEmpty(responseInfoMsg("The Requested Voucher No Does not Exist!"))
//                                                                                                .onErrorResume(ex -> responseErrorMsg("Create Voucher No First.Please Contact Developer."))
//                                                                                        ).switchIfEmpty(responseInfoMsg("The Requested Transaction Status Does not Exist!"))
//                                                                                        .onErrorResume(ex -> responseErrorMsg("Create Transaction Status First.Please Contact Developer."))
//                                                                                ).switchIfEmpty(responseInfoMsg("The Requested Calendar Period Does not Exist!"))
//                                                                                .onErrorResume(ex -> responseErrorMsg("Create Calendar Period First.Please Contact Developer."));
//                                                                    }).switchIfEmpty(responseInfoMsg("Branch does not exist"))
//                                                                    .onErrorResume(err -> responseErrorMsg("Branch does not exist.Please Contact Developer.")))
//                                                    ).switchIfEmpty(responseInfoMsg("Company does not exist"))
//                                                    .onErrorResume(err -> responseErrorMsg("Company does not exist.Please Contact Developer.")));
//                                }).switchIfEmpty(responseInfoMsg("Requested Transaction Does not exist"))
//                                .onErrorResume(ex -> responseErrorMsg("Requested Transaction Does not exist.Please Contact Developer."))
//                );
//    }

//
//    public SlaveTransactionWithLedgerEntriesAndDocumentsDto storeDtoTransactionBuilder
//            (List<SlaveLedgerEntryDto> ledgerEntryEntityList, TransactionEntity
//                    transactionEntity) {
//
//        SlaveTransactionWithLedgerEntriesAndDocumentsDto slaveTransactionEntityDto = SlaveTransactionWithLedgerEntriesAndDocumentsDto.builder()
//                .id(transactionEntity.getId())
//                .version(transactionEntity.getVersion())
////                .status(transactionEntity.getStatus())
//                .transactionDate(transactionEntity.getTransactionDate())
//                .description(transactionEntity.getDescription())
//                .voucherNo(transactionEntity.getVoucherNo())
//                .branchId(transactionEntity.getBranchId())
//                .companyId(transactionEntity.getCompanyId())
//                .uuid(transactionEntity.getUuid())
//                .calendarPeriodId(transactionEntity.getCalendarPeriodId())
//                .jobId(transactionEntity.getJobId())
//                .transactionStatusId(transactionEntity.getTransactionStatusId())
//                //Ledger Entries List
//                .ledgerEntryList(ledgerEntryEntityList)
//                //Audit Trials of Transactions Entity
//                .createdBy(transactionEntity.getCreatedBy())
//                .createdAt(transactionEntity.getCreatedAt())
//                .build();
//
//        return slaveTransactionEntityDto;
//
//    }


//    Mono<List<Long>> storingTransactionWithDocument(ServerRequest serverRequest, List<Long> documentIdList, Long
//            getTransactionId, Long userId) {
//
//        List<String> listOfDocumentsWithTransactions = new ArrayList<>();
//
//        for (Long id : documentIdList) {
//            listOfDocumentsWithTransactions.add(String.valueOf(id));
//        }
//
//        List<TransactionDocumentPvtEntity> listPvt = new ArrayList<TransactionDocumentPvtEntity>();
//
//
//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
//
//        for (String listOfValues : listOfDocumentsWithTransactions) {
//            formData.add("documentId", listOfValues);   //iterating over multiple values and then adding in list
//        }
//
//        //getting List of Ids from Drive Module Api from documents
//        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/showList")
//                .flatMap(jsonNode2 -> {
//                    final JsonNode arrNode2 = jsonNode2.get("data");
//                    String docValues = arrNode2.toString();
//
//                    docValues = docValues.replaceAll("\\[", "").replaceAll("\\]", "");
//
//
//                    String[] docsListData = new String[docValues.length()];
//                    if (!(docValues.isEmpty())) {
//                        docsListData = docValues.split(",");
//                    }
//
//
//                    for (int i = 0; i < docsListData.length; i++) {
//                        TransactionDocumentPvtEntity transactionDocumentPvtEntity = TransactionDocumentPvtEntity
//                                .builder()
//                                .documentId(Long.valueOf(docsListData[i]))
//                                .transactionId(getTransactionId)
//                                .createdBy(userId)
//                                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                .build();
//                        //Adding Values in Pvt Table
//                        listPvt.add(transactionDocumentPvtEntity);
//                    }
//
//
//                    //  converting string of Array type to Long Type Array
//                    Long[] convertDocListToArray = new Long[docsListData.length];
//
//                    for (int i = 0; i < docsListData.length; i++) {
//                        convertDocListToArray[i] = Long.parseLong(docsListData[i]);
//                    }
//                    //Creating an empty List and then adding values
//                    List<Long> listOfDocIds = Arrays.asList(convertDocListToArray);
//
//                    return transactionDocumentPvtRepository.saveAll(listPvt)
//                            .collectList().flatMap(transactionGroup -> {
//                                return Mono.just(listOfDocIds);
//                            });
//                });
//    }


//    public Mono<SlaveReportingLedgerEntriesDto> deleteTransactionDtoBuilder(String userId, List<SlaveLedgerEntryDto> ledgerEntryEntityList, TransactionEntity transactionEntity, Long
//            transactionId, List<UUID> getDocumentid) {
//
//        SlaveReportingLedgerEntriesDto slaveDocumentsDto = SlaveReportingLedgerEntriesDto.builder()
//                .id(transactionEntity.getId())
//                //Transactions Field List
//                .uuid(transactionEntity.getUuid())
//                .transactionDate(transactionEntity.getTransactionDate())
//                .description(transactionEntity.getDescription())
//                .branchUUID(transactionEntity.getBranchUUID())
//                .companyUUID(transactionEntity.getCompanyUUID())
//                .voucherUUID(transactionEntity.getVoucherUUID())
//                .calendarPeriodUUID(transactionEntity.getCalendarPeriodUUID())
//                .jobUUID(transactionEntity.getJobUUID())
//                .transactionStatusUUID(transactionEntity.getTransactionStatusUUID())
////                .status(Boolean.valueOf(transactionEntity.getStatus()))
//                //Ledger Entries List
//                .ledgerEntryList(ledgerEntryEntityList)
//                //documents List
//                .documentId(getDocumentid)
//                //Audit Trials of Transactions Entity
//                .createdBy(UUID.fromString(userId))
//                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                .updatedBy(transactionEntity.getUpdatedBy())
//                .updatedAt(transactionEntity.getUpdatedAt())
//                .deletedBy(transactionEntity.getDeletedBy())
//                .deletedAt(transactionEntity.getDeletedAt())
//                .build();
//        return Mono.just(slaveDocumentsDto);
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        final long transactionId = Long.parseLong(serverRequest.pathVariable("id"));
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return transactionRepository.findByIdAndDeletedAtIsNull(transactionId)
////                        .flatMap(transactionEntity -> cashFlowAdjustmentRepository.findByTransactionIdAndDeletedAtIsNull(transactionId)
////                                .flatMap(checkCashFlow -> responseInfoMsg("Unable to Delete Record as transaction is used by Cash Flow Adjustment"))
//                .flatMap(transactionEntity -> transactionDocumentPvtRepository.findFirstByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
//                        .flatMap(deletingFromPvt -> responseInfoMsg("Unable to Delete Record as the Reference exists"))
//                        .switchIfEmpty(Mono.defer(() -> ledgerEntryRepository.findAllByTransactionUUIDAndDeletedAtIsNull(transactionEntity.getUuid())
//                                .collectList()
//                                .flatMap(ledgerEntityDB -> {
//                                    for (LedgerEntryEntity ledgerEntryEntity : ledgerEntityDB) {
//                                        ledgerEntryEntity.setDeletedBy(UUID.fromString(userId));
//                                        ledgerEntryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                    }
//                                    return ledgerEntryRepository.saveAll(ledgerEntityDB)
//                                            .then(Mono.just(ledgerEntityDB));
//                                }).flatMap(ledgerEntry -> {
//                                    //Deleting from Transactions Table
//                                    transactionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                    transactionEntity.setDeletedBy(UUID.fromString(userId));
//                                    List<SlaveLedgerEntryDto> resultLedgerDto = new ArrayList<>();
//                                    for (LedgerEntryEntity ledger : ledgerEntry) {
//                                        SlaveLedgerEntryDto dtoLedger = SlaveLedgerEntryDto
//                                                .builder()
//                                                .transactionUUID(transactionEntity.getUuid())
//                                                .ledgerId(ledger.getId())
//                                                .build();
//                                        resultLedgerDto.add(dtoLedger);
//                                    }
//
//                                    return deleteTransactionDtoBuilder(userId, resultLedgerDto, transactionEntity, transactionId, null)
//                                            .flatMap(deleteEntity -> transactionRepository.save(transactionEntity)
//                                                    .flatMap(docNature -> responseSuccessMsg("Record Deleted Successfully", deleteEntity))
//                                                    .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
//                                                    .onErrorResume(ex -> responseInfoMsg("Unable to Delete Record .Please Contact Developer."))
//                                            );
//                                })
//                        ))
//                ).switchIfEmpty(responseInfoMsg("Record Does not exist."))
//                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
//    }


//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        final long id = Long.parseLong(serverRequest.pathVariable("id"));
//        Mono<MultiValueMap<String, String>> formData = serverRequest.formData();
//
//        return formData.flatMap(value -> {
//            Boolean status = Boolean.parseBoolean(value.getFirst("status"));
//
//            return transactionRepository.findByIdAndDeletedAtIsNull(id)
//                    .flatMap(val -> {
//                        // If status is not boolean vale
//                        if (status != false && status != true) {
//                            return responseErrorMsg("Status must be InActive or Active");
//                        }
//
//                        // If already same status exist in database.
//                        if (((val.getStatus() ? true : false) == status)) {
//                            return responseWarningMsg("Record already exist with same status");
//                        }
//
//                        // Need to update
//                        val.setStatus(status == true ? true : false);
//
//                        return transactionRepository.save(val)
//                                .flatMap(statusUpdate -> responseSuccessMsg("Status changed successfully", val))
//                                .onErrorResume(err -> responseErrorMsg("Unable to update the status!"))
//                                .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to update the status.There is something wrong please try again.")));
//                    });
//        });
//    }


    //    public ArrayList<SlaveTransactionWithLedgerEntriesAndDocumentsDto> documentMapping(ArrayList<SlaveTransactionRecordDto> list) {
//
//        ArrayList<SlaveTransactionWithLedgerEntriesAndDocumentsDto> newList = new ArrayList<SlaveTransactionWithLedgerEntriesAndDocumentsDto>();
//        ArrayList<Long> transactionIds = new ArrayList<Long>();
//
//        for (int i = 0; i < list.size(); i++) {
//
//            if (!transactionIds.contains(list.get(i).getId())) {
//                newList.add(list.get(i));
//                transactionIds.add(list.get(i).getId());
//            } else {
//                for (int j = 0; j < newList.size(); j++) {
//
//                    if (newList.get(j).getId() == list.get(i).getId()) {
//                        newList.set(j, list.get(i));
//                    }
//
//                }
//            }
//        }
//
//        return newList;
//    }

    @AuthHasPermission(value = "account_api_v1_transactions_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

        //This Query fetch all the Transactions
        Mono<SlaveTransactionDto> showTransactionWithIdMono = slaveTransactionRepository
                .showAllTransactions(transactionUUID);

        //This Query fetch all the Transactions with ledger Join
        Flux<SlaveLedgerRowDto> ledgerEntryEntityFlux = slaveTransactionRepository
                .showAllLedgerRows(transactionUUID);

        //This Query fetch all the Transactions with Documents
        Flux<SlaveDocumentAttachmentDto> documentAttachment = slaveTransactionRepository
                .showAllDocumentAttachments(transactionUUID);

        return transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
                .flatMap(transactionEntity -> showTransactionWithIdMono
                                .flatMap(transaction -> ledgerEntryEntityFlux.collectList()
                                        .flatMap(ledgerFlux -> documentAttachment.collectList()
                                                        .flatMap(documentData -> {

                                                            SlaveTransactionDataDto transactionDataDto = SlaveTransactionDataDto
                                                                    .builder()
                                                                    .rows(ledgerFlux)
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
//                                                            .attachments(documentData)
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
                                        ))
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
