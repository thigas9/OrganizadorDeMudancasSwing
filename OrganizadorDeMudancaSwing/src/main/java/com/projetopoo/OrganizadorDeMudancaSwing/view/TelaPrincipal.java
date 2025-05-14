package com.projetopoo.OrganizadorDeMudancaSwing.view;

import com.projetopoo.OrganizadorDeMudancaSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancaSwing.model.ItemMudanca;
import com.projetopoo.OrganizadorDeMudancaSwing.util.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TelaPrincipal extends JFrame {

    private List<Caixa> caixas = new ArrayList<>();

    private DefaultListModel<String> modeloListaCaixas = new DefaultListModel<>();
    private JList<String> listaCaixas = new JList<>(modeloListaCaixas);

    private DefaultListModel<String> modeloListaItens = new DefaultListModel<>();
    private JList<String> listaItens = new JList<>(modeloListaItens);

    public TelaPrincipal() {
        setTitle("Organizador de Mudança 📦");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel esquerdo: caixas
        JPanel painelCaixas = new JPanel(new BorderLayout());
        painelCaixas.setBorder(BorderFactory.createTitledBorder("Caixas"));
        painelCaixas.add(new JScrollPane(listaCaixas), BorderLayout.CENTER);

        JButton btnNovaCaixa = new JButton("Nova Caixa");
        painelCaixas.add(btnNovaCaixa, BorderLayout.SOUTH);

        // Painel direito: itens da caixa selecionada
        JPanel painelItens = new JPanel(new BorderLayout());
        painelItens.setBorder(BorderFactory.createTitledBorder("Itens da Caixa"));
        painelItens.add(new JScrollPane(listaItens), BorderLayout.CENTER);

        JPanel painelBotoesItens = new JPanel();
        JButton btnNovoItem = new JButton("Novo Item");
        JButton btnGerarQRCode = new JButton("Gerar QR Code");
        painelBotoesItens.add(btnNovoItem);
        painelBotoesItens.add(btnGerarQRCode);

        painelItens.add(painelBotoesItens, BorderLayout.SOUTH);

        add(painelCaixas, BorderLayout.WEST);
        add(painelItens, BorderLayout.CENTER);

        // Ação para criar nova caixa
        btnNovaCaixa.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da caixa:");
            if (nome != null && !nome.isBlank()) {
                String id = UUID.randomUUID().toString();
                Caixa caixa = new Caixa(id, nome);
                caixas.add(caixa);
                modeloListaCaixas.addElement(caixa.getNome());
            }
        });

        // Ação para selecionar caixa e mostrar itens
        listaCaixas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarListaItens();
            }
        });

        // Ação para adicionar item na caixa selecionada
        btnNovoItem.addActionListener(e -> {
            int index = listaCaixas.getSelectedIndex();
            if (index >= 0) {
                String nome = JOptionPane.showInputDialog(this, "Nome do item:");
                String descricao = JOptionPane.showInputDialog(this, "Descrição do item:");
                if (nome != null && !nome.isBlank() && descricao != null && !descricao.isBlank()) {
                    ItemMudanca item = new ItemMudanca(nome, descricao);
                    caixas.get(index).adicionarItem(item);
                    atualizarListaItens();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.");
            }
        });

        // Ação para gerar QR Code da caixa selecionada
        btnGerarQRCode.addActionListener(e -> {
            int index = listaCaixas.getSelectedIndex();
            if (index >= 0) {
                Caixa caixaSelecionada = caixas.get(index);
                String textoQR = "Caixa: " + caixaSelecionada.getNome() + "\n\n" + caixaSelecionada.gerarDescricaoItens();

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Salvar QR Code");
                fileChooser.setSelectedFile(new File("QRCode_" + caixaSelecionada.getNome() + ".png"));
                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File arquivo = fileChooser.getSelectedFile();
                    try {
                        QRCodeUtil.gerarQRCode(textoQR, arquivo.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "QR Code salvo com sucesso!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code.");
                    }
                }

            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.");
            }
        });
    }

    // Atualiza a lista de itens com base na caixa selecionada
    private void atualizarListaItens() {
        modeloListaItens.clear();
        int index = listaCaixas.getSelectedIndex();
        if (index >= 0) {
            Caixa caixaSelecionada = caixas.get(index);
            for (ItemMudanca item : caixaSelecionada.getItens()) {
                modeloListaItens.addElement(item.getNome() + " - " + item.getDescricao());
            }
        }
    }
}
