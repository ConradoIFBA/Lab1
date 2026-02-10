package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Pagamento;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.MetPag;

public class PagamentoDAO {
    private Connection conexao;

    public PagamentoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserir(Pagamento pagamento) throws Exception {
        // ✅ CORRIGIDO: Nomes de colunas em snake_case (vendas_id, metpag_id, valor)
        String sql = "INSERT INTO pagamento (vendas_id, metpag_id, valor, data_pagamento) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, pagamento.getVendasId().getIdVendas());
        stmt.setInt(2, pagamento.getMetPagId().getIdMetPag());
        stmt.setFloat(3, pagamento.getValor());
        stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            pagamento.setIdPag(rs.getInt(1));
        }
        stmt.close();
    }

    public void marcarComoExcluido(int idPagamento) throws Exception {
        // ✅ CORRIGIDO: Nomes de colunas em snake_case
        String sql = "UPDATE pagamento SET ativo = false WHERE id_pag = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, idPagamento);
        stmt.executeUpdate();
        stmt.close();
    }

    public List<Pagamento> listarApenasAtivos() throws Exception {
        List<Pagamento> pagamentos = new ArrayList<>();
        // ✅ CORRIGIDO: Nomes de colunas em snake_case
        String sql = "SELECT p.*, v.id_vendas, v.valor as valor_venda, m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.ativo = true " +
                "ORDER BY p.data_pagamento DESC";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Pagamento pagamento = criarPagamentoDoResultSet(rs);
            pagamentos.add(pagamento);
        }
        rs.close();
        stmt.close();
        return pagamentos;
    }

    public List<Pagamento> listarPorVenda(int idVenda) throws Exception {
        List<Pagamento> pagamentos = new ArrayList<>();
        // ✅ CORRIGIDO: Nomes de colunas em snake_case
        String sql = "SELECT p.*, v.id_vendas, v.valor as valor_venda, m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.ativo = true AND p.vendas_id = ? " +
                "ORDER BY p.data_pagamento";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, idVenda);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Pagamento pagamento = criarPagamentoDoResultSet(rs);
            pagamentos.add(pagamento);
        }
        rs.close();
        stmt.close();
        return pagamentos;
    }

    public Pagamento buscar(int id) throws Exception {
        // ✅ CORRIGIDO: Nomes de colunas em snake_case
        String sql = "SELECT p.*, v.id_vendas, v.valor as valor_venda, m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.id_pag = ? AND p.ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        Pagamento pagamento = null;
        if (rs.next()) {
            pagamento = criarPagamentoDoResultSet(rs);
        }
        rs.close();
        stmt.close();
        return pagamento;
    }

    private Pagamento criarPagamentoDoResultSet(ResultSet rs) throws SQLException {
        // Cria objetos básicos para as relações
        Vendas venda = new Vendas();
        venda.setIdVendas(rs.getInt("vendas_id"));
        venda.setValor(rs.getFloat("valor_venda"));

        MetPag metPag = new MetPag();
        metPag.setIdMetPag(rs.getInt("metpag_id"));
        metPag.setDescricao(rs.getString("metodo_pagamento"));

        // Cria o pagamento
        Pagamento pagamento = new Pagamento(
                rs.getInt("id_pag"),
                venda,
                metPag,
                rs.getFloat("valor")
        );

        return pagamento;
    }
}