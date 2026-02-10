package br.com.projeto.utils;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;

public class RelatorioPDF {
    
    private static final Font TITULO = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITULO = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font NEGRITO = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    
    private DecimalFormat moeda = new DecimalFormat("R$ #,##0.00");
    private SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Calcula totais por categoria
     * [0] = Revenda com NF
     * [1] = Revenda sem NF
     * [2] = Industrializados com NF
     * [3] = Industrializados sem NF
     * [4] = Serviços com NF
     * [5] = Serviços sem NF
     * [6] = Total Geral
     */
    public double[] calcularTotais(List<Vendas> vendas) {
        double[] totais = new double[7];
        
        for (Vendas v : vendas) {
            String categoria = v.getNomeCategoria().toLowerCase();
            boolean comNF = "S".equalsIgnoreCase(v.getNotaFiscalEmitida());
            
            if (categoria.contains("revenda") || categoria.contains("mercadoria")) {
                totais[comNF ? 0 : 1] += v.getValor();
            } else if (categoria.contains("industrial") || categoria.contains("produto")) {
                totais[comNF ? 2 : 3] += v.getValor();
            } else if (categoria.contains("servi")) {
                totais[comNF ? 4 : 5] += v.getValor();
            }
        }
        
        // Total geral
        for (int i = 0; i < 6; i++) {
            totais[6] += totais[i];
        }
        
        return totais;
    }
    
    /**
     * Gera o relatório PDF completo
     */
    public byte[] gerarRelatorio(Usuario usuario, int mes, int ano, 
                                 List<Vendas> vendas, double[] totais) throws Exception {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
        
        try {
            PdfWriter.getInstance(documento, baos);
            documento.open();
            
            // Cabeçalho
            adicionarCabecalho(documento, usuario, mes, ano);
            documento.add(new Paragraph("\n"));
            
            // Tabela de Receitas
            adicionarTabelaReceitas(documento, totais);
            documento.add(new Paragraph("\n"));
            
            // Detalhamento de Vendas
            adicionarDetalhamentoVendas(documento, vendas);
            documento.add(new Paragraph("\n"));
            
            // Rodapé
            adicionarRodape(documento, mes, ano);
            
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        
        return baos.toByteArray();
    }
    
    private void adicionarCabecalho(Document doc, Usuario usuario, int mes, int ano) 
            throws DocumentException {
        
        Paragraph titulo = new Paragraph("RELATÓRIO MENSAL DE RECEITAS BRUTAS", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);
        
        doc.add(new Paragraph("\n"));
        
        Paragraph subtitulo = new Paragraph(
            "MICROEMPREENDEDOR INDIVIDUAL (MEI)", SUBTITULO);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitulo);
        
        doc.add(new Paragraph("\n"));
        
        // Dados do MEI
        PdfPTable tabelaDados = new PdfPTable(2);
        tabelaDados.setWidthPercentage(100);
        tabelaDados.setWidths(new float[]{30, 70});
        
        adicionarCelulaDados(tabelaDados, "Nome:", usuario.getNome());
        adicionarCelulaDados(tabelaDados, "CPF:", formatarCPF(usuario.getCpf()));
        adicionarCelulaDados(tabelaDados, "Período:", getNomeMes(mes) + "/" + ano);
        
        doc.add(tabelaDados);
    }
    
    private void adicionarTabelaReceitas(Document doc, double[] totais) 
            throws DocumentException {
        
        Paragraph titulo = new Paragraph("RECEITAS DO MÊS", SUBTITULO);
        doc.add(titulo);
        doc.add(new Paragraph("\n"));
        
        PdfPTable tabela = new PdfPTable(3);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{50, 25, 25});
        
        // Cabeçalho
        adicionarCelulaHeader(tabela, "CATEGORIA");
        adicionarCelulaHeader(tabela, "COM NF");
        adicionarCelulaHeader(tabela, "SEM NF");
        
        // Revenda de Mercadorias
        adicionarLinhaCategoria(tabela, "I - Revenda de Mercadorias", 
            totais[0], totais[1]);
        
        // Produtos Industrializados
        adicionarLinhaCategoria(tabela, "II - Produtos Industrializados", 
            totais[2], totais[3]);
        
        // Prestação de Serviços
        adicionarLinhaCategoria(tabela, "III - Prestação de Serviços", 
            totais[4], totais[5]);
        
        // Total
        PdfPCell celulaTotal = new PdfPCell(new Phrase("TOTAL GERAL", NEGRITO));
        celulaTotal.setBackgroundColor(new BaseColor(230, 230, 230));
        celulaTotal.setPadding(8);
        celulaTotal.setColspan(2);
        tabela.addCell(celulaTotal);
        
        PdfPCell celulaValorTotal = new PdfPCell(new Phrase(moeda.format(totais[6]), NEGRITO));
        celulaValorTotal.setBackgroundColor(new BaseColor(230, 230, 230));
        celulaValorTotal.setPadding(8);
        celulaValorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(celulaValorTotal);
        
        doc.add(tabela);
    }
    
    private void adicionarDetalhamentoVendas(Document doc, List<Vendas> vendas) 
            throws DocumentException {
        
        if (vendas.isEmpty()) return;
        
        Paragraph titulo = new Paragraph("DETALHAMENTO DAS VENDAS", SUBTITULO);
        doc.add(titulo);
        doc.add(new Paragraph("\n"));
        
        PdfPTable tabela = new PdfPTable(5);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{15, 30, 20, 15, 20});
        
        // Cabeçalho
        adicionarCelulaHeader(tabela, "Data");
        adicionarCelulaHeader(tabela, "Categoria");
        adicionarCelulaHeader(tabela, "Descrição");
        adicionarCelulaHeader(tabela, "NF");
        adicionarCelulaHeader(tabela, "Valor");
        
        // Linhas
        for (Vendas v : vendas) {
            java.util.Date data = null;
            if (v.getDataVendas() instanceof java.util.Date) {
                data = (java.util.Date) v.getDataVendas();
            } else if (v.getDataVendas() instanceof java.time.LocalDateTime) {
                data = java.sql.Timestamp.valueOf((java.time.LocalDateTime) v.getDataVendas());
            }
            
            tabela.addCell(new PdfPCell(new Phrase(dataFormat.format(data), NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNomeCategoria(), NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(
                v.getDescricao() != null ? v.getDescricao() : "-", NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNotaFiscalEmitida(), NORMAL)));
            
            PdfPCell celulaValor = new PdfPCell(new Phrase(moeda.format(v.getValor()), NORMAL));
            celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabela.addCell(celulaValor);
        }
        
        doc.add(tabela);
    }
    
    private void adicionarRodape(Document doc, int mes, int ano) 
            throws DocumentException {
        
        doc.add(new Paragraph("\n\n"));
        
        Paragraph obs = new Paragraph(
            "Relatório gerado automaticamente pelo Sistema MEI - " +
            "Conforme exigências da Receita Federal do Brasil", 
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
        obs.setAlignment(Element.ALIGN_CENTER);
        doc.add(obs);
        
        Paragraph data = new Paragraph(
            "Emitido em: " + dataFormat.format(new java.util.Date()),
            new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL));
        data.setAlignment(Element.ALIGN_CENTER);
        doc.add(data);
    }
    
    // Métodos auxiliares
    
    private void adicionarCelulaDados(PdfPTable tabela, String titulo, String valor) {
        PdfPCell celulaTitulo = new PdfPCell(new Phrase(titulo, NEGRITO));
        celulaTitulo.setBorder(Rectangle.NO_BORDER);
        celulaTitulo.setPadding(5);
        tabela.addCell(celulaTitulo);
        
        PdfPCell celulaValor = new PdfPCell(new Phrase(valor, NORMAL));
        celulaValor.setBorder(Rectangle.NO_BORDER);
        celulaValor.setPadding(5);
        tabela.addCell(celulaValor);
    }
    
    private void adicionarCelulaHeader(PdfPTable tabela, String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, NEGRITO));
        celula.setBackgroundColor(new BaseColor(66, 133, 244));
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(8);
        tabela.addCell(celula);
    }
    
    private void adicionarLinhaCategoria(PdfPTable tabela, String categoria, 
                                        double comNF, double semNF) {
        PdfPCell celulaCategoria = new PdfPCell(new Phrase(categoria, NEGRITO));
        celulaCategoria.setPadding(8);
        tabela.addCell(celulaCategoria);
        
        PdfPCell celulaComNF = new PdfPCell(new Phrase(moeda.format(comNF), NORMAL));
        celulaComNF.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaComNF.setPadding(8);
        tabela.addCell(celulaComNF);
        
        PdfPCell celulaSemNF = new PdfPCell(new Phrase(moeda.format(semNF), NORMAL));
        celulaSemNF.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaSemNF.setPadding(8);
        tabela.addCell(celulaSemNF);
    }
    
    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0,3) + "." + cpf.substring(3,6) + "." + 
               cpf.substring(6,9) + "-" + cpf.substring(9,11);
    }
    
    private String getNomeMes(int mes) {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                         "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return meses[mes - 1];
    }
}