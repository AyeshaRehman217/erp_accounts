package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;


@Service
public class SeederTransactionService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;

    public Mono<ServerResponse> seedTransactionData(String url) {

        List<String> list = new ArrayList<>();

        String json = "{\n" +
                "  \"id\": 0,\n" +
                "  \"transaction_id\": \"\",\n" +
                "  \"transaction_status\": {\n" +
                "    \"uuid\": \"\",\n" +
                "    \"name\": \"\"\n" +
                "  },\n" +
                "  \"voucher_uuid\": \"22ddf035-fc41-4c60-8729-2453b497d9e4\",\n" +
                "  \"transaction_data\": {\n" +
                "    \"rows\": [\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"4-001\",\n" +
                "          \"account_name\": \"Revenue\",\n" +
                "          \"uuid\": \"5474bf49-ff6f-4393-b93b-a422d074144a\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 1000.76,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-001\",\n" +
                "          \"account_name\": \"Cost of Goods Sold\",\n" +
                "          \"uuid\": \"229a88e6-895e-41d6-9c8d-c44bffedcb39\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 750.0,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"1-001-001-001\",\n" +
                "          \"account_name\": \"Petty Cash\",\n" +
                "          \"uuid\": \"dea021ed-878f-49a4-acf3-ee3e918a5109\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 1750.76\n" +
                "      }\n" +
                "    ],\n" +
                "    \"calendar_period_uuid\": \"8811b9b8-56cf-42d3-b705-b8ef4c1f309c\",\n" +
                "    \"date\": \"2023-04-04T06:54:39\",\n" +
                "    \"company_uuid\": \"fe9a7354-b784-4a1f-854f-2240d4457c93\",\n" +
                "    \"branch_uuid\": \"56b84a97-3e6c-40fb-aba4-feb8d4d6f94a\",\n" +
                "    \"transaction_description\": \"This is transaction record\",\n" +
                "    \"job_center\": {\n" +
                "      \"uuid\": \"47027a03-02b5-4026-ab29-898b745ade98\",\n" +
                "      \"name\": \"Convocation\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"doc_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"doc_name\": \"string\",\n" +
                "      \"doc_bucket_uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        list.add(json);

        String json2 = "{\n" +
                "  \"id\": 0,\n" +
                "  \"transaction_id\": \"\",\n" +
                "  \"transaction_status\": {\n" +
                "    \"uuid\": \"\",\n" +
                "    \"name\": \"\"\n" +
                "  },\n" +
                "  \"voucher_uuid\": \"53cfcf35-54d7-47e9-8033-64aa5660bfce\",\n" +
                "  \"transaction_data\": {\n" +
                "    \"rows\": [\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"1-001-001\",\n" +
                "          \"account_name\": \"Cash in Hand\",\n" +
                "          \"uuid\": \"c2fad0d9-c13e-4cca-a23f-9daabf9fe8c1\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 3000.0,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-080\",\n" +
                "          \"account_name\": \"Wages Expense\",\n" +
                "          \"uuid\": \"3ac383f2-753c-474c-99da-24ee095741fa\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 2000.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-050\",\n" +
                "          \"account_name\": \"Rent Expense\",\n" +
                "          \"uuid\": \"3159e742-94e9-4788-b5b6-56a3f6b39f73\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 1000.0\n" +
                "      }\n" +
                "    ],\n" +
                "    \"calendar_period_uuid\": \"8811b9b8-56cf-42d3-b705-b8ef4c1f309c\",\n" +
                "    \"date\": \"2023-04-04T06:54:39\",\n" +
                "    \"company_uuid\": \"fe9a7354-b784-4a1f-854f-2240d4457c93\",\n" +
                "    \"branch_uuid\": \"56b84a97-3e6c-40fb-aba4-feb8d4d6f94a\",\n" +
                "    \"transaction_description\": \"This is transaction record\",\n" +
                "    \"job_center\": {\n" +
                "      \"uuid\": \"\",\n" +
                "      \"name\": \"\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"doc_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"doc_name\": \"string\",\n" +
                "      \"doc_bucket_uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        list.add(json2);


        String json3 = "{\n" +
                "  \"id\": 0,\n" +
                "  \"transaction_id\": \"\",\n" +
                "  \"transaction_status\": {\n" +
                "    \"uuid\": \"\",\n" +
                "    \"name\": \"\"\n" +
                "  },\n" +
                "  \"voucher_uuid\": \"71f56c92-b7bb-48d0-b3ae-698faebae406\",\n" +
                "  \"transaction_data\": {\n" +
                "    \"rows\": [\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"3-001\",\n" +
                "          \"account_name\": \"Common Stock\",\n" +
                "          \"uuid\": \"c4f787c9-26fa-4d25-b093-43cc49806a7a\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 3276.90,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-030\",\n" +
                "          \"account_name\": \"Depreciation Expense\",\n" +
                "          \"uuid\": \"3dce1b66-ed61-4ca5-9483-e2fb4e2327bf\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 3276.90\n" +
                "      }\n" +
                "    ],\n" +
                "    \"calendar_period_uuid\": \"8811b9b8-56cf-42d3-b705-b8ef4c1f309c\",\n" +
                "    \"date\": \"2023-04-04T06:54:39\",\n" +
                "    \"company_uuid\": \"fe9a7354-b784-4a1f-854f-2240d4457c93\",\n" +
                "    \"branch_uuid\": \"56b84a97-3e6c-40fb-aba4-feb8d4d6f94a\",\n" +
                "    \"transaction_description\": \"This is transaction record\",\n" +
                "    \"job_center\": {\n" +
                "      \"uuid\": \"\",\n" +
                "      \"name\": \"\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"doc_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"doc_name\": \"string\",\n" +
                "      \"doc_bucket_uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        list.add(json3);


        String json4 = "{\n" +
                "  \"id\": 0,\n" +
                "  \"transaction_id\": \"\",\n" +
                "  \"transaction_status\": {\n" +
                "    \"uuid\": \"\",\n" +
                "    \"name\": \"\"\n" +
                "  },\n" +
                "  \"voucher_uuid\": \"359164e8-3e1e-4075-8de9-b62e6c40e3f5\",\n" +
                "  \"transaction_data\": {\n" +
                "    \"rows\": [\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-070\",\n" +
                "          \"account_name\": \"Utilities Expense\",\n" +
                "          \"uuid\": \"4bb85a9a-52eb-4a16-b667-e165914ce12c\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 12500.0,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"1-001-001-001\",\n" +
                "          \"account_name\": \"Petty Cash\",\n" +
                "          \"uuid\": \"dea021ed-878f-49a4-acf3-ee3e918a5109\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 12500.0\n" +
                "      }\n" +
                "    ],\n" +
                "    \"calendar_period_uuid\": \"8811b9b8-56cf-42d3-b705-b8ef4c1f309c\",\n" +
                "    \"date\": \"2023-04-04T06:54:39\",\n" +
                "    \"company_uuid\": \"fe9a7354-b784-4a1f-854f-2240d4457c93\",\n" +
                "    \"branch_uuid\": \"56b84a97-3e6c-40fb-aba4-feb8d4d6f94a\",\n" +
                "    \"transaction_description\": \"This is transaction record\",\n" +
                "    \"job_center\": {\n" +
                "      \"uuid\": \"47027a03-02b5-4026-ab29-898b745ade98\",\n" +
                "      \"name\": \"Convocation\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"doc_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"doc_name\": \"string\",\n" +
                "      \"doc_bucket_uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        list.add(json4);


        String json5 = "{\n" +
                "  \"id\": 0,\n" +
                "  \"transaction_id\": \"\",\n" +
                "  \"transaction_status\": {\n" +
                "    \"uuid\": \"\",\n" +
                "    \"name\": \"\"\n" +
                "  },\n" +
                "  \"voucher_uuid\": \"359164e8-3e1e-4075-8de9-b62e6c40e3f5\",\n" +
                "  \"transaction_data\": {\n" +
                "    \"rows\": [\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"5-050\",\n" +
                "          \"account_name\": \"Rent Expense\",\n" +
                "          \"uuid\": \"3159e742-94e9-4788-b5b6-56a3f6b39f73\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 25000.0,\n" +
                "        \"dr\": 0.0\n" +
                "      },\n" +
                "      {\n" +
                "        \"account\": {\n" +
                "          \"account_code\": \"1-001-001-001\",\n" +
                "          \"account_name\": \"Petty Cash\",\n" +
                "          \"uuid\": \"dea021ed-878f-49a4-acf3-ee3e918a5109\"\n" +
                "        },\n" +
                "        \"description\": \"This is ledger row\",\n" +
                "        \"profit_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cost_center\": {\n" +
                "          \"uuid\": \"\",\n" +
                "          \"name\": \"\"\n" +
                "        },\n" +
                "        \"cr\": 0.0,\n" +
                "        \"dr\": 25000.0\n" +
                "      }\n" +
                "    ],\n" +
                "    \"calendar_period_uuid\": \"8811b9b8-56cf-42d3-b705-b8ef4c1f309c\",\n" +
                "    \"date\": \"2023-04-04T06:54:39\",\n" +
                "    \"company_uuid\": \"fe9a7354-b784-4a1f-854f-2240d4457c93\",\n" +
                "    \"branch_uuid\": \"56b84a97-3e6c-40fb-aba4-feb8d4d6f94a\",\n" +
                "    \"transaction_description\": \"This is transaction record\",\n" +
                "    \"job_center\": {\n" +
                "      \"uuid\": \"47027a03-02b5-4026-ab29-898b745ade98\",\n" +
                "      \"name\": \"Convocation\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"attachments\": [\n" +
                "    {\n" +
                "      \"doc_id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"doc_name\": \"string\",\n" +
                "      \"doc_bucket_uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        list.add(json5);


        Flux<Boolean> flux = Flux.just(false);
        for (int i = 0; i < list.size(); i++) {
            Mono<Boolean> res = seederService.seedData(url, list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if (aBoolean) {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    } else {
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Unsuccessful")
                        );
                    }

                    return appresponse.set(
                            HttpStatus.OK.value(),
                            HttpStatus.OK.name(),
                            null,
                            "eng",
                            "token",
                            0L,
                            0L,
                            messages,
                            Mono.just(aBoolean)
                    );
        });
    }
}