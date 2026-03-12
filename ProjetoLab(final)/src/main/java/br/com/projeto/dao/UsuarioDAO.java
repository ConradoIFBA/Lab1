package br.com.projeto.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.projeto.model.Usuario;

/**
 * ================================================================
 * DAO: USUARIO
 * ================================================================
 *
 * PROPÓSITO:
 * Gerencia operações de banco de dados para usuários MEI.
 *
 * MÉTODOS:
 * - inserir(Usuario)           → Cadastra novo usuário
 * - buscarPorCpf(String)       → Busca usuário por CPF (login)
 * - buscarPorEmail(String)     → Busca usuário por email
 * - buscarPorId(int)           → Busca usuário por ID
 * - listar()                   → Lista todos os usuários
 * - editar(Usuario)            → Atualiza dados do usuário
 * - excluir(int)               → Exclui usuário (física ou lógica)
 *
 * TABELA:
 * Nome: usuario
 * Colunas: id_usuario, cpf, nome, email, cnpj, senha
 *
 * SEGURANÇA:
 * - PreparedStatement (previne SQL injection)
 * - Senha SEMPRE como hash BCrypt
 * - CPF e email únicos
 *
 * @author Sistema MEI
 * @version 2.0 - Super comentado
 */
public class UsuarioDAO {

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
     *     UsuarioDAO dao = new UsuarioDAO(conn);
     *     // usar dao...
     * }
     * ```
     */
    public UsuarioDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /* ================================================================
       MÉTODO 1: INSERIR - Cadastra novo usuário
       ================================================================

       Usado por: CadastroController

       Fluxo:
       1. Recebe objeto Usuario com dados
       2. Executa INSERT no banco
       3. Retorna ID gerado no objeto

       IMPORTANTE:
       - Senha deve vir HASH BCrypt (não texto plano)
       - CPF deve ser único (verificar antes)
       - Email deve ser único se fornecido
       - CNPJ é opcional (pode ser null)
    */

    /**
     * Insere novo usuário no banco.
     *
     * @param usuario Objeto Usuario com dados
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * Usuario user = new Usuario();
     * user.setCpf("12345678901");
     * user.setNome("João Silva");
     * user.setEmail("joao@email.com");
     * user.setCnpj("12345678000190");  // Opcional
     * user.setSenha(BCrypt.hashpw("senha123", BCrypt.gensalt()));
     *
     * usuarioDAO.inserir(user);
     * System.out.println("ID gerado: " + user.getIdUsuario());
     * ```
     */
    public void inserir(Usuario usuario) throws SQLException {

        // ========== SQL COM CNPJ ==========
        String sql = "INSERT INTO usuario (cpf, nome, email, cnpj, senha) " +
                "VALUES (?, ?, ?, ?, ?)";

        System.out.println("⏳ Inserindo usuário no banco...");

        try (PreparedStatement stmt = conexao.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            // ========== SETAR PARÂMETROS ==========
            stmt.setString(1, usuario.getCpf());     // CPF (obrigatório)
            stmt.setString(2, usuario.getNome());    // Nome (obrigatório)
            stmt.setString(3, usuario.getEmail());   // Email (opcional, pode ser null)
            stmt.setString(4, usuario.getCnpj());    // CNPJ (opcional, pode ser null) ⭐
            stmt.setString(5, usuario.getSenha());   // Senha hash BCrypt (obrigatório)

            // ========== EXECUTAR INSERT ==========
            int linhasAfetadas = stmt.executeUpdate();

            System.out.println("✅ Linhas inseridas: " + linhasAfetadas);

            // ========== PEGAR ID GERADO ==========
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);
                usuario.setIdUsuario(idGerado);
                System.out.println("✅ ID gerado: " + idGerado);
            }
        }
    }

    /* ================================================================
       MÉTODO 2: BUSCAR POR CPF - Login
       ================================================================

       Usado por: LoginController, CadastroController

       Fluxo:
       1. Recebe CPF (11 dígitos sem máscara)
       2. Busca no banco
       3. Retorna Usuario completo ou null

       IMPORTANTE:
       - CPF sem máscara: "12345678901"
       - Retorna null se não encontrar
       - Inclui CNPJ no resultado
    */

    /**
     * Busca usuário por CPF.
     *
     * @param cpf CPF sem máscara (11 dígitos)
     * @return Usuario ou null se não encontrado
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * Usuario user = usuarioDAO.buscarPorCpf("12345678901");
     * if (user != null) {
     *     System.out.println("Usuário: " + user.getNome());
     *     System.out.println("CNPJ: " + user.getCnpj());
     * } else {
     *     System.out.println("CPF não encontrado");
     * }
     * ```
     */
    public Usuario buscarPorCpf(String cpf) throws SQLException {

        String sql = "SELECT * FROM usuario WHERE cpf = ?";

        System.out.println("⏳ Buscando usuário por CPF: " + cpf);

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setString(1, cpf);

            // ========== EXECUTAR QUERY ==========
            ResultSet rs = stmt.executeQuery();

            // ========== PROCESSAR RESULTADO ==========
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setCnpj(rs.getString("cnpj"));  // ⭐ CNPJ incluído

                System.out.println("✅ Usuário encontrado: " + usuario.getNome());
                return usuario;
            }

            System.out.println("❌ CPF não encontrado");
            return null;
        }
    }

    /* ================================================================
       MÉTODO 3: BUSCAR POR EMAIL - Verificação de duplicata
       ================================================================

       Usado por: CadastroController

       Fluxo:
       1. Recebe email
       2. Busca no banco
       3. Retorna Usuario ou null

       IMPORTANTE:
       - Usado para validar email único
       - Retorna null se não encontrar
    */

    /**
     * Busca usuário por email.
     *
     * @param email Email do usuário
     * @return Usuario ou null se não encontrado
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * Usuario user = usuarioDAO.buscarPorEmail("joao@email.com");
     * if (user != null) {
     *     System.out.println("Email já cadastrado!");
     * } else {
     *     System.out.println("Email disponível");
     * }
     * ```
     */
    public Usuario buscarPorEmail(String email) throws SQLException {

        String sql = "SELECT * FROM usuario WHERE email = ?";

        System.out.println("⏳ Buscando usuário por email: " + email);

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setString(1, email);

            // ========== EXECUTAR QUERY ==========
            ResultSet rs = stmt.executeQuery();

            // ========== PROCESSAR RESULTADO ==========
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setCnpj(rs.getString("cnpj"));  // ⭐ CNPJ incluído

                System.out.println("✅ Usuário encontrado: " + usuario.getNome());
                return usuario;
            }

            System.out.println("❌ Email não encontrado");
            return null;
        }
    }

    /* ================================================================
       MÉTODO 4: BUSCAR POR ID - Busca específica
       ================================================================

       Usado por: Controllers gerais

       Fluxo:
       1. Recebe ID
       2. Busca no banco
       3. Retorna Usuario ou null
    */

    /**
     * Busca usuário por ID.
     *
     * @param idUsuario ID do usuário
     * @return Usuario ou null se não encontrado
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * Usuario user = usuarioDAO.buscarPorId(1);
     * if (user != null) {
     *     System.out.println("Nome: " + user.getNome());
     * }
     * ```
     */
    public Usuario buscarPorId(int idUsuario) throws SQLException {

        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        System.out.println("⏳ Buscando usuário por ID: " + idUsuario);

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, idUsuario);

            // ========== EXECUTAR QUERY ==========
            ResultSet rs = stmt.executeQuery();

            // ========== PROCESSAR RESULTADO ==========
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setCnpj(rs.getString("cnpj"));  // ⭐ CNPJ incluído

                System.out.println("✅ Usuário encontrado: " + usuario.getNome());
                return usuario;
            }

            System.out.println("❌ ID não encontrado");
            return null;
        }
    }

    /* ================================================================
       MÉTODO 5: LISTAR - Todos os usuários
       ================================================================

       Usado por: Administração, relatórios

       Retorna lista com TODOS os usuários.
    */

    /**
     * Lista todos os usuários.
     *
     * @return Lista de usuários (vazia se nenhum)
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * List<Usuario> usuarios = usuarioDAO.listar();
     * for (Usuario u : usuarios) {
     *     System.out.println(u.getNome() + " - " + u.getCpf());
     * }
     * ```
     */
    public List<Usuario> listar() throws SQLException {

        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY nome";

        System.out.println("⏳ Listando todos os usuários...");

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // ========== PROCESSAR RESULTADOS ==========
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setCnpj(rs.getString("cnpj"));  // ⭐ CNPJ incluído

                usuarios.add(usuario);
            }
        }

        System.out.println("✅ Total de usuários: " + usuarios.size());
        return usuarios;
    }

    /* ================================================================
       MÉTODO 6: EDITAR - Atualizar dados
       ================================================================

       Usado por: Perfil, administração

       Atualiza todos os campos exceto ID.

       IMPORTANTE:
       - Não altera CPF (é chave única)
       - Senha deve vir hash BCrypt se alterada
    */

    /**
     * Edita dados do usuário.
     *
     * @param usuario Objeto Usuario com dados atualizados
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * Usuario user = usuarioDAO.buscarPorId(1);
     * user.setNome("Novo Nome");
     * user.setCnpj("98765432000199");  // Atualizar CNPJ
     * usuarioDAO.editar(user);
     * ```
     */
    public void editar(Usuario usuario) throws SQLException {

        // ========== SQL COM CNPJ ==========
        String sql = "UPDATE usuario SET " +
                "nome = ?, " +
                "email = ?, " +
                "cnpj = ?, " +    // ⭐ CNPJ incluído
                "senha = ? " +
                "WHERE id_usuario = ?";

        System.out.println("⏳ Editando usuário ID: " + usuario.getIdUsuario());

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETROS ==========
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCnpj());  // ⭐ CNPJ
            stmt.setString(4, usuario.getSenha());
            stmt.setInt(5, usuario.getIdUsuario());

            // ========== EXECUTAR UPDATE ==========
            int linhasAfetadas = stmt.executeUpdate();

            System.out.println("✅ Linhas atualizadas: " + linhasAfetadas);
        }
    }

    /* ================================================================
       MÉTODO 7: EXCLUIR - Exclusão física
       ================================================================

       ATENÇÃO: Este método faz DELETE permanente!

       Considere usar exclusão lógica (campo ativo) em produção.
    */

    /**
     * Exclui usuário PERMANENTEMENTE.
     *
     * ⚠️ ATENÇÃO: Exclusão física (DELETE)!
     * Use com cuidado. Considere soft delete em produção.
     *
     * @param idUsuario ID do usuário a excluir
     * @throws SQLException Se erro no banco
     *
     * Exemplo:
     * ```java
     * usuarioDAO.excluir(1);  // CUIDADO: Exclusão permanente!
     * ```
     */
    public void excluir(int idUsuario) throws SQLException {

        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        System.out.println("⚠️ Excluindo usuário ID: " + idUsuario);

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {

            // ========== SETAR PARÂMETRO ==========
            stmt.setInt(1, idUsuario);

            // ========== EXECUTAR DELETE ==========
            int linhasAfetadas = stmt.executeUpdate();

            System.out.println("✅ Linhas excluídas: " + linhasAfetadas);
        }
    }
}

/* ================================================================
   RESUMO DO DAO
   ================================================================

   MÉTODOS PRINCIPAIS:
   1. inserir(Usuario)        → INSERT (com CNPJ)
   2. buscarPorCpf(String)    → SELECT WHERE cpf (para login)
   3. buscarPorEmail(String)  → SELECT WHERE email (validação)
   4. buscarPorId(int)        → SELECT WHERE id
   5. listar()                → SELECT * (todos)
   6. editar(Usuario)         → UPDATE (com CNPJ)
   7. excluir(int)            → DELETE (físico)

   COMPATIBILIDADE:
   ✅ CadastroController (inserir, buscarPorCpf, buscarPorEmail)
   ✅ LoginController (buscarPorCpf)
   ✅ CNPJ em todos os métodos

   SEGURANÇA:
   ✅ PreparedStatement (SQL injection safe)
   ✅ Senha como hash BCrypt
   ✅ Validação de unicidade (CPF, email)

   LOGS:
   ✅ Cada método tem logs informativos
   ✅ Sucesso (✅) e erro (❌) claros

   CNPJ:
   ✅ inserir() - salva CNPJ
   ✅ buscarPorCpf() - retorna CNPJ
   ✅ buscarPorEmail() - retorna CNPJ
   ✅ buscarPorId() - retorna CNPJ
   ✅ listar() - retorna CNPJ
   ✅ editar() - atualiza CNPJ

   OBSERVAÇÕES:
   - Conexão passada no construtor
   - Métodos throws SQLException
   - Não fecha conexão (responsabilidade do caller)
   - PreparedStatement fechado com try-with-resources
   ================================================================ */