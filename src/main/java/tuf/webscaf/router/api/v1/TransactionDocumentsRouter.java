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
import tuf.webscaf.app.dbContext.master.dto.TransactionDocumentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentEntity;
import tuf.webscaf.app.http.handler.TransactionDocumentHandler;
import tuf.webscaf.app.http.validationFilters.transactionDocumentPvtHandler.DeleteTransactionDocumentHandlerFilter;
import tuf.webscaf.app.http.validationFilters.transactionDocumentPvtHandler.ShowTransactionDocumentPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.transactionDocumentPvtHandler.StoreTransactionDocumentHandlerFilter;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TransactionDocumentsRouter {
    @Bean
    @RouterOperations(
            {
//                    @RouterOperation(
//                            path = "/account/api/v1/transaction-document/show/{id}",
//                            produces = {
//                                    MediaType.APPLICATION_JSON_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = TransactionDocumentHandler.class,
//                            beanMethod = "show",
//                            operation = @Operation(
//                                    operationId = "show",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveTransactionDocumentDto.class
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
                    @RouterOperation(
                            path = "/account/api/v1/transaction-documents/list/show/{transactionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionDocumentHandler.class,
                            beanMethod = "showListOfDocumentsAgainstTransaction",
                            operation = @Operation(
                                    operationId = "showListOfDocumentsAgainstTransaction",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveDocumentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the list of Document UUIDs that are mapped for given Transaction",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "transactionUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transaction-documents/un-mapped/show/{transactionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TransactionDocumentHandler.class,
                            beanMethod = "showUnMappedDocumentsAgainstTransaction",
                            operation = @Operation(
                                    operationId = "showUnMappedDocumentsAgainstTransaction",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveDocumentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the Document Records that are not mapped for given Transaction",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "transactionUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name, slug or extension"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transaction-document/store/{transactionUUID}",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = TransactionDocumentHandler.class,
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
                                                            implementation = TransactionDocumentDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Transaction With Document",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/json",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/json")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TransactionDocumentDto.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid"),
                                            @Parameter(in = ParameterIn.PATH, name = "transactionUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/transaction-document/delete/{transactionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = TransactionDocumentHandler.class,
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
                                                            implementation = SlaveDocumentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Used to Delete Documents Against Transaction UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "transactionUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "docId"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> TransactionDocumentRouter(TransactionDocumentHandler handle) {
        return RouterFunctions.route(POST("account/api/v1/transaction-document/store/{transactionUUID}").and(accept(APPLICATION_JSON)), handle::store).filter(new StoreTransactionDocumentHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/transaction-documents/un-mapped/show/{transactionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedDocumentsAgainstTransaction))
                .and(RouterFunctions.route(GET("account/api/v1/transaction-documents/list/show/{transactionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showListOfDocumentsAgainstTransaction).filter(new ShowTransactionDocumentPvtHandlerFilter()))
//                .and(RouterFunctions.route(GET("account/api/v1/transaction-document/show/{id}").and(accept(APPLICATION_JSON)), handle::show))
                .and(RouterFunctions.route(DELETE("account/api/v1/transaction-document/delete/{transactionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteTransactionDocumentHandlerFilter()));
    }
}
