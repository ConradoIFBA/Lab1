package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Categoria;

/**
 * ================================================================
 * DAO: CATEGORIA
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia operações de banco de dados para categorias de vendas.
 *
 * MÉTODOS:
 * - listar()                    → Lista categorias ativas
 * - listarTodas()               → Lista todas (ativas + inativas)
 * - buscarPorId(int)            → Busca categoria específica
 * - inserir(Categoria)          → Cadastra nova categoria
 * - atualizar(Categoria)        → Atualiza categoria existente
 * - deletar(int)                → Exclusão lógica (soft delete)
 * - deletarPermanentemente(int) → Exclusão física (hard delete)
 *
 * TABELA:
 * Nome: categoria
 * Colunas:
 * - id_categoria   INT PRIMARY KEY AUTO_INCREMENT
 * - nome_categoria VARCHAR(100) NOT NULL
 * - ativo          BOOLEAN DEFAULT TRUE
 *
 * SOFT DELETE:
 * Este DAO usa exclusão lógica (ativo = 0/1).
 * Categorias "excluídas" ficam ativo=0 mas permanecem no banco.
 *
 * SEGURANÇA:
 * - PreparedStatement em TODOS os métodos (anti SQL injection)
 * - Try-with-resources para fechar recursos automaticamente
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class CategoriaDAO {

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
     *     CategoriaDAO dao = new CategoriaDAO(conn);
     *     List<Categoria> categorias = dao.listar();
     * }
     * ```
     */
    public CategoriaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO 1: LISTAR - Apenas categorias ativas
       ================================================================

       Usado por: Formulários de venda (dropdown)

       Retorna apenas categorias com ativo = 1.
       Útil para exibir opções válidas ao usuário.

       Ordenação: Por nome (alfabética)
    */

    /**
     * Lista todas as categorias ATIVAS.
     *
     * @return Lista de categorias ativas (vazia se nenhuma)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<Categoria> categorias = categoriaDAO.listar();
     * for (Categoria c : categorias) {
     *     System.out.println(c.getNomeCategoria());
     * }
     * ```
     *
     * SQL executado:
     * SELECT id_categoria, nome_categoria, ativo
     * FROM categoria
     * WHERE ativo = 1
     * ORDER BY nome_categoria
     */
    public List<Categoria> listar() throws Exception {
        List<Categoria> categorias = new ArrayList<>();

        // ========== SQL: APENAS ATIVAS ==========
        String sql = "SELECT id_categoria, nome_categoria, ativo " +
                     "FROM categoria " +
                     "WHERE ativo = 1 " +
                     "ORDER BY nome_categoria";

        // ========== TRY-WITH-RESOURCES ==========
        // Fecha stmt e rs automaticamente
        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // ========== PROCESSAR RESULTADOS ==========
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

    /* ================================================================
       MÉTODO 2: LISTAR TODAS - Ativas + Inativas
       ================================================================

       Usado por: Telas de administração

       Retorna TODAS as categorias, incluindo inativas.
       Útil para gerenciamento e relatórios.

       Ordenação: Por nome (alfabética)
    */

    /**
     * Lista TODAS as categorias (ativas + inativas).
     *
     * @return Lista de todas as categorias
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<Categoria> todas = categoriaDAO.listarTodas();
     * for (Categoria c : todas) {
     *     String status = c.isAtivo() ? "Ativa" : "Inativa";
     *     System.out.println(c.getNomeCategoria() + " - " + status);
     * }
     * ```
     *
     * SQL executado:
     * SELECT id_categoria, nome_categoria, ativo
     * FROM categoria
     * ORDER BY nome_categoria
     */
    public List<Categoria> listarTodas() throws Exception {
        List<Categoria> categorias = new ArrayList<>();

        // ========== SQL: TODAS (SEM WHERE) ==========
        String sql = "SELECT id_categoria, nome_categoria, ativo " +
                     "FROM categoria " +
                     "ORDER BY nome_categoria";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // ========== PROCESSAR RESULTADOS ==========
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

    /* ================================================================
       MÉTODO 3: BUSCAR POR ID - Categoria específica
       ================================================================

       Usado por: Edição, visualização

       Busca uma categoria por seu ID.
       Retorna null se não encontrar.
    */

    /**
     * Busca categoria por ID.
     *
     * @param id ID da categoria
     * @return Categoria ou null se não encontrada
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * Categoria cat = categoriaDAO.buscarPorId(1);
     * if (cat != null) {
     *     System.out.println("Encontrada: " + cat.getNomeCategoria());
     * } else {
     *     System.out.println("Categoria não encontrada");
     * }
     * ```
     *
     * SEGURANÇA:
     * Usa PreparedStatement com parâmetro (? previne SQL injection)
     */
    public Categoria buscarPorId(int id) throws Exception {
        Categoria categoria = null;

        // ========== SQL COM PARÂMETRO ==========
        String sql = "SELECT id_categoria, nome_categoria, ativo " +
                     "FROM categoria " +
                     "WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                // ========== PROCESSAR RESULTADO ==========
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

    /* ================================================================
       MÉTODO 4: INSERIR - Nova categoria
       ================================================================

       Usado por: Formulário de cadastro de categoria

       Insere nova categoria no banco.
       Retorna o ID gerado no próprio objeto.

       IMPORTANTE:
       - Nome é obrigatório
       - Ativo padrão = true
    */

    /**
     * Insere nova categoria no banco.
     *
     * @param categoria Objeto Categoria com dados
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria();
     * cat.setNomeCategoria("Eletrônicos");
     * cat.setAtivo(true);
     *
     * categoriaDAO.inserir(cat);
     * System.out.println("ID gerado: " + cat.getIdCategoria());
     * ```
     *
     * SEGURANÇA:
     * PreparedStatement previne SQL injection mesmo com strings maliciosas:
     * - Input: "'; DROP TABLE categoria; --"
     * - Result: Inserido literalmente como nome (seguro!)
     */
    public void inserir(Categoria categoria) throws Exception {

        // ========== SQL COM PARÂMETROS ==========
        String sql = "INSERT INTO categoria (nome_categoria, ativo) " +
                     "VALUES (?, ?)";

        // ========== RETURN_GENERATED_KEYS ==========
        // Permite pegar o ID auto_increment gerado
        try (PreparedStatement stmt = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            // ========== SETAR PARÂMETROS ==========
            stmt.setString(1, categoria.getNomeCategoria());
            stmt.setBoolean(2, categoria.isAtivo());

            // ========== EXECUTAR INSERT ==========
            stmt.executeUpdate();

            // ========== RECUPERAR ID GERADO ==========
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    categoria.setIdCategoria(rs.getInt(1));
                }
            }
        }
    }

    /* ================================================================
       MÉTODO 5: ATUALIZAR - Editar categoria
       ================================================================

       Usado por: Formulário de edição de categoria

       Atualiza nome e status (ativo) de uma categoria existente.
       Usa o ID do objeto para localizar o registro.
    */

    /**
     * Atualiza dados de uma categoria.
     *
     * @param categoria Objeto Categoria com dados atualizados
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * Categoria cat = categoriaDAO.buscarPorId(1);
     * cat.setNomeCategoria("Eletrônicos e Games");
     * cat.setAtivo(true);
     *
     * categoriaDAO.atualizar(cat);
     * System.out.println("Categoria atualizada!");
     * ```
     *
     * IMPORTANTE:
     * O ID da categoria NÃO é alterado (usado no WHERE).
     */
    public void atualizar(Categoria categoria) throws Exception {

        // ========== SQL COM PARÂMETROS ==========
        String sql = "UPDATE categoria " +
                     "SET nome_categoria = ?, ativo = ? " +
                     "WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETROS ==========
            stmt.setString(1, categoria.getNomeCategoria());
            stmt.setBoolean(2, categoria.isAtivo());
            stmt.setInt(3, categoria.getIdCategoria());

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();
        }
    }

    /* ================================================================
       MÉTODO 6: DELETAR - Exclusão lógica (SOFT DELETE)
       ================================================================

       Usado por: Ação de excluir categoria

       NÃO remove fisicamente do banco!
       Apenas marca ativo = 0 (false).

       VANTAGENS DO SOFT DELETE:
       - Mantém histórico
       - Preserva integridade referencial
       - Permite restauração
       - Mantém relatórios antigos consistentes
    */

    /**
     * Exclui categoria LOGICAMENTE (soft delete).
     *
     * Marca ativo = 0 sem remover do banco.
     * A categoria não aparecerá mais em listar(), mas permanece no banco.
     *
     * @param id ID da categoria a excluir
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * categoriaDAO.deletar(1);
     * // Categoria fica ativo=0, mas permanece no banco
     * // Não aparece em listar(), mas aparece em listarTodas()
     * ```
     *
     * Para restaurar:
     * ```java
     * Categoria cat = categoriaDAO.buscarPorId(1);
     * cat.setAtivo(true);
     * categoriaDAO.atualizar(cat);
     * ```
     */
    public void deletar(int id) throws Exception {

        // ========== SQL: UPDATE ativo = 0 ==========
        // Não usa DELETE! Apenas desativa.
        String sql = "UPDATE categoria " +
                     "SET ativo = 0 " +
                     "WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, id);

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();
        }
    }

    /* ================================================================
       MÉTODO 7: DELETAR PERMANENTEMENTE - Exclusão física (HARD DELETE)
       ================================================================

       ⚠️ ATENÇÃO: Remove PERMANENTEMENTE do banco!

       Usado APENAS em casos especiais:
       - Dados de teste
       - Categorias criadas por engano
       - Limpeza de banco

       EVITE usar em produção!
       Prefira deletar() (soft delete) para preservar histórico.
    */

    /**
     * Exclui categoria PERMANENTEMENTE (hard delete).
     *
     * ⚠️ CUIDADO: Remove fisicamente do banco!
     * Não pode ser desfeito.
     *
     * @param id ID da categoria a excluir
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * // ⚠️ CUIDADO: Exclusão permanente!
     * categoriaDAO.deletarPermanentemente(1);
     * // Categoria removida do banco para sempre
     * ```
     *
     * RECOMENDAÇÃO:
     * Use deletar() (soft delete) ao invés deste método.
     * Preserve o histórico!
     *
     * PODE FALHAR SE:
     * - Categoria tem vendas associadas (foreign key constraint)
     * - Use soft delete nestes casos!
     */
    public void deletarPermanentemente(int id) throws Exception {

        // ========== SQL: DELETE FÍSICO ==========
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, id);

            // ========== EXECUTAR DELETE ==========
            stmt.executeUpdate();
        }
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   MÉTODOS:
   1. listar()                    → Apenas ativas
   2. listarTodas()               → Todas (ativas + inativas)
   3. buscarPorId(int)            → Por ID
   4. inserir(Categoria)          → Nova categoria
   5. atualizar(Categoria)        → Editar existente
   6. deletar(int)                → Soft delete (ativo=0)
   7. deletarPermanentemente(int) → Hard delete (remove)

   SOFT DELETE:
   ✅ deletar() marca ativo=0 (recomendado)
   ❌ deletarPermanentemente() remove fisicamente (evitar!)

   SEGURANÇA:
   ✅ PreparedStatement em TODOS os métodos
   ✅ Try-with-resources (fecha recursos automaticamente)
   ✅ Parâmetros tipados (anti SQL injection)

   INTEGRIDADE:
   - Soft delete preserva histórico
   - Permite restauração (setar ativo=1)
   - Mantém integridade referencial com vendas

   USO TÍPICO:
   ```java
   try (Connection conn = Conexao.getConnection()) {
       CategoriaDAO dao = new CategoriaDAO(conn);

       // Listar para dropdown
       List<Categoria> ativas = dao.listar();

       // Nova categoria
       Categoria cat = new Categoria();
       cat.setNomeCategoria("Eletrônicos");
       cat.setAtivo(true);
       dao.inserir(cat);

       // Editar
       cat.setNomeCategoria("Eletrônicos e Games");
       dao.atualizar(cat);

       // Excluir (soft)
       dao.deletar(cat.getIdCategoria());
   }
   ```

   OBSERVAÇÕES:
   - Conexão passada no construtor
   - Não fecha conexão (responsabilidade do caller)
   - Try-with-resources fecha PreparedStatement e ResultSet
   ================================================================ */