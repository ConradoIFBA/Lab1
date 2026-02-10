package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import br.com.projeto.model.Usuario;
import br.com.projeto.model.Vendas;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.utils.Conexao;

@WebServlet("/historico")
public class HistoricoController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Parâmetros de filtro
        String anoParam = request.getParameter("ano");
        String filtroNF = request.getParameter("filtroNF");

        int ano = 2026;
        if (anoParam != null && !anoParam.isEmpty()) {
            try {
                ano = Integer.parseInt(anoParam);
            } catch (NumberFormatException e) {
                System.err.println("Ano inválido: " + anoParam);
                ano = 2026;
            }
        }

        // Se filtroNF não foi especificado, usar "todas"
        if (filtroNF == null || filtroNF.isEmpty()) {
            filtroNF = "todas";
        }

        System.out.println("========== DEBUG HISTORICO ==========");
        System.out.println("Ano: " + ano);
        System.out.println("Filtro NF: " + filtroNF);
        System.out.println("Usuario ID: " + usuario.getIdUsuario());

        // ✅ PADRÃO: try-with-resources como no DashboardController
        try (Connection conexao = Conexao.getConnection()) {

            VendasDAO vendasDAO = new VendasDAO(conexao);

            // Buscar anos disponíveis
            List<Integer> anos = new ArrayList<>();
            try {
                anos = vendasDAO.listarAnosComVendas(usuario.getIdUsuario());
                System.out.println("Anos encontrados: " + anos.size());
                if (anos.isEmpty()) {
                    anos.add(2024);
                    anos.add(2025);
                    anos.add(2026);
                    System.out.println("Lista vazia, usando anos padrão");
                }
            } catch (Exception e) {
                System.err.println("Erro ao listar anos: " + e.getMessage());
                e.printStackTrace();
                anos.add(2024);
                anos.add(2025);
                anos.add(2026);
            }

            // Buscar vendas com filtro
            List<Vendas> vendasDetalhadas = new ArrayList<>();
            try {
                System.out.println("Buscando vendas com filtro...");
                vendasDetalhadas = vendasDAO.listarPorAnoComFiltroNF(
                        usuario.getIdUsuario(),
                        ano,
                        filtroNF
                );
                System.out.println("Vendas encontradas: " + vendasDetalhadas.size());
            } catch (Exception e) {
                System.err.println("❌ Erro ao buscar vendas detalhadas: " + e.getMessage());
                e.printStackTrace();
            }

            // Calcular estatísticas
            int totalVendas = vendasDetalhadas.size();
            double totalValor = 0;
            int totalComNF = 0;
            int totalSemNF = 0;
            double valorComNF = 0;
            double valorSemNF = 0;

            for (Vendas v : vendasDetalhadas) {
                totalValor += v.getValor();

                if ("S".equalsIgnoreCase(v.getNotaFiscalEmitida())) {
                    totalComNF++;
                    valorComNF += v.getValor();
                } else {
                    totalSemNF++;
                    valorSemNF += v.getValor();
                }
            }

            System.out.println("Total Vendas: " + totalVendas);
            System.out.println("Com NF: " + totalComNF);
            System.out.println("Sem NF: " + totalSemNF);
            System.out.println("=====================================");

            // Atributos para JSP
            request.setAttribute("ano", ano);
            request.setAttribute("anos", anos);
            request.setAttribute("filtroNF", filtroNF);
            request.setAttribute("vendasDetalhadas", vendasDetalhadas);
            request.setAttribute("totalVendas", totalVendas);
            request.setAttribute("totalValor", totalValor);
            request.setAttribute("totalComNF", totalComNF);
            request.setAttribute("totalSemNF", totalSemNF);
            request.setAttribute("valorComNF", valorComNF);
            request.setAttribute("valorSemNF", valorSemNF);

            request.getRequestDispatcher("historico.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ ERRO CRÍTICO NO HISTORICO: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao carregar histórico: " + e.getMessage());
            response.sendRedirect("dashboard");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}