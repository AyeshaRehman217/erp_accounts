package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tuf.webscaf.app.dbContext.master.dto.ListProfitAndLossStatementDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChartOfAccountDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerReportDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveLedgerSummaryReportDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTrialBalanceReportDto;
import tuf.webscaf.app.http.handler.FinancialReportsHandler;
import tuf.webscaf.app.http.validationFilters.financialReportAccountHandler.*;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class FinancialReportsRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/chart-of-accounts/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = FinancialReportsHandler.class,
                            beanMethod = "chartOfAccounts",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "chartOfAccounts",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveChartOfAccountDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/profit-and-loss-report",
//                            produces = {
//                                    MediaType.APPLICATION_JSON_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = FinancialReportsHandler.class,
//                            beanMethod = "profitAndLossStatement",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "profitAndLossStatement",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = ListProfitAndLossStatementDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Profit And Loss Statement",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/json",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/json")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = ListProfitAndLossStatementDto.class)
//                                            )),
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "startDate"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "endDate")
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/account/api/v1/chart-of-accounts-list-with-pagination/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = FinancialReportsHandler.class,
                            beanMethod = "chartOfAccountsWithPagination",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "chartOfAccountsWithPagination",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveChartOfAccountDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Chart of Accounts (parent child hierarchy) With pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p")

                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/trial-balance",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = FinancialReportsHandler.class,
//                            beanMethod = "trialBalanceReport",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "trialBalanceReport",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "Successful Operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveTrialBalanceDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Trial Balance with Running Balance Sum of Debit and Credit",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Direction property will be based on accountName,accountCode, transactionDate ,accountUUID,accountTypeUUID,accountTypeName"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "startDate"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "endDate")
//                                    }
//                            )
//                    ),

                    @RouterOperation(
                            path = "/account/api/v1/trial-balance",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = FinancialReportsHandler.class,
                            beanMethod = "trialBalance",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "trialBalance",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTrialBalanceReportDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get trial balance report",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns in accounts. Default sort is in ascending order by account name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "startDate"),
                                            @Parameter(in = ParameterIn.QUERY, name = "endDate")
                                    }
                            )
                    ),

//                    @RouterOperation(
//                            path = "/account/api/v1/ledger-report-summary/show/{accountUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = FinancialReportsHandler.class,
//                            beanMethod = "ledgerSummaryReport",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "ledgerSummaryReport",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveLedgerSummaryReportDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show List of Transaction Ledger Entry Summary Report Against the given Account UUID  Start Date and End Date",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "accountUUID"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "startDate"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "endDate")
//
//                                    }
//                            )
//                    ),

                    @RouterOperation(
                            path = "/account/api/v1/ledger-report/show/{accountUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = FinancialReportsHandler.class,
                            beanMethod = "ledgerReport",
                            operation = @Operation(
                                    operationId = "ledgerReport",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLedgerReportDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Ledger Report for given Account",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "startDate"),
                                            @Parameter(in = ParameterIn.QUERY, name = "endDate"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p")

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/child-to-parent/list/show/{accountUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = FinancialReportsHandler.class,
                            beanMethod = "childToParentAccountList",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "childToParentAccountList",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveChartOfAccountDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Fetch All Parent Accounts of Given Account",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountUUID")
                                    }
                            )
                    ), @RouterOperation(
                    path = "/account/api/v1/parent-to-child/list/show/{parentAccountUUID}",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = FinancialReportsHandler.class,
                    beanMethod = "parentToChildAccountList",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                    operation = @Operation(
                            operationId = "parentToChildAccountList",
                            security = {@SecurityRequirement(name = "bearer")},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = SlaveChartOfAccountDto.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Records not found!",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            },
                            description = "This Route Shows All Child Accounts of Given Parent Account",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "parentAccountUUID")
                            }
                    )
            ),
            }
    )

    public RouterFunction<ServerResponse> FinancialReportRoutes(FinancialReportsHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/ledger-report-summary/show/{accountUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::ledgerSummaryReport).filter(new ShowLedgerSummaryReportHandlerFilter())
        return RouterFunctions.route(GET("account/api/v1/ledger-report/show/{accountUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::ledgerReport).filter(new ShowLedgerSummaryReportHandlerFilter())
//                .and(RouterFunctions.route(GET("account/api/v1/trial-balance").and(accept(APPLICATION_FORM_URLENCODED)), handle::trialBalanceReport).filter(new ShowTrialBalanceReportHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/trial-balance").and(accept(APPLICATION_FORM_URLENCODED)), handle::trialBalance).filter(new ShowTrialBalanceReportHandlerFilter()))
//                .and(RouterFunctions.route(POST("account/api/v1/profit-and-loss-report").and(accept(APPLICATION_JSON)), handle::profitAndLossStatement))
                .and(RouterFunctions.route(GET("account/api/v1/chart-of-accounts/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::chartOfAccounts))
                .and(RouterFunctions.route(GET("account/api/v1/chart-of-accounts-list-with-pagination/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::chartOfAccountsWithPagination).filter(new IndexChartOfAccountHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/child-to-parent/list/show/{accountUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::childToParentAccountList).filter(new ShowParentAgainstChildsHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/parent-to-child/list/show/{parentAccountUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::parentToChildAccountList).filter(new ShowChildListAgainstParentHandlerFilter()));
    }


}




