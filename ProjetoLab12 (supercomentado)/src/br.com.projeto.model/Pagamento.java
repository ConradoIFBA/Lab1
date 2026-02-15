package br.com.projeto.model;

/**
 * ================================================================
 * MODEL: PAGAMENTO
 * ================================================================
 *
 * PROPÓSITO:
 * Representa um pagamento realizado para uma venda.
 * Uma venda pode ter múltiplos pagamentos (parcelamento, divisão).
 *
 * TABELA DO BANCO:
 * Nome: pagamento
 * Colunas:
 *   - id_pagamento (INT, PK, AUTO_INCREMENT)
 *   - vendas_id (INT, FK → vendas)
 *   - metodo_pagamento_id (INT, FK → metodo_pagamento)
 *   - valor (DECIMAL(10,2), NOT NULL)
 *
 * RELACIONAMENTOS:
 * - N:1 com Vendas (muitos pagamentos para uma venda)
 * - N:1 com MetPag (muitos pagamentos usam um método)
 *
 * CENÁRIOS DE USO:
 *
 * 1. PAGAMENTO SIMPLES (100% um método):
 *    Venda: R$ 100,00
 *    Pagamento 1: R$ 100,00 (PIX)
 *
 * 2. PAGAMENTO DIVIDIDO (2+ métodos):
 *    Venda: R$ 100,00
 *    Pagamento 1: R$ 50,00 (Dinheiro)
 *    Pagamento 2: R$ 50,00 (Cartão)
 *
 * 3. PAGAMENTO PARCELADO:
 *    Venda: R$ 300,00
 *    Pagamento 1: R$ 100,00 (Cartão - parcela 1)
 *    Pagamento 2: R$ 100,00 (Cartão - parcela 2)
 *    Pagamento 3: R$ 100,00 (Cartão - parcela 3)
 *
 * REGRAS DE NEGÓCIO:
 * - Soma dos pagamentos DEVE = valor da venda
 * - Cada pagamento tem um método específico
 * - Valor do pagamento > 0
 * - Uma venda pode ter 1 ou mais pagamentos
 *
 * USO TÍPICO:
 * ```java
 * // Pagamento simples
 * Pagamento pag = new Pagamento();
 * pag.setVendasId(venda);
 * pag.setMetPagId(metodoPix);
 * pag.setValor(venda.getValor());
 * pagamentoDAO.inserir(pag);
 *
 * // Pagamento dividido
 * Pagamento pag1 = new Pagamento(venda, metodoDinheiro, 50.00f);
 * Pagamento pag2 = new Pagamento(venda, metodoCartao, 50.00f);
 * pagamentoDAO.inserir(pag1);
 * pagamentoDAO.inserir(pag2);
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class Pagamento {

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único do pagamento (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     */
    private int idPag;

    /**
     * Venda relacionada (relacionamento N:1).
     *
     * Objeto completo Vendas com:
     * - idVendas
     * - dataVendas
     * - valor total
     * - categoria
     * - etc.
     *
     * Múltiplos pagamentos podem apontar para a mesma venda.
     *
     * Exemplo:
     * ```java
     * Vendas venda = vendasDAO.buscar(123);
     *
     * Pagamento pag1 = new Pagamento();
     * pag1.setVendasId(venda); // Mesmo objeto venda
     *
     * Pagamento pag2 = new Pagamento();
     * pag2.setVendasId(venda); // Mesmo objeto venda
     *
     * // Ambos os pagamentos são da mesma venda
     * ```
     */
    private Vendas vendasId;

    /**
     * Método de pagamento usado (relacionamento N:1).
     *
     * Objeto completo MetPag com:
     * - idMetPag
     * - descricao (ex: "PIX", "Dinheiro")
     *
     * Exemplo:
     * ```java
     * MetPag metodo = metPagDAO.buscar(1); // PIX
     *
     * Pagamento pag = new Pagamento();
     * pag.setMetPagId(metodo);
     *
     * // Para acessar descrição:
     * String desc = pag.getMetPagId().getDescricao(); // "PIX"
     * // OU usar método auxiliar:
     * int idMetodo = pag.getIdMetPag(); // 1
     * ```
     */
    private MetPag metPagId;

    /**
     * Valor pago neste pagamento.
     *
     * Tipo float para compatibilidade com MySQL DECIMAL(10,2).
     *
     * REGRAS:
     * - Valor > 0 (sempre positivo)
     * - Soma de todos os pagamentos = valor da venda
     * - 2 casas decimais
     *
     * Validação recomendada:
     * ```java
     * if (valor <= 0) {
     *     throw new IllegalArgumentException("Valor deve ser maior que zero");
     * }
     *
     * // Verificar soma ao inserir último pagamento
     * List<Pagamento> pagamentos = pagamentoDAO.listarPorVenda(vendaId);
     * float somaPagamentos = pagamentos.stream()
     *                                   .map(Pagamento::getValor)
     *                                   .reduce(0f, Float::sum);
     * somaPagamentos += novoPagamento.getValor();
     *
     * if (Math.abs(somaPagamentos - venda.getValor()) > 0.01) {
     *     throw new IllegalArgumentException("Soma dos pagamentos não bate com valor da venda");
     * }
     * ```
     */
    private float valor;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       4 construtores para diferentes casos de uso:
       1. Vazio → Para criar objeto antes de preencher
       2. Completo com objetos → Após buscar do banco
       3. Sem ID com objetos → Para inserção
       4. Simplificado com IDs → Inserção rápida
    */

    /**
     * CONSTRUTOR VAZIO
     *
     * Uso:
     * - Criar objeto antes de preencher com setters
     * - Frameworks ORM
     * - Inicialização genérica
     *
     * Exemplo:
     * ```java
     * Pagamento pag = new Pagamento();
     * pag.setVendasId(venda);
     * pag.setMetPagId(metodo);
     * pag.setValor(100.00f);
     * pagamentoDAO.inserir(pag);
     * ```
     */
    public Pagamento() {}

    /**
     * CONSTRUTOR COMPLETO (com objetos)
     *
     * Usado após buscar pagamento do banco.
     * Inclui ID e objetos completos de Vendas e MetPag.
     *
     * @param idPag ID do pagamento (do banco)
     * @param vendasId Objeto Vendas completo
     * @param metPagId Objeto MetPag completo
     * @param valor Valor pago
     *
     * Exemplo (ResultSet com JOINs):
     * ```java
     * int id = rs.getInt("id_pagamento");
     * float valor = rs.getFloat("valor");
     *
     * // Buscar venda e método (ou popular via JOIN)
     * Vendas venda = vendasDAO.buscar(rs.getInt("vendas_id"));
     * MetPag metodo = metPagDAO.buscar(rs.getInt("metodo_pagamento_id"));
     *
     * Pagamento pag = new Pagamento(id, venda, metodo, valor);
     * ```
     */
    public Pagamento(int idPag, Vendas vendasId, MetPag metPagId, float valor) {
        this.idPag = idPag;
        this.vendasId = vendasId;
        this.metPagId = metPagId;
        this.valor = valor;
    }

    /**
     * CONSTRUTOR SEM ID (para inserção)
     *
     * Usado ao criar novo pagamento para inserir no banco.
     * ID será gerado automaticamente.
     *
     * @param vendasId Objeto Vendas
     * @param metPagId Objeto MetPag
     * @param valor Valor pago
     *
     * Exemplo:
     * ```java
     * Vendas venda = vendasDAO.buscar(123);
     * MetPag metodoPix = metPagDAO.buscar(4); // PIX
     *
     * Pagamento pag = new Pagamento(venda, metodoPix, 150.00f);
     * pagamentoDAO.inserir(pag); // ID gerado automaticamente
     * ```
     */
    public Pagamento(Vendas vendasId, MetPag metPagId, float valor) {
        this.vendasId = vendasId;
        this.metPagId = metPagId;
        this.valor = valor;
        // idPag será gerado automaticamente pelo banco
    }

    /**
     * CONSTRUTOR SIMPLIFICADO (com IDs básicos)
     *
     * Usado quando você tem apenas os IDs, não os objetos completos.
     * Cria objetos Vendas e MetPag apenas com IDs.
     *
     * @param idVenda ID da venda
     * @param idMetPag ID do método de pagamento
     * @param valor Valor pago
     *
     * Exemplo:
     * ```java
     * // Receber dados de formulário
     * int vendaId = Integer.parseInt(request.getParameter("vendaId"));
     * int metodoId = Integer.parseInt(request.getParameter("metodoId"));
     * float valor = Float.parseFloat(request.getParameter("valor"));
     *
     * Pagamento pag = new Pagamento(vendaId, metodoId, valor);
     * pagamentoDAO.inserir(pag);
     * ```
     */
    public Pagamento(int idVenda, int idMetPag, float valor) {
        // Criar objeto Vendas apenas com ID
        this.vendasId = new Vendas();
        this.vendasId.setIdVenda(idVenda);

        // Criar objeto MetPag apenas com ID
        this.metPagId = new MetPag();
        this.metPagId.setIdMetPag(idMetPag);

        this.valor = valor;
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================ */

    /**
     * Retorna o ID do pagamento.
     *
     * @return ID único do pagamento (PK)
     */
    public int getIdPag() {
        return idPag;
    }

    /**
     * Define o ID do pagamento.
     * Normalmente setado após INSERT no banco.
     *
     * @param idPag ID gerado pelo banco
     */
    public void setIdPag(int idPag) {
        this.idPag = idPag;
    }

    /**
     * Retorna o objeto Vendas relacionado.
     *
     * @return Objeto Vendas completo
     *
     * Exemplo:
     * ```java
     * Pagamento pag = pagamentoDAO.buscar(1);
     * Vendas venda = pag.getVendasId();
     *
     * float valorVenda = venda.getValor();
     * String categoria = venda.getNomeCategoria();
     * ```
     */
    public Vendas getVendasId() {
        return vendasId;
    }

    /**
     * Define o objeto Vendas relacionado.
     *
     * @param vendasId Objeto Vendas completo
     *
     * Exemplo:
     * ```java
     * Vendas venda = vendasDAO.buscar(123);
     *
     * Pagamento pag = new Pagamento();
     * pag.setVendasId(venda);
     * ```
     */
    public void setVendasId(Vendas vendasId) {
        this.vendasId = vendasId;
    }

    /**
     * Retorna o objeto MetPag relacionado.
     *
     * @return Objeto MetPag completo
     *
     * Exemplo:
     * ```java
     * Pagamento pag = pagamentoDAO.buscar(1);
     * MetPag metodo = pag.getMetPagId();
     *
     * String descricao = metodo.getDescricao(); // "PIX"
     * ```
     */
    public MetPag getMetPagId() {
        return metPagId;
    }

    /**
     * Define o objeto MetPag relacionado.
     *
     * @param metPagId Objeto MetPag completo
     *
     * Exemplo:
     * ```java
     * MetPag metodoPix = metPagDAO.buscar(4);
     *
     * Pagamento pag = new Pagamento();
     * pag.setMetPagId(metodoPix);
     * ```
     */
    public void setMetPagId(MetPag metPagId) {
        this.metPagId = metPagId;
    }

    /**
     * Retorna o valor do pagamento.
     *
     * @return Valor pago (float)
     */
    public float getValor() {
        return valor;
    }

    /**
     * Define o valor do pagamento.
     *
     * IMPORTANTE: Valor deve ser > 0.
     * Soma de todos os pagamentos deve = valor da venda.
     *
     * @param valor Valor pago (sempre positivo)
     *
     * Validação recomendada:
     * ```java
     * if (valor <= 0) {
     *     throw new IllegalArgumentException("Valor deve ser maior que zero");
     * }
     * pag.setValor(valor);
     * ```
     */
    public void setValor(float valor) {
        this.valor = valor;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * AUXILIAR: Retorna ID da venda.
     * Atalho para getVendasId().getIdVenda().
     *
     * @return ID da venda ou 0 se vendasId for null
     *
     * Exemplo:
     * ```java
     * int idVenda = pag.getIdVenda();
     * // Em vez de:
     * int idVenda = pag.getVendasId() != null ?
     *               pag.getVendasId().getIdVenda() : 0;
     * ```
     */
    public int getIdVenda() {
        return vendasId != null ? vendasId.getIdVenda() : 0;
    }

    /**
     * AUXILIAR: Retorna ID do método de pagamento.
     * Atalho para getMetPagId().getIdMetPag().
     *
     * @return ID do método ou 0 se metPagId for null
     *
     * Exemplo:
     * ```java
     * int idMetodo = pag.getIdMetPag();
     * // Em vez de:
     * int idMetodo = pag.getMetPagId() != null ?
     *                pag.getMetPagId().getIdMetPag() : 0;
     * ```
     */
    public int getIdMetPag() {
        return metPagId != null ? metPagId.getIdMetPag() : 0;
    }

    /**
     * Representação em String do pagamento.
     * Útil para logs e debug.
     *
     * @return String com dados do pagamento
     *
     * Exemplo de saída:
     * "Pagamento [idPag=1, vendasId=123, metPagId=4, valor=150.0]"
     */
    @Override
    public String toString() {
        return "Pagamento [idPag=" + idPag +
                ", vendasId=" + (vendasId != null ? vendasId.getIdVenda() : "null") +
                ", metPagId=" + (metPagId != null ? metPagId.getIdMetPag() : "null") +
                ", valor=" + valor + "]";
    }

    /**
     * Verifica se dois pagamentos são iguais.
     * Igualdade baseada apenas no ID (chave primária).
     *
     * @param o Objeto a comparar
     * @return true se mesmo ID, false caso contrário
     *
     * Exemplo:
     * ```java
     * Pagamento p1 = new Pagamento();
     * p1.setIdPag(1);
     *
     * Pagamento p2 = new Pagamento();
     * p2.setIdPag(1);
     *
     * p1.equals(p2); // true (mesmo ID)
     * ```
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return idPag == pagamento.idPag;
    }

    /**
     * Gera código hash baseado no ID.
     * Necessário ao usar equals() customizado.
     *
     * @return Hash code do ID
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(idPag);
    }
}

/* ================================================================
   RESUMO DO MODEL
   ================================================================

   CAMPOS:
   - idPag      → PK, auto increment
   - vendasId   → FK para vendas (objeto Vendas)
   - metPagId   → FK para metodo_pagamento (objeto MetPag)
   - valor      → DECIMAL(10,2), parte do total da venda

   CONSTRUTORES:
   - ()                                  → Vazio
   - (id, venda, metodo, valor)          → Completo (buscar)
   - (venda, metodo, valor)              → Sem ID (inserir)
   - (idVenda, idMetodo, valor)          → Simplificado (IDs)

   RELACIONAMENTOS:
   - N:1 com Vendas (muitos pagamentos → uma venda)
   - N:1 com MetPag (muitos pagamentos → um método)

   REGRAS DE NEGÓCIO:
   - Valor > 0
   - Soma dos pagamentos = valor da venda
   - Uma venda pode ter 1+ pagamentos
   - Cada pagamento tem 1 método

   CENÁRIOS:
   1. Pagamento simples:
      - Venda R$ 100,00
      - 1 pagamento: R$ 100,00 (PIX)

   2. Pagamento dividido:
      - Venda R$ 100,00
      - Pagamento 1: R$ 50,00 (Dinheiro)
      - Pagamento 2: R$ 50,00 (Cartão)

   3. Parcelamento:
      - Venda R$ 300,00
      - Pagamento 1: R$ 100,00 (Cartão)
      - Pagamento 2: R$ 100,00 (Cartão)
      - Pagamento 3: R$ 100,00 (Cartão)

   MÉTODOS AUXILIARES:
   - getIdVenda()   → Retorna ID da venda (atalho)
   - getIdMetPag()  → Retorna ID do método (atalho)

   USO NO SISTEMA:
   - Registrar como venda foi paga
   - Relatórios por método de pagamento
   - Controle de recebimentos
   - Dashboard de pagamentos

   QUERIES COMUNS:
   - SELECT * FROM pagamento WHERE vendas_id = ?
   - SELECT SUM(valor) FROM pagamento WHERE vendas_id = ?
   - SELECT * FROM pagamento WHERE metodo_pagamento_id = ?
   - SELECT COUNT(*), SUM(valor) FROM pagamento
     WHERE usuario_id IN (SELECT id FROM vendas WHERE usuario_id = ?)

   VALIDAÇÃO IMPORTANTE:
   ```java
   // Verificar soma dos pagamentos
   List<Pagamento> pagamentos = pagamentoDAO.listarPorVenda(vendaId);
   float soma = pagamentos.stream()
                           .map(Pagamento::getValor)
                           .reduce(0f, Float::sum);

   Vendas venda = vendasDAO.buscar(vendaId);

   if (Math.abs(soma - venda.getValor()) > 0.01) {
       throw new IllegalStateException("Soma incorreta!");
   }
   ```

   OBSERVAÇÕES:
   - toString() mostra IDs (não objetos completos)
   - equals() baseado em ID
   - hashCode() baseado em ID
   - Métodos auxiliares para acessar IDs
   - Construtores flexíveis (objetos ou IDs)
   ================================================================ */