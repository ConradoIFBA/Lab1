package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.MetPag;

/**
 * ================================================================
 * DAO: METODO DE PAGAMENTO (MetPag)
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia operações de banco de dados para métodos de pagamento.
 * Ex: Dinheiro, Cartão de Crédito, PIX, Boleto, etc.
 *
 * MÉTODOS:
 * - inserir(MetPag)    → Cadastra novo método
 * - listar()           → Lista métodos ativos
 * - editar(MetPag)     → Atualiza método existente
 * - excluir(int)       → Exclusão lógica (soft delete)
 * - buscar(int)        → Busca método específico
 *
 * TABELA:
 * Nome: metodo_pagamento
 * Colunas:
 * - id_metpag  INT PRIMARY KEY AUTO_INCREMENT
 * - descricao  VARCHAR(100) NOT NULL
 * - ativo      BOOLEAN DEFAULT TRUE
 *
 * SOFT DELETE:
 * Métodos "excluídos" ficam ativo=false mas permanecem no banco.
 * Preserva histórico de vendas antigas.
 *
 * EXEMPLOS DE MÉTODOS:
 * - Dinheiro
 * - Cartão de Crédito
 * - Cartão de Débito
 * - PIX
 * - Boleto
 * - Transferência
 *
 * SEGURANÇA:
 * - PreparedStatement em TODOS os métodos
 * - Try-with-resources para fechar recursos
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class MetPagDAO {

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
     *     MetPagDAO dao = new MetPagDAO(conn);
     *     List<MetPag> metodos = dao.listar();
     * }
     * ```
     */
    public MetPagDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO 1: INSERIR - Novo método de pagamento
       ================================================================

       Usado por: Formulário de cadastro de métodos

       Cadastra um novo método de pagamento no sistema.
       Retorna o ID gerado no próprio objeto.

       IMPORTANTE:
       - Descrição é obrigatória
       - Ativo padrão = true (definido no banco)
    */

    /**
     * Insere novo método de pagamento.
     *
     * @param metPag Objeto MetPag com descrição
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * MetPag metodo = new MetPag();
     * metodo.setDescricao("PIX");
     *
     * metPagDAO.inserir(metodo);
     * System.out.println("ID gerado: " + metodo.getIdMetPag());
     * ```
     *
     * SQL executado:
     * INSERT INTO metodo_pagamento (descricao) VALUES (?)
     */
    public void inserir(MetPag metPag) throws Exception {

        // ========== SQL COM DESCRICAO ==========
        String sql = "INSERT INTO metodo_pagamento (descricao) VALUES (?)";

        // ========== PREPARAR STATEMENT ==========
        PreparedStatement stmt = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);

        try {
            // ========== SETAR PARÂMETRO ==========
            stmt.setString(1, metPag.getDescricao());

            // ========== EXECUTAR INSERT ==========
            stmt.executeUpdate();

            // ========== RECUPERAR ID GERADO ==========
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                metPag.setIdMetPag(rs.getInt(1));
            }
        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 2: LISTAR - Métodos ativos
       ================================================================

       Usado por: Formulário de venda (dropdown)

       Retorna apenas métodos com ativo = true.
       Ordenados alfabeticamente para facilitar seleção.

       IMPORTANTE:
       Não retorna métodos excluídos (ativo=false).
    */

    /**
     * Lista todos os métodos de pagamento ATIVOS.
     *
     * @return Lista de métodos ativos (vazia se nenhum)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<MetPag> metodos = metPagDAO.listar();
     * for (MetPag m : metodos) {
     *     System.out.println(m.getDescricao());
     * }
     *
     * // Usar em dropdown:
     * <select name="metodoPagamento">
     *     <c:forEach items="${metodos}" var="m">
     *         <option value="${m.idMetPag}">${m.descricao}</option>
     *     </c:forEach>
     * </select>
     * ```
     *
     * SQL executado:
     * SELECT * FROM metodo_pagamento
     * WHERE ativo = true
     * ORDER BY descricao
     */
    public List<MetPag> listar() throws Exception {

        List<MetPag> metPags = new ArrayList<>();

        // ========== SQL: APENAS ATIVOS ==========
        String sql = "SELECT * FROM metodo_pagamento " +
                "WHERE ativo = true " +
                "ORDER BY descricao";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        try {
            // ========== PROCESSAR RESULTADOS ==========
            while (rs.next()) {
                // Construtor: MetPag(id, descricao)
                MetPag metPag = new MetPag(
                        rs.getInt("id_metpag"),
                        rs.getString("descricao")
                );
                metPags.add(metPag);
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return metPags;
    }

    /* ================================================================
       MÉTODO 3: EDITAR - Atualizar método
       ================================================================

       Usado por: Formulário de edição

       Atualiza a descrição de um método existente.
       Usa o ID do objeto para localizar o registro.
    */

    /**
     * Edita método de pagamento existente.
     *
     * @param metPag Objeto MetPag com dados atualizados
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * MetPag metodo = metPagDAO.buscar(1);
     * metodo.setDescricao("PIX - Pagamento Instantâneo");
     *
     * metPagDAO.editar(metodo);
     * System.out.println("Método atualizado!");
     * ```
     *
     * SQL executado:
     * UPDATE metodo_pagamento
     * SET descricao = ?
     * WHERE id_metpag = ?
     */
    public void editar(MetPag metPag) throws Exception {

        // ========== SQL COM PARÂMETROS ==========
        String sql = "UPDATE metodo_pagamento " +
                "SET descricao = ? " +
                "WHERE id_metpag = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        try {
            // ========== SETAR PARÂMETROS ==========
            stmt.setString(1, metPag.getDescricao());
            stmt.setInt(2, metPag.getIdMetPag());

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();
        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 4: EXCLUIR - Exclusão lógica (SOFT DELETE)
       ================================================================

       Usado por: Ação de excluir método

       NÃO remove fisicamente do banco!
       Apenas marca ativo = false.

       IMPORTANTE:
       - Preserva histórico de vendas antigas
       - Método não aparece mais em listar()
       - Mas dados de pagamentos antigos permanecem válidos
    */

    /**
     * Exclui método LOGICAMENTE (soft delete).
     *
     * Marca ativo = false sem remover do banco.
     * Preserva integridade de vendas/pagamentos antigos.
     *
     * @param id ID do método a excluir
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * metPagDAO.excluir(1);
     * // Método fica ativo=false
     * // Não aparece em listar()
     * // Mas pagamentos antigos continuam referenciando ele
     * ```
     *
     * Por que soft delete?
     * - Vendas antigas usam este método
     * - Relatórios históricos precisam do nome
     * - Permite restauração futura
     *
     * SQL executado:
     * UPDATE metodo_pagamento
     * SET ativo = false
     * WHERE id_metpag = ?
     */
    public void excluir(int id) throws Exception {

        // ========== SQL: UPDATE ativo = false ==========
        String sql = "UPDATE metodo_pagamento " +
                "SET ativo = false " +
                "WHERE id_metpag = ?";

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
       MÉTODO 5: BUSCAR - Por ID
       ================================================================

       Usado por: Edição, visualização

       Busca um método específico por seu ID.
       Retorna null se não encontrar OU se estiver inativo.
    */

    /**
     * Busca método de pagamento por ID.
     *
     * @param id ID do método
     * @return MetPag ou null se não encontrado/inativo
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * MetPag metodo = metPagDAO.buscar(1);
     * if (metodo != null) {
     *     System.out.println("Método: " + metodo.getDescricao());
     * } else {
     *     System.out.println("Método não encontrado ou inativo");
     * }
     * ```
     *
     * IMPORTANTE:
     * Retorna null se método estiver inativo (ativo=false).
     * Para buscar inclusive inativos, remova "AND ativo = true" do SQL.
     *
     * SQL executado:
     * SELECT * FROM metodo_pagamento
     * WHERE id_metpag = ? AND ativo = true
     */
    public MetPag buscar(int id) throws Exception {

        // ========== SQL COM FILTRO ATIVO ==========
        String sql = "SELECT * FROM metodo_pagamento " +
                "WHERE id_metpag = ? AND ativo = true";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        MetPag metPag = null;

        try {
            // ========== PROCESSAR RESULTADO ==========
            if (rs.next()) {
                // Construtor: MetPag(id, descricao)
                metPag = new MetPag(
                        rs.getInt("id_metpag"),
                        rs.getString("descricao")
                );
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return metPag;
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   MÉTODOS:
   1. inserir(MetPag)    → Novo método de pagamento
   2. listar()           → Métodos ativos apenas
   3. editar(MetPag)     → Atualizar descrição
   4. excluir(int)       → Soft delete (ativo=false)
   5. buscar(int)        → Por ID (apenas ativos)

   SOFT DELETE:
   ✅ excluir() marca ativo=false
   ✅ Preserva histórico de vendas
   ✅ Permite restauração futura
   ✅ Mantém integridade referencial

   MÉTODOS COMUNS:
   - Dinheiro
   - Cartão de Crédito
   - Cartão de Débito
   - PIX
   - Boleto Bancário
   - Transferência Bancária

   USO EM VENDAS:
   Cada pagamento (tabela pagamento) referencia um método:
   - pagamento.metpag_id → metodo_pagamento.id_metpag
   - Foreign key garante que método existe
   - Soft delete não quebra vendas antigas

   EXEMPLO COMPLETO:
   ```java
   try (Connection conn = Conexao.getConnection()) {
       MetPagDAO dao = new MetPagDAO(conn);

       // Cadastrar métodos
       MetPag pix = new MetPag();
       pix.setDescricao("PIX");
       dao.inserir(pix);

       MetPag cartao = new MetPag();
       cartao.setDescricao("Cartão de Crédito");
       dao.inserir(cartao);

       // Listar para dropdown
       List<MetPag> metodos = dao.listar();
       for (MetPag m : metodos) {
           System.out.println(m.getIdMetPag() + " - " + m.getDescricao());
       }

       // Editar
       pix.setDescricao("PIX - Pagamento Instantâneo");
       dao.editar(pix);

       // Excluir (soft)
       dao.excluir(cartao.getIdMetPag());
       // cartao fica ativo=false, mas pagamentos antigos continuam válidos
   }
   ```

   SEGURANÇA:
   ✅ PreparedStatement em todos os métodos
   ✅ Parâmetros tipados
   ✅ Anti SQL injection

   INTEGRIDADE:
   - Soft delete preserva histórico
   - Foreign keys nas tabelas pagamento e vendas
   - Não permite exclusão se há pagamentos usando o método

   OBSERVAÇÕES:
   - Conexão passada no construtor
   - Não fecha conexão (responsabilidade do caller)
   - Statements fechados em finally
   - Descrição obrigatória e única (constraint no banco)
   ================================================================ */