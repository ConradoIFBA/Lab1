package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Categoria;

/**
 * DAO para Categoria - VERSÃO CORRIGIDA
 * ✅ CORRIGIDO: Usa PreparedStatement em TODOS os métodos
 * ✅ Elimina vulnerabilidade de SQL Injection
 */
public class CategoriaDAO {

    private Connection conexao;

    public CategoriaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Categoria> listar() throws Exception {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nome_categoria, ativo FROM categoria WHERE ativo = 1 ORDER BY nome_categoria";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNomeCategoria(rs.getString("nome_categoria"));
                categoria.setAtivo(rs.getBoolean("ativo"));
                categorias.add(categoria);
            }
        }
        return categorias;
    }

    public List<Categoria> listarTodas() throws Exception {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nome_categoria, ativo FROM categoria ORDER BY nome_categoria";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Categoria categoria = new Categoria();
                categoria.setIdCategoria(rs.getInt("id_categoria"));
                categoria.setNomeCategoria(rs.getString("nome_categoria"));
                categoria.setAtivo(rs.getBoolean("ativo"));
                categorias.add(categoria);
            }
        }
        return categorias;
    }

    // ✅ CORRIGIDO: PreparedStatement com parâmetro
    public Categoria buscarPorId(int id) throws Exception {
        Categoria categoria = null;
        String sql = "SELECT id_categoria, nome_categoria, ativo FROM categoria WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria = new Categoria();
                    categoria.setIdCategoria(rs.getInt("id_categoria"));
                    categoria.setNomeCategoria(rs.getString("nome_categoria"));
                    categoria.setAtivo(rs.getBoolean("ativo"));
                }
            }
        }
        return categoria;
    }

    // ✅ CORRIGIDO: PreparedStatement previne SQL Injection
    public void inserir(Categoria categoria) throws Exception {
        String sql = "INSERT INTO categoria (nome_categoria, ativo) VALUES (?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNomeCategoria());
            stmt.setBoolean(2, categoria.isAtivo());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    categoria.setIdCategoria(rs.getInt(1));
                }
            }
        }
    }

    // ✅ CORRIGIDO: PreparedStatement
    public void atualizar(Categoria categoria) throws Exception {
        String sql = "UPDATE categoria SET nome_categoria = ?, ativo = ? WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNomeCategoria());
            stmt.setBoolean(2, categoria.isAtivo());
            stmt.setInt(3, categoria.getIdCategoria());
            stmt.executeUpdate();
        }
    }

    // ✅ CORRIGIDO: PreparedStatement
    public void deletar(int id) throws Exception {
        String sql = "UPDATE categoria SET ativo = 0 WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ✅ CORRIGIDO: PreparedStatement
    public void deletarPermanentemente(int id) throws Exception {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}