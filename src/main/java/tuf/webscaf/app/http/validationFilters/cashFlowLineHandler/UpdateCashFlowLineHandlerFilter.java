package tuf.webscaf.app.http.validationFilters.cashFlowLineHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;


public class UpdateCashFlowLineHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.containsKey("name")) {
                        if (value.getFirst("name").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Name Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("name").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Name!"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Name Field Required"
                                )
                        );
                    }

                    if (value.getFirst("cashFlowReportId").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Cash Flow Report Id is Required"
                                )
                        );
                    }

                    if (!value.getFirst("cashFlowReportId").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Cash Flow Report Id will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("visiblePositionIndex").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Visible Position Index Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("visiblePositionIndex").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Visible Position Index will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("printedNo").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Printed No Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("printedNo")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In Printed No Field!"
                                )
                        );
                    }

                    if (value.getFirst("lineTextShow").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "line Text Show Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("lineTextShow")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In Line Text Show Field!"
                                )
                        );
                    }

                    if (value.getFirst("totalOfLinePositions").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Total of line Positions Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("totalOfLinePositions")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In Total of Line Position Field!"
                                )
                        );
                    }

                    if (value.getFirst("linePosition").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Line Positions Field is Required"
                                )
                        );
                    }

                    if (value.getFirst("lineValueType").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Line Value Type Field is Required"
                                )
                        );
                    }

                    if (value.getFirst("lineIntendentation").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Line Intendentation Field is Required"
                                )
                        );
                    }

                    if (messages.isEmpty() != true) {
                        CustomResponse appresponse = new CustomResponse();
                        return appresponse.set(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.name(),
                                null,
                                "eng",
                                "token",
                                0L,
                                0L,
                                messages,
                                Mono.empty()
                        );
                    }

                    return next.handle(request);
                });
    }
}


