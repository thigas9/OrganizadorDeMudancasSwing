package com.projetopoo.OrganizadorDeMudancasSwing.view; 

import com.projetopoo.OrganizadorDeMudancasSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancasSwing.model.StatusCaixa;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CaixasTableModel extends AbstractTableModel {
    private final List<Caixa> caixas;
    private final String[] colunas = {"Nome", "Tipo", "Categoria", "Status", "Nº Itens"};

    public CaixasTableModel() {
        this.caixas = new ArrayList<>();
    }

    public CaixasTableModel(List<Caixa> caixas) {
        this.caixas = new ArrayList<>(caixas);
    }

    @Override
    public int getRowCount() {
        return caixas.size();
    }

    @Override
    public int getColumnCount() {
        return colunas.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return colunas[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Caixa caixa = caixas.get(rowIndex);
        switch (columnIndex) {
            case 0: return caixa.getNome();
            case 1: return caixa.getTipoCaixa();
            case 2: return caixa.getCategoria();
            case 3: return caixa.getStatus().toString(); // Usa o toString() do Enum para a descrição
            case 4: return caixa.getItens().size();
            default: return null;
        }
    }

    // Método para atualizar os dados da tabela
    public void setCaixas(List<Caixa> novasCaixas) {
        this.caixas.clear();
        this.caixas.addAll(novasCaixas);
        fireTableDataChanged(); // Notifica a JTable que os dados mudaram
    }

    // Método para obter a Caixa em uma linha específica, útil para ações
    public Caixa getCaixaAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < caixas.size()) {
            return caixas.get(rowIndex);
        }
        return null;
    }
}