package com.projetopoo.OrganizadorDeMudancasSwing.model;

import com.projetopoo.OrganizadorDeMudancasSwing.service.GerenciadorMudanca;

public class CaixaFragil extends Caixa {
    private String instrucoesCuidado;

    public CaixaFragil(String nome, String categoria, StatusCaixa status, String instrucoesCuidado) {
        super(nome, categoria, status);
        this.instrucoesCuidado = (instrucoesCuidado == null || instrucoesCuidado.isBlank()) ? "Manusear com cuidado" : instrucoesCuidado;
    }

    public CaixaFragil(String id, String nome, String categoria, StatusCaixa status, String instrucoesCuidado) {
        super(id, nome, categoria, status);
        this.instrucoesCuidado = (instrucoesCuidado == null || instrucoesCuidado.isBlank()) ? "Manusear com cuidado" : instrucoesCuidado;
    }

    public String getInstrucoesCuidado() {
        return instrucoesCuidado;
    }

    public void setInstrucoesCuidado(String instrucoesCuidado) {
        this.instrucoesCuidado = instrucoesCuidado;
    }

    @Override
    public String getTipoCaixa() {
        return "Fragil";
    }

    @Override
    public String getDetalhesEspecificosParaSalvar() {
        return GerenciadorMudanca.MARCADOR_INSTRUCOES_CUIDADO + this.instrucoesCuidado; // Usa a constante do Gerenciador
    }
}