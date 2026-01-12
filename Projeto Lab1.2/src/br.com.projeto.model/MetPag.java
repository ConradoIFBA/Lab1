package br.com.projeto.model;

public class MetPag {

    private int idMetPag;
    private String descricao;
    
    // Construtor vazio 
    public MetPag() {}

    // Construtor com ID 
    public MetPag(int idMetPag, String descricao) {
        this.idMetPag = idMetPag;
        this.descricao = descricao;
    }
    
    // Construtor Sem ID (auto incremento)
    public MetPag(String descricao) {
        this.descricao = descricao;
    }

    // Getters e Setters
    public int getIdMetPag() {
        return idMetPag;
    }

    public void setIdMetPag(int idMetPag) {
        this.idMetPag = idMetPag;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    /*
    // Método toString para facilitar a visualização
    @Override
    public String toString() {
        return "MetPag [idMetPag=" + idMetPag + ", descricao=" + descricao + "]";
    }
    
    // Métodos equals e hashCode baseados no ID (opcional, mas recomendado)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetPag metPag = (MetPag) o;
        return idMetPag == metPag.idMetPag;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idMetPag);
    }*/
}