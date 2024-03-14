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
//import tuf.webscaf.app.dbContext.master.entity.CashFlowAdjustmentEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCashFlowAdjustmentEntity;
//import tuf.webscaf.app.http.handler.CashFlowAdjustmentHandler;
//import tuf.webscaf.app.http.validationFilters.cashFlowAdjustmentHandler.ShowWithUuidCashFlowAdjustmentHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.cashFlowAdjustmentHandler.StoreCashFlowAdjustmentHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.cashFlowAdjustmentHandler.UpdateCashFlowAdjustmentHandlerFilter;
//import tuf.webscaf.springDocImpl.StatusDocImpl;
//
//import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//
//@Configuration
//public class CashFlowAdjustmentsRouter {
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/account/api/v1/cash-flow-adjustments/index",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "index",
//                            operation = @Operation(
//                                    operationId = "index",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCashFlowAdjustmentEntity.class
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
//                            path = "/account/api/v1/cash-flow-adjustments/show/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "show",
//                            operation = @Operation(
//                                    operationId = "show",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCashFlowAdjustmentEntity.class
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
//                            path = "/account/api/v1/uuid/cash-flow-adjustments/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "showByUuid",
//                            operation = @Operation(
//                                    operationId = "showByUuid",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCashFlowAdjustmentEntity.class
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
//                            path = "/account/api/v1/cash-flow-adjustments/store",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "store",
//                            operation = @Operation(
//                                    operationId = "store",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CashFlowAdjustmentEntity.class
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
//                                                    schema = @Schema(type = "object", implementation = CashFlowAdjustmentEntity.class)
//                                            )),
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/cash-flow-adjustments/update/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "update",
//                            operation = @Operation(
//                                    operationId = "update",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CashFlowAdjustmentEntity.class
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
//                                            description = "Create file",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = CashFlowAdjustmentEntity.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/cash-flow-adjustments/status/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "status",
//                            operation = @Operation(
//                                    operationId = "status",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CashFlowAdjustmentEntity.class
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
//                            path = "/account/api/v1/cash-flow-adjustments/delete/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.DELETE,
//                            beanClass = CashFlowAdjustmentHandler.class,
//                            beanMethod = "delete",
//                            operation = @Operation(
//                                    operationId = "delete",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = CashFlowAdjustmentEntity.class
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
//    public RouterFunction<ServerResponse> CashFlowAdjustmentRoutes(CashFlowAdjustmentHandler handle) {
//        return RouterFunctions.route(GET("account/api/v1/cash-flow-adjustments/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index)
//                .and(RouterFunctions.route(GET("account/api/v1/cash-flow-adjustments/show/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show))
//                .and(RouterFunctions.route(GET("account/api/v1/uuid/cash-flow-adjustments/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUuid).filter(new ShowWithUuidCashFlowAdjustmentHandlerFilter()))
//                .and(RouterFunctions.route(POST("account/api/v1/cash-flow-adjustments/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCashFlowAdjustmentHandlerFilter()))
//                .and(RouterFunctions.route(PUT("account/api/v1/cash-flow-adjustments/update/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateCashFlowAdjustmentHandlerFilter()))
//                .and(RouterFunctions.route(PUT("account/api/v1/cash-flow-adjustments/status/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status))
//                .and(RouterFunctions.route(DELETE("account/api/v1/cash-flow-adjustments/delete/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete));
//    }
//}
