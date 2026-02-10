package br.com.projeto.model;

public class Pagamento {

    private int idPag;
    private Vendas vendasId;
    private MetPag metPagId;
    private float valor;

    // Construtor vazio
    public Pagamento() {}

    // Construtor completo (para consultas/atualizações)
    public Pagamento(int idPag, Vendas vendasId, MetPag metPagId, float valor) {
        this.idPag = idPag;
        this.vendasId = vendasId;
        this.metPagId = metPagId;
        this.valor = valor;
    }

    // Construtor Sem ID (auto incremento) - para inserções
    public Pagamento(Vendas vendasId, MetPag metPagId, float valor) {
        this.vendasId = vendasId;
        this.metPagId = metPagId;
        this.valor = valor;
        // idPag será gerado automaticamente pelo banco
    }

    // Construtor simplificado (IDs básicos)
    public Pagamento(int idVenda, int idMetPag, float valor) {
        this.vendasId = new Vendas();
        this.vendasId.setIdVenda(idVenda);
        
        this.metPagId = new MetPag();
        this.metPagId.setIdMetPag(idMetPag);
        
        this.valor = valor;
    }

    // Getters e Setters
    public int getIdPag() {
        return idPag;
    }

    public void setIdPag(int idPag) {
        this.idPag = idPag;
    }

    public Vendas getVendasId() {
        return vendasId;
    }

    public void setVendasId(Vendas vendasId) {
        this.vendasId = vendasId;
    }

    public MetPag getMetPagId() {
        return metPagId;
    }

    public void setMetPagId(MetPag metPagId) {
        this.metPagId = metPagId;
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
        return "Pagamento [idPag=" + idPag + 
               ", vendasId=" + (vendasId != null ? vendasId.getIdVenda() : "null") + 
               ", metPagId=" + (metPagId != null ? metPagId.getIdMetPag() : "null") + 
               ", valor=" + valor + "]";
    }

    // Métodos equals e hashCode baseados no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return idPag == pagamento.idPag;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idPag);
    }
    
    // Métodos auxiliares
    public int getIdVenda() {
        return vendasId != null ? vendasId.getIdVenda() : 0;
    }
    
    public int getIdMetPag() {
        return metPagId != null ? metPagId.getIdMetPag() : 0;
    }
}