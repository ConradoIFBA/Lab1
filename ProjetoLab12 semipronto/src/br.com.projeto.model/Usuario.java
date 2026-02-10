package br.com.projeto.model;

public class Usuario {
    private int idUsuario;
    private String cpf;
    private String nome;
    private String email;
    private String senha;   // hashada com BCrypt
    private String cnpj;    // CNPJ da empresa MEI
    
    // Construtores
    public Usuario() {} //Criar objeto Usuario sem inicialização
    
    public Usuario(String cpf, String nome, String email, String senha) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }
    
    // Getters e Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    @Override
    public String toString() {
        return "Usuario [id=" + idUsuario + ", nome=" + nome + ", cpf=" + cpf + "]";
    }
} 