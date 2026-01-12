
package br.com.projeto.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    // Configurações do banco 
    private static final String URL = "jdbc:mysql://localhost:3306/MEI";
    private static final String USER = "root"; 
    private static final String PASSWORD = "123546";  
    
    static {
        try {
            // Registrar o driver do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL registrado com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("ERRO: Driver MySQL não encontrado!");
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão estabelecida com: " + URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("ERRO ao conectar ao MySQL: " + e.getMessage());
            System.err.println("URL: " + URL);
            System.err.println("User: " + USER);
            throw e;
        }
    }
    
    // Método de teste rápido
    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("✅ Conexão bem-sucedida!");
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Falha na conexão: " + e.getMessage());
        }
    }
}





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

}
*/