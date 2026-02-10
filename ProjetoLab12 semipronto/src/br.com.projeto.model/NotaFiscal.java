package br.com.projeto.model;

import java.time.LocalDateTime;

public class NotaFiscal {

    private int idNotaFiscal;
    private String numero;  // // Numero da NF (ex: NF-2026001)
    private Object dataEmissao;  // Date ou LocalDateTime
    private float valor;
    private int vendasId;  // FK para vendas
    private int usuarioId; // FK para usuario

    // Construtor Vazio
    public NotaFiscal() {}

    // Construtor completo
    public NotaFiscal(int idNotaFiscal, String numero, LocalDateTime dataEmissao, float valor) {
        this.idNotaFiscal = idNotaFiscal;
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }

    // Construtor sem ID (auto incremento)
    public NotaFiscal(String numero, LocalDateTime dataEmissao, float valor) {
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }

    // Construtor simplificado (apenas para inserção rápida)
    public NotaFiscal(String numero, float valor) {
        this(numero, LocalDateTime.now(), valor);
    }

    // Getters e Setters
    public int getIdNotaFiscal() {
        return idNotaFiscal;
    }

    public void setIdNotaFiscal(int idNotaFiscal) {
        this.idNotaFiscal = idNotaFiscal;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Object getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(Object dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    // Método auxiliar para LocalDateTime
    public LocalDateTime getDataEmissaoAsLocalDateTime() {
        if (dataEmissao instanceof LocalDateTime) {
            return (LocalDateTime) dataEmissao;
        } else if (dataEmissao instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) dataEmissao).getTime()).toLocalDateTime();
        }
        return null;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public int getVendasId() {
        return vendasId;
    }

    public void setVendasId(int vendasId) {
        this.vendasId = vendasId;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Método toString
    @Override
    public String toString() {
        return "NotaFiscal [idNotaFiscal=" + idNotaFiscal +
               ", numero=" + numero +
               ", dataEmissao=" + dataEmissao +
               ", valor=" + valor +
               ", vendasId=" + vendasId +
               ", usuarioId=" + usuarioId + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotaFiscal that = (NotaFiscal) o;
        return idNotaFiscal == that.idNotaFiscal;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idNotaFiscal);
    }
}