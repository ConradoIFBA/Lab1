package br.com.projeto.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;
import br.com.projeto.utils.Conexao;
import br.com.projeto.utils.RelatorioPDF;

@WebServlet("/relatorio")
public class RelatorioController extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }
        
        // Página para selecionar período do relatório
        request.getRequestDispatcher("/relatorio.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        try {
            // Obter parâmetros do formulário
            int mes = Integer.parseInt(request.getParameter("mes"));
            int ano = Integer.parseInt(request.getParameter("ano"));
            
            // Buscar vendas do período
            VendasDAO vendasDAO = new VendasDAO(Conexao.getConnection());
            List<Vendas> vendas = vendasDAO.listarPorMesAno(usuario.getIdUsuario(), mes, ano);
            
            if (vendas.isEmpty()) {
                session.setAttribute("erro", "Nenhuma venda encontrada para o período selecionado.");
                response.sendRedirect("relatorio");
                return;
            }
            
            // Calcular totais
            RelatorioPDF relatorio = new RelatorioPDF();
            double[] totais = relatorio.calcularTotais(vendas);
            
            // Gerar PDF
            byte[] pdfBytes = relatorio.gerarRelatorio(usuario, mes, ano, vendas, totais);
            
            // Configurar resposta para download
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"relatorio_mei_" + mes + "_" + ano + ".pdf\"");
            response.setContentLength(pdfBytes.length);
            
            // Enviar PDF
            OutputStream out = response.getOutputStream();
            out.write(pdfBytes);
            out.flush();
            out.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao gerar relatório: " + e.getMessage());
            response.sendRedirect("relatorio");
        }
    }
    
    // Método para visualizar histórico
    @WebServlet("/relatorio/historico")
    public static class HistoricoController extends HttpServlet {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) 
                throws ServletException, IOException {
            
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                response.sendRedirect("login");
                return;
            }
            
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            try {
                VendasDAO vendasDAO = new VendasDAO(Conexao.getConnection());
                
                // Buscar anos que têm vendas
                List<Integer> anosComVendas = vendasDAO.listarAnosComVendas(usuario.getIdUsuario());
                request.setAttribute("anosComVendas", anosComVendas);
                
                // Se selecionou um ano, buscar meses
                String anoParam = request.getParameter("ano");
                if (anoParam != null && !anoParam.isEmpty()) {
                    int ano = Integer.parseInt(anoParam);
                    List<Object[]> resumoMensal = vendasDAO.resumoMensal(usuario.getIdUsuario(), ano);
                    request.setAttribute("resumoMensal", resumoMensal);
                    request.setAttribute("anoSelecionado", ano);
                }
                
                request.getRequestDispatcher("/historico.jsp").forward(request, response);
                
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao carregar histórico: " + e.getMessage());
                response.sendRedirect("dashboard");
            }
        }
    }
}