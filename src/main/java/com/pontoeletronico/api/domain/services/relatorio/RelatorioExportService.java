package com.pontoeletronico.api.domain.services.relatorio;

import com.pontoeletronico.api.domain.enums.FormatoRelatorio;
import com.pontoeletronico.api.infrastructure.input.dto.relatorio.RelatorioPontoDetalhadoDto;
import com.pontoeletronico.api.infrastructure.input.dto.relatorio.RelatorioPontoResumoDto;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Gera relatório em PDF ou Excel com tabelas formatadas e comprime a saída (GZIP).
 */
@Service
public class RelatorioExportService {

    private static final com.lowagie.text.Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final com.lowagie.text.Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final com.lowagie.text.Font FONT_CELL = FontFactory.getFont(FontFactory.HELVETICA, 8);

    /**
     * Gera o arquivo do relatório (PDF ou Excel), comprime com GZIP e retorna os bytes.
     */
    public byte[] exportarDetalhado(RelatorioPontoDetalhadoDto dados, FormatoRelatorio formato) throws IOException {
        byte[] raw = formato == FormatoRelatorio.PDF
                ? gerarPdfDetalhado(dados)
                : gerarExcelDetalhado(dados);
        return comprimir(raw);
    }

    public byte[] exportarResumo(RelatorioPontoResumoDto dados, FormatoRelatorio formato) throws IOException {
        byte[] raw = formato == FormatoRelatorio.PDF
                ? gerarPdfResumo(dados)
                : gerarExcelResumo(dados);
        return comprimir(raw);
    }

    public String contentType(FormatoRelatorio formato) {
        return formato == FormatoRelatorio.PDF
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    public String extensaoArquivo(FormatoRelatorio formato) {
        return formato == FormatoRelatorio.PDF ? "pdf" : "xlsx";
    }

    private byte[] comprimir(byte[] raw) throws IOException {
        var out = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(out)) {
            gzip.write(raw);
        }
        return out.toByteArray();
    }

    private byte[] gerarPdfDetalhado(RelatorioPontoDetalhadoDto dto) throws IOException {
        var out = new ByteArrayOutputStream();
        var doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("Relatório de Ponto Detalhado", FONT_TITLE));
        doc.add(new Paragraph("Período: " + dto.periodo().inicio() + " a " + dto.periodo().fim(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(Chunk.NEWLINE);

        for (var func : dto.funcionarios()) {
            doc.add(new Paragraph(func.nome() + " — Jornada prevista: " + func.jornadaPrevistaDia(), FONT_HEADER));
            var table = new PdfPTable(10);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(4f);
            table.setSpacingAfter(8f);
            String[] headers = { "Data", "Dia", "Entrada 1", "Saída 1", "Entrada 2", "Saída 2", "Horas Dia", "Extras", "Falta", "Ocorrência" };
            for (String h : headers) {
                var cell = new PdfPCell(new Phrase(h, FONT_HEADER));
                cell.setBackgroundColor(new java.awt.Color(0xE8, 0xE8, 0xE8));
                table.addCell(cell);
            }
            for (var reg : func.registros()) {
                table.addCell(cell(reg.data().toString()));
                table.addCell(cell(reg.diaSemana()));
                table.addCell(cell(reg.entrada1()));
                table.addCell(cell(reg.saida1()));
                table.addCell(cell(reg.entrada2()));
                table.addCell(cell(reg.saida2()));
                table.addCell(cell(reg.horasDia()));
                table.addCell(cell(reg.extrasDia()));
                table.addCell(cell(reg.faltaDia()));
                table.addCell(cell(reg.ocorrencia()));
            }
            doc.add(table);
        }

        doc.close();
        return out.toByteArray();
    }

    private byte[] gerarPdfResumo(RelatorioPontoResumoDto dto) throws IOException {
        var out = new ByteArrayOutputStream();
        var doc = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("Relatório de Ponto Resumo", FONT_TITLE));
        doc.add(new Paragraph("Período: " + dto.periodo(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        doc.add(Chunk.NEWLINE);

        var table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        String[] headers = { "Funcionário", "Nome", "Total Horas Esperadas", "Total Horas Trabalhadas", "Total Banco Horas Final", "Status" };
        for (String h : headers) {
            var cell = new PdfPCell(new Phrase(h, FONT_HEADER));
            cell.setBackgroundColor(new java.awt.Color(0xE8, 0xE8, 0xE8));
            table.addCell(cell);
        }
        for (var f : dto.lista()) {
            table.addCell(cell(f.funcionarioId()));
            table.addCell(cell(f.nome()));
            table.addCell(cell(f.totalHorasEsperadas()));
            table.addCell(cell(f.totalHorasTrabalhadas()));
            table.addCell(cell(f.totalBancoHorasFinal()));
            table.addCell(cell(f.status()));
        }
        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    private PdfPCell cell(String text) {
        var cell = new PdfPCell(new Phrase(text != null ? text : "-", FONT_CELL));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private byte[] gerarExcelDetalhado(RelatorioPontoDetalhadoDto dto) throws IOException {
        try (var wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("Ponto Detalhado");
            var headerStyle = estiloCabecalho(wb);
            var cellStyle = estiloCelda(wb);

            int rowNum = 0;
            criarLinha(sheet, rowNum++, new String[]{ "Relatório de Ponto Detalhado", "Período: " + dto.periodo().inicio() + " a " + dto.periodo().fim() }, headerStyle);
            rowNum++;

            for (var func : dto.funcionarios()) {
                criarLinha(sheet, rowNum++, new String[]{ func.nome(), "Jornada: " + func.jornadaPrevistaDia() }, headerStyle);
                criarLinha(sheet, rowNum++, new String[]{ "Data", "Dia", "Entrada 1", "Saída 1", "Entrada 2", "Saída 2", "Horas Dia", "Extras", "Falta", "Ocorrência" }, headerStyle);
                for (var reg : func.registros()) {
                    criarLinha(sheet, rowNum++, new String[]{
                            reg.data().toString(), reg.diaSemana(),
                            reg.entrada1(), reg.saida1(), reg.entrada2(), reg.saida2(),
                            reg.horasDia(), reg.extrasDia(), reg.faltaDia(), reg.ocorrencia()
                    }, cellStyle);
                }
                rowNum++;
            }

            for (int i = 0; i < 10; i++) sheet.autoSizeColumn(i);
            return workbookToBytes(wb);
        }
    }

    private byte[] gerarExcelResumo(RelatorioPontoResumoDto dto) throws IOException {
        try (var wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet("Ponto Resumo");
            var headerStyle = estiloCabecalho(wb);
            var cellStyle = estiloCelda(wb);

            int rowNum = 0;
            criarLinha(sheet, rowNum++, new String[]{ "Relatório de Ponto Resumo", "Período: " + dto.periodo() }, headerStyle);
            rowNum++;
            criarLinha(sheet, rowNum++, new String[]{ "Funcionário (ID)", "Nome", "Total Horas Esperadas", "Total Horas Trabalhadas", "Total Banco Horas Final", "Status" }, headerStyle);
            for (var f : dto.lista()) {
                criarLinha(sheet, rowNum++, new String[]{
                        f.funcionarioId(), f.nome(), f.totalHorasEsperadas(), f.totalHorasTrabalhadas(),
                        f.totalBancoHorasFinal(), f.status()
                }, cellStyle);
            }
            for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);
            return workbookToBytes(wb);
        }
    }

    private CellStyle estiloCabecalho(Workbook wb) {
        var style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle estiloCelda(Workbook wb) {
        var style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void criarLinha(Sheet sheet, int rowNum, String[] valores, CellStyle style) {
        var row = sheet.createRow(rowNum);
        for (int i = 0; i < valores.length; i++) {
            var cell = row.createCell(i);
            cell.setCellValue(valores[i] != null ? valores[i] : "-");
            cell.setCellStyle(style);
        }
    }

    private byte[] workbookToBytes(Workbook wb) throws IOException {
        var out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
