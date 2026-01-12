package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Categoria;

public class CategoriaDAO {
    private Connection conexao;
    
    public CategoriaDAO(Connection conexao) {
        this.conexao = conexao;
    }
    
    public void inserir(Categoria categoria) throws Exception {
        String sql = "INSERT INTO categoria (nome_categoria) VALUES (?)";
        PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, categoria.getNomeCategoria());
        stmt.executeUpdate();
        
        // Recupera o ID gerado
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            categoria.setIdCategoria(rs.getInt(1));
        }
        stmt.close();
    }
    
    public List<Categoria> listar() throws Exception {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria WHERE ativo = true ORDER BY nome_categoria";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Categoria categoria = new Categoria(
                rs.getInt("id_categoria"),
                rs.getString("nome_categoria")
            );
            categorias.add(categoria);
        }
        rs.close();
        stmt.close();
        return categorias;
    }
    
    public void editar(Categoria categoria) throws Exception {
        String sql = "UPDATE categoria SET nome_categoria = ? WHERE id_categoria = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setString(1, categoria.getNomeCategoria());
        stmt.setInt(2, categoria.getIdCategoria());
        stmt.executeUpdate();
        stmt.close();
    }
    
    public void excluir(int id) throws Exception {
        // Exclusão lógica
        String sql = "UPDATE categoria SET ativo = false WHERE id_categoria = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }
    
    public Categoria buscar(int id) throws Exception {
        String sql = "SELECT * FROM categoria WHERE id_categoria = ? AND ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        Categoria categoria = null;
        if (rs.next()) {
            categoria = new Categoria(
                rs.getInt("id_categoria"),
                rs.getString("nome_categoria")
            );
        }
        rs.close();
        stmt.close();
        return categoria;
    }
}