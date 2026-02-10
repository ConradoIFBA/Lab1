package br.com.projeto.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
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
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }

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

        String mesParam = request.getParameter("mes");
        String anoParam = request.getParameter("ano");

        if (mesParam == null || anoParam == null) {
            session.setAttribute("erro", "Mês e ano são obrigatórios!");
            response.sendRedirect("relatorio");
            return;
        }

        int mes, ano;
        try {
            mes = Integer.parseInt(mesParam);
            ano = Integer.parseInt(anoParam);
        } catch (NumberFormatException e) {
            session.setAttribute("erro", "Mês ou ano inválido!");
            response.sendRedirect("relatorio");
            return;
        }

        if (mes < 1 || mes > 12) {
            session.setAttribute("erro", "Mês deve estar entre 1 e 12!");
            response.sendRedirect("relatorio");
            return;
        }

        // ✅ CORRIGIDO: try-with-resources
        try (Connection conexao = Conexao.getConnection()) {

            VendasDAO vendasDAO = new VendasDAO(conexao);
            List<Vendas> vendas = new ArrayList<>();

            try {
                vendas = vendasDAO.listarPorMesAno(usuario.getIdUsuario(), mes, ano);
            } catch (Exception e) {
                System.err.println("Erro ao buscar vendas: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao buscar vendas: " + e.getMessage());
                response.sendRedirect("relatorio");
                return;
            }

            if (vendas.isEmpty()) {
                session.setAttribute("erro", "Nenhuma venda encontrada para o período selecionado.");
                response.sendRedirect("relatorio");
                return;
            }

            try {
                RelatorioPDF relatorio = new RelatorioPDF();
                double[] totais = relatorio.calcularTotais(vendas);
                byte[] pdfBytes = relatorio.gerarRelatorio(usuario, mes, ano, vendas, totais);

                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"relatorio_mei_" + mes + "_" + ano + ".pdf\"");
                response.setContentLength(pdfBytes.length);

                OutputStream out = response.getOutputStream();
                out.write(pdfBytes);
                out.flush();

                System.out.println("✅ Relatório gerado: " + vendas.size() + " vendas");

            } catch (Exception e) {
                System.err.println("Erro ao gerar PDF: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao gerar PDF: " + e.getMessage());
                response.sendRedirect("relatorio");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao gerar relatório: " + e.getMessage());
            response.sendRedirect("relatorio");
        }
        // ✅ Conexão fecha automaticamente
    }
}