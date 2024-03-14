package tuf.webscaf.app.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import tuf.webscaf.app.dbContext.master.dto.CompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.master.dto.TransactionStatusDto;
import tuf.webscaf.app.dbContext.master.dto.VoucherDto;
import tuf.webscaf.app.dbContext.master.dto.VoucherTypeDto;
import tuf.webscaf.app.dbContext.master.entity.BranchEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.slave.dto.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentEntity;
import tuf.webscaf.config.service.response.CustomResponse;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApiCallService {

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Autowired
    CustomResponse appresponse;

    @Value("${webclient.backend.token}")
    private String token;

    public WebClient initWebClient() {
        try {
            SslContext context = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(t -> t.sslContext(context));

            if (sslStatus.equals("enable")) {
                return WebClient.builder()
                        .clientConnector(
                                new ReactorClientHttpConnector(httpClient)
                        )
                        .build();
            } else {
                return WebClient.builder()
                        .build();
            }
        } catch (SSLException e) {
            return WebClient.builder()
                    .build();
        }
    }

    public Mono<JsonNode> getResponseData(String url) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
//                .uri(url, uriBuilder-> uriBuilder.queryParam("param", "value")
//                        .build())
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getDataWithUUID(String url, UUID uuid) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + uuid)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getVoucherResponse(String url, UUID uuid) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + uuid)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }
    public SlaveTransactionReportDto getTransactionData(JsonNode jsonNode) {
        SlaveTransactionReportDto dto = SlaveTransactionReportDto.builder().build();
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("transaction_id") != null) {
                        dto.setTransaction_id(UUID.fromString(objNode.get("transaction_id").toString().replaceAll("\"", "")));
                    }
                    if (objNode.get("id") != null) {
                        dto.setId(Long.valueOf(objNode.get("id").toString().replaceAll("\"", "")));
                    }
                    if (objNode.get("transaction_status") != null) {
                        JsonNode tNode = objNode.get("transaction_status");
                        SlaveTransactionStatusDto statusDto = SlaveTransactionStatusDto.builder().build();
                        if (!tNode.get("uuid").isNull()) {
                            statusDto.setUuid(UUID.fromString(tNode.get("uuid").toString().replaceAll("\"", "")));
                        }
                        if (tNode.get("name") != null) {
                            statusDto.setName(tNode.get("name").toString().replaceAll("\"", ""));
                        }
                        dto.setTransaction_status(statusDto);
                    }
                    if (objNode.get("voucher") != null) {
                        JsonNode vNode = objNode.get("voucher");
                        VoucherDto voDto = VoucherDto.builder().build();
                        if (vNode.get("uuid") != null) {
                            voDto.setUuid(UUID.fromString(vNode.get("uuid").toString().replaceAll("\"", "")));
                        }
                        if (vNode.get("name") != null) {
                            voDto.setName(vNode.get("name").toString().replaceAll("\"", ""));
                        }

                        if (vNode.get("voucherType") != null) {
                            VoucherTypeDto voucherType = VoucherTypeDto.builder().build();
                            JsonNode voucherTypeNode = vNode.get("voucherType");
                            if (voucherTypeNode.get("name") != null) {
                                String name = voucherTypeNode.get("name").toString().replaceAll("\"", "");
                                voucherType.setName(name);
                            }
                            if (!voucherTypeNode.get("uuid").isNull()) {
                                UUID uuid = UUID.fromString(voucherTypeNode.get("uuid").toString().replaceAll("\"", ""));
                                voucherType.setUuid(uuid);
                            }
                            if (voucherTypeNode.get("slug") != null) {
                                String slug = voucherTypeNode.get("slug").toString().replaceAll("\"", "");
                                voucherType.setSlug(slug);
                            }
                            voDto.setVoucherType(voucherType);
                        }
                        dto.setVoucher(voDto);
                    }
                    if (objNode.get("transaction_data") != null) {
                        JsonNode tdNode = objNode.get("transaction_data");
                        SlaveTransactionDataDto tdDto = SlaveTransactionDataDto.builder().build();
                        if (tdNode.get("rows") != null) {
                            JsonNode rtdNode = tdNode.get("rows");
//                            System.out.println("=============================1 "+tdNode.toString());
                            List<SlaveLedgerRowDto> rowsList = new ArrayList<>();
                            if (rtdNode.isArray()) {
//                                System.out.println("=============================2 "+tdNode.toString());
                                for (JsonNode rowNode : rtdNode) {
                                    SlaveLedgerRowDto rowDto = SlaveLedgerRowDto.builder().build();
                                    if (rowNode.get("description") != null) {
                                        String description = rowNode.get("description").toString().replaceAll("\"", "");
                                        rowDto.setDescription(description);
                                    }
                                    if (rowNode.get("cr") != null) {
                                        Double cr = Double.valueOf(rowNode.get("cr").toString().replaceAll("\"", ""));
                                        rowDto.setCr(cr);
                                    }
                                    if (rowNode.get("dr") != null) {
                                        Double dr = Double.valueOf(rowNode.get("dr").toString().replaceAll("\"", ""));
                                        rowDto.setDr(dr);
                                    }
                                    if (rowNode.get("account") != null) {
                                        SlaveLedgerRowAccountDto account = SlaveLedgerRowAccountDto.builder().build();
                                        JsonNode accountNode = rowNode.get("account");
                                        if (accountNode.get("account_code") != null) {
                                            String account_code = accountNode.get("account_code").toString().replaceAll("\"", "");
                                            account.setAccount_code(account_code);
                                        }
                                        if (accountNode.get("account_name") != null) {
                                            String account_name = accountNode.get("account_name").toString().replaceAll("\"", "");
                                            account.setAccount_name(account_name);
                                        }
                                        if (!accountNode.get("uuid").isNull()) {
                                            UUID uuid = UUID.fromString(accountNode.get("uuid").toString().replaceAll("\"", ""));
                                            account.setUuid(uuid);
                                        }
                                        rowDto.setAccount(account);
                                    }
                                    if (rowNode.get("profit_center") != null) {
                                        SlaveLedgerRowProfitCenterDto profit_center = SlaveLedgerRowProfitCenterDto.builder().build();
                                        JsonNode profitCenterNode = rowNode.get("profit_center");
                                        if (profitCenterNode.get("name") != null) {
                                            String name = profitCenterNode.get("name").toString().replaceAll("\"", "");
                                            profit_center.setName(name);
                                        }
                                        if (!profitCenterNode.get("uuid").isNull()) {
                                            UUID uuid = UUID.fromString(profitCenterNode.get("uuid").toString().replaceAll("\"", ""));
                                            profit_center.setUuid(uuid);
                                        }
                                        rowDto.setProfit_center(profit_center);
                                    }
                                    if (rowNode.get("cost_center") != null) {
                                        SlaveLedgerRowCostCenterDto cost_center = SlaveLedgerRowCostCenterDto.builder().build();
                                        JsonNode costCenterNode = rowNode.get("cost_center");
                                        if (costCenterNode.get("name") != null) {
                                            String name = costCenterNode.get("name").toString().replaceAll("\"", "");
                                                cost_center.setName(name);
                                        }
                                        if (!costCenterNode.get("uuid").isNull()) {
                                            UUID uuid = UUID.fromString(costCenterNode.get("uuid").toString().replaceAll("\"", ""));
                                            cost_center.setUuid(uuid);
                                        }
                                        rowDto.setCost_center(cost_center);
                                    }
                                    rowsList.add(rowDto);
                                }
                                tdDto.setRows(rowsList);
                            }
                        }

                        if (tdNode.get("calendar_period_uuid") != null) {
                            tdDto.setCalendar_period_uuid(UUID.fromString(tdNode.get("calendar_period_uuid").toString().replaceAll("\"", "")));
                        }
                        if (tdNode.get("credit") != null) {
                            Double credit = Double.valueOf(tdNode.get("credit").toString().replaceAll("\"", ""));
                            tdDto.setCredit(credit);
                        }
                        if (tdNode.get("debit") != null) {
                            Double debit = Double.valueOf(tdNode.get("debit").toString().replaceAll("\"", ""));
                            tdDto.setDebit(debit);
                        }
                        if (tdNode.get("date") != null) {
                            tdDto.setDate(LocalDateTime.parse(tdNode.get("date").toString().replaceAll("\"", "")));
                        }
                        if (tdNode.get("company_uuid") != null) {
                            tdDto.setCompany_uuid(UUID.fromString(tdNode.get("company_uuid").toString().replaceAll("\"", "")));
                        }
                        if (tdNode.get("branch_uuid") != null) {
                            tdDto.setBranch_uuid(UUID.fromString(tdNode.get("branch_uuid").toString().replaceAll("\"", "")));
                        }
                        if (tdNode.get("transaction_description") != null) {
                            tdDto.setTransaction_description(tdNode.get("transaction_description").toString().replaceAll("\"", ""));
                        }
                        if (tdNode.get("job_center") != null) {
                            SlaveTransactionDataJobCenterDto job_center = SlaveTransactionDataJobCenterDto.builder().build();
                            JsonNode jobCenterNode = tdNode.get("job_center");
                            if (jobCenterNode.get("name") != null) {
                                String name = jobCenterNode.get("name").toString().replaceAll("\"", "");
                                job_center.setName(name);
                            }
                            if (!jobCenterNode.get("uuid").isNull()) {
                                UUID uuid = UUID.fromString(jobCenterNode.get("uuid").toString().replaceAll("\"", ""));
                                job_center.setUuid(uuid);
                            }
                            tdDto.setJob_center(job_center);
                        }
                        dto.setTransaction_data(tdDto);
                    }
                    if (objNode.get("attachments") != null) {
                        List<String> attList = new ArrayList<>();
                       String atNode = objNode.get("attachments").toString().replaceAll("\"", "");
                       attList.add(atNode);
                       dto.setAttachments(attList);
                    }
                }
            }
        }

        return dto;
    }

    public String getClientBrowser(ServerRequest request) {
        final String browserDetails = String.valueOf(request.headers().firstHeader("User-Agent"));
        final String user = browserDetails.toLowerCase();
        String browser = "";
        if (user.contains("msie")) {
            String substring = browserDetails.substring(browserDetails.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        } else if (user.contains("safari") && user.contains("version")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Safari")).split(" ")[0]).split(
                    "/")[0] + "-" + (browserDetails.substring(
                    browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
        } else if (user.contains("opr") || user.contains("opera")) {
            if (user.contains("opera"))
                browser = (browserDetails.substring(browserDetails.indexOf("Opera")).split(" ")[0]).split(
                        "/")[0] + "-" + (browserDetails.substring(
                        browserDetails.indexOf("Version")).split(" ")[0]).split("/")[1];
            else if (user.contains("opr"))
                browser = ((browserDetails.substring(browserDetails.indexOf("OPR")).split(" ")[0]).replace("/",
                        "-")).replace(
                        "OPR", "Opera");
        } else if (user.contains("chrome")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
        } else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1) || (user.indexOf(
                "mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1) || (user.indexOf(
                "mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1)) {
            browser = "Netscape-?";
        } else if (user.contains("firefox")) {
            browser = (browserDetails.substring(browserDetails.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("rv")) {
            browser = "IE";
        } else {
            browser = "UnKnown, More-Info: " + browserDetails;
        }
        return browser;
    }
    public String getClientOS(ServerRequest request) {
        final String browserDetails = String.valueOf(request.headers().firstHeader("User-Agent"));

        final String lowerCaseBrowser = browserDetails.toLowerCase();
        if (lowerCaseBrowser.contains("windows")) {
            return "Windows";
        } else if (lowerCaseBrowser.contains("mac")) {
            return "Mac";
        } else if (lowerCaseBrowser.contains("x11")) {
            return "Unix";
        } else if (lowerCaseBrowser.contains("android")) {
            return "Android";
        } else if (lowerCaseBrowser.contains("iphone")) {
            return "IPhone";
        } else {
            return "UnKnown, More-Info: " + browserDetails;
        }
    }

    public Mono<JsonNode> getData(String url) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getDataWithQueryParams(String url, MultiValueMap<String, String> params) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url, uriBuilder -> uriBuilder.queryParams(params)
                        .build())
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<Long> getModuleId(JsonNode jsonNode) {
        Long moduleId = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("moduleId") != null) {
                        moduleId = Long.valueOf(objNode.get("moduleId").toString().replaceAll("\"", ""));
                    }
                }
            }
        }
        if (moduleId != null) {
            return Mono.just(moduleId);
        } else {
            return Mono.empty();
        }
    }

    public Mono<String> getUserName(String url) {
        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {
                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                        String email = "";
                        Integer status = Integer.valueOf(jsonNode.get("status").toString());
                        if (status.equals(200)) {
                            final JsonNode arrNode = jsonNode.get("data");
                            if (arrNode.isArray()) {
                                for (final JsonNode objNode : arrNode) {
                                    if (objNode.get("email") != null) {
                                        email = objNode.get("email").toString().replaceAll("\"", "");
                                    }
                                }
                            }
                        }
                        return Mono.just(email);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just("");
                });

    }

    public Mono<JsonNode> getMultipleRecordWithQueryParams(String url, String queryParamKey, List<String> queryParamValue) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url, uriBuilder -> uriBuilder.queryParam(queryParamKey, queryParamValue).build())
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getDataWithId(String url, Long id) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + id)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Long getTotalDataRowsWithFilter(JsonNode jsonNode) {
        Long count = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode objectNode = jsonNode.get("appResponse");
            // getting the count of records
            count = Long.valueOf(String.valueOf(objectNode.get("totalDataRowsWithFilter")).replaceAll("\"", ""));

        }

        return count;
    }

    public Mono<UUID> getUUID(JsonNode jsonNode) {
        UUID uuid = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("uuid") != null) {

                        uuid = UUID.fromString(objNode.get("uuid").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (uuid != null) {
            return Mono.just(uuid);
        } else {
            return Mono.empty();
        }
    }


    public Mono<UUID> getBranchUUID(JsonNode jsonNode) {
        UUID uuid = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("branchUUID") != null) {

                        uuid = UUID.fromString(objNode.get("branchUUID").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (uuid != null) {
            return Mono.just(uuid);
        } else {
            return Mono.empty();
        }
    }
//    public Mono<JsonNode> getDataWithIdAndQueryParameter(String url, Long id, Long companyId) {
//
//        WebClient webClient = initWebClient();
//        return webClient.get()
//                .uri(url + id + companyId)
//                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
//                .retrieve()
//                .bodyToMono(JsonNode.class).flatMap(jsonData -> {
//
//                    ObjectMapper objectMapper = new ObjectMapper();
//
//                    JsonNode jsonNode = null;
//                    try {
//                        jsonNode = objectMapper.readTree(jsonData.toString());
//                    } catch (JsonProcessingException e) {
//                        e.printStackTrace();
//                    }
//                    return Mono.just(jsonNode);
//
//                });
//    }

    public Mono<Long> checkId(JsonNode jsonNode) {
        final JsonNode arrNode = jsonNode.get("data");
        Long getId = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (objNode.get("id") != null) {
                    getId = Long.valueOf(objNode.get("id").toString());
                }
            }
        }
        if (getId != null) {
            return Mono.just(getId);
        } else {
            return Mono.empty();
        }
    }


    public Mono<UUID> checkUUID(JsonNode jsonNode) {
        final JsonNode arrNode = jsonNode.get("data");
        UUID getUUID = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (objNode.get("uuid") != null) {
                    getUUID = UUID.fromString(objNode.get("uuid").toString());
                }
            }
        }
        if (getUUID != null) {
            return Mono.just(getUUID);
        } else {
            return Mono.empty();
        }
    }


    public List<CompanyEntity> getCompanyList(JsonNode jsonNode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<CompanyEntity>>() {
        });
        List<CompanyEntity> companies = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            companies = reader.readValue(objectNode);
        }
        return companies;
    }

    public Mono<SlaveCompanyWithCompanyProfileDto> getCompanyEntity(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<SlaveCompanyWithCompanyProfileDto>() {
        });

        if (!jsonNode.get("data").isEmpty()) {
            try {
                SlaveCompanyWithCompanyProfileDto slaveCompanyWithCompanyProfileDto = reader.readValue(objectNode);
                return Mono.just(slaveCompanyWithCompanyProfileDto);
            } catch (IOException e) {
                return Mono.error(e);
            }
        } else {
            return Mono.empty();
        }
    }

    public List<BranchEntity> getBranchList(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<BranchEntity>>() {
        });
        List<BranchEntity> branches = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            try {
                branches = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return branches;
    }

    public Mono<SlaveBranchWithBranchProfileDto> getBranchEntity(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<SlaveBranchWithBranchProfileDto>() {
        });

        if (!jsonNode.get("data").isEmpty()) {
            try {
                SlaveBranchWithBranchProfileDto slaveBranchWithBranchProfileDto = reader.readValue(objectNode);
                return Mono.just(slaveBranchWithBranchProfileDto);
            } catch (IOException e) {
                return Mono.error(e);
            }
        } else {
            return Mono.empty();
        }
    }

    public List<UUID> getListUUID(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<UUID>>() {
        });
        List<UUID> listOfUuids = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            try {
                listOfUuids = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return listOfUuids;
    }

    public List<Long> getList(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<Long>>() {
        });
        List<Long> listOfIds = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            try {
                listOfIds = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return listOfIds;
    }

    public Long getCompanyId(JsonNode jsonNode) {
        final JsonNode arrNode = jsonNode.get("data");
        Long getId = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (objNode.get("companyId") != null) {
                    getId = Long.valueOf(objNode.get("companyId").toString());
                }
            }
        }
        if (getId != null) {
            return getId;
        } else {
            return null;
        }
    }
//
//    public UUID getCompanyUUID(JsonNode jsonNode) {
//        final JsonNode arrNode = jsonNode.get("data");
//
//        System.out.println(arrNode);
//
//        UUID getUUID = null;
//        if (arrNode.isArray()) {
//            for (final JsonNode objNode : arrNode) {
//                if (objNode.get("companyUUID") != null) {
//                    getUUID = UUID.fromString(objNode.get("companyUUID").toString().replaceAll("\"", ""));
//                }
//            }
//        }
//
//        if (getUUID != null) {
//            return getUUID;
//        } else {
//            return null;
//        }
//    }

    public Mono<UUID> getCompanyUUID(JsonNode jsonNode) {
        UUID uuid = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("companyUUID") != null) {
                        uuid = UUID.fromString(objNode.get("companyUUID").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (uuid != null) {
            return Mono.just(uuid);
        } else {
            return Mono.empty();
        }
    }

    public Mono<JsonNode> postDataList(MultiValueMap<String, String> formData, String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.post()
                .uri(url)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> updateDataList(MultiValueMap<String, String> formData, String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.put()
                .uri(url)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> postDataListWithId(MultiValueMap<String, String> formData, String url, Long id) {

        WebClient webClient = initWebClient();
        return webClient.post()
                .uri(url + id)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer "+token)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public SlaveDocumentEntity getDocumentEntity(JsonNode jsonNode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<SlaveDocumentEntity>() {
        });
        SlaveDocumentEntity slaveDocumentEntity = null;
        if (!jsonNode.get("data").isEmpty()) {
            slaveDocumentEntity = reader.readValue(objectNode);
        }
        return slaveDocumentEntity;
    }

    public Mono<String> checkResponse(JsonNode jsonNode) {
        String response = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode objectNode = jsonNode.get("appResponse");
            final JsonNode arrNode = objectNode.get("message");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    response = String.valueOf(objNode.get("message")).replaceAll("\"", "");
                }
            }
        }

        if (response != null) {
            return Mono.just(response);
        } else {
            return Mono.empty();
        }
    }

    public Mono<UUID> checkDocId(JsonNode jsonNode) {
        UUID docId = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("docId") != null) {
                        docId = UUID.fromString(objNode.get("docId").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (docId != null) {
            return Mono.just(docId);
        } else {
            return Mono.empty();
        }
    }

    public Boolean getStatus(JsonNode jsonNode) {
        boolean status = false;
        final JsonNode arrNode = jsonNode.get("data");

        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (objNode.get("status") != null) {
                    status = Boolean.parseBoolean(objNode.get("status").toString().replaceAll("\"", ""));
                }
            }
        }

        return status;
    }

}
