package com.projetopoo.OrganizadorDeMudancasSwing.model;

public class CaixaComum extends Caixa {

    public CaixaComum(String nome, String categoria, StatusCaixa status) {
        super(nome, categoria, status);
    }

    public CaixaComum(String id, String nome, String categoria, StatusCaixa status) {
        super(id, nome, categoria, status);
    }

    @Override
    public String getTipoCaixa() {
        return "Comum";
    }
}