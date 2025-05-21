package com.projetopoo.OrganizadorDeMudancasSwing.model;

public enum StatusCaixa {
    A_EMBALAR("A Embalar"),
    EMBALADA("Embalada"),
    TRANSPORTADA("Transportada"),
    DESEMBALADA("Desembalada");

    private final String descricao;

    StatusCaixa(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }

    // Método para converter string de volta para o Enum
    public static StatusCaixa fromString(String text) {
        if (text != null) {
            for (StatusCaixa s : StatusCaixa.values()) {
                // Compara tanto pela descrição quanto pelo nome do enum (para flexibilidade)
                if (text.equalsIgnoreCase(s.descricao) || text.equalsIgnoreCase(s.name())) {
                    return s;
                }
            }
        }
        return A_EMBALAR; // Retorna um padrão se não encontrar ou se o texto for nulo
    }
}