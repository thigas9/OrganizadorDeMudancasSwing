package com.projetopoo.OrganizadorDeMudancasSwing.view;

import com.projetopoo.OrganizadorDeMudancasSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancasSwing.model.ItemMudanca;
import com.projetopoo.OrganizadorDeMudancasSwing.model.StatusCaixa;
import com.projetopoo.OrganizadorDeMudancasSwing.service.GerenciadorMudanca;
import com.projetopoo.OrganizadorDeMudancasSwing.util.QRCodeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TelaPrincipal extends JFrame {

	private GerenciadorMudanca gerenciador;
	private CaixasTableModel caixasTableModel;
	private JTable tabelaCaixas;

	private DefaultListModel<String> modeloListaItens = new DefaultListModel<>();
	private JList<String> listaItens = new JList<>(modeloListaItens);

	private JTextField campoBuscaItem;
	private JComboBox<String> comboFiltroCategoriaBusca;
	private JButton btnBuscarItem;
	private JTextArea areaResultadosBusca;

	private static final String MARCADOR_TIPO_CAIXA_QR = "TipoCaixa: ";
	private static final String PREFIXO_CAIXA_QR = "Caixa: ";
	private static final String MARCADOR_CATEGORIA_QR = "Categoria: ";
	private static final String MARCADOR_STATUS_QR = "Status: ";
	private static final String INDICADOR_ITENS_QR = "Itens:";
	private static final String PREFIXO_ITEM_QR = "- ";
	private static final String SEPARADOR_ITEM_DESCRICAO_QR = " | Descrição: ";
	private static final String MARCADOR_INSTRUCOES_CUIDADO_QR = GerenciadorMudanca.MARCADOR_INSTRUCOES_CUIDADO;

	private final String[] OPCOES_CATEGORIA = { "Geral", "Cozinha", "Quarto", "Sala", "Banheiro", "Escritório",
			"Documentos", "Frágil (Geral)", "Doação", "Outra..." };

	// Construtor: Configura a janela principal, inicializa componentes da UI e define listeners de eventos.
	public TelaPrincipal() {
		this.gerenciador = new GerenciadorMudanca();
		this.caixasTableModel = new CaixasTableModel(gerenciador.getCaixas());

		setTitle("Organizador de Mudanças 📦");
		setLayout(new BorderLayout(10, 10));
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		criarMenu();

		JPanel painelCaixas = new JPanel(new BorderLayout(5, 5));
		painelCaixas.setBorder(BorderFactory.createTitledBorder("Caixas"));
		tabelaCaixas = new JTable(caixasTableModel);
		tabelaCaixas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabelaCaixas.setAutoCreateRowSorter(true);
		tabelaCaixas.getColumnModel().getColumn(0).setPreferredWidth(150);
		tabelaCaixas.getColumnModel().getColumn(1).setPreferredWidth(80);
		tabelaCaixas.getColumnModel().getColumn(2).setPreferredWidth(100);
		tabelaCaixas.getColumnModel().getColumn(3).setPreferredWidth(100);
		tabelaCaixas.getColumnModel().getColumn(4).setPreferredWidth(60);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		tabelaCaixas.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

		painelCaixas.add(new JScrollPane(tabelaCaixas), BorderLayout.CENTER);

		JPanel painelBotoesCaixas = new JPanel(new GridLayout(1, 0, 5, 5));
		JButton btnNovaCaixa = new JButton("Nova Caixa");
		JButton btnEditarCaixa = new JButton("Editar Caixa");
		JButton btnRemoverCaixa = new JButton("Remover Caixa");
		painelBotoesCaixas.add(btnNovaCaixa);
		painelBotoesCaixas.add(btnEditarCaixa);
		painelBotoesCaixas.add(btnRemoverCaixa);
		painelCaixas.add(painelBotoesCaixas, BorderLayout.SOUTH);

		JPanel painelItens = new JPanel(new BorderLayout(5, 5));
		painelItens.setBorder(BorderFactory.createTitledBorder("Itens da Caixa Selecionada"));
		painelItens.add(new JScrollPane(listaItens), BorderLayout.CENTER);

		JPanel painelBotoesItens = new JPanel(new GridLayout(1, 0, 5, 5));
		JButton btnNovoItem = new JButton("Novo Item");
		JButton btnEditarItem = new JButton("Editar Item");
		JButton btnRemoverItem = new JButton("Remover Item");
		JButton btnGerarQRCode = new JButton("Gerar QR Code");
		painelBotoesItens.add(btnNovoItem);
		painelBotoesItens.add(btnEditarItem);
		painelBotoesItens.add(btnRemoverItem);
		painelBotoesItens.add(btnGerarQRCode);
		painelItens.add(painelBotoesItens, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelCaixas, painelItens);
		splitPane.setResizeWeight(0.6);
		add(splitPane, BorderLayout.CENTER);

		JPanel painelBusca = new JPanel(new BorderLayout(5, 5));
		painelBusca.setBorder(BorderFactory.createTitledBorder("Buscar Item"));

		JPanel painelEntradaBuscaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel labelBuscar = new JLabel("Nome do Item:");
		campoBuscaItem = new JTextField(20);
		JLabel labelFiltroCategoria = new JLabel("Filtrar por Categoria da Caixa:");
		comboFiltroCategoriaBusca = new JComboBox<>(getOpcoesFiltroCategoria());
		btnBuscarItem = new JButton("Buscar");
		painelEntradaBuscaSuperior.add(labelBuscar);
		painelEntradaBuscaSuperior.add(campoBuscaItem);
		painelEntradaBuscaSuperior.add(labelFiltroCategoria);
		painelEntradaBuscaSuperior.add(comboFiltroCategoriaBusca);
		painelEntradaBuscaSuperior.add(btnBuscarItem);

		areaResultadosBusca = new JTextArea(6, 0);
		areaResultadosBusca.setEditable(false);
		areaResultadosBusca.setLineWrap(true);
		areaResultadosBusca.setWrapStyleWord(true);

		painelBusca.add(painelEntradaBuscaSuperior, BorderLayout.NORTH);
		painelBusca.add(new JScrollPane(areaResultadosBusca), BorderLayout.CENTER);

		add(painelBusca, BorderLayout.SOUTH);

		btnNovaCaixa.addActionListener(e -> acaoNovaCaixa());
		btnEditarCaixa.addActionListener(e -> acaoEditarCaixa());
		btnRemoverCaixa.addActionListener(e -> acaoRemoverCaixa());

		tabelaCaixas.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				atualizarListaItens();
			}
		});

		btnNovoItem.addActionListener(e -> acaoNovoItem());
		btnEditarItem.addActionListener(e -> acaoEditarItem());
		btnRemoverItem.addActionListener(e -> acaoRemoverItem());
		btnGerarQRCode.addActionListener(e -> acaoGerarQRCode());
		btnBuscarItem.addActionListener(e -> acaoBuscarItem());

		carregarSessaoAnterior();

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				acaoSalvarSessaoAoFechar();
			}
		});

		pack();
		setMinimumSize(new Dimension(750, 600));
		setLocationRelativeTo(null);
		SwingUtilities.invokeLater(() -> {
			splitPane.setDividerLocation(0.6);
		});
	}

	// Retorna as opções de categoria para o ComboBox de filtro na busca, incluindo "Todas as Categorias".
	private String[] getOpcoesFiltroCategoria() {
		List<String> todasOpcoes = new ArrayList<>();
		todasOpcoes.add("Todas as Categorias");
		todasOpcoes.addAll(Arrays.asList(OPCOES_CATEGORIA));
		todasOpcoes.remove("Outra...");
		return todasOpcoes.toArray(new String[0]);
	}

	// Atualiza as opções do ComboBox de filtro de categoria, tentando manter a seleção anterior.
	private void atualizarComboFiltroCategoriaBusca() {
		String selecionado = (String) comboFiltroCategoriaBusca.getSelectedItem();
		comboFiltroCategoriaBusca.setModel(new DefaultComboBoxModel<>(getOpcoesFiltroCategoria()));
		if (selecionado != null && Arrays.asList(getOpcoesFiltroCategoria()).contains(selecionado)) {
			comboFiltroCategoriaBusca.setSelectedItem(selecionado);
		} else if (comboFiltroCategoriaBusca.getItemCount() > 0) {
			comboFiltroCategoriaBusca.setSelectedIndex(0); // Seleciona "Todas as Categorias"
		}
	}

	// Cria e configura a barra de menu da aplicação.
	private void criarMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuArquivo = new JMenu("Arquivo");
		JMenuItem itemExportarTudo = new JMenuItem("Exportar Todas as Caixas...");
		itemExportarTudo.addActionListener(e -> acaoExportarTudoManual());
		menuArquivo.add(itemExportarTudo);
		JMenuItem itemImportar = new JMenuItem("Importar Caixas...");
		itemImportar.addActionListener(e -> acaoImportarTudoManual());
		menuArquivo.add(itemImportar);
		menuBar.add(menuArquivo);
		setJMenuBar(menuBar);
	}

	// Define a ação para criar uma nova caixa, exibindo um diálogo para coletar os dados necessários.
	private void acaoNovaCaixa() {
		JTextField campoNomeCaixa = new JTextField();
		JComboBox<String> comboTipoCaixa = new JComboBox<>(new String[] { "Comum", "Frágil" });
		JComboBox<String> comboCategoria = new JComboBox<>(OPCOES_CATEGORIA);
		comboCategoria.setEditable(true);
		JComboBox<StatusCaixa> comboStatus = new JComboBox<>(StatusCaixa.values());
		JTextField campoInstrucoesFragil = new JTextField();

		JPanel painelDialogo = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);

		gbc.gridx = 0;
		gbc.gridy = 0;
		painelDialogo.add(new JLabel("Nome da Caixa:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		painelDialogo.add(campoNomeCaixa, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		painelDialogo.add(new JLabel("Tipo de Caixa:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		painelDialogo.add(comboTipoCaixa, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		painelDialogo.add(new JLabel("Categoria:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		painelDialogo.add(comboCategoria, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		painelDialogo.add(new JLabel("Status:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		painelDialogo.add(comboStatus, gbc);

		JLabel labelInstrucoes = new JLabel("Instruções (Frágil):");
		gbc.gridx = 0;
		gbc.gridy = 4;
		painelDialogo.add(labelInstrucoes, gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		painelDialogo.add(campoInstrucoesFragil, gbc);

		labelInstrucoes.setVisible(false);
		campoInstrucoesFragil.setVisible(false);
		comboTipoCaixa.addActionListener(e -> {
			boolean isFragil = "Frágil".equals(comboTipoCaixa.getSelectedItem());
			labelInstrucoes.setVisible(isFragil);
			campoInstrucoesFragil.setVisible(isFragil);
			Window window = SwingUtilities.getWindowAncestor(painelDialogo);
			if (window instanceof JDialog) {
				((JDialog) window).pack();
			}
		});

		int result = JOptionPane.showConfirmDialog(this, painelDialogo, "Nova Caixa", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			String nome = campoNomeCaixa.getText();
			String tipoSelecionado = (String) comboTipoCaixa.getSelectedItem();
			String categoria = (String) comboCategoria.getSelectedItem();
			StatusCaixa status = (StatusCaixa) comboStatus.getSelectedItem();
			String instrucoes = ("Frágil".equals(tipoSelecionado)) ? campoInstrucoesFragil.getText() : null;

			if (nome == null || nome.isBlank()) {
				JOptionPane.showMessageDialog(this, "O nome da caixa não pode ser vazio.", "Aviso",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			boolean sucesso = gerenciador.adicionarCaixa(nome, tipoSelecionado, categoria, status, instrucoes);
			if (sucesso) {
				atualizarVisualizacaoListaCaixas();
			} else {
				JOptionPane.showMessageDialog(this, "Já existe uma caixa com este nome ou o nome é inválido.", "Erro",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Define a ação para editar uma caixa selecionada, exibindo um diálogo com os dados atuais para modificação.
	private void acaoEditarCaixa() {
		int selectedRow = tabelaCaixas.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = tabelaCaixas.convertRowIndexToModel(selectedRow);
			Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRow);
			if (caixaSelecionada == null)
				return;

			JTextField campoNomeCaixa = new JTextField(caixaSelecionada.getNome());
			JComboBox<String> comboCategoria = new JComboBox<>(OPCOES_CATEGORIA);
			comboCategoria.setEditable(true);
			comboCategoria.setSelectedItem(caixaSelecionada.getCategoria());
			JComboBox<StatusCaixa> comboStatus = new JComboBox<>(StatusCaixa.values());
			comboStatus.setSelectedItem(caixaSelecionada.getStatus());

			JPanel painelDialogo = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(2, 2, 2, 2);

			gbc.gridx = 0;
			gbc.gridy = 0;
			painelDialogo.add(new JLabel("Nome da Caixa:"), gbc);
			gbc.gridx = 1;
			gbc.gridy = 0;
			painelDialogo.add(campoNomeCaixa, gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			painelDialogo.add(new JLabel("Categoria:"), gbc);
			gbc.gridx = 1;
			gbc.gridy = 1;
			painelDialogo.add(comboCategoria, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			painelDialogo.add(new JLabel("Status:"), gbc);
			gbc.gridx = 1;
			gbc.gridy = 2;
			painelDialogo.add(comboStatus, gbc);

			JTextField campoInstrucoesFragilGUI = null;
			if (caixaSelecionada instanceof com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil) {
				campoInstrucoesFragilGUI = new JTextField(
						((com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil) caixaSelecionada)
								.getInstrucoesCuidado());
				gbc.gridx = 0;
				gbc.gridy = 3;
				painelDialogo.add(new JLabel("Instruções (Frágil):"), gbc);
				gbc.gridx = 1;
				gbc.gridy = 3;
				painelDialogo.add(campoInstrucoesFragilGUI, gbc);
			}

			int result = JOptionPane.showConfirmDialog(this, painelDialogo, "Editar Caixa",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if (result == JOptionPane.OK_OPTION) {
				String novoNome = campoNomeCaixa.getText();
				String novaCategoria = (String) comboCategoria.getSelectedItem();
				StatusCaixa novoStatus = (StatusCaixa) comboStatus.getSelectedItem();
				String novasInstrucoes = (campoInstrucoesFragilGUI != null) ? campoInstrucoesFragilGUI.getText() : null;

				if (novoNome == null || novoNome.isBlank()) {
					JOptionPane.showMessageDialog(this, "O nome da caixa não pode ser vazio.", "Aviso",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				boolean sucesso = gerenciador.editarCaixa(caixaSelecionada, novoNome, novaCategoria, novoStatus,
						novasInstrucoes);
				if (sucesso) {
					atualizarVisualizacaoListaCaixas();
				} else {
					JOptionPane.showMessageDialog(this,
							"Já existe outra caixa com o nome '" + novoNome.trim() + "' ou o nome é inválido.", "Erro",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa para editar.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Define a ação para remover uma caixa selecionada da JTable, após confirmação.
	private void acaoRemoverCaixa() {
		int selectedRow = tabelaCaixas.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = tabelaCaixas.convertRowIndexToModel(selectedRow);
			Caixa caixaParaRemover = caixasTableModel.getCaixaAt(modelRow);
			if (caixaParaRemover == null)
				return;

			int confirmacao = JOptionPane.showConfirmDialog(this,
					"Tem certeza que deseja remover a caixa \"" + caixaParaRemover.getNome()
							+ "\" e todos os seus itens?",
					"Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (confirmacao == JOptionPane.YES_OPTION) {
				gerenciador.removerCaixa(caixaParaRemover);
				atualizarVisualizacaoListaCaixas();
				modeloListaItens.clear();
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa para remover.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Define a ação para adicionar um novo item à caixa atualmente selecionada na JTable.
	private void acaoNovoItem() {
		int selectedRow = tabelaCaixas.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = tabelaCaixas.convertRowIndexToModel(selectedRow);
			Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRow);
			if (caixaSelecionada == null) {
				JOptionPane.showMessageDialog(this, "Caixa selecionada inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JTextField campoNomeItem = new JTextField();
			JTextField campoDescricaoItem = new JTextField();
			Object[] campos = { "Nome do item:", campoNomeItem, "Descrição do item:", campoDescricaoItem };
			int option = JOptionPane.showConfirmDialog(this, campos, "Novo Item", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (option == JOptionPane.OK_OPTION) {
				String nome = campoNomeItem.getText();
				String descricao = campoDescricaoItem.getText();

				if (nome == null || nome.isBlank()) {
					JOptionPane.showMessageDialog(this, "O nome do item não pode ser vazio.", "Aviso",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				gerenciador.adicionarItemEmCaixa(caixaSelecionada, nome, descricao);
				atualizarListaItens();
				caixasTableModel.fireTableRowsUpdated(modelRow, modelRow);
				listaItens.setSelectedIndex(modeloListaItens.getSize() - 1);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro para adicionar um item.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Define a ação para editar um item selecionado na JList de itens.
	private void acaoEditarItem() {
		int selectedRowCaixa = tabelaCaixas.getSelectedRow();
		int indexItemSelecionado = listaItens.getSelectedIndex();

		if (selectedRowCaixa < 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (indexItemSelecionado < 0) {
			JOptionPane.showMessageDialog(this, "Selecione um item para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRowCaixa = tabelaCaixas.convertRowIndexToModel(selectedRowCaixa);
		Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRowCaixa);
		if (caixaSelecionada == null || indexItemSelecionado >= caixaSelecionada.getItens().size()) {
			JOptionPane.showMessageDialog(this, "Seleção de item inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ItemMudanca itemSelecionado = caixaSelecionada.getItens().get(indexItemSelecionado);

		JTextField campoNomeItem = new JTextField(itemSelecionado.getNome());
		JTextField campoDescricaoItem = new JTextField(itemSelecionado.getDescricao());
		Object[] campos = { "Nome do item:", campoNomeItem, "Descrição do item:", campoDescricaoItem };

		int option = JOptionPane.showConfirmDialog(this, campos, "Editar Item", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			String novoNome = campoNomeItem.getText();
			String novaDescricao = campoDescricaoItem.getText();

			if (novoNome == null || novoNome.isBlank()) {
				JOptionPane.showMessageDialog(this, "O nome do item não pode ser vazio.", "Aviso",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			boolean sucesso = gerenciador.editarItemEmCaixa(itemSelecionado, novoNome, novaDescricao);
			if (sucesso) {
				atualizarListaItens();
				listaItens.setSelectedIndex(indexItemSelecionado);
			}
		}
	}

	// Define a ação para remover um item selecionado da JList de itens.
	private void acaoRemoverItem() {
		int selectedRowCaixa = tabelaCaixas.getSelectedRow();
		int indexItemSelecionado = listaItens.getSelectedIndex();

		if (selectedRowCaixa < 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (indexItemSelecionado < 0) {
			JOptionPane.showMessageDialog(this, "Selecione um item para remover.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int modelRowCaixa = tabelaCaixas.convertRowIndexToModel(selectedRowCaixa);
		Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRowCaixa);
		if (caixaSelecionada == null || indexItemSelecionado >= caixaSelecionada.getItens().size()) {
			JOptionPane.showMessageDialog(this, "Seleção de item inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ItemMudanca itemParaRemover = caixaSelecionada.getItens().get(indexItemSelecionado);

		int confirmacao = JOptionPane.showConfirmDialog(this,
				"Tem certeza que deseja remover o item \"" + itemParaRemover.getNome() + "\"?", "Confirmar Remoção",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirmacao == JOptionPane.YES_OPTION) {
			gerenciador.removerItemDeCaixa(caixaSelecionada, indexItemSelecionado);
			atualizarListaItens();
			caixasTableModel.fireTableRowsUpdated(modelRowCaixa, modelRowCaixa);
			if (modeloListaItens.getSize() > 0) {
				listaItens.setSelectedIndex(Math.max(0, indexItemSelecionado - 1));
			}
		}
	}

	// Define a ação para gerar um QR Code para a caixa selecionada na JTable.
	private void acaoGerarQRCode() {
		int selectedRow = tabelaCaixas.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = tabelaCaixas.convertRowIndexToModel(selectedRow);
			Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRow);
			if (caixaSelecionada == null)
				return;

            // 1. Preparar as linhas de texto descritivo para a etiqueta
            List<String> linhasDescritivas = new ArrayList<>();
            linhasDescritivas.add("Caixa: " + caixaSelecionada.getNome()); // Linha principal, maior
            linhasDescritivas.add("Tipo: " + caixaSelecionada.getTipoCaixa());
            linhasDescritivas.add("Categoria: " + caixaSelecionada.getCategoria());
            linhasDescritivas.add("Status: " + caixaSelecionada.getStatus().toString());

            if (caixaSelecionada instanceof com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil) {
                com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil cf = (com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil) caixaSelecionada;
                if (cf.getInstrucoesCuidado() != null && !cf.getInstrucoesCuidado().isEmpty()){
                    linhasDescritivas.add("Cuidados: " + cf.getInstrucoesCuidado());
                }
            }
            // Você pode adicionar mais linhas aqui, como um resumo dos itens, se desejar,
            // mas cuidado para não poluir demais a etiqueta visual.

            // 2. Preparar o payload (conteúdo) para o QR Code em si (o que o scanner vai ler)
			StringBuilder payloadQR = new StringBuilder();
			payloadQR.append(MARCADOR_TIPO_CAIXA_QR).append(caixaSelecionada.getTipoCaixa()).append("\n");
			payloadQR.append(PREFIXO_CAIXA_QR).append(caixaSelecionada.getNome()).append("\n");
			payloadQR.append(MARCADOR_CATEGORIA_QR).append(caixaSelecionada.getCategoria()).append("\n");
			payloadQR.append(MARCADOR_STATUS_QR).append(caixaSelecionada.getStatus().name()).append("\n");

			String detalhesEspecificos = caixaSelecionada.getDetalhesEspecificosParaSalvar();
			if (detalhesEspecificos != null && !detalhesEspecificos.isEmpty()) {
                // O MARCADOR_INSTRUCOES_CUIDADO_QR é usado se getDetalhesEspecificosParaSalvar() o incluir
				payloadQR.append(detalhesEspecificos).append("\n"); 
			}

			payloadQR.append(INDICADOR_ITENS_QR).append("\n");
			for (ItemMudanca item : caixaSelecionada.getItens()) {
				payloadQR.append(PREFIXO_ITEM_QR).append(item.getNome()).append(SEPARADOR_ITEM_DESCRICAO_QR)
						.append(item.getDescricao()).append("\n");
			}

            // 3. Escolher local para salvar e gerar a etiqueta
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Salvar Etiqueta com QR Code");
			fileChooser.setSelectedFile(
					new File("Etiqueta_Caixa_" + caixaSelecionada.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") + ".png"));
			int userSelection = fileChooser.showSaveDialog(this);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File arquivo = fileChooser.getSelectedFile();
				try {
                    // Chama o novo método do QRCodeUtil
					QRCodeUtil.gerarEtiquetaComQRCode(payloadQR.toString(), linhasDescritivas, arquivo.getAbsolutePath());
					JOptionPane.showMessageDialog(this, "Etiqueta com QR Code salva com sucesso em:\n" + arquivo.getAbsolutePath(),
							"Sucesso", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Erro ao gerar etiqueta com QR Code: " + ex.getMessage(), "Erro",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa para gerar a etiqueta com QR Code.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Atualiza os dados da JTable de caixas com base nos dados do gerenciador.
	private void atualizarVisualizacaoListaCaixas() {
		caixasTableModel.setCaixas(gerenciador.getCaixas());
		atualizarComboFiltroCategoriaBusca();
	}

	// Atualiza a JList de itens com base na caixa selecionada na JTable.
	private void atualizarListaItens() {
		modeloListaItens.clear();
		int selectedRow = tabelaCaixas.getSelectedRow();
		if (selectedRow >= 0) {
			int modelRow = tabelaCaixas.convertRowIndexToModel(selectedRow);
			Caixa caixaSelecionada = caixasTableModel.getCaixaAt(modelRow);
			if (caixaSelecionada != null) {
				for (ItemMudanca item : caixaSelecionada.getItens()) {
					modeloListaItens.addElement(
							item.getNome() + (item.getDescricao().isEmpty() ? "" : " - " + item.getDescricao()));
				}
			}
		}
	}

	// Define a ação para buscar itens, utilizando o termo de busca e o filtro de categoria selecionado.
	private void acaoBuscarItem() {
		String termoBusca = campoBuscaItem.getText();
		String categoriaFiltro = (String) comboFiltroCategoriaBusca.getSelectedItem();
		areaResultadosBusca.setText("");

		List<String> resultados = gerenciador.buscarItens(termoBusca, categoriaFiltro);
		if (resultados.size() == 1
				&& (resultados.get(0).startsWith("Digite um termo") || resultados.get(0).startsWith("Nenhum item"))) {
			areaResultadosBusca.setText(resultados.get(0));
		} else if (!resultados.isEmpty()) {
			areaResultadosBusca.setText(String.join("\n", resultados));
		} else {
			areaResultadosBusca.setText("Nenhum item encontrado com os critérios especificados.");
		}
		areaResultadosBusca.setCaretPosition(0);
	}

	// Define a ação para exportar manualmente todos os dados das caixas.
	private void acaoExportarTudoManual() {
		if (gerenciador.getCaixas().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Não há caixas para exportar.", "Aviso",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Exportar Todas as Caixas");
		fileChooser.setSelectedFile(new File("backup_organizador_mudancas.txt"));
		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File arquivoParaSalvar = fileChooser.getSelectedFile();
			if (gerenciador.exportarDados(arquivoParaSalvar)) {
				JOptionPane.showMessageDialog(this,
						"Dados exportados com sucesso para:\n" + arquivoParaSalvar.getAbsolutePath(),
						"Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Erro ao exportar dados.", "Erro de Exportação",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Define a ação para importar dados de um arquivo, substituindo os dados atuais após confirmação.
	private void acaoImportarTudoManual() {
		int confirmacao = JOptionPane.showConfirmDialog(this,
				"A importação substituirá todas as caixas e itens atuais. Deseja continuar?", "Confirmar Importação",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirmacao != JOptionPane.YES_OPTION) {
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Importar Caixas de Arquivo TXT");
		int userSelection = fileChooser.showOpenDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File arquivoParaImportar = fileChooser.getSelectedFile();
			if (gerenciador.importarDados(arquivoParaImportar)) {
				atualizarVisualizacaoListaCaixas();
				modeloListaItens.clear();
				if (!gerenciador.getCaixas().isEmpty()) {
					tabelaCaixas.setRowSelectionInterval(0, 0);
					atualizarListaItens();
				}
				JOptionPane.showMessageDialog(this,
						"Dados importados com sucesso de:\n" + arquivoParaImportar.getAbsolutePath(),
						"Importação Concluída", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "Erro ao importar dados ou formato de arquivo inválido.",
						"Erro de Importação", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Carrega os dados da sessão anterior ao iniciar a aplicação, se o usuário confirmar.
	private void carregarSessaoAnterior() {
		File arquivoSessao = new File(GerenciadorMudanca.NOME_ARQUIVO_SESSAO);
		if (arquivoSessao.exists() && arquivoSessao.length() > 0) {
			int resposta = JOptionPane.showConfirmDialog(this, "Deseja carregar os dados da última sessão?",
					"Carregar Sessão Anterior", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (resposta == JOptionPane.YES_OPTION) {
				if (!gerenciador.carregarSessao()) {
					JOptionPane.showMessageDialog(this,
							"Não foi possível carregar os dados da sessão anterior. Iniciando uma nova sessão.",
							"Erro ao Carregar Sessão", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		atualizarVisualizacaoListaCaixas();
		modeloListaItens.clear();
		if (!gerenciador.getCaixas().isEmpty()) {
			tabelaCaixas.setRowSelectionInterval(0, 0);
			atualizarListaItens();
		}
	}

	// Salva a sessão atual no arquivo padrão e encerra a aplicação.
	// É chamado quando o usuário tenta fechar a janela.
	private void acaoSalvarSessaoAoFechar() {
		gerenciador.salvarSessao();
		dispose();
		System.exit(0);
	}
}