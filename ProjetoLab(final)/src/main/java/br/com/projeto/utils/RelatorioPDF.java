package br.com.projeto.utils;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;

/**
 * ================================================================
 * RELATORIO PDF OFICIAL - Formato Receita Federal
 * ================================================================
 *
 * PROPÓSITO:
 * Gera relatórios mensais em PDF no formato OFICIAL da Receita Federal.
 * Modelo: "Relatório Mensal das Receitas Brutas" (DASN-SIMEI).
 *
 * DIFERENÇAS DO FORMATO ANTERIOR:
 * - Segue layout oficial da RF
 * - 3 seções separadas (Revenda, Industrial, Serviços)
 * - Subtotais (III, VI, IX)
 * - Total geral (X)
 * - Campos de assinatura
 * - SEM detalhamento de vendas
 *
 * BIBLIOTECA: iText 5.x (com.itextpdf)
 * FORMATO: PDF A4 (210x297mm)
 *
 * @author Sistema MEI
 * @version 3.0 (Formato Oficial RF)
 */
public class RelatorioPDF {

    // ========== ESTILOS DE FONTE ==========
    private static final Font TITULO = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITULO = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font NEGRITO = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font PEQUENO = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

    // ========== FORMATADORES ==========
    private DecimalFormat moeda = new DecimalFormat("R$ #,##0.00");
    private SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * ================================================================
     * CALCULAR TOTAIS NO FORMATO OFICIAL DA RF
     * ================================================================
     *
     * Calcula valores para as linhas I a VIII do formulário oficial,
     * mais a categoria "Outro" (XI e XII).
     *
     * Array de retorno (8 posições):
     * [0] I    - Revenda sem NF (com dispensa de emissão)
     * [1] II   - Revenda com NF (com documento fiscal emitido)
     * [2] IV   - Industrializados sem NF
     * [3] V    - Industrializados com NF
     * [4] VII  - Serviços sem NF
     * [5] VIII - Serviços com NF
     * [6] XI   - Outro sem NF
     * [7] XII  - Outro com NF
     *
     * @param vendas Lista de vendas do mês
     * @return Array com 8 valores (I, II, IV, V, VII, VIII, XI, XII)
     */
    public double[] calcularTotais(List<Vendas> vendas) {
        double[] totais = new double[8];

        for (Vendas v : vendas) {
            String categoria = v.getNomeCategoria().toLowerCase();
            boolean comNF = "S".equalsIgnoreCase(v.getNotaFiscalEmitida());
            double valor = v.getValor();

            // I/II - Revenda de Mercadorias
            if (categoria.contains("revenda") || categoria.contains("mercadoria")) {
                totais[comNF ? 1 : 0] += valor;
            }
            // IV/V - Produtos Industrializados
            else if (categoria.contains("industrial") || categoria.contains("produto")) {
                totais[comNF ? 3 : 2] += valor;
            }
            // VII/VIII - Prestação de Serviços
            else if (categoria.contains("servi")) {
                totais[comNF ? 5 : 4] += valor;
            }
            // XI/XII - Outro
            else {
                totais[comNF ? 7 : 6] += valor;
            }
        }

        return totais;
    }

    /**
     * ================================================================
     * GERAR RELATÓRIO PDF NO FORMATO OFICIAL DA RF
     * ================================================================
     *
     * Cria PDF conforme modelo oficial da Receita Federal.
     *
     * ESTRUTURA:
     * 1.  Título: RELATÓRIO MENSAL DAS RECEITAS BRUTAS
     * 2.  Cabeçalho: CNPJ, Nome, Período
     * 3.  Seção I:   Revenda de Mercadorias
     * 4.  Seção II:  Produtos Industrializados
     * 5.  Seção III: Prestação de Serviços
     * 6.  Seção IV:  Outro
     * 7.  Total Geral (XIV)
     * 8.  Campos de assinatura
     * 9.  Observação final
     * 10. Detalhamento das vendas (lista individual)
     *
     * @param usuario MEI
     * @param mes Mês (1-12)
     * @param ano Ano (ex: 2026)
     * @param vendas Lista de vendas
     * @param totais Array de calcularTotais()
     * @return byte[] do PDF
     * @throws Exception se erro ao gerar
     */
    public byte[] gerarRelatorio(Usuario usuario, int mes, int ano,
                                 List<Vendas> vendas, double[] totais) throws Exception {

        // Criar documento em memória
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(documento, baos);
        documento.open();

        // 1. TÍTULO PRINCIPAL
        adicionarTitulo(documento);

        // 2. CABEÇALHO (CNPJ, Nome, Período)
        adicionarCabecalho(documento, usuario, mes, ano);

        // 3. SEÇÃO REVENDA DE MERCADORIAS
        adicionarSecaoRevenda(documento, totais);

        // 4. SEÇÃO PRODUTOS INDUSTRIALIZADOS
        adicionarSecaoProdutos(documento, totais);

        // 5. SEÇÃO PRESTAÇÃO DE SERVIÇOS
        adicionarSecaoServicos(documento, totais);

        // 6. SEÇÃO OUTRO
        adicionarSecaoOutro(documento, totais);

        // 7. TOTAL GERAL
        adicionarTotalGeral(documento, totais);

        // 8. CAMPOS DE ASSINATURA
        adicionarCamposAssinatura(documento);

        // 9. OBSERVAÇÃO FINAL
        adicionarObservacao(documento);

        // 10. DETALHAMENTO DAS VENDAS (lista individual)
        adicionarDetalhamentoVendas(documento, vendas);

        documento.close();
        return baos.toByteArray();
    }

    /**
     * Adiciona título principal do relatório
     */
    private void adicionarTitulo(Document doc) throws DocumentException {
        Paragraph titulo = new Paragraph("RELATÓRIO MENSAL DAS RECEITAS BRUTAS", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        doc.add(titulo);
    }

    /**
     * Adiciona cabeçalho com CNPJ, Nome e Período
     */
    private void adicionarCabecalho(Document doc, Usuario usuario, int mes, int ano)
            throws DocumentException {

        // CNPJ (se houver)
        if (usuario.getCnpj() != null && !usuario.getCnpj().trim().isEmpty()) {
            Paragraph cnpj = new Paragraph("CNPJ: " + formatarCNPJ(usuario.getCnpj()), NORMAL);
            cnpj.setSpacingAfter(5);
            doc.add(cnpj);
        }

        // Nome do empreendedor
        Paragraph nome = new Paragraph("Empreendedor individual: " + usuario.getNome(), NORMAL);
        nome.setSpacingAfter(5);
        doc.add(nome);

        // Período de apuração
        String[] meses = {"", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                          "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        Paragraph periodo = new Paragraph("Período de apuração: " + meses[mes] + "/" + ano, NORMAL);
        periodo.setSpacingAfter(15);
        doc.add(periodo);
    }

    /**
     * Formata CNPJ: 12.345.678/0001-90
     */
    private String formatarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." +
               cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" +
               cnpj.substring(12, 14);
    }

    /**
     * ================================================================
     * SEÇÃO 1: RECEITA BRUTA MENSAL – REVENDA DE MERCADORIAS (COMÉRCIO)
     * ================================================================
     */
    private void adicionarSecaoRevenda(Document doc, double[] totais) throws DocumentException {
        // Título da seção
        Paragraph titulo = new Paragraph(
            "RECEITA BRUTA MENSAL – REVENDA DE MERCADORIAS (COMÉRCIO)",
            SUBTITULO
        );
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // Tabela com 2 colunas
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{75, 25});

        // I - Revenda sem NF
        adicionarLinha(tabela,
            "I – Revenda de mercadorias com dispensa de emissão de documento fiscal",
            totais[0]);

        // II - Revenda com NF
        adicionarLinha(tabela,
            "II – Revenda de mercadorias com documento fiscal emitido",
            totais[1]);

        // III - Total Revenda (I + II)
        double subtotalRevenda = totais[0] + totais[1];
        adicionarLinhaSubtotal(tabela,
            "III – Total das receitas com revenda de mercadorias (I + II)",
            subtotalRevenda);

        doc.add(tabela);
        doc.add(new Paragraph("\n"));
    }

    /**
     * ================================================================
     * SEÇÃO 2: RECEITA BRUTA MENSAL – VENDA DE PRODUTOS INDUSTRIALIZADOS (INDÚSTRIA)
     * ================================================================
     */
    private void adicionarSecaoProdutos(Document doc, double[] totais) throws DocumentException {
        // Título da seção
        Paragraph titulo = new Paragraph(
            "RECEITA BRUTA MENSAL – VENDA DE PRODUTOS INDUSTRIALIZADOS (INDÚSTRIA)",
            SUBTITULO
        );
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // Tabela com 2 colunas
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{75, 25});

        // IV - Produtos sem NF
        adicionarLinha(tabela,
            "IV – Venda de produtos industrializados com dispensa de emissão de documento fiscal",
            totais[2]);

        // V - Produtos com NF
        adicionarLinha(tabela,
            "V – Venda de produtos industrializados com documento fiscal emitido",
            totais[3]);

        // VI - Total Produtos (IV + V)
        double subtotalProdutos = totais[2] + totais[3];
        adicionarLinhaSubtotal(tabela,
            "VI – Total das receitas com venda de produtos industrializados (IV + V)",
            subtotalProdutos);

        doc.add(tabela);
        doc.add(new Paragraph("\n"));
    }

    /**
     * ================================================================
     * SEÇÃO 3: RECEITA BRUTA MENSAL – PRESTAÇÃO DE SERVIÇOS
     * ================================================================
     */
    private void adicionarSecaoServicos(Document doc, double[] totais) throws DocumentException {
        // Título da seção
        Paragraph titulo = new Paragraph(
            "RECEITA BRUTA MENSAL – PRESTAÇÃO DE SERVIÇOS",
            SUBTITULO
        );
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // Tabela com 2 colunas
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{75, 25});

        // VII - Serviços sem NF
        adicionarLinha(tabela,
            "VII – Receita com prestação de serviços com dispensa de emissão de documento fiscal",
            totais[4]);

        // VIII - Serviços com NF
        adicionarLinha(tabela,
            "VIII – Receita com prestação de serviços com documento fiscal emitido",
            totais[5]);

        // IX - Total Serviços (VII + VIII)
        double subtotalServicos = totais[4] + totais[5];
        adicionarLinhaSubtotal(tabela,
            "IX – Total das receitas com prestação de serviços (VII + VIII)",
            subtotalServicos);

        doc.add(tabela);
        doc.add(new Paragraph("\n"));
    }

    /**
     * ================================================================
     * SEÇÃO 4: RECEITA BRUTA MENSAL – OUTRO
     * ================================================================
     */
    private void adicionarSecaoOutro(Document doc, double[] totais) throws DocumentException {
        // Título da seção
        Paragraph titulo = new Paragraph(
            "RECEITA BRUTA MENSAL – OUTRO",
            SUBTITULO
        );
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // Tabela com 2 colunas
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{75, 25});

        // XI - Outro sem NF
        adicionarLinha(tabela,
            "XI – Outras receitas com dispensa de emissão de documento fiscal",
            totais[6]);

        // XII - Outro com NF
        adicionarLinha(tabela,
            "XII – Outras receitas com documento fiscal emitido",
            totais[7]);

        // XIII - Total Outro (XI + XII)
        double subtotalOutro = totais[6] + totais[7];
        adicionarLinhaSubtotal(tabela,
            "XIII – Total das outras receitas (XI + XII)",
            subtotalOutro);

        doc.add(tabela);
        doc.add(new Paragraph("\n"));
    }

    /**
     * ================================================================
     * TOTAL GERAL (XIV = III + VI + IX + XIII)
     * ================================================================
     */
    private void adicionarTotalGeral(Document doc, double[] totais) throws DocumentException {
        // Calcula subtotais
        double III  = totais[0] + totais[1];   // Revenda
        double VI   = totais[2] + totais[3];   // Produtos
        double IX   = totais[4] + totais[5];   // Serviços
        double XIII = totais[6] + totais[7];   // Outro
        double XIV  = III + VI + IX + XIII;    // Total Geral

        // Tabela com 2 colunas
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{75, 25});

        // Linha do total geral (destaque)
        PdfPCell celulaTexto = new PdfPCell(
            new Phrase("XIV - Total geral das receitas brutas no mês (III + VI + IX + XIII)", NEGRITO)
        );
        celulaTexto.setPadding(10);
        celulaTexto.setBackgroundColor(new BaseColor(220, 220, 220));
        celulaTexto.setBorder(Rectangle.BOX);
        tabela.addCell(celulaTexto);

        PdfPCell celulaValor = new PdfPCell(new Phrase(moeda.format(XIV), NEGRITO));
        celulaValor.setPadding(10);
        celulaValor.setBackgroundColor(new BaseColor(220, 220, 220));
        celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaValor.setBorder(Rectangle.BOX);
        tabela.addCell(celulaValor);

        doc.add(tabela);
        doc.add(new Paragraph("\n\n"));
    }

    /**
     * Adiciona campos de assinatura (local, data, assinatura)
     */
    private void adicionarCamposAssinatura(Document doc) throws DocumentException {
        // Tabela com 2 colunas para assinatura
        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{50, 50});

        // LOCAL E DATA
        PdfPCell celulaData = new PdfPCell(new Phrase("LOCAL E DATA: _______________________", NORMAL));
        celulaData.setPadding(15);
        celulaData.setBorder(Rectangle.NO_BORDER);
        tabela.addCell(celulaData);

        // ASSINATURA
        PdfPCell celulaAssinatura = new PdfPCell(
            new Phrase("ASSINATURA DO EMPRESÁRIO: _______________________", NORMAL)
        );
        celulaAssinatura.setPadding(15);
        celulaAssinatura.setBorder(Rectangle.NO_BORDER);
        tabela.addCell(celulaAssinatura);

        doc.add(tabela);
        doc.add(new Paragraph("\n"));
    }

    /**
     * Adiciona observação final
     */
    private void adicionarObservacao(Document doc) throws DocumentException {
        Paragraph obs = new Paragraph(
            "ENCONTRAM-SE ANEXADOS A ESTE RELATÓRIO:\n" +
            "- Os documentos fiscais comprobatórios das entradas de mercadorias e serviços tomados referentes ao período;\n" +
            "- As notas fiscais relativas às operações ou prestações realizadas eventualmente emitidas.",
            PEQUENO
        );
        obs.setSpacingBefore(20);
        doc.add(obs);
    }

    /**
     * ================================================================
     * DETALHAMENTO DAS VENDAS (LISTA INDIVIDUAL)
     * ================================================================
     *
     * Lista todas as vendas do mês em formato tabela, em nova página.
     *
     * Colunas: [Data] [Categoria] [Descrição] [NF] [Número] [Valor]
     *
     * Layout:
     * ┌────────┬───────────┬──────────┬────┬──────────┬──────────┐
     * │ Data   │ Categoria │ Descr.   │ NF │ Número   │ Valor    │
     * ├────────┼───────────┼──────────┼────┼──────────┼──────────┤
     * │ 01/02  │ Produtos  │ Venda X  │ S  │ NF-2026  │ R$ 100   │
     * │ 05/02  │ Serviços  │ Consult. │ N  │ -        │ R$ 500   │
     * └────────┴───────────┴──────────┴────┴──────────┴──────────┘
     *
     * IMPORTANTE: Se não houver vendas, a seção é omitida.
     *
     * @param doc    Documento PDF
     * @param vendas Lista de vendas do mês
     */
    private void adicionarDetalhamentoVendas(Document doc, List<Vendas> vendas)
            throws DocumentException {

        // Se não há vendas, não exibe seção
        if (vendas == null || vendas.isEmpty()) return;

        // ========== NOVA PÁGINA ==========
        // Detalhamento sempre inicia em página nova
        doc.newPage();

        // ========== TÍTULO DA SEÇÃO ==========
        Paragraph titulo = new Paragraph("DETALHAMENTO DAS VENDAS", SUBTITULO);
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        // ========== CRIAR TABELA ==========
        // 6 colunas com larguras proporcionais
        PdfPTable tabela = new PdfPTable(6);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{12, 22, 22, 8, 16, 20});

        // ========== CABEÇALHO ==========
        adicionarCelulaHeader(tabela, "Data");
        adicionarCelulaHeader(tabela, "Categoria");
        adicionarCelulaHeader(tabela, "Descrição");
        adicionarCelulaHeader(tabela, "NF");
        adicionarCelulaHeader(tabela, "Número");
        adicionarCelulaHeader(tabela, "Valor");

        // ========== LINHAS (cada venda) ==========
        for (Vendas v : vendas) {

            // ========== CONVERTER DATA ==========
            // Vendas.dataVendas pode ser Date ou LocalDateTime
            java.util.Date data = null;

            if (v.getDataVendas() instanceof java.util.Date) {
                // Já é Date
                data = (java.util.Date) v.getDataVendas();

            } else if (v.getDataVendas() instanceof java.time.LocalDateTime) {
                // Converter LocalDateTime → Date
                data = java.sql.Timestamp.valueOf((java.time.LocalDateTime) v.getDataVendas());
            }

            // ========== ADICIONAR CÉLULAS ==========
            tabela.addCell(new PdfPCell(new Phrase(
                    data != null ? dataFormat.format(data) : "-", PEQUENO)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNomeCategoria(), PEQUENO)));
            tabela.addCell(new PdfPCell(new Phrase(
                    v.getDescricao() != null ? v.getDescricao() : "-", PEQUENO)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNotaFiscalEmitida(), PEQUENO)));

            // Número da NF (se tiver)
            String numeroNF = "-";
            if (v.getNotaFiscal() != null && v.getNotaFiscal().getNumero() != null) {
                numeroNF = v.getNotaFiscal().getNumero();
            }
            tabela.addCell(new PdfPCell(new Phrase(numeroNF, PEQUENO)));

            // Célula de valor (alinhada à direita)
            PdfPCell celulaValor = new PdfPCell(
                    new Phrase(moeda.format(v.getValor()), PEQUENO));
            celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabela.addCell(celulaValor);
        }

        doc.add(tabela);
    }

    /**
     * Adiciona célula de cabeçalho (azul, centralizada, negrito)
     * Reutilizada no detalhamento de vendas
     */
    private void adicionarCelulaHeader(PdfPTable tabela, String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, NEGRITO));
        celula.setBackgroundColor(new BaseColor(220, 220, 220)); // Cinza padrão OFICIAL
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(7);
        celula.setBorder(Rectangle.BOX);
        tabela.addCell(celula);
    }

    /**
     * Adiciona linha normal (I, II, IV, V, VII, VIII)
     */
    private void adicionarLinha(PdfPTable tabela, String texto, double valor) {
        PdfPCell celulaTexto = new PdfPCell(new Phrase(texto, NORMAL));
        celulaTexto.setPadding(8);
        celulaTexto.setBorder(Rectangle.BOX);
        tabela.addCell(celulaTexto);

        PdfPCell celulaValor = new PdfPCell(new Phrase(moeda.format(valor), NORMAL));
        celulaValor.setPadding(8);
        celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaValor.setBorder(Rectangle.BOX);
        tabela.addCell(celulaValor);
    }

    /**
     * Adiciona linha de subtotal (III, VI, IX) com destaque
     */
    private void adicionarLinhaSubtotal(PdfPTable tabela, String texto, double valor) {
        PdfPCell celulaTexto = new PdfPCell(new Phrase(texto, NEGRITO));
        celulaTexto.setPadding(8);
        celulaTexto.setBackgroundColor(new BaseColor(240, 240, 240));
        celulaTexto.setBorder(Rectangle.BOX);
        tabela.addCell(celulaTexto);

        PdfPCell celulaValor = new PdfPCell(new Phrase(moeda.format(valor), NEGRITO));
        celulaValor.setPadding(8);
        celulaValor.setBackgroundColor(new BaseColor(240, 240, 240));
        celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaValor.setBorder(Rectangle.BOX);
        tabela.addCell(celulaValor);
    }
}