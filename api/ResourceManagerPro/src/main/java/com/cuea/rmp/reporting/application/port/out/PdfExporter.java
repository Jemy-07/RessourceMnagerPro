package com.cuea.rmp.reporting.application.port.out;

import java.util.List;

/** Outbound port that renders a titled table to PDF bytes. */
public interface PdfExporter {

    byte[] exportTable(String title, List<String> headers, List<List<String>> rows);
}
