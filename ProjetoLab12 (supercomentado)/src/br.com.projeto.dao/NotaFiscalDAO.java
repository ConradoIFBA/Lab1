package br.com.projeto.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.NotaFiscal;

/**
 * ================================================================
 * DAO: NOTA FISCAL
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia operações de banco de dados para notas fiscais.
 * Cada venda pode ter uma nota fiscal associada.
 *
 * MÉTODOS:
 * - inserir(NotaFiscal)         → Cadastra nova nota
 * - listar()                    → Lista todas as notas
 * - listarPorUsuario(int)       → Notas de um usuário
 * - editar(NotaFiscal)          → Atualiza nota existente
 * - excluir(int)                → Exclusão lógica (soft delete)
 * - buscar(int)                 → Por ID
 * - buscarPorVenda(int)         → Nota de uma venda específica
 *
 * TABELA:
 * Nome: nota_fiscal
 * Colunas:
 * - id_nota_fiscal  INT PRIMARY KEY AUTO_INCREMENT
 * - numero          VARCHAR(50) NOT NULL UNIQUE
 * - data_emissao    DATETIME NOT NULL
 * - valor           FLOAT NOT NULL
 * - vendas_id       INT NOT NULL (FK → vendas)
 * - usuario_id      INT NOT NULL (FK → usuario)
 * - ativo           BOOLEAN DEFAULT TRUE
 *
 * RELACIONAMENTOS:
 * - nota_fiscal.vendas_id  → vendas.id_vendas  (1:1)
 * - nota_fiscal.usuario_id → usuario.id_usuario (N:1)
 *
 * SOFT DELETE:
 * Notas "excluídas" ficam ativo=false mas permanecem no banco.
 *
 * FORMATO DO NÚMERO:
 * Pode ser NF-e (44 dígitos), série/número, ou formato customizado.
 * Ex: "35210212345678000190550010000001231000000123"
 * Ex: "001/2024"
 *
 * SEGURANÇA:
 * - PreparedStatement em TODOS os métodos
 * - Conversão flexível de datas
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class NotaFiscalDAO {

    /* ================================================================
       ATRIBUTO - Conexão com banco
       ================================================================ */

    /**
     * Conexão com banco de dados.
     * Passada no construtor, não criada aqui.
     */
    private Connection conexao;

    /* ================================================================
       CONSTRUTOR
       ================================================================ */

    /**
     * Construtor do DAO.
     *
     * @param conexao Conexão ativa com banco
     *
     * Exemplo de uso:
     * ```java
     * try (Connection conn = Conexao.getConnection()) {
     *     NotaFiscalDAO dao = new NotaFiscalDAO(conn);
     *     List<NotaFiscal> notas = dao.listar();
     * }
     * ```
     */
    public NotaFiscalDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO 1: INSERIR - Nova nota fiscal
       ================================================================

       Usado por: Cadastro de venda com nota

       Cadastra uma nova nota fiscal associada a uma venda.
       Retorna o ID gerado no próprio objeto.

       IMPORTANTE:
       - Número deve ser único
       - Data de emissão é convertida automaticamente
       - Valor normalmente = valor da venda
       - vendas_id e usuario_id obrigatórios
    */

    /**
     * Insere nova nota fiscal.
     *
     * @param notaFiscal Objeto NotaFiscal com dados
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf = new NotaFiscal();
     * nf.setNumero("001/2024");
     * nf.setDataEmissao(LocalDateTime.now());
     * nf.setValor(150.50f);
     * nf.setVendasId(10);      // ID da venda
     * nf.setUsuarioId(1);      // ID do usuário MEI
     *
     * notaFiscalDAO.inserir(nf);
     * System.out.println("ID gerado: " + nf.getIdNotaFiscal());
     * ```
     *
     * CONVERSÃO DE DATA:
     * Aceita múltiplos tipos:
     * - java.util.Date
     * - LocalDateTime
     * - String (formato SQL: "2024-01-15 10:30:00")
     * - Se inválido, usa data/hora atual
     */
    public void inserir(NotaFiscal notaFiscal) throws Exception {

        // ========== SQL COM COLUNAS SNAKE_CASE ==========
        String sql = "INSERT INTO nota_fiscal " +
                "(numero, data_emissao, valor, vendas_id, usuario_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement stmt = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);

        try {
            // ========== SETAR PARÂMETRO 1: NÚMERO ==========
            stmt.setString(1, notaFiscal.getNumero());

            // ========== SETAR PARÂMETRO 2: DATA EMISSÃO ==========
            // Converte Object para Timestamp de forma segura
            Object dataEmissao = notaFiscal.getDataEmissao();

            if (dataEmissao instanceof java.util.Date) {
                // Se é Date, converte para Timestamp
                stmt.setTimestamp(2, new Timestamp(
                        ((java.util.Date)dataEmissao).getTime()));

            } else if (dataEmissao instanceof LocalDateTime) {
                // Se é LocalDateTime, converte para Timestamp
                stmt.setTimestamp(2, Timestamp.valueOf(
                        (LocalDateTime)dataEmissao));

            } else if (dataEmissao instanceof String) {
                // Se é String, tenta parsear
                stmt.setTimestamp(2, Timestamp.valueOf(
                        (String)dataEmissao));

            } else {
                // Fallback: usa data/hora atual
                stmt.setTimestamp(2, new Timestamp(
                        System.currentTimeMillis()));
            }

            // ========== SETAR PARÂMETROS 3-5 ==========
            stmt.setFloat(3, notaFiscal.getValor());
            stmt.setInt(4, notaFiscal.getVendasId());
            stmt.setInt(5, notaFiscal.getUsuarioId());

            // ========== EXECUTAR INSERT ==========
            stmt.executeUpdate();

            // ========== RECUPERAR ID GERADO ==========
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                notaFiscal.setIdNotaFiscal(rs.getInt(1));
            }
            rs.close();

        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 2: LISTAR - Todas as notas
       ================================================================

       Usado por: Relatórios, listagem geral

       Retorna todas as notas ativas, ordenadas da mais recente
       para a mais antiga.
    */

    /**
     * Lista todas as notas fiscais ATIVAS.
     *
     * @return Lista de notas (vazia se nenhuma)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<NotaFiscal> notas = notaFiscalDAO.listar();
     * for (NotaFiscal nf : notas) {
     *     System.out.println(nf.getNumero() + " - R$ " + nf.getValor());
     * }
     * ```
     *
     * Ordenação: Mais recentes primeiro (data_emissao DESC)
     */
    public List<NotaFiscal> listar() throws Exception {

        List<NotaFiscal> notas = new ArrayList<>();

        // ========== SQL: APENAS ATIVAS ==========
        String sql = "SELECT * FROM nota_fiscal " +
                "WHERE ativo = true " +
                "ORDER BY data_emissao DESC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        try {
            // ========== PROCESSAR RESULTADOS ==========
            while (rs.next()) {
                // Construtor: NotaFiscal(id, numero, dataEmissao, valor)
                NotaFiscal nf = new NotaFiscal(
                        rs.getInt("id_nota_fiscal"),
                        rs.getString("numero"),
                        rs.getTimestamp("data_emissao").toLocalDateTime(),
                        rs.getFloat("valor")
                );

                // Setar IDs de relacionamento
                nf.setVendasId(rs.getInt("vendas_id"));
                nf.setUsuarioId(rs.getInt("usuario_id"));

                notas.add(nf);
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return notas;
    }

    /* ================================================================
       MÉTODO 3: LISTAR POR USUARIO - Notas de um MEI
       ================================================================

       Usado por: Dashboard do usuário, relatórios pessoais

       Retorna apenas notas de um usuário específico.
       Útil para MEI ver apenas suas próprias notas.
    */

    /**
     * Lista notas fiscais de um usuário específico.
     *
     * @param usuarioId ID do usuário
     * @return Lista de notas do usuário (vazia se nenhuma)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * // Notas do usuário logado
     * Usuario usuarioLogado = (Usuario) session.getAttribute("usuario");
     * List<NotaFiscal> minhasNotas = notaFiscalDAO.listarPorUsuario(
     *     usuarioLogado.getIdUsuario()
     * );
     *
     * for (NotaFiscal nf : minhasNotas) {
     *     System.out.println("Minha nota: " + nf.getNumero());
     * }
     * ```
     */
    public List<NotaFiscal> listarPorUsuario(int usuarioId) throws Exception {

        List<NotaFiscal> notas = new ArrayList<>();

        // ========== SQL COM FILTRO DE USUARIO ==========
        String sql = "SELECT * FROM nota_fiscal " +
                "WHERE usuario_id = ? AND ativo = true " +
                "ORDER BY data_emissao DESC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, usuarioId);
        ResultSet rs = stmt.executeQuery();

        try {
            // ========== PROCESSAR RESULTADOS ==========
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
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return notas;
    }

    /* ================================================================
       MÉTODO 4: EDITAR - Atualizar nota
       ================================================================

       Usado por: Correção de dados, atualização

       Atualiza número, data e valor de uma nota existente.
       Não altera vendas_id nem usuario_id (relacionamentos fixos).
    */

    /**
     * Edita dados de uma nota fiscal.
     *
     * @param notaFiscal Objeto NotaFiscal com dados atualizados
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf = notaFiscalDAO.buscar(1);
     * nf.setNumero("002/2024");  // Correção de número
     * nf.setValor(200.00f);      // Correção de valor
     *
     * notaFiscalDAO.editar(nf);
     * System.out.println("Nota atualizada!");
     * ```
     *
     * IMPORTANTE:
     * - Não altera vendas_id (nota sempre da mesma venda)
     * - Não altera usuario_id (nota sempre do mesmo MEI)
     */
    public void editar(NotaFiscal notaFiscal) throws Exception {

        // ========== SQL COM PARÂMETROS ==========
        String sql = "UPDATE nota_fiscal " +
                "SET numero = ?, data_emissao = ?, valor = ? " +
                "WHERE id_nota_fiscal = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        try {
            // ========== SETAR PARÂMETRO 1: NÚMERO ==========
            stmt.setString(1, notaFiscal.getNumero());

            // ========== SETAR PARÂMETRO 2: DATA EMISSÃO ==========
            // Mesma lógica de conversão do inserir()
            Object dataEmissao = notaFiscal.getDataEmissao();

            if (dataEmissao instanceof java.util.Date) {
                stmt.setTimestamp(2, new Timestamp(
                        ((java.util.Date)dataEmissao).getTime()));
            } else if (dataEmissao instanceof LocalDateTime) {
                stmt.setTimestamp(2, Timestamp.valueOf(
                        (LocalDateTime)dataEmissao));
            } else if (dataEmissao instanceof String) {
                stmt.setTimestamp(2, Timestamp.valueOf(
                        (String)dataEmissao));
            } else {
                stmt.setTimestamp(2, new Timestamp(
                        System.currentTimeMillis()));
            }

            // ========== SETAR PARÂMETROS 3-4 ==========
            stmt.setFloat(3, notaFiscal.getValor());
            stmt.setInt(4, notaFiscal.getIdNotaFiscal());

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();

        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 5: EXCLUIR - Exclusão lógica (SOFT DELETE)
       ================================================================

       Usado por: Cancelamento de nota

       NÃO remove fisicamente!
       Marca ativo = false.
       Nota não aparece em listagens mas permanece no banco.
    */

    /**
     * Exclui nota LOGICAMENTE (soft delete).
     *
     * Marca ativo = false sem remover do banco.
     * Preserva histórico e integridade referencial.
     *
     * @param id ID da nota a excluir
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * notaFiscalDAO.excluir(1);
     * // Nota fica ativo=false
     * // Não aparece em listar()
     * // Mas permanece no banco para histórico
     * ```
     */
    public void excluir(int id) throws Exception {

        // ========== SQL: UPDATE ativo = false ==========
        String sql = "UPDATE nota_fiscal " +
                "SET ativo = false " +
                "WHERE id_nota_fiscal = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        try {
            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, id);

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();
        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 6: BUSCAR - Por ID
       ================================================================

       Usado por: Edição, visualização

       Busca uma nota específica por seu ID.
       Retorna null se não encontrar ou se estiver inativa.
    */

    /**
     * Busca nota fiscal por ID.
     *
     * @param id ID da nota
     * @return NotaFiscal ou null se não encontrada/inativa
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf = notaFiscalDAO.buscar(1);
     * if (nf != null) {
     *     System.out.println("Nota: " + nf.getNumero());
     *     System.out.println("Valor: R$ " + nf.getValor());
     * } else {
     *     System.out.println("Nota não encontrada");
     * }
     * ```
     */
    public NotaFiscal buscar(int id) throws Exception {

        // ========== SQL COM FILTROS ==========
        String sql = "SELECT * FROM nota_fiscal " +
                "WHERE id_nota_fiscal = ? AND ativo = true";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        NotaFiscal nf = null;

        try {
            // ========== PROCESSAR RESULTADO ==========
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
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return nf;
    }

    /* ================================================================
       MÉTODO 7: BUSCAR POR VENDA - Nota de uma venda
       ================================================================

       Usado por: Detalhes da venda

       Busca a nota fiscal associada a uma venda específica.
       Retorna null se a venda não tem nota ou se está inativa.
    */

    /**
     * Busca nota fiscal de uma venda específica.
     *
     * @param vendasId ID da venda
     * @return NotaFiscal ou null se não encontrada
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * // Buscar nota da venda ID 10
     * NotaFiscal nf = notaFiscalDAO.buscarPorVenda(10);
     * if (nf != null) {
     *     System.out.println("Venda tem nota: " + nf.getNumero());
     * } else {
     *     System.out.println("Venda sem nota fiscal");
     * }
     * ```
     *
     * USO EM CONTROLLER:
     * ```java
     * Vendas venda = vendasDAO.buscar(10);
     * NotaFiscal nota = notaFiscalDAO.buscarPorVenda(venda.getIdVendas());
     *
     * if (nota != null) {
     *     venda.setNotaFiscal(nota);
     * }
     * ```
     */
    public NotaFiscal buscarPorVenda(int vendasId) throws Exception {

        // ========== SQL COM FILTRO DE VENDA ==========
        String sql = "SELECT * FROM nota_fiscal " +
                "WHERE vendas_id = ? AND ativo = true";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, vendasId);
        ResultSet rs = stmt.executeQuery();

        NotaFiscal nf = null;

        try {
            // ========== PROCESSAR RESULTADO ==========
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
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return nf;
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   MÉTODOS:
   1. inserir(NotaFiscal)       → Nova nota
   2. listar()                  → Todas ativas
   3. listarPorUsuario(int)     → Por MEI
   4. editar(NotaFiscal)        → Atualizar dados
   5. excluir(int)              → Soft delete
   6. buscar(int)               → Por ID
   7. buscarPorVenda(int)       → Nota de uma venda

   RELACIONAMENTOS:
   - nota_fiscal ↔ vendas  (1:1)
   - nota_fiscal ↔ usuario (N:1)

   CONVERSÃO DE DATA:
   Aceita múltiplos formatos:
   - java.util.Date
   - LocalDateTime (recomendado)
   - String (formato SQL)
   - Fallback: data/hora atual

   SOFT DELETE:
   ✅ excluir() marca ativo=false
   ✅ Preserva histórico fiscal
   ✅ Mantém conformidade legal

   EXEMPLO COMPLETO:
   ```java
   try (Connection conn = Conexao.getConnection()) {
       NotaFiscalDAO dao = new NotaFiscalDAO(conn);

       // Nova nota
       NotaFiscal nf = new NotaFiscal();
       nf.setNumero("001/2024");
       nf.setDataEmissao(LocalDateTime.now());
       nf.setValor(250.00f);
       nf.setVendasId(10);
       nf.setUsuarioId(1);
       dao.inserir(nf);

       // Buscar nota da venda
       NotaFiscal notaDaVenda = dao.buscarPorVenda(10);

       // Listar minhas notas
       List<NotaFiscal> minhas = dao.listarPorUsuario(1);
   }
   ```

   SEGURANÇA:
   ✅ PreparedStatement
   ✅ Conversão segura de datas
   ✅ Soft delete para auditoria

   OBSERVAÇÕES:
   - Número deve ser único (constraint no banco)
   - Data conversão flexível para compatibilidade
   - Relacionamentos obrigatórios (vendas_id, usuario_id)
   - Soft delete por questões fiscais/legais
   ================================================================ */