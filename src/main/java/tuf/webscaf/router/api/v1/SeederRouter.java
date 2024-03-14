package tuf.webscaf.router.api.v1;


import io.swagger.v3.oas.annotations.Operation;
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
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionRecordDto;
import tuf.webscaf.seeder.handler.SeederHandler;
import tuf.webscaf.seeder.model.AccountDataDocImpl;
import tuf.webscaf.seeder.model.CalendarDataDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SeederRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/seeder/account-type/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeAccountType",
                            operation = @Operation(
                                    operationId = "storeAccountType",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = AccountTypeEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/account/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeAccount",
                            operation = @Operation(
                                    operationId = "storeAccount",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = AccountEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/voucher/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeVoucher",
                            operation = @Operation(
                                    operationId = "storeVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = VoucherEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/job/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeJob",
                            operation = @Operation(
                                    operationId = "storeJob",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = JobEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/profit-centers/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeProfitCenter",
                            operation = @Operation(
                                    operationId = "storeProfitCenter",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = ProfitCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/cost-centers/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCostCenter",
                            operation = @Operation(
                                    operationId = "storeCostCenter",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CostCenterEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/calendar/type/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendarType",
                            operation = @Operation(
                                    operationId = "storeCalendarType",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarTypesEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/calendar/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendar",
                            operation = @Operation(
                                    operationId = "storeCalendar",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/calendar/period/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendarPeriods",
                            operation = @Operation(
                                    operationId = "storeCalendarPeriods",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarPeriodEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/transaction-status/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeTransactionStatus",
                            operation = @Operation(
                                    operationId = "storeTransactionStatus",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarPeriodEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/sub-account-types/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeSubAccountType",
                            operation = @Operation(
                                    operationId = "storeSubAccountType",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarPeriodEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/account-parent/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeChartOfAccountParent",
                            operation = @Operation(
                                    operationId = "storeChartOfAccountParent",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarPeriodEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/account-child/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeChartOfAccountChild",
                    operation = @Operation(
                            operationId = "storeChartOfAccountChild",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/cost-center-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeCostCenterGroup",
                    operation = @Operation(
                            operationId = "storeCostCenterGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/profit-center-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeProfitCenterGroup",
                    operation = @Operation(
                            operationId = "storeProfitCenterGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/job-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeJobGroup",
                    operation = @Operation(
                            operationId = "storeJobGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/account-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeAccountGroup",
                    operation = @Operation(
                            operationId = "storeAccountGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/voucher-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeVoucherGroup",
                    operation = @Operation(
                            operationId = "storeVoucherGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            )
                    , @RouterOperation(
                    path = "/account/api/v1/seeder/calendar-group/store",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.POST,
                    beanClass = SeederHandler.class,
                    beanMethod = "storeCalendarGroup",
                    operation = @Operation(
                            operationId = "storeCalendarGroup",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful",
                                            content = @Content(schema = @Schema(
                                                    implementation = CalendarPeriodEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            }
                    )
            ),
                    @RouterOperation(
                            path = "/account/api/v1/seeder/voucher-type-catalogues/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeVoucherTypeCatalogue",
                            operation = @Operation(
                                    operationId = "storeVoucherTypeCatalogue",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = CalendarPeriodEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/account/api/v1/seeder/transactions/store",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeTransaction",
                            operation = @Operation(
                                    operationId = "storeTransaction",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> SeederRoutes(SeederHandler handle) {
        return RouterFunctions.route(POST("/account/api/v1/seeder/account-type/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeAccountType)
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/account/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeAccount))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/voucher/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeVoucher))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/job/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeJob))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/profit-centers/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeProfitCenter))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/cost-centers/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCostCenter))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/calendar/type/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendarType))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/calendar/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendar))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/calendar/period/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendarPeriods))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/transaction-status/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeTransactionStatus))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/sub-account-types/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeSubAccountType))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/account-parent/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeChartOfAccountParent))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/account-child/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeChartOfAccountChild))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/cost-center-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCostCenterGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/profit-center-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeProfitCenterGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/job-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeJobGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/account-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeAccountGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/voucher-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeVoucherGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/calendar-group/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendarGroup))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/voucher-type-catalogues/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeVoucherTypeCatalogue))
                .and(RouterFunctions.route(POST("/account/api/v1/seeder/transactions/store").and(accept(APPLICATION_JSON)), handle::storeTransaction));
    }
}