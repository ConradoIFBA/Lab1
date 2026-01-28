package br.com.projeto.model;

public class Categoria {
	
    private int idCategoria;
    private String nomeCategoria;
    
    // Construtor vazio
    public Categoria() {}
    
    // Construtor sem a chave primária 
    public Categoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
        // idCategoria será gerado automaticamente pelo banco de dados
    }
    
    // Construtor completo 
    public Categoria(int idCategoria, String nomeCategoria) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
    }

    // Getters e Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }
    
    /*
     // Método toString para facilitar a visualização
     
    @Override
    public String toString() {
        return "Categoria [idCategoria=" + idCategoria + ", nomeCategoria=" + nomeCategoria + "]";
    }
    
    // Métodos equals e hashCode baseados no ID (opcional, mas recomendado)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return idCategoria == categoria.idCategoria;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    } */
}