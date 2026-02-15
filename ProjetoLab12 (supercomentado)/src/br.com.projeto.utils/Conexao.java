package br.com.projeto.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * ================================================================
 * CONEXAO - Gerenciador de Conexões MySQL
 * ================================================================
 *
 * PROPÓSITO:
 * Fornece conexões únicas com o banco MySQL para os DAOs.
 * Padrão Singleton implícito (métodos estáticos).
 *
 * USO:
 * try (Connection conn = Conexao.getConnection()) {
 *     // Usar conn aqui
 * } // Fecha automaticamente
 *
 * CONFIGURAÇÃO:
 * - URL: jdbc:mysql://localhost:3306/MEI
 * - User: root
 * - Password: (vazio para XAMPP)
 *
 * SEGURANÇA:
 * ⚠️ Em produção, usar variáveis de ambiente para credenciais!
 * ⚠️ Nunca commitar senhas no código!
 *
 * @author Sistema MEI
 * @version 2.0
 */
public class Conexao {

    // ========== CONFIGURAÇÕES DO BANCO ==========
    // IMPORTANTE: Ajustar conforme seu ambiente

    /**
     * URL de conexão JDBC
     * Formato: jdbc:mysql://[host]:[porta]/[database]
     *
     * PRODUÇÃO: Usar variável de ambiente
     * System.getenv("DB_URL")
     */
    private static final String URL = "jdbc:mysql://localhost:3306/MEI";

    /**
     * Usuário do banco
     * XAMPP default: root
     *
     * PRODUÇÃO: Criar usuário específico
     * GRANT ALL ON mei.* TO 'mei_user'@'localhost';
     */
    private static final String USER = "root";

    /**
     * Senha do banco
     * XAMPP default: sem senha (vazio)
     *
     * PRODUÇÃO: Senha forte obrigatória!
     * Usar: System.getenv("DB_PASSWORD")
     */
    private static final String PASSWORD = "";

    // ========== BLOCO ESTÁTICO - REGISTRA DRIVER ==========
    // Executado uma vez quando classe é carregada
    static {
        try {
            // Registra driver MySQL JDBC
            // Necessário para JDBC funcionar
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL carregado com sucesso!");

        } catch (ClassNotFoundException e) {
            // Driver não encontrado no classpath
            System.err.println("❌ ERRO CRÍTICO: Driver MySQL não encontrado!");
            System.err.println("   Adicione mysql-connector-java ao projeto");
            e.printStackTrace();

            // Aplicação não funciona sem driver
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * ================================================================
     * OBTER CONEXÃO COM O BANCO
     * ================================================================
     *
     * Retorna uma nova conexão MySQL.
     * IMPORTANTE: Sempre fechar a conexão após uso!
     *
     * Padrão recomendado (try-with-resources):
     * <pre>
     * try (Connection conn = Conexao.getConnection()) {
     *     PreparedStatement stmt = conn.prepareStatement("SELECT * FROM usuario");
     *     ResultSet rs = stmt.executeQuery();
     *     // Processar rs...
     * } // conn fecha automaticamente aqui
     * </pre>
     *
     * @return Connection ativa e pronta para uso
     * @throws SQLException se não conseguir conectar
     *
     * POSSÍVEIS ERROS:
     * - Communications link failure: MySQL não está rodando
     * - Access denied: Usuário/senha incorretos
     * - Unknown database: Banco 'MEI' não existe
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Tenta estabelecer conexão
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            // Log de sucesso (opcional, remover em produção)
            System.out.println("✅ Conexão estabelecida: " + URL);

            return conn;

        } catch (SQLException e) {
            // Log do erro específico
            System.err.println("❌ ERRO ao conectar ao MySQL:");
            System.err.println("   URL: " + URL);
            System.err.println("   User: " + USER);
            System.err.println("   Erro: " + e.getMessage());

            // Dicas baseadas no erro
            if (e.getMessage().contains("Communications link failure")) {
                System.err.println("   💡 Verifique se MySQL está rodando!");

            } else if (e.getMessage().contains("Access denied")) {
                System.err.println("   💡 Verifique usuário e senha!");

            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("   💡 Crie o banco: CREATE DATABASE MEI;");
            }

            // Propaga exceção para quem chamou
            throw e;
        }
    }

    /**
     * ================================================================
     * TESTE DE CONEXÃO (main)
     * ================================================================
     *
     * Executa teste simples de conexão.
     * Útil para verificar configurações antes de rodar aplicação.
     *
     * Como usar:
     * 1. Run As → Java Application
     * 2. Verificar console
     * 3. Se "✅ Conexão bem-sucedida!", está OK
     *
     * CHECKLIST:
     * - [ ] MySQL rodando (XAMPP Control Panel)
     * - [ ] Banco 'MEI' criado
     * - [ ] URL/User/Password corretos
     * - [ ] Driver mysql-connector-java no projeto
     */
    public static void main(String[] args) {
        System.out.println("========== TESTE DE CONEXÃO ==========");

        try {
            // Tenta obter conexão
            Connection conn = getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexão bem-sucedida!");
                System.out.println("   Banco: " + conn.getCatalog());
                System.out.println("   Timeout: " + conn.getNetworkTimeout() + "ms");

                // Fecha conexão de teste
                conn.close();
                System.out.println("✅ Conexão fechada corretamente");

            } else {
                System.err.println("❌ Conexão retornou null!");
            }

        } catch (Exception e) {
            System.err.println("❌ FALHA NA CONEXÃO:");
            System.err.println("   " + e.getMessage());
            System.err.println("\n💡 SOLUÇÕES:");
            System.err.println("   1. Inicie MySQL no XAMPP");
            System.err.println("   2. Crie o banco: CREATE DATABASE MEI;");
            System.err.println("   3. Verifique URL/User/Password");
            System.err.println("   4. Adicione mysql-connector-java ao projeto");
        }

        System.out.println("======================================");
    }
}

/* ================================================================
   CONFIGURAÇÕES ALTERNATIVAS
   ================================================================

   // MYSQL LOCAL (senha definida):
   private static final String URL = "jdbc:mysql://localhost:3306/MEI";
   private static final String USER = "root";
   private static final String PASSWORD = "sua_senha_aqui";

   // MYSQL REMOTO (servidor externo):
   private static final String URL = "jdbc:mysql://192.168.1.100:3306/MEI";
   private static final String USER = "mei_user";
   private static final String PASSWORD = "senha_forte_123";

   // COM SSL (segurança extra):
   private static final String URL =
       "jdbc:mysql://localhost:3306/MEI?useSSL=true&requireSSL=true";

   // COM TIMEZONE (evita warnings):
   private static final String URL =
       "jdbc:mysql://localhost:3306/MEI?serverTimezone=America/Sao_Paulo";

   // PRODUÇÃO (variáveis de ambiente):
   private static final String URL = System.getenv("DB_URL");
   private static final String USER = System.getenv("DB_USER");
   private static final String PASSWORD = System.getenv("DB_PASSWORD");

   ================================================================ */

/* ================================================================
   TROUBLESHOOTING COMUM
   ================================================================

   ERRO: ClassNotFoundException com.mysql.cj.jdbc.Driver
   SOLUÇÃO: Adicionar mysql-connector-java ao projeto

   ERRO: Communications link failure
   SOLUÇÃO: Iniciar MySQL no XAMPP Control Panel

   ERRO: Access denied for user 'root'@'localhost'
   SOLUÇÃO: Verificar senha do MySQL

   ERRO: Unknown database 'MEI'
   SOLUÇÃO: CREATE DATABASE MEI; no MySQL

   ERRO: The server time zone value is unrecognized
   SOLUÇÃO: Adicionar ?serverTimezone=UTC na URL

   ================================================================ */

/*package br.com.projeto.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexao {
	
    private static final String URL = "jdbc:mysql://localhost:3306/conrado";
    private static final String USUARIO = "root";
    private static final String SENHA = "123546";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Erro na conexão com o banco de dados", e);
        }
    }
    /* para testar a conexão após instalar o Apache na maquina e ter criado uma new server no Eclipse, sem ter implementado código ainda, 
     * basta inicar o TomCat no Eclipse e tirar o comentário abaixo e rodar a classe. Daí, estará confirmando se a conexão com o banco foi realizada. 
     * Só não esquece de iniciar o serviço do MySQL no services do Windows.*/
     
   /*
    public static void main(String[] args) {
        try {
            Connection conexao = getConnection();
            if (conexao != null) {
                System.out.println("Conexão bem-sucedida!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


