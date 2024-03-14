package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeederChartOfAccountService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;


    public Mono<ServerResponse> seedChartOfAccounts(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Asset");
        formData.add("description", "Asset Account");
        formData.add("code","1");
        formData.add("isEntryAllowed", "false");
        formData.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Liability");
        formData1.add("description", "Liability Account");
        formData1.add("code","2");
        formData1.add("isEntryAllowed", "false");
        formData1.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData1.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData1.add("status", "true");
        list.add(formData1);


        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Equity");
        formData2.add("description", "Equity Account");
        formData2.add("code","3");
        formData2.add("isEntryAllowed", "false");
        formData2.add("accountTypeUUID", "762e81a6-15b9-4073-8485-e4d24bc834ce");
        formData2.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
        formData2.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData2.add("status", "true");
        list.add(formData2);

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Income");
        formData3.add("description", "Income Account");
        formData3.add("code","4");
        formData3.add("isEntryAllowed", "false");
        formData3.add("accountTypeUUID", "3dbab946-2827-403e-af14-cafc88d02859");
        formData3.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
        formData3.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData3.add("status", "true");
        list.add(formData3);


        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Expense");
        formData4.add("description", "Expense Account");
        formData4.add("code","5");
        formData4.add("isEntryAllowed", "false");
        formData4.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData4.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
        formData4.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData4.add("status", "true");
        list.add(formData4);


        Flux<Boolean> flux = Flux.just(false);
        for(int i=0; i< list.size(); i++){
            Mono<Boolean> res = seederService.seedData(url, list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if(aBoolean){
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    }else {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Unsuccessful")
                        );
                    }

                    return appresponse.set(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.name(),
                            null,
                            "eng",
                            "token",
                            0L,
                            0L,
                            messages,
                            Mono.just(aBoolean)
                    );
                });
    }

    public Mono<ServerResponse> seedChartOfAccountsChild(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Cash");
        formData.add("description", "Cash Account");
        formData.add("code","001");
        formData.add("isEntryAllowed", "true");
        formData.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formDataa = new LinkedMultiValueMap<>();
        formDataa.add("name", "Cash in Hand");
        formDataa.add("description", "Cash in Hand Account");
        formDataa.add("code","001");
        formDataa.add("isEntryAllowed", "true");
        formDataa.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formDataa.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formDataa.add("subAccountTypeUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formDataa.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formDataa.add("status", "true");
        list.add(formDataa);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Accounts Receivable");
        formData1.add("description", "Accounts Receivable Account");
        formData1.add("code","010");
        formData1.add("isEntryAllowed", "true");
        formData1.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData1.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData1.add("status", "true");
        list.add(formData1);

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Prepaid Expenses");
        formData2.add("description", "Prepaid Expenses Account");
        formData2.add("code","020");
        formData2.add("isEntryAllowed", "true");
        formData2.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData2.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData2.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData2.add("status", "true");
        list.add(formData2);


        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Inventory");
        formData3.add("description", "Inventory Account");
        formData3.add("code","030");
        formData3.add("isEntryAllowed", "true");
        formData3.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData3.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData3.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData3.add("status", "true");
        list.add(formData3);

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Fixed Assets");
        formData4.add("description", "Fixed Assets Account");
        formData4.add("code","040");
        formData4.add("isEntryAllowed", "true");
        formData4.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData4.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData4.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData4.add("status", "true");
        list.add(formData4);


        MultiValueMap<String, String> formData5 = new LinkedMultiValueMap<>();
        formData5.add("name", "Accumulated Depreciation");
        formData5.add("description", "Accumulated Depreciation Account");
        formData5.add("code","050");
        formData5.add("isEntryAllowed", "true");
        formData5.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData5.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData5.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData5.add("status", "true");
        list.add(formData5);

        MultiValueMap<String, String> formData6 = new LinkedMultiValueMap<>();
        formData6.add("name", "Current Assets");
        formData6.add("description", "Current Assets Account");
        formData6.add("code","060");
        formData6.add("isEntryAllowed", "true");
        formData6.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData6.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData6.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData6.add("status", "true");
        list.add(formData6);

        MultiValueMap<String, String> formData7 = new LinkedMultiValueMap<>();
        formData7.add("name", "Other Assets");
        formData7.add("description", "Other Assets Account");
        formData7.add("code","070");
        formData7.add("isEntryAllowed", "true");
        formData7.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
        formData7.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData7.add("parentAccountUUID", "94e205b1-4a70-430f-b5a6-b2b983243ba9");
        formData7.add("status", "true");
        list.add(formData7);


        MultiValueMap<String, String> formData8= new LinkedMultiValueMap<>();
        formData8.add("name", "Accounts Payable");
        formData8.add("description", "Accounts Payable Account");
        formData8.add("code","001");
        formData8.add("isEntryAllowed", "true");
        formData8.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData8.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData8.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData8.add("status", "true");
        list.add(formData8);

        MultiValueMap<String, String> formData9= new LinkedMultiValueMap<>();
        formData9.add("name", "Accrued Liabilities");
        formData9.add("description", "Accrued Liabilities Account");
        formData9.add("code","010");
        formData9.add("isEntryAllowed", "true");
        formData9.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData9.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData9.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData9.add("status", "true");
        list.add(formData9);

        MultiValueMap<String, String> formData10= new LinkedMultiValueMap<>();
        formData10.add("name", "Current Liabilities");
        formData10.add("description", "Current Liabilities Account");
        formData10.add("code","020");
        formData10.add("isEntryAllowed", "true");
        formData10.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData10.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData10.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData10.add("status", "true");
        list.add(formData10);

        MultiValueMap<String, String> formData11= new LinkedMultiValueMap<>();
        formData11.add("name", "Taxes Payable");
        formData11.add("description", "Taxes Payable Account");
        formData11.add("code","030");
        formData11.add("isEntryAllowed", "true");
        formData11.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData11.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData11.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData11.add("status", "true");
        list.add(formData11);


        MultiValueMap<String, String> formData12= new LinkedMultiValueMap<>();
        formData12.add("name", "Payroll Payable");
        formData12.add("description", "Payroll Payable Account");
        formData12.add("code","040");
        formData12.add("isEntryAllowed", "true");
        formData12.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData12.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData12.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData12.add("status", "true");
        list.add(formData12);


        MultiValueMap<String, String> formData13= new LinkedMultiValueMap<>();
        formData13.add("name", "Notes Payable");
        formData13.add("description", "Notes Payable Account");
        formData13.add("code","050");
        formData13.add("isEntryAllowed", "true");
        formData13.add("accountTypeUUID", "722c6478-e459-4a64-a724-81bd90ee4f74");
        formData13.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData13.add("parentAccountUUID", "3beffd06-57a1-4fa5-8779-52c44f623987");
        formData13.add("status", "true");
        list.add(formData13);



        MultiValueMap<String, String> formData14 = new LinkedMultiValueMap<>();
        formData14.add("name", "Common Stock");
        formData14.add("description", "Common Stock Account");
        formData14.add("code","001");
        formData14.add("isEntryAllowed", "true");
        formData14.add("accountTypeUUID", "762e81a6-15b9-4073-8485-e4d24bc834ce");
        formData14.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData14.add("parentAccountUUID", "63bb3412-617c-4c6c-8449-78b098e8c063");
        formData14.add("status", "true");
        list.add(formData14);

        MultiValueMap<String, String> formData15 = new LinkedMultiValueMap<>();
        formData15.add("name", "Retained Earnings");
        formData15.add("description", "Retained Earnings Account");
        formData15.add("code","010");
        formData15.add("isEntryAllowed", "true");
        formData15.add("accountTypeUUID", "762e81a6-15b9-4073-8485-e4d24bc834ce");
        formData15.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData15.add("parentAccountUUID", "63bb3412-617c-4c6c-8449-78b098e8c063");
        formData15.add("status", "true");
        list.add(formData15);

        MultiValueMap<String, String> formData16 = new LinkedMultiValueMap<>();
        formData16.add("name", "Additional Paid in Capital");
        formData16.add("description", "Additional Paid in Capital Account");
        formData16.add("code","020");
        formData16.add("isEntryAllowed", "true");
        formData16.add("accountTypeUUID", "762e81a6-15b9-4073-8485-e4d24bc834ce");
        formData16.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData16.add("parentAccountUUID", "63bb3412-617c-4c6c-8449-78b098e8c063");
        formData16.add("status", "true");
        list.add(formData16);


        MultiValueMap<String, String> formData17 = new LinkedMultiValueMap<>();
        formData17.add("name", "Revenue");
        formData17.add("description", "Revenue Account");
        formData17.add("code","001");
        formData17.add("isEntryAllowed", "true");
        formData17.add("accountTypeUUID", "3dbab946-2827-403e-af14-cafc88d02859");
        formData17.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData17.add("parentAccountUUID", "4e9eb2ef-8d8b-495c-b28d-cf990ce73096");
        formData17.add("status", "true");
        list.add(formData17);


        MultiValueMap<String, String> formData18 = new LinkedMultiValueMap<>();
        formData18.add("name", "Sales return and allowances");
        formData18.add("description", "Sales return and allowances Account");
        formData18.add("code","010");
        formData18.add("isEntryAllowed", "true");
        formData18.add("accountTypeUUID", "3dbab946-2827-403e-af14-cafc88d02859");
        formData18.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData18.add("parentAccountUUID", "4e9eb2ef-8d8b-495c-b28d-cf990ce73096");
        formData18.add("status", "true");
        list.add(formData18);


        MultiValueMap<String, String> formData19 = new LinkedMultiValueMap<>();
        formData19.add("name", "Cost of Goods Sold");
        formData19.add("description", "Cost of Goods Sold Account");
        formData19.add("code","001");
        formData19.add("isEntryAllowed", "true");
        formData19.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData19.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData19.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData19.add("status", "true");
        list.add(formData19);


        MultiValueMap<String, String> formData20 = new LinkedMultiValueMap<>();
        formData20.add("name", "Advertising Expense");
        formData20.add("description", "Advertising Expense Account");
        formData20.add("code","010");
        formData20.add("isEntryAllowed", "true");
        formData20.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData20.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData20.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData20.add("status", "true");
        list.add(formData20);

        MultiValueMap<String, String> formData21 = new LinkedMultiValueMap<>();
        formData21.add("name", "Bank Fees");
        formData21.add("description", "Bank Fees Account");
        formData21.add("code","020");
        formData21.add("isEntryAllowed", "true");
        formData21.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData21.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData21.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData21.add("status", "true");
        list.add(formData21);

        MultiValueMap<String, String> formData22 = new LinkedMultiValueMap<>();
        formData22.add("name", "Depreciation Expense");
        formData22.add("description", "Depreciation Expense Account");
        formData22.add("code","030");
        formData22.add("isEntryAllowed", "true");
        formData22.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData22.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData22.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData22.add("status", "true");
        list.add(formData22);

        MultiValueMap<String, String> formData23 = new LinkedMultiValueMap<>();
        formData23.add("name", "Payroll Tax Expense");
        formData23.add("description", "Payroll Tax Expense Account");
        formData23.add("code","040");
        formData23.add("isEntryAllowed", "true");
        formData23.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData23.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData23.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData23.add("status", "true");
        list.add(formData23);


        MultiValueMap<String, String> formData24 = new LinkedMultiValueMap<>();
        formData24.add("name", "Rent Expense");
        formData24.add("description", "Rent Expense Account");
        formData24.add("code","050");
        formData24.add("isEntryAllowed", "true");
        formData24.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData24.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData24.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData24.add("status", "true");
        list.add(formData24);


        MultiValueMap<String, String> formData25 = new LinkedMultiValueMap<>();
        formData25.add("name", "Supplies Expense");
        formData25.add("description", "Supplies Expense Account");
        formData25.add("code","060");
        formData25.add("isEntryAllowed", "true");
        formData25.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData25.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData25.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData25.add("status", "true");
        list.add(formData25);

        MultiValueMap<String, String> formData26 = new LinkedMultiValueMap<>();
        formData26.add("name", "Utilities Expense");
        formData26.add("description", "Utilities Expense Account");
        formData26.add("code","070");
        formData26.add("isEntryAllowed", "true");
        formData26.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData26.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData26.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData26.add("status", "true");
        list.add(formData26);

        MultiValueMap<String, String> formData27 = new LinkedMultiValueMap<>();
        formData27.add("name", "Wages Expense");
        formData27.add("description", "Wages Expense Account");
        formData27.add("code","080");
        formData27.add("isEntryAllowed", "true");
        formData27.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData27.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData27.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData27.add("status", "true");
        list.add(formData27);

        MultiValueMap<String, String> formData28 = new LinkedMultiValueMap<>();
        formData28.add("name", "Other Expense");
        formData28.add("description", "Other Expense Account");
        formData28.add("code","090");
        formData28.add("isEntryAllowed", "true");
        formData28.add("accountTypeUUID", "f8917e65-86a3-4f84-84f2-ba3f0ec64870");
        formData28.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData28.add("parentAccountUUID", "938ef85a-e4ef-40cb-b88e-3a5d63558215");
        formData28.add("status", "true");
        list.add(formData28);


        Flux<Boolean> flux = Flux.just(false);
        for(int i=0; i< list.size(); i++){
            Mono<Boolean> res = seederService.seedData(url, list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if(aBoolean){
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    }else {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Unsuccessful")
                        );
                    }

                    return appresponse.set(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.name(),
                            null,
                            "eng",
                            "token",
                            0L,
                            0L,
                            messages,
                            Mono.just(aBoolean)
                    );
                });
    }

    public Mono<ServerResponse> seedChartOfAccountsChildLevel(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();


//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.add("name", "Cash in Hand");
//        formData.add("description", "Cash in Hand Account");
//        formData.add("code","001");
//        formData.add("isEntryAllowed", "true");
//        formData.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
//        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
//        formData.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
//        formData.add("parentAccountUUID", "f80f0a0a-88ff-4c43-9b1b-6196a65238b9");
//        formData.add("status", "true");
//        list.add(formData);

//        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
//        formData1.add("name", "Petty Cash");
//        formData1.add("description", "Petty Cash Account");
//        formData1.add("code","001");
//        formData1.add("isEntryAllowed", "true");
//        formData1.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
//        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
//        formData1.add("subAccountTypeUUID", "d98559f8-da76-4dba-9324-23e9c40bcaf2");
//        formData1.add("parentAccountUUID", "c2fad0d9-c13e-4cca-a23f-9daabf9fe8c1");
//        formData1.add("status", "true");
//        list.add(formData1);

//
//        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
//        formData2.add("name", "Cash in Bank");
//        formData2.add("description", "Cash in bank Account");
//        formData2.add("code","010");
//        formData2.add("isEntryAllowed", "true");
//        formData2.add("accountTypeUUID", "a7a4d79b-89fa-4a97-b6dd-1cec6e501858");
//        formData2.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
//        formData2.add("subAccountTypeUUID", "4e2711b5-bafc-4ae9-b78a-6f5de465aea8");
//        formData2.add("parentAccountUUID", "f80f0a0a-88ff-4c43-9b1b-6196a65238b9");
//        formData2.add("status", "true");
//        list.add(formData2);


        Flux<Boolean> flux = Flux.just(false);
        for(int i=0; i< list.size(); i++){
            Mono<Boolean> res = seederService.seedData(url, list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if(aBoolean){
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    }else {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Unsuccessful")
                        );
                    }

                    return appresponse.set(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.name(),
                            null,
                            "eng",
                            "token",
                            0L,
                            0L,
                            messages,
                            Mono.just(aBoolean)
                    );
                });
    }
}
