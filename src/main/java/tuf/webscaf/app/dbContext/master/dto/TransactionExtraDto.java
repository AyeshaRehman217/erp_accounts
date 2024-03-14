package tuf.webscaf.app.dbContext.master.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class TransactionExtraDto {
    private TransactionExtraEntryDto entry;
    private TransactionCompanyDetailDto companydetails;
    private List<TransactionExtraDocumentDto> attachments;
    private TransactionExtraCalendarDto calendar;
}
