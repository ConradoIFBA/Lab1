package br.com.projeto.model;

import java.time.LocalDateTime;

public class Vendas {

    private int idVendas;
    private Object dataVendas;  // Aceita Date ou LocalDateTime
    private float valor;
    private String notaFiscalEmitida;  // S/N
    private Categoria categoria;
    private NotaFiscal notaFiscal;
    private int usuarioId;  // FK para usuario
    private String descricao;  // Descrição da venda

    // Construtor vazio
    public Vendas() {}

    // Construtor completo
    public Vendas(int idVendas, LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria, NotaFiscal notaFiscal) {
        this.idVendas = idVendas;
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = notaFiscalEmitida;
        this.categoria = categoria;
        this.notaFiscal = notaFiscal;
    }

    // Construtor sem ID (auto incremento)
    public Vendas(LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria, NotaFiscal notaFiscal) {
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = notaFiscalEmitida;
        this.categoria = categoria;
        this.notaFiscal = notaFiscal;
    }

    // Construtor simplificado sem nota fiscal
    public Vendas(LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria) {
        this(dataVendas, valor, notaFiscalEmitida, categoria, null);
    }

    // Construtor simplificado com valores básicos
    public Vendas(LocalDateTime dataVendas, float valor, int idCategoria) {
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = "N";
        this.categoria = new Categoria(idCategoria, "");
        this.notaFiscal = null;
    }

    // Construtor para venda rápida (data atual)
    public Vendas(float valor, Categoria categoria) {
        this(LocalDateTime.now(), valor, "N", categoria, null);
    }

    // Getters e Setters
    public int getIdVendas() {
        return idVendas;
    }

    public void setIdVendas(int idVendas) {
        this.idVendas = idVendas;
    }
    
    // Compatibilidade com Pagamento.java
    public int getIdVenda() {
        return idVendas;
    }

    public void setIdVenda(int idVenda) {
        this.idVendas = idVenda;
    }

    public Object getDataVendas() {
        return dataVendas;
    }

    public void setDataVendas(Object dataVendas) {
        this.dataVendas = dataVendas;
    }
    
    // LocalDateTime
    public LocalDateTime getDataVendasAsLocalDateTime() {
        if (dataVendas instanceof LocalDateTime) {
            return (LocalDateTime) dataVendas;
        } else if (dataVendas instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) dataVendas).getTime()).toLocalDateTime();
        }
        return null;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public String getNotaFiscalEmitida() {
        return notaFiscalEmitida;
    }

    public void setNotaFiscalEmitida(String notaFiscalEmitida) {
        this.notaFiscalEmitida = notaFiscalEmitida;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public NotaFiscal getNotaFiscal() {
        return notaFiscal;
    }

    public void setNotaFiscal(NotaFiscal notaFiscal) {
        this.notaFiscal = notaFiscal;
    }

    // usuarioId
    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // descricao
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // auxiliares
    public int getIdCategoria() {
        return categoria != null ? categoria.getIdCategoria() : 0;
    }

    public String getNomeCategoria() {
        return categoria != null ? categoria.getNomeCategoria() : "";
    }

    public boolean isNotaFiscalEmitida() {
        return "S".equalsIgnoreCase(notaFiscalEmitida);
    }
    
    @Override
    public String toString() {
        return "Vendas [idVendas=" + idVendas + 
               ", valor=" + valor + 
               ", usuarioId=" + usuarioId + 
               ", descricao=" + descricao + "]";
    }
}