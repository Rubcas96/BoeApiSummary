package com.paellasoft.BoeApiSummary.mail;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmailSender {
    private static final int LINES_PER_PAGE =50 ;
    @Autowired
    private final JavaMailSender javaMailSender;

    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public void sendEmailWithPdfAttachment(String to, String subject, String text) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);

            String title = "Resumen Boletín Oficial del Estado (" + LocalDate.now() + ")";
            byte[] pdfBytes = createPdfFromText(text, title);

            InputStreamSource pdfResource = new ByteArrayResource(pdfBytes);

            helper.setText("Estimado usuario,\n\nSe adjunta el resumen del BOE en formato PDF.\n\nAtentamente,\nEquipo de BoeApiSummary");

            helper.addAttachment("resumen_boe_"+LocalDate.now(), pdfResource, "application/pdf");

            javaMailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            // Manejo de errores al enviar el correo electrónico
        }
    }

    private byte[] createPdfFromText(String text, String title) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Divide el texto en líneas
            String[] lines = text.split("\\r?\\n");

            // Calcula el número de páginas necesarias
            int totalPages = (int) Math.ceil((double) lines.length / LINES_PER_PAGE);

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText(title);
                    contentStream.endText();

                    // Agregar una línea horizontal después del título
                    contentStream.moveTo(50, 740); // Posición de inicio de la línea
                    contentStream.lineTo(page.getMediaBox().getWidth() - 50, 740); // Posición de fin de la línea
                    contentStream.stroke(); // Dibujar la línea

                    contentStream.setFont(PDType1Font.HELVETICA, 12);

                    // Calcular el índice de inicio y fin de las líneas para esta página
                    int startLineIndex = pageIndex * LINES_PER_PAGE;
                    int endLineIndex = Math.min(startLineIndex + LINES_PER_PAGE, lines.length);

                    float y = 700; // Posición vertical inicial
                    for (int i = startLineIndex; i < endLineIndex; i++) {
                        String line = lines[i];
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y); // Posición horizontal: 50, vertical: y
                        contentStream.showText(line);
                        contentStream.endText();
                        y -= 12; // Espacio vertical entre líneas
                    }

                    // Agregar una línea horizontal al final del resumen
                    contentStream.moveTo(50, y); // Posición de inicio de la línea
                    contentStream.lineTo(page.getMediaBox().getWidth() - 50, y); // Posición de fin de la línea
                    contentStream.stroke(); // Dibujar la línea
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private List<String> splitLine(String line, float maxWidth) throws IOException {
        List<String> subLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        // Dividir la línea en palabras y ajustarlas para que se ajusten dentro del ancho máximo
        String[] words = line.split("\\s+");
        for (String word : words) {
            float currentWidth = PDType1Font.HELVETICA.getStringWidth(currentLine.toString() + word) / 1000 * 12;
            if (currentWidth <= maxWidth) {
                // Si la palabra cabe dentro de la línea actual, agregarla
                currentLine.append(word).append(" ");
            } else {
                // Si la palabra excede el ancho máximo, agregar la línea actual a la lista y comenzar una nueva línea
                subLines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            }
        }

        // Agregar la última línea al resultado
        subLines.add(currentLine.toString());

        return subLines;
    }
}