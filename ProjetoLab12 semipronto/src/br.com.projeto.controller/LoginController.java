package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import br.com.projeto.dao.UsuarioDAO;
import br.com.projeto.model.Usuario;
import br.com.projeto.utils.Conexao;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            response.sendRedirect("dashboard");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");

        if ("cadastrar".equals(acao)) {
            cadastrarUsuario(request, response);
        } else {
            autenticarUsuario(request, response);
        }
    }

    private void autenticarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cpfOriginal = request.getParameter("cpf");
        String cpf = cpfOriginal.replaceAll("[^0-9]", "");
        String senha = request.getParameter("senha");

        System.out.println("\n========== DEBUG LOGIN ==========");
        System.out.println("CPF limpo: " + cpf);

        // ✅ CORRIGIDO: try-with-resources garante fechamento da conexão
        try (Connection conexao = Conexao.getConnection()) {

            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);
            Usuario usuario = usuarioDAO.buscarPorCPF(cpf);

            System.out.println("Usuario encontrado: " + (usuario != null ? "SIM" : "NAO"));

            if (usuario != null) {
                boolean senhaCorreta = BCrypt.checkpw(senha, usuario.getSenha());
                System.out.println("Senha correta: " + (senhaCorreta ? "SIM" : "NAO"));

                if (senhaCorreta) {
                    System.out.println("✅ LOGIN BEM-SUCEDIDO!");
                    System.out.println("=================================\n");

                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuario);
                    session.setMaxInactiveInterval(1800);

                    response.sendRedirect("dashboard");
                    return;
                } else {
                    System.out.println("❌ SENHA INCORRETA!");
                }
            } else {
                System.out.println("❌ USUARIO NAO ENCONTRADO!");
            }

            System.out.println("=================================\n");
            request.setAttribute("erro", "CPF ou senha incorretos");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (Exception e) {
            System.out.println("❌ ERRO NO LOGIN: " + e.getMessage());
            System.out.println("=================================\n");
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao fazer login: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
        // ✅ Conexão fecha automaticamente aqui
    }

    private void cadastrarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cpfOriginal = request.getParameter("cpf");
        String cpf = cpfOriginal.replaceAll("[^0-9]", "");
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        System.out.println("\n========== DEBUG CADASTRO ==========");
        System.out.println("Nome: " + nome);
        System.out.println("CPF limpo: " + cpf);
        System.out.println("Email: " + email);

        // ✅ CORRIGIDO: try-with-resources
        try (Connection conexao = Conexao.getConnection()) {

            UsuarioDAO usuarioDAO = new UsuarioDAO(conexao);

            if (usuarioDAO.cpfExiste(cpf)) {
                System.out.println("❌ CPF JÁ EXISTE!");
                System.out.println("====================================\n");
                request.setAttribute("erro", "CPF já cadastrado!");
                request.getRequestDispatcher("/cadastro.jsp").forward(request, response);
                return;
            }

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt(12));
            System.out.println("Hash gerado: " + senhaHash.substring(0, 30) + "...");

            Usuario usuario = new Usuario();
            usuario.setCpf(cpf);
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senhaHash);

            usuarioDAO.inserir(usuario);

            System.out.println("✅ USUARIO CADASTRADO COM SUCESSO!");
            System.out.println("====================================\n");

            request.setAttribute("sucesso", "Cadastro realizado! Faça login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (Exception e) {
            System.out.println("❌ ERRO NO CADASTRO: " + e.getMessage());
            System.out.println("====================================\n");
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            request.getRequestDispatcher("/cadastro.jsp").forward(request, response);
        }
        // ✅ Conexão fecha automaticamente aqui
    }

    @WebServlet("/logout")
    public static class LogoutController extends HttpServlet {
        private static final long serialVersionUID = 1L;

        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("login");
        }
    }
}