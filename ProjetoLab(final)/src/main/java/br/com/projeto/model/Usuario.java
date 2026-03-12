package br.com.projeto.model;

/**
 * ================================================================
 * MODEL: USUARIO
 * ================================================================
 *
 * PROPÓSITO:
 * Representa um usuário MEI no sistema.
 * Cada usuário pode ter múltiplas vendas e notas fiscais associadas.
 *
 * TABELA DO BANCO:
 * Nome: usuario
 * Colunas:
 *   - id_usuario (INT, PK, AUTO_INCREMENT)
 *   - cpf (VARCHAR, UNIQUE, NOT NULL)
 *   - nome (VARCHAR, NOT NULL)
 *   - email (VARCHAR, UNIQUE)
 *   - senha (VARCHAR, NOT NULL) - hash BCrypt
 *   - cnpj (VARCHAR) - CNPJ da empresa MEI
 *
 * RELACIONAMENTOS:
 * - 1:N com Vendas (um usuário tem muitas vendas)
 * - 1:N com NotaFiscal (um usuário tem muitas notas fiscais)
 *
 * SEGURANÇA:
 * - Senha SEMPRE armazenada com hash BCrypt
 * - CPF usado como username no login
 * - Email opcional mas único se fornecido
 */
public class Usuario {

    /* ================================================================
       ATRIBUTOS (Mapeamento 1:1 com colunas do banco)
       ================================================================ */

    /**
     * ID único do usuário (chave primária).
     * Gerado automaticamente pelo banco (AUTO_INCREMENT).
     * Usado como FK em outras tabelas (vendas, nota_fiscal).
     */
    private int idUsuario;

    /**
     * CPF do usuário (11 dígitos, sem máscara).
     * Usado como USERNAME no login.
     * UNIQUE NOT NULL no banco.
     *
     * Exemplos:
     * - "12345678910" (válido)
     * - "123.456.789-10" (remover máscara antes de salvar)
     */
    private String cpf;

    /**
     * Nome completo do usuário.
     * Exibido na topbar e relatórios.
     * NOT NULL no banco.
     */
    private String nome;

    /**
     * Email do usuário (opcional).
     * Se fornecido, deve ser UNIQUE.
     * Usado para recuperação de senha (idea futura).
     */
    private String email;

    /**
     * Senha do usuário (SEMPRE hash BCrypt).
     * NUNCA armazene senha em texto plano!
     *
     * Processo:
     * 1. User digita: "senha123"
     * 2. BCrypt.hashpw("senha123") → "$2a$10$..."
     * 3. Salva hash no banco: "$2a$10$..."
     * 4. Login verifica: BCrypt.checkpw("senha123", hash)
     *
     * Exemplo de hash:
     * "$2a$10$N9qo8uLOickgx2ZMRZoMye.IVI8VKjOyC3J7gNwXhH9oM6DYk2J6i"
     */
    private String senha;

    /**
     * CNPJ da empresa MEI (14 dígitos, sem máscara).
     * Opcional mas recomendado.
     * Usado em relatórios e notas fiscais.
     *
     * Exemplos:
     * - "12345678000190" (válido)
     * - "12.345.678/0001-90" (remover máscara)
     */
    private String cnpj;

    /* ================================================================
       CONSTRUTORES
       ================================================================

       Múltiplos construtores para diferentes casos de uso:
       1. Vazio → Para criar objeto antes de preencher
       2. Completo → Para criar usuário com todos os dados
    */

    /**
     * CONSTRUTOR VAZIO
     *
     * Uso:
     * - Criar objeto antes de preencher com dados do banco
     * - Frameworks ORM (Hibernate, JPA)
     * - Inicialização antes de setters
     *
     * Exemplo:
     * ```java
     * Usuario user = new Usuario();
     * user.setNome("João");
     * user.setCpf("12345678901");
     * ```
     */
    public Usuario() {}

    /**
     * CONSTRUTOR COMPLETO (para cadastro)
     *
     * Usado ao cadastrar novo usuário.
     * ID será gerado automaticamente pelo banco.
     *
     * @param cpf CPF sem máscara (11 dígitos)
     * @param nome Nome completo
     * @param email Email (opcional)
     * @param senha Hash BCrypt da senha
     *
     * Exemplo:
     * ```java
     * String hash = BCrypt.hashpw("senha123", BCrypt.gensalt());
     * Usuario user = new Usuario("12345678901", "João Silva", "joao@email.com", hash);
     * usuarioDAO.inserir(user);
     * ```
     */
    public Usuario(String cpf, String nome, String email, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    /* ================================================================
       GETTERS E SETTERS
       ================================================================

       Métodos de acesso aos atributos privados.
       Seguem padrão JavaBeans para compatibilidade com JSP/JSTL.
    */

    /**
     * Retorna o ID do usuário.
     *
     * @return ID único do usuário (PK)
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Define o ID do usuário.
     * Normalmente setado após INSERT no banco.
     *
     * @param idUsuario ID gerado pelo banco
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Retorna o CPF do usuário.
     *
     * @return CPF sem máscara (11 dígitos)
     */
    public String getCpf() {
        return cpf;
    }

    /**
     * Define o CPF do usuário.
     *
     * IMPORTANTE: Remova a máscara antes de chamar:
     * ```java
     * String cpfComMascara = "123.456.789-01";
     * String cpfLimpo = cpfComMascara.replaceAll("[^0-9]", "");
     * user.setCpf(cpfLimpo); // "12345678901"
     * ```
     *
     * @param cpf CPF sem máscara
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /**
     * Retorna o nome completo do usuário.
     *
     * @return Nome completo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do usuário.
     *
     * @param nome Nome completo
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna o email do usuário.
     *
     * @return Email ou null se não fornecido
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o email do usuário.
     *
     * @param email Email válido (opcional)
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retorna o hash BCrypt da senha.
     *
     * ATENÇÃO: Este método retorna o HASH, não a senha original!
     * Senha original não pode ser recuperada (one-way hash).
     *
     * @return Hash BCrypt da senha
     */
    public String getSenha() {
        return senha;
    }

    /**
     * Define o hash BCrypt da senha.
     *
     * IMPORTANTE: SEMPRE passe um hash BCrypt, NUNCA senha em texto plano!
     *
     * Correto:
     * ```java
     * String hash = BCrypt.hashpw("senha123", BCrypt.gensalt());
     * user.setSenha(hash);
     * ```
     *
     * Errado:
     * ```java
     * user.setSenha("senha123"); // NUNCA FAÇA ISSO!
     * ```
     *
     * @param senha Hash BCrypt (nunca texto plano)
     */
    public void setSenha(String senha) {
        this.senha = senha;
    }

    /**
     * Retorna o CNPJ da empresa MEI.
     *
     * @return CNPJ sem máscara (14 dígitos) ou null
     */
    public String getCnpj() {
        return cnpj;
    }

    /**
     * Define o CNPJ da empresa MEI.
     *
     * IMPORTANTE: Remova a máscara antes:
     * ```java
     * String cnpjComMascara = "12.345.678/0001-90";
     * String cnpjLimpo = cnpjComMascara.replaceAll("[^0-9]", "");
     * user.setCnpj(cnpjLimpo); // "12345678000190"
     * ```
     *
     * @param cnpj CNPJ sem máscara
     */
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    /* ================================================================
       MÉTODOS AUXILIARES
       ================================================================ */

    /**
     * Retorna representação em String do usuário.
     * Útil para logs e debug.
     *
     *
     *
     * @return String com dados do usuário
     *
     * Exemplo de saída:
     * "Usuario [id=1, nome=João Silva, cpf=12345678901, email=joao@email.com, cnpj=12345678000190]"
     */
    @Override
    public String toString() {
        return "Usuario [id=" + idUsuario +
                ", nome=" + nome +
                ", cpf=" + cpf +
                ", email=" + email +
                ", cnpj=" + cnpj + "]";
    }

    /**
     * Verifica se dois usuários são iguais.
     * Igualdade baseada apenas no ID (chave primária).
     *
     * @param o Objeto a comparar
     * @return true se mesmo ID, false caso contrário
     *
     * Exemplo:
     * ```java
     * Usuario u1 = new Usuario();
     * u1.setIdUsuario(1);
     *
     * Usuario u2 = new Usuario();
     * u2.setIdUsuario(1);
     *
     * u1.equals(u2); // true (mesmo ID)
     * ```
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return idUsuario == usuario.idUsuario;
    }

    /**
     * Gera código hash baseado no ID.
     * Necessário ao usar equals() customizado.
     *
     * @return Hash code do ID
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(idUsuario);
    }
}

/* ================================================================
   RESUMO DO MODEL
   ================================================================

   CAMPOS:
   - idUsuario  → PK, auto increment
   - cpf        → UNIQUE, usado no login
   - nome       → Nome completo
   - email      → Opcional, UNIQUE
   - senha      → Hash BCrypt (never plain text!)
   - cnpj       → CNPJ da empresa MEI

   CONSTRUTORES:
   - ()                            → Vazio
   - (cpf, nome, email, senha)     → Completo

   RELACIONAMENTOS:
   - 1:N com Vendas
   - 1:N com NotaFiscal

   SEGURANÇA:
   - Senha SEMPRE com BCrypt
   - CPF único (username)
   - Email único se fornecido

   USO NO SISTEMA:
   - Login: CPF + senha
   - Sessão: session.getAttribute("usuario")
   - FK: usuario_id em vendas e nota_fiscal

   OBSERVAÇÕES:
   - Remover máscaras antes de salvar (CPF/CNPJ)
   - Validar formato de email
   - Senha mínima 6 caracteres (validar no controller)
   ================================================================ */