//package tuf.webscaf.router.api.v1;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.enums.ParameterIn;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Encoding;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springdoc.core.annotations.RouterOperation;
//import org.springdoc.core.annotations.RouterOperations;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import tuf.webscaf.app.dbContext.master.entity.BalanceAndIncomeLineEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveBalanceAndIncomeLineEntity;
//import tuf.webscaf.app.http.handler.BalanceAndIncomeLineHandler;
//import tuf.webscaf.app.http.validationFilters.balanceAndIncomeLineHandler.ShowWithUuidBalanceAndIncomeLineHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.balanceAndIncomeLineHandler.StoreBalanceAndIncomeLineHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.balanceAndIncomeLineHandler.UpdateBalanceAndIncomeLineHandlerFilter;
//import tuf.webscaf.springDocImpl.StatusDocImpl;
//
//import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//
//@Configuration
//public class BalanceAndIncomeLinesRouter {
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/index",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "index",
//                            operation = @Operation(
//                                    operationId = "index",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveBalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "skw")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/show/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "show",
//                            operation = @Operation(
//                                    operationId = "show",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveBalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "id")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/uuid/balance-and-income-lines/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "showByUuid",
//                            operation = @Operation(
//                                    operationId = "showByUuid",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveBalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/store",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "store",
//                            operation = @Operation(
//                                    operationId = "store",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = BalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Create file",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = BalanceAndIncomeLineEntity.class)
//                                            )),
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/update/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "update",
//                            operation = @Operation(
//                                    operationId = "update",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = BalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "id"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Update file",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = BalanceAndIncomeLineEntity.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/status/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "status",
//                            operation = @Operation(
//                                    operationId = "status",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = BalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "id")
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Create file",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/balance-and-income-lines/delete/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.DELETE,
//                            beanClass = BalanceAndIncomeLineHandler.class,
//                            beanMethod = "delete",
//                            operation = @Operation(
//                                    operationId = "delete",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = BalanceAndIncomeLineEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "id"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    )
//            }
//    )
//    public RouterFunction<ServerResponse> BalanceAndIncomeLineRoutes(BalanceAndIncomeLineHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/balance-and-income-lines/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index)
//                .and(RouterFunctions.route(GET("account/api/v1/balance-and-income-lines/show/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show))
//                .and(RouterFunctions.route(GET("account/api/v1/uuid/balance-and-income-lines/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUuid).filter(new ShowWithUuidBalanceAndIncomeLineHandlerFilter()))
//                .and(RouterFunctions.route(POST("account/api/v1/balance-and-income-lines/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreBalanceAndIncomeLineHandlerFilter()))
//                .and(RouterFunctions.route(PUT("account/api/v1/balance-and-income-lines/update/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateBalanceAndIncomeLineHandlerFilter()))
//                .and(RouterFunctions.route(PUT("account/api/v1/balance-and-income-lines/status/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status))
//                .and(RouterFunctions.route(DELETE("account/api/v1/balance-and-income-lines/delete/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete));
//    }
//}
