package tuf.webscaf.app.service;

import org.springframework.stereotype.Service;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tuf.webscaf.app.dbContext.slave.dto.SlaveChartOfAccountDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class PdfService {

    private static final String PDF_RESOURCES = "/pdf-resources/";
    @Autowired
    ChartOfAccountService chartOfAccountService;
    @Autowired
    SpringTemplateEngine templateEngine;

    public  File generatePdf(List<SlaveChartOfAccountDto> chartOfAccountDto) throws IOException, DocumentException {
        Context context = getContext(chartOfAccountDto);
        String html = loadAndFillTemplate(context);
        return renderPdf(html);
    }

    private  File renderPdf(String html) throws IOException, DocumentException {
        File file = File.createTempFile("chartOfAccounts", ".pdf");
        OutputStream outputStream = new FileOutputStream(file);
        ITextRenderer renderer = new ITextRenderer(20f * 4f / 3f, 20);
        renderer.setDocumentFromString(html, new ClassPathResource(PDF_RESOURCES).getURL().toExternalForm());
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();
        file.deleteOnExit();
        return file;
    }

    private  Context getContext(List<SlaveChartOfAccountDto> chartOfAccountDto) {
        Context context = new Context();
        context.setVariable("chartOfAccounts", chartOfAccountDto);
        return context;
    }

    private  String loadAndFillTemplate(Context context) {
        return templateEngine.process("pdf_students", context);
    }

}
