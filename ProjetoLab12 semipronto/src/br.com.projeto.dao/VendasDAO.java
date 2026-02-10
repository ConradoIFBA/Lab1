package br.com.projeto.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.Categoria;
import br.com.projeto.model.NotaFiscal;

/**
 * VendasDAO - Data Access Object para manipulação de vendas
 * Versão: 2.0 - Completa e sem duplicatas
 */
public class VendasDAO {
    private Connection conexao;

    public VendasDAO(Connection conexao) {
        this.conexao = conexao;
    }

    // ============================================================
    // MÉTODOS DE INSERÇÃO
    // ============================================================

    /**
     * Insere uma nova venda no banco de dados
     * @param venda Objeto Vendas a ser inserido
     * @throws Exception Erro ao inserir
     */
    public void inserir(Vendas venda) throws Exception {
        String sqlVenda = "INSERT INTO vendas (data_vendas, valor, nota_fiscal_emitida, categoria_id, usuario_id, descricao) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmtVenda = null;
        ResultSet rs = null;

        try {
            conexao.setAutoCommit(false);

            stmtVenda = conexao.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS);

            // Converte java.util.Date ou LocalDateTime para Timestamp
            if (venda.getDataVendas() instanceof java.util.Date) {
                stmtVenda.setTimestamp(1, new Timestamp(((java.util.Date)venda.getDataVendas()).getTime()));
            } else if (venda.getDataVendas() instanceof LocalDateTime) {
                stmtVenda.setTimestamp(1, Timestamp.valueOf((LocalDateTime)venda.getDataVendas()));
            } else {
                stmtVenda.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }

            stmtVenda.setFloat(2, venda.getValor());
            stmtVenda.setString(3, venda.getNotaFiscalEmitida());
            stmtVenda.setInt(4, venda.getCategoria().getIdCategoria());
            stmtVenda.setInt(5, venda.getUsuarioId());
            stmtVenda.setString(6, venda.getDescricao());
            stmtVenda.executeUpdate();

            // Recupera ID gerado
            rs = stmtVenda.getGeneratedKeys();
            int idVendaGerado = 0;
            if (rs.next()) {
                idVendaGerado = rs.getInt(1);
                venda.setIdVendas(idVendaGerado);
            }

            // Se tem nota fiscal, insere ela
            if (venda.getNotaFiscal() != null && venda.getNotaFiscal().getNumero() != null) {
                inserirNotaFiscal(venda.getNotaFiscal(), idVendaGerado, venda.getUsuarioId());
            }

            conexao.commit();
            System.out.println("✅ Venda inserida com sucesso! ID: " + idVendaGerado);

        } catch (Exception e) {
            conexao.rollback();
            System.err.println("❌ Erro ao inserir venda: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (stmtVenda != null) stmtVenda.close();
            conexao.setAutoCommit(true);
        }
    }

    /**
     * Insere uma nota fiscal associada a uma venda
     */
    private void inserirNotaFiscal(NotaFiscal notaFiscal, int idVenda, int usuarioId) throws Exception {
        String sql = "INSERT INTO nota_fiscal (numero, data_emissao, valor, vendas_id, usuario_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, notaFiscal.getNumero());

        if (notaFiscal.getDataEmissao() instanceof java.util.Date) {
            stmt.setTimestamp(2, new Timestamp(((java.util.Date)notaFiscal.getDataEmissao()).getTime()));
        } else if (notaFiscal.getDataEmissao() instanceof LocalDateTime) {
            stmt.setTimestamp(2, Timestamp.valueOf((LocalDateTime)notaFiscal.getDataEmissao()));
        } else {
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }

        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, idVenda);
        stmt.setInt(5, usuarioId);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            notaFiscal.setIdNotaFiscal(rs.getInt(1));
        }
        rs.close();
        stmt.close();
    }

    // ============================================================
    // MÉTODOS DE ATUALIZAÇÃO
    // ============================================================

    /**
     * Atualiza uma venda existente
     */
    public void editar(Vendas venda) throws Exception {
        String sql = "UPDATE vendas SET data_vendas = ?, valor = ?, nota_fiscal_emitida = ?, " +
                "categoria_id = ?, descricao = ? WHERE id_vendas = ?";
        PreparedStatement stmt = null;

        try {
            conexao.setAutoCommit(false);

            stmt = conexao.prepareStatement(sql);

            if (venda.getDataVendas() instanceof java.util.Date) {
                stmt.setTimestamp(1, new Timestamp(((java.util.Date)venda.getDataVendas()).getTime()));
            } else if (venda.getDataVendas() instanceof LocalDateTime) {
                stmt.setTimestamp(1, Timestamp.valueOf((LocalDateTime)venda.getDataVendas()));
            } else {
                stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setFloat(2, venda.getValor());
            stmt.setString(3, venda.getNotaFiscalEmitida());
            stmt.setInt(4, venda.getCategoria().getIdCategoria());
            stmt.setString(5, venda.getDescricao());
            stmt.setInt(6, venda.getIdVendas());
            stmt.executeUpdate();

            // Atualiza ou insere nota fiscal
            if (venda.getNotaFiscal() != null) {
                if (venda.getNotaFiscal().getIdNotaFiscal() > 0) {
                    atualizarNotaFiscal(venda.getNotaFiscal());
                } else {
                    inserirNotaFiscal(venda.getNotaFiscal(), venda.getIdVendas(), venda.getUsuarioId());
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

    /**
     * Atualiza uma nota fiscal existente
     */
    private void atualizarNotaFiscal(NotaFiscal notaFiscal) throws Exception {
        String sql = "UPDATE nota_fiscal SET numero = ?, data_emissao = ?, valor = ? WHERE id_nota_fiscal = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setString(1, notaFiscal.getNumero());

        if (notaFiscal.getDataEmissao() instanceof java.util.Date) {
            stmt.setTimestamp(2, new Timestamp(((java.util.Date)notaFiscal.getDataEmissao()).getTime()));
        } else if (notaFiscal.getDataEmissao() instanceof LocalDateTime) {
            stmt.setTimestamp(2, Timestamp.valueOf((LocalDateTime)notaFiscal.getDataEmissao()));
        } else {
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        }

        stmt.setFloat(3, notaFiscal.getValor());
        stmt.setInt(4, notaFiscal.getIdNotaFiscal());
        stmt.executeUpdate();
        stmt.close();
    }

    // ============================================================
    // MÉTODOS DE EXCLUSÃO (LÓGICA)
    // ============================================================

    /**
     * Exclui logicamente uma venda (ativo = false)
     */
    public void excluir(int id) throws Exception {
        String sql = "UPDATE vendas SET ativo = false WHERE id_vendas = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }

    // ============================================================
    // MÉTODOS DE CONSULTA - BÁSICOS
    // ============================================================

    /**
     * Lista todas as vendas ativas
     */
    public List<Vendas> listar() throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                "FROM vendas v " +
                "INNER JOIN categoria c ON v.categoria_id = c.id_categoria " +
                "LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id " +
                "WHERE v.ativo = true " +
                "ORDER BY v.data_vendas DESC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            vendas.add(criarVendaDoResultSet(rs));
        }

        rs.close();
        stmt.close();
        return vendas;
    }

    /**
     * Busca uma venda específica por ID
     */
    public Vendas buscar(int id) throws Exception {
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                "FROM vendas v " +
                "INNER JOIN categoria c ON v.categoria_id = c.id_categoria " +
                "LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id " +
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

    // ============================================================
    // MÉTODOS DE CONSULTA - POR USUÁRIO
    // ============================================================

    /**
     * Lista últimas N vendas de um usuário
     * @param usuarioId ID do usuário
     * @param limite Quantidade máxima de vendas
     */
    public List<Vendas> listarPorUsuario(int usuarioId, int limite) throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                "FROM vendas v " +
                "INNER JOIN categoria c ON v.categoria_id = c.id_categoria " +
                "LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id " +
                "WHERE v.ativo = true AND v.usuario_id = ? " +
                "ORDER BY v.data_vendas DESC " +
                "LIMIT ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        stmt.setInt(2, limite);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            vendas.add(criarVendaDoResultSet(rs));
        }

        rs.close();
        stmt.close();
        return vendas;
    }

    // ============================================================
    // MÉTODOS DE CONSULTA - POR PERÍODO
    // ============================================================

    /**
     * Lista vendas de um mês/ano específico
     */
    public List<Vendas> listarPorMesAno(int usuarioId, int mes, int ano) throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                "FROM vendas v " +
                "INNER JOIN categoria c ON v.categoria_id = c.id_categoria " +
                "LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id " +
                "WHERE v.ativo = true AND v.usuario_id = ? " +
                "AND MONTH(v.data_vendas) = ? AND YEAR(v.data_vendas) = ? " +
                "ORDER BY v.data_vendas";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        stmt.setInt(2, mes);
        stmt.setInt(3, ano);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            vendas.add(criarVendaDoResultSet(rs));
        }

        rs.close();
        stmt.close();
        return vendas;
    }

    /**
     * Lista vendas por período (data início e fim)
     */
    public List<Vendas> listarPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) throws Exception {
        List<Vendas> vendas = new ArrayList<>();
        String sql = "SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor " +
                "FROM vendas v " +
                "INNER JOIN categoria c ON v.categoria_id = c.id_categoria " +
                "LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id " +
                "WHERE v.ativo = true AND v.data_vendas BETWEEN ? AND ? " +
                "ORDER BY v.data_vendas";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setTimestamp(1, Timestamp.valueOf(dataInicio));
        stmt.setTimestamp(2, Timestamp.valueOf(dataFim));
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            vendas.add(criarVendaDoResultSet(rs));
        }

        rs.close();
        stmt.close();
        return vendas;
    }

    /**
     * Lista anos que possuem vendas do usuário
     */
    public List<Integer> listarAnosComVendas(int usuarioId) throws Exception {
        List<Integer> anos = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(data_vendas) AS ano " +
                "FROM vendas " +
                "WHERE usuario_id = ? AND ativo = true " +
                "ORDER BY ano DESC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            anos.add(rs.getInt("ano"));
        }

        rs.close();
        stmt.close();
        return anos;
    }

    /**
     * Lista vendas de um ano com filtro de Nota Fiscal
     * @param usuarioId ID do usuário
     * @param ano Ano das vendas
     * @param filtroNF "todas", "comNF", "semNF"
     */
    public List<Vendas> listarPorAnoComFiltroNF(int usuarioId, int ano, String filtroNF) throws Exception {
        List<Vendas> vendas = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.*, c.nome_categoria, nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor ");
        sql.append("FROM vendas v ");
        sql.append("INNER JOIN categoria c ON v.categoria_id = c.id_categoria ");
        sql.append("LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id ");
        sql.append("WHERE v.usuario_id = ? ");
        sql.append("AND YEAR(v.data_vendas) = ? ");
        sql.append("AND v.ativo = true ");

        // Aplicar filtro de NF
        if ("comNF".equals(filtroNF)) {
            sql.append("AND v.nota_fiscal_emitida = 'S' ");
        } else if ("semNF".equals(filtroNF)) {
            sql.append("AND v.nota_fiscal_emitida = 'N' ");
        }

        sql.append("ORDER BY v.data_vendas DESC");

        PreparedStatement stmt = conexao.prepareStatement(sql.toString());
        stmt.setInt(1, usuarioId);
        stmt.setInt(2, ano);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            vendas.add(criarVendaDoResultSet(rs));
        }

        rs.close();
        stmt.close();
        return vendas;
    }

    // ============================================================
    // MÉTODOS DE CÁLCULO E RESUMOS
    // ============================================================

    /**
     * Calcula total de vendas do mês atual
     */
    public double calcularTotalMes(int usuarioId) throws Exception {
        Calendar cal = Calendar.getInstance();
        int mesAtual = cal.get(Calendar.MONTH) + 1;
        int anoAtual = cal.get(Calendar.YEAR);

        String sql = "SELECT COALESCE(SUM(valor), 0) as total " +
                "FROM vendas " +
                "WHERE usuario_id = ? AND ativo = true " +
                "AND MONTH(data_vendas) = ? AND YEAR(data_vendas) = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        stmt.setInt(2, mesAtual);
        stmt.setInt(3, anoAtual);
        ResultSet rs = stmt.executeQuery();

        double total = 0;
        if (rs.next()) {
            total = rs.getDouble("total");
        }

        rs.close();
        stmt.close();
        return total;
    }

    /**
     * Resumo mensal do ano (mês, quantidade, total)
     */
    public List<Object[]> resumoMensal(int usuarioId, int ano) throws Exception {
        List<Object[]> resumo = new ArrayList<>();
        String sql = "SELECT MONTH(data_vendas) as mes, " +
                "COUNT(*) as quantidade, " +
                "SUM(valor) as total " +
                "FROM vendas " +
                "WHERE usuario_id = ? AND YEAR(data_vendas) = ? AND ativo = true " +
                "GROUP BY MONTH(data_vendas) " +
                "ORDER BY mes";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        stmt.setInt(2, ano);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Object[] linha = new Object[3];
            linha[0] = rs.getInt("mes");
            linha[1] = rs.getInt("quantidade");
            linha[2] = rs.getDouble("total");
            resumo.add(linha);
        }

        rs.close();
        stmt.close();
        return resumo;
    }

    // ============================================================
    // MÉTODO AUXILIAR - CRIAR OBJETO DO RESULTSET
    // ============================================================

    /**
     * Cria objeto Vendas a partir do ResultSet
     * Método auxiliar reutilizável
     */
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
                null
        );

        venda.setUsuarioId(rs.getInt("usuario_id"));

        String descricao = rs.getString("descricao");
        venda.setDescricao(descricao != null ? descricao : "");

        // Cria nota fiscal se existir
        if (rs.getObject("id_nota_fiscal") != null) {
            NotaFiscal nf = new NotaFiscal(
                    rs.getInt("id_nota_fiscal"),
                    rs.getString("numero"),
                    rs.getTimestamp("data_emissao").toLocalDateTime(),
                    rs.getFloat("nf_valor")
            );
            venda.setNotaFiscal(nf);
        }

        return venda;
    }
}