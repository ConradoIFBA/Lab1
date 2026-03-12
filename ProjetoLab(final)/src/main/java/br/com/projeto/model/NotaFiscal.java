package br.com.projeto.model;

import java.time.LocalDateTime;

/**
 * ================================================================
 * MODEL: NOTA FISCAL
 * ================================================================
 *
 * PROPÓSITO:
 * Representa uma nota fiscal emitida para uma venda.
 * Relacionamento 1:1 com Vendas - cada NF pertence a uma única venda.
 *
 * TABELA DO BANCO:
 * Nome: nota_fiscal
 * Colunas:
 *   - id_nota_fiscal (INT, PK, AUTO_INCREMENT)
 *   - numero (VARCHAR, UNIQUE, NOT NULL)
 *   - data_emissao (DATETIME, NOT NULL)
 *   - valor (DECIMAL(10,2), NOT NULL)
 *   - vendas_id (INT, FK → vendas)
 *   - usuario_id (INT, FK → usuario)
 *
 * RELACIONAMENTOS:
 * - 1:1 com Vendas (uma NF pertence a uma venda)
 * - N:1 com Usuario (muitas NFs pertencem a um usuário)
 *
 * FLUXO DE CRIAÇÃO:
 * 1. User cadastra venda no dashboard
 * 2. Marca checkbox "Emitir Nota Fiscal"
 * 3. Informa número da NF (ex: "NF-2026001")
 * 4. Sistema cria venda E nota fiscal
 * 5. Nota fiscal aparece no histórico
 *
 * REGRAS DE NEGÓCIO:
 * - Número da NF deve ser único no sistema
 * - Data de emissão = data da venda (normalmente)
 * - Valor da NF = valor da venda (deve coincidir)
 * - Uma venda pode ter 0 ou 1 nota fiscal (nunca mais de 1)
 *
 * FORMATO DO NÚMERO:
 * Recomendado: "NF-AAAANNNN" onde:
 * - AAAA = ano (2026)
 * - NNNN = sequencial (0001, 0002, etc)
 * Exemplos: "NF-2026001", "NF-2026002", "NF-20260123"
 *
 * USO TÍPICO:
 * ```java
 * // Criar NF ao cadastrar venda
 * NotaFiscal nf = new NotaFiscal("NF-2026001", 150.00f);
 * venda.setNotaFiscal(nf);
 * vendasDAO.inserir(venda); // Insere venda + NF
 *
 * // Buscar NF de uma venda
 * Vendas venda = vendasDAO.buscar(123);
 * if (venda.getNotaFiscal() != null) {
 *     String numero = venda.getNotaFiscal().getNumero();
 * }
 * ```
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class NotaFiscal {

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único da nota fiscal (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     */
    private int idNotaFiscal;

    /**
     * Número da nota fiscal (identificador único).
     *
     * UNIQUE NOT NULL no banco.
     *
     * Formato recomendado: "NF-AAAANNNN"
     * - NF- : Prefixo fixo
     * - AAAA: Ano (2026)
     * - NNNN: Sequencial (0001, 0002, ...)
     *
     * Exemplos válidos:
     * - "NF-2026001"
     * - "NF-2026002"
     * - "NF-20260123"
     * - "2026/001" (outro formato aceito)
     *
     * Validação recomendada:
     * - Não vazio
     * - Único no sistema
     * - Máximo 50 caracteres
     *
     * Uso em relatórios e exportações.
     */
    private String numero;

    /**
     * Data e hora de emissão da nota fiscal.
     *
     * TIPO FLEXÍVEL: Aceita java.util.Date OU LocalDateTime
     * - Date: Compatibilidade com código antigo
     * - LocalDateTime: API moderna de datas
     *
     * Normalmente igual à data da venda.
     *
     * Conversão automática via getDataEmissaoAsLocalDateTime().
     *
     * Exemplo:
     * ```java
     * // Opção 1: Date
     * nf.setDataEmissao(new Date());
     *
     * // Opção 2: LocalDateTime
     * nf.setDataEmissao(LocalDateTime.now());
     *
     * // Recuperar como LocalDateTime
     * LocalDateTime data = nf.getDataEmissaoAsLocalDateTime();
     * ```
     */
    private Object dataEmissao;

    /**
     * Valor total da nota fiscal.
     *
     * Tipo float para compatibilidade com MySQL DECIMAL(10,2).
     *
     * IMPORTANTE: Deve coincidir com valor da venda!
     *
     * Validação recomendada:
     * - valor > 0
     * - valor == venda.valor (mesma quantia)
     * - 2 casas decimais
     *
     * Formatação:
     * ```java
     * DecimalFormat df = new DecimalFormat("R$ #,##0.00");
     * String valorFormatado = df.format(nf.getValor());
     * // "R$ 1.234,56"
     * ```
     */
    private float valor;

    /**
     * ID da venda relacionada (FK).
     *
     * Relacionamento 1:1 com Vendas.
     * Uma nota fiscal pertence a uma única venda.
     *
     * Setado automaticamente ao inserir venda com NF.
     *
     * Uso em queries:
     * ```sql
     * SELECT * FROM nota_fiscal WHERE vendas_id = ?
     * ```
     */
    private int vendasId;

    /**
     * ID do usuário dono da nota fiscal (FK).
     *
     * Relacionamento N:1 com Usuario.
     * Muitas notas fiscais pertencem a um usuário.
     *
     * Normalmente = venda.usuarioId.
     *
     * Usado em:
     * - Filtros de consulta
     * - Relatórios por usuário
     * - Controle de acesso
     */
    private int usuarioId;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       Múltiplos construtores para diferentes casos de uso:
       1. Vazio → Para criar objeto antes de preencher
       2. Completo → Após buscar do banco
       3. Sem ID → Para inserção (ID auto gerado)
       4. Simplificado → Inserção rápida (data atual)
    */

    /**
     * CONSTRUTOR VAZIO
     *
     * Uso:
     * - Criar objeto antes de preencher com setters
     * - Frameworks ORM (Hibernate, JPA)
     * - Inicialização antes de setar valores
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf = new NotaFiscal();
     * nf.setNumero("NF-2026001");
     * nf.setDataEmissao(new Date());
     * nf.setValor(150.00f);
     * nf.setVendasId(123);
     * nf.setUsuarioId(1);
     * ```
     */
    public NotaFiscal() {}

    /**
     * CONSTRUTOR COMPLETO
     *
     * Usado após buscar nota fiscal do banco.
     * Inclui ID e todos os campos.
     *
     * @param idNotaFiscal ID da nota fiscal (do banco)
     * @param numero Número da NF (ex: "NF-2026001")
     * @param dataEmissao Data de emissão
     * @param valor Valor total da NF
     *
     * Exemplo (ResultSet):
     * ```java
     * int id = rs.getInt("id_nota_fiscal");
     * String numero = rs.getString("numero");
     * LocalDateTime data = rs.getTimestamp("data_emissao").toLocalDateTime();
     * float valor = rs.getFloat("valor");
     *
     * NotaFiscal nf = new NotaFiscal(id, numero, data, valor);
     * nf.setVendasId(rs.getInt("vendas_id"));
     * nf.setUsuarioId(rs.getInt("usuario_id"));
     * ```
     */
    public NotaFiscal(int idNotaFiscal, String numero, LocalDateTime dataEmissao, float valor) {
        this.idNotaFiscal = idNotaFiscal;
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }

    /**
     * CONSTRUTOR SEM ID (para inserção)
     *
     * Usado ao criar nova nota fiscal para inserir no banco.
     * ID será gerado automaticamente (AUTO_INCREMENT).
     *
     * @param numero Número da NF (único)
     * @param dataEmissao Data de emissão
     * @param valor Valor total
     *
     * Exemplo (ao cadastrar venda):
     * ```java
     * NotaFiscal nf = new NotaFiscal(
     *     "NF-2026001",
     *     LocalDateTime.now(),
     *     150.00f
     * );
     * nf.setVendasId(venda.getIdVendas());
     * nf.setUsuarioId(usuario.getIdUsuario());
     *
     * notaFiscalDAO.inserir(nf);
     * ```
     */
    public NotaFiscal(String numero, LocalDateTime dataEmissao, float valor) {
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }

    /**
     * CONSTRUTOR SIMPLIFICADO (inserção rápida)
     *
     * Apenas número e valor.
     * Data de emissão = agora (LocalDateTime.now()).
     *
     * Útil para cadastro rápido de venda com NF.
     *
     * @param numero Número da NF
     * @param valor Valor total
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf = new NotaFiscal("NF-2026001", 150.00f);
     * // dataEmissao será LocalDateTime.now() automaticamente
     * ```
     */
    public NotaFiscal(String numero, float valor) {
        this(numero, LocalDateTime.now(), valor);
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================ */

    /**
     * Retorna o ID da nota fiscal.
     *
     * @return ID único da NF (PK)
     */
    public int getIdNotaFiscal() {
        return idNotaFiscal;
    }

    /**
     * Define o ID da nota fiscal.
     * Normalmente setado após INSERT no banco.
     *
     * @param idNotaFiscal ID gerado pelo banco
     */
    public void setIdNotaFiscal(int idNotaFiscal) {
        this.idNotaFiscal = idNotaFiscal;
    }

    /**
     * Retorna o número da nota fiscal.
     *
     * @return Número da NF (ex: "NF-2026001")
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Define o número da nota fiscal.
     *
     * IMPORTANTE: Deve ser ÚNICO no sistema!
     *
     * Validação recomendada:
     * ```java
     * if (numero == null || numero.trim().isEmpty()) {
     *     throw new IllegalArgumentException("Número da NF é obrigatório");
     * }
     * if (notaFiscalDAO.existeNumero(numero)) {
     *     throw new IllegalArgumentException("Número da NF já existe");
     * }
     * nf.setNumero(numero.trim().toUpperCase());
     * ```
     *
     * @param numero Número único da NF
     */
    public void setNumero(String numero) {
        this.numero = numero;
    }

    /**
     * Retorna a data de emissão (tipo genérico).
     *
     * ATENÇÃO: Retorna Object (pode ser Date ou LocalDateTime).
     * Use getDataEmissaoAsLocalDateTime() para obter LocalDateTime sempre.
     *
     * @return Data como Object (Date ou LocalDateTime)
     */
    public Object getDataEmissao() {
        return dataEmissao;
    }

    /**
     * Define a data de emissão da nota fiscal.
     *
     * FLEXÍVEL: Aceita Date ou LocalDateTime.
     *
     * @param dataEmissao Date ou LocalDateTime
     *
     * Exemplos:
     * ```java
     * // Opção 1: Date
     * nf.setDataEmissao(new Date());
     *
     * // Opção 2: LocalDateTime
     * nf.setDataEmissao(LocalDateTime.now());
     *
     * // Opção 3: Data específica
     * nf.setDataEmissao(LocalDateTime.of(2026, 2, 10, 14, 30));
     *
     * // Opção 4: Mesma data da venda
     * nf.setDataEmissao(venda.getDataVendas());
     * ```
     */
    public void setDataEmissao(Object dataEmissao) {
        this.dataEmissao = dataEmissao;
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
     * LocalDateTime data = nf.getDataEmissaoAsLocalDateTime();
     * int ano = data.getYear();
     * int mes = data.getMonthValue();
     *
     * // Formatação
     * DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
     * String formatada = data.format(fmt);
     * // "10/02/2026 14:30"
     * ```
     */
    public LocalDateTime getDataEmissaoAsLocalDateTime() {
        if (dataEmissao instanceof LocalDateTime) {
            // Já é LocalDateTime, retorna direto
            return (LocalDateTime) dataEmissao;
        } else if (dataEmissao instanceof java.util.Date) {
            // É Date, converte para LocalDateTime
            return new java.sql.Timestamp(((java.util.Date) dataEmissao).getTime())
                    .toLocalDateTime();
        }
        return null; // Data não definida
    }

    /**
     * Retorna o valor da nota fiscal.
     *
     * @return Valor total (float)
     */
    public float getValor() {
        return valor;
    }

    /**
     * Define o valor da nota fiscal.
     *
     * IMPORTANTE: Deve ser igual ao valor da venda!
     *
     * Validação recomendada:
     * ```java
     * if (valor <= 0) {
     *     throw new IllegalArgumentException("Valor deve ser maior que zero");
     * }
     * if (Math.abs(valor - venda.getValor()) > 0.01) {
     *     throw new IllegalArgumentException("Valor da NF deve ser igual ao da venda");
     * }
     * nf.setValor(valor);
     * ```
     *
     * @param valor Valor total (sempre positivo)
     */
    public void setValor(float valor) {
        this.valor = valor;
    }

    /**
     * Retorna o ID da venda relacionada.
     *
     * @return ID da venda (FK)
     */
    public int getVendasId() {
        return vendasId;
    }

    /**
     * Define o ID da venda relacionada.
     *
     * @param vendasId ID da venda
     *
     * Exemplo:
     * ```java
     * // Após inserir venda
     * vendasDAO.inserir(venda); // Gera ID
     *
     * // Criar NF vinculada
     * NotaFiscal nf = new NotaFiscal("NF-2026001", venda.getValor());
     * nf.setVendasId(venda.getIdVendas());
     * nf.setUsuarioId(venda.getUsuarioId());
     *
     * notaFiscalDAO.inserir(nf);
     * ```
     */
    public void setVendasId(int vendasId) {
        this.vendasId = vendasId;
    }

    /**
     * Retorna o ID do usuário dono da NF.
     *
     * @return ID do usuário (FK)
     */
    public int getUsuarioId() {
        return usuarioId;
    }

    /**
     * Define o ID do usuário dono da NF.
     *
     * @param usuarioId ID do usuário
     *
     * Exemplo (pegar da sessão):
     * ```java
     * Usuario usuario = (Usuario) session.getAttribute("usuario");
     * nf.setUsuarioId(usuario.getIdUsuario());
     * ```
     */
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * Representação em String da nota fiscal.
     * Útil para logs e debug.
     *
     * @return String com todos os dados da NF
     *
     * Exemplo de saída:
     * "NotaFiscal [idNotaFiscal=1, numero=NF-2026001, dataEmissao=2026-02-10T14:30, valor=150.0, vendasId=123, usuarioId=1]"
     */
    @Override
    public String toString() {
        return "NotaFiscal [idNotaFiscal=" + idNotaFiscal +
                ", numero=" + numero +
                ", dataEmissao=" + dataEmissao +
                ", valor=" + valor +
                ", vendasId=" + vendasId +
                ", usuarioId=" + usuarioId + "]";
    }

    /**
     * Verifica se duas notas fiscais são iguais.
     * Igualdade baseada apenas no ID (chave primária).
     *
     * @param o Objeto a comparar
     * @return true se mesmo ID, false caso contrário
     *
     * Exemplo:
     * ```java
     * NotaFiscal nf1 = new NotaFiscal();
     * nf1.setIdNotaFiscal(1);
     *
     * NotaFiscal nf2 = new NotaFiscal();
     * nf2.setIdNotaFiscal(1);
     *
     * nf1.equals(nf2); // true (mesmo ID)
     * ```
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotaFiscal that = (NotaFiscal) o;
        return idNotaFiscal == that.idNotaFiscal;
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
     * Set<NotaFiscal> notas = new HashSet<>();
     * NotaFiscal nf1 = new NotaFiscal();
     * nf1.setIdNotaFiscal(1);
     * notas.add(nf1);
     *
     * NotaFiscal nf2 = new NotaFiscal();
     * nf2.setIdNotaFiscal(1); // Mesmo ID
     * notas.add(nf2); // Não adiciona (duplicata)
     *
     * notas.size(); // 1 (não duplicou)
     * ```
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(idNotaFiscal);
    }
}

/* ================================================================
   RESUMO DO MODEL
   ================================================================

   CAMPOS:
   - idNotaFiscal  → PK, auto increment
   - numero        → UNIQUE, formato "NF-AAAANNNN"
   - dataEmissao   → Date ou LocalDateTime (flexível)
   - valor         → DECIMAL(10,2), = valor da venda
   - vendasId      → FK para vendas (1:1)
   - usuarioId     → FK para usuario (N:1)

   CONSTRUTORES:
   - ()                                → Vazio
   - (id, numero, data, valor)         → Completo (buscar)
   - (numero, data, valor)             → Sem ID (inserir)
   - (numero, valor)                   → Simplificado (data = now)

   RELACIONAMENTOS:
   - 1:1 com Vendas (uma NF por venda)
   - N:1 com Usuario (muitas NFs por usuário)

   REGRAS:
   - Número ÚNICO no sistema
   - Valor = valor da venda
   - Data = data da venda (normalmente)
   - Uma venda pode ter 0 ou 1 NF (nunca mais)

   FORMATO NÚMERO:
   - "NF-2026001" (recomendado)
   - "NF-20260123"
   - "2026/001" (alternativo)
   - Max 50 caracteres

   COMPATIBILIDADE DATA:
   - Aceita: Date, LocalDateTime
   - Método auxiliar: getDataEmissaoAsLocalDateTime()
   - Banco: DATETIME

   USO NO SISTEMA:
   - Checkbox no dashboard: "Emitir Nota Fiscal"
   - Campo número NF: aparece se marcado
   - Inserção: junto com venda (transação)
   - Histórico: exibe badge ✅ Sim / ❌ Não
   - Coluna "Número": mostra número da NF

   QUERIES COMUNS:
   - SELECT * FROM nota_fiscal WHERE vendas_id = ?
   - SELECT * FROM nota_fiscal WHERE usuario_id = ?
   - SELECT * FROM nota_fiscal WHERE numero = ?
   - SELECT COUNT(*) FROM nota_fiscal WHERE usuario_id = ? AND YEAR(data_emissao) = ?

   OBSERVAÇÕES:
   - toString() mostra todos os campos
   - equals() baseado em ID
   - hashCode() baseado em ID
   - getDataEmissaoAsLocalDateTime() para conversão
   ================================================================ */