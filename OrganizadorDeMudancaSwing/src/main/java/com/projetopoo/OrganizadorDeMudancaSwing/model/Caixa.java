package com.projetopoo.OrganizadorDeMudancaSwing.model;

import java.util.ArrayList;
import java.util.List;

public class Caixa {
    private String id;
    private String nome;
    private List<ItemMudanca> itens;

    public Caixa(String id, String nome) {
        this.id = id;
        this.nome = nome;
        this.itens = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<ItemMudanca> getItens() {
        return itens;
    }

    public void adicionarItem(ItemMudanca item) {
        itens.add(item);
    }

    public void removerItem(ItemMudanca item) {
        itens.remove(item);
    }

    public String gerarDescricaoItens() {
        StringBuilder descricao = new StringBuilder();
        for (ItemMudanca item : itens) {
            descricao.append("Item: ").append(item.getNome())
                     .append(" | Descrição: ").append(item.getDescricao())
                     .append("\n");
        }
        return descricao.toString();
    }
}
