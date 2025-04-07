package com.swajyot.log.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.swajyot.log.model.Characteristic;
import com.swajyot.log.model.InspectionForm;
import com.swajyot.log.model.Lacquer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class InspectionFormPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss");

    // Border properties
    private static final float BORDER_WIDTH = 1f;
    private static final Border SOLID_BORDER = new SolidBorder(ColorConstants.BLACK, BORDER_WIDTH);
    private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(230, 230, 230);

    /**
     * Generate a PDF for an inspection form
     * @param form The inspection form data
     * @return PDF as byte array
     */
    public byte[] generatePdf(InspectionForm form) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // Load fonts
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Add content to PDF
        addHeader(document, form, fontBold);
        addHeaderInfo(document, form, font, fontBold);
        addLacquerTable(document, form, font, fontBold);
        addCharacteristicsTable(document, form, font, fontBold);
        addSignatureSection(document, form, font, fontBold);
        addReviewInfo(document, form, font, fontBold);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Add the header section to the PDF
     */
    private void addHeader(Document document, InspectionForm form, PdfFont fontBold) throws IOException {
        // Create a 3-column table for the header
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{30, 40, 30}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(SOLID_BORDER);

        // Column 1: Document Info
        Table docInfoTable = createDocumentInfoTable(form, fontBold);
        Cell docInfoCell = new Cell().add(docInfoTable).setBorder(Border.NO_BORDER);
        headerTable.addCell(docInfoCell);

        // Column 2: Title
        Table titleTable = createTitleTable(fontBold);
        Cell titleCell = new Cell().add(titleTable).setBorder(Border.NO_BORDER);
        headerTable.addCell(titleCell);

        // Column 3: Logo
        // Load the AGI logo
        Image logo;
        try {
            // Try to load from classpath
            ClassPathResource resource = new ClassPathResource("static/images/agilogo.png");
            logo = new Image(ImageDataFactory.create(resource.getInputStream().readAllBytes()));
        } catch (Exception e) {
            // Fallback to a placeholder logo
            byte[] placeholder = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAIAAAD/gAIDAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NTkzRTlDQkUyMEU5MTFFQTk4RkNBRkJDODVCRTZCODMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NTkzRTlDQkYyMEU5MTFFQTk4RkNBRkJDODVCRTZCODMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo1OTNFOUNCQzIwRTkxMUVBOThGQ0FGQkM4NUJFNkI4MyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo1OTNFOUNCRDIwRTkxMUVBOThGQ0FGQkM4NUJFNkI4MyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PpR4Hs4AAAgISURBVHja7JxrbBRVFMfPnd3Z3S7d0pWWbYEi5VGMWAPxAZgoBsLDGISoRIN8IcaYKJ+MSvxgCD7wQwkJMTHRRGIk8YMJGk0kBIwvSDRCRMFAIgYUaAHb0hf7nN0ZzwxndnZ3Znfb6ba7c+C07cydu3vv/57//5x7Z2dNQggMx8IyTGAYtjBsYdjCsIVhC8MWhi0MWxi2MGxh2MKwhWELw9Yw1m/O/Q8tCyYzRGmNDaFQMDyQSuA4Dviv1BobAMdR39d1xDl3HOq6jmVNcM1fsdpaIgQIQQD5f5ybpYfRUCXTnE6nwHEyU2/JskgTQizLEkIEQRAEQRRFnudr7TEfQUF8JEl0Y5qZCDAGRifBp8rGRgXgNJUa4ZKlGK4Mm2dnWf1Q30VFAZqYikSOKoqqquGwlP8eRcQDaiiKarooBIDU1NQtXBiBJ0pUgJd7dRtlZQVwoQQRCvE8D/C6LsU1Lpu6D3A6nYbfG24QQjRNczgc+eVcVKLJ9wJSoWoaQC5aDYWigXUcL8t6NBp2OADPcznAo7RFIpFYLOZwOEzWa+DLTIJlZCSJPP5YcLYdXVYUzYz5KQfTGhoa7rrrruXLl4dCIT6vB2nLqxcfPnzY4XDMnDmTZxaVp6xlyGxRIqyUWM37UPkMiouyzJZjwc5SAyGkqmogEFixYsXZs2e//vprmyfzgBgIBJLJZHt7O0+ZYEUd/Pju6JBVNZ9FZctR/Lnvn3PZKQRnwTxDsJlM5vHHH+/r6/v6669nzJhhcXIFYrt27XrooYduvvlmi0t46Rlf5aZ7TU1NLpfL5XK5XE6n0+l0u9yiKAqiwPN8JpPq7e21Xu/sHwwN9RA1BKPBT4lrGlJVVVGUTCaTSCQGBgb6+vquX7/e39//2GOP9fb2fvXVV1OnTrUYzOXLl7/88st9+/ZZXMJLr3rNQ6FQU1NTe3t7W1vblClTmpubXS7XwEBU06QTJ46LSqdlcjfbR2CUPXsufXju8kBU5ngfPcUYERFtMElJDdHfTU1Nd9xxx6pVq7xe75YtW1577bXe3t6vvvrK7/fbOhRCyJYtWzZs2DA4ONjS0mIN3lJ8M6yt7e3tXbZs2Z133mm4UT7uTgYG0q9/8HXw/OnQgFf3+yBeIIXfGCf6FI2mqaZpiURiYGCAYqS72Lx58zPPPNPR0XHo0KG5c+dajOaLL75YuHDha6+9Zt2DJcs1TnS5XAsWLJg+fTohJJVKaZrW19d38eJFnuc9Hk9zc7PP5+N5LpWSXn75vS++unH8ZPrqNYUQJxUZzLVYVBcZGJEkSZKkdDqdTCYlSRoaGlIUJZ1O9/T0NDY2iqLY2dnZ1NRk8SDPnj27cePGl156ieOsXUMr9TqkiGR0Op3zrwk6nU6fz+f3+1taWnw+HyFEVbWvvz31ye7rv/xGYrEoz1MwR3wDwJFEMjk0NBSJRPr7+3/77bdLly4dOXLkp59+2r9//+HDh48dO3bhwoXVq1fbOk44HH7xxRfXrVvX0dFRJnhLzUKCINBhNZvNZrPZbDZL3a6RSCSRSPh8Po/HI8syn0yqv1+JZLME+xxYP+RCoUVcEgQhEAjU1dX5/f729vahUEgURa/X29LS4vF4RFGMRCLP/vPi3r1777nnnvr6enunef/99+fNm9fa2lpO9pVWDiw0TUskEocOHfrhhx8OHDhw5syZeDweDodvv/32OXPmdHV1zZ8/f+bMme3t7YIgIISu/9GPEKqwABYuXLh79+6DBw8+8MADtk7T3d39zjvvbNu2rZyA5tDq0UfpvtFRo/0qPVAGYkYs3QIkSQoGgx6PR5ZlVVUDgUBXV1ddXd3DDz+8cOHCu+++e8aMGTSfvnz5w2eeebOvbwivbCgEcIvIQnSZInrHFFWmU4uyVF31OBRDqMbmfz737LPPvvnmm7bwOXfu3IoVK372iy/+Y7J9Q7VuM7bZ5KweFiCErHcZFoYtbGHYGpO/sQ17U1i2MGwNM9vCsDVsYdi6dU+djb3FGSPQmTaEEPiKgZ1xS3WZ3M3TYolWKpXq6ur66aefEokE5y+Hxd66kKqqtbW1W7duraurKzmLLOqWTrx1dHQcP358586dhw4dunbtWiQSoYcZxeUv9s0OwGqz2fZAINCazWZDodCePXs2bdp0/vz5wf8M/q3QEYvF7r///s8++2zNmjXlxbJoQmiOAo7jescdXV1dXV1dUvCGJMmqqhVOY+B1TVOUTDQajUQidMuOvgY1Go1eunSpt7f3xhfBAl4QBLfb3dbWNnfuXJcrkM2SUEjNZAb46vCw7UYZ6DlkuZlKpaLRKN0NphuwiUQilUrRjdhMJiPLMj09lG7L0udOVFWVpolUKhWLxX7//ffz588fP3786tWrgAOcc4aosrLZrNvtnj17tsfzoN//XE3NQcDtZDxVMxCCeJ6SJfI8ouwHAoHW1laEkKqqkiTJ+g/dJyQAMpmMpmnG8ydkhGiaJkmSpmm6rhfevKF+ZTIZmn/Rj5woivRTPM+XdLiwJHK5XJqmXblyJS5qmYwqCIFQSMHYFwrpAKFQSBSCTqen+GlNYn72G31S/O7du2trazdu3Lht2zav12u1xWx8TjUe7+jo6O7unmTUxeO9oZBPEFprampPnz59cWAwEAjiKmIcaXdQHBs+Px/w+7xu97xw2F0mIUW7wTbRxQIw0VWcLcyMDVsYtjBsYdjCsIVhC8MWhi0MWxi2MGxh2MKwhWELw9Yw1n8CDAA+JuJxGAh5aQAAAABJRU5ErkJggg==");
            logo = new Image(ImageDataFactory.create(placeholder));
        }
        
        logo.setWidth(100);
        Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
        logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        logoCell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headerTable.addCell(logoCell);

        document.add(headerTable);
    }

    /**
     * Create the document info table (left column of header)
     */
    private Table createDocumentInfoTable(InspectionForm form, PdfFont fontBold) {
        Table docInfoTable = new Table(UnitValue.createPercentArray(new float[]{60, 60}))
                .setWidth(UnitValue.createPercentValue(120));

        // Row 1: Document No.
        addInfoRow(docInfoTable, "Document No. :", form.getDocumentNo(), fontBold);
        
        // Row 2: Issuance No.
        addInfoRow(docInfoTable, "Issuance No. :", form.getIssuanceNo(), fontBold);
        
        // Row 3: Date of Issue
        String issueDate = form.getIssueDate() != null ? form.getIssueDate().format(DATE_FORMATTER) : "";
        addInfoRow(docInfoTable, "Date of Issue :", issueDate, fontBold);
        
        // Row 4: Reviewed by
        String reviewedDate = form.getReviewedDate() != null ? form.getReviewedDate().format(DATE_FORMATTER) : "";
        addInfoRow(docInfoTable, "Reviewed by :", reviewedDate, fontBold);
        
        // Row 5: Page
        addInfoRow(docInfoTable, "Page :", form.getPage(), fontBold);
        
        // Row 6: Prepared By
        addInfoRow(docInfoTable, "Prepared By :", form.getPreparedBy(), fontBold);
        
        // Row 7: Approved by
        addInfoRow(docInfoTable, "Approved by :", form.getApprovedBy(), fontBold);
        
        // Row 8: Issued
        addInfoRow(docInfoTable, "Issued :", form.getIssued(), fontBold);

        return docInfoTable;
    }

    /**
     * Create the title table (middle column of header)
     */
    private Table createTitleTable(PdfFont fontBold) {
        Table titleTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
        		.setMarginLeft(20);
        // Company name
        Paragraph companyName = new Paragraph("AGI Greenpac Limited")
                .setFont(fontBold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        titleTable.addCell(new Cell().add(companyName).setBorder(Border.NO_BORDER));

        // Unit name
        Paragraph unitName = new Paragraph("Unit :- AGI Speciality Glas Division")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        titleTable.addCell(new Cell().add(unitName).setBorder(Border.NO_BORDER));

        // Empty space
        titleTable.addCell(new Cell().setHeight(30).setBorder(Border.NO_BORDER));

        // Scope - Modified to match image styling with color
        Paragraph scope = new Paragraph();
        scope.add(new Text("SCOPE : ").setFont(fontBold));
        scope.add(new Text("AGI / DEC / COATING").setFont(fontBold).setFontColor(ColorConstants.DARK_GRAY));
        scope.setTextAlignment(TextAlignment.CENTER);
        scope.setFontSize(10);
        titleTable.addCell(new Cell().add(scope).setBorder(Border.NO_BORDER));

        // Title - Modified to match image styling with color
        Paragraph title = new Paragraph();
        title.add(new Text("TITLE : ").setFont(fontBold));
        title.add(new Text("FIRST ARTICLE INSPECTION REPORT - COATING").setFont(fontBold).setFontColor(ColorConstants.DARK_GRAY));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFontSize(10);
        titleTable.addCell(new Cell().add(title).setBorder(Border.NO_BORDER));

        return titleTable;
    }

    // Update the addHeaderInfo method for the inspection details to match the style in your image
    private void addHeaderInfo(Document document, InspectionForm form, PdfFont font, PdfFont fontBold) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{33.3f, 33.3f, 33.3f}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(SOLID_BORDER)
                .setBorderBottom(SOLID_BORDER);

        // Column 1
        Table col1 = new Table(2).setWidth(UnitValue.createPercentValue(100));
        col1.addCell(new Cell().add(new Paragraph("Date:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col1.addCell(new Cell().add(new Paragraph(form.getInspectionDate() != null ? form.getInspectionDate().format(DATE_FORMATTER) : "")).setBorder(Border.NO_BORDER));
        
        col1.addCell(new Cell().add(new Paragraph("Product:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col1.addCell(new Cell().add(new Paragraph(form.getProduct())).setBorder(Border.NO_BORDER));
        
        col1.addCell(new Cell().add(new Paragraph("Size No.:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col1.addCell(new Cell().add(new Paragraph(form.getSizeNo())).setBorder(Border.NO_BORDER));
        
        // Add col1 to main table with right border only
        Cell col1Cell = new Cell().add(col1);
        col1Cell.setBorderRight(SOLID_BORDER);
        col1Cell.setBorderLeft(Border.NO_BORDER);
        col1Cell.setBorderTop(Border.NO_BORDER);
        col1Cell.setBorderBottom(Border.NO_BORDER);
        infoTable.addCell(col1Cell);

        // Column 2
        Table col2 = new Table(2).setWidth(UnitValue.createPercentValue(100));
        col2.addCell(new Cell().add(new Paragraph("Shift:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col2.addCell(new Cell().add(new Paragraph(form.getShift())).setBorder(Border.NO_BORDER));
        
        col2.addCell(new Cell().add(new Paragraph("Variant:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col2.addCell(new Cell().add(new Paragraph(form.getVariant())).setBorder(Border.NO_BORDER));
        
        // Add col2 to main table with right border only
        Cell col2Cell = new Cell().add(col2);
        col2Cell.setBorderRight(SOLID_BORDER);
        col2Cell.setBorderLeft(Border.NO_BORDER);
        col2Cell.setBorderTop(Border.NO_BORDER);
        col2Cell.setBorderBottom(Border.NO_BORDER);
        infoTable.addCell(col2Cell);

        // Column 3
        Table col3 = new Table(2).setWidth(UnitValue.createPercentValue(100));
        col3.addCell(new Cell().add(new Paragraph("Line No.:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col3.addCell(new Cell().add(new Paragraph(form.getLineNo())).setBorder(Border.NO_BORDER));
        
        col3.addCell(new Cell().add(new Paragraph("Customer:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col3.addCell(new Cell().add(new Paragraph(form.getCustomer())).setBorder(Border.NO_BORDER));
        
        col3.addCell(new Cell().add(new Paragraph("Sample Size:").setFont(fontBold)).setBorder(Border.NO_BORDER));
        col3.addCell(new Cell().add(new Paragraph(form.getSampleSize())).setBorder(Border.NO_BORDER));
        
        // Add col3 to main table with no right border
        Cell col3Cell = new Cell().add(col3);
        col3Cell.setBorderRight(Border.NO_BORDER);
        col3Cell.setBorderLeft(Border.NO_BORDER);
        col3Cell.setBorderTop(Border.NO_BORDER);
        col3Cell.setBorderBottom(Border.NO_BORDER);
        infoTable.addCell(col3Cell);

        document.add(infoTable);
    }

    /**
     * Add lacquer table to the document
     */
    private void addLacquerTable(Document document, InspectionForm form, PdfFont font, PdfFont fontBold) {
        // Add some spacing
        document.add(new Paragraph("\n"));

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 30, 15, 25, 22}))
                .setWidth(UnitValue.createPercentValue(100));

        // Add Headers
        addTableHeader(table, "S.No.", fontBold);
        addTableHeader(table, "Lacquer / Dye", fontBold);
        addTableHeader(table, "wt.", fontBold);
        addTableHeader(table, "Batch No.", fontBold);
        addTableHeader(table, "Expiry Date", fontBold);

        // Add Rows
        if (form.getLacquers() != null) {
            for (Lacquer lacquer : form.getLacquers()) {
                if (lacquer.getName() == null || lacquer.getName().isEmpty()) {
                    continue; // Skip empty rows
                }
                
                // S.No.
                table.addCell(new Cell().add(new Paragraph(String.valueOf(lacquer.getId())))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(SOLID_BORDER));
                
                // Lacquer/Dye
                table.addCell(new Cell().add(new Paragraph(lacquer.getName()))
                        .setBorder(SOLID_BORDER));
                
                // Weight
                String unit = "Clear Extn".equals(lacquer.getName()) ? "kg" : "gm";
                table.addCell(new Cell().add(new Paragraph(lacquer.getWeight() + " " + unit))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(SOLID_BORDER));
                
                // Batch No.
                table.addCell(new Cell().add(new Paragraph(lacquer.getBatchNo()))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(SOLID_BORDER));
                
             // Expiry Date
                String expiryDate = "";
                if (lacquer.getExpiryDate() != null) {
                    expiryDate = lacquer.getExpiryDate().format(DATE_FORMATTER);
                }
                table.addCell(new Cell().add(new Paragraph(expiryDate))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(SOLID_BORDER));
            }
        }

        document.add(table);
    }

    /**
     * Add characteristics table to the document
     */
    private void addCharacteristicsTable(Document document, InspectionForm form, PdfFont font, PdfFont fontBold) {
        // Add some spacing
        document.add(new Paragraph("\n"));

        // Create table
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 25, 42, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        // Add Headers
        addTableHeader(table, "S.No.", fontBold);
        addTableHeader(table, "Characteristic", fontBold);
        
        Cell obsHeader = new Cell(1, 1)
                .setBorder(SOLID_BORDER)
                .setBackgroundColor(HEADER_BG_COLOR)
                .setPadding(5);
        
        Paragraph obsHeaderText = new Paragraph()
                .add(new Text("As per Reference sample no. X-211\n").setFont(fontBold))
                .add(new Text("Observations").setFont(fontBold));
        obsHeader.add(obsHeaderText);
        table.addHeaderCell(obsHeader);
        
        addTableHeader(table, "Comments", fontBold);

        // Add Rows
        if (form.getCharacteristics() != null) {
            for (Characteristic characteristic : form.getCharacteristics()) {
                if (characteristic.getName() == null || characteristic.getName().isEmpty()) {
                    continue; // Skip empty rows
                }
                
                // S.No.
                table.addCell(new Cell().add(new Paragraph(String.valueOf(characteristic.getId())))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBorder(SOLID_BORDER));
                
                // Characteristic
                table.addCell(new Cell().add(new Paragraph(characteristic.getName()))
                        .setBorder(SOLID_BORDER));
                
                // Observations
                if (characteristic.getId() == 6 && "Coating Thickness".equals(characteristic.getName())) {
                    // Special case for coating thickness with Body and Bottom
                    Table thicknessTable = new Table(2);
                    
                    // Body row
                    Cell bodyLabelCell = new Cell().add(new Paragraph("Body")
                            .setFont(fontBold))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorderRight(SOLID_BORDER)
                            .setBorderBottom(SOLID_BORDER);
                    thicknessTable.addCell(bodyLabelCell);
                    
                    Cell bodyValueCell = new Cell().add(new Paragraph(characteristic.getBodyThickness()))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorderBottom(SOLID_BORDER);
                    thicknessTable.addCell(bodyValueCell);
                    
                    // Bottom row
                    Cell bottomLabelCell = new Cell().add(new Paragraph("Bottom")
                            .setFont(fontBold))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBorderRight(SOLID_BORDER);
                    thicknessTable.addCell(bottomLabelCell);
                    
                    Cell bottomValueCell = new Cell().add(new Paragraph(characteristic.getBottomThickness()))
                            .setTextAlignment(TextAlignment.CENTER);
                    thicknessTable.addCell(bottomValueCell);
                    
                    table.addCell(new Cell().add(thicknessTable)
                            .setBorder(SOLID_BORDER));
                } else {
                    table.addCell(new Cell().add(new Paragraph(characteristic.getObservation()))
                            .setBorder(SOLID_BORDER));
                }
                
                // Comments
                table.addCell(new Cell().add(new Paragraph(characteristic.getComments()))
                        .setBorder(SOLID_BORDER));
            }
        }

        document.add(table);
    }

    /**
     * Add signature section to the document
     */
    private void addSignatureSection(Document document, InspectionForm form, PdfFont font, PdfFont fontBold) throws IOException {
        // Add spacing
        document.add(new Paragraph("\n"));

        // Create a table for signatures and final approval time
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(SOLID_BORDER);

        // QA Signature (Left side)
        Cell qaCell = new Cell();
        Paragraph qaLabel = new Paragraph("QA Exe.:").setFont(fontBold);
        qaCell.add(qaLabel);
        
        // Add QA signature image if available
        if (form.getQaSignature() != null && !form.getQaSignature().isEmpty()) {
            try {
                // Load signature image
                ClassPathResource resource = new ClassPathResource("static/images/QASign.png");
                Image qaSignatureImg = new Image(ImageDataFactory.create(resource.getInputStream().readAllBytes()));
                qaSignatureImg.setWidth(60);  // Set appropriate width
                qaSignatureImg.setHeight(30); // Set appropriate height
                qaCell.add(qaSignatureImg);
            } catch (Exception e) {
                // Fallback if image can't be loaded
                qaCell.add(new Paragraph(form.getQaExecutive() + " (signed)"));
            }
        } else {
            qaCell.add(new Paragraph("______________________"));
        }

        // Operator Signature (Right side)
        Cell operatorCell = new Cell();
        Paragraph opLabel = new Paragraph("Production Sup. / Operator:").setFont(fontBold);
        operatorCell.add(opLabel);
        
        // Add Operator signature image if available
        if (form.getOperatorSignature() != null && !form.getOperatorSignature().isEmpty()) {
            try {
                // Load signature image
                ClassPathResource resource = new ClassPathResource("static/images/OperatorSign.png");
                Image opSignatureImg = new Image(ImageDataFactory.create(resource.getInputStream().readAllBytes()));
                opSignatureImg.setWidth(60);  // Set appropriate width
                opSignatureImg.setHeight(30); // Set appropriate height
                operatorCell.add(opSignatureImg);
            } catch (Exception e) {
                // Fallback if image can't be loaded
                operatorCell.add(new Paragraph(form.getProductionOperator() + " (signed)"));
            }
        } else {
            operatorCell.add(new Paragraph("______________________"));
        }

        // Add signature cells
        signatureTable.addCell(qaCell);
        signatureTable.addCell(operatorCell);

        // Final Approval Time (spans full width)
        Paragraph finalApproval = new Paragraph();
        finalApproval.add(new Text("Time (Final Approval) : ").setFont(fontBold));
        finalApproval.add(new Text(form.getFinalApprovalTime()));
        Cell finalApprovalCell = new Cell(1, 2)
                .add(finalApproval)
                .setBorderTop(SOLID_BORDER);
        signatureTable.addCell(finalApprovalCell);

        document.add(signatureTable);
    }

    /**
     * Add review information section to the document
     */
    private void addReviewInfo(Document document, InspectionForm form, PdfFont font, PdfFont fontBold) {
        // Only add review info for submitted, approved, or rejected forms
        if (form.getStatus() != InspectionForm.FormStatus.SUBMITTED && 
            form.getStatus() != InspectionForm.FormStatus.APPROVED && 
            form.getStatus() != InspectionForm.FormStatus.REJECTED) {
            return;
        }

        // Add spacing
        document.add(new Paragraph("\n"));

        // Create a review info table
        Table reviewTable = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(SOLID_BORDER);

        // Review Information header
        Cell headerCell = new Cell()
                .add(new Paragraph("Review Information").setFont(fontBold))
                .setBackgroundColor(HEADER_BG_COLOR);
        reviewTable.addCell(headerCell);

        // Submission info
        if (form.getSubmittedBy() != null && !form.getSubmittedBy().isEmpty()) {
            Cell submittedCell = new Cell();
            Paragraph submittedText = new Paragraph();
            submittedText.add(new Text("Submitted by: ").setFont(fontBold));
            submittedText.add(new Text(form.getSubmittedBy()));
            
            if (form.getSubmittedAt() != null) {
                submittedText.add(new Text(" on " + form.getSubmittedAt().format(DATETIME_FORMATTER)));
            }
            
            submittedCell.add(submittedText);
            reviewTable.addCell(submittedCell);
        }

        // Review info
        if (form.getReviewedBy() != null && !form.getReviewedBy().isEmpty()) {
            Cell reviewedCell = new Cell();
            Paragraph reviewedText = new Paragraph();
            reviewedText.add(new Text("Reviewed by: ").setFont(fontBold));
            reviewedText.add(new Text(form.getReviewedBy()));
            
            if (form.getReviewedAt() != null) {
                reviewedText.add(new Text(" on " + form.getReviewedAt().format(DATETIME_FORMATTER)));
            }
            
            reviewedCell.add(reviewedText);
            reviewTable.addCell(reviewedCell);
        }

        // Comments
        if (form.getComments() != null && !form.getComments().isEmpty()) {
            Cell commentsCell = new Cell();
            Paragraph commentsHeader = new Paragraph("Comments:").setFont(fontBold);
            Paragraph commentsText = new Paragraph(form.getComments());
            
            commentsCell.add(commentsHeader);
            commentsCell.add(commentsText);
            reviewTable.addCell(commentsCell);
        }

        document.add(reviewTable);
    }

    /**
     * Helper method to add a table header cell
     */
    private void addTableHeader(Table table, String text, PdfFont fontBold) {
        table.addHeaderCell(
            new Cell()
                .add(new Paragraph(text).setFont(fontBold))
                .setBackgroundColor(HEADER_BG_COLOR)
                .setBorder(SOLID_BORDER)
                .setPadding(5)
        );
    }

    /**
     * Helper method to add info row to a table
     */
    private void addInfoRow(Table table, String label, String value, PdfFont fontBold) {
        table.addCell(
            new Cell()
                .add(new Paragraph(label).setFont(fontBold))
                .setBorderRight(SOLID_BORDER)
                .setBorderBottom(SOLID_BORDER)
        );
        
        table.addCell(
            new Cell()
                .add(new Paragraph(value != null ? value : ""))
                .setBorderBottom(SOLID_BORDER)
        );
    }
}