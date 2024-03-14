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
import tuf.webscaf.app.dbContext.master.dto.SaleReturnVoucherDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveTransactionRecordDto;
import tuf.webscaf.app.http.handler.SaleReturnVoucherHandler;
import tuf.webscaf.app.http.handler.SaleReturnVoucherHandler;
import tuf.webscaf.app.http.validationFilters.saleReturnVoucherHandler.IndexSaleReturnVoucherHandlerFilter;
import tuf.webscaf.app.http.validationFilters.saleReturnVoucherHandler.ShowSaleReturnVoucherHandlerFilter;
import tuf.webscaf.app.http.validationFilters.saleReturnVoucherHandler.StoreSaleReturnVoucherHandlerFilter;
import tuf.webscaf.app.http.validationFilters.saleReturnVoucherHandler.UpdateSaleReturnVoucherHandlerFilter;
import tuf.webscaf.app.http.validationFilters.saleReturnVoucherHandler.ShowSaleReturnVoucherHandlerFilter;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SaleReturnVouchersRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/account/api/v1/sale-return-vouchers/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SaleReturnVoucherHandler.class,
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
                            path = "/account/api/v1/sale-return-vouchers/store",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SaleReturnVoucherHandler.class,
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
                                                            implementation = SaleReturnVoucherDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Sale Return Voucher Transaction",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/json",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/json")
                                                    },
                                                    schema = @Schema(type = "object", implementation = SaleReturnVoucherDto.class)
                                            )),
                                    parameters = {
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sale-return-vouchers/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = SaleReturnVoucherHandler.class,
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
                                                            implementation = SaleReturnVoucherDto.class
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
                                            description = "Update Sale Return Voucher Transaction",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/json",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = SaleReturnVoucherDto.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sale-return-vouchers/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SaleReturnVoucherHandler.class,
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
                                    description = "Show the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sale-return-vouchers/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = SaleReturnVoucherHandler.class,
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
                                    description = "Delete the record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/account/api/v1/sale-return-vouchers/report/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_OCTET_STREAM_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SaleReturnVoucherHandler.class,
                            beanMethod = "saleReturnVoucherReport",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "saleReturnVoucherReport",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTransactionRecordDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get Sale Voucher Report",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),

                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> saleReturnVoucherRoutes(SaleReturnVoucherHandler handle) {
        return RouterFunctions.route(GET("account/api/v1/sale-return-vouchers/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexSaleReturnVoucherHandlerFilter())
                .and(RouterFunctions.route(GET("account/api/v1/sale-return-vouchers/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowSaleReturnVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("account/api/v1/sale-return-vouchers/report/{uuid}").and(accept(APPLICATION_OCTET_STREAM)), handle::saleReturnVoucherReport).filter(new ShowSaleReturnVoucherHandlerFilter()))
                .and(RouterFunctions.route(DELETE("account/api/v1/sale-return-vouchers/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowSaleReturnVoucherHandlerFilter()))
                .and(RouterFunctions.route(POST("account/api/v1/sale-return-vouchers/store").and(accept(APPLICATION_JSON)), handle::store).filter(new StoreSaleReturnVoucherHandlerFilter()))
                .and(RouterFunctions.route(PUT("account/api/v1/sale-return-vouchers/update/{uuid}").and(accept(APPLICATION_JSON)), handle::update).filter(new UpdateSaleReturnVoucherHandlerFilter()));
    }
}
