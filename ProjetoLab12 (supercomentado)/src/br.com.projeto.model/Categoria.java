package br.com.projeto.model;

import java.io.Serializable;

/**
 * ================================================================
 * MODEL: CATEGORIA
 * ================================================================
 *
 * PROPÓSITO:
 * Representa uma categoria de venda do MEI.
 * Categorias são usadas para classificar vendas e gerar relatórios.
 *
 * TABELA DO BANCO:
 * Nome: categoria
 * Colunas:
 *   - id_categoria (INT, PK, AUTO_INCREMENT)
 *   - nome_categoria (VARCHAR, NOT NULL)
 *   - ativo (BOOLEAN, DEFAULT true)
 *
 * RELACIONAMENTOS:
 * - 1:N com Vendas (uma categoria tem muitas vendas)
 *
 * CATEGORIAS PADRÃO NO SISTEMA:
 * 3. Prestação de Serviços
 * 4. Produtos Industrializados
 * 5. Revenda de Mercadorias
 * 6. Outro (categoria genérica)
 *
 * EXCLUSÃO LÓGICA:
 * Categorias não são deletadas, apenas desativadas (ativo = false).
 * Isso preserva o histórico de vendas antigas.
 *
 * USO TÍPICO:
 * ```java
 * // Criar categoria
 * Categoria cat = new Categoria("Prestação de Serviços");
 * categoriaDAO.inserir(cat);
 *
 * // Buscar categorias ativas
 * List<Categoria> ativas = categoriaDAO.listar(); // WHERE ativo = true
 *
 * // Desativar categoria (soft delete)
 * cat.setAtivo(false);
 * categoriaDAO.editar(cat);
 * ```
 *
 * @author Sistema MEI
 */
public class Categoria implements Serializable {

    /**
     * Serial Version UID para serialização.
     * Necessário porque implementa Serializable.
     * Usado ao salvar objeto em sessão ou arquivo.
     */
    private static final long serialVersionUID = 1L;

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único da categoria (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     * Usado como FK na tabela vendas.
     */
    private int idCategoria;

    /**
     * Nome da categoria.
     * Exibido em dropdowns e relatórios.
     * NOT NULL no banco.
     *
     * Exemplos:
     * - "Prestação de Serviços"
     * - "Comércio Varejista"
     * - "Outro"
     */
    private String nomeCategoria;

    /**
     * Status da categoria (ativo/inativo).
     *
     * true  → Categoria ativa (aparece em dropdowns)
     * false → Categoria desativada (exclusão lógica)
     *
     * Vantagens da exclusão lógica:
     * - Preserva histórico de vendas antigas
     * - Permite reativar categoria se necessário
     * - Mantém integridade referencial
     *
     * SQL:
     * - Inserir: DEFAULT true
     * - Desativar: UPDATE categoria SET ativo = false WHERE id = ?
     * - Listar: SELECT * FROM categoria WHERE ativo = true
     */
    private boolean ativo;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       4 construtores para diferentes casos de uso:
       1. Vazio → Default ativo = true
       2. Com nome → Para inserção rápida
       3. Com ID e nome → Após buscar do banco
       4. Completo → Com controle de status
    */

    /**
     * CONSTRUTOR VAZIO
     *
     * Inicializa categoria com ativo = true.
     *
     * Uso:
     * - Criar objeto antes de preencher
     * - Frameworks ORM
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria();
     * cat.setNomeCategoria("Nova Categoria");
     * cat.setAtivo(true);
     * ```
     */
    public Categoria() {
        this.ativo = true; // Nova categoria sempre ativa por padrão
    }

    /**
     * CONSTRUTOR COM NOME
     *
     * Usado ao criar nova categoria para inserir no banco.
     * ID será gerado automaticamente.
     * Ativo será true por padrão.
     *
     * @param nomeCategoria Nome da categoria
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria("Prestação de Serviços");
     * categoriaDAO.inserir(cat); // ID gerado pelo banco
     * ```
     */
    public Categoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
        this.ativo = true;
    }

    /**
     * CONSTRUTOR COM ID E NOME
     *
     * Usado após buscar categoria do banco.
     * Ativo será true por padrão.
     *
     * @param idCategoria ID da categoria (do banco)
     * @param nomeCategoria Nome da categoria
     *
     * Exemplo (ResultSet):
     * ```java
     * int id = rs.getInt("id_categoria");
     * String nome = rs.getString("nome_categoria");
     * Categoria cat = new Categoria(id, nome);
     * ```
     */
    public Categoria(int idCategoria, String nomeCategoria) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
        this.ativo = true;
    }

    /**
     * CONSTRUTOR COMPLETO
     *
     * Usado quando precisa controlar status (ativo/inativo).
     * Útil ao buscar do banco todas as categorias (incluindo inativas).
     *
     * @param idCategoria ID da categoria
     * @param nomeCategoria Nome da categoria
     * @param ativo Status (true = ativo, false = inativo)
     *
     * Exemplo (ResultSet com status):
     * ```java
     * int id = rs.getInt("id_categoria");
     * String nome = rs.getString("nome_categoria");
     * boolean ativo = rs.getBoolean("ativo");
     * Categoria cat = new Categoria(id, nome, ativo);
     * ```
     */
    public Categoria(int idCategoria, String nomeCategoria, boolean ativo) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
        this.ativo = ativo;
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================ */

    /**
     * Retorna o ID da categoria.
     *
     * @return ID único da categoria (PK)
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    /**
     * Define o ID da categoria.
     * Normalmente setado após INSERT no banco.
     *
     * @param idCategoria ID gerado pelo banco
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * Retorna o nome da categoria.
     *
     * @return Nome da categoria
     */
    public String getNomeCategoria() {
        return nomeCategoria;
    }

    /**
     * Define o nome da categoria.
     *
     * @param nomeCategoria Nome da categoria
     */
    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    /**
     * Verifica se a categoria está ativa.
     * Padrão JavaBeans para boolean (is + nome).
     *
     * @return true se ativa, false se desativada
     *
     * Exemplo:
     * ```java
     * if (categoria.isAtivo()) {
     *     // Mostrar no dropdown
     * }
     * ```
     */
    public boolean isAtivo() {
        return ativo;
    }

    /**
     * Retorna status da categoria.
     * Método alternativo (get + nome).
     * Ambos isAtivo() e getAtivo() retornam o mesmo valor.
     *
     * @return true se ativa, false se desativada
     */
    public boolean getAtivo() {
        return ativo;
    }

    /**
     * Define o status da categoria.
     *
     * @param ativo true para ativar, false para desativar
     *
     * Exemplo de desativação (exclusão lógica):
     * ```java
     * Categoria cat = categoriaDAO.buscar(idCategoria);
     * cat.setAtivo(false);
     * categoriaDAO.editar(cat);
     * // SQL: UPDATE categoria SET ativo = false WHERE id = ?
     * ```
     */
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * Retorna o nome da categoria.
     *
     * Este método é especialmente útil para:
     * - <select> no JSP: ${categoria} exibirá o nome
     * - Logs: System.out.println(categoria) mostra nome
     *
     * @return Nome da categoria
     *
     * Exemplo de uso em JSP:
     * ```jsp
     * <c:forEach items="${categorias}" var="cat">
     *     <option value="${cat.idCategoria}">
     *         ${cat} <!-- Chama toString(), exibe nome -->
     *     </option>
     * </c:forEach>
     * ```
     */
    @Override
    public String toString() {
        return nomeCategoria;
    }

    /**
     * Verifica se duas categorias são iguais.
     * Igualdade baseada apenas no ID (chave primária).
     *
     * @param o Objeto a comparar
     * @return true se mesmo ID, false caso contrário
     *
     * Exemplo:
     * ```java
     * Categoria c1 = new Categoria(1, "Serviços");
     * Categoria c2 = new Categoria(1, "Outro Nome");
     * c1.equals(c2); // true (mesmo ID, nome não importa)
     * ```
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Categoria categoria = (Categoria) o;

        return idCategoria == categoria.idCategoria;
    }

    /**
     * Gera código hash baseado no ID.
     * Necessário ao usar equals() customizado.
     * Usado em HashSet, HashMap, etc.
     *
     * @return Hash code do ID
     *
     * Exemplo:
     * ```java
     * Set<Categoria> categorias = new HashSet<>();
     * categorias.add(new Categoria(1, "A"));
     * categorias.add(new Categoria(1, "B")); // Não adiciona (mesmo ID)
     * categorias.size(); // 1
     * ```
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    }
}

/* ================================================================
   RESUMO DO MODEL
   ================================================================

   CAMPOS:
   - idCategoria     → PK, auto increment
   - nomeCategoria   → Nome exibido
   - ativo           → Status (exclusão lógica)

   CONSTRUTORES:
   - ()                              → Vazio (ativo = true)
   - (nome)                          → Inserção rápida
   - (id, nome)                      → Após buscar do banco
   - (id, nome, ativo)               → Completo

   CATEGORIAS PADRÃO:
   1. Comércio Atacadista
   2. Comércio Varejista
   3. Prestação de Serviços
   4. Produtos Industrializados
   5. Revenda de Mercadorias
   6. Outro

   EXCLUSÃO LÓGICA:
   - Não usa DELETE
   - Usa UPDATE categoria SET ativo = false
   - Preserva histórico de vendas

   USO NO SISTEMA:
   - Dropdown de seleção: WHERE ativo = true
   - FK em vendas: categoria_id
   - Relatórios: GROUP BY categoria

   SERIALIZABLE:
   - Pode ser salva em sessão
   - Pode ser enviada via RMI/EJB
   - serialVersionUID = 1L

   OBSERVAÇÕES:
   - toString() retorna apenas nome (útil em JSPs)
   - equals() baseado em ID (permite comparação)
   - hashCode() baseado em ID (para coleções)
   ================================================================ */