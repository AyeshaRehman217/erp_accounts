package tuf.webscaf.app.http.validationFilters.cashFlowAdjustmentHandler;

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

public class UpdateCashFlowAdjustmentHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.getFirst("drAmount").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "dr Amount Field Required"
                                )
                        );
                    }

                    if (!value.getFirst("drAmount").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Dr Amount will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("crAmount").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "cr Amount Field Required"
                                )
                        );
                    }

                    if (!value.getFirst("crAmount").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Cr Amount will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("accountId").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "account Id Field Required"
                                )
                        );
                    }

                    if (!value.getFirst("accountId").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Account Id will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("transactionId").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "transaction Id Field Required"
                                )
                        );
                    }

                    if (!value.getFirst("transactionId").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Transaction Id will contain only Numeric Values"
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
