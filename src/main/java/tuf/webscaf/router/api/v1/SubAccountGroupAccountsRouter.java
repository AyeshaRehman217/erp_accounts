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
import tuf.webscaf.app.dbContext.master.entity.AccountEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountEntity;
import tuf.webscaf.app.http.handler.SubAccountGroupAccountHandler;
import tuf.webscaf.app.http.validationFilters.subAccountGroupAccountHandler.DeleteSubAccountGroupAccountHandlerFilter;
import tuf.webscaf.app.http.validationFilters.subAccountGroupAccountHandler.ShowSubAccountGroupAccountHandlerFilter;
import tuf.webscaf.app.http.validationFilters.subAccountGroupAccountHandler.StoreSubAccountGroupAccountHandlerFilter;
import tuf.webscaf.springDocImpl.AccountGroupAccountsImpl;
import tuf.webscaf.springDocImpl.AccountGroupWithAccountDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SubAccountGroupAccountsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/sub-account-group-accounts/un-mapped/show/{subAccountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SubAccountGroupAccountHandler.class,
                            beanMethod = "showUnMappedAccountListAgainstSubAccountGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedAccountListAgainstSubAccountGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAccountEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Accounts that are Not Mapped Against Given Sub Account Group UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "subAccountGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name, description, code or control code"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sub-account-group-accounts/mapped/show/{subAccountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SubAccountGroupAccountHandler.class,
                            beanMethod = "showMappedAccountListAgainstSubAccountGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedAccountListAgainstSubAccountGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAccountEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Accounts that are Mapped Against Given Sub Account Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "subAccountGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name, description, code or control code"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sub-account-group-accounts/store/{subAccountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SubAccountGroupAccountHandler.class,
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
                                                            implementation = AccountEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store Multiple Accounts for Sub Account Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "subAccountGroupUUID")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Accounts for a Sub Account Group",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = AccountGroupWithAccountDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sub-account-group-accounts/delete/{subAccountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = SubAccountGroupAccountHandler.class,
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
                                                            implementation = AccountEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Account against Given Sub Account Group UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "subAccountGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Account for a Sub Account Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = AccountGroupAccountsImpl.class)
                                            ))
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> subAccountGroupAccountsRoutes(SubAccountGroupAccountHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/sub-account-group-accounts/un-mapped/show/{subAccountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedAccountListAgainstSubAccountGroup).filter(new ShowSubAccountGroupAccountHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/sub-account-group-accounts/mapped/show/{subAccountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedAccountListAgainstSubAccountGroup).filter(new ShowSubAccountGroupAccountHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/sub-account-group-accounts/store/{subAccountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreSubAccountGroupAccountHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/sub-account-group-accounts/delete/{subAccountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteSubAccountGroupAccountHandlerFilter()));
    }

}
