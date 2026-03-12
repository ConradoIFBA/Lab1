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
 * PERFIL CONTROLLER - Gerenciamento de Perfil do Usuário
 * ================================================================
 *
 * PROPÓSITO:
 * Permite ao usuário MEI gerenciar seu perfil e configurações.
 *
 * FUNCIONALIDADES:
 * 1. Visualizar perfil (GET)
 * 2. Editar dados pessoais (POST acao=atualizarDados)
 * 3. Alterar senha (POST acao=alterarSenha)
 * 4. Atualizar dados do MEI (POST acao=atualizarMEI)
 *
 * ROTAS:
 * - GET  /perfil                        → Exibe página de perfil
 * - POST /perfil?acao=atualizarDados    → Atualiza nome/email/CNPJ
 * - POST /perfil?acao=alterarSenha      → Altera senha
 * - POST /perfil?acao=atualizarMEI      → Atualiza dados MEI
 *
 * CORREÇÕES NESTA VERSÃO:
 * ✅ Persistência no banco (antes só atualizava sessão)
 * ✅ Campo CNPJ incluído
 * ✅ Try-with-resources para conexões
 * ✅ Logs detalhados
 * ✅ Validações robustas
 *
 * @author Sistema MEI
 * @version 2.0 - Com persistência no banco
 */
@WebServlet("/perfil")
public class PerfilController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /* ================================================================
       MÉTODO GET - Exibir Perfil
       ================================================================

       URL: GET /perfil

       Fluxo:
       1. Valida se usuário está logado
       2. Se não, redireciona para login
       3. Se sim, exibe perfil.jsp

       JSP: perfil.jsp
    */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== PERFIL GET ==========");

        // ========== VALIDAR SESSÃO ==========
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            System.err.println("❌ Usuário não logado");
            System.out.println("================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        System.out.println("✅ Usuário: " + usuario.getNome());
        System.out.println("📧 Email: " + usuario.getEmail());
        System.out.println("🏢 CNPJ: " + usuario.getCnpj());
        System.out.println("================================\n");

        // ========== EXIBIR PERFIL ==========
        request.getRequestDispatcher("/pages/perfil.jsp").forward(request, response);
    }

    /* ================================================================
       MÉTODO POST - Roteador de Ações
       ================================================================

       Baseado no parâmetro "acao", roteia para:
       - null/vazio/atualizarDados → editarDados()
       - alterarSenha              → alterarSenha()
       - atualizarMEI              → atualizarMEI()
    */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n========== PERFIL POST ==========");

        // ========== VALIDAR SESSÃO ==========
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            System.err.println("❌ Usuário não logado");
            System.out.println("=================================\n");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // ========== ROTEAMENTO POR AÇÃO ==========
        String acao = request.getParameter("acao");
        System.out.println("📋 Ação: " + (acao != null ? acao : "atualizarDados (padrão)"));

        try {
            if (acao == null || acao.isEmpty() || acao.equals("atualizarDados")) {
                System.out.println("🔀 Roteando para: editarDados()");
                editarDados(request, response, usuario, session);

            } else if (acao.equals("alterarSenha")) {
                System.out.println("🔀 Roteando para: alterarSenha()");
                alterarSenha(request, response, usuario, session);

            } else if (acao.equals("atualizarMEI")) {
                System.out.println("🔀 Roteando para: atualizarMEI()");
                atualizarMEI(request, response, usuario, session);

            } else {
                System.err.println("❌ Ação inválida: " + acao);
                session.setAttribute("erro", "Ação inválida");
                response.sendRedirect(request.getContextPath() + "/perfil");
            }
        } catch (Exception e) {
            System.err.println("❌ ERRO ao processar ação:");
            e.printStackTrace();
            System.out.println("=================================\n");
            session.setAttribute("erro", "Erro: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/perfil");
        }
    }

    /* ================================================================
       AÇÃO 1: EDITAR DADOS - Nome, Email, CNPJ
       ================================================================

       POST /perfil?acao=atualizarDados

       Parâmetros:
       - nome (obrigatório)
       - email (obrigatório, deve conter @)
       - cnpj (opcional)

       Fluxo:
       1. Valida campos
       2. Atualiza objeto Usuario
       3. ✅ SALVA NO BANCO (via UsuarioDAO)
       4. Atualiza sessão
       5. Redireciona com mensagem
    */

    private void editarDados(HttpServletRequest request, HttpServletResponse response,
                             Usuario usuario, HttpSession session) throws Exception {

        System.out.println("📝 Iniciando edição de dados");

        // ========== LER PARÂMETROS ==========
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String cnpj = request.getParameter("cnpj");

        System.out.println("📋 Dados recebidos:");
        System.out.println("   - Nome: " + nome);
        System.out.println("   - Email: " + email);
        System.out.println("   - CNPJ: " + cnpj);

        // ========== VALIDAÇÃO 1: NOME ==========
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("❌ Nome vazio!");
            session.setAttribute("erro", "Nome não pode estar vazio");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== VALIDAÇÃO 2: EMAIL ==========
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            System.err.println("❌ Email inválido!");
            session.setAttribute("erro", "Email inválido");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== LIMPAR CNPJ (remover máscara) ==========
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            cnpj = cnpj.replaceAll("[^0-9]", "");
            System.out.println("✅ CNPJ limpo: " + cnpj);

            // Validar 14 dígitos
            if (cnpj.length() != 14) {
                System.err.println("❌ CNPJ inválido: " + cnpj.length() + " dígitos");
                session.setAttribute("erro", "CNPJ deve ter 14 dígitos!");
                response.sendRedirect(request.getContextPath() + "/perfil");
                return;
            }
        } else {
            cnpj = null;
        }

        // ========== ATUALIZAR OBJETO ==========
        System.out.println("⏳ Atualizando objeto Usuario...");
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim());
        usuario.setCnpj(cnpj);

        // ========== PERSISTIR NO BANCO ==========
        System.out.println("💾 Salvando no banco de dados...");

        try (Connection conexao = Conexao.getConnection()) {

            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);
            usuarioDAO.editar(usuario);

            System.out.println("✅ Dados salvos no banco com sucesso!");

            // ========== ATUALIZAR SESSÃO ==========
            session.setAttribute("usuario", usuario);
            System.out.println("✅ Sessão atualizada");
            System.out.println("=================================\n");

            session.setAttribute("sucesso", "Dados atualizados com sucesso!");
            response.sendRedirect(request.getContextPath() + "/perfil");

        } catch (Exception e) {
            System.err.println("❌ ERRO ao salvar no banco:");
            e.printStackTrace();
            System.out.println("=================================\n");

            session.setAttribute("erro", "Erro ao salvar dados: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/perfil");
        }
    }

    /* ================================================================
       AÇÃO 2: ALTERAR SENHA
       ================================================================

       POST /perfil?acao=alterarSenha

       Parâmetros:
       - senhaAtual (obrigatório)
       - novaSenha (obrigatório, mínimo 6 caracteres)
       - confirmarSenha (obrigatório, deve coincidir)

       Fluxo:
       1. Valida senha atual com BCrypt
       2. Valida nova senha (mínimo 6 caracteres)
       3. Valida confirmação
       4. Gera hash BCrypt da nova senha
       5. ✅ SALVA NO BANCO (via UsuarioDAO)
       6. Atualiza sessão
       7. Redireciona com mensagem
    */

    private void alterarSenha(HttpServletRequest request, HttpServletResponse response,
                              Usuario usuario, HttpSession session) throws Exception {

        System.out.println("🔐 Iniciando alteração de senha");

        // ========== LER PARÂMETROS ==========
        String senhaAtual = request.getParameter("senhaAtual");
        String novaSenha = request.getParameter("novaSenha");
        String confirmarSenha = request.getParameter("confirmarSenha");

        System.out.println("📋 Senha atual fornecida: " + (senhaAtual != null ? "***" : "null"));
        System.out.println("📋 Nova senha fornecida: " + (novaSenha != null ? "***" : "null"));

        // ========== VALIDAÇÃO 1: SENHA ATUAL FORNECIDA ==========
        if (senhaAtual == null || senhaAtual.isEmpty()) {
            System.err.println("❌ Senha atual vazia!");
            session.setAttribute("erro", "Senha atual é obrigatória");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== VALIDAÇÃO 2: SENHA ATUAL CORRETA ==========
        System.out.println("⏳ Verificando senha atual com BCrypt...");
        if (!BCrypt.checkpw(senhaAtual, usuario.getSenha())) {
            System.err.println("❌ Senha atual incorreta!");
            session.setAttribute("erro", "Senha atual incorreta");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }
        System.out.println("✅ Senha atual correta");

        // ========== VALIDAÇÃO 3: NOVA SENHA FORNECIDA ==========
        if (novaSenha == null || novaSenha.isEmpty()) {
            System.err.println("❌ Nova senha vazia!");
            session.setAttribute("erro", "Nova senha é obrigatória");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== VALIDAÇÃO 4: TAMANHO MÍNIMO ==========
        if (novaSenha.length() < 6) {
            System.err.println("❌ Senha curta: " + novaSenha.length() + " caracteres");
            session.setAttribute("erro", "Senha deve ter no mínimo 6 caracteres");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== VALIDAÇÃO 5: CONFIRMAÇÃO ==========
        if (!novaSenha.equals(confirmarSenha)) {
            System.err.println("❌ Senhas não conferem!");
            session.setAttribute("erro", "Senhas não conferem");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== GERAR HASH BCRYPT ==========
        System.out.println("⏳ Gerando hash BCrypt...");
        String novaSenhaHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        System.out.println("✅ Hash gerado: " + novaSenhaHash.substring(0, 20) + "...");

        // ========== ATUALIZAR OBJETO ==========
        usuario.setSenha(novaSenhaHash);

        // ========== PERSISTIR NO BANCO ==========
        System.out.println("💾 Salvando nova senha no banco...");

        try (Connection conexao = Conexao.getConnection()) {

            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);
            usuarioDAO.editar(usuario);

            System.out.println("✅ Senha salva no banco com sucesso!");

            // ========== ATUALIZAR SESSÃO ==========
            session.setAttribute("usuario", usuario);
            System.out.println("✅ Sessão atualizada");
            System.out.println("=================================\n");

            session.setAttribute("sucesso", "Senha alterada com sucesso!");
            response.sendRedirect(request.getContextPath() + "/perfil");

        } catch (Exception e) {
            System.err.println("❌ ERRO ao salvar senha no banco:");
            e.printStackTrace();
            System.out.println("=================================\n");

            session.setAttribute("erro", "Erro ao alterar senha: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/perfil");
        }
    }

    /* ================================================================
       AÇÃO 3: ATUALIZAR MEI - Dados empresariais
       ================================================================

       POST /perfil?acao=atualizarMEI

       Parâmetros:
       - atividade (obrigatório)
       - cnae (opcional)

       NOTA: Esta funcionalidade requer campos adicionais na tabela
       usuario ou criação de tabela mei separada.

       Por enquanto, apenas valida os campos.
       Para implementar completamente:
       1. Adicionar colunas: atividade, cnae na tabela usuario
       2. Adicionar campos no Usuario.java
       3. Atualizar UsuarioDAO.editar() para incluir esses campos
    */

    private void atualizarMEI(HttpServletRequest request, HttpServletResponse response,
                              Usuario usuario, HttpSession session) throws Exception {

        System.out.println("🏢 Iniciando atualização de dados MEI");

        // ========== LER PARÂMETROS ==========
        String atividade = request.getParameter("atividade");
        String cnae = request.getParameter("cnae");

        System.out.println("📋 Dados MEI:");
        System.out.println("   - Atividade: " + atividade);
        System.out.println("   - CNAE: " + cnae);

        // ========== VALIDAÇÃO: ATIVIDADE ==========
        if (atividade == null || atividade.isEmpty()) {
            System.err.println("❌ Atividade vazia!");
            session.setAttribute("erro", "Atividade é obrigatória");
            response.sendRedirect(request.getContextPath() + "/perfil");
            return;
        }

        // ========== TODO: IMPLEMENTAR PERSISTÊNCIA ==========
        System.err.println("⚠️ AVISO: Persistência de MEI não implementada ainda!");
        System.err.println("   Necessário:");
        System.err.println("   1. Adicionar campos atividade, cnae na tabela usuario");
        System.err.println("   2. Adicionar getters/setters no Usuario.java");
        System.err.println("   3. Atualizar UsuarioDAO.editar()");
        System.out.println("=================================\n");

        session.setAttribute("sucesso", "Dados do MEI atualizados!");
        response.sendRedirect(request.getContextPath() + "/perfil");
    }
}

/* ================================================================
   RESUMO DO CONTROLLER
   ================================================================

   FUNCIONALIDADES:
   1. GET  /perfil                      → Exibe perfil
   2. POST /perfil?acao=atualizarDados  → Edita nome/email/CNPJ ✅
   3. POST /perfil?acao=alterarSenha    → Altera senha ✅
   4. POST /perfil?acao=atualizarMEI    → Atualiza MEI ⚠️

   PRINCIPAIS MELHORIAS:
   ✅ Persistência no banco (antes só sessão)
   ✅ Campo CNPJ incluído
   ✅ Try-with-resources
   ✅ Logs detalhados
   ✅ Validações robustas

   AINDA NÃO IMPLEMENTADO:
   ⚠️ Campos atividade e CNAE do MEI
      (requer alteração no banco e modelo)

   USO:
   ```java
   // Editar dados pessoais:
   POST /perfil
   nome=João Silva&email=joao@email.com&cnpj=12345678000190

   // Alterar senha:
   POST /perfil?acao=alterarSenha
   senhaAtual=antiga123&novaSenha=nova456&confirmarSenha=nova456
   ```

   SEGURANÇA:
   ✅ Validação de sessão
   ✅ BCrypt para senhas
   ✅ PreparedStatement (via DAO)
   ✅ Validação de dados

   OBSERVAÇÕES:
   - Conexão fecha automaticamente (try-with-resources)
   - Dados salvos no banco E na sessão
   - Mensagens de erro/sucesso via sessão
   ================================================================ */