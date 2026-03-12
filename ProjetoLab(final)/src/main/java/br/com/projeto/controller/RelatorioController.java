package br.com.projeto.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import br.com.projeto.dao.VendasDAO;
import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;
import br.com.projeto.utils.Conexao;
import br.com.projeto.utils.RelatorioPDF;

/**
 * ================================================================
 * RELATORIO CONTROLLER - Geração de Relatórios em PDF
 * ================================================================
 *
 * PROPÓSITO:
 * Gera relatórios mensais de vendas em formato PDF para download.
 * Usado pelo MEI para declarações e controle financeiro.
 *
 * FUNCIONALIDADES:
 * 1. Exibir formulário de seleção (mês/ano)
 * 2. Gerar PDF com vendas do período
 * 3. Download automático do arquivo
 *
 * ROTAS:
 * - GET  /relatorio → Exibe formulário (relatorio.jsp)
 * - POST /relatorio → Gera e baixa PDF
 *
 * PARÂMETROS DO FORMULÁRIO (POST):
 * - mes* (obrigatório): 1-12
 * - ano* (obrigatório): ano válido
 *
 * CONTEÚDO DO PDF:
 * 1. Cabeçalho:
 *    - Nome do usuário
 *    - CPF
 *    - CNPJ (se cadastrado)
 *    - Período (mês/ano)
 *
 * 2. Tabela de vendas:
 *    - Data
 *    - Categoria
 *    - Descrição
 *    - Valor
 *    - Nota Fiscal
 *
 * 3. Totalizadores:
 *    - Total de vendas
 *    - Valor total
 *    - Total com NF
 *    - Total sem NF
 *
 * VALIDAÇÕES:
 * - Mês entre 1 e 12
 * - Ano numérico válido
 * - Pelo menos 1 venda no período
 *
 * FLUXO GET:
 * 1. Valida autenticação
 * 2. Exibe formulário de seleção
 *
 * FLUXO POST:
 * 1. Valida autenticação
 * 2. Valida parâmetros (mês, ano)
 * 3. Busca vendas no período
 * 4. Valida se há vendas
 * 5. Calcula totalizadores
 * 6. Gera PDF (RelatorioPDF)
 * 7. Configura headers HTTP
 * 8. Envia arquivo para download
 *
 * NOME DO ARQUIVO:
 * relatorio_mei_[mes]_[ano].pdf
 * Exemplo: relatorio_mei_3_2025.pdf
 *
 * HEADERS HTTP:
 * - Content-Type: application/pdf
 * - Content-Disposition: attachment; filename="..."
 * - Content-Length: [tamanho em bytes]
 *
 * EXEMPLO DE USO:
 * ```
 * // Gerar relatório de Março/2025:
 * POST /relatorio
 * mes=3&ano=2025
 *
 * // Resultado: Download de relatorio_mei_3_2025.pdf
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Com validações e super comentado
 * @see VendasDAO
 * @see RelatorioPDF
 */
@WebServlet("/relatorio")
public class RelatorioController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Exibir Formulário
       ================================================================

       URL: GET /relatorio

       Responsabilidades:
       1. Validar autenticação
       2. Exibir formulário de seleção (relatorio.jsp)

       JSP: relatorio.jsp

       Formulário contém:
       - Dropdown de mês (1-12)
       - Campo de ano
       - Botão "Gerar Relatório"
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== RELATORIO GET ==========");

        // ========== STEP 1: VALIDAR AUTENTICAÇÃO ==========
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            System.err.println("❌ Usuário não autenticado");
            System.out.println("===================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        System.out.println("✅ Usuário: " + usuario.getNome());

        // ========== STEP 2: EXIBIR FORMULÁRIO ==========
        System.out.println("📄 Exibindo formulário de relatório");
        System.out.println("===================================\n");

        request.getRequestDispatcher("/pages/relatorio.jsp").forward(request, response);
    }

    /* ================================================================
       MÉTODO POST - Gerar e Baixar PDF
       ================================================================

       URL: POST /relatorio

       Parâmetros obrigatórios:
       - mes: 1-12 (numérico)
       - ano: Ano válido (numérico)

       Validações:
       1. Usuário autenticado
       2. Mês e ano informados
       3. Mês entre 1 e 12
       4. Ano é número válido
       5. Há vendas no período

       Processo:
       1. Valida autenticação
       2. Lê e valida parâmetros
       3. Busca vendas do período
       4. Verifica se há vendas
       5. Calcula totalizadores
       6. Gera PDF
       7. Configura HTTP headers
       8. Envia para download

       Em caso de erro:
       - Redireciona para /relatorio com mensagem

       Em caso de sucesso:
       - Download do PDF
       - Log de sucesso
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== RELATORIO POST ==========");

        // ========== STEP 1: VALIDAR AUTENTICAÇÃO ==========
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            System.err.println("❌ Usuário não autenticado");
            System.out.println("====================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        System.out.println("✅ Usuário: " + usuario.getNome() + " (ID: " + usuario.getIdUsuario() + ")");

        // ========== STEP 2: LER PARÂMETROS ==========
        String mesParam = request.getParameter("mes");
        String anoParam = request.getParameter("ano");

        System.out.println("📋 Parâmetros recebidos:");
        System.out.println("   - Mês: " + mesParam);
        System.out.println("   - Ano: " + anoParam);

        // ========== STEP 3: VALIDAR PARÂMETROS OBRIGATÓRIOS ==========
        if (mesParam == null || anoParam == null) {
            System.err.println("❌ Mês ou ano não informado!");
            session.setAttribute("erro", "Mês e ano são obrigatórios!");
            response.sendRedirect(request.getContextPath() + "/relatorio");
            return;
        }

        // ========== STEP 4: CONVERTER E VALIDAR NÚMEROS ==========
        int mes, ano;

        try {
            mes = Integer.parseInt(mesParam);
            ano = Integer.parseInt(anoParam);

            System.out.println("✅ Valores convertidos:");
            System.out.println("   - Mês: " + mes);
            System.out.println("   - Ano: " + ano);

        } catch (NumberFormatException e) {
            System.err.println("❌ Mês ou ano inválido!");
            System.err.println("   Mês: " + mesParam);
            System.err.println("   Ano: " + anoParam);
            session.setAttribute("erro", "Mês ou ano inválido!");
            response.sendRedirect(request.getContextPath() + "/relatorio");
            return;
        }

        // ========== STEP 5: VALIDAR INTERVALO DO MÊS ==========
        if (mes < 1 || mes > 12) {
            System.err.println("❌ Mês fora do intervalo: " + mes + " (deve ser 1-12)");
            session.setAttribute("erro", "Mês deve estar entre 1 e 12!");
            response.sendRedirect(request.getContextPath() + "/relatorio");
            return;
        }

        System.out.println("✅ Mês válido: " + mes);

        // ========== STEP 6: CONECTAR AO BANCO ==========
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão estabelecida");

            // ========== STEP 7: BUSCAR VENDAS DO PERÍODO ==========
            VendasDAO vendasDAO = new VendasDAO(conexao);
            List<Vendas> vendas = new ArrayList<>();

            System.out.println("⏳ Buscando vendas...");
            System.out.println("   - Usuário ID: " + usuario.getIdUsuario());
            System.out.println("   - Mês: " + mes);
            System.out.println("   - Ano: " + ano);
            System.out.println("   - SQL: listarPorMesAno(" + usuario.getIdUsuario() + ", " + mes + ", " + ano + ")");

            try {
                vendas = vendasDAO.listarPorMesAno(usuario.getIdUsuario(), mes, ano);

                System.out.println("✅ Vendas retornadas: " + vendas.size());

                // Log das vendas
                if (!vendas.isEmpty()) {
                    System.out.println("📋 Vendas encontradas:");
                    for (Vendas v : vendas) {
                        System.out.println("   - ID: " + v.getIdVendas() +
                                " | Data: " + v.getDataVendas() +
                                " | Valor: R$ " + v.getValor() +
                                " | NF: " + v.getNotaFiscalEmitida());
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao buscar vendas:");
                System.err.println("   Mensagem: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao buscar vendas: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/relatorio");
                return;
            }

            // ========== STEP 8: VALIDAR SE HÁ VENDAS ==========
            if (vendas.isEmpty()) {
                System.err.println("⚠️ Nenhuma venda encontrada no período!");
                System.err.println("   Mês: " + mes);
                System.err.println("   Ano: " + ano);
                System.out.println("====================================\n");

                session.setAttribute("erro", "Nenhuma venda encontrada para o período selecionado.");
                response.sendRedirect(request.getContextPath() + "/relatorio");
                return;
            }

            System.out.println("✅ Vendas disponíveis: " + vendas.size());

            // ========== STEP 9: GERAR PDF ==========
            try {
                System.out.println("⏳ Gerando PDF...");

                RelatorioPDF relatorio = new RelatorioPDF();

                // Calcular totalizadores
                System.out.println("⏳ Calculando totais...");
                double[] totais = relatorio.calcularTotais(vendas);

                System.out.println("✅ Totais calculados:");
                System.out.println("   - Total geral: R$ " + String.format("%.2f", totais[0]));
                System.out.println("   - Total com NF: R$ " + String.format("%.2f", totais[1]));
                System.out.println("   - Total sem NF: R$ " + String.format("%.2f", totais[2]));

                // Gerar PDF
                System.out.println("⏳ Gerando arquivo PDF...");
                byte[] pdfBytes = relatorio.gerarRelatorio(usuario, mes, ano, vendas, totais);

                System.out.println("✅ PDF gerado:");
                System.out.println("   - Tamanho: " + pdfBytes.length + " bytes");
                System.out.println("   - Formato: PDF");

                // ========== STEP 10: CONFIGURAR HEADERS HTTP ==========
                String nomeArquivo = "relatorio_mei_" + mes + "_" + ano + ".pdf";

                System.out.println("⏳ Configurando headers HTTP...");

                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + nomeArquivo + "\"");
                response.setContentLength(pdfBytes.length);

                System.out.println("✅ Headers configurados:");
                System.out.println("   - Content-Type: application/pdf");
                System.out.println("   - Content-Disposition: attachment");
                System.out.println("   - Filename: " + nomeArquivo);
                System.out.println("   - Content-Length: " + pdfBytes.length);

                // ========== STEP 11: ENVIAR PDF PARA DOWNLOAD ==========
                System.out.println("⏳ Enviando PDF para cliente...");

                OutputStream out = response.getOutputStream();
                out.write(pdfBytes);
                out.flush();

                System.out.println("✅ RELATÓRIO GERADO COM SUCESSO!");
                System.out.println("📥 Download iniciado: " + nomeArquivo);
                System.out.println("📊 Estatísticas:");
                System.out.println("   - Vendas incluídas: " + vendas.size());
                System.out.println("   - Período: " + mes + "/" + ano);
                System.out.println("   - Total: R$ " + String.format("%.2f", totais[0]));
                System.out.println("====================================\n");

            } catch (Exception e) {
                // ========== ERRO AO GERAR PDF ==========
                System.err.println("❌ Erro ao gerar PDF:");
                System.err.println("   Tipo: " + e.getClass().getName());
                System.err.println("   Mensagem: " + e.getMessage());
                e.printStackTrace();
                System.out.println("====================================\n");

                session.setAttribute("erro", "Erro ao gerar PDF: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/relatorio");
            }

        } catch (Exception e) {
            // ========== ERRO DE CONEXÃO ==========
            System.err.println("❌ Erro de conexão:");
            e.printStackTrace();
            System.out.println("====================================\n");

            session.setAttribute("erro", "Erro ao gerar relatório: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/relatorio");
        }
        // Conexão fecha automaticamente (try-with-resources)
    }
}

/* ================================================================
   RESUMO DO CONTROLLER
   ================================================================

   FUNCIONALIDADE PRINCIPAL:
   Gerar relatórios mensais em PDF para download

   ROTAS:
   - GET  /relatorio → Formulário de seleção
   - POST /relatorio → Gera e baixa PDF

   PARÂMETROS DO FORMULÁRIO:
   - mes* (1-12, obrigatório)
   - ano* (numérico, obrigatório)

   VALIDAÇÕES:
   ✅ Autenticação obrigatória
   ✅ Mês e ano informados
   ✅ Mês entre 1 e 12
   ✅ Ano é número válido
   ✅ Período tem vendas

   CONTEÚDO DO PDF:

   1. CABEÇALHO:
      - Nome do usuário
      - CPF
      - CNPJ (se cadastrado)
      - Período: Mês/Ano

   2. TABELA DE VENDAS:
      | Data | Categoria | Descrição | Valor | NF |
      |------|-----------|-----------|-------|----|
      | ...  | ...       | ...       | ...   | .. |

   3. TOTALIZADORES:
      - Total de vendas: [quantidade]
      - Valor total: R$ [valor]
      - Com Nota Fiscal: R$ [valor]
      - Sem Nota Fiscal: R$ [valor]

   EXEMPLO DE USO:

   1. Relatório de Janeiro/2025:
      POST /relatorio
      mes=1&ano=2025
      → Download: relatorio_mei_1_2025.pdf

   2. Relatório de Dezembro/2024:
      POST /relatorio
      mes=12&ano=2024
      → Download: relatorio_mei_12_2024.pdf

   FORMATO DO NOME:
   relatorio_mei_[MES]_[ANO].pdf

   Exemplos:
   - relatorio_mei_1_2025.pdf  (Janeiro/2025)
   - relatorio_mei_3_2025.pdf  (Março/2025)
   - relatorio_mei_12_2024.pdf (Dezembro/2024)

   HEADERS HTTP:
   Content-Type: application/pdf
   Content-Disposition: attachment; filename="relatorio_mei_3_2025.pdf"
   Content-Length: [bytes]

   PROCESSO COMPLETO:

   1. Usuário acessa GET /relatorio
      → Vê formulário com mês e ano

   2. Usuário seleciona mês=3, ano=2025
      → Clica em "Gerar Relatório"

   3. POST /relatorio com mes=3&ano=2025
      → Controller valida dados

   4. Busca vendas de Março/2025
      → SELECT * FROM vendas WHERE ...

   5. Se não há vendas:
      → Mensagem "Nenhuma venda encontrada"
      → Volta para formulário

   6. Se há vendas:
      → Calcula totais
      → Gera PDF
      → Envia para download

   7. Navegador baixa arquivo:
      → relatorio_mei_3_2025.pdf

   CÁLCULO DE TOTAIS (RelatorioPDF):
   ```java
   double totalGeral = 0;
   double totalComNF = 0;
   double totalSemNF = 0;

   for (Vendas v : vendas) {
       totalGeral += v.getValor();

       if (v.getNotaFiscalEmitida().equals("S")) {
           totalComNF += v.getValor();
       } else {
           totalSemNF += v.getValor();
       }
   }

   return new double[] {totalGeral, totalComNF, totalSemNF};
   ```

   CASOS DE USO:

   1. Declaração mensal de receitas:
      → MEI gera PDF do mês para enviar à contabilidade

   2. Comprovação de renda:
      → MEI comprova receitas para banco/imobiliária

   3. Controle financeiro:
      → MEI imprime relatório mensal para arquivo físico

   4. Análise de NF:
      → Verificar quantas vendas têm nota fiscal

   SEGURANÇA:
   ✅ Validação de sessão
   ✅ PreparedStatement (via DAO)
   ✅ Try-with-resources
   ✅ Filtra por usuario_id (isolamento)

   DEPENDÊNCIAS:
   - VendasDAO: Buscar vendas do período
   - RelatorioPDF: Gerar arquivo PDF
   - Conexao: Gerenciar conexão
   - HttpSession: Autenticação

   MENSAGENS:
   - Erro: "Mês e ano são obrigatórios!"
   - Erro: "Mês deve estar entre 1 e 12!"
   - Erro: "Nenhuma venda encontrada para o período"
   - Erro: "Erro ao gerar PDF: [mensagem]"
   - Sucesso: Download automático do PDF

   OBSERVAÇÕES:
   - Conexão fecha automaticamente
   - PDF gerado em memória (byte[])
   - Download automático (não salva no servidor)
   - Logs detalhados em cada etapa
   - Sem limite de vendas por relatório
   - Formato padrão: A4, portrait

   MELHORIAS FUTURAS:
   - [ ] Relatório por categoria
   - [ ] Relatório anual (todos os meses)
   - [ ] Gráficos de vendas
   - [ ] Exportar em Excel
   - [ ] Filtro por Nota Fiscal
   - [ ] Assinatura digital
   ================================================================ */