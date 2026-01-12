package br.com.projeto.model;
import java.time.LocalDateTime;

public class Vendas {

    private int idVendas;
    private LocalDateTime dataVendas;
    private float valor;
    private String notaFiscalEmitida;  //S/N
    private Categoria categoria;            
    private NotaFiscal notaFiscal;    
    
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
    
    // Construtor Sem ID (auto incremento) - para inserção
    public Vendas(LocalDateTime dataVendas, float valor,
                  String notaFiscalEmitida, Categoria categoria, NotaFiscal notaFiscal) {
        this.dataVendas = dataVendas;
        this.valor = valor;
        this.notaFiscalEmitida = notaFiscalEmitida;
        this.categoria = categoria;
        this.notaFiscal = notaFiscal;
        // idVendas será gerado automaticamente pelo banco
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
        this.notaFiscalEmitida = "N"; // Padrão sem nota fiscal
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

    public LocalDateTime getDataVendas() {
        return dataVendas;
    }

    public void setDataVendas(LocalDateTime dataVendas) {
        this.dataVendas = dataVendas;
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
    
    // Métodos auxiliares
    public int getIdCategoria() {
        return categoria != null ? categoria.getIdCategoria() : 0;
    }
    
    public String getNomeCategoria() {
        return categoria != null ? categoria.getNomeCategoria() : "";
    }
    
    public boolean isNotaFiscalEmitida() {
        return "S".equalsIgnoreCase(notaFiscalEmitida);
    }
    
    
}