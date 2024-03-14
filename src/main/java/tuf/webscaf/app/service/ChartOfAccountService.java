package tuf.webscaf.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChartOfAccountDto;
import tuf.webscaf.app.dbContext.slave.repository.SlaveAccountRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ChartOfAccountService{

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlaveAccountRepository slaveAccountRepository;

//    public List<SlaveChartOfAccountDto> chartOfAccountsList() {
//        Flux<SlaveChartOfAccountDto> chartReportingFlux = slaveAccountRepository
//                .fetchingParentOfAllChild();
//
//        return chartReportingFlux
//                .collectList()
//                .flatMap(charts -> {
//
//                    //Map where parent Account UUID is not Null
//                    MultiValueMap<UUID, SlaveChartOfAccountDto> innerChildMap = new LinkedMultiValueMap<UUID, SlaveChartOfAccountDto>();
//
//                    //creating List of Parent UUID
//                    List<SlaveChartOfAccountDto> chartOfAccounts = new ArrayList<>();
//
//                    //iterating over the loop from query
//                    for (SlaveChartOfAccountDto chartList : charts) {
//
//                        //check where parent Account UUID is null then add in the Main Parent Account List
//                        if (chartList.getParentAccountUUID() == null) {
//                            chartOfAccounts.add(chartList);
//
//
//                        } else {
//                            innerChildMap.add(chartList.getParentAccountUUID(), chartList);
//                        }
//                    }
//
//                    //iterating over the Main Parent List
//                    for (SlaveChartOfAccountDto parentWhereNull : chartOfAccounts) {
//                        // Calling Recursive Function to check
//                        parentWhereNull.setChildAccount(setChildAccounts(innerChildMap, parentWhereNull));
//                    }
//
//                    return slaveAccountRepository.countFinancialChartOfAccounts()
//                            .flatMap(countCharts -> {
//                                return responseIndexSuccessMsg("Record Fetched Successfully", chartOfAccounts, countCharts);
//                            });
//                });
//        final List<SlaveChartOfAccountDto> chartOfAccounts = slaveAccountRepository
//                .fetchingParentOfAllChild(v -> SlaveChartOfAccountDto.builder()
//                        .name("Name " + v)
//                        .name("Name " + v)
//                        .controlCode("Last Name " + v)
//                        .isEntryAllowed(v%2==0?true:false)
//                        .accountTypeName("accountTypeName " + v)
//                        .build())
//                .collect(Collectors.toList());
//        return chartOfAccounts;
//        }
    //Setting Inner Child Accounts
    public List<SlaveChartOfAccountDto> setChildAccounts(MultiValueMap<UUID, SlaveChartOfAccountDto> childMap, SlaveChartOfAccountDto chartsDto) {

        //Creating a Chart of Account List for returning records
        List<SlaveChartOfAccountDto> chartList = new ArrayList<>();

        //check where map key is not null
        if (childMap.get(chartsDto.getUuid()) != null) {
            //iterating over the map based on UUID key
            for (SlaveChartOfAccountDto chart : childMap.get(chartsDto.getUuid())) {
                //Setting Inner Child Accounts
                chart.setChildAccount(setChildAccounts(childMap, chart));
                //Adding in Returning List
                chartList.add(chart);
            }
        }
        return chartList;
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }
    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }
}
