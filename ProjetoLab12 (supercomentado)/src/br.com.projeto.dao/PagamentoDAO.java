package br.com.projeto.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.projeto.model.Pagamento;
import br.com.projeto.model.Vendas;
import br.com.projeto.model.MetPag;

/**
 * ================================================================
 * DAO: PAGAMENTO
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia operações de banco de dados para pagamentos de vendas.
 * Uma venda pode ter múltiplos pagamentos (parcelamento, pagamento misto).
 *
 * MÉTODOS:
 * - inserir(Pagamento)         → Registra novo pagamento
 * - marcarComoExcluido(int)    → Exclusão lógica (soft delete)
 * - listarApenasAtivos()       → Todos os pagamentos ativos
 * - listarPorVenda(int)        → Pagamentos de uma venda
 * - buscar(int)                → Por ID
 *
 * TABELA:
 * Nome: pagamento
 * Colunas:
 * - id_pag         INT PRIMARY KEY AUTO_INCREMENT
 * - vendas_id      INT NOT NULL (FK → vendas)
 * - metpag_id      INT NOT NULL (FK → metodo_pagamento)
 * - valor          FLOAT NOT NULL
 * - data_pagamento DATETIME NOT NULL
 * - ativo          BOOLEAN DEFAULT TRUE
 *
 * RELACIONAMENTOS:
 * - pagamento.vendas_id  → vendas.id_vendas            (N:1)
 * - pagamento.metpag_id  → metodo_pagamento.id_metpag  (N:1)
 *
 * CASOS DE USO:
 * 1. Pagamento único: 1 venda = 1 pagamento
 * 2. Pagamento parcelado: 1 venda = N pagamentos
 * 3. Pagamento misto: 1 venda = pagamentos em métodos diferentes
 *    Ex: R$ 50 em dinheiro + R$ 100 no cartão
 *
 * SOFT DELETE:
 * Pagamentos "excluídos" ficam ativo=false mas permanecem no banco.
 * Importante para auditoria e conformidade fiscal.
 *
 * SEGURANÇA:
 * - PreparedStatement em TODOS os métodos
 * - JOINs com tabelas relacionadas
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class PagamentoDAO {

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
     *     PagamentoDAO dao = new PagamentoDAO(conn);
     *     List<Pagamento> pagamentos = dao.listarApenasAtivos();
     * }
     * ```
     */
    public PagamentoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO 1: INSERIR - Novo pagamento
       ================================================================

       Usado por: Cadastro de venda, registro de pagamento

       Registra um novo pagamento para uma venda.
       Data do pagamento é setada automaticamente como agora.

       IMPORTANTE:
       - vendas_id e metpag_id obrigatórios
       - Valor pode ser diferente do valor da venda (pagamento parcial)
       - Data setada automaticamente
    */

    /**
     * Insere novo pagamento.
     *
     * @param pagamento Objeto Pagamento com dados
     * @throws Exception Se erro no banco
     *
     * Exemplo de uso:
     * ```java
     * // Criar objetos de relacionamento
     * Vendas venda = new Vendas();
     * venda.setIdVendas(10);
     *
     * MetPag metodoPix = new MetPag();
     * metodoPix.setIdMetPag(3);
     *
     * // Criar pagamento
     * Pagamento pag = new Pagamento();
     * pag.setVendasId(venda);
     * pag.setMetPagId(metodoPix);
     * pag.setValor(150.00f);
     *
     * pagamentoDAO.inserir(pag);
     * System.out.println("Pagamento registrado! ID: " + pag.getIdPag());
     * ```
     *
     * EXEMPLO: Pagamento misto (múltiplos métodos)
     * ```java
     * // Venda de R$ 200
     * Vendas venda = vendasDAO.buscar(10);
     *
     * // Pagamento 1: R$ 50 em dinheiro
     * Pagamento pag1 = new Pagamento();
     * pag1.setVendasId(venda);
     * pag1.setMetPagId(metodoDinheiro);
     * pag1.setValor(50.00f);
     * pagamentoDAO.inserir(pag1);
     *
     * // Pagamento 2: R$ 150 no cartão
     * Pagamento pag2 = new Pagamento();
     * pag2.setVendasId(venda);
     * pag2.setMetPagId(metodoCartao);
     * pag2.setValor(150.00f);
     * pagamentoDAO.inserir(pag2);
     *
     * // Total pago: R$ 200
     * ```
     */
    public void inserir(Pagamento pagamento) throws Exception {

        // ========== SQL COM COLUNAS SNAKE_CASE ==========
        String sql = "INSERT INTO pagamento " +
                "(vendas_id, metpag_id, valor, data_pagamento) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement stmt = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);

        try {
            // ========== SETAR PARÂMETROS ==========
            stmt.setInt(1, pagamento.getVendasId().getIdVendas());
            stmt.setInt(2, pagamento.getMetPagId().getIdMetPag());
            stmt.setFloat(3, pagamento.getValor());

            // ========== DATA PAGAMENTO = AGORA ==========
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            // ========== EXECUTAR INSERT ==========
            stmt.executeUpdate();

            // ========== RECUPERAR ID GERADO ==========
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                pagamento.setIdPag(rs.getInt(1));
            }
        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 2: MARCAR COMO EXCLUIDO - Soft delete
       ================================================================

       Usado por: Cancelamento de pagamento, correção

       NÃO remove fisicamente!
       Marca ativo = false.
       Importante para auditoria fiscal.
    */

    /**
     * Marca pagamento como excluído (soft delete).
     *
     * Seta ativo = false sem remover do banco.
     * Preserva histórico para auditoria e conformidade.
     *
     * @param idPagamento ID do pagamento a excluir
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * // Cancelar pagamento
     * pagamentoDAO.marcarComoExcluido(5);
     *
     * // Pagamento fica ativo=false
     * // Não aparece em listarApenasAtivos()
     * // Mas permanece no banco para auditoria
     * ```
     *
     * QUANDO USAR:
     * - Pagamento lançado incorretamente
     * - Cancelamento de venda
     * - Estorno de pagamento
     *
     * IMPORTANTE:
     * Não use DELETE físico!
     * Dados de pagamento são críticos para:
     * - Auditoria fiscal
     * - Relatórios financeiros
     * - Conformidade legal
     */
    public void marcarComoExcluido(int idPagamento) throws Exception {

        // ========== SQL: UPDATE ativo = false ==========
        String sql = "UPDATE pagamento " +
                "SET ativo = false " +
                "WHERE id_pag = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        try {
            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, idPagamento);

            // ========== EXECUTAR UPDATE ==========
            stmt.executeUpdate();
        } finally {
            // ========== FECHAR STATEMENT ==========
            stmt.close();
        }
    }

    /* ================================================================
       MÉTODO 3: LISTAR APENAS ATIVOS - Todos os pagamentos
       ================================================================

       Usado por: Relatórios, listagem geral

       Retorna todos os pagamentos ativos com dados das tabelas
       relacionadas (vendas e metodo_pagamento) via JOIN.

       Ordenação: Mais recentes primeiro.
    */

    /**
     * Lista todos os pagamentos ATIVOS.
     *
     * @return Lista de pagamentos (vazia se nenhum)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<Pagamento> pagamentos = pagamentoDAO.listarApenasAtivos();
     *
     * for (Pagamento pag : pagamentos) {
     *     System.out.println("Venda ID: " + pag.getVendasId().getIdVendas());
     *     System.out.println("Método: " + pag.getMetPagId().getDescricao());
     *     System.out.println("Valor: R$ " + pag.getValor());
     *     System.out.println("---");
     * }
     * ```
     *
     * QUERY COM JOIN:
     * Traz dados de 3 tabelas:
     * - pagamento (principal)
     * - vendas (via vendas_id)
     * - metodo_pagamento (via metpag_id)
     */
    public List<Pagamento> listarApenasAtivos() throws Exception {

        List<Pagamento> pagamentos = new ArrayList<>();

        // ========== SQL COM JOINS ==========
        String sql = "SELECT p.*, " +
                "v.id_vendas, v.valor as valor_venda, " +
                "m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.ativo = true " +
                "ORDER BY p.data_pagamento DESC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        try {
            // ========== PROCESSAR RESULTADOS ==========
            while (rs.next()) {
                // Helper cria objeto Pagamento completo
                Pagamento pagamento = criarPagamentoDoResultSet(rs);
                pagamentos.add(pagamento);
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return pagamentos;
    }

    /* ================================================================
       MÉTODO 4: LISTAR POR VENDA - Pagamentos de uma venda
       ================================================================

       Usado por: Detalhes da venda, verificação de pagamento

       Retorna todos os pagamentos (ativos) de uma venda específica.
       Útil para ver se venda foi paga totalmente, parcialmente, etc.
    */

    /**
     * Lista pagamentos de uma venda específica.
     *
     * @param idVenda ID da venda
     * @return Lista de pagamentos da venda (vazia se nenhum)
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * // Buscar pagamentos de uma venda
     * List<Pagamento> pagamentosVenda = pagamentoDAO.listarPorVenda(10);
     *
     * // Calcular total pago
     * float totalPago = 0;
     * for (Pagamento pag : pagamentosVenda) {
     *     totalPago += pag.getValor();
     * }
     *
     * System.out.println("Total pago: R$ " + totalPago);
     *
     * // Verificar se venda foi quitada
     * Vendas venda = vendasDAO.buscar(10);
     * boolean quitada = (totalPago >= venda.getValor());
     * System.out.println("Venda quitada? " + (quitada ? "SIM" : "NÃO"));
     * ```
     *
     * CASOS DE USO:
     * 1. Pagamento único: retorna 1 pagamento
     * 2. Parcelado: retorna N pagamentos
     * 3. Misto: retorna pagamentos em diferentes métodos
     */
    public List<Pagamento> listarPorVenda(int idVenda) throws Exception {

        List<Pagamento> pagamentos = new ArrayList<>();

        // ========== SQL COM JOINS E FILTRO ==========
        String sql = "SELECT p.*, " +
                "v.id_vendas, v.valor as valor_venda, " +
                "m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.ativo = true AND p.vendas_id = ? " +
                "ORDER BY p.data_pagamento";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, idVenda);
        ResultSet rs = stmt.executeQuery();

        try {
            // ========== PROCESSAR RESULTADOS ==========
            while (rs.next()) {
                Pagamento pagamento = criarPagamentoDoResultSet(rs);
                pagamentos.add(pagamento);
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return pagamentos;
    }

    /* ================================================================
       MÉTODO 5: BUSCAR - Por ID
       ================================================================

       Usado por: Edição, visualização

       Busca um pagamento específico por seu ID.
       Retorna null se não encontrar ou se estiver inativo.
    */

    /**
     * Busca pagamento por ID.
     *
     * @param id ID do pagamento
     * @return Pagamento ou null se não encontrado/inativo
     * @throws Exception Se erro no banco
     *
     * Exemplo:
     * ```java
     * Pagamento pag = pagamentoDAO.buscar(5);
     * if (pag != null) {
     *     System.out.println("Método: " + pag.getMetPagId().getDescricao());
     *     System.out.println("Valor: R$ " + pag.getValor());
     * } else {
     *     System.out.println("Pagamento não encontrado");
     * }
     * ```
     */
    public Pagamento buscar(int id) throws Exception {

        // ========== SQL COM JOINS E FILTROS ==========
        String sql = "SELECT p.*, " +
                "v.id_vendas, v.valor as valor_venda, " +
                "m.descricao as metodo_pagamento " +
                "FROM pagamento p " +
                "JOIN vendas v ON p.vendas_id = v.id_vendas " +
                "JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag " +
                "WHERE p.id_pag = ? AND p.ativo = true";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        Pagamento pagamento = null;

        try {
            // ========== PROCESSAR RESULTADO ==========
            if (rs.next()) {
                pagamento = criarPagamentoDoResultSet(rs);
            }
        } finally {
            // ========== FECHAR RECURSOS ==========
            rs.close();
            stmt.close();
        }

        return pagamento;
    }

    /* ================================================================
       MÉTODO PRIVADO: CRIAR PAGAMENTO DO RESULTSET
       ================================================================

       HELPER privado usado pelos métodos de busca.

       Cria objeto Pagamento completo a partir do ResultSet.
       Inclui objetos Vendas e MetPag relacionados.

       IMPORTANTE:
       Este método espera que o ResultSet contenha colunas de JOIN:
       - Colunas de pagamento (p.*)
       - Colunas de vendas (vendas_id, valor_venda)
       - Colunas de metodo_pagamento (metpag_id, metodo_pagamento)
    */

    /**
     * Cria objeto Pagamento a partir do ResultSet.
     *
     * MÉTODO PRIVADO - Usado internamente pelos métodos de busca.
     *
     * @param rs ResultSet com dados do JOIN
     * @return Objeto Pagamento completo
     * @throws SQLException Se erro ao ler ResultSet
     *
     * ESTRUTURA DO OBJETO RETORNADO:
     * ```
     * Pagamento
     * ├── idPag (id do pagamento)
     * ├── valor (valor do pagamento)
     * ├── vendasId (objeto Vendas)
     * │   ├── idVendas
     * │   └── valor
     * └── metPagId (objeto MetPag)
     *     ├── idMetPag
     *     └── descricao
     * ```
     */
    private Pagamento criarPagamentoDoResultSet(ResultSet rs) throws SQLException {

        // ========== CRIAR OBJETO VENDAS ==========
        Vendas venda = new Vendas();
        venda.setIdVendas(rs.getInt("vendas_id"));
        venda.setValor(rs.getFloat("valor_venda"));

        // ========== CRIAR OBJETO METPAG ==========
        MetPag metPag = new MetPag();
        metPag.setIdMetPag(rs.getInt("metpag_id"));
        metPag.setDescricao(rs.getString("metodo_pagamento"));

        // ========== CRIAR PAGAMENTO COM RELACIONAMENTOS ==========
        // Construtor: Pagamento(id, venda, metPag, valor)
        Pagamento pagamento = new Pagamento(
                rs.getInt("id_pag"),
                venda,
                metPag,
                rs.getFloat("valor")
        );

        return pagamento;
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   MÉTODOS PÚBLICOS:
   1. inserir(Pagamento)            → Novo pagamento
   2. marcarComoExcluido(int)       → Soft delete
   3. listarApenasAtivos()          → Todos ativos
   4. listarPorVenda(int)           → Por venda
   5. buscar(int)                   → Por ID

   MÉTODO PRIVADO:
   - criarPagamentoDoResultSet(rs)  → Helper para criar objeto

   RELACIONAMENTOS:
   - pagamento → vendas (N:1)
   - pagamento → metodo_pagamento (N:1)

   CASOS DE USO:

   1. PAGAMENTO ÚNICO:
   ```java
   Vendas venda = new Vendas(); venda.setIdVendas(10);
   MetPag pix = new MetPag(); pix.setIdMetPag(3);

   Pagamento pag = new Pagamento();
   pag.setVendasId(venda);
   pag.setMetPagId(pix);
   pag.setValor(200.00f);

   pagamentoDAO.inserir(pag);
   ```

   2. PAGAMENTO PARCELADO (3x):
   ```java
   for (int i = 1; i <= 3; i++) {
       Pagamento parcela = new Pagamento();
       parcela.setVendasId(venda);
       parcela.setMetPagId(cartao);
       parcela.setValor(100.00f);  // R$ 100 cada parcela
       pagamentoDAO.inserir(parcela);
   }
   ```

   3. PAGAMENTO MISTO:
   ```java
   // R$ 50 dinheiro
   Pagamento pag1 = new Pagamento();
   pag1.setVendasId(venda);
   pag1.setMetPagId(dinheiro);
   pag1.setValor(50.00f);
   pagamentoDAO.inserir(pag1);

   // R$ 150 cartão
   Pagamento pag2 = new Pagamento();
   pag2.setVendasId(venda);
   pag2.setMetPagId(cartao);
   pag2.setValor(150.00f);
   pagamentoDAO.inserir(pag2);
   ```

   SOFT DELETE:
   ✅ marcarComoExcluido() marca ativo=false
   ✅ Preserva histórico fiscal
   ✅ Auditoria e conformidade

   JOINS:
   Métodos de busca usam JOIN para trazer dados completos:
   - Dados do pagamento
   - Dados da venda
   - Descrição do método de pagamento

   SEGURANÇA:
   ✅ PreparedStatement
   ✅ Parâmetros tipados
   ✅ Try-finally para fechar recursos

   OBSERVAÇÕES:
   - Data de pagamento setada automaticamente como agora
   - Valor do pagamento pode ser ≠ valor da venda (parcial/misto)
   - Múltiplos pagamentos por venda são permitidos
   - Soft delete por questões fiscais/legais
   ================================================================ */