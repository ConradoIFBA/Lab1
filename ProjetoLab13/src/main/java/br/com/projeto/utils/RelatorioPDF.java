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
 * RELATORIO PDF - Gerador de Relatórios Mensais MEI
 * ================================================================
 *
 * PROPÓSITO:
 * Gera relatórios mensais em PDF conforme exigências da Receita Federal.
 * Usado para declaração anual do MEI (DASN-SIMEI).
 *
 * BIBLIOTECA: iText 5.x (com.itextpdf)
 * FORMATO: PDF A4 (210x297mm)
 *
 * CONTEÚDO DO PDF:
 * 1. Cabeçalho (nome, CPF, período)
 * 2. Tabela de receitas (por categoria + NF)
 * 3. Detalhamento de vendas
 * 4. Rodapé (data emissão)
 *
 * USO:
 * RelatorioPDF rel = new RelatorioPDF();
 * double[] totais = rel.calcularTotais(vendas);
 * byte[] pdf = rel.gerarRelatorio(usuario, mes, ano, vendas, totais);
 *
 * @author Sistema MEI
 * @version 2.0
 */
public class RelatorioPDF {

    // ========== ESTILOS DE FONTE ==========
    // iText Font: FontFamily, size, style

    /** Título principal (18pt, negrito) */
    private static final Font TITULO = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);

    /** Subtítulos (14pt, negrito) */
    private static final Font SUBTITULO = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);

    /** Texto normal (10pt) */
    private static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /** Texto negrito (10pt, bold) */
    private static final Font NEGRITO = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

    // ========== FORMATADORES ==========

    /** Formatador de moeda: R$ 1.234,56 */
    private DecimalFormat moeda = new DecimalFormat("R$ #,##0.00");

    /** Formatador de data: 14/02/2026 */
    private SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * ================================================================
     * CALCULAR TOTAIS POR CATEGORIA + NOTA FISCAL
     * ================================================================
     *
     * Agrupa vendas por:
     * - Categoria (Revenda, Industrializados, Serviços)
     * - Nota Fiscal (Com NF, Sem NF)
     *
     * Array de retorno (7 posições):
     * [0] = Revenda COM NF
     * [1] = Revenda SEM NF
     * [2] = Industrializados COM NF
     * [3] = Industrializados SEM NF
     * [4] = Serviços COM NF
     * [5] = Serviços SEM NF
     * [6] = Total Geral (soma de tudo)
     *
     * IMPORTANTE: Usado para tabela de receitas do relatório.
     *
     * @param vendas Lista de vendas do mês
     * @return Array de 7 doubles com totais
     */
    public double[] calcularTotais(List<Vendas> vendas) {
        // Inicializa array com zeros
        double[] totais = new double[7];

        // Itera cada venda
        for (Vendas v : vendas) {
            // Nome da categoria (minúsculas para comparação)
            String categoria = v.getNomeCategoria().toLowerCase();

            // Tem nota fiscal?
            boolean comNF = "S".equalsIgnoreCase(v.getNotaFiscalEmitida());

            // ========== CLASSIFICAÇÃO POR CATEGORIA ==========

            // REVENDA DE MERCADORIAS
            if (categoria.contains("revenda") || categoria.contains("mercadoria")) {
                totais[comNF ? 0 : 1] += v.getValor();

                // PRODUTOS INDUSTRIALIZADOS
            } else if (categoria.contains("industrial") || categoria.contains("produto")) {
                totais[comNF ? 2 : 3] += v.getValor();

                // PRESTAÇÃO DE SERVIÇOS
            } else if (categoria.contains("servi")) {
                totais[comNF ? 4 : 5] += v.getValor();
            }

            // OBS: Categorias "Outro" não são somadas (não são obrigatórias no DASN)
        }

        // ========== TOTAL GERAL ==========
        // Soma todas as 6 categorias
        for (int i = 0; i < 6; i++) {
            totais[6] += totais[i];
        }

        return totais;
    }

    /**
     * ================================================================
     * GERAR RELATÓRIO PDF COMPLETO
     * ================================================================
     *
     * Cria PDF em memória (ByteArray) para download.
     *
     * ESTRUTURA DO PDF:
     * 1. Cabeçalho: Nome, CPF, Período
     * 2. Tabela de Receitas: Categorias + Com/Sem NF
     * 3. Detalhamento: Lista todas as vendas
     * 4. Rodapé: Data de emissão
     *
     * @param usuario Dono do relatório (MEI)
     * @param mes Mês do relatório (1-12)
     * @param ano Ano do relatório (ex: 2026)
     * @param vendas Lista de vendas do período
     * @param totais Array de totais (de calcularTotais())
     * @return byte[] do PDF pronto para download
     * @throws Exception se erro ao gerar PDF
     */
    public byte[] gerarRelatorio(Usuario usuario, int mes, int ano,
                                 List<Vendas> vendas, double[] totais) throws Exception {

        // ========== CRIAR PDF EM MEMÓRIA ==========
        // ByteArrayOutputStream = stream de bytes em RAM
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Document = objeto PDF do iText
        // PageSize.A4 = 210x297mm
        // Margens: 50pt (esquerda, direita, topo, rodapé)
        Document documento = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            // ========== VINCULAR WRITER AO DOCUMENTO ==========
            // PdfWriter escreve o PDF no stream
            PdfWriter.getInstance(documento, baos);

            // ========== ABRIR DOCUMENTO ==========
            // A partir daqui pode adicionar conteúdo
            documento.open();

            // ========== SEÇÃO 1: CABEÇALHO ==========
            adicionarCabecalho(documento, usuario, mes, ano);
            documento.add(new Paragraph("\n")); // Espaço

            // ========== SEÇÃO 2: TABELA DE RECEITAS ==========
            adicionarTabelaReceitas(documento, totais);
            documento.add(new Paragraph("\n")); // Espaço

            // ========== SEÇÃO 3: DETALHAMENTO DE VENDAS ==========
            adicionarDetalhamentoVendas(documento, vendas);
            documento.add(new Paragraph("\n")); // Espaço

            // ========== SEÇÃO 4: RODAPÉ ==========
            adicionarRodape(documento, mes, ano);

        } finally {
            // ========== FECHAR DOCUMENTO ==========
            // SEMPRE fechar, mesmo se der erro
            if (documento.isOpen()) {
                documento.close();
            }
        }

        // ========== RETORNAR BYTES DO PDF ==========
        // Converte stream para array de bytes
        return baos.toByteArray();
    }

    /**
     * ================================================================
     * ADICIONAR CABEÇALHO DO RELATÓRIO
     * ================================================================
     *
     * Título + Dados do MEI
     *
     * Layout:
     * ┌─────────────────────────────────────┐
     * │   RELATÓRIO MENSAL DE RECEITAS BRUTAS │
     * │   MICROEMPREENDEDOR INDIVIDUAL (MEI)  │
     * │                                       │
     * │   Nome:     João Silva                │
     * │   CPF:      123.456.789-01            │
     * │   Período:  Fevereiro/2026            │
     * └─────────────────────────────────────┘
     */
    private void adicionarCabecalho(Document doc, Usuario usuario, int mes, int ano)
            throws DocumentException {

        // ========== TÍTULO PRINCIPAL ==========
        Paragraph titulo = new Paragraph("RELATÓRIO MENSAL DE RECEITAS BRUTAS", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER); // Centralizado
        doc.add(titulo);

        doc.add(new Paragraph("\n")); // Espaço

        // ========== SUBTÍTULO ==========
        Paragraph subtitulo = new Paragraph(
                "MICROEMPREENDEDOR INDIVIDUAL (MEI)", SUBTITULO);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(subtitulo);

        doc.add(new Paragraph("\n")); // Espaço

        // ========== TABELA DE DADOS DO MEI ==========
        // 2 colunas: [Label] [Valor]
        PdfPTable tabelaDados = new PdfPTable(2);
        tabelaDados.setWidthPercentage(100); // 100% da largura
        tabelaDados.setWidths(new float[]{30, 70}); // 30% label, 70% valor

        // Adiciona linhas
        adicionarCelulaDados(tabelaDados, "Nome:", usuario.getNome());
        adicionarCelulaDados(tabelaDados, "CPF:", formatarCPF(usuario.getCpf()));
        adicionarCelulaDados(tabelaDados, "Período:", getNomeMes(mes) + "/" + ano);

        doc.add(tabelaDados);
    }

    /**
     * ================================================================
     * ADICIONAR TABELA DE RECEITAS (PRINCIPAL)
     * ================================================================
     *
     * Tabela 3 colunas: [Categoria] [Com NF] [Sem NF]
     *
     * Layout:
     * ┌────────────────────────┬──────────┬──────────┐
     * │ CATEGORIA              │ COM NF   │ SEM NF   │
     * ├────────────────────────┼──────────┼──────────┤
     * │ I - Revenda            │ R$ 500   │ R$ 300   │
     * │ II - Industrializados  │ R$ 0     │ R$ 200   │
     * │ III - Serviços         │ R$ 1.000 │ R$ 0     │
     * ├────────────────────────┴──────────┼──────────┤
     * │ TOTAL GERAL                       │ R$ 2.000 │
     * └───────────────────────────────────┴──────────┘
     *
     * IMPORTANTE: Valores de totais[] calculados antes.
     */
    private void adicionarTabelaReceitas(Document doc, double[] totais)
            throws DocumentException {

        // ========== TÍTULO DA SEÇÃO ==========
        Paragraph titulo = new Paragraph("RECEITAS DO MÊS", SUBTITULO);
        doc.add(titulo);
        doc.add(new Paragraph("\n"));

        // ========== CRIAR TABELA ==========
        // 3 colunas: [Categoria (50%)] [Com NF (25%)] [Sem NF (25%)]
        PdfPTable tabela = new PdfPTable(3);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{50, 25, 25});

        // ========== CABEÇALHO ==========
        adicionarCelulaHeader(tabela, "CATEGORIA");
        adicionarCelulaHeader(tabela, "COM NF");
        adicionarCelulaHeader(tabela, "SEM NF");

        // ========== LINHA 1: REVENDA DE MERCADORIAS ==========
        adicionarLinhaCategoria(tabela, "I - Revenda de Mercadorias",
                totais[0], totais[1]);

        // ========== LINHA 2: PRODUTOS INDUSTRIALIZADOS ==========
        adicionarLinhaCategoria(tabela, "II - Produtos Industrializados",
                totais[2], totais[3]);

        // ========== LINHA 3: PRESTAÇÃO DE SERVIÇOS ==========
        adicionarLinhaCategoria(tabela, "III - Prestação de Serviços",
                totais[4], totais[5]);

        // ========== LINHA 4: TOTAL GERAL ==========
        // Célula mesclada (2 colunas) para "TOTAL GERAL"
        PdfPCell celulaTotal = new PdfPCell(new Phrase("TOTAL GERAL", NEGRITO));
        celulaTotal.setBackgroundColor(new BaseColor(230, 230, 230)); // Cinza claro
        celulaTotal.setPadding(8);
        celulaTotal.setColspan(2); // Mescla 2 colunas
        tabela.addCell(celulaTotal);

        // Célula do valor total
        PdfPCell celulaValorTotal = new PdfPCell(new Phrase(moeda.format(totais[6]), NEGRITO));
        celulaValorTotal.setBackgroundColor(new BaseColor(230, 230, 230));
        celulaValorTotal.setPadding(8);
        celulaValorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alinha à direita
        tabela.addCell(celulaValorTotal);

        doc.add(tabela);
    }

    /**
     * ================================================================
     * ADICIONAR DETALHAMENTO DE VENDAS
     * ================================================================
     *
     * Lista TODAS as vendas do mês em formato tabela.
     *
     * Colunas: [Data] [Categoria] [Descrição] [NF] [Valor]
     *
     * Layout:
     * ┌────────┬───────────┬──────────┬────┬──────────┐
     * │ Data   │ Categoria │ Descr.   │ NF │ Valor    │
     * ├────────┼───────────┼──────────┼────┼──────────┤
     * │ 01/02  │ Produtos  │ Venda X  │ S  │ R$ 100   │
     * │ 05/02  │ Serviços  │ Consult. │ N  │ R$ 500   │
     * └────────┴───────────┴──────────┴────┴──────────┘
     */
    private void adicionarDetalhamentoVendas(Document doc, List<Vendas> vendas)
            throws DocumentException {

        // Se não há vendas, não exibe seção
        if (vendas.isEmpty()) return;

        // ========== TÍTULO DA SEÇÃO ==========
        Paragraph titulo = new Paragraph("DETALHAMENTO DAS VENDAS", SUBTITULO);
        doc.add(titulo);
        doc.add(new Paragraph("\n"));

        // ========== CRIAR TABELA ==========
        // 5 colunas com larguras proporcionais
        PdfPTable tabela = new PdfPTable(5);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{15, 30, 20, 15, 20});

        // ========== CABEÇALHO ==========
        adicionarCelulaHeader(tabela, "Data");
        adicionarCelulaHeader(tabela, "Categoria");
        adicionarCelulaHeader(tabela, "Descrição");
        adicionarCelulaHeader(tabela, "NF");
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
            tabela.addCell(new PdfPCell(new Phrase(dataFormat.format(data), NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNomeCategoria(), NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(
                    v.getDescricao() != null ? v.getDescricao() : "-", NORMAL)));
            tabela.addCell(new PdfPCell(new Phrase(v.getNotaFiscalEmitida(), NORMAL)));

            // Célula de valor (alinhada à direita)
            PdfPCell celulaValor = new PdfPCell(new Phrase(moeda.format(v.getValor()), NORMAL));
            celulaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabela.addCell(celulaValor);
        }

        doc.add(tabela);
    }

    /**
     * ================================================================
     * ADICIONAR RODAPÉ DO RELATÓRIO
     * ================================================================
     *
     * Observações legais + Data de emissão
     */
    private void adicionarRodape(Document doc, int mes, int ano)
            throws DocumentException {

        doc.add(new Paragraph("\n\n")); // Espaços

        // ========== OBSERVAÇÃO LEGAL ==========
        Paragraph obs = new Paragraph(
                "Relatório gerado automaticamente pelo Sistema MEI - " +
                        "Conforme exigências da Receita Federal do Brasil",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
        obs.setAlignment(Element.ALIGN_CENTER);
        doc.add(obs);

        // ========== DATA DE EMISSÃO ==========
        Paragraph data = new Paragraph(
                "Emitido em: " + dataFormat.format(new java.util.Date()),
                new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL));
        data.setAlignment(Element.ALIGN_CENTER);
        doc.add(data);
    }

    // ================================================================
    // MÉTODOS AUXILIARES (PRIVADOS)
    // ================================================================

    /**
     * Adiciona linha [Label: Valor] em tabela (sem bordas)
     */
    private void adicionarCelulaDados(PdfPTable tabela, String titulo, String valor) {
        // Célula do label (ex: "Nome:")
        PdfPCell celulaTitulo = new PdfPCell(new Phrase(titulo, NEGRITO));
        celulaTitulo.setBorder(Rectangle.NO_BORDER); // Sem borda
        celulaTitulo.setPadding(5);
        tabela.addCell(celulaTitulo);

        // Célula do valor (ex: "João Silva")
        PdfPCell celulaValor = new PdfPCell(new Phrase(valor, NORMAL));
        celulaValor.setBorder(Rectangle.NO_BORDER);
        celulaValor.setPadding(5);
        tabela.addCell(celulaValor);
    }

    /**
     * Adiciona célula de cabeçalho (azul, centralizada, negrito)
     */
    private void adicionarCelulaHeader(PdfPTable tabela, String texto) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, NEGRITO));
        celula.setBackgroundColor(new BaseColor(66, 133, 244)); // Azul Google
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(8);
        tabela.addCell(celula);
    }

    /**
     * Adiciona linha de categoria com 3 células:
     * [Nome Categoria (negrito)] [Com NF (R$)] [Sem NF (R$)]
     */
    private void adicionarLinhaCategoria(PdfPTable tabela, String categoria,
                                         double comNF, double semNF) {
        // Célula 1: Nome da categoria
        PdfPCell celulaCategoria = new PdfPCell(new Phrase(categoria, NEGRITO));
        celulaCategoria.setPadding(8);
        tabela.addCell(celulaCategoria);

        // Célula 2: Valor COM NF (alinhado à direita)
        PdfPCell celulaComNF = new PdfPCell(new Phrase(moeda.format(comNF), NORMAL));
        celulaComNF.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaComNF.setPadding(8);
        tabela.addCell(celulaComNF);

        // Célula 3: Valor SEM NF (alinhado à direita)
        PdfPCell celulaSemNF = new PdfPCell(new Phrase(moeda.format(semNF), NORMAL));
        celulaSemNF.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celulaSemNF.setPadding(8);
        tabela.addCell(celulaSemNF);
    }

    /**
     * Formata CPF: 12345678901 → 123.456.789-01
     */
    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;

        return cpf.substring(0,3) + "." +
                cpf.substring(3,6) + "." +
                cpf.substring(6,9) + "-" +
                cpf.substring(9,11);
    }

    /**
     * Retorna nome do mês por número
     * @param mes 1-12
     * @return "Janeiro", "Fevereiro", etc
     */
    private String getNomeMes(int mes) {
        String[] meses = {
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return meses[mes - 1];
    }
}

/* ================================================================
   USO COMPLETO DO RELATÓRIO
   ================================================================

   // No RelatorioController (POST):

   // 1. Buscar vendas do mês
   List<Vendas> vendas = vendasDAO.listarPorMesAno(usuarioId, mes, ano);

   // 2. Calcular totais
   RelatorioPDF relatorio = new RelatorioPDF();
   double[] totais = relatorio.calcularTotais(vendas);

   // 3. Gerar PDF
   byte[] pdfBytes = relatorio.gerarRelatorio(usuario, mes, ano, vendas, totais);

   // 4. Configurar response HTTP
   response.setContentType("application/pdf");
   response.setHeader("Content-Disposition",
                    "attachment; filename=\"relatorio_mei_" + mes + "_" + ano + ".pdf\"");
   response.setContentLength(pdfBytes.length);

   // 5. Enviar para download
   OutputStream out = response.getOutputStream();
   out.write(pdfBytes);
   out.flush();

   ================================================================ */

/* ================================================================
   ESTRUTURA DO ARRAY TOTAIS
   ================================================================

   totais[0] = Revenda COM NF
   totais[1] = Revenda SEM NF
   totais[2] = Industrializados COM NF
   totais[3] = Industrializados SEM NF
   totais[4] = Serviços COM NF
   totais[5] = Serviços SEM NF
   totais[6] = Total Geral (soma de 0 a 5)

   Exemplo:
   totais[0] = 500.00   (Revenda com NF)
   totais[1] = 300.00   (Revenda sem NF)
   totais[2] = 0.00     (Industrial com NF)
   totais[3] = 200.00   (Industrial sem NF)
   totais[4] = 1000.00  (Serviços com NF)
   totais[5] = 0.00     (Serviços sem NF)
   totais[6] = 2000.00  (Total)

   ================================================================ */