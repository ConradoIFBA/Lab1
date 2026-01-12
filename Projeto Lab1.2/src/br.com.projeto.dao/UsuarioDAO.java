package br.com.projeto.dao;

import java.sql.*;
import br.com.projeto.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDAO {
    private Connection conexao;
    
    public UsuarioDAO(Connection conexao) {
        this.conexao = conexao;
    }
    
    // INSERIR novo usuário
    public boolean inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO Usuario (cpf, nome, email, senha) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            // Hash da senha antes de salvar
            String hashSenha = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt(12));
            
            stmt.setString(1, usuario.getCpf());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, hashSenha);
            
            int linhas = stmt.executeUpdate();
            return linhas > 0;
        }
    }
    
    // BUSCAR por CPF (para login)
    public Usuario buscarPorCPF(String cpf) throws SQLException {
        String sql = "SELECT * FROM Usuario WHERE cpf = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setCpf(rs.getString("cpf"));
                    usuario.setNome(rs.getString("nome"));
                    usuario.setEmail(rs.getString("email"));
                    usuario.setSenha(rs.getString("senha")); // Já vem com hash
                    return usuario;
                }
            }
        }
        return null;
    }
    
    // VERIFICAR login
    public boolean verificarLogin(String cpf, String senha) throws SQLException {
        Usuario usuario = buscarPorCPF(cpf);
        if (usuario == null) return false;
        
        // Compara senha com hash usando BCrypt
        return BCrypt.checkpw(senha, usuario.getSenha());
    }
    
    // VERIFICAR se CPF já existe
    public boolean cpfExiste(String cpf) throws SQLException {
        String sql = "SELECT 1 FROM Usuario WHERE cpf = ?";
        
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}