package br.com.projeto.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

import br.com.projeto.model.Usuario;

@WebServlet("/perfil")
public class PerfilController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect("login");
            return;
        }

        request.getRequestDispatcher("perfil.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect("login");
            return;
        }

        String acao = request.getParameter("acao");

        try {
            if (acao == null || acao.isEmpty() || acao.equals("atualizarDados")) {
                editarDados(request, response, usuario, session);
            } else if (acao.equals("alterarSenha")) {
                alterarSenha(request, response, usuario, session);
            } else if (acao.equals("atualizarMEI")) {
                atualizarMEI(request, response, usuario, session);
            } else {
                session.setAttribute("erro", "Ação inválida");
                response.sendRedirect("perfil");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("erro", "Erro: " + e.getMessage());
            response.sendRedirect("perfil");
        }
    }

    private void editarDados(HttpServletRequest request, HttpServletResponse response,
                             Usuario usuario, HttpSession session) throws Exception {

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");

        if (nome == null || nome.trim().isEmpty()) {
            session.setAttribute("erro", "Nome não pode estar vazio");
            response.sendRedirect("perfil");
            return;
        }

        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            session.setAttribute("erro", "Email inválido");
            response.sendRedirect("perfil");
            return;
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim());
        session.setAttribute("usuario", usuario);

        // ⚠️ NOTA: Aqui deveria ter persistência no banco via UsuarioDAO.atualizar()
        // Por enquanto atualiza apenas na sessão

        session.setAttribute("sucesso", "Dados atualizados com sucesso!");
        response.sendRedirect("perfil");
    }

    private void alterarSenha(HttpServletRequest request, HttpServletResponse response,
                              Usuario usuario, HttpSession session) throws Exception {

        String senhaAtual = request.getParameter("senhaAtual");
        String novaSenha = request.getParameter("novaSenha");
        String confirmarSenha = request.getParameter("confirmarSenha");

        if (senhaAtual == null || senhaAtual.isEmpty()) {
            session.setAttribute("erro", "Senha atual é obrigatória");
            response.sendRedirect("perfil");
            return;
        }

        if (!BCrypt.checkpw(senhaAtual, usuario.getSenha())) {
            session.setAttribute("erro", "Senha atual incorreta");
            response.sendRedirect("perfil");
            return;
        }

        if (novaSenha == null || novaSenha.isEmpty()) {
            session.setAttribute("erro", "Nova senha é obrigatória");
            response.sendRedirect("perfil");
            return;
        }

        if (novaSenha.length() < 6) {
            session.setAttribute("erro", "Senha deve ter no mínimo 6 caracteres");
            response.sendRedirect("perfil");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            session.setAttribute("erro", "Senhas não conferem");
            response.sendRedirect("perfil");
            return;
        }

        String novaSenhaHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());
        usuario.setSenha(novaSenhaHash);
        session.setAttribute("usuario", usuario);

        // ⚠️ NOTA: Aqui deveria ter persistência no banco via UsuarioDAO.alterarSenha()

        session.setAttribute("sucesso", "Senha alterada com sucesso!");
        response.sendRedirect("perfil");
    }

    private void atualizarMEI(HttpServletRequest request, HttpServletResponse response,
                              Usuario usuario, HttpSession session) throws Exception {

        String atividade = request.getParameter("atividade");
        String cnae = request.getParameter("cnae");

        if (atividade == null || atividade.isEmpty()) {
            session.setAttribute("erro", "Atividade é obrigatória");
            response.sendRedirect("perfil");
            return;
        }

        // ⚠️ NOTA: Aqui deveria ter persistência no banco com atividade e CNAE

        session.setAttribute("sucesso", "Dados do MEI atualizados!");
        response.sendRedirect("perfil");
    }
}