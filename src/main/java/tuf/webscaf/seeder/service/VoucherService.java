package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.VoucherEntity;
import tuf.webscaf.app.dbContext.master.repository.VoucherRepository;
import tuf.webscaf.app.dbContext.master.repository.VoucherTypeCatalogueRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse appresponse;

    public Mono<ServerResponse> seedVoucherData(String url) {

        ArrayList<MultiValueMap<String, String>> list = new ArrayList<>();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "CRV-1");
        formData.add("description", "Cash Receipt Voucher 1");
        formData.add("voucherTypeCatalogueUUID", "5a62f88e-30d3-443a-9ea2-37e65ac1a5fd");
        formData.add("status", "true");
        list.add(formData);

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "CRV-2");
        formData1.add("description", "Cash Receipt Voucher 2");
        formData1.add("voucherTypeCatalogueUUID", "5a62f88e-30d3-443a-9ea2-37e65ac1a5fd");
        formData1.add("status", "true");
        list.add(formData1);

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "CPV-1");
        formData2.add("description", "Cash Payment Voucher 1");
        formData2.add("voucherTypeCatalogueUUID", "89d7035d-d274-438b-b7f2-3af764c84421");
        formData2.add("status", "true");
        list.add(formData2);

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "CPV-2");
        formData3.add("description", "Cash Payment Voucher 2");
        formData3.add("voucherTypeCatalogueUUID", "89d7035d-d274-438b-b7f2-3af764c84421");
        formData3.add("status", "true");
        list.add(formData3);

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "JV-1");
        formData4.add("description", "Journal Voucher 1");
        formData4.add("voucherTypeCatalogueUUID", "23806ff1-ebb1-412c-9fa5-a9525fd9d28e");
        formData4.add("status", "true");
        list.add(formData4);

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
