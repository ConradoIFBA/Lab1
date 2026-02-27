package br.com.projeto.test;

import java.sql.Connection;
import java.util.List;
import br.com.projeto.dao.CategoriaDAO;
import br.com.projeto.model.Categoria;
import br.com.projeto.utils.Conexao;

/**
 * Teste para verificar se CategoriaDAO está funcionando
 * Execute esta classe para testar a conexão e listagem de categorias
 */
public class TesteCategoriaDAO {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TESTE: CategoriaDAO");
        System.out.println("========================================\n");

        try {
            // Teste 1: Conexão
            System.out.println("1️⃣ Testando conexão com banco...");
            Connection conexao = Conexao.getConnection();
            System.out.println("✅ Conexão estabelecida!");
            System.out.println("   URL: jdbc:mysql://localhost:3306/MEI");
            System.out.println("   Fechada: " + conexao.isClosed());
            System.out.println();

            // Teste 2: CategoriaDAO
            System.out.println("2️⃣ Testando CategoriaDAO.listar()...");
            CategoriaDAO dao = new CategoriaDAO(conexao);
            System.out.println("✅ CategoriaDAO criado!");
            System.out.println();

            // Teste 3: Listar categorias
            System.out.println("3️⃣ Buscando categorias do banco...");
            List<Categoria> categorias = dao.listar();
            System.out.println("✅ Consulta executada!");
            System.out.println("   Total de categorias: " + categorias.size());
            System.out.println();

            // Teste 4: Exibir categorias
            if (categorias.isEmpty()) {
                System.out.println("⚠️ PROBLEMA: Nenhuma categoria encontrada!");
                System.out.println("   Verifique se o banco tem dados.");
            } else {
                System.out.println("4️⃣ Lista de categorias encontradas:");
                System.out.println("----------------------------------------");
                for (Categoria cat : categorias) {
                    System.out.println("   ID: " + cat.getIdCategoria());
                    System.out.println("   Nome: " + cat.getNomeCategoria());
                    System.out.println("   Ativo: " + cat.isAtivo());
                    System.out.println("   --------");
                }
            }
            System.out.println();

            // Fechar conexão
            conexao.close();
            System.out.println("✅ Conexão fechada com sucesso!");
            System.out.println();

            System.out.println("========================================");
            System.out.println("RESULTADO: TESTE CONCLUÍDO COM SUCESSO!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n❌ ERRO NO TESTE!");
            System.err.println("Tipo: " + e.getClass().getName());
            System.err.println("Mensagem: " + e.getMessage());
            System.err.println("\nStack Trace:");
            e.printStackTrace();

            System.err.println("\n========================================");
            System.err.println("POSSÍVEIS CAUSAS:");
            System.err.println("========================================");
            System.err.println("1. MySQL não está rodando");
            System.err.println("2. Banco 'MEI' não existe");
            System.err.println("3. Tabela 'categoria' não existe");
            System.err.println("4. Credenciais incorretas em Conexao.java");
            System.err.println("5. Driver MySQL não está no classpath");
            System.err.println("========================================");
        }
    }
}