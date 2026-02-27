package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import br.com.projeto.dao.VendasDAO;
import br.com.projeto.dao.CategoriaDAO;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.Usuario;
import br.com.projeto.model.NotaFiscal;
import br.com.projeto.utils.Conexao;

/**
 * ================================================================
 * DASHBOARD CONTROLLER - Painel Principal do Sistema
 * ================================================================
 *
 * PROPÓSITO:
 * Exibe o painel principal após login, com resumo de vendas
 * e formulário inline para cadastro rápido de novas vendas.
 *
 * FUNCIONALIDADES:
 * 1. Exibir dashboard com:
 *    - Últimas 10 vendas
 *    - Total de vendas do mês atual
 *    - Lista de categorias (para formulário)
 * 2. Cadastrar nova venda (formulário inline)
 *    - Com ou sem Nota Fiscal
 *
 * ROTAS:
 * - GET  /dashboard → Exibe painel principal
 * - POST /dashboard → Cadastra nova venda
 *
 * PARÂMETROS DO FORMULÁRIO (POST):
 * - categoria* (ID da categoria, obrigatório)
 * - valor* (decimal, obrigatório, maior que zero)
 * - descricao (texto, opcional)
 * - emitirNF (S/N, default N)
 * - numeroNF (obrigatório se emitirNF=S)
 *
 * TABELAS ENVOLVIDAS:
 * - vendas: Registro principal da venda
 * - categoria: Classificação da venda
 * - nota_fiscal: Dados da NF (se emitida)
 * - usuario: Proprietário da venda
 *
 * DADOS EXIBIDOS NO DASHBOARD:
 * 1. Últimas vendas (limite 10):
 *    - Data, categoria, valor, descrição, NF
 * 2. Total do mês:
 *    - Soma de todas as vendas do mês atual
 * 3. Categorias:
 *    - Para preencher dropdown do formulário
 *
 * FLUXO GET (Exibir Dashboard):
 * 1. Valida se usuário está logado
 * 2. Busca categorias no banco
 * 3. Busca últimas 10 vendas do usuário
 * 4. Calcula total do mês
 * 5. Envia dados para dashboard.jsp
 *
 * FLUXO POST (Cadastrar Venda):
 * 1. Valida se usuário está logado
 * 2. Valida campos obrigatórios
 * 3. Valida valor (número > 0)
 * 4. Valida Nota Fiscal (se marcado)
 * 5. Cria objeto Vendas
 * 6. Se NF marcada, cria objeto NotaFiscal
 * 7. Insere no banco via VendasDAO
 * 8. Redireciona para dashboard com mensagem
 *
 * EXEMPLO DE USO:
 * ```
 * // Exibir dashboard:
 * GET /dashboard
 *
 * // Cadastrar venda sem NF:
 * POST /dashboard
 * categoria=1&valor=150.50&descricao=Venda produto X&emitirNF=N
 *
 * // Cadastrar venda com NF:
 * POST /dashboard
 * categoria=2&valor=200.00&emitirNF=S&numeroNF=12345
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Com Nota Fiscal e super comentado
 * @see VendasDAO
 * @see CategoriaDAO
 * @see NotaFiscal
 */
@WebServlet("/dashboard")
public class DashboardController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Exibir Dashboard
       ================================================================

       URL: GET /dashboard

       Responsabilidades:
       1. Validar autenticação
       2. Buscar categorias (para formulário)
       3. Buscar últimas 10 vendas
       4. Calcular total do mês
       5. Preparar dados para JSP
       6. Exibir dashboard.jsp

       Atributos enviados ao JSP:
       - categorias: List<Categoria>
       - ultimasVendas: List<Vendas>
       - totalMes: Double
       - usuario: Usuario (da sessão)

       Em caso de erro:
       - Exibe dashboard.jsp com mensagem de erro
       - Não redireciona (mantém na página)
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== DASHBOARD GET ==========");

        // ========== STEP 1: VALIDAR AUTENTICAÇÃO ==========
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            System.err.println("❌ Usuário não autenticado");
            System.out.println("➡️ Redirecionando para /login");
            System.out.println("===================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        System.out.println("✅ Usuário autenticado:");
        System.out.println("   - ID: " + usuario.getIdUsuario());
        System.out.println("   - Nome: " + usuario.getNome());
        System.out.println("   - CPF: " + usuario.getCpf());

        // ========== STEP 2: CONECTAR AO BANCO ==========
        // Try-with-resources garante fechamento automático
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão com banco estabelecida");

            // ========== STEP 3: BUSCAR CATEGORIAS ==========
            // Usado para popular dropdown do formulário
            System.out.println("⏳ Buscando categorias...");

            CategoriaDAO categoriaDAO = new CategoriaDAO(conexao);
            List<Categoria> categorias = new ArrayList<>();

            try {
                categorias = categoriaDAO.listar();
                System.out.println("✅ Categorias carregadas: " + categorias.size());

                // Log de cada categoria
                if (!categorias.isEmpty()) {
                    System.out.println("📋 Lista de categorias:");
                    for (Categoria c : categorias) {
                        System.out.println("   - ID: " + c.getIdCategoria() +
                                " | Nome: " + c.getNomeCategoria() +
                                " | Ativo: " + c.isAtivo());
                    }
                } else {
                    System.err.println("⚠️ Nenhuma categoria encontrada!");
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao listar categorias:");
                System.err.println("   " + e.getMessage());
                e.printStackTrace();
            }

            // Envia para JSP (mesmo que vazia)
            request.setAttribute("categorias", categorias);

            // ========== STEP 4: BUSCAR ÚLTIMAS VENDAS ==========
            // Exibe as 10 vendas mais recentes na tabela
            System.out.println("⏳ Buscando últimas vendas...");

            VendasDAO vendasDAO = new VendasDAO(conexao);
            List<Vendas> ultimasVendas = new ArrayList<>();

            try {
                ultimasVendas = vendasDAO.listarPorUsuario(usuario.getIdUsuario(), 10);
                System.out.println("✅ Vendas carregadas: " + ultimasVendas.size());

                // Log resumido das vendas
                if (!ultimasVendas.isEmpty()) {
                    System.out.println("📋 Últimas vendas:");
                    for (Vendas v : ultimasVendas) {
                        System.out.println("   - ID: " + v.getIdVendas() +
                                " | Valor: R$ " + v.getValor() +
                                " | Data: " + v.getDataVendas() +
                                " | NF: " + v.getNotaFiscalEmitida());
                    }
                } else {
                    System.out.println("ℹ️ Nenhuma venda encontrada");
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao listar vendas:");
                System.err.println("   " + e.getMessage());
                e.printStackTrace();
            }

            // Envia para JSP
            request.setAttribute("ultimasVendas", ultimasVendas);

            // ========== STEP 5: CONTAR VENDAS DO MÊS ==========
            // Conta TODAS as vendas do mês (não apenas as 10 últimas)
            System.out.println("⏳ Contando vendas do mês...");

            int totalVendas = 0;

            try {
                totalVendas = vendasDAO.contarVendasDoMes(usuario.getIdUsuario());
                System.out.println("✅ Total de vendas do mês: " + totalVendas);

            } catch (Exception e) {
                System.err.println("❌ Erro ao contar vendas:");
                System.err.println("   " + e.getMessage());
                e.printStackTrace();
            }

            // Envia para JSP
            request.setAttribute("totalVendas", totalVendas);

            // ========== STEP 6: CALCULAR SOMA DO MÊS ==========
            // Soma de todas as vendas do mês atual
            System.out.println("⏳ Calculando valor total do mês...");

            double totalMes = 0.0;

            try {
                totalMes = vendasDAO.calcularTotalMes(usuario.getIdUsuario());
                System.out.println("✅ Total do mês: R$ " + totalMes);

            } catch (Exception e) {
                System.err.println("❌ Erro ao calcular total:");
                System.err.println("   " + e.getMessage());
                e.printStackTrace();
            }

            // Envia para JSP
            request.setAttribute("totalMes", totalMes);

            // ========== STEP 7: CALCULAR DADOS DO ANO ==========
            System.out.println("⏳ Calculando dados do ano...");

            // Contar vendas do ano
            int vendasAno = 0;
            try {
                vendasAno = vendasDAO.contarVendasDoAno(usuario.getIdUsuario());
                System.out.println("✅ Vendas do ano: " + vendasAno);
            } catch (Exception e) {
                System.err.println("❌ Erro ao contar vendas do ano: " + e.getMessage());
                e.printStackTrace();
            }

            // Calcular total do ano
            double totalAno = 0.0;
            try {
                totalAno = vendasDAO.calcularTotalAno(usuario.getIdUsuario());
                System.out.println("✅ Total do ano: R$ " + totalAno);
            } catch (Exception e) {
                System.err.println("❌ Erro ao calcular total do ano: " + e.getMessage());
                e.printStackTrace();
            }

            // Enviar para JSP
            request.setAttribute("vendasAno", vendasAno);
            request.setAttribute("totalAno", totalAno);

            // ========== STEP 8: ENVIAR DADOS E EXIBIR JSP ==========
            request.setAttribute("usuario", usuario);

            System.out.println("✅ Dados preparados para JSP:");
            System.out.println("   - Categorias: " + categorias.size());
            System.out.println("   - Últimas vendas (exibir): " + ultimasVendas.size());
            System.out.println("   - Total vendas do mês: " + totalVendas);
            System.out.println("   - Valor total do mês: R$ " + totalMes);
            System.out.println("   - Total vendas do ano: " + vendasAno);
            System.out.println("   - Valor total do ano: R$ " + totalAno);
            System.out.println("📄 Encaminhando para dashboard.jsp");
            System.out.println("===================================\n");

            request.getRequestDispatcher("/pages/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            // ========== TRATAMENTO DE ERRO GERAL ==========
            System.err.println("❌ ERRO CRÍTICO ao carregar dashboard:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            System.out.println("===================================\n");

            request.setAttribute("erro", "Erro ao carregar dashboard: " + e.getMessage());
            request.getRequestDispatcher("/pages/dashboard.jsp").forward(request, response);
        }
        // Conexão fecha automaticamente aqui
    }

    /* ================================================================
       MÉTODO POST - Cadastrar Nova Venda
       ================================================================

       URL: POST /dashboard

       Parâmetros obrigatórios:
       - categoria: ID numérico da categoria
       - valor: Número decimal, maior que zero

       Parâmetros opcionais:
       - descricao: Texto descritivo
       - emitirNF: S ou N (default N)
       - numeroNF: Obrigatório se emitirNF=S

       Validações:
       1. Categoria não vazia
       2. Valor não vazio e > 0
       3. Valor é número válido
       4. Se emitirNF=S, numeroNF é obrigatório

       Fluxo:
       1. Valida autenticação
       2. Lê e valida parâmetros
       3. Cria objeto Vendas
       4. Se NF marcada, cria NotaFiscal
       5. Insere via VendasDAO
       6. Redireciona com mensagem de sucesso

       Em caso de erro:
       - Redireciona para /dashboard com mensagem
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== DASHBOARD POST ==========");

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

        // ========== STEP 2: LER PARÂMETROS ==========
        String categoriaStr = request.getParameter("categoria");
        String valorStr = request.getParameter("valor");
        String descricao = request.getParameter("descricao");
        String emitirNF = request.getParameter("emitirNF");
        String numeroNF = request.getParameter("numeroNF");

        System.out.println("📋 Dados recebidos:");
        System.out.println("   - Categoria ID: " + categoriaStr);
        System.out.println("   - Valor: " + valorStr);
        System.out.println("   - Descrição: " + descricao);
        System.out.println("   - Emitir NF: " + emitirNF);
        System.out.println("   - Número NF: " + numeroNF);

        // ========== STEP 3: VALIDAR CATEGORIA ==========
        if (categoriaStr == null || categoriaStr.isEmpty()) {
            System.err.println("❌ Categoria vazia!");
            session.setAttribute("erro", "Categoria é obrigatória!");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // ========== STEP 4: VALIDAR VALOR ==========
        if (valorStr == null || valorStr.isEmpty()) {
            System.err.println("❌ Valor vazio!");
            session.setAttribute("erro", "Valor é obrigatório!");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // Converter valor (aceita vírgula ou ponto)
        double valor;
        try {
            valorStr = valorStr.replace(",", ".");
            valor = Double.parseDouble(valorStr);
            System.out.println("✅ Valor convertido: " + valor);
        } catch (NumberFormatException e) {
            System.err.println("❌ Valor inválido: " + valorStr);
            session.setAttribute("erro", "Valor inválido!");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // Validar valor > 0
        if (valor <= 0) {
            System.err.println("❌ Valor <= 0!");
            session.setAttribute("erro", "Valor deve ser maior que zero!");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        System.out.println("✅ Valor válido: R$ " + valor);

        // ========== STEP 5: CONVERTER CATEGORIA ID ==========
        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaStr);
            System.out.println("✅ Categoria ID: " + categoriaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ Categoria ID inválido!");
            session.setAttribute("erro", "Categoria inválida!");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // ========== STEP 6: VALIDAR NOTA FISCAL ==========
        // Default: não emitir NF
        if (emitirNF == null || emitirNF.isEmpty()) {
            emitirNF = "N";
        }

        System.out.println("📄 Nota Fiscal: " + emitirNF);

        // Se marcou para emitir, número é obrigatório
        if ("S".equalsIgnoreCase(emitirNF)) {
            if (numeroNF == null || numeroNF.trim().isEmpty()) {
                System.err.println("❌ NF marcada mas número vazio!");
                session.setAttribute("erro", "Número da Nota Fiscal é obrigatório quando marca 'Emitir NF'!");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            System.out.println("✅ Número NF: " + numeroNF.trim());
        }

        // ========== STEP 7: CONECTAR AO BANCO E INSERIR ==========
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão estabelecida");

            // ========== STEP 8: CRIAR OBJETO VENDAS ==========
            System.out.println("⏳ Criando objeto Vendas...");

            Vendas venda = new Vendas();
            venda.setDataVendas(new Date());
            venda.setValor((float) valor);
            venda.setDescricao(descricao != null ? descricao.trim() : "");
            venda.setUsuarioId(usuario.getIdUsuario());
            venda.setNotaFiscalEmitida(emitirNF.toUpperCase());

            // Categoria
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(categoriaId);
            venda.setCategoria(categoria);

            System.out.println("✅ Objeto Vendas criado:");
            System.out.println("   - Data: " + venda.getDataVendas());
            System.out.println("   - Valor: R$ " + venda.getValor());
            System.out.println("   - Categoria ID: " + categoriaId);
            System.out.println("   - Usuário ID: " + venda.getUsuarioId());
            System.out.println("   - NF Emitida: " + venda.getNotaFiscalEmitida());

            // ========== STEP 9: CRIAR NOTA FISCAL (se marcado) ==========
            if ("S".equalsIgnoreCase(emitirNF) && numeroNF != null && !numeroNF.trim().isEmpty()) {
                System.out.println("⏳ Criando Nota Fiscal...");

                NotaFiscal nf = new NotaFiscal();
                nf.setNumero(numeroNF.trim());
                nf.setDataEmissao(new Date());
                nf.setValor((float) valor);
                venda.setNotaFiscal(nf);

                System.out.println("✅ Nota Fiscal criada:");
                System.out.println("   - Número: " + nf.getNumero());
                System.out.println("   - Data: " + nf.getDataEmissao());
                System.out.println("   - Valor: R$ " + nf.getValor());
            }

            // ========== STEP 10: INSERIR NO BANCO ==========
            System.out.println("⏳ Inserindo venda no banco...");

            VendasDAO vendasDAO = new VendasDAO(conexao);

            try {
                vendasDAO.inserir(venda);

                System.out.println("✅ VENDA CADASTRADA COM SUCESSO!");
                System.out.println("   - ID gerado: " + venda.getIdVendas());
                System.out.println("   - Valor: R$ " + venda.getValor());
                System.out.println("   - Com NF: " + ("S".equals(emitirNF) ? "Sim" : "Não"));
                System.out.println("===================================\n");

                // Mensagem de sucesso
                if ("S".equalsIgnoreCase(emitirNF)) {
                    session.setAttribute("sucesso", "Venda cadastrada com Nota Fiscal " + numeroNF + "!");
                } else {
                    session.setAttribute("sucesso", "Venda cadastrada com sucesso!");
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao inserir venda:");
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao cadastrar venda: " + e.getMessage());
            }

            System.out.println("===================================\n");
            response.sendRedirect(request.getContextPath() + "/dashboard");

        } catch (Exception e) {
            // ========== TRATAMENTO DE ERRO GERAL ==========
            System.err.println("❌ ERRO de conexão:");
            e.printStackTrace();
            System.out.println("===================================\n");

            session.setAttribute("erro", "Erro de conexão: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
        // Conexão fecha automaticamente
    }
}

/* ================================================================
   RESUMO DO CONTROLLER
   ================================================================

   FUNCIONALIDADES:
   1. GET  /dashboard → Exibe painel principal com:
      - Últimas 10 vendas
      - Total do mês
      - Categorias (dropdown)

   2. POST /dashboard → Cadastra nova venda:
      - Com ou sem Nota Fiscal
      - Validações robustas

   DADOS EXIBIDOS (GET):
   - categorias: List<Categoria> (para formulário)
   - ultimasVendas: List<Vendas> (limite 10)
   - totalMes: Double (soma do mês)
   - usuario: Usuario (da sessão)

   CAMPOS DO FORMULÁRIO (POST):
   - categoria* (ID, obrigatório)
   - valor* (decimal > 0, obrigatório)
   - descricao (opcional)
   - emitirNF (S/N, default N)
   - numeroNF (obrigatório se S)

   VALIDAÇÕES:
   ✅ Autenticação obrigatória
   ✅ Categoria não vazia
   ✅ Valor não vazio e > 0
   ✅ Valor é número válido
   ✅ Se NF=S, número obrigatório

   TABELAS:
   - vendas (insert)
   - nota_fiscal (insert se marcado)
   - categoria (select para dropdown)
   - usuario (session)

   FLUXO COMPLETO:
   Login → Dashboard → Cadastrar Venda → Dashboard (atualizado)

   MENSAGENS:
   - Sucesso: "Venda cadastrada com sucesso!"
   - Com NF: "Venda cadastrada com Nota Fiscal 12345!"
   - Erro: Específica por validação

   SEGURANÇA:
   ✅ Validação de sessão
   ✅ PreparedStatement (via DAO)
   ✅ Try-with-resources
   ✅ Validações de tipo e formato

   EXEMPLO:
   ```
   // Venda simples:
   POST /dashboard
   categoria=1&valor=100.50&descricao=Venda teste&emitirNF=N

   // Venda com NF:
   POST /dashboard
   categoria=2&valor=250.00&emitirNF=S&numeroNF=98765
   ```

   OBSERVAÇÕES:
   - Conexão fecha automaticamente
   - Logs detalhados em cada etapa
   - Redireciona após POST (PRG pattern)
   - Lista sempre atualizada após insert
   ================================================================ */