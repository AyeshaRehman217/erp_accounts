package tuf.webscaf.app.http.validationFilters.documentNatureMetaDataHandler;

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


public class UpdateDocumentNatureMetaDataHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.getFirst("key").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Key Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("key")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In Key Field!"
                                )
                        );
                    }

                    if (value.getFirst("valueType").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Value Type Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("valueType")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In value Field!"
                                )
                        );
                    }

                    if (value.getFirst("valueChar").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Value Char Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("valueChar")
                            .matches("^[a-zA-Z]+(?:\\s+[a-zA-Z]+)*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "No Digits and Special Characters are allowed In value Char Field!"
                                )
                        );
                    }

                    if (value.getFirst("valueInt").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Value Int Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("valueInt").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Value Int will contain only Numeric Values"
                                )
                        );
                    }

                    if (value.getFirst("valueDate").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Value Date Field is Required"
                                )
                        );
                    }

//                    if (value.getFirst("documentId").isEmpty()) {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Document Id Field is Required"
//                                )
//                        );
//                    }
//
//                    if (!value.getFirst("documentId").matches("^[0-9]*$")) {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Document Id will contain only Numeric Values"
//                                )
//                        );
//                    }
//
//                    if (value.getFirst("documentNatureId").isEmpty()) {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Document Nature Id Field is Required"
//                                )
//                        );
//                    }
//
//                    if (!value.getFirst("documentNatureId").matches("^[0-9]*$")) {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Document Nature Id will contain only Numeric Values"
//                                )
//                        );
//                    }

                    if (value.getFirst("documentNatureMetaFieldId").isEmpty()) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Document Nature Meta Field Id Field is Required"
                                )
                        );
                    }

                    if (!value.getFirst("documentNatureMetaFieldId").matches("^[0-9]*$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Document Nature Meta Field Id will contain only Numeric Values"
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


