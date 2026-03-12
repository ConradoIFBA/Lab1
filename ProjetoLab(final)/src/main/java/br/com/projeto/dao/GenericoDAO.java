package br.com.projeto.dao;

import java.sql.*;

/**
 * ================================================================
 * DAO: GENERICO - Classe Utilitária
 * ================================================================
 *
 * PROPÓSITO:
 * Fornece métodos genéricos para executar comandos SQL simples.
 * Útil para operações que não justificam criar um DAO específico.
 *
 * MÉTODOS:
 * - executarComando(sql, params)  → INSERT, UPDATE, DELETE genéricos
 * - executarConsulta(sql, params) → SELECT genéricos
 * - fecharConexao()               → Fecha conexão
 *
 * QUANDO USAR:
 * ✅ Queries rápidas e pontuais
 * ✅ Scripts de manutenção
 * ✅ Operações que não justificam DAO próprio
 *
 * QUANDO NÃO USAR:
 * ❌ Operações complexas com lógica de negócio
 * ❌ Queries frequentes (crie DAO específico!)
 * ❌ Como substituto de DAOs especializados
 *
 * SEGURANÇA:
 * ✅ Usa PreparedStatement (anti SQL injection)
 * ✅ Parâmetros variáveis (varargs)
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class GenericoDAO {

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
     * Construtor do DAO genérico.
     *
     * @param conexao Conexão ativa com banco
     *
     * Exemplo de uso:
     * ```java
     * try (Connection conn = Conexao.getConnection()) {
     *     GenericoDAO dao = new GenericoDAO(conn);
     *
     *     // Executar comando genérico
     *     String sql = "UPDATE vendas SET processado = ? WHERE id_vendas = ?";
     *     dao.executarComando(sql, true, 10);
     * }
     * ```
     */
    public GenericoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO: GET CONEXAO - Acesso à conexão
       ================================================================ */

    /**
     * Retorna a conexão para uso externo.
     *
     * Útil quando você precisa passar a conexão para outro DAO
     * ou executar operações mais complexas.
     *
     * @return Conexão ativa com banco
     *
     * Exemplo:
     * ```java
     * GenericoDAO genericoDAO = new GenericoDAO(conn);
     * Connection mesmaConexao = genericoDAO.getConexao();
     *
     * // Usar em outro DAO
     * UsuarioDAO usuarioDAO = new UsuarioDAO(mesmaConexao);
     * ```
     */
    public Connection getConexao() {
        return conexao;
    }

    /* ================================================================
       MÉTODO 1: EXECUTAR COMANDO - INSERT, UPDATE, DELETE genéricos
       ================================================================

       Executa comandos que MODIFICAM dados (não retornam ResultSet).

       USADO PARA:
       - INSERT: Inserir dados
       - UPDATE: Atualizar dados
       - DELETE: Remover dados

       RETORNA:
       Número de linhas afetadas pela operação.
    */

    /**
     * Executa comando genérico (INSERT, UPDATE, DELETE).
     *
     * @param sql       Query SQL com ? para parâmetros
     * @param parametros Valores dos parâmetros (ordem do SQL)
     * @return Número de linhas afetadas
     * @throws SQLException Se erro no banco
     *
     * EXEMPLOS DE USO:
     *
     * 1. INSERT simples:
     * ```java
     * String sql = "INSERT INTO log (mensagem, data) VALUES (?, ?)";
     * int linhas = dao.executarComando(sql, "Login realizado", new Timestamp(System.currentTimeMillis()));
     * System.out.println("Linhas inseridas: " + linhas);
     * ```
     *
     * 2. UPDATE com múltiplos parâmetros:
     * ```java
     * String sql = "UPDATE vendas SET status = ?, data_processamento = ? WHERE id_vendas = ?";
     * int linhas = dao.executarComando(sql, "Processado", new Timestamp(System.currentTimeMillis()), 10);
     * System.out.println("Linhas atualizadas: " + linhas);
     * ```
     *
     * 3. DELETE condicional:
     * ```java
     * String sql = "DELETE FROM sessao WHERE data_expiracao < ?";
     * int linhas = dao.executarComando(sql, new Timestamp(System.currentTimeMillis()));
     * System.out.println("Sessões expiradas removidas: " + linhas);
     * ```
     *
     * SEGURANÇA:
     * PreparedStatement previne SQL injection:
     * - Input malicioso: "'; DROP TABLE vendas; --"
     * - Resultado: Tratado como string literal (seguro!)
     */
    public int executarComando(String sql, Object... parametros) throws SQLException {

        // ========== TRY-WITH-RESOURCES ==========
        // PreparedStatement fecha automaticamente
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETROS ==========
            // Loop pelos parâmetros variáveis (varargs)
            for (int i = 0; i < parametros.length; i++) {
                // setObject detecta o tipo automaticamente
                stmt.setObject(i + 1, parametros[i]);
            }

            // ========== EXECUTAR COMANDO ==========
            // executeUpdate retorna número de linhas afetadas
            return stmt.executeUpdate();
        }
    }

    /* ================================================================
       MÉTODO 2: EXECUTAR CONSULTA - SELECT genérico
       ================================================================

       Executa consultas que RETORNAM dados (ResultSet).

       USADO PARA:
       - SELECT: Buscar dados

       ⚠️ IMPORTANTE:
       O ResultSet retornado DEVE ser fechado manualmente!
       Ou use try-with-resources no código que chamar.
    */

    /**
     * Executa consulta genérica (SELECT).
     *
     * ⚠️ ATENÇÃO: O ResultSet retornado DEVE ser fechado pelo caller!
     *
     * @param sql       Query SQL com ? para parâmetros
     * @param parametros Valores dos parâmetros (ordem do SQL)
     * @return ResultSet com resultados (DEVE ser fechado!)
     * @throws SQLException Se erro no banco
     *
     * EXEMPLOS DE USO:
     *
     * 1. SELECT simples:
     * ```java
     * String sql = "SELECT * FROM vendas WHERE usuario_id = ?";
     * ResultSet rs = dao.executarConsulta(sql, 1);
     *
     * while (rs.next()) {
     *     System.out.println("Venda: " + rs.getInt("id_vendas"));
     * }
     *
     * rs.close(); // ⚠️ IMPORTANTE: Fechar manualmente!
     * ```
     *
     * 2. SELECT com múltiplos parâmetros:
     * ```java
     * String sql = "SELECT * FROM vendas WHERE usuario_id = ? AND data_vendas > ?";
     * ResultSet rs = dao.executarConsulta(sql, 1, new Timestamp(System.currentTimeMillis()));
     *
     * try {
     *     while (rs.next()) {
     *         // processar...
     *     }
     * } finally {
     *     rs.close(); // ⚠️ Sempre fechar!
     * }
     * ```
     *
     * 3. COM try-with-resources (RECOMENDADO):
     * ```java
     * String sql = "SELECT * FROM vendas WHERE usuario_id = ?";
     * try (ResultSet rs = dao.executarConsulta(sql, 1)) {
     *     while (rs.next()) {
     *         // processar...
     *     }
     *     // rs fecha automaticamente aqui
     * }
     * ```
     *
     * SEGURANÇA:
     * PreparedStatement previne SQL injection mesmo em consultas.
     */
    public ResultSet executarConsulta(String sql, Object... parametros) throws SQLException {

        // ========== CRIAR PREPAREDSTATEMENT ==========
        // NÃO usar try-with-resources aqui!
        // Statement deve permanecer aberto enquanto ResultSet está em uso
        PreparedStatement stmt = conexao.prepareStatement(sql);

        // ========== SETAR PARÂMETROS ==========
        for (int i = 0; i < parametros.length; i++) {
            stmt.setObject(i + 1, parametros[i]);
        }

        // ========== EXECUTAR QUERY E RETORNAR ==========
        // ⚠️ ATENÇÃO:
        // Quem chamar este método DEVE fechar o ResultSet!
        // Quando o ResultSet fechar, o PreparedStatement também fecha.
        return stmt.executeQuery();
    }

    /* ================================================================
       MÉTODO 3: FECHAR CONEXAO - Encerra conexão
       ================================================================

       Fecha a conexão com banco.

       ⚠️ CUIDADO:
       - Só use se você gerencia a conexão manualmente
       - Não use se está usando try-with-resources
       - Não feche se outros DAOs usam a mesma conexão!
    */

    /**
     * Fecha a conexão com banco de dados.
     *
     * ⚠️ USE COM CUIDADO:
     * - Apenas se você gerencia conexão manualmente
     * - Não use com try-with-resources (fecha automaticamente)
     * - Não feche se outros objetos usam esta conexão!
     *
     * Exemplo de uso CORRETO:
     * ```java
     * Connection conn = Conexao.getConnection();
     * GenericoDAO dao = new GenericoDAO(conn);
     *
     * try {
     *     dao.executarComando("INSERT INTO log VALUES (?)", "teste");
     * } finally {
     *     dao.fecharConexao(); // Fecha manualmente
     * }
     * ```
     *
     * PREFERÍVEL (try-with-resources):
     * ```java
     * try (Connection conn = Conexao.getConnection()) {
     *     GenericoDAO dao = new GenericoDAO(conn);
     *     dao.executarComando("INSERT INTO log VALUES (?)", "teste");
     *     // Conexão fecha automaticamente, NÃO chamar fecharConexao()
     * }
     * ```
     */
    public void fecharConexao() {
        try {
            // ========== VERIFICAR SE JÁ ESTÁ FECHADA ==========
            if (conexao != null && !conexao.isClosed()) {
                // ========== FECHAR CONEXÃO ==========
                conexao.close();
            }
        } catch (SQLException e) {
            // ========== LOG DE ERRO ==========
            e.printStackTrace();
        }
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   PROPÓSITO:
   Classe utilitária para operações SQL genéricas.

   MÉTODOS:
   1. executarComando(sql, params)  → INSERT/UPDATE/DELETE
   2. executarConsulta(sql, params) → SELECT (retorna ResultSet)
   3. fecharConexao()               → Fecha conexão

   QUANDO USAR:
   ✅ Operações SQL pontuais e simples
   ✅ Scripts de manutenção
   ✅ Testes rápidos
   ✅ Operações que não justificam DAO próprio

   QUANDO NÃO USAR:
   ❌ Como substituto de DAO especializado
   ❌ Para operações complexas com lógica de negócio
   ❌ Para queries executadas frequentemente

   SEGURANÇA:
   ✅ PreparedStatement (anti SQL injection)
   ✅ Parâmetros tipados
   ✅ setObject() detecta tipo automaticamente

   EXEMPLO COMPLETO:
   ```java
   try (Connection conn = Conexao.getConnection()) {
       GenericoDAO dao = new GenericoDAO(conn);

       // INSERT
       String sqlInsert = "INSERT INTO log (mensagem) VALUES (?)";
       int inseridas = dao.executarComando(sqlInsert, "Sistema iniciado");
       System.out.println("Linhas inseridas: " + inseridas);

       // UPDATE
       String sqlUpdate = "UPDATE vendas SET processado = ? WHERE id_vendas = ?";
       int atualizadas = dao.executarComando(sqlUpdate, true, 10);
       System.out.println("Linhas atualizadas: " + atualizadas);

       // SELECT
       String sqlSelect = "SELECT * FROM vendas WHERE usuario_id = ?";
       try (ResultSet rs = dao.executarConsulta(sqlSelect, 1)) {
           while (rs.next()) {
               System.out.println("Venda: " + rs.getInt("id_vendas"));
           }
       }

       // Conexão fecha automaticamente (try-with-resources)
   }
   ```

   VARARGS (Object... parametros):
   Permite passar quantidade variável de parâmetros:
   - dao.executarComando(sql)              → 0 params
   - dao.executarComando(sql, 1)           → 1 param
   - dao.executarComando(sql, 1, "texto")  → 2 params
   - dao.executarComando(sql, 1, 2, 3, 4)  → 4 params

   OBSERVAÇÕES:
   - Classe utilitária simples
   - Não fecha conexão automaticamente
   - ResultSet de executarConsulta() DEVE ser fechado pelo caller
   - PreparedStatement em executarConsulta() fecha quando ResultSet fecha
   ================================================================ */