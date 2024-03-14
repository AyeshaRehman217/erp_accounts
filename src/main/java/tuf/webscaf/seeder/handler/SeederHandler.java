package tuf.webscaf.seeder.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarRepository;
import tuf.webscaf.app.dbContext.slave.repository.SlaveCalendarTypesRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.seeder.service.*;

import java.util.ArrayList;
import java.util.List;

@Component
@Tag(name = "seederHandler")
public class SeederHandler {

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlaveCalendarTypesRepository slaveCalendarTypesRepository;

    @Autowired
    SlaveCalendarRepository slaveCalendarRepository;

    @Autowired
    AccountTypeService accountTypeService;

    @Autowired
    VoucherService voucherService;

    @Autowired
    AccountService accountService;

    @Autowired
    SeederJobService seederJobService;

    @Autowired
    VoucherService seederVoucherService;

    @Autowired
    SeederProfitCenterService seederProfitCenterService;

    @Autowired
    SeederCostCenterService seederCostCenterService;

    @Autowired
    SeederCalendarTypeService seederCalendarTypeService;

    @Autowired
    SeederCalendarService seederCalendarService;

    @Autowired
    SeederCalendarPeriodService seederCalendarPeriodService;

    @Autowired
    SeederTransactionStatusService seederTransactionStatusService;

    @Autowired
    SubAccountService subAccountService;

    @Autowired
    SeederChartOfAccountService seederChartOfAccountService;

    @Autowired
    SeederAccessGroupService seederAccessGroupService;

    @Autowired
    VoucherTypeCatalog voucherTypeCatalog;

    @Autowired
    SeederTransactionService seederTransactionService;


    @Value("${server.erp_account_module.uri}")
    private String accountBaseURI;

    public Mono<ServerResponse> storeAccountType(ServerRequest serverRequest) {

        Mono<String> res = accountTypeService.saveAllAccountTypes();

        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful")
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
                res
        );
    }

    public Mono<ServerResponse> storeAccount(ServerRequest serverRequest) {
        return accountService.saveAllAccounts()
                .flatMap(res -> {
                    var messages = List.of(
                            new AppResponseMessage(
                                    AppResponse.Response.SUCCESS,
                                    "Successful")
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
                            Mono.just(res)
                    );
                });
    }

    public Mono<ServerResponse> storeVoucher(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederVoucherService.seedVoucherData(accountBaseURI + "api/v1/vouchers/store");
        });
    }

    public Mono<ServerResponse> storeJob(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {

            return seederJobService.seedJobData(accountBaseURI + "api/v1/jobs/store");
        });
    }

    public Mono<ServerResponse> storeTransactionStatus(ServerRequest serverRequest) {
        return seederTransactionStatusService.seedTransactionStatusData(accountBaseURI + "api/v1/transaction-status/store");
    }

    public Mono<ServerResponse> storeProfitCenter(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {

            return seederProfitCenterService.seedProfitCenterData(accountBaseURI + "api/v1/profit-centers/store");
        });
    }

    public Mono<ServerResponse> storeCostCenter(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederCostCenterService.seedCostCenterData(accountBaseURI + "api/v1/cost-centers/store");
        });
    }

    public Mono<ServerResponse> storeCalendarType(ServerRequest serverRequest) {

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Fiscal Year");
        formData.add("description", "This is Fiscal Year Calendar Type Starting from January");
        formData.add("periods", "13");
        formData.add("status", "true");

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "calendar Year");
        formData1.add("description", "This is Calendar Year starting from January or April");
        formData1.add("periods", "25");
        formData1.add("status", "true");

        formDataList.add(formData);
        formDataList.add(formData1);

        Flux<String> fres = Flux.just("");

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<String> res = seederCalendarTypeService
                    .seedCalendarTypeData(accountBaseURI + "api/v1/calendar-type/store", formDataList.get(i));
            fres = fres.concatWith(res);
        }
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
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
                fres.last()
        );
    }

    public Mono<ServerResponse> storeCalendar(ServerRequest serverRequest) {


        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Calendar Year 2022");
        formData.add("description", "This is Yearly Calendar 2022");
        formData.add("fiscalYear", "01-01-2022 00:00:00");
        formData.add("startDate", "01-01-2022 00:00:00");
        formData.add("endDate", "31-12-2022 00:00:00");
        formData.add("calendarTypeUUID", "5d5fb265-5bf1-41c0-a4e2-077ecf6a4c04");
        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData.add("status", "true");

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Calendar Year 2023");
        formData1.add("description", "This is Yearly Calendar 2023");
        formData1.add("fiscalYear", "01-01-2023 00:00:00");
        formData1.add("startDate", "01-01-2023 00:00:00");
        formData1.add("endDate", "31-12-2023 00:00:00");
        formData1.add("calendarTypeUUID", "5d5fb265-5bf1-41c0-a4e2-077ecf6a4c04");
        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData1.add("status", "true");

        formDataList.add(formData);
        formDataList.add(formData1);

        Flux<String> fres = Flux.just("");
        int i = 0;
        for (i = 0; i < formDataList.size(); i++) {
            MultiValueMap<String, String> myFormData = formDataList.get(i);
            Mono<String> res = seederCalendarService.seedCalendarData(accountBaseURI + "api/v1/calendar/store", myFormData);
            fres = fres.concatWith(res);
        }
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
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
                fres.last()
        );
    }

    public Mono<ServerResponse> storeCalendarPeriods(ServerRequest serverRequest) {

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> period1_2022 = new LinkedMultiValueMap<>();
        period1_2022.add("prefix", "Jan");
        period1_2022.add("name", "January-2022");
        period1_2022.add("description", "This is First Calendar Period 2022");
        period1_2022.add("isOpenAuto", "true");
        period1_2022.add("adjustments", "false");
        period1_2022.add("periodNo", "1");
        period1_2022.add("quarter", "1");
        period1_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period1_2022.add("startDate", "01-01-2022 00:00:00");
        period1_2022.add("endDate", "31-01-2022 00:00:00");

        MultiValueMap<String, String> period2_2022 = new LinkedMultiValueMap<>();
        period2_2022.add("prefix", "Feb");
        period2_2022.add("name", "February-2022");
        period2_2022.add("description", "This is Second Calendar Period 2022");
        period2_2022.add("isOpenAuto", "true");
        period2_2022.add("adjustments", "false");
        period2_2022.add("periodNo", "2");
        period2_2022.add("quarter", "1");
        period2_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period2_2022.add("startDate", "01-02-2022 00:00:00");
        period2_2022.add("endDate", "28-02-2022 00:00:00");

        MultiValueMap<String, String> period3_2022 = new LinkedMultiValueMap<>();
        period3_2022.add("prefix", "Mar");
        period3_2022.add("name", "March-2022");
        period3_2022.add("description", "This is Third Calendar Period 2022");
        period3_2022.add("isOpenAuto", "true");
        period3_2022.add("adjustments", "false");
        period3_2022.add("periodNo", "3");
        period3_2022.add("quarter", "1");
        period3_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period3_2022.add("startDate", "01-03-2022 00:00:00");
        period3_2022.add("endDate", "31-03-2022 00:00:00");

        MultiValueMap<String, String> period4_2022 = new LinkedMultiValueMap<>();
        period4_2022.add("prefix", "Apr");
        period4_2022.add("name", "April-2022");
        period4_2022.add("description", "This is Fourth Calendar Period 2022");
        period4_2022.add("isOpenAuto", "true");
        period4_2022.add("adjustments", "false");
        period4_2022.add("periodNo", "4");
        period4_2022.add("quarter", "2");
        period4_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period4_2022.add("startDate", "01-04-2022 00:00:00");
        period4_2022.add("endDate", "30-04-2022 00:00:00");

        MultiValueMap<String, String> period5_2022 = new LinkedMultiValueMap<>();
        period5_2022.add("prefix", "May");
        period5_2022.add("name", "May-2022");
        period5_2022.add("description", "This is Fifth Calendar Period 2022");
        period5_2022.add("isOpenAuto", "true");
        period5_2022.add("adjustments", "false");
        period5_2022.add("periodNo", "5");
        period5_2022.add("quarter", "2");
        period5_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period5_2022.add("startDate", "01-05-2022 00:00:00");
        period5_2022.add("endDate", "31-05-2022 00:00:00");

        MultiValueMap<String, String> period6_2022 = new LinkedMultiValueMap<>();
        period6_2022.add("prefix", "Jun");
        period6_2022.add("name", "June-2022");
        period6_2022.add("description", "This is Sixth Calendar Period 2022");
        period6_2022.add("isOpenAuto", "true");
        period6_2022.add("adjustments", "false");
        period6_2022.add("periodNo", "6");
        period6_2022.add("quarter", "2");
        period6_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period6_2022.add("startDate", "01-06-2022 00:00:00");
        period6_2022.add("endDate", "30-06-2022 00:00:00");

        MultiValueMap<String, String> period7_2022 = new LinkedMultiValueMap<>();
        period7_2022.add("prefix", "Jul");
        period7_2022.add("name", "July-2022");
        period7_2022.add("description", "This is Seventh Calendar Period 2022");
        period7_2022.add("isOpenAuto", "true");
        period7_2022.add("adjustments", "false");
        period7_2022.add("periodNo", "7");
        period7_2022.add("quarter", "3");
        period7_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period7_2022.add("startDate", "01-07-2022 00:00:00");
        period7_2022.add("endDate", "31-07-2022 00:00:00");

        MultiValueMap<String, String> period8_2022 = new LinkedMultiValueMap<>();
        period8_2022.add("prefix", "Aug");
        period8_2022.add("name", "August-2022");
        period8_2022.add("description", "This is Eighth Calendar Period 2022");
        period8_2022.add("isOpenAuto", "true");
        period8_2022.add("adjustments", "false");
        period8_2022.add("periodNo", "8");
        period8_2022.add("quarter", "3");
        period8_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period8_2022.add("startDate", "01-08-2022 00:00:00");
        period8_2022.add("endDate", "31-08-2022 00:00:00");

        MultiValueMap<String, String> period9_2022 = new LinkedMultiValueMap<>();
        period9_2022.add("prefix", "Sep");
        period9_2022.add("name", "September-2022");
        period9_2022.add("description", "This is Ninth Calendar Period 2022");
        period9_2022.add("isOpenAuto", "true");
        period9_2022.add("adjustments", "false");
        period9_2022.add("periodNo", "9");
        period9_2022.add("quarter", "3");
        period9_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period9_2022.add("startDate", "01-09-2022 00:00:00");
        period9_2022.add("endDate", "30-09-2022 00:00:00");

        MultiValueMap<String, String> period10_2022 = new LinkedMultiValueMap<>();
        period10_2022.add("prefix", "Oct");
        period10_2022.add("name", "October-2022");
        period10_2022.add("description", "This is Tenth Calendar Period 2022");
        period10_2022.add("isOpenAuto", "true");
        period10_2022.add("adjustments", "false");
        period10_2022.add("periodNo", "10");
        period10_2022.add("quarter", "4");
        period10_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period10_2022.add("startDate", "01-10-2022 00:00:00");
        period10_2022.add("endDate", "31-10-2022 00:00:00");

        MultiValueMap<String, String> period11_2022 = new LinkedMultiValueMap<>();
        period11_2022.add("prefix", "Nov");
        period11_2022.add("name", "November-2022");
        period11_2022.add("description", "This is Eleventh Calendar Period 2022");
        period11_2022.add("isOpenAuto", "true");
        period11_2022.add("adjustments", "false");
        period11_2022.add("periodNo", "11");
        period11_2022.add("quarter", "4");
        period11_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period11_2022.add("startDate", "01-11-2022 00:00:00");
        period11_2022.add("endDate", "30-11-2022 00:00:00");


        MultiValueMap<String, String> period12_2022 = new LinkedMultiValueMap<>();
        period12_2022.add("prefix", "Dec");
        period12_2022.add("name", "December-2022");
        period12_2022.add("description", "This is Twelfth Calendar Period 2022");
        period12_2022.add("isOpenAuto", "true");
        period12_2022.add("adjustments", "false");
        period12_2022.add("periodNo", "12");
        period12_2022.add("quarter", "4");
        period12_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period12_2022.add("startDate", "01-12-2022 00:00:00");
        period12_2022.add("endDate", "31-12-2022 00:00:00");

        MultiValueMap<String, String> period13_2022 = new LinkedMultiValueMap<>();
        period13_2022.add("prefix", "Adj");
        period13_2022.add("name", "Adjustment-2022");
        period13_2022.add("description", "This is Adjustment Calendar Period 2022");
        period13_2022.add("isOpenAuto", "true");
        period13_2022.add("adjustments", "true");
        period13_2022.add("periodNo", "13");
        period13_2022.add("quarter", "4");
        period13_2022.add("calendarUUID", "189096b2-151d-4d10-9d10-682bf568d53b");
        period13_2022.add("startDate", "01-12-2022 00:00:00");
        period13_2022.add("endDate", "31-12-2022 00:00:00");

        formDataList.add(period1_2022);
        formDataList.add(period2_2022);
        formDataList.add(period3_2022);
        formDataList.add(period4_2022);
        formDataList.add(period5_2022);
        formDataList.add(period6_2022);
        formDataList.add(period7_2022);
        formDataList.add(period8_2022);
        formDataList.add(period9_2022);
        formDataList.add(period10_2022);
        formDataList.add(period11_2022);
        formDataList.add(period12_2022);
        formDataList.add(period13_2022);


        MultiValueMap<String, String> period1_2023 = new LinkedMultiValueMap<>();
        period1_2023.add("prefix", "Jan");
        period1_2023.add("name", "January-2023");
        period1_2023.add("description", "This is First Calendar Period 2023");
        period1_2023.add("isOpenAuto", "true");
        period1_2023.add("adjustments", "false");
        period1_2023.add("periodNo", "1");
        period1_2023.add("quarter", "1");
        period1_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period1_2023.add("startDate", "01-01-2023 00:00:00");
        period1_2023.add("endDate", "31-01-2023 00:00:00");

        MultiValueMap<String, String> period2_2023 = new LinkedMultiValueMap<>();
        period2_2023.add("prefix", "Feb");
        period2_2023.add("name", "February-2023");
        period2_2023.add("description", "This is Second Calendar Period 2023");
        period2_2023.add("isOpenAuto", "true");
        period2_2023.add("adjustments", "false");
        period2_2023.add("periodNo", "2");
        period2_2023.add("quarter", "1");
        period2_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period2_2023.add("startDate", "01-02-2023 00:00:00");
        period2_2023.add("endDate", "28-02-2023 00:00:00");

        MultiValueMap<String, String> period3_2023 = new LinkedMultiValueMap<>();
        period3_2023.add("prefix", "Mar");
        period3_2023.add("name", "March-2023");
        period3_2023.add("description", "This is Third Calendar Period 2023");
        period3_2023.add("isOpenAuto", "true");
        period3_2023.add("adjustments", "false");
        period3_2023.add("periodNo", "3");
        period3_2023.add("quarter", "1");
        period3_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period3_2023.add("startDate", "01-03-2023 00:00:00");
        period3_2023.add("endDate", "31-03-2023 00:00:00");

        MultiValueMap<String, String> period4_2023 = new LinkedMultiValueMap<>();
        period4_2023.add("prefix", "Apr");
        period4_2023.add("name", "April-2023");
        period4_2023.add("description", "This is Fourth Calendar Period 2023");
        period4_2023.add("isOpenAuto", "true");
        period4_2023.add("adjustments", "false");
        period4_2023.add("periodNo", "4");
        period4_2023.add("quarter", "2");
        period4_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period4_2023.add("startDate", "01-04-2023 00:00:00");
        period4_2023.add("endDate", "30-04-2023 00:00:00");

        MultiValueMap<String, String> period5_2023 = new LinkedMultiValueMap<>();
        period5_2023.add("prefix", "May");
        period5_2023.add("name", "May-2023");
        period5_2023.add("description", "This is Fifth Calendar Period 2023");
        period5_2023.add("isOpenAuto", "true");
        period5_2023.add("adjustments", "false");
        period5_2023.add("periodNo", "5");
        period5_2023.add("quarter", "2");
        period5_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period5_2023.add("startDate", "01-05-2023 00:00:00");
        period5_2023.add("endDate", "31-05-2023 00:00:00");

        MultiValueMap<String, String> period6_2023 = new LinkedMultiValueMap<>();
        period6_2023.add("prefix", "Jun");
        period6_2023.add("name", "June-2023");
        period6_2023.add("description", "This is Sixth Calendar Period 2023");
        period6_2023.add("isOpenAuto", "true");
        period6_2023.add("adjustments", "false");
        period6_2023.add("periodNo", "6");
        period6_2023.add("quarter", "2");
        period6_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period6_2023.add("startDate", "01-06-2023 00:00:00");
        period6_2023.add("endDate", "30-06-2023 00:00:00");

        MultiValueMap<String, String> period7_2023 = new LinkedMultiValueMap<>();
        period7_2023.add("prefix", "Jul");
        period7_2023.add("name", "July-2023");
        period7_2023.add("description", "This is Seventh Calendar Period 2023");
        period7_2023.add("isOpenAuto", "true");
        period7_2023.add("adjustments", "false");
        period7_2023.add("periodNo", "7");
        period7_2023.add("quarter", "3");
        period7_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period7_2023.add("startDate", "01-07-2023 00:00:00");
        period7_2023.add("endDate", "31-07-2023 00:00:00");

        MultiValueMap<String, String> period8_2023 = new LinkedMultiValueMap<>();
        period8_2023.add("prefix", "Aug");
        period8_2023.add("name", "August-2023");
        period8_2023.add("description", "This is Eighth Calendar Period 2023");
        period8_2023.add("isOpenAuto", "true");
        period8_2023.add("adjustments", "false");
        period8_2023.add("periodNo", "8");
        period8_2023.add("quarter", "3");
        period8_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period8_2023.add("startDate", "01-08-2023 00:00:00");
        period8_2023.add("endDate", "31-08-2023 00:00:00");

        MultiValueMap<String, String> period9_2023 = new LinkedMultiValueMap<>();
        period9_2023.add("prefix", "Sep");
        period9_2023.add("name", "September-2023");
        period9_2023.add("description", "This is Ninth Calendar Period 2023");
        period9_2023.add("isOpenAuto", "true");
        period9_2023.add("adjustments", "false");
        period9_2023.add("periodNo", "9");
        period9_2023.add("quarter", "3");
        period9_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period9_2023.add("startDate", "01-09-2023 00:00:00");
        period9_2023.add("endDate", "30-09-2023 00:00:00");

        MultiValueMap<String, String> period10_2023 = new LinkedMultiValueMap<>();
        period10_2023.add("prefix", "Oct");
        period10_2023.add("name", "October-2023");
        period10_2023.add("description", "This is Tenth Calendar Period 2023");
        period10_2023.add("isOpenAuto", "true");
        period10_2023.add("adjustments", "false");
        period10_2023.add("periodNo", "10");
        period10_2023.add("quarter", "4");
        period10_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period10_2023.add("startDate", "01-10-2023 00:00:00");
        period10_2023.add("endDate", "31-10-2023 00:00:00");

        MultiValueMap<String, String> period11_2023 = new LinkedMultiValueMap<>();
        period11_2023.add("prefix", "Nov");
        period11_2023.add("name", "November-2023");
        period11_2023.add("description", "This is Eleventh Calendar Period 2023");
        period11_2023.add("isOpenAuto", "true");
        period11_2023.add("adjustments", "false");
        period11_2023.add("periodNo", "11");
        period11_2023.add("quarter", "4");
        period11_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period11_2023.add("startDate", "01-11-2023 00:00:00");
        period11_2023.add("endDate", "30-11-2023 00:00:00");


        MultiValueMap<String, String> period12_2023 = new LinkedMultiValueMap<>();
        period12_2023.add("prefix", "Dec");
        period12_2023.add("name", "December-2023");
        period12_2023.add("description", "This is Twelfth Calendar Period 2023");
        period12_2023.add("isOpenAuto", "true");
        period12_2023.add("adjustments", "false");
        period12_2023.add("periodNo", "12");
        period12_2023.add("quarter", "4");
        period12_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period12_2023.add("startDate", "01-12-2023 00:00:00");
        period12_2023.add("endDate", "31-12-2023 00:00:00");

        MultiValueMap<String, String> period13_2023 = new LinkedMultiValueMap<>();
        period13_2023.add("prefix", "Adj");
        period13_2023.add("name", "Adjustment-2023");
        period13_2023.add("description", "This is Adjustment Calendar Period 2023");
        period13_2023.add("isOpenAuto", "true");
        period13_2023.add("adjustments", "true");
        period13_2023.add("periodNo", "13");
        period13_2023.add("quarter", "4");
        period13_2023.add("calendarUUID", "af3330cc-8db6-409e-bcf9-f13f9fb84f2e");
        period13_2023.add("startDate", "01-12-2023 00:00:00");
        period13_2023.add("endDate", "31-12-2023 00:00:00");

        formDataList.add(period1_2023);
        formDataList.add(period2_2023);
        formDataList.add(period3_2023);
        formDataList.add(period4_2023);
        formDataList.add(period5_2023);
        formDataList.add(period6_2023);
        formDataList.add(period7_2023);
        formDataList.add(period8_2023);
        formDataList.add(period9_2023);
        formDataList.add(period10_2023);
        formDataList.add(period11_2023);
        formDataList.add(period12_2023);
        formDataList.add(period13_2023);


        Flux<String> fres = Flux.just("");

        for (int i = 0; i < formDataList.size(); i++) {
            int ab = i;
            Mono<String> res;
            if (ab == 12 || ab == 25) {
                res = seederCalendarPeriodService.seedCalendarPeriodData(accountBaseURI + "api/v1/calendar-period/adjustments/store",
                        formDataList.get(i));
            } else {
                res = seederCalendarPeriodService.seedCalendarPeriodData(accountBaseURI + "api/v1/calendar-period/store",
                        formDataList.get(i));
            }
            fres = fres.concatWith(res);
        }
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
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
                fres.last()
        );
    }

    public Mono<ServerResponse> storeSubAccountType(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return subAccountService.seedSubAccountData(accountBaseURI + "api/v1/sub-account-types/store");
        });
    }

    public Mono<ServerResponse> storeChartOfAccountParent(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederChartOfAccountService.seedChartOfAccounts(accountBaseURI + "api/v1/accounts/store");
        });
    }

    public Mono<ServerResponse> storeChartOfAccountChild(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederChartOfAccountService.seedChartOfAccountsChild(accountBaseURI + "api/v1/accounts/store");
        });
    }


    public Mono<ServerResponse> storeCostCenterGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/cost-center-groups/store");
        });
    }


    public Mono<ServerResponse> storeProfitCenterGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/profit-center-groups/store");
        });
    }

    public Mono<ServerResponse> storeJobGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/job-groups/store");
        });
    }

    public Mono<ServerResponse> storeAccountGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/account-group/store");
        });
    }

    public Mono<ServerResponse> storeVoucherGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/voucher-groups/store");
        });
    }

    public Mono<ServerResponse> storeCalendarGroup(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederAccessGroupService.seedAccessGroupData(accountBaseURI + "api/v1/calendar-group/store");
        });
    }

    public Mono<ServerResponse> storeVoucherTypeCatalogue(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return voucherTypeCatalog.seedVoucherTypeCatalogue(accountBaseURI + "api/v1/voucher-type-catalogues/store");
        });
    }

    public Mono<ServerResponse> storeTransaction(ServerRequest serverRequest) {
        return serverRequest.formData().flatMap(value -> {
            return seederTransactionService.seedTransactionData(accountBaseURI + "api/v1/transactions/store");
        });
    }


//    public Mono<ServerResponse> storeCalendarAdjPeriods(ServerRequest serverRequest) {
//
//        List<List<MultiValueMap<String, String>>> formDataList = new ArrayList<>();
//        List<MultiValueMap<String, String>> formDataList1 = new ArrayList<>();
//        List<MultiValueMap<String, String>> formDataList2 = new ArrayList<>();
//
//
//        MultiValueMap<String, String> period13_2021 = new LinkedMultiValueMap<>();
//        period13_2021.add("prefix", "Adj");
//        period13_2021.add("name", "Adjustment-2021");
//        period13_2021.add("description", "This is Adjustment Calendar Period 2021");
//        period13_2021.add("isOpenAuto", "false");
//        period13_2021.add("adjustments", "true");
//        period13_2021.add("periodNo", "13");
//        period13_2021.add("quarter", "4");
//        period13_2021.add("startDate", "01-12-2021 00:00:00");
//        period13_2021.add("endDate", "31-12-2021 00:00:00");
//
//        formDataList1.add(period13_2021);
//
//
//        MultiValueMap<String, String> period13_2022 = new LinkedMultiValueMap<>();
//        period13_2022.add("prefix", "Adj");
//        period13_2022.add("name", "Adjustment-2022");
//        period13_2022.add("description", "This is Adjustment Calendar Period 2022");
//        period13_2022.add("isOpenAuto", "false");
//        period13_2022.add("adjustments", "true");
//        period13_2022.add("periodNo", "13");
//        period13_2022.add("quarter", "4");
//        period13_2022.add("startDate", "01-12-2022 00:00:00");
//        period13_2022.add("endDate", "31-12-2022 00:00:00");
//
//        formDataList2.add(period13_2022);
//
//
//        formDataList.add(formDataList1);
//        formDataList.add(formDataList2);
//
//        List<String> calendarList = new ArrayList<>();
//        calendarList.add("Year 2021");
//        calendarList.add("Year 2022");
//
//        Flux<String> fres = Flux.just("");
//
//        for (int i = 0; i < formDataList.size(); i++) {
//
//            List<MultiValueMap<String, String>> myFormDataList = formDataList.get(i);
//            String calendarName = calendarList.get(i);
//
//            for (int j = 0; j < myFormDataList.size(); j++) {
//
//                MultiValueMap<String, String> myFormData = myFormDataList.get(j);
//
//                Mono<String> res = slaveCalendarRepository
//                        .findFirstByNameAndDeletedAtIsNull(calendarName)
//                        .flatMap(slaveCalendarEntity -> {
//                            myFormData.add("calendarId", "" + slaveCalendarEntity.getId());
//                            return seederCalendarPeriodService.seedCalendarPeriodData(accountBaseURI + "api/v1/calendar-period/adjustments/store",
//                                    myFormData);
//                        });
//                fres = fres.concatWith(res);
//            }
//        }
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        "Successful"
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                fres.last()
//        );
//    }

}