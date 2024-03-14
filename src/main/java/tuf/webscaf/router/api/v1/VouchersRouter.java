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
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;
import tuf.webscaf.app.dbContext.slave.dto.SlaveVoucherDto;
import tuf.webscaf.app.dbContext.slave.entity.*;
import tuf.webscaf.app.http.handler.VoucherHandler;
import tuf.webscaf.app.http.validationFilters.voucherHandler.*;
import tuf.webscaf.springDocImpl.*;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;

@Configuration
public class VouchersRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
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
                                                            implementation = SlaveVoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records With Pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/active/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "indexWithActiveStatus",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "indexWithActiveStatus",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get Active Records With Pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = VoucherHandler.class,
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
                                                            implementation = SlaveVoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store the Record",
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Voucher Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = VoucherEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = VoucherHandler.class,
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
                                                            implementation = SlaveVoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Update the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Update Voucher Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = VoucherEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherEntity.class
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
                            path = "/account/api/v1/vouchers/job-group/mapped/show/{jobGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstJobGroup",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstJobGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Job Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "jobGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/account-group/mapped/show/{accountGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstAccountGroup",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstAccountGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Account Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "accountGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/cost-center-group/mapped/show/{costCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstCostCenterGroup",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstCostCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Cost Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "costCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/profit-center-group/mapped/show/{profitCenterGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstProfitCenterGroup",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstProfitCenterGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Profit Center Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "profitCenterGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/calendar-group/mapped/show/{calendarGroupUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstCalendarGroup",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstCalendarGroup",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Calendar Group",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "calendarGroupUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/company/mapped/show/{companyUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstCompany",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstCompany",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Company",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "companyUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/branch/mapped/show/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVouchersAgainstBranch",
                            operation = @Operation(
                                    operationId = "showVouchersAgainstBranch",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Mapped Vouchers for Given Branch",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/vouchers/document-nature-group/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = VoucherHandler.class,
                            beanMethod = "showVoucherWithDocumentNatureGroups",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showVoucherWithDocumentNatureGroups",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveDocumentNatureGroupEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name or description"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/vouchers/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = VoucherHandler.class,
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
                                                            implementation = VoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Update the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid"),
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    },
                                    requestBody = @RequestBody(
                                            description = "Update the Status",
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
                            path = "/account/api/v1/vouchers/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = VoucherHandler.class,
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
                                                            implementation = VoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Update the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> voucherRoutes(VoucherHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/vouchers/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexVoucherHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/active/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::indexWithActiveStatus).filter(new IndexVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/job-group/mapped/show/{jobGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstJobGroup).filter(new ShowMappedRecordsAgainstJobGroupHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/cost-center-group/mapped/show/{costCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstCostCenterGroup).filter(new ShowMappedRecordsAgainstCostCenterGroupHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/profit-center-group/mapped/show/{profitCenterGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstProfitCenterGroup).filter(new ShowMappedRecordsAgainstProfitGroupHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/calendar-group/mapped/show/{calendarGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstCalendarGroup).filter(new ShowMappedRecordsAgainstCalendarGroupHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/company/mapped/show/{companyUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstCompany).filter(new ShowMappedRecordsAgainstCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/branch/mapped/show/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstBranch).filter(new ShowMappedRecordsAgainstBranchHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/account-group/mapped/show/{accountGroupUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVouchersAgainstAccountGroup).filter(new ShowMappedRecordsAgainstAccountGroupHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/vouchers/document-nature-group/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showVoucherWithDocumentNatureGroups).filter(new ShowMappedRecordsAgainstVoucherHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/vouchers/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowVoucherHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/vouchers/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreVoucherHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/vouchers/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateVoucherHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/vouchers/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowVoucherHandlerFilter()));
    }
}
