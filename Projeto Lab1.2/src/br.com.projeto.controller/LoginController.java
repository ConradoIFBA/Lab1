package br.com.projeto.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
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
        
        String cpf = request.getParameter("cpf").replaceAll("[^0-9]", "");
        String senha = request.getParameter("senha");
        
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO(Conexao.getConnection());
            Usuario usuario = usuarioDAO.buscarPorCPF(cpf);
            
            if (usuario != null && BCrypt.checkpw(senha, usuario.getSenha())) {
                // Login bem-sucedido
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setMaxInactiveInterval(1800); // 30 minutos
                
                response.sendRedirect("dashboard");
            } else {
                request.setAttribute("erro", "CPF ou senha incorretos");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao fazer login: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    private void cadastrarUsuario(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Usuario usuario = new Usuario();
            usuario.setCpf(request.getParameter("cpf").replaceAll("[^0-9]", ""));
            usuario.setNome(request.getParameter("nome"));
            usuario.setEmail(request.getParameter("email"));
            
            // Hash da senha
            String senha = request.getParameter("senha");
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt(12));
            usuario.setSenha(senhaHash);
            
            UsuarioDAO usuarioDAO = new UsuarioDAO(Conexao.getConnection());
            usuarioDAO.inserir(usuario);
            
            request.setAttribute("sucesso", "Cadastro realizado! Faça login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
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