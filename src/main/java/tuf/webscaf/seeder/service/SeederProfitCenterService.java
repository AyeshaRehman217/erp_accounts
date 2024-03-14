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
public class SeederProfitCenterService {


    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;

    public Mono<ServerResponse> seedProfitCenterData(String url){

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "BS Computer Science");
        formData.add("description", "Profit center description");
        formData.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "BS Software Engineering");
        formData1.add("description", "Profit center description");
        formData1.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData1.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData1.add("status", "true");
        list.add(formData1);

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "BS Mathematics");
        formData2.add("description", "Profit center description");
        formData2.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData2.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData2.add("status", "true");
        list.add(formData2);

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "BS Phycology");
        formData3.add("description", "Profit center description");
        formData3.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData3.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData3.add("status", "true");
        list.add(formData3);

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "BS Physics");
        formData4.add("description", "Profit center description");
        formData4.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData4.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData4.add("status", "true");
        list.add(formData4);

        MultiValueMap<String, String> formData5 = new LinkedMultiValueMap<>();
        formData5.add("name", "BS Chemistry");
        formData5.add("description", "Profit center description");
        formData5.add("companyUUID","fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData5.add("branchUUID", "56b84a97-3e6c-40fb-aba4-feb8d4d6f94a");
        formData5.add("status", "true");
        list.add(formData5);

        Flux<Boolean> flux = Flux.just(false);
        for(int i=0; i< list.size(); i++){
            Mono<Boolean> res = seederService.seedData(url,list.get(i));
            flux = flux.concatWith(res);
        }
        return flux.last()
                .flatMap(aBoolean -> {
                    List<AppResponseMessage> messages = new ArrayList<>();
                    if(aBoolean){
                        messages = List.of(
                                new AppResponseMessage(
                                        AppResponse.Response.SUCCESS,
                                        "Successful")
                        );
                    }else {
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
