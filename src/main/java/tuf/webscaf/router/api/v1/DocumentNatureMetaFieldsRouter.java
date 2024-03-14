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
import tuf.webscaf.app.dbContext.master.entity.DocumentNatureMetaFieldEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocumentNatureMetaFieldEntity;
import tuf.webscaf.app.http.handler.DocumentNatureMetaFieldHandler;
//import tuf.webscaf.app.http.validationFilters.DocumentNatureMetaFieldHandler.StoreDocumentNatureMetaFieldHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.DocumentNatureMetaFieldHandler.UpdateDocumentNatureMetaFieldHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DocumentNatureMetaFieldsRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/doc-nature-meta-fields/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = SlaveDocumentNatureMetaFieldEntity.class
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
                    @RouterOperation(
                            path = "/account/api/v1/doc-nature-meta-fields/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = DocumentNatureMetaFieldEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Document Nature",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = DocumentNatureMetaFieldEntity.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/doc-nature-meta-fields/update/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = DocumentNatureMetaFieldEntity.class
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
                                            description = "Update file",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = DocumentNatureMetaFieldEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/doc-nature-meta-fields/show/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = SlaveDocumentNatureMetaFieldEntity.class
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
                            path = "/account/api/v1/doc-nature-meta-fields/status/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = DocumentNatureMetaFieldEntity.class
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
                                            description = "Updating The Status",
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
                            path = "/account/api/v1/doc-nature-meta-fields/delete/{id}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = DocumentNatureMetaFieldHandler.class,
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
                                                            implementation = DocumentNatureMetaFieldEntity.class
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

    public RouterFunction<ServerResponse> DocumentNatureMetaFieldRoutes(DocumentNatureMetaFieldHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/doc-nature-meta-fields/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index)
                .and(RouterFunctions.route(GET("account/api/v1/doc-nature-meta-fields/show/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show))
                .and(RouterFunctions.route(DELETE("account/api/v1/doc-nature-meta-fields/delete/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete))
                .and(RouterFunctions.route(POST("account/api/v1/doc-nature-meta-fields/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store))
                .and(RouterFunctions.route(PUT("account/api/v1/doc-nature-meta-fields/update/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update))
                .and(RouterFunctions.route(PUT("account/api/v1/doc-nature-meta-fields/status/{id}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status));
    }
}
