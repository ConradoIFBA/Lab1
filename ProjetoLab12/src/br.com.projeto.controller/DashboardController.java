package br.com.projeto.controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.dao.CategoriaDAO;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.Usuario;
import br.com.projeto.utils.Conexao;

@WebServlet("/dashboard")
public class DashboardController extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        try {
            // Buscar categorias para o formulário
            CategoriaDAO categoriaDAO = new CategoriaDAO(Conexao.getConnection());
            List<Categoria> categorias = categoriaDAO.listar();
            request.setAttribute("categorias", categorias);
            
            // Buscar últimas vendas do usuário
            VendasDAO vendasDAO = new VendasDAO(Conexao.getConnection());
            List<Vendas> ultimasVendas = vendasDAO.listarPorUsuario(usuario.getIdUsuario(), 10);
            request.setAttribute("ultimasVendas", ultimasVendas);
            
            // Calcular totais do mês atual
            double totalMes = vendasDAO.calcularTotalMes(usuario.getIdUsuario());
            request.setAttribute("totalMes", totalMes);
            
            request.setAttribute("usuario", usuario);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao carregar dashboard: " + e.getMessage());
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        }
    }
}