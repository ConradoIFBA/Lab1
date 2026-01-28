package br.com.projeto.dao;

import java.sql.*;

public class GenericoDAO {
    
    private Connection conexao;
    
    public GenericoDAO(Connection conexao) {
        this.conexao = conexao;
    }
    
    public Connection getConexao() {
        return conexao;
    }
    
    public int executarComando(String sql, Object... parametros) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            return stmt.executeUpdate();
        }
    }
    
    public ResultSet executarConsulta(String sql, Object... parametros) throws SQLException {
        PreparedStatement stmt = conexao.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++) {
            stmt.setObject(i + 1, parametros[i]);
        }
        return stmt.executeQuery();
    }
    
    public void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}