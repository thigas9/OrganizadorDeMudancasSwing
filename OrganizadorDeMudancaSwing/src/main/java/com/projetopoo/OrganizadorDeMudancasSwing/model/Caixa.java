package com.projetopoo.OrganizadorDeMudancasSwing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Caixa {
    protected String id;
    protected String nome;
    protected List<ItemMudanca> itens;
    protected String categoria; 
    protected StatusCaixa status;

    
    public Caixa(String nome, String categoria, StatusCaixa status) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.itens = new ArrayList<>();
        this.categoria = (categoria == null || categoria.isBlank()) ? "Geral" : categoria;
        this.status = (status == null) ? StatusCaixa.A_EMBALAR : status;
    }

    // Construtor para carregar de arquivo
    public Caixa(String id, String nome, String categoria, StatusCaixa status) {
        this.id = id;
        this.nome = nome;
        this.itens = new ArrayList<>();
        this.categoria = (categoria == null || categoria.isBlank()) ? "Geral" : categoria;
        this.status = (status == null) ? StatusCaixa.A_EMBALAR : status;
    }

    // Getters e Setters para os novos campos
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = (categoria == null || categoria.isBlank()) ? "Geral" : categoria;
    }

    public StatusCaixa getStatus() {
        return status;
    }

    public void setStatus(StatusCaixa status) {
        this.status = (status == null) ? StatusCaixa.A_EMBALAR : status;
    }

    // Métodos existentes (getId, getNome, setNome, getItens, adicionarItem, removerItem)
    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public List<ItemMudanca> getItens() { return itens; }
    public void adicionarItem(ItemMudanca item) { this.itens.add(item); }
    public void removerItem(int index) {
        if (index >= 0 && index < itens.size()) {
            this.itens.remove(index);
        }
    }
    
    public abstract String getTipoCaixa();

    public String getDetalhesEspecificosParaSalvar() {
        return ""; 
    }

    @Override
    public String toString() {
        return getTipoCaixa() + ": " + nome + " (Cat: " + categoria + ", Status: " + status + ", Itens: " + itens.size() + ")";
    }
}