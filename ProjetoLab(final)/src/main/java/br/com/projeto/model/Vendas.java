package br.com.projeto.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 * MODEL: VENDAS
 * ================================================================
 *
 * PROPÓSITO:
 * Representa uma venda realizada pelo MEI.
 * Central do sistema - conecta usuário, categoria, pagamento e nota fiscal.
 *
 * TABELA DO BANCO:
 * Nome: vendas
 * Colunas:
 *   - id_vendas (INT, PK, AUTO_INCREMENT)
 *   - data_vendas (DATETIME, NOT NULL)
 *   - valor (DECIMAL(10,2), NOT NULL)
 *   - nota_fiscal_emitida (CHAR(1), 'S' ou 'N')
 *   - categoria_id (INT, FK → categoria)
 *   - usuario_id (INT, FK → usuario)
 *   - descricao (VARCHAR, opcional)
 *   - ativo (BOOLEAN, DEFAULT true)
 *
 * RELACIONAMENTOS:
 * - N:1 com Usuario (muitas vendas pertencem a um usuário)
 * - N:1 com Categoria (muitas vendas têm uma categoria)
 * - 1:1 com NotaFiscal (venda pode ter uma nota fiscal)
 * - 1:N com Pagamento (venda pode ter vários pagamentos)
 *
 * FLUXO COMPLETO:
 * 1. User cadastra venda no dashboard
 * 2. Escolhe categoria (dropdown)
 * 3. Informa valor
 * 4. Marca se emitiu NF (checkbox)
 * 5. Se marcou NF, informa número
 * 6. Sistema salva venda + nota fiscal (se houver)
 * 7. Venda aparece no histórico
 *
 * EXCLUSÃO LÓGICA:
 * Vendas não são deletadas, apenas desativadas (ativo = false).
 * Preserva histórico completo para auditorias e relatórios anuais.
 *
 * COMPATIBILIDADE DE DATA:
 * Suporta java.util.Date E LocalDateTime para compatibilidade.
 * Banco usa DATETIME (compatível com ambos).
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class Vendas {

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único da venda (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     * Usado como FK em pagamento e nota_fiscal.
     */
    private int idVendas;

    /**
     * Data e hora da venda.
     *
     * TIPO FLEXÍVEL: Aceita java.util.Date OU LocalDateTime
     * - Date: Vindo de formulários antigos
     * - LocalDateTime: Vindo de código moderno
     *
     * Conversão automática via métodos auxiliares.
     *
     * Exemplo:
     * ```java
     * // Opção 1: Date
     * venda.setDataVendas(new Date());
     *
     * // Opção 2: LocalDateTime
     * venda.setDataVendas(LocalDateTime.now());
     *
     * // Recuperar como LocalDateTime
     * LocalDateTime data = venda.getDataVendasAsLocalDateTime();
     * ```
     */
    private Object dataVendas;

    /**
     * Valor total da venda.
     * Tipo float para compatibilidade com MySQL DECIMAL.
     *
     * IMPORTANTE: Sempre use 2 casas decimais.
     * Exemplo: 100.50 (não 100.5)
     *
     * Formatação:
     * ```java
     * DecimalFormat df = new DecimalFormat("R$ #,##0.00");
     * String valorFormatado = df.format(venda.getValor());
     * // "R$ 1.234,56"
     * ```
     */
    private float valor;

    /**
     * Indica se nota fiscal foi emitida.
     *
     * Valores aceitos:
     * - "S" → Nota fiscal emitida
     * - "N" → Sem nota fiscal
     *
     * Armazenado como CHAR(1) no banco.
     *
     * Verificação:
     * ```java
     * if ("S".equalsIgnoreCase(venda.getNotaFiscalEmitida())) {
     *     // Tem NF
     * }
     * // OU usar método auxiliar:
     * if (venda.isNotaFiscalEmitida()) {
     *     // Tem NF
     * }
     * ```
     */
    private String notaFiscalEmitida;

    /**
     * Categoria da venda (relacionamento N:1).
     *
     * Objeto completo Categoria com:
     * - idCategoria
     * - nomeCategoria
     * - ativo
     *
     * Para acessar dados:
     * ```java
     * int idCat = venda.getCategoria().getIdCategoria();
     * String nome = venda.getCategoria().getNomeCategoria();
     * // OU usar métodos auxiliares:
     * int idCat = venda.getIdCategoria();
     * String nome = venda.getNomeCategoria();
     * ```
     */
    private Categoria categoria;

    /**
     * Nota fiscal relacionada (relacionamento 1:1).
     *
     * Pode ser NULL se notaFiscalEmitida = "N".
     *
     * Se venda tem NF, este objeto contém:
     * - idNotaFiscal
     * - numero (ex: "NF-2026001")
     * - dataEmissao
     * - valor
     *
     * Verificação:
     * ```java
     * if (venda.getNotaFiscal() != null) {
     *     String numero = venda.getNotaFiscal().getNumero();
     * }
     * ```
     */
    private NotaFiscal notaFiscal;

    /**
     * ID do usuário dono da venda (FK).
     *
     * Relacionamento N:1 com Usuario.
     * Uma venda pertence a um usuário.
     * Um usuário tem muitas vendas.
     *
     * Usado em:
     * - Filtros de consulta (WHERE usuario_id = ?)
     * - Relatórios por usuário
     * - Controle de acesso
     */
    private int usuarioId;

    /**
     * Descrição opcional da venda.
     *
     * Campo livre para detalhes adicionais:
     * - "Venda de produtos de limpeza"
     * - "Consultoria em TI - 10 horas"
     * - "Revenda de celulares"
     *
     * Pode ser NULL ou vazio.
     */
    private String descricao;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       Múltiplos construtores para diferentes casos de uso:
       1. Vazio
       2. Completo (com NF)
       3. Sem ID (para inserção)
       4. Sem NF
       5. Simplificado (apenas essenciais)
       6. Rápido (data atual)
    */

    /**
     * CONSTRUTOR VAZIO
     *
     * Uso:
     * - Criar objeto antes de preencher com setters
     * - Frameworks ORM
     * - ResultSet mapping manual
     *
     * Exemplo:
     * ```java
     * Vendas venda = new Vendas();
     * venda.setDataVendas(new Date());
     * venda.setValor(100.50f);
     * venda.setUsuarioId(1);
     * ```
     */
    public Vendas() {}

    /**
     * CONSTRUTOR COMPLETO (com NF)
     *
     * Usado após buscar venda completa do banco.
     * Inclui todos os campos e relacionamentos.
     *
     * @param idVendas ID da venda (do banco)
     * @param dataVendas Data da venda
     * @param valor Valor total
     * @param notaFiscalEmitida "S" ou "N"
     * @param categoria Objeto Categoria completo
     * @param notaFiscal Objeto NotaFiscal (ou null)
     *
     * Exemplo (ResultSet):
     * ```java
     * int id = rs.getInt("id_vendas");
     * LocalDateTime data = rs.getTimestamp("data_vendas").toLocalDateTime();
     * float valor = rs.getFloat("valor");
     * String nf = rs.getString("nota_fiscal_emitida");
     * // ... buscar categoria e nota fiscal
     * Vendas venda = new Vendas(id, data, valor, nf, categoria, notaFiscal);
     * ```
     */
    public Vendas(int idVendas, LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria, NotaFiscal notaFiscal) {
        this.idVendas = idVendas;
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = notaFiscalEmitida;
        this.categoria = categoria;
        this.notaFiscal = notaFiscal;
    }

    /**
     * CONSTRUTOR SEM ID (para inserção)
     *
     * Usado ao criar nova venda para inserir no banco.
     * ID será gerado automaticamente (AUTO_INCREMENT).
     *
     * @param dataVendas Data da venda
     * @param valor Valor total
     * @param notaFiscalEmitida "S" ou "N"
     * @param categoria Objeto Categoria
     * @param notaFiscal Objeto NotaFiscal (ou null)
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria(3, "Serviços");
     * NotaFiscal nf = new NotaFiscal("NF-2026001", 150.00f);
     * Vendas venda = new Vendas(
     *     LocalDateTime.now(),
     *     150.00f,
     *     "S",
     *     cat,
     *     nf
     * );
     * vendasDAO.inserir(venda);
     * ```
     */
    public Vendas(LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria, NotaFiscal notaFiscal) {
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = notaFiscalEmitida;
        this.categoria = categoria;
        this.notaFiscal = notaFiscal;
    }

    /**
     * CONSTRUTOR SEM NOTA FISCAL
     *
     * Usado ao criar venda SEM nota fiscal.
     * Comum para vendas pequenas ou informais.
     *
     * @param dataVendas Data da venda
     * @param valor Valor total
     * @param notaFiscalEmitida "N" (sem NF)
     * @param categoria Objeto Categoria
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria(2, "Comércio");
     * Vendas venda = new Vendas(
     *     LocalDateTime.now(),
     *     50.00f,
     *     "N",
     *     cat
     * );
     * ```
     */
    public Vendas(LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria) {
        this(dataVendas, valor, notaFiscalEmitida, categoria, null);
    }

    /**
     * CONSTRUTOR SIMPLIFICADO
     *
     * Apenas valores essenciais.
     * Assume:
     * - Sem NF (notaFiscalEmitida = "N")
     * - Categoria apenas com ID
     *
     * @param dataVendas Data da venda
     * @param valor Valor total
     * @param idCategoria ID da categoria
     *
     * Exemplo:
     * ```java
     * Vendas venda = new Vendas(LocalDateTime.now(), 100.00f, 3);
     * venda.setUsuarioId(1);
     * vendasDAO.inserir(venda);
     * ```
     */
    public Vendas(LocalDateTime dataVendas, float valor, int idCategoria) {
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = "N";
        this.categoria = new Categoria(idCategoria, "");
        this.notaFiscal = null;
    }

    /**
     * CONSTRUTOR RÁPIDO (data atual)
     *
     * Cria venda com:
     * - Data/hora atual
     * - Sem NF
     * - Categoria fornecida
     *
     * @param valor Valor total
     * @param categoria Objeto Categoria
     *
     * Exemplo de uso em teste:
     * ```java
     * Categoria cat = categoriaDAO.buscar(3);
     * Vendas venda = new Vendas(150.00f, cat);
     * venda.setUsuarioId(1);
     * vendasDAO.inserir(venda);
     * ```
     */
    public Vendas(float valor, Categoria categoria) {
        this(LocalDateTime.now(), valor, "N", categoria, null);
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================ */

    /**
     * Retorna o ID da venda.
     *
     * @return ID único da venda (PK)
     */
    public int getIdVendas() {
        return idVendas;
    }

    /**
     * Define o ID da venda.
     * Normalmente setado após INSERT no banco.
     *
     * @param idVendas ID gerado pelo banco
     */
    public void setIdVendas(int idVendas) {
        this.idVendas = idVendas;
    }

    /**
     * MÉTODO DE COMPATIBILIDADE: getIdVenda()
     *
     * Alguns DAOs/Classes usam getIdVenda() em vez de getIdVendas().
     * Este método garante compatibilidade.
     *
     * @return ID da venda (mesmo que getIdVendas)
     */
    public int getIdVenda() {
        return idVendas;
    }

    /**
     * MÉTODO DE COMPATIBILIDADE: setIdVenda()
     *
     * @param idVenda ID da venda
     */
    public void setIdVenda(int idVenda) {
        this.idVendas = idVenda;
    }

    /**
     * Retorna a data da venda (tipo genérico).
     *
     * ATENÇÃO: Retorna Object (pode ser Date ou LocalDateTime).
     * Use getDataVendasAsLocalDateTime() para obter LocalDateTime sempre.
     *
     * @return Data como Object (Date ou LocalDateTime)
     */
    public Object getDataVendas() {
        return dataVendas;
    }

    /**
     * Define a data da venda.
     *
     * FLEXÍVEL: Aceita Date ou LocalDateTime.
     *
     * @param dataVendas Date ou LocalDateTime
     *
     * Exemplos:
     * ```java
     * // Opção 1: Date
     * venda.setDataVendas(new Date());
     *
     * // Opção 2: LocalDateTime
     * venda.setDataVendas(LocalDateTime.now());
     *
     * // Opção 3: Data específica
     * venda.setDataVendas(LocalDateTime.of(2026, 2, 10, 14, 30));
     * ```
     */
    public void setDataVendas(Object dataVendas) {
        this.dataVendas = dataVendas;
    }

    /**
     * MÉTODO AUXILIAR: Retorna data como LocalDateTime
     *
     * Converte automaticamente se for Date.
     * Útil para trabalhar com API moderna de datas.
     *
     * @return LocalDateTime ou null se data for null
     *
     * Exemplo:
     * ```java
     * LocalDateTime data = venda.getDataVendasAsLocalDateTime();
     * int ano = data.getYear();
     * String formatada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
     * ```
     */
    public LocalDateTime getDataVendasAsLocalDateTime() {
        if (dataVendas instanceof LocalDateTime) {
            // Já é LocalDateTime, retorna direto
            return (LocalDateTime) dataVendas;
        } else if (dataVendas instanceof java.util.Date) {
            // É Date, converte para LocalDateTime
            return new java.sql.Timestamp(((java.util.Date) dataVendas).getTime())
                    .toLocalDateTime();
        }
        return null; // Data não definida
    }

    /**
     * Retorna o valor da venda.
     *
     * @return Valor total (float)
     */
    public float getValor() {
        return valor;
    }

    /**
     * Define o valor da venda.
     *
     * @param valor Valor total (sempre positivo)
     *
     * Validação recomendada no Controller:
     * ```java
     * if (valor <= 0) {
     *     throw new IllegalArgumentException("Valor deve ser maior que zero");
     * }
     * venda.setValor(valor);
     * ```
     */
    public void setValor(float valor) {
        this.valor = valor;
    }

    /**
     * Retorna se nota fiscal foi emitida.
     *
     * @return "S" ou "N"
     */
    public String getNotaFiscalEmitida() {
        return notaFiscalEmitida;
    }

    /**
     * Define se nota fiscal foi emitida.
     *
     * @param notaFiscalEmitida "S" (sim) ou "N" (não)
     *
     * IMPORTANTE: Sempre use uppercase:
     * ```java
     * venda.setNotaFiscalEmitida(emitirNF.toUpperCase()); // "S" ou "N"
     * ```
     */
    public void setNotaFiscalEmitida(String notaFiscalEmitida) {
        this.notaFiscalEmitida = notaFiscalEmitida;
    }

    /**
     * Retorna o objeto Categoria completo.
     *
     * @return Categoria ou null
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Define a categoria da venda.
     *
     * @param categoria Objeto Categoria completo
     *
     * Exemplo:
     * ```java
     * Categoria cat = new Categoria();
     * cat.setIdCategoria(3);
     * venda.setCategoria(cat);
     * ```
     */
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Retorna o objeto NotaFiscal.
     *
     * @return NotaFiscal ou null (se não tem NF)
     */
    public NotaFiscal getNotaFiscal() {
        return notaFiscal;
    }

    /**
     * Define a nota fiscal da venda.
     *
     * @param notaFiscal Objeto NotaFiscal ou null
     *
     * Exemplo:
     * ```java
     * if ("S".equals(venda.getNotaFiscalEmitida())) {
     *     NotaFiscal nf = new NotaFiscal("NF-2026001", 150.00f);
     *     venda.setNotaFiscal(nf);
     * }
     * ```
     */
    public void setNotaFiscal(NotaFiscal notaFiscal) {
        this.notaFiscal = notaFiscal;
    }

    /**
     * Retorna o ID do usuário dono da venda.
     *
     * @return ID do usuário (FK)
     */
    public int getUsuarioId() {
        return usuarioId;
    }

    /**
     * Define o ID do usuário dono da venda.
     *
     * @param usuarioId ID do usuário
     *
     * Exemplo (pegar da sessão):
     * ```java
     * Usuario usuario = (Usuario) session.getAttribute("usuario");
     * venda.setUsuarioId(usuario.getIdUsuario());
     * ```
     */
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    /**
     * Retorna a descrição da venda.
     *
     * @return Descrição ou null/vazio
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição da venda.
     *
     * @param descricao Descrição opcional
     *
     * Exemplo:
     * ```java
     * venda.setDescricao("Consultoria em TI - 10 horas");
     * ```
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * AUXILIAR: Retorna ID da categoria.
     * Atalho para getCategoria().getIdCategoria().
     *
     * @return ID da categoria ou 0 se categoria for null
     *
     * Exemplo:
     * ```java
     * int idCat = venda.getIdCategoria();
     * // Em vez de:
     * int idCat = venda.getCategoria() != null ?
     *             venda.getCategoria().getIdCategoria() : 0;
     * ```
     */
    public int getIdCategoria() {
        return categoria != null ? categoria.getIdCategoria() : 0;
    }

    /**
     * AUXILIAR: Retorna nome da categoria.
     * Atalho para getCategoria().getNomeCategoria().
     *
     * @return Nome da categoria ou string vazia
     *
     * Exemplo (JSP):
     * ```jsp
     * <td>${venda.nomeCategoria}</td>
     * <!-- Em vez de: -->
     * <td>${venda.categoria.nomeCategoria}</td>
     * ```
     */
    public String getNomeCategoria() {
        return categoria != null ? categoria.getNomeCategoria() : "";
    }

    /**
     * AUXILIAR: Verifica se nota fiscal foi emitida.
     *
     * @return true se NF = "S", false caso contrário
     *
     * Exemplo:
     * ```java
     * if (venda.isNotaFiscalEmitida()) {
     *     // Processar NF
     * }
     * ```
     */
    public boolean isNotaFiscalEmitida() {
        return "S".equalsIgnoreCase(notaFiscalEmitida);
    }

    /**
     * Representação em String da venda.
     * Útil para logs e debug.
     *
     * @return String com dados principais
     *
     * Exemplo de saída:
     * "Vendas [idVendas=1, valor=150.0, usuarioId=1, descricao=Consultoria]"
     */
    @Override
    public String toString() {
        return "Vendas [idVendas=" + idVendas +
                ", valor=" + valor +
                ", usuarioId=" + usuarioId +
                ", descricao=" + descricao + "]";
    }
}

/* ================================================================
   RESUMO DO MODEL (continua...)
   ================================================================ */