package com.projetopoo.OrganizadorDeMudancaSwing.view;

import com.projetopoo.OrganizadorDeMudancaSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancaSwing.model.ItemMudanca;
import com.projetopoo.OrganizadorDeMudancaSwing.util.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
// Removido: java.awt.event.ActionEvent; (não é mais necessário importar especificamente se usamos lambdas)
// Removido: java.awt.event.ActionListener; (não é mais necessário importar especificamente se usamos lambdas)
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
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Adicionado espaçamento entre componentes do BorderLayout
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margem na janela

        // === Painel Esquerdo: Caixas ===
        JPanel painelCaixas = new JPanel(new BorderLayout(5, 5));
        painelCaixas.setBorder(BorderFactory.createTitledBorder("Caixas"));
        painelCaixas.add(new JScrollPane(listaCaixas), BorderLayout.CENTER);

        JPanel painelBotoesCaixas = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Usar FlowLayout para botões lado a lado
        JButton btnNovaCaixa = new JButton("Nova Caixa");
        JButton btnRemoverCaixa = new JButton("Remover Caixa");
        painelBotoesCaixas.add(btnNovaCaixa);
        painelBotoesCaixas.add(btnRemoverCaixa);
        painelCaixas.add(painelBotoesCaixas, BorderLayout.SOUTH);

        // === Painel Direito: Itens da Caixa Selecionada ===
        JPanel painelItens = new JPanel(new BorderLayout(5, 5));
        painelItens.setBorder(BorderFactory.createTitledBorder("Itens da Caixa"));
        painelItens.add(new JScrollPane(listaItens), BorderLayout.CENTER);

        JPanel painelBotoesItens = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnNovoItem = new JButton("Novo Item");
        JButton btnRemoverItem = new JButton("Remover Item"); // Botão novo
        JButton btnGerarQRCode = new JButton("Gerar QR Code");
        painelBotoesItens.add(btnNovoItem);
        painelBotoesItens.add(btnRemoverItem); // Adicionado
        painelBotoesItens.add(btnGerarQRCode);
        painelItens.add(painelBotoesItens, BorderLayout.SOUTH);

        // Usando JSplitPane para permitir redimensionamento
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelCaixas, painelItens);
        splitPane.setDividerLocation(300); // Posição inicial do divisor
        add(splitPane, BorderLayout.CENTER);


        // --- Ações dos Botões ---

        // Ação para criar nova caixa
        btnNovaCaixa.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da caixa:", "Nova Caixa", JOptionPane.PLAIN_MESSAGE);
            if (nome != null && !nome.isBlank()) {
                // Validação simples para nome duplicado (pode ser melhorada)
                boolean nomeExistente = false;
                for (Caixa c : caixas) {
                    if (c.getNome().equalsIgnoreCase(nome.trim())) {
                        nomeExistente = true;
                        break;
                    }
                }
                if (nomeExistente) {
                    JOptionPane.showMessageDialog(this, "Já existe uma caixa com este nome.", "Erro", JOptionPane.ERROR_MESSAGE);
                } else {
                    String id = UUID.randomUUID().toString();
                    Caixa caixa = new Caixa(id, nome.trim());
                    caixas.add(caixa);
                    modeloListaCaixas.addElement(caixa.getNome());
                    listaCaixas.setSelectedIndex(modeloListaCaixas.getSize() - 1); // Seleciona a nova caixa
                }
            }
        });

        // Ação para remover caixa selecionada
        btnRemoverCaixa.addActionListener(e -> {
            int indexSelecionado = listaCaixas.getSelectedIndex();
            if (indexSelecionado >= 0) {
                String nomeCaixa = modeloListaCaixas.getElementAt(indexSelecionado);
                int confirmacao = JOptionPane.showConfirmDialog(this,
                        "Tem certeza que deseja remover a caixa \"" + nomeCaixa + "\" e todos os seus itens?",
                        "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    caixas.remove(indexSelecionado);
                    modeloListaCaixas.remove(indexSelecionado);
                    modeloListaItens.clear(); // Limpa a lista de itens, pois a caixa foi removida
                    if (modeloListaCaixas.getSize() > 0) { // Se ainda houver caixas
                        listaCaixas.setSelectedIndex(Math.max(0, indexSelecionado -1)); // Tenta selecionar a anterior ou a primeira
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
            int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
            if (indexCaixaSelecionada >= 0) {
                JTextField campoNomeItem = new JTextField();
                JTextField campoDescricaoItem = new JTextField();
                Object[] campos = {
                        "Nome do item:", campoNomeItem,
                        "Descrição do item:", campoDescricaoItem
                };
                int option = JOptionPane.showConfirmDialog(this, campos, "Novo Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    String nome = campoNomeItem.getText();
                    String descricao = campoDescricaoItem.getText();

                    if (nome != null && !nome.isBlank()) {
                        ItemMudanca item = new ItemMudanca(nome.trim(), (descricao != null ? descricao.trim() : ""));
                        caixas.get(indexCaixaSelecionada).adicionarItem(item);
                        atualizarListaItens();
                        listaItens.setSelectedIndex(modeloListaItens.getSize() -1); // Seleciona o novo item
                    } else {
                         JOptionPane.showMessageDialog(this, "O nome do item não pode ser vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro para adicionar um item.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Ação para remover item selecionado
        btnRemoverItem.addActionListener(e -> {
            int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
            int indexItemSelecionado = listaItens.getSelectedIndex();

            if (indexCaixaSelecionada < 0) {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (indexItemSelecionado < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um item para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Caixa caixaSelecionada = caixas.get(indexCaixaSelecionada);
            // Como a lista de itens mostra "nome - descrição", precisamos pegar o objeto ItemMudanca pela posição
            String nomeItem = modeloListaItens.getElementAt(indexItemSelecionado);

            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja remover o item \"" + nomeItem + "\"?",
                    "Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmacao == JOptionPane.YES_OPTION) {
                caixaSelecionada.getItens().remove(indexItemSelecionado); // Remove pelo índice
                atualizarListaItens();
                 if (modeloListaItens.getSize() > 0) { // Se ainda houver itens
                    listaItens.setSelectedIndex(Math.max(0, indexItemSelecionado -1)); // Tenta selecionar o anterior ou o primeiro
                }
            }
        });

        // Ação para gerar QR Code da caixa selecionada
        btnGerarQRCode.addActionListener(e -> {
            int index = listaCaixas.getSelectedIndex();
            if (index >= 0) {
                Caixa caixaSelecionada = caixas.get(index);
                String textoQR = "Caixa: " + caixaSelecionada.getNome() + "\n\nItens:\n" + caixaSelecionada.gerarDescricaoItens();

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Salvar QR Code");
                fileChooser.setSelectedFile(new File("QRCode_" + caixaSelecionada.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") + ".png")); // Nome de arquivo mais seguro
                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File arquivo = fileChooser.getSelectedFile();
                    try {
                        QRCodeUtil.gerarQRCode(textoQR, arquivo.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "QR Code salvo com sucesso em:\n" + arquivo.getAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro para gerar o QR Code.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        setLocationRelativeTo(null); // Centralizar a janela
    }

    // Atualiza a lista de itens com base na caixa selecionada
    private void atualizarListaItens() {
        modeloListaItens.clear();
        int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
        if (indexCaixaSelecionada >= 0) {
            Caixa caixaSelecionada = caixas.get(indexCaixaSelecionada);
            for (ItemMudanca item : caixaSelecionada.getItens()) {
                modeloListaItens.addElement(item.getNome() + (item.getDescricao().isEmpty() ? "" : " - " + item.getDescricao()));
            }
        }
    }
}