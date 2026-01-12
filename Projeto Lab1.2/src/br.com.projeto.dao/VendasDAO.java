package br.com.projeto.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.NotaFiscal;

public class VendasDAO {
    private Connection conexao;
    
    public VendasDAO(Connection conexao) {
        this.conexao = conexao;
    }
    
    public void inserir(Vendas venda) throws Exception {
        String sqlVenda = "INSERT INTO vendas (data_vendas, valor, nota_fiscal_emitida, categoria_id) VALUES (?, ?, ?, ?)";
        PreparedStatement stmtVenda = null;
        ResultSet rs = null;
        
        try {
            conexao.setAutoCommit(false); // Inicia transação
            
            // Insere a venda
            stmtVenda = conexao.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS);
            stmtVenda.setTimestamp(1, Timestamp.valueOf(venda.getDataVendas()));
            stmtVenda.setFloat(2, venda.getValor());
            stmtVenda.setString(3, venda.getNotaFiscalEmitida());
            stmtVenda.setInt(4, venda.getCategoria().getIdCategoria());
            stmtVenda.executeUpdate();
            
            // Obtém o ID gerado
            rs = stmtVenda.getGeneratedKeys();
            int idVendaGerado = 0;
            if (rs.next()) {
                idVendaGerado = rs.getInt(1);
                venda.setIdVendas(idVendaGerado);
            }
            
            // Se tem nota fiscal, insere ela
            if (venda.getNotaFiscal() != null && venda.getNotaFiscal().getNumero() > 0) {
                inserirNotaFiscal(venda.getNotaFiscal(), idVendaGerado);
            }
            
            conexao.commit(); // Confirma transação
            
        } catch (Exception e) {
            conexao.rollback(); // Rollback em caso de erro
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmtVenda != null) stmtVenda.close();
            conexao.setAutoCommit(true);
        }
    }
    
    private void inserirNotaFiscal(NotaFiscal notaFiscal, int idVenda) throws Exception {
        String sql = "INSERT INTO notafiscal (numero, data_emissao, valor, vendas_id) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, notaFiscal.getNumero());
        stmt.setTimestamp(2, Timestamp.valueOf(notaFiscal.getDataEmissao()));
        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, idVenda);
        stmt.executeUpdate();
        stmt.close();
    }
    
    public List<Vendas> listar() throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                    "FROM vendas v " +
                    "JOIN categoria c ON v.categoria_id = c.id_categoria " +
                    "LEFT JOIN notafiscal nf ON v.id_vendas = nf.vendas_id " +
                    "WHERE v.ativo = true " +
                    "ORDER BY v.data_vendas DESC";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Vendas venda = criarVendaDoResultSet(rs);
            vendas.add(venda);
        }
        rs.close();
        stmt.close();
        return vendas;
    }
    
    public void editar(Vendas venda) throws Exception {
        String sql = "UPDATE vendas SET data_vendas = ?, valor = ?, nota_fiscal_emitida = ?, categoria_id = ? " +
                    "WHERE id_vendas = ?";
        PreparedStatement stmt = null;
        
        try {
            conexao.setAutoCommit(false);
            
            // Atualiza a venda
            stmt = conexao.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(venda.getDataVendas()));
            stmt.setFloat(2, venda.getValor());
            stmt.setString(3, venda.getNotaFiscalEmitida());
            stmt.setInt(4, venda.getCategoria().getIdCategoria());
            stmt.setInt(5, venda.getIdVendas());
            stmt.executeUpdate();
            
            // Atualiza ou insere nota fiscal
            if (venda.getNotaFiscal() != null) {
                if (venda.getNotaFiscal().getIdNotaFiscal() > 0) {
                    atualizarNotaFiscal(venda.getNotaFiscal());
                } else {
                    inserirNotaFiscal(venda.getNotaFiscal(), venda.getIdVendas());
                }
            }
            
            conexao.commit();
            
        } catch (Exception e) {
            conexao.rollback();
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            conexao.setAutoCommit(true);
        }
    }
    
    private void atualizarNotaFiscal(NotaFiscal notaFiscal) throws Exception {
        String sql = "UPDATE notafiscal SET numero = ?, data_emissao = ?, valor = ? WHERE id_nota_fiscal = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, notaFiscal.getNumero());
        stmt.setTimestamp(2, Timestamp.valueOf(notaFiscal.getDataEmissao()));
        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, notaFiscal.getIdNotaFiscal());
        stmt.executeUpdate();
        stmt.close();
    }
    
    public void excluir(int id) throws Exception {
        // Exclusão lógica
        String sql = "UPDATE vendas SET ativo = false WHERE id_vendas = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }
    
    public Vendas buscar(int id) throws Exception {
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                    "FROM vendas v " +
                    "JOIN categoria c ON v.categoria_id = c.id_categoria " +
                    "LEFT JOIN notafiscal nf ON v.id_vendas = nf.vendas_id " +
                    "WHERE v.id_vendas = ? AND v.ativo = true";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        
        Vendas venda = null;
        if (rs.next()) {
            venda = criarVendaDoResultSet(rs);
        }
        rs.close();
        stmt.close();
        return venda;
    }
    
    public List<Vendas> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                    "FROM vendas v " +
                    "JOIN categoria c ON v.categoria_id = c.id_categoria " +
                    "LEFT JOIN notafiscal nf ON v.id_vendas = nf.vendas_id " +
                    "WHERE v.ativo = true AND v.data_vendas BETWEEN ? AND ? " +
                    "ORDER BY v.data_vendas";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setTimestamp(1, Timestamp.valueOf(dataInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(dataFim));
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Vendas venda = criarVendaDoResultSet(rs);
            vendas.add(venda);
        }
        rs.close();
        stmt.close();
        return vendas;
    }
    
    private Vendas criarVendaDoResultSet(ResultSet rs) throws SQLException {
        // Cria a categoria
        Categoria categoria = new Categoria(
            rs.getInt("categoria_id"),
            rs.getString("nome_categoria")
        );
        
        // Cria a venda
        Vendas venda = new Vendas(
            rs.getInt("id_vendas"),
            rs.getTimestamp("data_vendas").toLocalDateTime(),
            rs.getFloat("valor"),
            rs.getString("nota_fiscal_emitida"),
            categoria,
            null // Nota fiscal será criada abaixo se existir
        );
        
        // Cria nota fiscal se existir
        if (rs.getInt("id_nota_fiscal") > 0) {
            NotaFiscal nf = new NotaFiscal(
                rs.getInt("id_nota_fiscal"),
                rs.getInt("numero"),
                rs.getTimestamp("data_emissao").toLocalDateTime(),
                rs.getFloat("nf_valor")
            );
            venda.setNotaFiscal(nf);
        }
        
        return venda;
    }
}