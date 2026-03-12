package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.utils.Conexao;

/**
 * ================================================================
 * HISTORICO CONTROLLER - Consulta de Vendas com Filtros
 * ================================================================
 *
 * PROPÓSITO:
 * Permite ao usuário consultar todas as suas vendas históricas
 * com filtros por ano e por status de Nota Fiscal.
 *
 * FUNCIONALIDADES:
 * 1. Listar vendas filtradas por ano
 * 2. Filtrar por Nota Fiscal:
 *    - todas: Todas as vendas
 *    - com_nf: Apenas vendas com NF emitida
 *    - sem_nf: Apenas vendas sem NF
 * 3. Exibir estatísticas:
 *    - Total de vendas
 *    - Valor total
 *    - Quantidade com/sem NF
 *    - Valor com/sem NF
 * 4. Listar anos disponíveis (dropdown)
 *
 * ROTAS:
 * - GET  /historico              → Exibe vendas (ano padrão: 2026)
 * - GET  /historico?ano=2025     → Exibe vendas de 2025
 * - GET  /historico?filtroNF=com_nf → Apenas com NF
 * - POST /historico              → Mesma função que GET
 *
 * PARÂMETROS DE FILTRO:
 * - ano: Ano das vendas (default: 2026)
 * - filtroNF: Filtro de NF (default: "todas")
 *   Valores: "todas", "com_nf", "sem_nf"
 *
 * TABELAS ENVOLVIDAS:
 * - vendas: Registros de vendas
 * - categoria: Nome da categoria
 * - nota_fiscal: Dados da NF (JOIN)
 *
 * DADOS EXIBIDOS:
 * - vendasDetalhadas: List<Vendas> com categoria e NF
 * - anos: List<Integer> anos disponíveis
 * - ano: Ano selecionado
 * - filtroNF: Filtro selecionado
 * - Estatísticas:
 *   - totalVendas: Quantidade total
 *   - totalValor: Soma dos valores
 *   - totalComNF: Quantidade com NF
 *   - totalSemNF: Quantidade sem NF
 *   - valorComNF: Soma valores com NF
 *   - valorSemNF: Soma valores sem NF
 *
 * CÁLCULO DE ESTATÍSTICAS:
 * - Total de vendas: COUNT(*)
 * - Valor total: SUM(valor)
 * - Com NF: COUNT WHERE nota_fiscal_emitida = 'S'
 * - Sem NF: COUNT WHERE nota_fiscal_emitida = 'N'
 *
 * EXEMPLO DE USO:
 * ```
 * // Todas as vendas de 2026:
 * GET /historico
 *
 * // Vendas de 2025:
 * GET /historico?ano=2025
 *
 * // Apenas vendas com NF de 2026:
 * GET /historico?ano=2026&filtroNF=com_nf
 *
 * // Apenas vendas sem NF de 2024:
 * GET /historico?ano=2024&filtroNF=sem_nf
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Com filtros e estatísticas super comentado
 * @see VendasDAO
 * @see Vendas
 */
@WebServlet("/historico")
public class HistoricoController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Exibir Histórico de Vendas
       ================================================================

       URL: GET /historico?ano=2026&filtroNF=todas

       Parâmetros opcionais:
       - ano: Ano para filtrar (default: 2026)
       - filtroNF: "todas", "com_nf", "sem_nf" (default: "todas")

       Responsabilidades:
       1. Validar autenticação
       2. Processar parâmetros de filtro
       3. Buscar anos disponíveis (para dropdown)
       4. Buscar vendas filtradas
       5. Calcular estatísticas
       6. Exibir historico.jsp

       Atributos enviados ao JSP:
       - ano: Integer
       - anos: List<Integer>
       - filtroNF: String
       - vendasDetalhadas: List<Vendas>
       - totalVendas: Integer
       - totalValor: Double
       - totalComNF: Integer
       - totalSemNF: Integer
       - valorComNF: Double
       - valorSemNF: Double
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== HISTORICO GET ==========");

        // ========== STEP 1: VALIDAR AUTENTICAÇÃO ==========
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            System.err.println("❌ Usuário não autenticado");
            System.out.println("===================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        System.out.println("✅ Usuário: " + usuario.getNome() + " (ID: " + usuario.getIdUsuario() + ")");

        // ========== STEP 2: PROCESSAR PARÂMETROS DE FILTRO ==========

        // Ano: default 2026
        String anoParam = request.getParameter("ano");
        int ano = 2026;

        if (anoParam != null && !anoParam.isEmpty()) {
            try {
                ano = Integer.parseInt(anoParam);
                System.out.println("📅 Ano selecionado: " + ano);
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Ano inválido: " + anoParam + ", usando padrão 2026");
                ano = 2026;
            }
        } else {
            System.out.println("📅 Ano padrão: 2026");
        }

        // Mês: default 0 (todos os meses)
        // ================================================
        // VALORES POSSÍVEIS:
        // 0 = Todos os meses (não filtra por mês específico)
        // 1 = Janeiro
        // 2 = Fevereiro
        // 3 = Março
        // 4 = Abril
        // 5 = Maio
        // 6 = Junho
        // 7 = Julho
        // 8 = Agosto
        // 9 = Setembro
        // 10 = Outubro
        // 11 = Novembro
        // 12 = Dezembro
        // ================================================
        String mesParam = request.getParameter("mes");
        int mes = 0;

        if (mesParam != null && !mesParam.isEmpty()) {
            try {
                mes = Integer.parseInt(mesParam);
                
                // Validação: mês deve estar entre 0 e 12
                if (mes < 0 || mes > 12) {
                    System.err.println("⚠️ Mês inválido (" + mes + "), fora do intervalo 0-12");
                    System.err.println("   Usando padrão: 0 (Todos)");
                    mes = 0;
                } else {
                    // Log diferenciado para "Todos" vs mês específico
                    if (mes == 0) {
                        System.out.println("📅 Mês selecionado: 0 (Todos os meses)");
                    } else {
                        String[] nomesMeses = {"", "Janeiro", "Fevereiro", "Março", "Abril", 
                            "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", 
                            "Novembro", "Dezembro"};
                        System.out.println("📅 Mês selecionado: " + mes + " (" + nomesMeses[mes] + ")");
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Mês inválido: '" + mesParam + "' não é um número");
                System.err.println("   Usando padrão: 0 (Todos)");
                mes = 0;
            }
        } else {
            System.out.println("📅 Mês padrão: 0 (Todos os meses)");
        }

        // Filtro Categoria: default null (todas as categorias)
        // ================================================
        // NOVO FILTRO DE CATEGORIA
        // ================================================
        // VALORES POSSÍVEIS:
        // null ou "" = Todas as categorias (não filtra)
        // 1 = Revenda de Mercadorias
        // 2 = Produtos Industrializados
        // 3 = Prestação de Serviços
        // 7 = Outro
        // ================================================
        String categoriaParam = request.getParameter("categoria");
        Integer categoriaId = null;

        if (categoriaParam != null && !categoriaParam.isEmpty()) {
            try {
                categoriaId = Integer.parseInt(categoriaParam);
                
                if (categoriaId > 0) {
                    System.out.println("🏷️ Categoria selecionada: " + categoriaId);
                } else {
                    System.out.println("🏷️ Categoria: Todas (valor inválido: " + categoriaId + ")");
                    categoriaId = null;
                }
            } catch (NumberFormatException e) {
                System.err.println("⚠️ Categoria inválida: '" + categoriaParam + "' não é um número");
                System.err.println("   Usando padrão: null (Todas)");
                categoriaId = null;
            }
        } else {
            System.out.println("🏷️ Categoria padrão: null (Todas as categorias)");
        }

        // Filtro NF: default "todas"
        String filtroNF = request.getParameter("filtroNF");

        if (filtroNF == null || filtroNF.isEmpty()) {
            filtroNF = "todas";
        }

        System.out.println("🔍 Filtros aplicados:");
        System.out.println("   - Ano: " + ano);
        System.out.println("   - Mês: " + mes + (mes == 0 ? " (Todos)" : ""));
        System.out.println("   - Categoria: " + (categoriaId == null ? "Todas" : categoriaId));
        System.out.println("   - Filtro NF: " + filtroNF);
        System.out.println("   - Usuário ID: " + usuario.getIdUsuario());

        // ========== STEP 3: CONECTAR AO BANCO ==========
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão estabelecida");
            VendasDAO vendasDAO = new VendasDAO(conexao);

            // ========== STEP 4: BUSCAR ANOS DISPONÍVEIS ==========
            // Para popular dropdown de anos
            System.out.println("⏳ Buscando anos com vendas...");

            List<Integer> anos = new ArrayList<>();

            try {
                anos = vendasDAO.listarAnosComVendas(usuario.getIdUsuario());
                System.out.println("✅ Anos encontrados: " + anos.size());

                if (!anos.isEmpty()) {
                    System.out.println("📋 Lista de anos:");
                    for (Integer a : anos) {
                        System.out.println("   - " + a);
                    }
                } else {
                    System.err.println("⚠️ Nenhum ano com vendas, usando padrão");
                    // Anos padrão caso não encontre nada
                    anos.add(2024);
                    anos.add(2025);
                    anos.add(2026);
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao listar anos:");
                e.printStackTrace();
                // Anos padrão em caso de erro
                anos.add(2024);
                anos.add(2025);
                anos.add(2026);
            }

            // ========== STEP 5: BUSCAR VENDAS COM FILTRO ==========
            System.out.println("⏳ Buscando vendas filtradas...");
            System.out.println("   - SQL: listarPorAnoEMesComFiltroNF(" + 
                usuario.getIdUsuario() + ", " + ano + ", " + mes + ", '" + filtroNF + "')");

            List<Vendas> vendasDetalhadas = new ArrayList<>();

            try {
                vendasDetalhadas = vendasDAO.listarPorAnoEMesComFiltroNF(
                        usuario.getIdUsuario(),
                        ano,
                        mes,      // ⬅️ NOVO PARÂMETRO MÊS
                        filtroNF,
                        categoriaId   // ⬅️ NOVO PARÂMETRO CATEGORIA!
                );

                System.out.println("✅ Vendas encontradas: " + vendasDetalhadas.size());

                // Log resumido das vendas
                if (!vendasDetalhadas.isEmpty()) {
                    System.out.println("📋 Primeiras vendas:");
                    int max = Math.min(5, vendasDetalhadas.size());
                    for (int i = 0; i < max; i++) {
                        Vendas v = vendasDetalhadas.get(i);
                        System.out.println("   - ID: " + v.getIdVendas() +
                                " | Valor: R$ " + v.getValor() +
                                " | NF: " + v.getNotaFiscalEmitida() +
                                " | Data: " + v.getDataVendas());
                    }
                    if (vendasDetalhadas.size() > 5) {
                        System.out.println("   ... e mais " + (vendasDetalhadas.size() - 5) + " vendas");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao buscar vendas:");
                e.printStackTrace();
            }

            // ========== STEP 6: CALCULAR ESTATÍSTICAS ==========
            System.out.println("⏳ Calculando estatísticas...");

            int totalVendas = vendasDetalhadas.size();
            double totalValor = 0;
            int totalComNF = 0;
            int totalSemNF = 0;
            double valorComNF = 0;
            double valorSemNF = 0;

            // Iterar vendas para calcular
            for (Vendas v : vendasDetalhadas) {
                totalValor += v.getValor();

                if ("S".equalsIgnoreCase(v.getNotaFiscalEmitida())) {
                    totalComNF++;
                    valorComNF += v.getValor();
                } else {
                    totalSemNF++;
                    valorSemNF += v.getValor();
                }
            }

            System.out.println("✅ Estatísticas calculadas:");
            System.out.println("   📊 TOTAIS:");
            System.out.println("      - Vendas: " + totalVendas);
            System.out.println("      - Valor: R$ " + String.format("%.2f", totalValor));
            System.out.println("   📄 COM NOTA FISCAL:");
            System.out.println("      - Quantidade: " + totalComNF);
            System.out.println("      - Valor: R$ " + String.format("%.2f", valorComNF));
            System.out.println("   📋 SEM NOTA FISCAL:");
            System.out.println("      - Quantidade: " + totalSemNF);
            System.out.println("      - Valor: R$ " + String.format("%.2f", valorSemNF));

            // ========== STEP 7: PREPARAR DADOS PARA JSP ==========
            request.setAttribute("ano", ano);
            request.setAttribute("mes", mes);        // ⬅️ NOVO: envia mês selecionado
            request.setAttribute("categoriaId", categoriaId);  // ⬅️ NOVO: envia categoria selecionada
            request.setAttribute("anos", anos);
            request.setAttribute("filtroNF", filtroNF);
            request.setAttribute("vendasDetalhadas", vendasDetalhadas);
            request.setAttribute("totalVendas", totalVendas);
            request.setAttribute("totalValor", totalValor);
            request.setAttribute("totalComNF", totalComNF);
            request.setAttribute("totalSemNF", totalSemNF);
            request.setAttribute("valorComNF", valorComNF);
            request.setAttribute("valorSemNF", valorSemNF);

            System.out.println("✅ Dados preparados para JSP");
            System.out.println("📄 Encaminhando para historico.jsp");
            System.out.println("===================================\n");

            request.getRequestDispatcher("/pages/historico.jsp").forward(request, response);

        } catch (Exception e) {
            // ========== TRATAMENTO DE ERRO CRÍTICO ==========
            System.err.println("❌ ERRO CRÍTICO no histórico:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            System.out.println("===================================\n");

            session.setAttribute("erro", "Erro ao carregar histórico: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
        // Conexão fecha automaticamente
    }

    /* ================================================================
       MÉTODO POST - Mesma Funcionalidade do GET
       ================================================================

       Alguns formulários podem usar POST em vez de GET.
       Por isso, redireciona para o método doGet.

       NOTA: É boa prática usar GET para consultas (leitura)
       e POST para ações (escrita). Aqui, POST é aceito por
       compatibilidade, mas redireciona para GET.
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== HISTORICO POST ==========");
        System.out.println("ℹ️ POST detectado, redirecionando para doGet");
        System.out.println("====================================\n");

        // Redireciona para GET (mesma lógica)
        doGet(request, response);
    }
}

/* ================================================================
   RESUMO DO CONTROLLER
   ================================================================

   FUNCIONALIDADE PRINCIPAL:
   Exibir histórico de vendas com filtros e estatísticas

   ROTAS:
   - GET  /historico                       → Todas vendas 2026
   - GET  /historico?ano=2025              → Vendas 2025
   - GET  /historico?filtroNF=com_nf       → Apenas com NF
   - GET  /historico?filtroNF=sem_nf       → Apenas sem NF
   - GET  /historico?ano=2024&filtroNF=com_nf → 2024 com NF
   - POST /historico                       → Redireciona para GET

   PARÂMETROS:
   - ano (default: 2026)
     - Qualquer ano válido (int)

   - filtroNF (default: "todas")
     - "todas": Todas as vendas
     - "com_nf": nota_fiscal_emitida = 'S'
     - "sem_nf": nota_fiscal_emitida = 'N'

   DADOS EXIBIDOS:
   1. FILTROS:
      - ano: Ano selecionado
      - anos: Lista de anos disponíveis (dropdown)
      - filtroNF: Filtro selecionado

   2. VENDAS:
      - vendasDetalhadas: List<Vendas>
        Cada venda contém:
        - ID, data, valor, descrição
        - Categoria (nome)
        - Nota Fiscal (número, se existir)
        - Status NF (S/N)

   3. ESTATÍSTICAS:
      - totalVendas: Quantidade total
      - totalValor: Soma de todas
      - totalComNF: Quantidade com NF
      - totalSemNF: Quantidade sem NF
      - valorComNF: Soma com NF
      - valorSemNF: Soma sem NF

   CÁLCULO DE ESTATÍSTICAS:
   ```java
   for (Vendas v : vendas) {
       totalValor += v.getValor();
       if (v.getNotaFiscalEmitida().equals("S")) {
           totalComNF++;
           valorComNF += v.getValor();
       } else {
           totalSemNF++;
           valorSemNF += v.getValor();
       }
   }
   ```

   FILTROS SQL (VendasDAO):
   - todas: WHERE YEAR(data_vendas) = ? AND usuario_id = ?
   - com_nf: WHERE ... AND nota_fiscal_emitida = 'S'
   - sem_nf: WHERE ... AND nota_fiscal_emitida = 'N'

   CASOS DE USO:

   1. Consultar todas as vendas do ano:
      GET /historico?ano=2025
      → Retorna todas as vendas de 2025

   2. Verificar vendas com NF para declaração:
      GET /historico?ano=2024&filtroNF=com_nf
      → Retorna apenas vendas com NF de 2024

   3. Identificar vendas sem NF:
      GET /historico?filtroNF=sem_nf
      → Retorna vendas sem NF (ano atual)

   4. Análise anual completa:
      GET /historico?ano=2023
      → Ver todas vendas + estatísticas de 2023

   SEGURANÇA:
   ✅ Validação de sessão obrigatória
   ✅ PreparedStatement (via DAO)
   ✅ Try-with-resources
   ✅ Filtra por usuario_id (isolamento)

   PERFORMANCE:
   ✅ Índices em: data_vendas, usuario_id
   ✅ Filtro no banco (WHERE)
   ✅ JOIN otimizado (categoria, nota_fiscal)
   ✅ Estatísticas calculadas em memória

   EXEMPLO DE RESPOSTA JSP:
   ```
   Histórico de Vendas - 2025

   [Filtros]
   Ano: [2024][2025][2026]
   NF:  (•) Todas ( ) Com NF ( ) Sem NF

   [Estatísticas]
   Total: 50 vendas | R$ 25.000,00
   Com NF: 30 vendas | R$ 18.000,00
   Sem NF: 20 vendas | R$ 7.000,00

   [Tabela]
   Data       | Categoria | Valor     | NF      | Ações
   01/03/2025 | Produto   | R$ 500,00 | 12345   | [Editar][Excluir]
   05/03/2025 | Serviço   | R$ 300,00 | -       | [Editar][Excluir]
   ...
   ```

   OBSERVAÇÕES:
   - Lista vazia não é erro (exibe "Nenhuma venda")
   - Anos sem vendas exibem padrão (2024-2026)
   - Filtros mantidos após ações (via parâmetros)
   - Estatísticas sempre atualizadas
   - Conexão fecha automaticamente
   ================================================================ */