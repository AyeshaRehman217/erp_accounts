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
import tuf.webscaf.app.dbContext.master.entity.CostCenterEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAccountGroupEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCostCenterEntity;
import tuf.webscaf.app.http.handler.AccountGroupAccountHandler;
import tuf.webscaf.app.http.handler.CostCenterGroupCostCenterHandler;
import tuf.webscaf.app.http.validationFilters.costCenterGroupCostCenterHandler.DeleteCostCenterGroupCostCenterPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.costCenterGroupCostCenterHandler.ShowCostCenterGroupCostCenterPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.costCenterGroupCostCenterHandler.StoreCostCenterGroupCostCenterPvtHandlerFilter;
import tuf.webscaf.springDocImpl.CostCenterGroupsDocImpl;
import tuf.webscaf.springDocImpl.CostCenterDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class CostCenterGroupCostCenterRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/cost-center-group-cost-center/un-mapped/show/{costCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CostCenterGroupCostCenterHandler.class,
                            beanMethod = "showUnMappedCostCentersAgainstCostCenterGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedCostCentersAgainstCostCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCostCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Cost Centers that are not Mapped with given Cost Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "costCenterGroupUUID"),
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
                            path = "/account/api/v1/cost-center-group-cost-center/mapped/show/{costCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CostCenterGroupCostCenterHandler.class,
                            beanMethod = "showMappedCostCentersAgainstCostCenterGroup",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedCostCentersAgainstCostCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCostCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Cost Centers that are Mapped with given Cost Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "costCenterGroupUUID"),
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
                            path = "/account/api/v1/cost-center-group-cost-center/store/{costCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CostCenterGroupCostCenterHandler.class,
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
                                                            implementation = CostCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create CostCenters for a CostCenter Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CostCenterGroupsDocImpl.class)
                                            )),
                                    description = "Store Cost Centers for Cost Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "costCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/cost-center-group-cost-center/delete/{costCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CostCenterGroupCostCenterHandler.class,
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
                                                            implementation = CostCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Cost Centers for Cost Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "costCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Delete Cost Center for a Cost Center Group",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CostCenterDocImpl.class)
                                            ))
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> costCenterGroupCostCenterRoutes(CostCenterGroupCostCenterHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/cost-center-group-cost-center/un-mapped/show/{costCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCostCentersAgainstCostCenterGroup).filter(new ShowCostCenterGroupCostCenterPvtHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/cost-center-group-cost-center/mapped/show/{costCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCostCentersAgainstCostCenterGroup).filter(new ShowCostCenterGroupCostCenterPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/cost-center-group-cost-center/store/{costCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCostCenterGroupCostCenterPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/cost-center-group-cost-center/delete/{costCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCostCenterGroupCostCenterPvtHandlerFilter()));
    }

}
