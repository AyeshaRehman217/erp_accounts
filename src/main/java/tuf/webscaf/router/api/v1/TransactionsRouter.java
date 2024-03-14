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
import tuf.webscaf.app.dbContext.master.dto.TransactionRecordDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionRecordDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveReportingLedgerEntriesDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTransactionEntity;
import tuf.webscaf.app.http.handler.TransactionHandler;
import tuf.webscaf.app.http.validationFilters.transactionHandler.IndexTransactionHandlerFilter;

import tuf.webscaf.app.http.validationFilters.transactionHandler.ShowWithUuidTransactionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.transactionHandler.StoreTransactionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.transactionHandler.UpdateTransactionHandlerFilter;
import tuf.webscaf.springDocImpl.TransactionImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;

@Configuration
public class TransactionsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/transactions/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionHandler.class,
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
                                                            implementation = SlaveTransactionRecordDto.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "voucherUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/store",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = TransactionHandler.class,
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
                                                            implementation = TransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Transactions",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/json",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/json")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TransactionRecordDto.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = TransactionHandler.class,
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
                                                            implementation = TransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Update Transaction Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/json",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TransactionRecordDto.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionHandler.class,
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
                                                            implementation = SlaveTransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/uuid/transactions/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = TransactionHandler.class,
//                            beanMethod = "show",
//                            operation = @Operation(
//                                    operationId = "showByUuid",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveTransactionRecordDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show the Record for given Transaction UUID",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/uuid/transactions/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = TransactionHandler.class,
//                            beanMethod = "showByUuid",
//                            operation = @Operation(
//                                    operationId = "showByUuid",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveTransactionWithLedgerEntriesAndDocumentsDto.class
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
                    @RouterOperation(
                            path = "/account/api/v1/transactions/branch/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionHandler.class,
                            beanMethod = "getBranchUUID",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTransactionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Will be used By Config & Documents to check if Branch exists in Transactions",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/company/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionHandler.class,
                            beanMethod = "getCompanyUUID",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTransactionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Will be used By Config to check if Company exists in Transactions",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/documents/show/{documentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionHandler.class,
                            beanMethod = "getDocumentUUID",
                            operation = @Operation(
                                    operationId = "getDocumentUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTransactionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Will be used By Config & Documents to check if Documents exists in Transactions",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "documentUUID")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/account/api/v1/transactions/status/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = TransactionHandler.class,
//                            beanMethod = "status",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "status",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = TransactionEntity.class
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
//                                            description = "Updating The Status",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
//                                            ))
//                            )
//                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transactions/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = TransactionHandler.class,
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
                                                            implementation = SlaveTransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete the record for given transaction uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> TransactionRoutes(TransactionHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/transactions/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexTransactionHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/transactions/branch/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getBranchUUID).filter(new ShowWithUuidTransactionHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/transactions/company/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getCompanyUUID).filter(new ShowWithUuidTransactionHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/transactions/documents/show/{documentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getDocumentUUID))
                .and(RouterFunctions.route(GET("account/api/v1/transactions/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidTransactionHandlerFilter()))
//                .and(RouterFunctions.route(GET("account/api/v1/uuid/transactions/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUuid).filter(new ShowWithUuidTransactionHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/transactions/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowWithUuidTransactionHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/transactions/store").and(accept(APPLICATION_JSON)), handle::store).filter(new StoreTransactionHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/transactions/update/{uuid}").and(accept(APPLICATION_JSON)), handle::update).filter(new UpdateTransactionHandlerFilter()));
//                .and(RouterFunctions.route(PUT("account/api/v1/transactions/status/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status));
    }
}
