package tuf.webscaf.app.http.validationFilters.purchaseReturnVoucherHandler;

import org.json.JSONArray;
import org.json.JSONObject;
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

public class UpdatePurchaseReturnVoucherHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.bodyToMono(String.class)
                .flatMap(block -> {
                    JSONObject jsonObj = new JSONObject(block);
                    ServerRequest.Builder newRequestBuilder = ServerRequest.from(request);
                    newRequestBuilder.body(block);

                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (!request.pathVariable("uuid").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Invalid UUID"
                                )
                        );
                    }

                    // check for transaction status
                    if (jsonObj.has("transaction_status")) {

                        // transaction status json object
                        JSONObject transactionStatus = jsonObj.getJSONObject("transaction_status");

                        if (transactionStatus.has("uuid")) {
                            String transactionStatusUUID = transactionStatus.get("uuid").toString();

                            if (!transactionStatus.get("uuid").equals(null) && (!transactionStatusUUID.isEmpty())) {
                                if (!transactionStatusUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Transaction Status"
                                            )
                                    );
                                }
                            }
//                            else {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Transaction Status Field Required"
//                                        )
//                                );
//                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Transaction Status Field Required"
                                    )
                            );
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Transaction Status Required"
                                )
                        );
                    }

                    // check for voucher
                    if (jsonObj.has("voucher")) {

                        // transaction status json object
                        JSONObject voucher = jsonObj.getJSONObject("voucher");

                        if (voucher.has("uuid")) {
                            String voucherUUID = voucher.get("uuid").toString();

                            if (!voucher.get("uuid").equals(null) && (!voucherUUID.isEmpty())) {
                                if (!voucherUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Voucher"
                                            )
                                    );
                                }
                            }
                            else {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Voucher Field Required"
                                        )
                                );
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Voucher Field Required"
                                    )
                            );
                        }


                        if (voucher.has("name")) {
                            String name = voucher.get("name").toString();

                            if (!voucher.get("name").equals(null) && (!name.isEmpty())) {
                                if (!name.matches("^[\\sa-zA-Z0-9\\\\+.,:;\\/#$_-]*$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Voucher Name"
                                            )
                                    );
                                }
                            }
                        }

                        if (voucher.has("slug")) {
                            String slug = voucher.get("slug").toString();

                            if (!voucher.get("slug").equals(null) && (!slug.isEmpty())) {
                                if (!slug.matches("^[\\sa-zA-Z0-9\\\\+.,:;\\/#$_-]*$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Voucher Slug"
                                            )
                                    );
                                }
                            }
                        }

                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Voucher Required"
                                )
                        );
                    }

                    // check for transaction data
                    if (jsonObj.has("transaction_data")) {
                        JSONObject transactionData = jsonObj.getJSONObject("transaction_data");
                        if (transactionData.has("rows")) {
                            JSONArray rows = transactionData.getJSONArray("rows");

                            if (!rows.isEmpty()) {
                                JSONObject jsonObject = null;

                                for (int i = 0; i < rows.length(); i++) {
                                    jsonObject = rows.getJSONObject(i);

                                    if (jsonObject.has("account")) {

                                        // account json object
                                        JSONObject transactionAccount = jsonObject.getJSONObject("account");

                                        if (transactionAccount.has("uuid")) {
                                            String accountUUID = transactionAccount.get("uuid").toString();

                                            if (!transactionAccount.get("uuid").equals(null) && (!accountUUID.isEmpty())) {
                                                if (!accountUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Account"
                                                            )
                                                    );
                                                    break;
                                                }

                                            } else {
                                                messages.add(
                                                        new AppResponseMessage(
                                                                AppResponse.Response.ERROR,
                                                                "Account Field Required"
                                                        )
                                                );
                                                break;
                                            }
                                        } else {
                                            messages.add(
                                                    new AppResponseMessage(
                                                            AppResponse.Response.ERROR,
                                                            "Account Field Required"
                                                    )
                                            );
                                            break;
                                        }


                                        if (transactionAccount.has("account_code")) {
                                            String accountCode = transactionAccount.get("account_code").toString();

                                            if (!transactionAccount.get("account_code").equals(null) && (!accountCode.isEmpty())) {
                                                if (!accountCode.matches("^[\\sa-zA-Z0-9\\\\+.,:;\\/#$_-]*$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Account Code"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }

                                        if (transactionAccount.has("account_name")) {
                                            String accountName = transactionAccount.get("account_name").toString();
                                            if ((!transactionAccount.get("account_name").equals(null)) && (!accountName.isEmpty())) {
                                                if (!accountName.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Account Name"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }


                                    } else {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Account Required"
                                                )
                                        );
                                        break;
                                    }


                                    // check for profit center
                                    if (jsonObject.has("profit_center")) {

                                        // profit center json object
                                        JSONObject profitCenter = jsonObject.getJSONObject("profit_center");

                                        //check for profit center id
                                        if (profitCenter.has("uuid")) {
                                            String profitCenterUUID = profitCenter.get("uuid").toString();

                                            if (!profitCenter.get("uuid").equals(null) && (!profitCenterUUID.isEmpty())) {
                                                if (!profitCenterUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Profit Center"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }

                                        // check for profit center name
                                        if (profitCenter.has("name")) {
                                            String profitCenterName = profitCenter.get("name").toString();
                                            if ((!profitCenter.get("name").equals(null)) && (!profitCenterName.isEmpty())) {
                                                if (!profitCenterName.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Profit Center Name"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }
                                    }


                                    //check for cost center
                                    if (jsonObject.has("cost_center")) {

                                        // cost center json object
                                        JSONObject costCenter = jsonObject.getJSONObject("cost_center");
//
                                        // check for cost center uuid
                                        if (costCenter.has("uuid")) {
                                            String costCenterUUID = costCenter.get("uuid").toString();

                                            if (!costCenter.get("uuid").equals(null) && (!costCenterUUID.isEmpty())) {
                                                if (!costCenterUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Cost Center"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }

                                        // check for cost center name
                                        if (costCenter.has("name")) {
                                            String costCenterName = costCenter.get("name").toString();
                                            if ((!costCenter.get("name").equals(null)) && (!costCenterName.isEmpty())) {
                                                if (!costCenterName.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Invalid Cost Center Name"
                                                            )
                                                    );
                                                    break;
                                                }
                                            }
                                        }

                                    }

                                    String crAmount = jsonObject.get("cr").toString();
                                    String drAmount = jsonObject.get("dr").toString();

                                    // cr amount of ledger row
                                    Double cr = null;


                                    // dr amount of ledger row
                                    Double dr = null;


                                    // check for cr amount
                                    if (jsonObject.has("cr")) {

                                        if (!jsonObject.get("cr").equals(null) && (!crAmount.isEmpty())) {
                                            if (!crAmount.matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                                                messages.add(
                                                        new AppResponseMessage(
                                                                AppResponse.Response.ERROR,
                                                                "Invalid Credit Amount"
                                                        )
                                                );
                                                break;
                                            }
                                            else {
                                                // if cr amount is not empty, parse its value to double
                                                cr = Double.parseDouble(crAmount);
                                            }
                                        }
                                    }

                                    // check for dr amount
                                    if (jsonObject.has("dr")) {

                                        if (!jsonObject.get("dr").equals(null) && (!drAmount.isEmpty())) {
                                            if (!drAmount.matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                                                messages.add(
                                                        new AppResponseMessage(
                                                                AppResponse.Response.ERROR,
                                                                "Invalid Debit Amount"
                                                        )
                                                );
                                                break;
                                            }
                                            else {
                                                // if dr amount is not empty, parse its value to double
                                                dr = Double.parseDouble(drAmount);
                                            }
                                        }
                                    }

                                    if (jsonObject.has("cr") && jsonObject.has("cr")) {
                                        if ((jsonObject.get("cr").equals(null)) && (jsonObject.get("dr").equals(null))) {
                                            messages.add(
                                                    new AppResponseMessage(
                                                            AppResponse.Response.ERROR,
                                                            "Transaction must have one of credit or debit amount"
                                                    )
                                            );
                                            break;
                                        } else if ((crAmount.isEmpty()) && (drAmount.isEmpty())) {
                                            messages.add(
                                                    new AppResponseMessage(
                                                            AppResponse.Response.ERROR,
                                                            "Transaction must have one of credit or debit amount"
                                                    )
                                            );
                                            break;

                                        } else if ((jsonObject.get("cr").equals(null)) && (drAmount.isEmpty())) {
                                            messages.add(
                                                    new AppResponseMessage(
                                                            AppResponse.Response.ERROR,
                                                            "Transaction must have one of credit or debit amount"
                                                    )
                                            );
                                            break;

                                        } else if ((crAmount.isEmpty()) && (jsonObject.get("dr").equals(null))) {
                                            messages.add(
                                                    new AppResponseMessage(
                                                            AppResponse.Response.ERROR,
                                                            "Transaction must have one of credit or debit amount"
                                                    )
                                            );
                                            break;

                                        } else {
                                            // if cr amount and dr amount are not null, check to these conditions
                                            if ((cr != null) && (dr != null)) {
                                                if (cr == 0.0 && dr == 0.0) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Transaction must have one of credit or debit amount"
                                                            )
                                                    );
                                                    break;
                                                }

                                                // else check this condition
                                                else {
                                                    if (cr != 0.0 && dr != 0.0) {
                                                        messages.add(
                                                                new AppResponseMessage(
                                                                        AppResponse.Response.ERROR,
                                                                        "Transaction can have only one of credit or debit amount"
                                                                )
                                                        );
                                                        break;
                                                    }
                                                }

                                            }
                                            // if cr amount is not null, check to these conditions
                                            else if (cr != null) {
                                                if (cr == 0.0 && drAmount.isEmpty()) {
                                                    messages.add(
                                                            new AppResponseMessage(
                                                                    AppResponse.Response.ERROR,
                                                                    "Transaction must have one of credit or debit amount"
                                                            )
                                                    );
                                                    break;

                                                }

                                                // else check this condition
                                                else {
                                                    if (cr == 0.0 && (jsonObject.get("dr").equals(null))) {
                                                        messages.add(
                                                                new AppResponseMessage(
                                                                        AppResponse.Response.ERROR,
                                                                        "Transaction must have one of credit or debit amount"
                                                                )
                                                        );
                                                        break;

                                                    }
                                                }

                                            }
                                            // if dr amount is not null, check to these conditions
                                            else {
                                                if (dr != null) {

                                                    if (dr == 0.0 && crAmount.isEmpty()) {
                                                        messages.add(
                                                                new AppResponseMessage(
                                                                        AppResponse.Response.ERROR,
                                                                        "Transaction must have one of credit or debit amount"
                                                                )
                                                        );
                                                        break;

                                                    }

                                                    // else check this condition
                                                    else {
                                                        if (dr == 0.0 && (jsonObject.get("cr").equals(null))) {
                                                            messages.add(
                                                                    new AppResponseMessage(
                                                                            AppResponse.Response.ERROR,
                                                                            "Transaction must have one of credit or debit amount"
                                                                    )
                                                            );
                                                            break;

                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Transaction Ledger Rows Required"
                                    )
                            );
                        }


                        // check for calendar period uuid
                        if (transactionData.has("calendar_period_uuid")) {
                            String calendarPeriodUUID = transactionData.get("calendar_period_uuid").toString();

                            if (!transactionData.get("calendar_period_uuid").equals(null) && (!calendarPeriodUUID.isEmpty())) {
                                if (!calendarPeriodUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Calendar Period"
                                            )
                                    );
                                }
                            } else {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Calendar Period Field Required"
                                        )
                                );
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Calendar Period Field Required"
                                    )
                            );
                        }

                        // check for transaction date
                        if (transactionData.has("date")) {
                            String transactionDate = transactionData.get("date").toString();

                            if (!transactionData.get("date").equals(null) && (!transactionDate.isEmpty())) {
                                if (!transactionDate.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])T(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Transaction Date. Date must be in yyyy-MM-ddTHH:mm:ss format"
                                            )
                                    );
                                }
                            } else {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Transaction Date Field Required"
                                        )
                                );
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Transaction Date Field Required"
                                    )
                            );
                        }

                        // check for company uuid
                        if (transactionData.has("company_uuid")) {
                            String companyUUID = transactionData.get("company_uuid").toString();

                            if (!transactionData.get("company_uuid").equals(null) && (!companyUUID.isEmpty())) {
                                if (!companyUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Company"
                                            )
                                    );
                                }
                            } else {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Company Field Required"
                                        )
                                );
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Company Field Required"
                                    )
                            );
                        }

                        // check for branch uuid
                        if (transactionData.has("branch_uuid")) {
                            String branchUUID = transactionData.get("branch_uuid").toString();

                            if (!transactionData.get("branch_uuid").equals(null) && (!branchUUID.isEmpty())) {
                                if (!branchUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Branch"
                                            )
                                    );
                                }
                            } else {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Branch Field Required"
                                        )
                                );
                            }
                        } else {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Branch Field Required"
                                    )
                            );
                        }

                        // check for transaction description
                        if (transactionData.has("transaction_description")) {
                            String transactionDescription = transactionData.get("transaction_description").toString();

                            if ((!transactionData.get("transaction_description").equals(null)) && (!transactionDescription.isEmpty())) {
                                if (!transactionDescription.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                    messages.add(
                                            new AppResponseMessage(
                                                    AppResponse.Response.ERROR,
                                                    "Invalid Transaction Description"
                                            )
                                    );
                                }
                            }
                        }

                        if (transactionData.has("job_center")) {

                            // job center json object
                            JSONObject jobCenter = transactionData.getJSONObject("job_center");

                            if (jobCenter.has("uuid")) {
                                String jobCenterUUID = jobCenter.get("uuid").toString();

                                if (!jobCenter.get("uuid").equals(null) && (!jobCenterUUID.isEmpty())) {
                                    if (!jobCenterUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Invalid Job Center"
                                                )
                                        );
                                    }
                                }
                            }

                            if (jobCenter.has("name")) {
                                String jobCenterName = jobCenter.get("name").toString();

                                if ((!jobCenter.get("name").equals(null)) && (!jobCenterName.isEmpty())) {
                                    if (!jobCenterName.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Invalid Job Center Name"
                                                )
                                        );
                                    }
                                }
                            }
                        }

                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Transaction Data Required"
                                )
                        );
                    }

                    if (jsonObj.has("attachments")) {
                        JSONArray rows = jsonObj.getJSONArray("attachments");
                        JSONObject jsonObject = null;

                        for (int i = 0; i < rows.length(); i++) {
                            jsonObject = rows.getJSONObject(i);

                            //check for doc Id
                            if (jsonObject.has("doc_id")) {
                                String docId = jsonObject.get("doc_id").toString();

                                if (!jsonObject.get("doc_id").equals(null) && (!docId.isEmpty())) {
                                    if (!docId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Invalid Doc Id"
                                                )
                                        );
                                        break;
                                    }
                                }
                            }

                            //check for doc name
                            if (jsonObject.has("doc_name")) {
                                String docName = jsonObject.get("doc_name").toString();

                                if ((!jsonObject.get("doc_name").equals(null)) && (!docName.isEmpty())) {
                                    if (!docName.matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Invalid Doc Name"
                                                )
                                        );
                                        break;
                                    }
                                }
                            }

                            //check for doc Bucket uuid
                            if (jsonObject.has("doc_bucket_uuid")) {
                                String docBucketUUID = jsonObject.get("doc_bucket_uuid").toString();

                                if (!jsonObject.get("doc_bucket_uuid").equals(null) && (!docBucketUUID.isEmpty())) {
                                    if (!docBucketUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                        messages.add(
                                                new AppResponseMessage(
                                                        AppResponse.Response.ERROR,
                                                        "Invalid Doc Bucket UUID"
                                                )
                                        );
                                        break;
                                    }
                                }
                            }


                        }
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
                    return next.handle(newRequestBuilder.build());
                });
    }
}

