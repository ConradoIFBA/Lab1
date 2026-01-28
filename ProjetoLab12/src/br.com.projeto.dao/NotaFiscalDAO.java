package br.com.projeto.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.NotaFiscal;

public class NotaFiscalDAO {
    private Connection conexao;

    public NotaFiscalDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserir(NotaFiscal notaFiscal) throws Exception {
        String sql = "INSERT INTO notafiscal (numero, data_emissao, valor, vendas_id, usuario_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        stmt.setString(1, notaFiscal.getNumero());

        // Converte Object para Timestamp de forma segura
        Object dataEmissao = notaFiscal.getDataEmissao();
        if (dataEmissao instanceof java.util.Date) {
            stmt.setTimestamp(2, new Timestamp(((java.util.Date)dataEmissao).getTime()));
        } else if (dataEmissao instanceof LocalDateTime) {
            stmt.setTimestamp(2, Timestamp.valueOf((LocalDateTime)dataEmissao));
        } else if (dataEmissao instanceof String) {
            stmt.setTimestamp(2, Timestamp.valueOf((String)dataEmissao));
        } else {
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }

        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, notaFiscal.getVendasId());
        stmt.setInt(5, notaFiscal.getUsuarioId());

        stmt.executeUpdate();

        // Recupera o ID gerado
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            notaFiscal.setIdNotaFiscal(rs.getInt(1));
        }

        rs.close();
        stmt.close();
    }

    public List<NotaFiscal> listar() throws Exception {
        List<NotaFiscal> notas = new ArrayList<>();
        String sql = "SELECT * FROM notafiscal WHERE ativo = true ORDER BY data_emissao DESC";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            NotaFiscal nf = new NotaFiscal(
                    rs.getInt("id_nota_fiscal"),
                    rs.getString("numero"),
                    rs.getTimestamp("data_emissao").toLocalDateTime(),
                    rs.getFloat("valor")
            );
            nf.setVendasId(rs.getInt("vendas_id"));
            nf.setUsuarioId(rs.getInt("usuario_id"));
            notas.add(nf);
        }

        rs.close();
        stmt.close();
        return notas;
    }

    public List<NotaFiscal> listarPorUsuario(int usuarioId) throws Exception {
        List<NotaFiscal> notas = new ArrayList<>();
        String sql = "SELECT * FROM notafiscal WHERE usuario_id = ? AND ativo = true ORDER BY data_emissao DESC";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            NotaFiscal nf = new NotaFiscal(
                    rs.getInt("id_nota_fiscal"),
                    rs.getString("numero"),
                    rs.getTimestamp("data_emissao").toLocalDateTime(),
                    rs.getFloat("valor")
            );
            nf.setVendasId(rs.getInt("vendas_id"));
            nf.setUsuarioId(rs.getInt("usuario_id"));
            notas.add(nf);
        }

        rs.close();
        stmt.close();
        return notas;
    }

    public void editar(NotaFiscal notaFiscal) throws Exception {
        String sql = "UPDATE notafiscal SET numero = ?, data_emissao = ?, valor = ? WHERE id_nota_fiscal = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);

        stmt.setString(1, notaFiscal.getNumero());

        // Converte Object para Timestamp de forma segura
        Object dataEmissao = notaFiscal.getDataEmissao();
        if (dataEmissao instanceof java.util.Date) {
            stmt.setTimestamp(2, new Timestamp(((java.util.Date)dataEmissao).getTime()));
        } else if (dataEmissao instanceof LocalDateTime) {
            stmt.setTimestamp(2, Timestamp.valueOf((LocalDateTime)dataEmissao));
        } else if (dataEmissao instanceof String) {
            stmt.setTimestamp(2, Timestamp.valueOf((String)dataEmissao));
        } else {
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }

        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, notaFiscal.getIdNotaFiscal());

        stmt.executeUpdate();
        stmt.close();
    }

    public void excluir(int id) throws Exception {
        // Exclusão lógica
        String sql = "UPDATE notafiscal SET ativo = false WHERE id_nota_fiscal = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }

    public NotaFiscal buscar(int id) throws Exception {
        String sql = "SELECT * FROM notafiscal WHERE id_nota_fiscal = ? AND ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        NotaFiscal nf = null;
        if (rs.next()) {
            nf = new NotaFiscal(
                    rs.getInt("id_nota_fiscal"),
                    rs.getString("numero"),
                    rs.getTimestamp("data_emissao").toLocalDateTime(),
                    rs.getFloat("valor")
            );
            nf.setVendasId(rs.getInt("vendas_id"));
            nf.setUsuarioId(rs.getInt("usuario_id"));
        }

        rs.close();
        stmt.close();
        return nf;
    }

    public NotaFiscal buscarPorVenda(int vendasId) throws Exception {
        String sql = "SELECT * FROM notafiscal WHERE vendas_id = ? AND ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, vendasId);
        ResultSet rs = stmt.executeQuery();

        NotaFiscal nf = null;
        if (rs.next()) {
            nf = new NotaFiscal(
                    rs.getInt("id_nota_fiscal"),
                    rs.getString("numero"),
                    rs.getTimestamp("data_emissao").toLocalDateTime(),
                    rs.getFloat("valor")
            );
            nf.setVendasId(rs.getInt("vendas_id"));
            nf.setUsuarioId(rs.getInt("usuario_id"));
        }

        rs.close();
        stmt.close();
        return nf;
    }
}