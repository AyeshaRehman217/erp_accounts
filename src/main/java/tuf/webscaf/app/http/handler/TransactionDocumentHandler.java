package tuf.webscaf.app.http.handler;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
import tuf.webscaf.app.dbContext.master.dto.DocumentAttachmentDto;
import tuf.webscaf.app.dbContext.master.dto.DocumentDto;
import tuf.webscaf.app.dbContext.master.dto.TransactionDocumentDto;
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repository.TransactionDocumentPvtRepository;
import tuf.webscaf.app.dbContext.master.repository.TransactionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveVoucherEntity;
import tuf.webscaf.app.dbContext.slave.repository.SlaveTransactionDocumentPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.*;

@Component
@Tag(name = "transactionDocumentHandler")
public class TransactionDocumentHandler {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    TransactionDocumentPvtRepository transactionDocumentPvtRepository;

    @Autowired
    SlaveTransactionDocumentPvtRepository slaveTransactionDocumentPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "account_api_v1_transaction-documents_un-mapped_show")
    public Mono<ServerResponse> showUnMappedDocumentsAgainstTransaction(ServerRequest serverRequest) {

        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("transactionUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("skw", searchKeyWord);
        params.add("s", String.valueOf(size));
        params.add("p", String.valueOf(pageRequest));
        params.add("d", d);
        params.add("dp", directionProperty);


        return slaveTransactionDocumentPvtRepository.getAllDocumentListAgainstTransaction(transactionUUID)
                .flatMap(docIds -> {
                    List<String> docIdList = Arrays.asList(docIds.split("\\s*,\\s*"));
                    params.addAll("docId", docIdList);

                    return apiCallService.getDataWithQueryParams(driveUri + "api/v1/documents/un-mapped/show", params)
                            .flatMap(documentJsonNode -> {

                                final JsonNode arrNode = documentJsonNode.get("data");
                                JsonNode finalRecords= null;
                                for (final JsonNode objNode : arrNode) {
                                    finalRecords = objNode;

                                }

                                if (arrNode.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", apiCallService.getTotalDataRowsWithFilter(documentJsonNode));

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", finalRecords, apiCallService.getTotalDataRowsWithFilter(documentJsonNode));
                                }

                            });
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    //This Api Show List of Documents Against the Transaction Id in Drive Module
    @AuthHasPermission(value = "account_api_v1_transaction-documents_list_show")
    public Mono<ServerResponse> showListOfDocumentsAgainstTransaction(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("transactionUUID").trim());

        return slaveTransactionDocumentPvtRepository.getAllDocumentListAgainstTransaction(transactionUUID)
                .flatMap(ids -> {
                    List<String> listOfIds = Arrays.asList(ids.split("\\s*,\\s*"));
                    return responseSuccessMsg("Records Fetched Successfully", listOfIds);
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "account_api_v1_transaction-document_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        UUID transactionUUID = UUID.fromString((serverRequest.pathVariable("transactionUUID")));
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
            return responseWarningMsg("Unknown User");
        } else {
            if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User");
            }
        }
        return serverRequest.bodyToMono(TransactionDocumentDto.class)
                .flatMap(TransactionDocumentDto -> {

                    //Getting Attachment Object from Transaction Document Dto and Adding in List of Type Document Attachment Dto
                    List<DocumentAttachmentDto> documentAttachmentDto = new ArrayList<>(TransactionDocumentDto.getAttachments());

                    //Creating Empty List for Doc Id
                    List<UUID> listOfDocument = new ArrayList<>();

                    // Adding doc Ids to the List from request dto
                    for (DocumentAttachmentDto documentRow : documentAttachmentDto) {
                        listOfDocument.add(documentRow.getDoc_id());
                    }

                    // List for Doc Ids from Json Request that are not null
                    List<String> listOfDocumentsWithTransactions = new ArrayList<>();

                    // Add Doc Ids from Json Request that are not null
                    listOfDocument.forEach(uuid -> {
                        if (uuid != null) {
                            listOfDocumentsWithTransactions.add(uuid.toString());
                        }
                    });

                    if (listOfDocument.isEmpty()) {
                        return responseInfoMsg("Select the Documents First");
                    } else {

                        //Sending Doc ids in Form data to check if doc Ids exist
                        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                        for (String listOfValues : listOfDocumentsWithTransactions) {
                            formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
                        }

                        //posting Documents Ids in Drive Module Document Handler to get Only that document UUID's that exists
                        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map",userId, reqCompanyUUID, reqBranchUUID)
                                .flatMap(jsonNode2 -> {
                                    //Reading the Response "Data" Object from Json Node
                                    final JsonNode arrNode2 = jsonNode2.get("data");

                                    Map<String, String> documentMap = new HashMap<String, String>();

                                    List<DocumentAttachmentDto> responseAttachments = new LinkedList<>();

                                    if (arrNode2.isArray()) {
                                        for (final JsonNode objNode : arrNode2) {
                                            for (UUID documentIdData : listOfDocument) {
                                                JsonNode key = objNode.get(String.valueOf(documentIdData));

                                                // create document attachment dto for only
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


                                    return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDocumentUUIDInAndDeletedAtIsNull(transactionUUID, listOfDocument)
                                            .collectList()
                                            .flatMap(removelist -> {
                                                for (TransactionDocumentPvtEntity pvtEntity : removelist) {
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
                                                            .transactionUUID(transactionUUID)
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
                                                            //Empty List for Document Ids from Json Request user Enters
                                                            List<String> listOfDoc = new ArrayList<>();

                                                            finalDocumentList.forEach(uuid -> {
                                                                if (uuid != null) {
                                                                    listOfDoc.add(uuid.toString());
                                                                }
                                                            });


                                                            //Sending Document ids in Form data to check if document Id's exist
                                                            MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                                            for (String listOfDocumentUUID : listOfDoc) {
                                                                sendFormData.add("docId", listOfDocumentUUID);//iterating over multiple values and then adding in list
                                                            }

                                                            // if records already exist then document map key set will be empty
                                                            if (documentMap.keySet().isEmpty()) {
                                                                return responseSuccessMsg("Record Already Exists", responseAttachments)
                                                                        .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                        .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                            }

                                                            //
                                                            else {
                                                                return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userId, reqCompanyUUID, reqBranchUUID)
                                                                        .flatMap(document -> responseSuccessMsg("Record Stored Successfully", responseAttachments))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                        .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                            }
                                                        });
                                            });
                                }).switchIfEmpty(responseInfoMsg("Unable to attach document. There is something wrong please contact developer."))
                                .onErrorResume(err -> responseErrorMsg("Unable to attach document. Please contact developer."));
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    public DocumentDto documentDtoMapper(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<DocumentDto>() {
        });
        DocumentDto documentDto = null;
        if (!jsonNode.get("data").isEmpty()) {
            try {
                documentDto = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documentDto;
    }

    @AuthHasPermission(value = "account_api_v1_transaction-document_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID financialTransactionUUID = UUID.fromString(serverRequest.pathVariable("financialTransactionUUID"));
        UUID docId = UUID.fromString(serverRequest.queryParam("docId").map(String::toString).orElse(""));

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
        } else if (!(userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"))) {
            return responseWarningMsg("Unknown user");
        }

        return transactionRepository.findByUuidAndDeletedAtIsNull(financialTransactionUUID)
                .flatMap(financialTransactionEntity -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", docId)
                        .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                .flatMap(document -> transactionDocumentPvtRepository
                                        .findFirstByTransactionUUIDAndDocumentUUIDAndDeletedAtIsNull(financialTransactionUUID, docId)
                                        .flatMap(financialTransactionDocumentPvtEntity -> {

                                            financialTransactionDocumentPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                            financialTransactionDocumentPvtEntity.setDeletedBy(UUID.fromString(userId));
                                            financialTransactionDocumentPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                            financialTransactionDocumentPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                            financialTransactionDocumentPvtEntity.setReqDeletedIP(reqIp);
                                            financialTransactionDocumentPvtEntity.setReqDeletedPort(reqPort);
                                            financialTransactionDocumentPvtEntity.setReqDeletedBrowser(reqBrowser);
                                            financialTransactionDocumentPvtEntity.setReqDeletedOS(reqOs);
                                            financialTransactionDocumentPvtEntity.setReqDeletedDevice(reqDevice);
                                            financialTransactionDocumentPvtEntity.setReqDeletedReferer(reqReferer);

                                            return transactionDocumentPvtRepository.save(financialTransactionDocumentPvtEntity)
                                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", documentDtoMapper(documentJson)))
                                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer.")))
                        ).switchIfEmpty(responseInfoMsg("Document Record Does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Document Record Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Transaction Does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Transaction Does not exist.Please Contact Developer."));
    }

//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//
//        String userId = serverRequest.headers().firstHeader("auid");
//
//        final UUID transactionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        if (userId == null) {
//            return responseErrorMsg("Unknown user");
//        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//            return responseErrorMsg("Unknown user");
//        }
//
//        return serverRequest.bodyToMono(TransactionDocumentDto.class)
//                .flatMap(value -> transactionRepository.findByUuidAndDeletedAtIsNull(transactionUUID)
//                        .flatMap(transactionEntity -> {
//                            //Empty List for Document Ids from Json Request user Enters
//                            List<String> listOfDocumentsWithTransactions = new ArrayList<>();
//
//                            for (UUID id : value.getDocumentId()) {
//                                //Parsing Long list to String
//                                listOfDocumentsWithTransactions.add(String.valueOf(id));
//                            }
//
//                            if (!(listOfDocumentsWithTransactions.isEmpty())) {
//                                //List of Document ids to Store in Pvt Table
//                                List<TransactionDocumentPvtEntity> listPvt = new ArrayList<TransactionDocumentPvtEntity>();
//
//
//                                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
//                                for (String listOfValues : listOfDocumentsWithTransactions) {
//                                    formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
//                                }
//
//                                List<UUID> documentFinalList = new ArrayList<>();
//                                for (String s : listOfDocumentsWithTransactions) {
//                                    documentFinalList.add(UUID.fromString(s));
//                                }
//
//                                List<UUID> postDocumentList = new ArrayList<>();
//
//                                List<UUID> postBucketIdList = new ArrayList<>();
//                                //getting List of Ids from Drive Module Api from documents
//                                return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map")
//                                        .flatMap(jsonNode2 -> {
//                                            final JsonNode arrNode2 = jsonNode2.get("data");
//
//                                            List<Object> returningDocumentListOfObject = new ArrayList<>();
//
//
//                                            Map<String, String> documentMap = new HashMap<String, String>();
//
//                                            List<SlaveTransactionDocumentDto> listOfTransaction = new ArrayList<>();
//
//                                            if (arrNode2.isArray()) {
//                                                for (final JsonNode objNode : arrNode2) {
//                                                    for (UUID documentIdData : documentFinalList) {
//                                                        JsonNode key = objNode.get(String.valueOf(documentIdData));
//                                                        if (key != null) {
//                                                            //Adding Keys in Document Id in Returning List
//                                                            if (!(returningDocumentListOfObject.contains(key))) {
//                                                                returningDocumentListOfObject.add(key);
//                                                            }
//                                                            String documentId = key.get("documentId").toString();
//
//                                                            postDocumentList.add(UUID.fromString(documentId));
//                                                            String documentBucketId = key.get("docBucketId").toString();
//                                                            postBucketIdList.add(UUID.fromString(documentBucketId));
//                                                            documentMap.put(documentId, documentBucketId);
//                                                        }
//                                                    }
//                                                }
//                                            }
//
//                                            //Removing Already Existing Values from document Final List
//                                            documentFinalList.retainAll(postDocumentList);
//
//                                            return transactionDocumentPvtRepository.findAllByTransactionUUIDAndDocumentUUIDInAndDeletedAtIsNull(transactionEntity.getUuid(), documentFinalList)
//                                                    .collectList()
//                                                    .flatMap(removelist -> {
//                                                        for (TransactionDocumentPvtEntity pvtEntity : removelist) {
//                                                            documentFinalList.remove(pvtEntity.getDocumentUUID());
//                                                            documentMap.remove(pvtEntity.getDocumentUUID().toString());
//                                                        }
//
//                                                        for (String documentIdsListData : documentMap.keySet()) {
//                                                            TransactionDocumentPvtEntity transactionDocumentPvtEntity = TransactionDocumentPvtEntity
//                                                                    .builder()
//                                                                    .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData)))
//                                                                    .documentUUID(UUID.fromString(documentIdsListData))
//                                                                    .transactionUUID(transactionEntity.getUuid())
//                                                                    .createdBy(UUID.fromString(userId))
//                                                                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                                                    .build();
//                                                            listPvt.add(transactionDocumentPvtEntity);
//                                                        }
//
//                                                        return transactionDocumentPvtRepository.saveAll(listPvt)
//                                                                .collectList()
//                                                                .flatMap(transactionDB -> {
//                                                                    SlaveStoreTransactionDocumentDto slaveTransactionDocumentDto = SlaveStoreTransactionDocumentDto
//                                                                            .builder()
//                                                                            .transactionId(transactionEntity.getUuid())
//                                                                            .documents(returningDocumentListOfObject)
//                                                                            .build();
//                                                                    return responseSuccessMsg("Record Stored Successfully", slaveTransactionDocumentDto)
//                                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record,There is something wrong please try again!"))
//                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
//                                                                });
//                                                    }).switchIfEmpty(responseInfoMsg("Unable to store Record,There is something wrong please try again!"))
//                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
//                                        });
//                            } else {
//                                return responseInfoMsg("Select Documents First");
//                            }
//                        }).switchIfEmpty(responseInfoMsg("Transaction does not exist"))
//                        .onErrorResume(err -> responseErrorMsg("Transaction does not exist.Please Contact Developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
//    }


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
}
