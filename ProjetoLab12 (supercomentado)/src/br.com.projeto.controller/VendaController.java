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

import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.NotaFiscal;
import br.com.projeto.dao.CategoriaDAO;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.utils.Conexao;

/**
 * ================================================================
 * VENDA CONTROLLER - Controle Completo de Vendas
 * ================================================================
 *
 * Este controller gerencia TODAS as operações relacionadas a vendas:
 *
 * FUNCIONALIDADES:
 * 1. Cadastrar nova venda (POST /venda)
 * 2. Editar venda existente (GET /venda?acao=editar&id=X + POST)
 * 3. Excluir venda (GET /venda?acao=excluir&id=X)
 *
 * ROTAS:
 * - GET  /venda                         → Exibe formulário de nova venda
 * - POST /venda                         → Cadastra nova venda
 * - GET  /venda?acao=editar&id=123      → Exibe formulário de edição
 * - POST /venda?acao=editar             → Salva edição
 * - GET  /venda?acao=excluir&id=123     → Exclui venda
 *
 * PARÂMETROS:
 * - acao: "editar" ou "excluir"
 * - id: ID da venda (para editar/excluir)
 * - categoria: ID da categoria
 * - valor: Valor da venda
 * - descricao: Descrição opcional
 * - emitirNF: "S" ou "N"
 * - numeroNF: Número da nota fiscal (se emitirNF = "S")
 *
 * @author Sistema MEI
 * @version 3.0 - Versão completa com editar/excluir
 */
@WebServlet("/venda")
public class VendaController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Roteador de Ações
       ================================================================

       Decide qual ação executar baseado no parâmetro "acao":

       - SEM acao        → Exibe formulário de nova venda
       - acao=editar     → Exibe formulário de edição (id obrigatório)
       - acao=excluir    → Exclui venda (id obrigatório)

       FLUXO:
       1. Verifica se usuário está logado
       2. Lê parâmetro "acao"
       3. Roteia para método apropriado
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ========== VALIDAÇÃO DE SESSÃO ==========
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            System.err.println("❌ VENDA GET: Usuário não logado!");
            response.sendRedirect("login");
            return;
        }

        System.out.println("\n========== DEBUG VENDA GET ==========");
        System.out.println("✅ Usuário logado: " + usuario.getNome());

        // ========== ROTEAMENTO DE AÇÕES ==========
        String acao = request.getParameter("acao");
        System.out.println("📋 Ação solicitada: " + (acao != null ? acao : "nova venda (padrão)"));

        // ROTA 1: Editar venda existente
        if ("editar".equalsIgnoreCase(acao)) {
            System.out.println("🔀 Roteando para: exibirFormularioEdicao()");
            exibirFormularioEdicao(request, response, session, usuario);
            return;
        }

        // ROTA 2: Excluir venda
        if ("excluir".equalsIgnoreCase(acao)) {
            System.out.println("🔀 Roteando para: excluirVenda()");
            excluirVenda(request, response, session, usuario);
            return;
        }

        // ROTA 3 (PADRÃO): Exibir formulário de nova venda
        System.out.println("🔀 Roteando para: exibirFormularioNovaVenda()");
        exibirFormularioNovaVenda(request, response, session, usuario);
    }

    /* ================================================================
       ROTA 1: Exibir Formulário de Nova Venda
       ================================================================

       URL: GET /venda

       Ação:
       1. Busca lista de categorias no banco
       2. Envia categorias para o JSP
       3. Exibe formulário de cadastro

       JSP: cadastro_venda.jsp
    */
    private void exibirFormularioNovaVenda(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Usuario usuario) throws ServletException, IOException {

        System.out.println("📝 Iniciando exibição de formulário de NOVA VENDA");

        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão obtida com sucesso");

            // ========== BUSCAR CATEGORIAS ==========
            CategoriaDAO categoriaDAO = new CategoriaDAO(conexao);
            System.out.println("✅ CategoriaDAO criado");

            List<Categoria> categorias = new ArrayList<>();

            try {
                System.out.println("⏳ Buscando categorias no banco...");
                categorias = categoriaDAO.listar();
                System.out.println("✅ Categorias retornadas: " + (categorias != null ? categorias.size() : "NULL"));

                // Log de cada categoria encontrada
                if (categorias != null && !categorias.isEmpty()) {
                    System.out.println("📋 Lista de categorias:");
                    for (Categoria c : categorias) {
                        System.out.println("  - ID: " + c.getIdCategoria() +
                                ", Nome: " + c.getNomeCategoria() +
                                ", Ativo: " + c.isAtivo());
                    }
                } else {
                    System.err.println("⚠️ Lista de categorias VAZIA!");
                }

            } catch (Exception e) {
                System.err.println("❌ ERRO ao listar categorias:");
                System.err.println("   Mensagem: " + e.getMessage());
                System.err.println("   Tipo: " + e.getClass().getName());
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao carregar categorias: " + e.getMessage());
            }

            // ========== ENVIAR PARA JSP ==========
            System.out.println("📤 Setando categorias no request: " + categorias.size() + " itens");
            request.setAttribute("categorias", categorias);

            System.out.println("✅ Encaminhando para cadastro_venda.jsp");
            System.out.println("=====================================\n");

            request.getRequestDispatcher("cadastro_venda.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ ERRO GERAL ao exibir formulário:");
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao carregar formulário: " + e.getMessage());
            response.sendRedirect("dashboard");
        }
    }

    /* ================================================================
       ROTA 2: Exibir Formulário de Edição
       ================================================================

       URL: GET /venda?acao=editar&id=123

       Ação:
       1. Valida ID (obrigatório)
       2. Busca venda no banco
       3. Busca categorias
       4. Envia venda + categorias para JSP
       5. Exibe formulário pré-preenchido

       JSP: editar-venda.jsp
    */
    private void exibirFormularioEdicao(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Usuario usuario) throws ServletException, IOException {

        System.out.println("✏️ Iniciando exibição de formulário de EDIÇÃO");

        // ========== VALIDAR ID ==========
        String idStr = request.getParameter("id");
        System.out.println("📋 ID recebido: " + idStr);

        if (idStr == null || idStr.isEmpty()) {
            System.err.println("❌ ID não informado!");
            session.setAttribute("erro", "ID da venda não informado!");
            response.sendRedirect("historico");
            return;
        }

        int vendaId;
        try {
            vendaId = Integer.parseInt(idStr);
            System.out.println("✅ ID convertido: " + vendaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ ID inválido: " + idStr);
            session.setAttribute("erro", "ID inválido!");
            response.sendRedirect("historico");
            return;
        }

        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão obtida");

            // ========== BUSCAR VENDA ==========
            VendasDAO vendasDAO = new VendasDAO(conexao);
            System.out.println("⏳ Buscando venda ID " + vendaId + "...");

            Vendas venda = vendasDAO.buscar(vendaId);

            if (venda == null) {
                System.err.println("❌ Venda não encontrada! ID: " + vendaId);
                session.setAttribute("erro", "Venda não encontrada!");
                response.sendRedirect("historico");
                return;
            }

            System.out.println("✅ Venda encontrada:");
            System.out.println("   - ID: " + venda.getIdVendas());
            System.out.println("   - Valor: " + venda.getValor());
            System.out.println("   - Categoria: " + (venda.getCategoria() != null ? venda.getCategoria().getIdCategoria() : "NULL"));
            System.out.println("   - NF Emitida: " + venda.getNotaFiscalEmitida());

            // ========== BUSCAR CATEGORIAS ==========
            CategoriaDAO categoriaDAO = new CategoriaDAO(conexao);
            System.out.println("⏳ Buscando categorias...");

            List<Categoria> categorias = categoriaDAO.listar();
            System.out.println("✅ Categorias carregadas: " + categorias.size());

            // ========== ENVIAR PARA JSP ==========
            request.setAttribute("venda", venda);
            request.setAttribute("categorias", categorias);

            System.out.println("✅ Encaminhando para editar-venda.jsp");
            System.out.println("=====================================\n");

            request.getRequestDispatcher("/editar-venda.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ ERRO ao carregar venda para edição:");
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao carregar venda: " + e.getMessage());
            response.sendRedirect("historico");
        }
    }

    /* ================================================================
       ROTA 3: Excluir Venda (Exclusão Lógica)
       ================================================================

       URL: GET /venda?acao=excluir&id=123

       Ação:
       1. Valida ID (obrigatório)
       2. Chama VendasDAO.excluir(id)
       3. Faz UPDATE vendas SET ativo = false (não DELETE!)
       4. Redireciona para histórico

       NOTA: Usa exclusão LÓGICA (soft delete) para preservar histórico
    */
    private void excluirVenda(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Usuario usuario) throws ServletException, IOException {

        System.out.println("🗑️ Iniciando EXCLUSÃO de venda");

        // ========== VALIDAR ID ==========
        String idStr = request.getParameter("id");
        System.out.println("📋 ID recebido: " + idStr);

        if (idStr == null || idStr.isEmpty()) {
            System.err.println("❌ ID não informado!");
            session.setAttribute("erro", "ID da venda não informado!");
            response.sendRedirect("historico");
            return;
        }

        int vendaId;
        try {
            vendaId = Integer.parseInt(idStr);
            System.out.println("✅ ID convertido: " + vendaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ ID inválido: " + idStr);
            session.setAttribute("erro", "ID inválido!");
            response.sendRedirect("historico");
            return;
        }

        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão obtida");

            // ========== EXCLUIR VENDA (LÓGICO) ==========
            VendasDAO vendasDAO = new VendasDAO(conexao);
            System.out.println("⏳ Executando exclusão lógica (ativo = false)...");
            System.out.println("   SQL: UPDATE vendas SET ativo = false WHERE id_vendas = " + vendaId);

            vendasDAO.excluir(vendaId);

            System.out.println("✅ Venda ID " + vendaId + " excluída com sucesso!");
            System.out.println("   ⚠️ Exclusão LÓGICA: registro não foi deletado, apenas desativado");
            System.out.println("   ✅ Venda não aparecerá mais nas consultas (WHERE ativo = true)");
            System.out.println("=====================================\n");

            // ========== FEEDBACK E REDIRECT ==========
            session.setAttribute("sucesso", "Venda excluída com sucesso!");
            response.sendRedirect("historico");

        } catch (Exception e) {
            System.err.println("❌ ERRO ao excluir venda:");
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao excluir venda: " + e.getMessage());
            response.sendRedirect("historico");
        }
    }

    /* ================================================================
       MÉTODO POST - Roteador de Ações
       ================================================================

       Decide qual ação executar baseado no parâmetro "acao":

       - SEM acao        → Cadastra nova venda
       - acao=editar     → Salva edição de venda existente

       FLUXO:
       1. Verifica se usuário está logado
       2. Lê parâmetro "acao"
       3. Roteia para método apropriado
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ========== VALIDAÇÃO DE SESSÃO ==========
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            System.err.println("❌ VENDA POST: Usuário não logado!");
            response.sendRedirect("login");
            return;
        }

        System.out.println("\n========== DEBUG VENDA POST ==========");
        System.out.println("✅ Usuário logado: " + usuario.getNome());

        // ========== ROTEAMENTO DE AÇÕES ==========
        String acao = request.getParameter("acao");
        System.out.println("📋 Ação solicitada: " + (acao != null ? acao : "cadastrar (padrão)"));

        // ROTA 1: Salvar edição
        if ("editar".equalsIgnoreCase(acao)) {
            System.out.println("🔀 Roteando para: salvarEdicao()");
            salvarEdicao(request, response, session, usuario);
            return;
        }

        // ROTA 2 (PADRÃO): Cadastrar nova venda
        System.out.println("🔀 Roteando para: cadastrarNovaVenda()");
        cadastrarNovaVenda(request, response, session, usuario);
    }

    /* ================================================================
       ROTA 1: Cadastrar Nova Venda
       ================================================================

       URL: POST /venda

       Parâmetros:
       - categoria: ID da categoria
       - valor: Valor da venda
       - descricao: Descrição opcional
       - emitirNF: "S" ou "N" (checkbox)
       - numeroNF: Número da NF (se emitirNF = "S")

       Ação:
       1. Valida parâmetros
       2. Cria objeto Vendas
       3. Se emitirNF = "S", cria NotaFiscal
       4. Insere no banco
       5. Redireciona para dashboard
    */
    private void cadastrarNovaVenda(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Usuario usuario) throws ServletException, IOException {

        System.out.println("💾 Iniciando cadastro de NOVA VENDA");

        // ========== LER PARÂMETROS ==========
        String categoriaStr = request.getParameter("categoria");
        String valorStr = request.getParameter("valor");
        String descricao = request.getParameter("descricao");
        String emitirNF = request.getParameter("emitirNF");
        String numeroNF = request.getParameter("numeroNF");

        System.out.println("📋 Parâmetros recebidos:");
        System.out.println("   - Categoria: " + categoriaStr);
        System.out.println("   - Valor: " + valorStr);
        System.out.println("   - Descrição: " + descricao);
        System.out.println("   - Emitir NF: " + emitirNF);
        System.out.println("   - Número NF: " + numeroNF);

        // ========== VALIDAÇÃO DE CATEGORIA ==========
        if (categoriaStr == null || categoriaStr.isEmpty()) {
            System.err.println("❌ Categoria vazia!");
            session.setAttribute("erro", "Categoria é obrigatória!");
            response.sendRedirect("venda");
            return;
        }

        // ========== VALIDAÇÃO DE VALOR ==========
        if (valorStr == null || valorStr.isEmpty()) {
            System.err.println("❌ Valor vazio!");
            session.setAttribute("erro", "Valor é obrigatório!");
            response.sendRedirect("venda");
            return;
        }

        double valor;
        try {
            valorStr = valorStr.replace(",", ".");
            valor = Double.parseDouble(valorStr);
            System.out.println("✅ Valor convertido: " + valor);
        } catch (NumberFormatException e) {
            System.err.println("❌ Erro ao converter valor: " + valorStr);
            session.setAttribute("erro", "Valor inválido!");
            response.sendRedirect("venda");
            return;
        }

        if (valor <= 0) {
            System.err.println("❌ Valor <= 0!");
            session.setAttribute("erro", "Valor deve ser maior que zero!");
            response.sendRedirect("venda");
            return;
        }

        // ========== CONVERSÃO DE CATEGORIA ID ==========
        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaStr);
            System.out.println("✅ Categoria ID: " + categoriaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ Erro ao converter categoria ID!");
            session.setAttribute("erro", "Categoria inválida!");
            response.sendRedirect("venda");
            return;
        }

        // ========== VALIDAÇÃO DE NOTA FISCAL ==========
        if (emitirNF == null || emitirNF.isEmpty()) {
            emitirNF = "N";
            System.out.println("⚠️ emitirNF vazio, setando padrão: N");
        }

        // Se marcou "Emitir NF", número é obrigatório
        if ("S".equalsIgnoreCase(emitirNF)) {
            if (numeroNF == null || numeroNF.trim().isEmpty()) {
                System.err.println("❌ NF marcada mas número vazio!");
                session.setAttribute("erro", "Número da Nota Fiscal é obrigatório!");
                response.sendRedirect("venda");
                return;
            }
            System.out.println("✅ NF será emitida: " + numeroNF.trim());
        }

        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão obtida");

            // ========== CRIAR OBJETO VENDAS ==========
            Vendas venda = new Vendas();
            venda.setDataVendas(new Date());
            venda.setValor((float) valor);
            venda.setDescricao(descricao != null ? descricao.trim() : "");
            venda.setUsuarioId(usuario.getIdUsuario());
            venda.setNotaFiscalEmitida(emitirNF.toUpperCase());

            Categoria categoria = new Categoria();
            categoria.setIdCategoria(categoriaId);
            venda.setCategoria(categoria);

            System.out.println("📦 Objeto Venda criado:");
            System.out.println("   - Data: " + venda.getDataVendas());
            System.out.println("   - Valor: " + venda.getValor());
            System.out.println("   - Usuario ID: " + venda.getUsuarioId());
            System.out.println("   - Categoria ID: " + categoriaId);
            System.out.println("   - NF Emitida: " + venda.getNotaFiscalEmitida());

            // ========== CRIAR NOTA FISCAL (SE NECESSÁRIO) ==========
            if ("S".equalsIgnoreCase(emitirNF) && numeroNF != null && !numeroNF.trim().isEmpty()) {
                NotaFiscal nf = new NotaFiscal();
                nf.setNumero(numeroNF.trim());
                nf.setDataEmissao(new Date());
                nf.setValor((float) valor);

                venda.setNotaFiscal(nf);

                System.out.println("📄 Nota Fiscal criada:");
                System.out.println("   - Número: " + nf.getNumero());
                System.out.println("   - Data: " + nf.getDataEmissao());
                System.out.println("   - Valor: " + nf.getValor());
            }

            // ========== INSERIR NO BANCO ==========
            VendasDAO vendasDAO = new VendasDAO(conexao);
            System.out.println("⏳ Tentando inserir venda no banco...");

            try {
                vendasDAO.inserir(venda);
                System.out.println("✅ Venda inserida com sucesso!");
                System.out.println("   - ID gerado: " + venda.getIdVendas());

                // Se tinha NF, foi inserida também
                if (venda.getNotaFiscal() != null) {
                    System.out.println("   - Nota Fiscal inserida: " + venda.getNotaFiscal().getNumero());
                }

                System.out.println("=====================================\n");

                // ========== FEEDBACK E REDIRECT ==========
                String mensagemSucesso = "Venda cadastrada com sucesso!";
                if ("S".equalsIgnoreCase(emitirNF)) {
                    mensagemSucesso += " (Nota Fiscal: " + numeroNF.trim() + ")";
                }

                session.setAttribute("sucesso", mensagemSucesso);
                response.sendRedirect("dashboard");

            } catch (Exception e) {
                System.err.println("❌ ERRO ao inserir venda:");
                System.err.println("   SQL State: " + (e instanceof java.sql.SQLException ?
                        ((java.sql.SQLException)e).getSQLState() : "N/A"));
                System.err.println("   Mensagem: " + e.getMessage());
                e.printStackTrace();
                System.out.println("=====================================\n");

                session.setAttribute("erro", "Erro ao cadastrar venda: " + e.getMessage());
                response.sendRedirect("venda");
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO GERAL ao cadastrar venda:");
            e.printStackTrace();
            System.out.println("=====================================\n");

            session.setAttribute("erro", "Erro de conexão: " + e.getMessage());
            response.sendRedirect("venda");
        }
    }

    /* ================================================================
       ROTA 2: Salvar Edição de Venda
       ================================================================

       URL: POST /venda?acao=editar

       Parâmetros:
       - id: ID da venda (obrigatório)
       - categoria: ID da categoria
       - valor: Valor da venda
       - descricao: Descrição opcional
       - emitirNF: "S" ou "N"
       - numeroNF: Número da NF (se emitirNF = "S")

       Ação:
       1. Valida ID e parâmetros
       2. Busca venda existente
       3. Atualiza dados
       4. Atualiza/cria/remove Nota Fiscal
       5. Salva no banco
       6. Redireciona para histórico
    */
    private void salvarEdicao(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Usuario usuario) throws ServletException, IOException {

        System.out.println("💾 Iniciando EDIÇÃO de venda");

        // ========== LER PARÂMETROS ==========
        String idStr = request.getParameter("id");
        String categoriaStr = request.getParameter("categoria");
        String valorStr = request.getParameter("valor");
        String descricao = request.getParameter("descricao");
        String emitirNF = request.getParameter("emitirNF");
        String numeroNF = request.getParameter("numeroNF");

        System.out.println("📋 Parâmetros recebidos:");
        System.out.println("   - ID: " + idStr);
        System.out.println("   - Categoria: " + categoriaStr);
        System.out.println("   - Valor: " + valorStr);
        System.out.println("   - Emitir NF: " + emitirNF);
        System.out.println("   - Número NF: " + numeroNF);

        // ========== VALIDAR ID ==========
        if (idStr == null || idStr.isEmpty()) {
            System.err.println("❌ ID não informado!");
            session.setAttribute("erro", "ID da venda não informado!");
            response.sendRedirect("historico");
            return;
        }

        int vendaId;
        try {
            vendaId = Integer.parseInt(idStr);
            System.out.println("✅ ID convertido: " + vendaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ ID inválido!");
            session.setAttribute("erro", "ID inválido!");
            response.sendRedirect("historico");
            return;
        }

        // ========== VALIDAÇÕES (mesmo código do cadastro) ==========
        if (categoriaStr == null || categoriaStr.isEmpty()) {
            System.err.println("❌ Categoria vazia!");
            session.setAttribute("erro", "Categoria é obrigatória!");
            response.sendRedirect("venda?acao=editar&id=" + idStr);
            return;
        }

        if (valorStr == null || valorStr.isEmpty()) {
            System.err.println("❌ Valor vazio!");
            session.setAttribute("erro", "Valor é obrigatório!");
            response.sendRedirect("venda?acao=editar&id=" + idStr);
            return;
        }

        double valor;
        try {
            valorStr = valorStr.replace(",", ".");
            valor = Double.parseDouble(valorStr);
            System.out.println("✅ Valor convertido: " + valor);
        } catch (NumberFormatException e) {
            System.err.println("❌ Erro ao converter valor!");
            session.setAttribute("erro", "Valor inválido!");
            response.sendRedirect("venda?acao=editar&id=" + idStr);
            return;
        }

        if (valor <= 0) {
            System.err.println("❌ Valor <= 0!");
            session.setAttribute("erro", "Valor deve ser maior que zero!");
            response.sendRedirect("venda?acao=editar&id=" + idStr);
            return;
        }

        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaStr);
            System.out.println("✅ Categoria ID: " + categoriaId);
        } catch (NumberFormatException e) {
            System.err.println("❌ Erro ao converter categoria ID!");
            session.setAttribute("erro", "Categoria inválida!");
            response.sendRedirect("venda?acao=editar&id=" + idStr);
            return;
        }

        if (emitirNF == null || emitirNF.isEmpty()) {
            emitirNF = "N";
        }

        if ("S".equalsIgnoreCase(emitirNF)) {
            if (numeroNF == null || numeroNF.trim().isEmpty()) {
                System.err.println("❌ NF marcada mas número vazio!");
                session.setAttribute("erro", "Número da Nota Fiscal é obrigatório!");
                response.sendRedirect("venda?acao=editar&id=" + idStr);
                return;
            }
        }

        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão obtida");

            // ========== BUSCAR VENDA EXISTENTE ==========
            VendasDAO vendasDAO = new VendasDAO(conexao);
            System.out.println("⏳ Buscando venda ID " + vendaId + "...");

            Vendas venda = vendasDAO.buscar(vendaId);

            if (venda == null) {
                System.err.println("❌ Venda não encontrada!");
                session.setAttribute("erro", "Venda não encontrada!");
                response.sendRedirect("historico");
                return;
            }

            System.out.println("✅ Venda encontrada, atualizando dados...");

            // ========== ATUALIZAR DADOS ==========
            venda.setValor((float) valor);
            venda.setDescricao(descricao != null ? descricao.trim() : "");
            venda.setNotaFiscalEmitida(emitirNF.toUpperCase());

            Categoria categoria = new Categoria();
            categoria.setIdCategoria(categoriaId);
            venda.setCategoria(categoria);

            System.out.println("📝 Dados atualizados:");
            System.out.println("   - Novo valor: " + venda.getValor());
            System.out.println("   - Nova categoria: " + categoriaId);
            System.out.println("   - Nova NF: " + venda.getNotaFiscalEmitida());

            // ========== ATUALIZAR NOTA FISCAL ==========
            if ("S".equalsIgnoreCase(emitirNF) && numeroNF != null && !numeroNF.trim().isEmpty()) {
                // Criar ou atualizar NF
                NotaFiscal nf = venda.getNotaFiscal();
                if (nf == null) {
                    nf = new NotaFiscal();
                    nf.setDataEmissao(new Date());
                    System.out.println("📄 Criando nova Nota Fiscal");
                } else {
                    System.out.println("📄 Atualizando Nota Fiscal existente");
                }

                nf.setNumero(numeroNF.trim());
                nf.setValor((float) valor);
                venda.setNotaFiscal(nf);

                System.out.println("   - Número: " + nf.getNumero());
                System.out.println("   - Valor: " + nf.getValor());

            } else {
                // Remover NF se mudou para "N"
                venda.setNotaFiscal(null);
                System.out.println("🗑️ Nota Fiscal removida (emitirNF = N)");
            }

            // ========== SALVAR NO BANCO ==========
            System.out.println("⏳ Salvando alterações no banco...");

            try {
                vendasDAO.editar(venda);
                System.out.println("✅ Venda ID " + vendaId + " atualizada com sucesso!");
                System.out.println("=====================================\n");

                session.setAttribute("sucesso", "Venda atualizada com sucesso!");
                response.sendRedirect("historico");

            } catch (Exception e) {
                System.err.println("❌ ERRO ao atualizar venda:");
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao atualizar venda: " + e.getMessage());
                response.sendRedirect("venda?acao=editar&id=" + idStr);
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO GERAL ao editar venda:");
            e.printStackTrace();
            session.setAttribute("erro", "Erro de conexão: " + e.getMessage());
            response.sendRedirect("venda?acao=editar&id=" + idStr);
        }
    }
}

/* ================================================================
   RESUMO DE FUNCIONALIDADES
   ================================================================

   CADASTRAR NOVA VENDA:
   - URL: POST /venda
   - Valida: categoria, valor
   - Opcional: descrição, NF
   - Insere: vendas + nota_fiscal (se marcado)
   - Redireciona: /dashboard

   EDITAR VENDA:
   - URL: GET /venda?acao=editar&id=123 (formulário)
   - URL: POST /venda?acao=editar (salvar)
   - Busca venda existente
   - Atualiza dados
   - Gerencia NF: criar/atualizar/remover
   - Redireciona: /historico

   EXCLUIR VENDA:
   - URL: GET /venda?acao=excluir&id=123
   - Exclusão LÓGICA (ativo = false)
   - Preserva histórico
   - Redireciona: /historico

   VANTAGENS DESTA ABORDAGEM:
   ✅ Tudo em 1 controller (fácil manutenção)
   ✅ Roteamento claro por parâmetro "acao"
   ✅ Métodos privados bem organizados
   ✅ Logs detalhados em cada etapa
   ✅ Validações robustas
   ✅ Exclusão lógica (segura)
   ✅ Gerenciamento completo de NF
   ================================================================ */