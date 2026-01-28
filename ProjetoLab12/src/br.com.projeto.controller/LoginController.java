package br.com.projeto.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import br.com.projeto.dao.UsuarioDAO;
import br.com.projeto.model.Usuario;
import br.com.projeto.utils.Conexao;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Se já estiver logado, redireciona para dashboard
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
        System.out.println("CPF original: " + cpfOriginal);
        System.out.println("CPF limpo: " + cpf);
        System.out.println("Senha digitada: " + senha);

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO(Conexao.getConnection());
            Usuario usuario = usuarioDAO.buscarPorCPF(cpf);

            System.out.println("Usuario encontrado no banco: " + (usuario != null ? "SIM" : "NAO"));

            if (usuario != null) {
                System.out.println("Nome do usuario: " + usuario.getNome());
                System.out.println("Email do usuario: " + usuario.getEmail());
                System.out.println("Hash do banco (primeiros 30 chars): " + usuario.getSenha().substring(0, 30) + "...");
                System.out.println("Tamanho do hash: " + usuario.getSenha().length());

                // Verificar se a senha bate
                boolean senhaCorreta = BCrypt.checkpw(senha, usuario.getSenha());
                System.out.println("Senha correta: " + (senhaCorreta ? "SIM" : "NAO"));

                if (senhaCorreta) {
                    System.out.println("✅ LOGIN BEM-SUCEDIDO!");
                    System.out.println("=================================\n");

                    // Login bem-sucedido
                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuario);
                    session.setMaxInactiveInterval(1800); // 30 minutos

                    response.sendRedirect("dashboard");
                    return;
                } else {
                    System.out.println("❌ SENHA INCORRETA!");
                    System.out.println("=================================\n");
                }
            } else {
                System.out.println("❌ USUARIO NAO ENCONTRADO!");
                System.out.println("=================================\n");
            }

            request.setAttribute("erro", "CPF ou senha incorretos");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (Exception e) {
            System.out.println("❌ ERRO NO LOGIN: " + e.getMessage());
            System.out.println("=================================\n");
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao fazer login: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private void cadastrarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String cpfOriginal = request.getParameter("cpf");
            String cpf = cpfOriginal.replaceAll("[^0-9]", "");
            String nome = request.getParameter("nome");
            String email = request.getParameter("email");
            String senha = request.getParameter("senha");

            System.out.println("\n========== DEBUG CADASTRO ==========");
            System.out.println("Nome: " + nome);
            System.out.println("CPF original: " + cpfOriginal);
            System.out.println("CPF limpo: " + cpf);
            System.out.println("Email: " + email);
            System.out.println("Senha original: " + senha);

            // Verificar se CPF já existe
            UsuarioDAO usuarioDAO = new UsuarioDAO(Conexao.getConnection());
            if (usuarioDAO.cpfExiste(cpf)) {
                System.out.println("❌ CPF JÁ EXISTE NO BANCO!");
                System.out.println("====================================\n");
                request.setAttribute("erro", "CPF já cadastrado!");
                request.getRequestDispatcher("/cadastro.jsp").forward(request, response);
                return;
            }

            // Hash da senha
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt(12));
            System.out.println("Hash gerado: " + senhaHash);
            System.out.println("Tamanho do hash: " + senhaHash.length());

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
    }

    @WebServlet("/logout")
    public static class LogoutController extends HttpServlet {
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