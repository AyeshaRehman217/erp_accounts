package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveDocumentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionRecordDto;
import tuf.webscaf.app.dbContext.slave.repository.SlaveTransactionRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.*;

@Component
@Tag(name = "generalJournalHandler")
public class GeneralJournalHandler {

    @Autowired
    SlaveTransactionRepository slaveTransactionRepository;

    @Autowired
    CustomResponse appresponse;

    @AuthHasPermission(value = "account_api_v1_general-journals_index")
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
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err-> responseErrorMsg("Unable to read request. Please Contact Developer."));

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
