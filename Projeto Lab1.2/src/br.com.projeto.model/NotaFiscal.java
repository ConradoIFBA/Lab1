package br.com.projeto.model;
import java.time.LocalDateTime;

public class NotaFiscal {

    private int idNotaFiscal;
    private int numero;
    private LocalDateTime dataEmissao;
    private float valor;

    // Construtor Vazio
    public NotaFiscal() {}

    // Construtor completo
    public NotaFiscal(int idNotaFiscal, int numero, LocalDateTime dataEmissao, float valor) {
        this.idNotaFiscal = idNotaFiscal;
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
    }

    // Construtor sem ID (auto incremento)
    public NotaFiscal(int numero, LocalDateTime dataEmissao, float valor) {
        this.numero = numero;
        this.dataEmissao = dataEmissao;
        this.valor = valor;
       
    }

    // Construtor simplificado (apenas para inserção rápida)
    public NotaFiscal(int numero, float valor) {
        this(numero, LocalDateTime.now(), valor);
        // Usa data/hora atual como padrão
    }

    // Getters e Setters
    public int getIdNotaFiscal() {
        return idNotaFiscal;
    }

    public void setIdNotaFiscal(int idNotaFiscal) {
        this.idNotaFiscal = idNotaFiscal;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }
    
    // Método toString para facilitar a visualização
    @Override
    public String toString() {
        return "NotaFiscal [idNotaFiscal=" + idNotaFiscal + 
               ", numero=" + numero + 
               ", dataEmissao=" + dataEmissao + 
               ", valor=" + valor + "]";
    }
    
    // Métodos equals e hashCode baseados no ID
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