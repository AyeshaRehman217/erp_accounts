package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;
import tuf.webscaf.app.dbContext.master.repository.CalendarPeriodsRepository;
import tuf.webscaf.app.dbContext.master.repository.CalendarRepository;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveLedgerEntryRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.service.PdfService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Component
@Tag(name = "financialReportsHandler")
public class FinancialReportsHandler {

    @Autowired
    PdfService pdfService;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlaveLedgerEntryRepository slaveLedgerEntryRepository;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

    @Autowired
    CalendarPeriodsRepository calendarPeriodsRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_config_module.uri}")
    private String configUri;

    @Value("${server.erp_account_module.uri}")
    private String accountBaseURI;

    @Value("${server.erp_auth_module.uri}")
    private String authURI;

    public static double evaluate(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }


    public static ArrayNode setChildrenListData(ArrayNode arrayNode, MultiValueMap<String, Double> resultSetMap) {
        //Setting Response Node
        ArrayNode responseData = new ArrayNode(JsonNodeFactory.instance);
        //iterating over the array node
        for (int i = 0; i < arrayNode.size(); i++) {
            ObjectNode row = null;
            row = (ObjectNode) arrayNode.get(i);
            //putting calculated result from result map against the given formula
            row.put("calculationFormula", String.valueOf(resultSetMap.getFirst(String.valueOf(row.get("id")).replaceAll("\"", ""))));

            //iterating over the child list in rows
            if (row.has("childList")) {
                ArrayNode childrenRows = (ArrayNode) row.get("childList");
                setChildrenListData(childrenRows, resultSetMap);
                row.set("childList", childrenRows);
            }

            //adding all the rows in response data array node
            responseData.add(row);
        }

        return responseData;
    }

    //getting List from front End
    public static List<AccountChildDto> getChildrenList(ArrayNode arrayNode, List<AccountChildDto> accountChildDtoList) {
        //iterating over the array node
        for (int i = 0; i < arrayNode.size(); i++) {
            //creating Object Node from Array Node
            ObjectNode row = (ObjectNode) arrayNode.get(i);
            //creating Account Child Dto with Account UUID,Formula and ID's
            AccountChildDto accountChildDto = AccountChildDto.builder()
                    .id(String.valueOf(row.get("id")).replaceAll("\"", ""))
                    .accountUUID(String.valueOf(row.get("accountUUID")).replaceAll("\"", ""))
                    .formula(String.valueOf(row.get("calculationFormula")).replaceAll("\"", ""))
                    .build();
            //Adding All the Account child dto in list
            accountChildDtoList.add(accountChildDto);
            //check if row has child then iterate over the list
            if (row.has("childList")) {
                ArrayNode childrenRows = (ArrayNode) row.get("childList");
                getChildrenList(childrenRows, accountChildDtoList);
            }
        }

        return accountChildDtoList;
    }

    public Mono<ServerResponse> profitAndLossStatement(ServerRequest serverRequest) {

        String startDate = serverRequest.queryParam("startDate").map(String::toString).orElse("");

        String endDate = serverRequest.queryParam("endDate").map(String::toString).orElse("");

        LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        return serverRequest.bodyToMono(JsonNode.class)
                .flatMap(jsonNode -> {

                    //Adding the Two Values in Json Value Object Node
                    ArrayNode statementObjectNode = (ArrayNode) jsonNode;
                    //List of Account Child Dto to Pass in recursive function to get Child
                    List<AccountChildDto> accountChildDtoList = new LinkedList<>();

                    //Adding All Accounts From Request in List
                    List<UUID> accountUUIDList = new ArrayList<>();

                    //calculation formula map contains Account UUID as key and formulas against those keys
                    MultiValueMap<UUID, String> calculationFormulaMap = new LinkedMultiValueMap<>();

                    MultiValueMap<String, String> accountAndRowIDMap = new LinkedMultiValueMap<>();


                    for (AccountChildDto accountChildDto : getChildrenList(statementObjectNode, accountChildDtoList)) {

                        //get Accounts List from the dto
                        UUID accountUUID = UUID.fromString(accountChildDto.getAccountUUID().replaceAll("\"", ""));
                        accountUUIDList.add(accountUUID);

                        //get Calculation formula list from dto and check where formula provided then add in Map
                        String formula = accountChildDto.getFormula().replaceAll("\"", "");
                        if (!formula.equals("")) {
                            calculationFormulaMap.add(accountUUID, formula);
                        }

                        //getting Row/Path IDs from dto
                        String pathId = accountChildDto.getId().replaceAll("\"", "");

                        //getting Account UUID from dto
                        String account = accountChildDto.getAccountUUID().replaceAll("\"", "");

                        //Creating Map with Account and Row ID's
                        accountAndRowIDMap.add(account, pathId);
                    }


                    //Getting Distinct/Removing Repetitive values Fom the List of Accounts
                    accountUUIDList = accountUUIDList.stream()
                            .distinct()
                            .collect(Collectors.toList());

                    //Creating an Empty String for Transaction List
                    String accountList = "";
                    //Iterating Over The Transaction List
                    for (UUID val : accountUUIDList) {
                        //Getting the last index of list size
                        if (accountUUIDList.indexOf(val) == accountUUIDList.size() - 1) {
                            //Adding '' around each value of list
                            accountList = accountList + "'" + val + "'";
                        } else {
                            //Separating the list values with comma
                            accountList = accountList + "'" + val + "' ,";
                        }
                    }

                    //Final account List to be used in Flatmap
                    List<UUID> finalAccountUUIDList = accountUUIDList;

                    //Query over Ledger Entries to find Closing Balance of provided Accounts
                    return slaveLedgerEntryRepository.profitAndLossStatement(accountList, finalStartDate)
                            .collectList()
                            .flatMap(profitLossDto -> {

                                //Map with Account UUID and Closing Balance
                                MultiValueMap<String, Double> profitAndLossMap = new LinkedMultiValueMap<>();

                                for (SlaveProfitAndLossStatementDto dto : profitLossDto) {
                                    //removing Accounts that contains ledger entries from List
                                    finalAccountUUIDList.remove(dto.getAccountUUID());
                                    profitAndLossMap.add(dto.getAccountUUID().toString(), dto.getBalanceBroughtForward());
                                }


                                //This Map Contains Path/Row ID's with Balance
                                MultiValueMap<String, Double> mapWithPathAndCarry = new LinkedMultiValueMap<>();

                                //Iterating over the Account List that's not return by the query
                                for (UUID accountNotInLedger : finalAccountUUIDList) {
                                    if (accountAndRowIDMap.containsKey(accountNotInLedger.toString())) {
                                        //Assigning 0 value to account that does not contain any Ledger Entry
                                        mapWithPathAndCarry.add(accountAndRowIDMap.getFirst(String.valueOf(accountNotInLedger)), 0.0);
                                    }
                                }

                                for (String ids : profitAndLossMap.keySet()) {
                                    if (accountAndRowIDMap.containsKey(ids)) {
                                        //Adding Path Row ID's and Balance that returns from the query
                                        mapWithPathAndCarry.add(accountAndRowIDMap.getFirst(ids), profitAndLossMap.getFirst(ids));
                                    }
                                }

                                //result set map evaluates the formulas against Account UUID keys
                                MultiValueMap<String, Double> resultSetMap = new LinkedMultiValueMap<>();
                                for (UUID accountUUID : calculationFormulaMap.keySet()) {

                                    String formula = calculationFormulaMap.getFirst(accountUUID);

                                    String result = formula;

                                    for (String id : mapWithPathAndCarry.keySet()) {
                                        result = result.replace(id, mapWithPathAndCarry.getFirst(id).toString());

                                    }

                                    resultSetMap.set(accountAndRowIDMap.getFirst(String.valueOf(accountUUID)), evaluate(result));
                                }

                                if (!profitLossDto.isEmpty()) {
                                    return responseSuccessMsg("Record Fetched Successfully!", setChildrenListData(statementObjectNode, resultSetMap));
                                } else {
                                    return responseInfoMsg("Record Does not Exist");
                                }
                            });

                });
    }


    /**
     * This Ledger Report Shows Summary of Account
     **/
    public Mono<ServerResponse> ledgerSummaryReport(ServerRequest serverRequest) {

        final UUID accountUUID = UUID.fromString(serverRequest.pathVariable("accountUUID").trim());

        String startDate = serverRequest.queryParam("startDate").map(String::toString).orElse("");

        String endDate = serverRequest.queryParam("endDate").map(String::toString).orElse("");
        LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        Flux<SlaveReportingLedgerEntriesDto> ledgerReportingFlux = slaveLedgerEntryRepository
                .showFinancialLedgerReporting(accountUUID, finalStartDate, finalEndDate);

        return ledgerReportingFlux
                .collectList()
                .flatMap(ledgerRows -> slaveLedgerEntryRepository.countCarriedForward(accountUUID, finalStartDate, finalEndDate)
                        .flatMap(carryForward -> slaveLedgerEntryRepository.countBroughtForward(accountUUID, finalStartDate)
                                .flatMap(cashBroughtDown -> slaveLedgerEntryRepository.countLedgerEntrySummaryRecords(accountUUID, finalStartDate, finalEndDate)
                                        .flatMap(countLedger -> {

                                            String balanceBroughtDown = cashBroughtDown.toString();
                                            //check if cash brought down is less than zero and is negative than display with () parenthesis instead of - sign
                                            if (cashBroughtDown < 0.0) {
                                                balanceBroughtDown = "(" + Math.abs(cashBroughtDown) + ")";
                                            }

                                            String balanceCarriedForward = carryForward.toString();
                                            //check if carried forward down is less than zero and is negative than display with () parenthesis instead of - sign
                                            if (carryForward < 0.0) {
                                                balanceCarriedForward = "(" + Math.abs(carryForward) + ")";
                                            }

                                            SlaveLedgerSummaryReportDto slaveLedgerSummaryReportDto = SlaveLedgerSummaryReportDto
                                                    .builder()
                                                    .balanceBroughtForward(balanceBroughtDown)
                                                    .ledgerEntrySummaryList(ledgerRows)
                                                    .balanceCarriedForward(balanceCarriedForward)
                                                    .build();

                                            if (!ledgerRows.isEmpty()) {
                                                return responseIndexSuccessMsg("Record Fetched Successfully!", slaveLedgerSummaryReportDto, countLedger);
                                            } else {
                                                return responseIndexInfoMsg("Record Does not Exist.", countLedger);
                                            }
                                        })
                                )
                        )
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }


    /**
     * This Ledger Report Shows Summary of Account
     **/
    @AuthHasPermission(value = "account_api_v1_ledger-report_show")
    public Mono<ServerResponse> ledgerReport(ServerRequest serverRequest) {

        final UUID accountUUID = UUID.fromString(serverRequest.pathVariable("accountUUID").trim());

        String startDate = serverRequest.queryParam("startDate").map(String::toString).orElse("");

        String endDate = serverRequest.queryParam("endDate").map(String::toString).orElse("");
        LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        Pageable pageable = PageRequest.of(page, size);

        Flux<SlaveLedgerReportDto> ledgerReportingFlux = slaveLedgerEntryRepository
                .showLedgerReport(accountUUID, finalStartDate, finalEndDate, pageable.getPageSize(), pageable.getOffset());

        // if start date is before end date
        if (finalEndDate.isBefore(finalStartDate)) {
            return responseInfoMsg("Start Date must be before the End Date");
        }

        return ledgerReportingFlux
                .collectList()
                .flatMap(ledgerRows -> {

                    List<SlaveLedgerReportDto> ledgerReport = removeDuplicateInList(ledgerRows);

                    if (!ledgerReport.isEmpty()) {
                        return responseIndexSuccessMsg("Record Fetched Successfully", ledgerReport, (long) ledgerReport.size());
                    } else {
                        return responseIndexInfoMsg("Record Does not Exist.", (long) ledgerReport.size());
                    }

                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }


    //This Function is used to Remove Duplicates from List
    public List<SlaveLedgerReportDto> removeDuplicateInList(List<SlaveLedgerReportDto> list) {

        //Creating a Refined List with No Duplicates
        List<SlaveLedgerReportDto> newList = new LinkedList<>();
        List<String> rowIds = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            // if row id is empty
            if (list.get(i).getRowId().isEmpty()) {
                newList.add(list.get(i));
            }

            // if row id is not empty
            else {
                //if no Element Exists in list then add element in list
                if (!rowIds.contains(list.get(i).getRowId())) {
                    //Adding Transaction UUID in New List if No Element Exist
                    newList.add(list.get(i));
                    rowIds.add(list.get(i).getRowId());
                } else {
                    for (int j = 0; j < newList.size(); j++) {
                        //If the list element already exist than set the previous value
                        if (Objects.equals(newList.get(j).getRowId(), list.get(i).getRowId())) {
                            newList.set(j, list.get(i));
                        }

                    }
                }
            }
        }
        return newList;
    }

    /**
     * This Function Displays Trial Balance based on Account UUID group and the sum of debit and credit
     **/
    public Mono<ServerResponse> trialBalanceReport(ServerRequest serverRequest) {

        String startDate = serverRequest.queryParam("startDate").map(String::toString).orElse("");

        String endDate = serverRequest.queryParam("endDate").map(String::toString).orElse("");
        LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("transactionDate");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveTrialBalanceDto> trialBalanceFlux = slaveAccountRepository
                .trialBalance(finalStartDate, finalEndDate, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

        return trialBalanceFlux
                .collectList()
                .flatMap(trialBalanceDto -> slaveAccountRepository.countTrialBalanceRecords(finalStartDate, finalEndDate)
                        .flatMap(count -> {

                            SlaveTrialBalanceReturnDto slaveTrialBalanceReportDBDto = null;
                            MultiValueMap<String, SlaveTrialBalanceReturnDto> trialBalanceMapDto = new LinkedMultiValueMap<>();
                            for (SlaveTrialBalanceDto trialBalanceDto1 : trialBalanceDto) {

                                String netRunningBalance = trialBalanceDto1.getNetBalance().toString();

                                String balanceBroughtForward = trialBalanceDto1.getBalanceBroughtForward().toString();

                                //check if net Balance is less than zero and is negative than display with () parenthesis instead of - sign
                                if (trialBalanceDto1.getNetBalance() < 0.0) {
                                    netRunningBalance = "(" + Math.abs(trialBalanceDto1.getNetBalance()) + ")";
                                }

                                //check if carried forward is less than zero and is negative than display with () parenthesis instead of - sign
                                if (trialBalanceDto1.getBalanceBroughtForward() < 0.0) {
                                    balanceBroughtForward = "(" + Math.abs(trialBalanceDto1.getBalanceBroughtForward()) + ")";
                                }

                                slaveTrialBalanceReportDBDto = SlaveTrialBalanceReturnDto
                                        .builder()
                                        .accountCode(trialBalanceDto1.getAccountCode())
                                        .accountName(trialBalanceDto1.getAccountName())
                                        .accountUUID(trialBalanceDto1.getAccountUUID())
                                        .accountTypeUUID(trialBalanceDto1.getAccountTypeUUID())
                                        .accountTypeName(trialBalanceDto1.getAccountTypeName())
                                        .debit(trialBalanceDto1.getDebit())
                                        .credit(trialBalanceDto1.getCredit())
                                        .netBalance(netRunningBalance)
                                        .balanceBroughtForward(balanceBroughtForward)
                                        .build();

                                trialBalanceMapDto.add(trialBalanceDto1.getAccountTypeName(), slaveTrialBalanceReportDBDto);
                            }

                            if (!trialBalanceMapDto.isEmpty()) {
                                return responseIndexSuccessMsg("Record Fetched Successfully", trialBalanceMapDto, count);
                            } else {
                                return responseIndexInfoMsg("Record Does not exist", count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "account_api_v1_trial-balance")
    public Mono<ServerResponse> trialBalance(ServerRequest serverRequest) {

        String startDate = serverRequest.queryParam("startDate").map(String::toString).orElse("");

        String endDate = serverRequest.queryParam("endDate").map(String::toString).orElse("");

        LocalDateTime finalStartDate = LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        LocalDateTime finalEndDate = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("name");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveTrialBalanceReportDto> trialBalanceFlux = slaveAccountRepository
                .trialBalanceReport(finalStartDate, finalEndDate, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

        // if start date is before end date
        if (finalEndDate.isBefore(finalStartDate)) {
            return responseInfoMsg("Start Date must be before the End Date");
        }

        return trialBalanceFlux
                .collectList()
                .flatMap(trialBalanceDto -> {

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveTrialBalanceReportDto> innerChildMap = new LinkedMultiValueMap<>();

                    //creating List of Parent UUID
                    List<SlaveTrialBalanceReportDto> mainParent = new ArrayList<>();


                    //creating List of Accounts Where Parent Account UUID is null
                    List<SlaveTrialBalanceReportDto> mainParentWhereNull = new ArrayList<>();

                    //creating List of parent account uuid where not null
                    List<UUID> accountUUID = new ArrayList<>();

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveTrialBalanceReportDto> paginatingMap = new LinkedMultiValueMap<>();

                    //iterating over the loop from query
                    for (SlaveTrialBalanceReportDto chartList : trialBalanceDto) {
                        // Add all records to map with uuid as key
                        paginatingMap.add(chartList.getUuid(), chartList);

                        //check where parent Account UUID is null then add in the Main Parent Account List
                        if (chartList.getParentAccountUUID() == null) {
                            mainParentWhereNull.add(chartList);

                        } else {
                            // add records to map with parent account uuid as key
                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);

                            if (!accountUUID.contains(chartList.getParentAccountUUID())) {
                                accountUUID.add(chartList.getParentAccountUUID());
                            }
                        }
                    }

                    // iterating over parent uuids
                    for (UUID parentUUID : accountUUID) {
                        // check if all records with pagination map does not contain parent account uuid
                        if (!paginatingMap.containsKey(parentUUID)) {
                            mainParent.addAll(innerChildMap.get(parentUUID));
                        }
                    }

                    // add all root parents to Main Parent List
                    mainParent.addAll(mainParentWhereNull);

                    //iterating over the Main Parent List
                    for (SlaveTrialBalanceReportDto parentWhereNull : mainParent) {
                        // Calling Recursive Function to check
                        parentWhereNull.setChildAccounts(setChildAccountsInTrialBalance(innerChildMap, parentWhereNull));
                    }

                    return slaveAccountRepository.countFinancialChartOfAccountWithPagination()
                            .flatMap(count -> {

                                if (!mainParent.isEmpty()) {
                                    return responseIndexSuccessMsg("Record Fetched Successfully", mainParent, count);
                                } else {
                                    return responseIndexInfoMsg("Record Does not exist", count);
                                }
                            });
                }).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    //Setting Inner Child Accounts
    public List<SlaveChartOfAccountDto> setChildAccounts(MultiValueMap<UUID, SlaveChartOfAccountDto> childMap, SlaveChartOfAccountDto chartsDto) {

        //Creating a Chart of Account List for returning records
        List<SlaveChartOfAccountDto> chartList = new ArrayList<>();

        //check where map key is not null
        if (childMap.get(chartsDto.getUuid()) != null) {
            //iterating over the map based on UUID key
            for (SlaveChartOfAccountDto chart : childMap.get(chartsDto.getUuid())) {
                //Setting Inner Child Accounts
                chart.setChildAccount(setChildAccounts(childMap, chart));
                //Adding in Returning List
                chartList.add(chart);
            }
        }
        return chartList;
    }


    //Setting Inner Child Accounts
    public List<SlaveTrialBalanceReportDto> setChildAccountsInTrialBalance(MultiValueMap<UUID, SlaveTrialBalanceReportDto> childMap, SlaveTrialBalanceReportDto trialBalanceDto) {

        //Creating a Chart of Account List for returning records
        List<SlaveTrialBalanceReportDto> trialBalanceList = new ArrayList<>();

        //check where map key is not null
        if (childMap.get(trialBalanceDto.getUuid()) != null) {
            //iterating over the map based on UUID key
            for (SlaveTrialBalanceReportDto trialBalance : childMap.get(trialBalanceDto.getUuid())) {
                //Setting Inner Child Accounts
                trialBalance.setChildAccounts(setChildAccountsInTrialBalance(childMap, trialBalance));
                //Adding in Returning List
                trialBalanceList.add(trialBalance);
            }
        }
        return trialBalanceList;
    }

    @AuthHasPermission(value = "account_api_v1_chart-of-accounts_index")
    public Mono<ServerResponse> chartOfAccounts(ServerRequest serverRequest) {

        Flux<SlaveChartOfAccountDto> chartReportingFlux = slaveAccountRepository
                .chartOfAccounts();

        return chartReportingFlux
                .collectList()
                .flatMap(charts -> {

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveChartOfAccountDto> innerChildMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();

                    //creating List of Parent UUID
                    List<SlaveChartOfAccountDto> mainParent = new ArrayList<>();

                    //iterating over the loop from query
                    for (SlaveChartOfAccountDto chartList : charts) {

                        //check where parent Account UUID is null then add in the Main Parent Account List
                        if (chartList.getParentAccountUUID() == null) {
                            mainParent.add(chartList);


                        } else {
                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);
                        }
                    }

                    //iterating over the Main Parent List
                    for (SlaveChartOfAccountDto parentWhereNull : mainParent) {
                        // Calling Recursive Function to check
                        parentWhereNull.setChildAccount(setChildAccounts(innerChildMap, parentWhereNull));
                    }

                    return slaveAccountRepository.countFinancialChartOfAccounts()
                            .flatMap(countCharts -> {
                                if (!mainParent.isEmpty()) {

                                    return responseIndexSuccessMsg("Record Fetched Successfully", mainParent, countCharts);
                                } else {
                                    return responseIndexInfoMsg("Record Does not Exist", countCharts);
                                }
                            });
                });
    }


    @AuthHasPermission(value = "account_api_v1_chart-of-accounts-list-with-pagination_index")
    public Mono<ServerResponse> chartOfAccountsWithPagination(ServerRequest serverRequest) {

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

        Pageable pageable = PageRequest.of(page, size);

        Flux<SlaveChartOfAccountDto> chartReportingFlux = slaveAccountRepository
                .chartOfAccountsWithPagination(pageable.getPageSize(), pageable.getOffset());

        return chartReportingFlux
                .collectList()
                .flatMap(reportList -> {

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveChartOfAccountDto> innerChildMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();

                    //creating List of Parent UUID
                    List<SlaveChartOfAccountDto> mainParent = new ArrayList<>();


                    //creating List of Accounts Where Parent Account UUID is null
                    List<SlaveChartOfAccountDto> mainParentWhereNull = new ArrayList<>();

                    //creating List of parent account uuid where not null
                    List<UUID> accountUUID = new ArrayList<>();

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveChartOfAccountDto> paginatingMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();

                    //iterating over the loop from query
                    for (SlaveChartOfAccountDto chartList : reportList) {
                        // Add all records to map with uuid as key
                        paginatingMap.add(chartList.getUuid(), chartList);

                        //check where parent Account UUID is null then add in the Main Parent Account List
                        if (chartList.getParentAccountUUID() == null) {
                            mainParentWhereNull.add(chartList);

                        } else {
                            // add records to map with parent account uuid as key
                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);

                            if (!accountUUID.contains(chartList.getParentAccountUUID())) {
                                accountUUID.add(chartList.getParentAccountUUID());
                            }
                        }
                    }

                    // iterating over parent uuids
                    for (UUID parentUUID : accountUUID) {
                        // check if all records with pagination map does not contain parent account uuid
                        if (!paginatingMap.containsKey(parentUUID)) {
                            mainParent.addAll(innerChildMap.get(parentUUID));
                        }
                    }

                    // add all root parents to Main Parent List
                    mainParent.addAll(mainParentWhereNull);

                    //iterating over the Main Parent List
                    for (SlaveChartOfAccountDto parentWhereNull : mainParent) {
                        // Calling Recursive Function to check
                        parentWhereNull.setChildAccount(setChildAccounts(innerChildMap, parentWhereNull));
                    }

                    return slaveAccountRepository.countFinancialChartOfAccountWithPagination()
                            .flatMap(countCharts -> {

                                if (!mainParent.isEmpty()) {
                                    return responseIndexSuccessMsg("Record Fetched Successfully", mainParent, countCharts);
                                } else {
                                    return responseIndexInfoMsg("Record Does not Exist", countCharts);
                                }

                            });
                });
    }


    //show child to parent hierarchy based on parent account UUID
    @AuthHasPermission(value = "account_api_v1_child-to-parent_list_show")
    public Mono<ServerResponse> childToParentAccountList(ServerRequest serverRequest) {

        UUID accountUUID = UUID.fromString(serverRequest.pathVariable("accountUUID"));

        Flux<SlaveChartOfAccountDto> childParentFlux = slaveAccountRepository
                .fetchParentBasedOnChildParent(accountUUID);

        return childParentFlux
                .collectList()
                .flatMap(childParentList -> {

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveChartOfAccountDto> innerChildMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();

                    //creating List of Parent UUID
                    List<SlaveChartOfAccountDto> mainParent = new ArrayList<>();

                    //iterating over the loop from query
                    for (SlaveChartOfAccountDto chartList : childParentList) {

                        //check where parent Account UUID is null then add in the Main Parent Account List
                        if (chartList.getParentAccountUUID() == null) {
                            mainParent.add(chartList);
                        } else {
                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);
                        }
                    }

                    //iterating over the Main Parent List
                    for (SlaveChartOfAccountDto parentWhereNull : mainParent) {
                        // Calling Recursive Function to check
                        parentWhereNull.setChildAccount(setChildAccounts(innerChildMap, parentWhereNull));
                    }


                    return slaveAccountRepository.countChildToParentRecords(accountUUID)
                            .flatMap(childToParentCount -> {
                                if (!childParentList.isEmpty()) {
                                    return responseIndexSuccessMsg("Record Fetched Successfully", mainParent, childToParentCount);
                                } else {
                                    return responseIndexInfoMsg("Record Does not exist", childToParentCount);
                                }
                            });
                });
    }


    //show parent to child hierarchy based on account UUID
    @AuthHasPermission(value = "account_api_v1_parent-to-child_list_show")
    public Mono<ServerResponse> parentToChildAccountList(ServerRequest serverRequest) {

        UUID parentAccountUUID = UUID.fromString(serverRequest.pathVariable("parentAccountUUID"));

        Flux<SlaveChartOfAccountDto> childParentFlux = slaveAccountRepository
                .fetchChildBasedOnParentAccount(parentAccountUUID);

        return childParentFlux
                .collectList()
                .flatMap(parentToChildList -> {

                    //Map where parent Account UUID is not Null
                    MultiValueMap<UUID, SlaveChartOfAccountDto> innerChildMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();

                    //creating List of Parent UUID
                    List<SlaveChartOfAccountDto> mainParent = new ArrayList<>();

                    //iterating over the loop from query
                    for (SlaveChartOfAccountDto chartList : parentToChildList) {

                        //check where parent Account UUID is null then add in the Main Parent Account List
                        if (chartList.getParentAccountUUID() == null) {
                            mainParent.add(chartList);
                        } else {
                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);
                        }
                    }

                    //iterating over the Main Parent List
                    for (SlaveChartOfAccountDto parentWhereNull : mainParent) {
                        // Calling Recursive Function to check
                        parentWhereNull.setChildAccount(setChildAccounts(innerChildMap, parentWhereNull));
                    }


                    return slaveAccountRepository.countParentToChildRecords(parentAccountUUID)
                            .flatMap(childToParentCount -> {
                                if (!parentToChildList.isEmpty()) {
                                    return responseIndexSuccessMsg("Record Fetched Successfully", mainParent, childToParentCount);
                                } else {
                                    return responseIndexInfoMsg("Record Does not exist", childToParentCount);
                                }
                            });
                });
    }


//    public Mono<ServerResponse> getEmployeeRecordReport(ServerRequest serverRequest) {
//
//        String userId = serverRequest.headers().firstHeader("auid");
//        UUID cashPaymentVoucherUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
//        return apiCallService.getUserName(authURI + "api/v1/users/show/" + userId)
//                .flatMap(email -> apiCallService.getVoucherResponse(accountBaseURI + "api/v1/cash-payment-vouchers/show/", cashPaymentVoucherUUID)
//                                .flatMap(jsonNode -> {
//                                    System.out.println("------------------------");
//                                    System.out.println("jsonNode = "+ jsonNode);
//                                    System.out.println("------------------------");
//                                    SlaveTransactionReportDto transactionReportDto = apiCallService.getTransactionData(jsonNode);
//                                    return calendarPeriodsRepository.findByUuidAndDeletedAtIsNull(transactionReportDto.getTransaction_data().getCalendar_period_uuid())
//                                            .flatMap(calendarPeriodEntity -> calendarRepository.findByUuidAndDeletedAtIsNull(calendarPeriodEntity.getCalendarUUID())
//                                                            .flatMap(calendar -> apiCallService.getDataWithUUID(configUri + "api/v1/companies/show/", transactionReportDto.getTransaction_data().getCompany_uuid())
//                                                                            .flatMap(companyJson -> apiCallService.getCompanyEntity(companyJson)
//                                                                                    .flatMap(company -> apiCallService.getDataWithUUID(configUri + "api/v1/branches/show/", transactionReportDto.getTransaction_data().getBranch_uuid())
//                                                                                                    .flatMap(branchJson -> apiCallService.getBranchEntity(branchJson)
//                                                                                                            .flatMap(branch -> {
//                                                                                                                try {
//                                                                                                                    List<CostCenterEntity> costCenterEntityList = new ArrayList<>();
//
//                                                                                                                    for (int i = 0; i < transactionReportDto.getTransaction_data().getRows().size(); i++) {
////                                                                                                                for (SlaveTransactionReportDto jsonNode1 : transactionReportDto)
//
//                                                                                                                        CostCenterEntity emp1 = new CostCenterEntity(
//                                                                                                                                (i+1),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_code().equals("null") ? "" : transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_code(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_name().equals("null") ? "" : transactionReportDto.getTransaction_data().getRows().get(i).getAccount().getAccount_name(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getDescription().equals("null") ? "" : transactionReportDto.getTransaction_data().getRows().get(i).getDescription(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getCost_center().getName().equals("null") ? "" : transactionReportDto.getTransaction_data().getRows().get(i).getCost_center().getName(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getProfit_center().getName().equals("null") ? "" : transactionReportDto.getTransaction_data().getRows().get(i).getProfit_center().getName(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getDr().toString(),
//                                                                                                                                transactionReportDto.getTransaction_data().getRows().get(i).getCr().toString()
//                                                                                                                        );
//
//                                                                                                                        costCenterEntityList.add(emp1);
//                                                                                                                    }
//
//                                                                                                                    String getTransactionDate = String.valueOf(transactionReportDto.getTransaction_data().getDate());
//
//                                                                                                                    Date printDate = new Date();
//                                                                                                                    DateFormat formattedPrintDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
//
//                                                                                                                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
//                                                                                                                    DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//                                                                                                                    Date formatTransactionDate = inputFormat.parse(getTransactionDate);
//                                                                                                                    String transactionDate = outputFormat.format(formatTransactionDate);
//
//                                                                                                                    //dynamic parameters required for report
//                                                                                                                    Map<String, Object> empParams = new HashMap<String, Object>();
//                                                                                                                    empParams.put("CompanyName", company.getName().equals("null") ? "" : company.getName());
//                                                                                                                    empParams.put("BranchName", branch.getName().equals("null") ? "" : branch.getName());
//                                                                                                                    empParams.put("VoucherCatalogue", transactionReportDto.getVoucher().getVoucherType().getName().equals("null") ? "" : transactionReportDto.getVoucher().getVoucherType().getName());
//                                                                                                                    empParams.put("VoucherName", transactionReportDto.getVoucher().getName().equals("null") ? "" : transactionReportDto.getVoucher().getName());
//                                                                                                                    empParams.put("Voucher#", transactionReportDto.getTransaction_id().toString());
//                                                                                                                    empParams.put("Date", transactionDate);
//                                                                                                                    empParams.put("Cald", calendar.getName().equals("null") ? "" : calendar.getName());
//                                                                                                                    empParams.put("Job", transactionReportDto.getTransaction_data().getJob_center().getName().equals("null") ? "" : transactionReportDto.getTransaction_data().getJob_center().getName());
//                                                                                                                    empParams.put("Perd", calendarPeriodEntity.getPeriodNo().toString());
//                                                                                                                    empParams.put("Qua", calendarPeriodEntity.getQuarter().toString());
//                                                                                                                    empParams.put("TransactionDescription", transactionReportDto.getTransaction_data().getTransaction_description().equals("null") ? "" : transactionReportDto.getTransaction_data().getTransaction_description());
//                                                                                                                    empParams.put("pDate", formattedPrintDate.format(printDate.getTime()));
//                                                                                                                    empParams.put("email", email);
//                                                                                                                    empParams.put("totalCredit", transactionReportDto.getTransaction_data().getCredit());
//                                                                                                                    empParams.put("totalDebit", transactionReportDto.getTransaction_data().getDebit());
//                                                                                                                    empParams.put("ip", serverRequest.exchange().getRequest().getRemoteAddress().getAddress().getHostAddress().equals("null") ? "" : serverRequest.exchange().getRequest().getRemoteAddress().getAddress().getHostAddress());
//                                                                                                                    empParams.put("os", apiCallService.getClientOS(serverRequest).equals("null") ? "" : apiCallService.getClientOS(serverRequest));
//                                                                                                                    empParams.put("browser", apiCallService.getClientBrowser(serverRequest).equals("null") ? "" : apiCallService.getClientBrowser(serverRequest));
//                                                                                                                    empParams.put("employeeData", new JRBeanCollectionDataSource(costCenterEntityList));
//
//                                                                                                                    JasperPrint empReport =
//                                                                                                                            JasperFillManager.fillReport(
//                                                                                                                                    JasperCompileManager.compileReport(
//                                                                                                                                            // path of the jasper report
//                                                                                                                                            ResourceUtils.getFile("classpath:employees-details.jrxml").getAbsolutePath()) ,
//                                                                                                                                    // dynamic parameters
//                                                                                                                                    empParams,
//                                                                                                                                    new JREmptyDataSource()
//                                                                                                                            );
//
//                                                                                                                    HttpHeaders headers = new HttpHeaders();
//                                                                                                                    //set the PDF format
//                                                                                                                    headers.setContentType(APPLICATION_OCTET_STREAM);
//                                                                                                                    headers.setContentDispositionFormData("filename", "employees-details.pdf");
//                                                                                                                    //create the report in PDF format
//                                                                                                                    return ServerResponse.ok()
//                                                                                                                            .contentType(APPLICATION_OCTET_STREAM)
//                                                                                                                            .header(String.valueOf(headers))
//                                                                                                                            .body(BodyInserters.fromValue(JasperExportManager.exportReportToPdf(empReport)));
////                                                                                                    }
//                                                                                                                } catch (Exception e) {
//                                                                                                                    return responseErrorMsg("" + e.getMessage());
//                                                                                                                }
//                                                                                                            })).switchIfEmpty(responseInfoMsg("Branch doesn't Exit"))
//                                                                                                    .onErrorResume(ex -> responseErrorMsg("Branch doesn't Exit. Please contact Developer"))
//                                                                                    )).switchIfEmpty(responseInfoMsg("Company doesn't Exit"))
//                                                                            .onErrorResume(ex -> responseErrorMsg("Company doesn't Exit. Please contact Developer"))
//                                                            ).switchIfEmpty(responseInfoMsg("Calendar Against Calendar Period doesn't Exit"))
//                                                            .onErrorResume(ex -> responseErrorMsg("Calendar Against Calendar Period doesn't Exit. Please contact Developer"))
//                                            ).switchIfEmpty(responseInfoMsg("Calendar Period doesn't Exit"))
//                                            .onErrorResume(ex -> responseErrorMsg("Calendar Period doesn't Exit. Please contact Developer"));
//                                })
//                ).switchIfEmpty(responseInfoMsg("Unauthenticated user"))
//                .onErrorResume(ex -> responseErrorMsg("Unauthenticated user. Please contact Developer"));
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

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
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
                0L,
                0L,
                messages,
                Mono.just(entity)
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
