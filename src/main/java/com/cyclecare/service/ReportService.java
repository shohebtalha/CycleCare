package com.cyclecare.service;

import com.cyclecare.domain.User;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportDataService reportDataService;
    private final PdfReportRenderer pdfReportRenderer;

    public ReportService(ReportDataService reportDataService,
                         PdfReportRenderer pdfReportRenderer) {
        this.reportDataService = reportDataService;
        this.pdfReportRenderer = pdfReportRenderer;
    }

    public byte[] generate(User user) {
        return generate(user, ReportRange.PAST_MONTH);
    }

    public byte[] generate(User user, ReportRange range) {
        return pdfReportRenderer.render(reportDataService.collect(user, range));
    }
}
