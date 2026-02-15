package br.com.projeto.model;

/**
 * ================================================================
 * MODEL: MÉTODO DE PAGAMENTO (MetPag)
 * ================================================================
 *
 * PROPÓSITO:
 * Representa um método de pagamento aceito pelo MEI.
 * Tabela de referência simples (lookup table).
 *
 * TABELA DO BANCO:
 * Nome: metodo_pagamento
 * Colunas:
 *   - id_metodo_pagamento (INT, PK, AUTO_INCREMENT)
 *   - descricao (VARCHAR, NOT NULL)
 *
 * RELACIONAMENTOS:
 * - 1:N com Pagamento (um método tem muitos pagamentos)
 *
 * MÉTODOS PADRÃO NO SISTEMA:
 * 1. Dinheiro
 * 2. Cartão de Crédito
 * 3. Cartão de Débito
 * 4. PIX
 * 5. Boleto
 * 6. Transferência Bancária
 * 7. Cheque
 * 8. Outro
 *
 * CARACTERÍSTICAS:
 * - Tabela estática (não muda frequentemente)
 * - Pré-populada com métodos comuns
 * - Usada em dropdowns de seleção
 * - Não tem exclusão lógica (raramente deletada)
 *
 * USO TÍPICO:
 * ```java
 * // Buscar métodos disponíveis
 * List<MetPag> metodos = metPagDAO.listar();
 *
 * // Criar novo método
 * MetPag metodo = new MetPag("PayPal");
 * metPagDAO.inserir(metodo);
 *
 * // Usar em pagamento
 * Pagamento pag = new Pagamento();
 * pag.setMetPagId(metodo);
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class MetPag {

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único do método de pagamento (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     * Usado como FK na tabela pagamento.
     */
    private int idMetPag;

    /**
     * Descrição do método de pagamento.
     *
     * NOT NULL no banco.
     *
     * Valores comuns:
     * - "Dinheiro"
     * - "Cartão de Crédito"
     * - "Cartão de Débito"
     * - "PIX"
     * - "Boleto"
     * - "Transferência Bancária"
     * - "Cheque"
     * - "Outro"
     *
     * Exibido em:
     * - Dropdowns de seleção
     * - Relatórios
     * - Dashboards
     *
     * Máximo: 100 caracteres (VARCHAR(100))
     */
    private String descricao;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       3 construtores para diferentes casos de uso:
       1. Vazio → Para criar objeto antes de preencher
       2. Com ID → Após buscar do banco
       3. Sem ID → Para inserção (ID auto gerado)
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
     * MetPag metodo = new MetPag();
     * metodo.setDescricao("PIX");
     * metPagDAO.inserir(metodo);
     * ```
     */
    public MetPag() {}

    /**
     * CONSTRUTOR COM ID
     *
     * Usado após buscar método de pagamento do banco.
     * Inclui ID e descrição.
     *
     * @param idMetPag ID do método (do banco)
     * @param descricao Descrição do método
     *
     * Exemplo (ResultSet):
     * ```java
     * int id = rs.getInt("id_metodo_pagamento");
     * String desc = rs.getString("descricao");
     * MetPag metodo = new MetPag(id, desc);
     * ```
     */
    public MetPag(int idMetPag, String descricao) {
        this.idMetPag = idMetPag;
        this.descricao = descricao;
    }

    /**
     * CONSTRUTOR SEM ID (para inserção)
     *
     * Usado ao criar novo método de pagamento.
     * ID será gerado automaticamente pelo banco.
     *
     * @param descricao Descrição do método
     *
     * Exemplo:
     * ```java
     * MetPag metodo = new MetPag("PayPal");
     * metPagDAO.inserir(metodo); // ID gerado pelo banco
     * ```
     */
    public MetPag(String descricao) {
        this.descricao = descricao;
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================ */

    /**
     * Retorna o ID do método de pagamento.
     *
     * @return ID único do método (PK)
     */
    public int getIdMetPag() {
        return idMetPag;
    }

    /**
     * Define o ID do método de pagamento.
     * Normalmente setado após INSERT no banco.
     *
     * @param idMetPag ID gerado pelo banco
     */
    public void setIdMetPag(int idMetPag) {
        this.idMetPag = idMetPag;
    }

    /**
     * Retorna a descrição do método de pagamento.
     *
     * @return Descrição (ex: "PIX", "Dinheiro")
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição do método de pagamento.
     *
     * IMPORTANTE: Não vazio, max 100 caracteres.
     *
     * Validação recomendada:
     * ```java
     * if (descricao == null || descricao.trim().isEmpty()) {
     *     throw new IllegalArgumentException("Descrição é obrigatória");
     * }
     * if (descricao.length() > 100) {
     *     throw new IllegalArgumentException("Descrição muito longa (max 100)");
     * }
     * metodo.setDescricao(descricao.trim());
     * ```
     *
     * @param descricao Descrição do método
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * Representação em String do método de pagamento.
     *
     * Útil para:
     * - Logs e debug
     * - <select> no JSP: ${metodo} exibirá a descrição
     * - System.out.println(metodo) mostra dados completos
     *
     * @return String com ID e descrição
     *
     * Exemplo de saída:
     * "MetPag [idMetPag=1, descricao=PIX]"
     *
     * Uso em JSP:
     * ```jsp
     * <select name="metodoPagamento">
     *     <c:forEach items="${metodos}" var="m">
     *         <option value="${m.idMetPag}">
     *             ${m.descricao} <!-- OU apenas ${m} -->
     *         </option>
     *     </c:forEach>
     * </select>
     * ```
     */
    @Override
    public String toString() {
        return "MetPag [idMetPag=" + idMetPag + ", descricao=" + descricao + "]";
    }

    /**
     * Verifica se dois métodos de pagamento são iguais.
     * Igualdade baseada apenas no ID (chave primária).
     *
     * @param o Objeto a comparar
     * @return true se mesmo ID, false caso contrário
     *
     * Exemplo:
     * ```java
     * MetPag m1 = new MetPag(1, "PIX");
     * MetPag m2 = new MetPag(1, "Outro Nome");
     * m1.equals(m2); // true (mesmo ID, descrição não importa)
     *
     * MetPag m3 = new MetPag(2, "PIX");
     * m1.equals(m3); // false (IDs diferentes)
     * ```
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetPag metPag = (MetPag) o;
        return idMetPag == metPag.idMetPag;
    }

    /**
     * Gera código hash baseado no ID.
     * Necessário ao usar equals() customizado.
     * Usado em coleções (HashSet, HashMap).
     *
     * @return Hash code do ID
     *
     * Exemplo:
     * ```java
     * Set<MetPag> metodos = new HashSet<>();
     * MetPag m1 = new MetPag(1, "PIX");
     * metodos.add(m1);
     *
     * MetPag m2 = new MetPag(1, "Dinheiro"); // Mesmo ID
     * metodos.add(m2); // Não adiciona (duplicata)
     *
     * metodos.size(); // 1 (não duplicou)
     * ```
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(idMetPag);
    }
}

/* ================================================================
   RESUMO DO MODEL
   ================================================================

   CAMPOS:
   - idMetPag   → PK, auto increment
   - descricao  → Nome do método, max 100 chars

   CONSTRUTORES:
   - ()                  → Vazio
   - (id, descricao)     → Completo (buscar)
   - (descricao)         → Sem ID (inserir)

   RELACIONAMENTOS:
   - 1:N com Pagamento (um método tem muitos pagamentos)

   MÉTODOS PADRÃO:
   1. Dinheiro
   2. Cartão de Crédito
   3. Cartão de Débito
   4. PIX
   5. Boleto
   6. Transferência Bancária
   7. Cheque
   8. Outro

   CARACTERÍSTICAS:
   - Tabela de referência (lookup)
   - Pré-populada com métodos comuns
   - Raramente alterada
   - Não tem exclusão lógica
   - Usada em dropdowns

   USO NO SISTEMA:
   - Dropdown de seleção: "Como foi pago?"
   - FK em pagamento: metodo_pagamento_id
   - Relatórios: GROUP BY metodo_pagamento

   QUERIES COMUNS:
   - SELECT * FROM metodo_pagamento (listar todos)
   - SELECT * FROM metodo_pagamento WHERE id = ?
   - INSERT INTO metodo_pagamento (descricao) VALUES (?)

   SQL DE POPULAÇÃO INICIAL:
   ```sql
   INSERT INTO metodo_pagamento (descricao) VALUES
   ('Dinheiro'),
   ('Cartão de Crédito'),
   ('Cartão de Débito'),
   ('PIX'),
   ('Boleto'),
   ('Transferência Bancária'),
   ('Cheque'),
   ('Outro');
   ```

   OBSERVAÇÕES:
   - toString() retorna "[id, descricao]"
   - equals() baseado em ID
   - hashCode() baseado em ID
   - Model simples (apenas 2 campos)
   - Não tem relacionamento com Usuario
   - Não tem campo "ativo" (não precisa)
   ================================================================ */