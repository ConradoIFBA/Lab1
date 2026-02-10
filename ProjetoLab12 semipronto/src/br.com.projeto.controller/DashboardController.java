package br.com.projeto.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.dao.CategoriaDAO;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.Usuario;
import br.com.projeto.model.NotaFiscal;
import br.com.projeto.utils.Conexao;

@WebServlet("/dashboard")
public class DashboardController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // ✅ CORRIGIDO: try-with-resources
        try (Connection conexao = Conexao.getConnection()) {

            // Buscar categorias (para o formulário inline)
            CategoriaDAO categoriaDAO = new CategoriaDAO(conexao);
            List<Categoria> categorias = new ArrayList<>();
            try {
                categorias = categoriaDAO.listar();
                System.out.println("Categorias carregadas: " + categorias.size());
            } catch (Exception e) {
                System.err.println("Erro ao listar categorias: " + e.getMessage());
                e.printStackTrace();
            }
            request.setAttribute("categorias", categorias);

            // Buscar últimas vendas
            VendasDAO vendasDAO = new VendasDAO(conexao);
            List<Vendas> ultimasVendas = new ArrayList<>();
            try {
                ultimasVendas = vendasDAO.listarPorUsuario(usuario.getIdUsuario(), 10);
            } catch (Exception e) {
                System.err.println("Erro ao listar vendas: " + e.getMessage());
                e.printStackTrace();
            }
            request.setAttribute("ultimasVendas", ultimasVendas);

            // Calcular total do mês
            double totalMes = 0.0;
            try {
                totalMes = vendasDAO.calcularTotalMes(usuario.getIdUsuario());
            } catch (Exception e) {
                System.err.println("Erro ao calcular total: " + e.getMessage());
                e.printStackTrace();
            }
            request.setAttribute("totalMes", totalMes);

            request.setAttribute("usuario", usuario);
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erro", "Erro ao carregar dashboard: " + e.getMessage());
            request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
        }
    }

    // ✅ ATUALIZADO: POST com campos de Nota Fiscal
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        String categoriaStr = request.getParameter("categoria");
        String valorStr = request.getParameter("valor");
        String descricao = request.getParameter("descricao");

        // ✅ NOVOS CAMPOS DE NOTA FISCAL
        String emitirNF = request.getParameter("emitirNF");  // "S" ou "N"
        String numeroNF = request.getParameter("numeroNF");   // Número da NF (opcional)

        System.out.println("========== DEBUG CADASTRO VENDA ==========");
        System.out.println("Categoria: " + categoriaStr);
        System.out.println("Valor: " + valorStr);
        System.out.println("Emitir NF: " + emitirNF);
        System.out.println("Número NF: " + numeroNF);

        // Validações
        if (categoriaStr == null || categoriaStr.isEmpty()) {
            session.setAttribute("erro", "Categoria é obrigatória!");
            response.sendRedirect("dashboard");
            return;
        }

        if (valorStr == null || valorStr.isEmpty()) {
            session.setAttribute("erro", "Valor é obrigatório!");
            response.sendRedirect("dashboard");
            return;
        }

        double valor;
        try {
            valorStr = valorStr.replace(",", ".");
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            session.setAttribute("erro", "Valor inválido!");
            response.sendRedirect("dashboard");
            return;
        }

        if (valor <= 0) {
            session.setAttribute("erro", "Valor deve ser maior que zero!");
            response.sendRedirect("dashboard");
            return;
        }

        int categoriaId;
        try {
            categoriaId = Integer.parseInt(categoriaStr);
        } catch (NumberFormatException e) {
            session.setAttribute("erro", "Categoria inválida!");
            response.sendRedirect("dashboard");
            return;
        }

        // ✅ Validação de Nota Fiscal
        if (emitirNF == null || emitirNF.isEmpty()) {
            emitirNF = "N";  // Default: não emite NF
        }

        // Se marcou para emitir NF, número é obrigatório
        if ("S".equalsIgnoreCase(emitirNF)) {
            if (numeroNF == null || numeroNF.trim().isEmpty()) {
                session.setAttribute("erro", "Número da Nota Fiscal é obrigatório quando marca 'Emitir NF'!");
                response.sendRedirect("dashboard");
                return;
            }
        }

        // ✅ CORRIGIDO: try-with-resources
        try (Connection conexao = Conexao.getConnection()) {

            Vendas venda = new Vendas();
            venda.setDataVendas(new Date());
            venda.setValor((float) valor);
            venda.setDescricao(descricao != null ? descricao.trim() : "");
            venda.setUsuarioId(usuario.getIdUsuario());
            venda.setNotaFiscalEmitida(emitirNF.toUpperCase());  // 'S' ou 'N'

            Categoria categoria = new Categoria();
            categoria.setIdCategoria(categoriaId);
            venda.setCategoria(categoria);

            // ✅ Se emitiu NF, cria objeto NotaFiscal
            if ("S".equalsIgnoreCase(emitirNF) && numeroNF != null && !numeroNF.trim().isEmpty()) {
                NotaFiscal nf = new NotaFiscal();
                nf.setNumero(numeroNF.trim());
                nf.setDataEmissao(new Date());
                nf.setValor((float) valor);
                venda.setNotaFiscal(nf);

                System.out.println("✅ Nota Fiscal será criada: " + numeroNF.trim());
            }

            VendasDAO vendasDAO = new VendasDAO(conexao);
            try {
                vendasDAO.inserir(venda);
                System.out.println("✅ Venda inserida! ID: " + venda.getIdVendas());

                if ("S".equalsIgnoreCase(emitirNF)) {
                    session.setAttribute("sucesso", "Venda cadastrada com Nota Fiscal " + numeroNF + "!");
                } else {
                    session.setAttribute("sucesso", "Venda cadastrada com sucesso!");
                }

            } catch (Exception e) {
                System.err.println("❌ Erro ao inserir venda: " + e.getMessage());
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao cadastrar venda: " + e.getMessage());
            }

            System.out.println("==========================================");
            response.sendRedirect("dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("erro", "Erro de conexão: " + e.getMessage());
            response.sendRedirect("dashboard");
        }
    }
}