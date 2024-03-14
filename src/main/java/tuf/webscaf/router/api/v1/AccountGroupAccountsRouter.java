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
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.http.handler.AccountGroupAccountHandler;
import tuf.webscaf.app.http.validationFilters.accountGroupAccountHandler.DeleteAccountGroupAccountPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.accountGroupAccountHandler.ShowWithUuidAccountGroupAccountHandlerFilter;
import tuf.webscaf.app.http.validationFilters.accountGroupAccountHandler.StoreAccountGroupAccountPvtHandlerFilter;
import tuf.webscaf.springDocImpl.AccountGroupAccountsImpl;
import tuf.webscaf.springDocImpl.AccountGroupWithAccountDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class AccountGroupAccountsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/account-group-account/un-mapped/show/{accountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AccountGroupAccountHandler.class,
                            beanMethod = "showUnMappedAccountListAgainstAccountGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedAccountListAgainstAccountGroup",
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
                                    description = "Show Accounts that are Not Mapped Against Given Account Group UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name code or description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/account-group-account/mapped/show/{accountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AccountGroupAccountHandler.class,
                            beanMethod = "showMappedAccountListAgainstAccountGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedAccountListAgainstAccountGroup",
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
                                    description = "Show Accounts that are Mapped Against Given Account Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name code or description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/account-group-account/store/{accountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = AccountGroupAccountHandler.class,
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
                                    description = "Store Multiple Accounts for Account Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountGroupUUID")
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Accounts for a Account Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = AccountGroupWithAccountDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/account-group-account/delete/{accountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = AccountGroupAccountHandler.class,
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
                                    description = "Delete Accounts against Given Account Group UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Account for a Account Group",
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
    public RouterFunction<ServerResponse> accountGroupAccountsRoutes(AccountGroupAccountHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/account-group-account/un-mapped/show/{accountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedAccountListAgainstAccountGroup).filter(new ShowWithUuidAccountGroupAccountHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/account-group-account/mapped/show/{accountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedAccountListAgainstAccountGroup).filter(new ShowWithUuidAccountGroupAccountHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/account-group-account/store/{accountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreAccountGroupAccountPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/account-group-account/delete/{accountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteAccountGroupAccountPvtHandlerFilter()));
    }

}
