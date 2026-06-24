package com.cuea.rmp.reporting.infrastructure.export;

import com.cuea.rmp.reporting.application.port.out.PdfExporter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;

/** OpenPDF implementation of {@link PdfExporter} — renders a titled table. */
@Component
public class OpenPdfExporter implements PdfExporter {

    private static final Color HEADER_BG = new Color(55, 65, 81);

    @Override
    public byte[] exportTable(String title, List<String> headers, List<List<String>> rows) {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated: " + Instant.now() + "   ·   Rows: " + rows.size(),
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(HEADER_BG);
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            if (rows.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No data", cellFont));
                empty.setColspan(headers.size());
                empty.setPadding(8);
                table.addCell(empty);
            } else {
                for (List<String> row : rows) {
                    for (String value : row) {
                        PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, cellFont));
                        cell.setPadding(5);
                        table.addCell(cell);
                    }
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate PDF: " + ex.getMessage(), ex);
        }
    }
}
