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

    /**
     * ================================================================
     * LISTAR VENDAS POR ANO, MÊS E FILTRO DE NOTA FISCAL
     * ================================================================
     * 
     * PROPÓSITO:
     * Busca vendas de um usuário com filtros de ano, mês e nota fiscal.
     * Usado pela tela de Histórico para exibir vendas filtradas.
     * 
     * PARÂMETROS:
     * @param usuarioId - ID do usuário (obrigatório)
     * @param ano - Ano das vendas (obrigatório)
     * @param mes - Mês das vendas (0 = todos, 1-12 = mês específico)
     * @param filtroNF - Filtro de nota fiscal ("todas", "comNF", "semNF")
     * 
     * FILTRO DE MÊS:
     * - mes = 0: Não filtra por mês, retorna TODOS os meses do ano
     * - mes = 1-12: Filtra pelo mês específico (1=Jan, 2=Fev, ..., 12=Dez)
     * 
     * FILTRO DE NF:
     * - "todas": Retorna todas as vendas (com e sem NF)
     * - "comNF": Retorna apenas vendas COM nota fiscal (nota_fiscal_emitida='S')
     * - "semNF": Retorna apenas vendas SEM nota fiscal (nota_fiscal_emitida='N')
     * 
     * QUERY SQL:
     * Faz INNER JOIN com categoria (obrigatória para toda venda)
     * Faz LEFT JOIN com nota_fiscal (opcional, nem toda venda tem NF)
     * 
     * ORDENAÇÃO:
     * Vendas ordenadas por data DESC (mais recentes primeiro)
     * 
     * RETORNO:
     * List<Vendas> com objetos completos incluindo:
     * - Dados da venda (id, valor, data, descricao)
     * - Categoria associada
     * - Nota fiscal (se houver)
     * 
     * EXEMPLO DE USO:
     * // Buscar todas as vendas de fevereiro/2026:
     * List<Vendas> vendas = dao.listarPorAnoEMesComFiltroNF(1, 2026, 2, "todas");
     * 
     * // Buscar vendas com NF do ano todo de 2026:
     * List<Vendas> vendas = dao.listarPorAnoEMesComFiltroNF(1, 2026, 0, "comNF");
     * 
     * @throws Exception Se houver erro na consulta SQL
     */
    public List<Vendas> listarPorAnoEMesComFiltroNF(int usuarioId, int ano, int mes, String filtroNF) throws Exception {
        List<Vendas> vendas = new ArrayList<>();

        // ========== CONSTRUIR QUERY SQL DINÂMICA ==========
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.*, c.nome_categoria, ");
        sql.append("nf.id_nota_fiscal, nf.numero, nf.data_emissao, nf.valor as nf_valor ");
        sql.append("FROM vendas v ");
        sql.append("INNER JOIN categoria c ON v.categoria_id = c.id_categoria ");
        sql.append("LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id ");
        sql.append("WHERE v.usuario_id = ? ");
        sql.append("AND YEAR(v.data_vendas) = ? ");

        // ========== FILTRO DE MÊS (CONDICIONAL) ==========
        // Se mes > 0, adiciona filtro MONTH()
        // Se mes = 0, não adiciona (retorna todos os meses)
        if (mes > 0 && mes <= 12) {
            sql.append("AND MONTH(v.data_vendas) = ? ");
        }

        sql.append("AND v.ativo = true ");

        // ========== FILTRO DE NOTA FISCAL ==========
        // "comNF": apenas com NF
        // "semNF": apenas sem NF
        // "todas" ou qualquer outro valor: não filtra
        if ("comNF".equals(filtroNF)) {
            sql.append("AND v.nota_fiscal_emitida = 'S' ");
        } else if ("semNF".equals(filtroNF)) {
            sql.append("AND v.nota_fiscal_emitida = 'N' ");
        }

        sql.append("ORDER BY v.data_vendas DESC");

        // ========== PREPARAR E EXECUTAR STATEMENT ==========
        PreparedStatement stmt = conexao.prepareStatement(sql.toString());
        
        // Parâmetros obrigatórios
        int paramIndex = 1;
        stmt.setInt(paramIndex++, usuarioId);
        stmt.setInt(paramIndex++, ano);
        
        // Parâmetro condicional: mês (só se mes > 0)
        if (mes > 0 && mes <= 12) {
            stmt.setInt(paramIndex++, mes);
        }

        // Executar query
        ResultSet rs = stmt.executeQuery();

        // ========== PROCESSAR RESULTADOS ==========
        // criarVendaDoResultSet() monta objeto Vendas completo
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
    /**
     * Conta o número total de vendas do mês atual para um usuário
     * CORRIGIDO: usa usuario_id (consistente com calcularTotalMes)
     * 
     * @param idUsuario ID do usuário
     * @return Quantidade de vendas no mês atual
     * @throws Exception Se houver erro na consulta
     */
    public int contarVendasDoMes(int idUsuario) throws Exception {
        Calendar cal = Calendar.getInstance();
        int mesAtual = cal.get(Calendar.MONTH) + 1;
        int anoAtual = cal.get(Calendar.YEAR);
        
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM vendas " +
                     "WHERE usuario_id = ? AND ativo = true " +
                     "AND MONTH(data_vendas) = ? " +
                     "AND YEAR(data_vendas) = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, mesAtual);
            stmt.setInt(3, anoAtual);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar vendas do mês:");
            System.err.println("   Erro: " + e.getMessage());
            throw new Exception("Erro ao contar vendas do mês: " + e.getMessage(), e);
        }
    }
    
    /**
     * Conta o número total de vendas do ano atual para um usuário
     * 
     * @param idUsuario ID do usuário
     * @return Quantidade de vendas no ano atual
     * @throws Exception Se houver erro na consulta
     */
    public int contarVendasDoAno(int idUsuario) throws Exception {
        Calendar cal = Calendar.getInstance();
        int anoAtual = cal.get(Calendar.YEAR);
        
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM vendas " +
                     "WHERE usuario_id = ? AND ativo = true " +
                     "AND YEAR(data_vendas) = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, anoAtual);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar vendas do ano:");
            System.err.println("   Erro: " + e.getMessage());
            throw new Exception("Erro ao contar vendas do ano: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calcula a soma total das vendas do ano atual
     * 
     * @param idUsuario ID do usuário
     * @return Valor total das vendas do ano
     * @throws Exception Se houver erro na consulta
     */
    public double calcularTotalAno(int idUsuario) throws Exception {
        Calendar cal = Calendar.getInstance();
        int anoAtual = cal.get(Calendar.YEAR);
        
        String sql = "SELECT COALESCE(SUM(valor), 0) AS total " +
                     "FROM vendas " +
                     "WHERE usuario_id = ? AND ativo = true " +
                     "AND YEAR(data_vendas) = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, anoAtual);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
                return 0.0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao calcular total do ano:");
            System.err.println("   Erro: " + e.getMessage());
            throw new Exception("Erro ao calcular total do ano: " + e.getMessage(), e);
        }
    }
    
    /**
     * Conta TODAS as vendas do usuário (sem filtro de data)
     * Útil para estatísticas gerais
     * 
     * @param idUsuario ID do usuário
     * @return Quantidade total de vendas
     * @throws Exception Se houver erro na consulta
     */
    public int contarTodasVendas(int idUsuario) throws Exception {
        String sql = "SELECT COUNT(*) AS total " +
                     "FROM vendas " +
                     "WHERE usuario_id = ? AND ativo = true";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao contar todas as vendas:");
            System.err.println("   Erro: " + e.getMessage());
            throw new Exception("Erro ao contar vendas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calcula o total de TODAS as vendas (sem filtro de data)
     * Útil para estatísticas gerais
     * 
     * @param idUsuario ID do usuário
     * @return Valor total de todas as vendas
     * @throws Exception Se houver erro na consulta
     */
    public double calcularTotalGeral(int idUsuario) throws Exception {
        String sql = "SELECT COALESCE(SUM(valor), 0) AS total " +
                     "FROM vendas " +
                     "WHERE usuario_id = ? AND ativo = true";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
                return 0.0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao calcular total geral:");
            System.err.println("   Erro: " + e.getMessage());
            throw new Exception("Erro ao calcular total: " + e.getMessage(), e);
        }
    }
}