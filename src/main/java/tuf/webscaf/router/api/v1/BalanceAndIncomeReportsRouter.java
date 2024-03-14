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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeReportEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeReportEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowReportEntity;
import tuf.webscaf.app.http.handler.BalanceAndIncomeReportHandler;
import tuf.webscaf.app.http.validationFilters.balanceAndIncomeReportHandler.IndexBalanceAndIncomeReportFilter;
import tuf.webscaf.app.http.validationFilters.balanceAndIncomeReportHandler.ShowWithUuidBalanceAndIncomeReportHandlerFilter;
import tuf.webscaf.app.http.validationFilters.balanceAndIncomeReportHandler.StoreBalanceAndIncomeReportHandlerFilter;
import tuf.webscaf.app.http.validationFilters.balanceAndIncomeReportHandler.UpdateBalanceAndIncomeReportHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@Tag(name = "balanceAndIncomeReportHandler")
public class BalanceAndIncomeReportsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "index",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-report/findSlug",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = BalanceAndIncomeReportHandler.class,
//                            beanMethod = "findSlug",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "findSlug",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveBalanceAndIncomeReportEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "slug"),
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/show/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "show",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/uuid/balance-and-income-report/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "showByUuid",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showByUuid",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "store",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Store file",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = BalanceAndIncomeReportEntity.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/update/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "update",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "update file",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = BalanceAndIncomeReportEntity.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/status/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "status",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Status file",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/balance-and-income-report/delete/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = BalanceAndIncomeReportHandler.class,
                            beanMethod = "delete",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = BalanceAndIncomeReportEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> balanceAndIncomeReportRoutes(BalanceAndIncomeReportHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/balance-and-income-report/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexBalanceAndIncomeReportFilter())
//                .and(RouterFunctions.route(GET("account/api/v1/balance-and-income-report/findSlug").and(accept(APPLICATION_FORM_URLENCODED)), handle::findSlug))
                .and(RouterFunctions.route(GET("account/api/v1/balance-and-income-report/show/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show))
                .and(RouterFunctions.route(GET("account/api/v1/uuid/balance-and-income-report/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUuid).filter(new ShowWithUuidBalanceAndIncomeReportHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/balance-and-income-report/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreBalanceAndIncomeReportHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/balance-and-income-report/update/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateBalanceAndIncomeReportHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/balance-and-income-report/status/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status))
                .and(RouterFunctions.route(DELETE("account/api/v1/balance-and-income-report/delete/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete));
    }
}
