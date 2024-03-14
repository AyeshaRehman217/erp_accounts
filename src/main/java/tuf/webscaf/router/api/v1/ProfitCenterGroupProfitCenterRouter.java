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
import tuf.webscaf.app.dbContext.master.entity.ProfitCenterEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveProfitCenterEntity;
import tuf.webscaf.app.http.handler.ProfitCenterGroupProfitCenterHandler;
import tuf.webscaf.app.http.validationFilters.profitCenterGroupProfitCenterHandler.DeleteProfitCenterGroupProfitCenterPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.profitCenterGroupProfitCenterHandler.ShowProfitCenterGroupProfitCenterPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.profitCenterGroupProfitCenterHandler.StoreProfitCenterGroupProfitCenterPvtHandlerFilter;
import tuf.webscaf.springDocImpl.ProfitCenterDocImpl;
import tuf.webscaf.springDocImpl.ProfitCenterGroupsDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class ProfitCenterGroupProfitCenterRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/profit-center-group-profit-center/un-mapped/show/{profitCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ProfitCenterGroupProfitCenterHandler.class,
                            beanMethod = "showUnMappedProfitCenterAgainstProfitCenterGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedProfitCenterAgainstProfitCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveProfitCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Profit Centers that are Not Mapped for Profit Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "profitCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/profit-center-group-profit-center/mapped/show/{profitCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ProfitCenterGroupProfitCenterHandler.class,
                            beanMethod = "showMappedProfitCenterAgainstProfitCenterGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedProfitCenterAgainstProfitCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveProfitCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Profit Centers that are Mapped for Profit Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "profitCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/profit-center-group-profit-center/store/{profitCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = ProfitCenterGroupProfitCenterHandler.class,
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
                                                            implementation = ProfitCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Profit Centers for a Profit Center Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = ProfitCenterGroupsDocImpl.class)
                                            )),
                                    description = "Store Profit Centers for Profit Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "profitCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/profit-center-group-profit-center/delete/{profitCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = ProfitCenterGroupProfitCenterHandler.class,
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
                                                            implementation = ProfitCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Profit Centers for Profit Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "profitCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Profit Center for a Profit Center Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = ProfitCenterDocImpl.class)
                                            ))
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> profitCenterGroupProfitCenterRoutes(ProfitCenterGroupProfitCenterHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/profit-center-group-profit-center/un-mapped/show/{profitCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedProfitCenterAgainstProfitCenterGroup).filter(new ShowProfitCenterGroupProfitCenterPvtHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/profit-center-group-profit-center/mapped/show/{profitCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedProfitCenterAgainstProfitCenterGroup).filter(new ShowProfitCenterGroupProfitCenterPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/profit-center-group-profit-center/store/{profitCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreProfitCenterGroupProfitCenterPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/profit-center-group-profit-center/delete/{profitCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteProfitCenterGroupProfitCenterPvtHandlerFilter()));
    }

}
