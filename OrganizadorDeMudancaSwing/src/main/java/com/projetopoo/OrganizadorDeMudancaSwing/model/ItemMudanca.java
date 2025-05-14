package com.projetopoo.OrganizadorDeMudancaSwing.model;

public class ItemMudanca {
    private String nome;
    private String descricao;

    public ItemMudanca(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return "Nome: " + nome + " | Descrição: " + descricao;
    }
}
