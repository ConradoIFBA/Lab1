package br.com.projeto.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import br.com.projeto.dao.VendasDAO;
import br.com.projeto.dao.NotaFiscalDAO;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.NotaFiscal;
import br.com.projeto.model.Usuario;
import br.com.projeto.utils.Conexao;

@WebServlet("/venda")
public class VendaController extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("login");
            return;
        }
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        try {
            // Criar objeto Venda
            Vendas venda = new Vendas();
            venda.setDataVendas(new java.util.Date()); // Data atual
            
            // Converter valor
            String valorStr = request.getParameter("valor").replace(",", ".");
            float valor = Float.parseFloat(valorStr);
            venda.setValor(valor);
            
            venda.setNotaFiscalEmitida(request.getParameter("notaFiscal"));
            venda.setDescricao(request.getParameter("descricao"));
            
            // Categoria
            int categoriaId = Integer.parseInt(request.getParameter("categoria"));
            br.com.projeto.model.Categoria categoria = new br.com.projeto.model.Categoria();
            categoria.setIdCategoria(categoriaId);
            venda.setCategoria(categoria);
            
            // Usuário
            venda.setUsuarioId(usuario.getIdUsuario());
            
            // Se tem nota fiscal, cria ela
            if ("S".equals(request.getParameter("notaFiscal"))) {
                NotaFiscal nf = new NotaFiscal();
                nf.setNumero(request.getParameter("numeroNota"));
                
                // Converter data da NF
                String dataNfStr = request.getParameter("dataEmissaoNF");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                nf.setDataEmissao(sdf.parse(dataNfStr));
                
                nf.setValor(valor);
                nf.setUsuarioId(usuario.getIdUsuario());
                
                // Salvar nota fiscal primeiro
                NotaFiscalDAO nfDAO = new NotaFiscalDAO(Conexao.getConnection());
                nfDAO.inserir(nf);
                
                venda.setNotaFiscal(nf);
            }
            
            // Salvar venda
            VendasDAO vendasDAO = new VendasDAO(Conexao.getConnection());
            vendasDAO.inserir(venda);
            
            session.setAttribute("sucesso", "Venda cadastrada com sucesso!");
            response.sendRedirect("dashboard");
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("erro", "Erro ao cadastrar venda: " + e.getMessage());
            response.sendRedirect("dashboard");
        }
    }
    
    // Método para excluir venda (opcional)
    @WebServlet("/venda/excluir")
    public static class ExcluirVendaController extends HttpServlet {
        protected void doPost(HttpServletRequest request, HttpServletResponse response) 
                throws ServletException, IOException {
            
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                response.sendRedirect("login");
                return;
            }
            
            try {
                int vendaId = Integer.parseInt(request.getParameter("id"));
                VendasDAO vendasDAO = new VendasDAO(Conexao.getConnection());
                
                // Verificar se a venda pertence ao usuário
                Usuario usuario = (Usuario) session.getAttribute("usuario");
                Vendas venda = vendasDAO.buscar(vendaId);
                
                if (venda != null && venda.getUsuarioId() == usuario.getIdUsuario()) {
                    vendasDAO.excluir(vendaId);
                    session.setAttribute("sucesso", "Venda excluída com sucesso!");
                } else {
                    session.setAttribute("erro", "Venda não encontrada ou acesso negado!");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("erro", "Erro ao excluir venda: " + e.getMessage());
            }
            
            response.sendRedirect("dashboard");
        }
    }
}