package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import br.com.projeto.model.Usuario;
import br.com.projeto.dao.UsuarioDAO;
import br.com.projeto.utils.Conexao;

/**
 * ================================================================
 * LOGIN CONTROLLER - Autenticação e Cadastro
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia todo o processo de autenticação do sistema MEI.
 * Unifica Login, Cadastro e Logout em um único controller.
 *
 * FUNCIONALIDADES:
 * 1. Login de usuários (autenticação com CPF + senha)
 * 2. Cadastro de novos usuários MEI
 * 3. Logout (encerramento de sessão)
 *
 * ROTAS MAPEADAS:
 * - GET  /login     → Exibe formulário de login
 * - POST /login     → Processa autenticação
 * - GET  /cadastro  → Exibe formulário de cadastro
 * - POST /cadastro  → Processa novo usuário
 * - GET  /logout    → Encerra sessão e redireciona
 *
 * TABELA: usuario
 * Schema:
 * - id_usuario (PK, AUTO_INCREMENT)
 * - cpf (UNIQUE, NOT NULL) → Usado como username
 * - nome (NOT NULL)
 * - email (UNIQUE, nullable)
 * - cnpj (14 dígitos, opcional para MEI)
 * - senha (VARCHAR 60, hash BCrypt)
 * - ativo (BOOLEAN, default true)
 * - data_cadastro (TIMESTAMP)
 *
 * SEGURANÇA:
 * ✅ Senhas criptografadas com BCrypt (salt 10)
 * ✅ Validação de CPF único
 * ✅ Validação de email único
 * ✅ Sessões com timeout de 30 minutos
 * ✅ PreparedStatement para prevenir SQL injection
 * ✅ Máscaras removidas antes de salvar
 *
 * FLUXO DE LOGIN:
 * 1. Usuário digita CPF (com ou sem máscara) + senha
 * 2. Remove máscara do CPF (fica só números)
 * 3. Busca usuário no banco por CPF
 * 4. Valida senha com BCrypt.checkpw()
 * 5. Se OK: cria sessão e redireciona para /dashboard
 * 6. Se ERRO: exibe mensagem e volta para /login
 *
 * FLUXO DE CADASTRO:
 * 1. Valida todos os campos obrigatórios
 * 2. Remove máscaras (CPF, CNPJ)
 * 3. Valida formato (CPF=11 dígitos, CNPJ=14 dígitos)
 * 4. Verifica se CPF já existe
 * 5. Verifica se email já existe
 * 6. Gera hash BCrypt da senha
 * 7. Insere no banco via UsuarioDAO
 * 8. Redireciona para /login com mensagem de sucesso
 *
 * EXEMPLO DE USO:
 * ```
 * // Login:
 * POST /login
 * cpf=123.456.789-01&senha=minhaSenha123
 *
 * // Cadastro:
 * POST /cadastro
 * cpf=123.456.789-01&nome=João Silva&email=joao@email.com&
 * cnpj=12.345.678/0001-90&senha=senha123&confirmarSenha=senha123
 *
 * // Logout:
 * GET /logout
 * ```
 *
 * @author Sistema MEI
 * @version 3.0 - Com CNPJ e super comentado
 * @see UsuarioDAO
 * @see Usuario
 */
@WebServlet({"/login", "/cadastro", "/logout"})
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Roteador de Páginas
       ================================================================

       Decide qual página exibir baseado na rota acessada:

       /login    → exibirLogin()    → login.jsp
       /cadastro → exibirCadastro() → cadastro.jsp
       /logout   → executarLogout() → redireciona para /login

       IMPORTANTE: Se usuário já está logado e tenta acessar
       /login ou /cadastro, redireciona para /dashboard
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ========== IDENTIFICAR ROTA ==========
        String path = request.getServletPath();
        System.out.println("\n========== LOGIN CONTROLLER GET ==========");
        System.out.println("📍 Rota acessada: " + path);

        // ========== ROTEAMENTO ==========
        switch (path) {
            case "/login":
                System.out.println("🔀 Roteando para: exibirLogin()");
                exibirLogin(request, response);
                break;

            case "/cadastro":
                System.out.println("🔀 Roteando para: exibirCadastro()");
                exibirCadastro(request, response);
                break;

            case "/logout":
                System.out.println("🔀 Roteando para: executarLogout()");
                executarLogout(request, response);
                break;

            default:
                System.err.println("❌ Rota GET desconhecida: " + path);
                System.out.println("==========================================\n");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /* ================================================================
       MÉTODO POST - Roteador de Ações
       ================================================================

       Processa dados enviados por formulários:

       POST /login    → processarLogin()    → Autentica usuário
       POST /cadastro → processarCadastro() → Cria novo usuário

       NOTA: Logout não usa POST, apenas GET
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ========== IDENTIFICAR ROTA ==========
        String path = request.getServletPath();
        System.out.println("\n========== LOGIN CONTROLLER POST ==========");
        System.out.println("📍 Rota acessada: " + path);

        // ========== ROTEAMENTO ==========
        switch (path) {
            case "/login":
                System.out.println("🔀 Roteando para: processarLogin()");
                processarLogin(request, response);
                break;

            case "/cadastro":
                System.out.println("🔀 Roteando para: processarCadastro()");
                processarCadastro(request, response);
                break;

            default:
                System.err.println("❌ Rota POST desconhecida: " + path);
                System.out.println("===========================================\n");
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /* ================================================================
       ROTA 1: EXIBIR FORMULÁRIO DE LOGIN
       ================================================================

       URL: GET /login

       Comportamento:
       - Se usuário JÁ está logado → redireciona para /dashboard
       - Se usuário NÃO está logado → exibe login.jsp

       JSP: login.jsp

       Sessão: Verifica atributo "usuario"
    */

    private void exibirLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("📝 Iniciando exibição de LOGIN");

        // ========== VERIFICAR SE JÁ ESTÁ LOGADO ==========
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("usuario") != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            System.out.println("✅ Usuário já logado: " + usuario.getNome());
            System.out.println("➡️ Redirecionando para dashboard");
            System.out.println("==========================================\n");
            response.sendRedirect("dashboard");
            return;
        }

        // ========== EXIBIR FORMULÁRIO ==========
        System.out.println("📄 Exibindo formulário de login");
        System.out.println("==========================================\n");
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /* ================================================================
       ROTA 2: EXIBIR FORMULÁRIO DE CADASTRO
       ================================================================

       URL: GET /cadastro

       Comportamento:
       - Se usuário JÁ está logado → redireciona para /dashboard
       - Se usuário NÃO está logado → exibe cadastro.jsp

       JSP: cadastro.jsp

       Campos do formulário:
       - cpf (obrigatório, 11 dígitos)
       - nome (obrigatório)
       - email (opcional, mas único)
       - cnpj (opcional, 14 dígitos se informado)
       - senha (obrigatório, mínimo 6 caracteres)
       - confirmarSenha (obrigatório, deve coincidir)
    */

    private void exibirCadastro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("📝 Iniciando exibição de CADASTRO");

        // ========== VERIFICAR SE JÁ ESTÁ LOGADO ==========
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("usuario") != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            System.out.println("✅ Usuário já logado: " + usuario.getNome());
            System.out.println("➡️ Redirecionando para dashboard");
            System.out.println("==========================================\n");
            response.sendRedirect("dashboard");
            return;
        }

        // ========== EXIBIR FORMULÁRIO ==========
        System.out.println("📄 Exibindo formulário de cadastro");
        System.out.println("==========================================\n");
        request.getRequestDispatcher("/cadastro.jsp").forward(request, response);
    }

    /* ================================================================
       AÇÃO 1: PROCESSAR LOGIN (Autenticação)
       ================================================================

       URL: POST /login

       Parâmetros obrigatórios:
       - cpf: Pode ter máscara (removida automaticamente)
       - senha: Texto plano (comparado com hash BCrypt)

       Fluxo completo:
       1. Recebe CPF e senha do formulário
       2. Remove máscara do CPF (deixa só números)
       3. Valida se campos não estão vazios
       4. Busca usuário no banco por CPF (UsuarioDAO)
       5. Verifica se usuário existe
       6. Compara senha com hash usando BCrypt.checkpw()
       7. Se OK: cria sessão e redireciona para dashboard
       8. Se ERRO: volta para login.jsp com mensagem

       Sessão criada:
       - Atributo "usuario": objeto Usuario completo
       - Timeout: 1800 segundos (30 minutos)

       Mensagens de erro:
       - "CPF e senha são obrigatórios"
       - "CPF ou senha incorretos" (não especifica qual)

       SEGURANÇA:
       ✅ Não informa se CPF existe ou não (evita ataques)
       ✅ Mensagem genérica "CPF ou senha incorretos"
       ✅ BCrypt com salt para validação
    */

    private void processarLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("🔐 Iniciando processamento de LOGIN");

        // ========== STEP 1: RECEBER PARÂMETROS ==========
        String cpfOriginal = request.getParameter("cpf");
        String senha = request.getParameter("senha");

        System.out.println("📋 Dados recebidos:");
        System.out.println("   - CPF (original): " + cpfOriginal);
        System.out.println("   - Senha: " + (senha != null ? "***" : "null"));

        // ========== STEP 2: REMOVER MÁSCARA DO CPF ==========
        // Remove tudo que não é número: 123.456.789-01 → 12345678901
        String cpf = cpfOriginal != null ? cpfOriginal.replaceAll("[^0-9]", "") : "";
        System.out.println("   - CPF (limpo): " + cpf);

        // ========== STEP 3: VALIDAR CAMPOS VAZIOS ==========
        if (cpf.isEmpty() || senha == null || senha.isEmpty()) {
            System.err.println("❌ Validação falhou: CPF ou senha vazio");
            System.out.println("==========================================\n");
            request.setAttribute("erro", "CPF e senha são obrigatórios");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        System.out.println("✅ Campos obrigatórios preenchidos");

        // ========== STEP 4: CONECTAR AO BANCO ==========
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão com banco estabelecida");

            // ========== STEP 5: BUSCAR USUÁRIO POR CPF ==========
            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);
            System.out.println("⏳ Buscando usuário no banco...");
            System.out.println("   SQL: SELECT * FROM usuario WHERE cpf = ?");
            System.out.println("   Parâmetro: " + cpf);

            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);

            // ========== STEP 6: VALIDAR SE USUÁRIO EXISTE ==========
            if (usuario == null) {
                System.err.println("❌ CPF não encontrado no banco!");
                System.out.println("   Mensagem genérica (segurança)");
                System.out.println("==========================================\n");
                request.setAttribute("erro", "CPF ou senha incorretos");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            System.out.println("✅ Usuário encontrado:");
            System.out.println("   - ID: " + usuario.getIdUsuario());
            System.out.println("   - Nome: " + usuario.getNome());
            System.out.println("   - Email: " + usuario.getEmail());
            System.out.println("   - CNPJ: " + usuario.getCnpj());

            // ========== STEP 7: VALIDAR SENHA COM BCRYPT ==========
            System.out.println("⏳ Verificando senha com BCrypt...");
            System.out.println("   - Senha digitada: ***");
            System.out.println("   - Hash no banco: " + usuario.getSenha().substring(0, 20) + "...");

            boolean senhaCorreta = BCrypt.checkpw(senha, usuario.getSenha());

            if (!senhaCorreta) {
                System.err.println("❌ Senha incorreta!");
                System.out.println("   Mensagem genérica (segurança)");
                System.out.println("==========================================\n");
                request.setAttribute("erro", "CPF ou senha incorretos");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            System.out.println("✅ Senha correta!");

            // ========== STEP 8: CRIAR SESSÃO ==========
            System.out.println("⏳ Criando sessão...");
            HttpSession session = request.getSession();
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(1800); // 30 minutos

            System.out.println("✅ Sessão criada:");
            System.out.println("   - Session ID: " + session.getId());
            System.out.println("   - Timeout: 1800s (30 min)");
            System.out.println("   - Atributo 'usuario': " + usuario.getNome());

            // ========== STEP 9: REDIRECIONAR PARA DASHBOARD ==========
            System.out.println("✅ LOGIN BEM-SUCEDIDO!");
            System.out.println("➡️ Redirecionando para /dashboard");
            System.out.println("==========================================\n");

            response.sendRedirect("dashboard");

        } catch (Exception e) {
            // ========== TRATAMENTO DE ERRO ==========
            System.err.println("❌ ERRO ao processar login:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            System.out.println("==========================================\n");

            request.setAttribute("erro", "Erro no sistema. Tente novamente.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    /* ================================================================
       AÇÃO 2: PROCESSAR CADASTRO
       ================================================================

       URL: POST /cadastro

       Parâmetros obrigatórios:
       - cpf: 11 dígitos (máscara removida)
       - nome: Texto não vazio
       - senha: Mínimo 6 caracteres
       - confirmarSenha: Deve ser igual a senha

       Parâmetros opcionais:
       - email: Se informado, deve ser único
       - cnpj: 14 dígitos (máscara removida)

       Validações realizadas:
       1. Campos obrigatórios preenchidos
       2. CPF com 11 dígitos
       3. CNPJ com 14 dígitos (se informado)
       4. Senha com mínimo 6 caracteres
       5. Confirmação de senha
       6. CPF não cadastrado (único)
       7. Email não cadastrado (único, se informado)

       Fluxo completo:
       1. Recebe dados do formulário
       2. Valida campos obrigatórios
       3. Remove máscaras (CPF, CNPJ)
       4. Valida formatos (11 e 14 dígitos)
       5. Valida senha (mínimo 6 caracteres)
       6. Valida confirmação de senha
       7. Verifica se CPF já existe no banco
       8. Verifica se email já existe (se informado)
       9. Gera hash BCrypt da senha (salt 10)
       10. Cria objeto Usuario
       11. Insere no banco via UsuarioDAO
       12. Redireciona para /login com sucesso

       Em caso de erro:
       - Volta para /cadastro com mensagem específica

       SEGURANÇA:
       ✅ Senha com hash BCrypt (salt 10)
       ✅ CPF único na base
       ✅ Email único na base
       ✅ PreparedStatement (via DAO)
    */

    private void processarCadastro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("📝 Iniciando processamento de CADASTRO");

        // ========== STEP 1: RECEBER PARÂMETROS ==========
        String cpf = request.getParameter("cpf");
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String cnpj = request.getParameter("cnpj");
        String senha = request.getParameter("senha");
        String confirmarSenha = request.getParameter("confirmarSenha");

        System.out.println("📋 Dados recebidos:");
        System.out.println("   - CPF: " + cpf);
        System.out.println("   - Nome: " + nome);
        System.out.println("   - Email: " + email);
        System.out.println("   - CNPJ: " + cnpj);
        System.out.println("   - Senha: " + (senha != null ? "***" : "null"));
        System.out.println("   - Confirmar: " + (confirmarSenha != null ? "***" : "null"));

        HttpSession session = request.getSession();

        // ========== STEP 2: VALIDAR CAMPOS OBRIGATÓRIOS ==========

        // Validação 1: CPF
        if (cpf == null || cpf.trim().isEmpty()) {
            System.err.println("❌ CPF vazio!");
            session.setAttribute("erro", "CPF é obrigatório!");
            response.sendRedirect("cadastro");
            return;
        }

        // Validação 2: Nome
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("❌ Nome vazio!");
            session.setAttribute("erro", "Nome é obrigatório!");
            response.sendRedirect("cadastro");
            return;
        }

        // Validação 3: Senha
        if (senha == null || senha.isEmpty()) {
            System.err.println("❌ Senha vazia!");
            session.setAttribute("erro", "Senha é obrigatória!");
            response.sendRedirect("cadastro");
            return;
        }

        // Validação 4: Confirmação de senha
        if (confirmarSenha == null || confirmarSenha.isEmpty()) {
            System.err.println("❌ Confirmação de senha vazia!");
            session.setAttribute("erro", "Confirmação de senha é obrigatória!");
            response.sendRedirect("cadastro");
            return;
        }

        System.out.println("✅ Campos obrigatórios OK");

        // ========== STEP 3: REMOVER MÁSCARAS ==========

        // CPF: 123.456.789-01 → 12345678901
        cpf = cpf.replaceAll("[^0-9]", "");
        System.out.println("✅ CPF limpo: " + cpf);

        // CNPJ: 12.345.678/0001-90 → 12345678000190 (se informado)
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            cnpj = cnpj.replaceAll("[^0-9]", "");
            System.out.println("✅ CNPJ limpo: " + cnpj);
        } else {
            cnpj = null;
            System.out.println("ℹ️ CNPJ não informado (opcional)");
        }

        // ========== STEP 4: VALIDAR FORMATOS ==========

        // CPF deve ter exatamente 11 dígitos
        if (cpf.length() != 11) {
            System.err.println("❌ CPF inválido: " + cpf.length() + " dígitos (esperado: 11)");
            session.setAttribute("erro", "CPF deve ter 11 dígitos!");
            response.sendRedirect("cadastro");
            return;
        }
        System.out.println("✅ CPF com 11 dígitos");

        // CNPJ deve ter exatamente 14 dígitos (se informado)
        if (cnpj != null && cnpj.length() != 14) {
            System.err.println("❌ CNPJ inválido: " + cnpj.length() + " dígitos (esperado: 14)");
            session.setAttribute("erro", "CNPJ deve ter 14 dígitos!");
            response.sendRedirect("cadastro");
            return;
        }
        if (cnpj != null) {
            System.out.println("✅ CNPJ com 14 dígitos");
        }

        // ========== STEP 5: VALIDAR SENHA ==========

        // Senha mínima: 6 caracteres
        if (senha.length() < 6) {
            System.err.println("❌ Senha curta: " + senha.length() + " caracteres (mínimo: 6)");
            session.setAttribute("erro", "Senha deve ter no mínimo 6 caracteres!");
            response.sendRedirect("cadastro");
            return;
        }
        System.out.println("✅ Senha com tamanho adequado");

        // Confirmação deve coincidir
        if (!senha.equals(confirmarSenha)) {
            System.err.println("❌ Senhas não coincidem!");
            session.setAttribute("erro", "Senhas não coincidem!");
            response.sendRedirect("cadastro");
            return;
        }
        System.out.println("✅ Confirmação de senha OK");

        // ========== STEP 6: CONECTAR AO BANCO ==========
        try (Connection conexao = Conexao.getConnection()) {

            System.out.println("✅ Conexão com banco estabelecida");
            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);

            // ========== STEP 7: VERIFICAR CPF ÚNICO ==========
            System.out.println("⏳ Verificando se CPF já existe...");
            System.out.println("   SQL: SELECT * FROM usuario WHERE cpf = ?");
            System.out.println("   Parâmetro: " + cpf);

            Usuario usuarioExistente = usuarioDAO.buscarPorCpf(cpf);

            if (usuarioExistente != null) {
                System.err.println("❌ CPF já cadastrado!");
                System.out.println("   - ID existente: " + usuarioExistente.getIdUsuario());
                System.out.println("   - Nome: " + usuarioExistente.getNome());
                session.setAttribute("erro", "CPF já cadastrado no sistema!");
                response.sendRedirect("cadastro");
                return;
            }

            System.out.println("✅ CPF disponível");

            // ========== STEP 8: VERIFICAR EMAIL ÚNICO (se informado) ==========
            if (email != null && !email.trim().isEmpty()) {
                System.out.println("⏳ Verificando se email já existe...");
                System.out.println("   SQL: SELECT * FROM usuario WHERE email = ?");
                System.out.println("   Parâmetro: " + email);

                Usuario usuarioEmail = usuarioDAO.buscarPorEmail(email);

                if (usuarioEmail != null) {
                    System.err.println("❌ Email já cadastrado!");
                    System.out.println("   - ID existente: " + usuarioEmail.getIdUsuario());
                    System.out.println("   - Nome: " + usuarioEmail.getNome());
                    session.setAttribute("erro", "Email já cadastrado no sistema!");
                    response.sendRedirect("cadastro");
                    return;
                }

                System.out.println("✅ Email disponível");
            }

            // ========== STEP 9: GERAR HASH BCRYPT DA SENHA ==========
            System.out.println("⏳ Gerando hash BCrypt da senha...");
            System.out.println("   - Algoritmo: BCrypt");
            System.out.println("   - Salt rounds: 10 (padrão)");

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            System.out.println("✅ Hash gerado:");
            System.out.println("   " + senhaHash.substring(0, 30) + "...");

            // ========== STEP 10: CRIAR OBJETO USUARIO ==========
            System.out.println("⏳ Criando objeto Usuario...");

            Usuario usuario = new Usuario();
            usuario.setCpf(cpf);
            usuario.setNome(nome.trim());
            usuario.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
            usuario.setCnpj(cnpj);
            usuario.setSenha(senhaHash);

            System.out.println("✅ Objeto criado:");
            System.out.println("   - CPF: " + usuario.getCpf());
            System.out.println("   - Nome: " + usuario.getNome());
            System.out.println("   - Email: " + usuario.getEmail());
            System.out.println("   - CNPJ: " + usuario.getCnpj());
            System.out.println("   - Senha: [hash]");

            // ========== STEP 11: INSERIR NO BANCO ==========
            System.out.println("⏳ Inserindo no banco de dados...");
            System.out.println("   SQL: INSERT INTO usuario (cpf, nome, email, cnpj, senha) VALUES (?, ?, ?, ?, ?)");

            usuarioDAO.inserir(usuario);

            System.out.println("✅ CADASTRO BEM-SUCEDIDO!");
            System.out.println("   - ID gerado: " + usuario.getIdUsuario());
            System.out.println("   - CPF: " + usuario.getCpf());
            System.out.println("   - Nome: " + usuario.getNome());
            System.out.println("==========================================\n");

            // ========== STEP 12: REDIRECIONAR PARA LOGIN ==========
            session.setAttribute("sucesso", "Cadastro realizado com sucesso! Faça login.");
            response.sendRedirect("login");

        } catch (Exception e) {
            // ========== TRATAMENTO DE ERRO ==========
            System.err.println("❌ ERRO ao processar cadastro:");
            System.err.println("   Tipo: " + e.getClass().getName());
            System.err.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
            System.out.println("==========================================\n");

            session.setAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            response.sendRedirect("cadastro");
        }
    }

    /* ================================================================
       AÇÃO 3: EXECUTAR LOGOUT
       ================================================================

       URL: GET /logout

       Comportamento:
       1. Invalida a sessão atual
       2. Remove todos os atributos
       3. Redireciona para /login

       Não requer autenticação (pode executar mesmo sem sessão)

       IMPORTANTE: Sempre redireciona para /login, nunca exibe página
    */

    private void executarLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("🚪 Iniciando LOGOUT");

        // ========== STEP 1: OBTER SESSÃO (sem criar nova) ==========
        HttpSession session = request.getSession(false);

        // ========== STEP 2: INVALIDAR SESSÃO (se existir) ==========
        if (session != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            if (usuario != null) {
                System.out.println("👤 Usuário a deslogar:");
                System.out.println("   - ID: " + usuario.getIdUsuario());
                System.out.println("   - Nome: " + usuario.getNome());
                System.out.println("   - CPF: " + usuario.getCpf());
            }

            System.out.println("⏳ Invalidando sessão...");
            System.out.println("   - Session ID: " + session.getId());

            session.invalidate();

            System.out.println("✅ Sessão invalidada com sucesso");

        } else {
            System.out.println("ℹ️ Nenhuma sessão ativa para invalidar");
        }

        // ========== STEP 3: REDIRECIONAR PARA LOGIN ==========
        System.out.println("➡️ Redirecionando para /login");
        System.out.println("==========================================\n");

        response.sendRedirect("login");
    }
}

/* ================================================================
   RESUMO DO CONTROLLER
   ================================================================

   ROTAS MAPEADAS:
   1. GET  /login     → Exibe formulário login.jsp
   2. POST /login     → Autentica usuário (CPF + senha)
   3. GET  /cadastro  → Exibe formulário cadastro.jsp
   4. POST /cadastro  → Cria novo usuário
   5. GET  /logout    → Encerra sessão

   CAMPOS DO FORMULÁRIO DE LOGIN:
   - cpf (com ou sem máscara)
   - senha

   CAMPOS DO FORMULÁRIO DE CADASTRO:
   - cpf* (obrigatório, 11 dígitos)
   - nome* (obrigatório)
   - email (opcional, único)
   - cnpj (opcional, 14 dígitos)
   - senha* (obrigatório, mínimo 6)
   - confirmarSenha* (obrigatório, deve coincidir)

   VALIDAÇÕES:
   ✅ CPF: 11 dígitos, único
   ✅ CNPJ: 14 dígitos (se informado)
   ✅ Email: único (se informado)
   ✅ Senha: mínimo 6 caracteres
   ✅ Confirmação: deve coincidir

   SEGURANÇA:
   ✅ BCrypt para senhas (salt 10)
   ✅ PreparedStatement (via DAO)
   ✅ Mensagens genéricas (não revela CPF existe)
   ✅ Sessão com timeout 30 minutos
   ✅ Máscaras removidas antes de salvar

   SESSÃO CRIADA NO LOGIN:
   - Atributo: "usuario" (objeto Usuario)
   - Timeout: 1800 segundos (30 minutos)
   - Inativa após timeout ou logout

   EXEMPLOS DE USO:
   ```
   // Login:
   POST /login
   cpf=12345678901&senha=minhasenha

   // Cadastro:
   POST /cadastro
   cpf=12345678901&nome=João&email=joao@mail.com&
   cnpj=12345678000190&senha=senha123&confirmarSenha=senha123

   // Logout:
   GET /logout
   ```

   MENSAGENS DE ERRO/SUCESSO:
   - Via request.setAttribute("erro", "...")
   - Via session.setAttribute("sucesso", "...")
   - Exibidas nos JSPs

   DEPENDÊNCIAS:
   - UsuarioDAO: Acesso ao banco
   - Usuario: Model
   - Conexao: Gerenciamento de conexões
   - BCrypt: Criptografia de senhas

   OBSERVAÇÕES:
   - Conexões fecham automaticamente (try-with-resources)
   - Logs detalhados em cada etapa
   - Tratamento de exceções robusto
   ================================================================ */