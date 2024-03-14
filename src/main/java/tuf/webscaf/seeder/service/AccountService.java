package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.master.repository.AccountRepository;
import tuf.webscaf.app.dbContext.master.repository.AccountTypesRepository;
import tuf.webscaf.seeder.model.Account;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountTypesRepository accountTypesRepository;

    @Autowired
    SeederService seederService;

    @Value("${server.erp_account_module.uri}")
    private String accountBaseURI;

    @Value("${server.zone}")
    private String zone;

    public Mono<Boolean> saveAllAccounts() {
        ArrayList<MultiValueMap<String, String>> data = getData();
        Flux<Boolean> fluxRes = Flux.just(false);
        for (int i = 0; i < data.size(); i++) {
            MultiValueMap<String, String> formData = data.get(i);
            Mono<Boolean> res = seederService.seedData(accountBaseURI + "api/v1/accounts/store", formData);
            fluxRes = fluxRes.concatWith(res);
        }
        return fluxRes.last();
    }

//    private Mono<String> checkAccount(Account account, String companyId, String branchId) {
//        return accountRepository.findFirstByNameAndDeletedAtIsNull(account.getName())
//                .flatMap(accountEntity -> {
//                    return Mono.just("");
//                }).switchIfEmpty(saveAccount(account, companyId, branchId));
//    }
//
//    private Mono<String> saveAccount(Account account, String companyId, String branchId) {
//        return accountTypesRepository.findFirstByNameAndDeletedAtIsNull(account.getAccountType())
//                .flatMap(accountTypeEntity -> {
//
//                    AccountEntity accountEntity = AccountEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(account.getName())
//                            .description(account.getDescription())
//                            .code(account.getCode())
//                            .accountTypeUUID(accountTypeEntity.getUuid())
//                            .companyUUID(UUID.fromString(companyId))
//                            .branchUUID(UUID.fromString(branchId))
//                            .status(true)
//                            .isEntryAllowed(true)
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString("4d54bb9c-37c9-49c3-9566-1f77b554d218"))
//                            .build();
//
//                    return accountRepository.save(accountEntity)
//                            .flatMap(accountEntity1 -> {
//                                return Mono.just("");
//                            });
//                });
//    }


    private ArrayList<MultiValueMap<String, String>> getData() {

        Account ac1 = Account.builder()
                .name("Checking Account")
                .description("Checking Account")
                .code("101")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")
                .build();

        Account ac2 = Account.builder()
                .name("Savings Account")
                .description("Savings Account")
                .code("102")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac3 = Account.builder()
                .name("Accounts Receivable")
                .description("Accounts Receivable")
                .code("120")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac4 = Account.builder()
                .name("Prepayments")
                .description("Prepayments")
                .code("130")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac5 = Account.builder()
                .name("Inventory")
                .description("Inventory")
                .code("140")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac6 = Account.builder()
                .name("Office Equipment")
                .description("Office Equipment")
                .code("150")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac7 = Account.builder()
                .name("Less Accumulated Depreciation on Office Equipment")
                .description("Less Accumulated Depreciation on Office Equipment")
                .code("151")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac8 = Account.builder()
                .name("Computer Equipment")
                .description("Computer Equipment")
                .code("160")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac9 = Account.builder()
                .name("Less Accumulated Depreciation on Computer Equipment")
                .description("Less Accumulated Depreciation on Computer Equipment")
                .code("161")
                .accountTypeUUID("a7a4d79b-89fa-4a97-b6dd-1cec6e501858")


                .build();

        Account ac10 = Account.builder()
                .name("Accounts Payable")
                .description("Accounts Payable")
                .code("200")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac11 = Account.builder()
                .name("Accruals")
                .description("Accruals")
                .code("205")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac12 = Account.builder()
                .name("Unpaid Expense Claims")
                .description("Unpaid Expense Claims")
                .code("210")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac13 = Account.builder()
                .name("Wages Payable")
                .description("Wages Payable")
                .code("215")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")
                .build();

        Account ac14 = Account.builder()
                .name("Wages Payable - Payroll")
                .description("Wages Payable - Payroll")
                .code("216")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac15 = Account.builder()
                .name("Sales Tax")
                .description("Sales Tax")
                .code("220")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac16 = Account.builder()
                .name("Employee Tax Payable")
                .description("Employee Tax Payable")
                .code("230")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac17 = Account.builder()
                .name("Federal Tax withholding")
                .description("Federal Tax withholding")
                .code("231")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac18 = Account.builder()
                .name("State Tax withholding")
                .description("State Tax withholding")
                .code("232")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac19 = Account.builder()
                .name("Employee Benefits Payable")
                .description("Employee Benefits Payable")
                .code("233")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac20 = Account.builder()
                .name("Employee Deductions Payable")
                .description("Employee Deductions Payable")
                .code("234")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac21 = Account.builder()
                .name("PTO Payable")
                .description("PTO Payable")
                .code("235")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac22 = Account.builder()
                .name("Income Tax Payable")
                .description("Income Tax Payable")
                .code("240")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac23 = Account.builder()
                .name("Suspense")
                .description("Suspense")
                .code("250")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac24 = Account.builder()
                .name("Historical Adjustment")
                .description("Historical Adjustment")
                .code("255")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac25 = Account.builder()
                .name("Rounding")
                .description("Rounding")
                .code("260")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac26 = Account.builder()
                .name("Tracking Transfers")
                .description("Tracking Transfers")
                .code("265")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();
        Account ac27 = Account.builder()
                .name("Loan")
                .description("Loan")
                .code("290")
                .accountTypeUUID("722c6478-e459-4a64-a724-81bd90ee4f74")


                .build();

        Account ac28 = Account.builder()
                .name("Owners Contribution")
                .description("Owners Contribution")
                .code("300")
                .accountTypeUUID("762e81a6-15b9-4073-8485-e4d24bc834ce")


                .build();

        Account ac29 = Account.builder()
                .name("Owners Draw")
                .description("Owners Draw")
                .code("310")
                .accountTypeUUID("762e81a6-15b9-4073-8485-e4d24bc834ce")


                .build();

        Account ac30 = Account.builder()
                .name("Retained Earnings")
                .description("Retained Earnings")
                .code("320")
                .accountTypeUUID("762e81a6-15b9-4073-8485-e4d24bc834ce")


                .build();

        Account ac31 = Account.builder()
                .name("Common Stock")
                .description("Common Stock")
                .code("330")
                .accountTypeUUID("762e81a6-15b9-4073-8485-e4d24bc834ce")


                .build();


        Account ac32 = Account.builder()
                .name("Sales")
                .description("Sales")
                .code("400")
                .accountTypeUUID("3dbab946-2827-403e-af14-cafc88d02859")


                .build();

        Account ac33 = Account.builder()
                .name("Other Revenue")
                .description("Other Revenue")
                .code("460")
                .accountTypeUUID("3dbab946-2827-403e-af14-cafc88d02859")


                .build();

        Account ac34 = Account.builder()
                .name("Interest Income")
                .description("Interest Income")
                .code("470")
                .accountTypeUUID("3dbab946-2827-403e-af14-cafc88d02859")


                .build();
        Account ac35 = Account.builder()
                .name("Refunds")
                .description("Refunds")
                .code("480")
                .accountTypeUUID("3dbab946-2827-403e-af14-cafc88d02859")


                .build();

        Account ac36 = Account.builder()
                .name("Cost of Goods Sold")
                .description("Cost of Goods Sold")
                .code("500")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac37 = Account.builder()
                .name("Advertising")
                .description("Advertising")
                .code("600")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac38 = Account.builder()
                .name("Bank Service Charges")
                .description("Bank Service Charges")
                .code("604")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac39 = Account.builder()
                .name("Janitorial Expense")
                .description("Janitorial Expense")
                .code("608")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac40 = Account.builder()
                .name("Consulting and Accounting")
                .description("Consulting and Accounting")
                .code("612")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac41 = Account.builder()
                .name("Entertainment")
                .description("Entertainment")
                .code("620")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac42 = Account.builder()
                .name("Postage and Delivery")
                .description("Postage and Delivery")
                .code("624")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac43 = Account.builder()
                .name("General Expense")
                .description("General Expense")
                .code("628")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac44 = Account.builder()
                .name("Insurance")
                .description("Insurance")
                .code("632")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac45 = Account.builder()
                .name("Legal Expense")
                .description("Legal Expense")
                .code("640")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();

        Account ac46 = Account.builder()
                .name("Utilities")
                .description("Utilities")
                .code("644")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac47 = Account.builder()
                .name("Automobile Expense")
                .description("Automobile Expense")
                .code("648")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac48 = Account.builder()
                .name("Office Expense")
                .description("Office Expense")
                .code("652")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac49 = Account.builder()
                .name("Printing and Stationery")
                .description("Printing and Stationery")
                .code("656")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac50 = Account.builder()
                .name("Rent")
                .description("Rent")
                .code("660")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac51 = Account.builder()
                .name("Repairs and Maintenance")
                .description("Repairs and Maintenance")
                .code("664")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac52 = Account.builder()
                .name("Wages and Salaries")
                .description("Wages and Salaries")
                .code("668")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac53 = Account.builder()
                .name("Wages and Salaries - California")
                .description("Wages and Salaries - California")
                .code("669")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac54 = Account.builder()
                .name("Payroll Tax Expense")
                .description("Payroll Tax Expense")
                .code("672")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")


                .build();
        Account ac55 = Account.builder()
                .name("Dues and Subscriptions")
                .description("Dues and Subscriptions")
                .code("676")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac56 = Account.builder()
                .name("Telephone and Internet")
                .description("Telephone and Internet")
                .code("680")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac57 = Account.builder()
                .name("Travel")
                .description("Travel")
                .code("684")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac58 = Account.builder()
                .name("Bad Debts")
                .description("Bad Debts")
                .code("690")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac59 = Account.builder()
                .name("Depreciation")
                .description("Depreciation")
                .code("700")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac60 = Account.builder()
                .name("Income Tax Expense")
                .description("Income Tax Expense")
                .code("710")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac61 = Account.builder()
                .name("Federal Tax Expense")
                .description("Federal Tax Expense")
                .code("720")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac62 = Account.builder()
                .name("State Tax Expense")
                .description("State Tax Expense")
                .code("721")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac63 = Account.builder()
                .name("Employee Benefits Expense")
                .description("Employee Benefits Expense")
                .code("722")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac64 = Account.builder()
                .name("PTO Expense")
                .description("PTO Expense")
                .code("723")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac65 = Account.builder()
                .name("Interest Expense")
                .description("Interest Expense")
                .code("800")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac66 = Account.builder()
                .name("Bank Revaluations")
                .description("Bank Revaluations")
                .code("810")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac67 = Account.builder()
                .name("Unrealized Currency Gains")
                .description("Unrealized Currency Gains")
                .code("815")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac68 = Account.builder()
                .name("Realized Currency Gains")
                .description("Realized Currency Gains")
                .code("820")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac69 = Account.builder()
                .name("Revenue Received in Advance")
                .description("Revenue Received in Advance")
                .code("835")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        Account ac70 = Account.builder()
                .name("Clearing Account")
                .description("Clearing Account")
                .code("855")
                .accountTypeUUID("f8917e65-86a3-4f84-84f2-ba3f0ec64870")
                .build();

        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(ac1);
        accounts.add(ac2);
        accounts.add(ac3);
        accounts.add(ac4);
        accounts.add(ac5);
        accounts.add(ac6);
        accounts.add(ac7);
        accounts.add(ac8);
        accounts.add(ac9);
        accounts.add(ac10);
        accounts.add(ac11);
        accounts.add(ac12);
        accounts.add(ac13);
        accounts.add(ac14);
        accounts.add(ac15);
        accounts.add(ac16);
        accounts.add(ac17);
        accounts.add(ac18);
        accounts.add(ac19);
        accounts.add(ac20);
        accounts.add(ac21);
        accounts.add(ac22);
        accounts.add(ac23);
        accounts.add(ac24);
        accounts.add(ac25);
        accounts.add(ac26);
        accounts.add(ac27);
        accounts.add(ac28);
        accounts.add(ac29);
        accounts.add(ac30);
        accounts.add(ac31);
        accounts.add(ac32);
        accounts.add(ac33);
        accounts.add(ac34);
        accounts.add(ac35);
        accounts.add(ac36);
        accounts.add(ac37);
        accounts.add(ac38);
        accounts.add(ac39);
        accounts.add(ac40);
        accounts.add(ac41);
        accounts.add(ac42);
        accounts.add(ac43);
        accounts.add(ac44);
        accounts.add(ac45);
        accounts.add(ac46);
        accounts.add(ac47);
        accounts.add(ac48);
        accounts.add(ac49);
        accounts.add(ac50);
        accounts.add(ac51);
        accounts.add(ac52);
        accounts.add(ac53);
        accounts.add(ac54);
        accounts.add(ac55);
        accounts.add(ac56);
        accounts.add(ac57);
        accounts.add(ac58);
        accounts.add(ac59);
        accounts.add(ac60);
        accounts.add(ac61);
        accounts.add(ac62);
        accounts.add(ac63);
        accounts.add(ac64);
        accounts.add(ac65);
        accounts.add(ac66);
        accounts.add(ac67);
        accounts.add(ac68);
        accounts.add(ac69);
        accounts.add(ac70);

        ArrayList<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        for (int i = 0; i < accounts.size(); i++) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("name", accounts.get(i).getName());
            formData.add("code", accounts.get(i).getCode());
            formData.add("description", accounts.get(i).getDescription());
            formData.add("accountTypeUUID", accounts.get(i).getAccountTypeUUID());
            formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
            formData.add("branchUUID", "a887756d-a6f2-47ce-8b85-1c0e1b392756");
            formData.add("status", "true");
            formData.add("isEntryAllowed", "true");
            formDataList.add(formData);
        }
        return formDataList;
    }
}