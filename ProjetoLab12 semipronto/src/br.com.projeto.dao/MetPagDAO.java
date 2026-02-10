package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.MetPag;

public class MetPagDAO {
    private Connection conexao;

    public MetPagDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserir(MetPag metPag) throws Exception {
        // ✅ CORRIGIDO: Nome correto da tabela (metodo_pagamento) e coluna (descricao)
        String sql = "INSERT INTO metodo_pagamento (descricao) VALUES (?)";
        PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, metPag.getDescricao());
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            metPag.setIdMetPag(rs.getInt(1));
        }
        stmt.close();
    }

    public List<MetPag> listar() throws Exception {
        List<MetPag> metPags = new ArrayList<>();
        // ✅ CORRIGIDO: Nome correto da tabela (metodo_pagamento) e colunas
        String sql = "SELECT * FROM metodo_pagamento WHERE ativo = true ORDER BY descricao";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            MetPag metPag = new MetPag(
                    rs.getInt("id_metpag"),
                    rs.getString("descricao")
            );
            metPags.add(metPag);
        }
        rs.close();
        stmt.close();
        return metPags;
    }

    public void editar(MetPag metPag) throws Exception {
        // ✅ CORRIGIDO: Nome correto da tabela (metodo_pagamento) e colunas
        String sql = "UPDATE metodo_pagamento SET descricao = ? WHERE id_metpag = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setString(1, metPag.getDescricao());
        stmt.setInt(2, metPag.getIdMetPag());
        stmt.executeUpdate();
        stmt.close();
    }

    public void excluir(int id) throws Exception {
        // ✅ CORRIGIDO: Nome correto da tabela (metodo_pagamento) e coluna (id_metpag)
        String sql = "UPDATE metodo_pagamento SET ativo = false WHERE id_metpag = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }

    public MetPag buscar(int id) throws Exception {
        // ✅ CORRIGIDO: Nome correto da tabela (metodo_pagamento) e colunas
        String sql = "SELECT * FROM metodo_pagamento WHERE id_metpag = ? AND ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        MetPag metPag = null;
        if (rs.next()) {
            metPag = new MetPag(
                    rs.getInt("id_metpag"),
                    rs.getString("descricao")
            );
        }
        rs.close();
        stmt.close();
        return metPag;
    }
}