package br.com.projeto.model;

import java.io.Serializable;

/*
 Modelo de Categoria
 Mapeia para a tabela: categoria
  Colunas: id_categoria, nome_categoria, ativo
 */
public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idCategoria;
    private String nomeCategoria;
    private boolean ativo; //status

     //Construtor vazio

    public Categoria() {
        this.ativo = true;
    }


     //Construtor com nome

    public Categoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
        this.ativo = true;
    }


     // Construtor completo

    public Categoria(int idCategoria, String nomeCategoria) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
        this.ativo = true;
    }


    // Construtor completo com ativo

    public Categoria(int idCategoria, String nomeCategoria, boolean ativo) {
        this.idCategoria = idCategoria;
        this.nomeCategoria = nomeCategoria;
        this.ativo = ativo;
    }

    // GETTERS

    public int getIdCategoria() {
        return idCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean getAtivo() {
        return ativo;
    }

    // SETTERS

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // toString

    @Override
    public String toString() {
        return nomeCategoria;
    }

    // equals e hashCode

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
    }
}