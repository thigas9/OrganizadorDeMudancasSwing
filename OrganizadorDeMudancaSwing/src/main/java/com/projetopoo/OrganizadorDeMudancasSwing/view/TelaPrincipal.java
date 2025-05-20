package com.projetopoo.OrganizadorDeMudancasSwing.view;

import com.projetopoo.OrganizadorDeMudancasSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancasSwing.model.ItemMudanca;
import com.projetopoo.OrganizadorDeMudancasSwing.util.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TelaPrincipal extends JFrame {

	private List<Caixa> caixas = new ArrayList<>();

	private DefaultListModel<String> modeloListaCaixas = new DefaultListModel<>();
	private JList<String> listaCaixas = new JList<>(modeloListaCaixas);

	private DefaultListModel<String> modeloListaItens = new DefaultListModel<>();
	private JList<String> listaItens = new JList<>(modeloListaItens);

	private JTextField campoBuscaItem;
	private JButton btnBuscarItem;
	private JTextArea areaResultadosBusca;

	private static final String PREFIXO_CAIXA = "Caixa: ";
	private static final String INDICADOR_ITENS = "Itens:";
	private static final String PREFIXO_ITEM = "- ";
	private static final String SEPARADOR_ITEM_DESCRICAO = " | Descrição: ";
	private static final String SEPARADOR_CAIXAS = "-----------------";
	private static final String NOME_ARQUIVO_SESSAO = "ultima_sessao.txt";

	// Construtor: Inicializa a interface gráfica principal, seus componentes e listeners.
	public TelaPrincipal() {
		setTitle("Organizador de Mudanças");
		setLayout(new BorderLayout(10, 10));
		getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		criarMenu();

		JPanel painelCaixas = new JPanel(new BorderLayout(5, 5));
		painelCaixas.setBorder(BorderFactory.createTitledBorder("Caixas"));
		painelCaixas.add(new JScrollPane(listaCaixas), BorderLayout.CENTER);

		// Alterado para GridLayout para garantir que todos os botões sejam exibidos
		JPanel painelBotoesCaixas = new JPanel(new GridLayout(1, 0, 5, 5)); // 1 linha, colunas conforme necessário, espaçamento 5px
		JButton btnNovaCaixa = new JButton("Nova Caixa");
		JButton btnEditarCaixa = new JButton("Editar Caixa");
		JButton btnRemoverCaixa = new JButton("Remover Caixa");
		painelBotoesCaixas.add(btnNovaCaixa);
		painelBotoesCaixas.add(btnEditarCaixa);
		painelBotoesCaixas.add(btnRemoverCaixa);
		painelCaixas.add(painelBotoesCaixas, BorderLayout.SOUTH);

		JPanel painelItens = new JPanel(new BorderLayout(5, 5));
		painelItens.setBorder(BorderFactory.createTitledBorder("Itens da Caixa"));
		painelItens.add(new JScrollPane(listaItens), BorderLayout.CENTER);

		// Alterado para GridLayout para garantir que todos os botões sejam exibidos
		JPanel painelBotoesItens = new JPanel(new GridLayout(1, 0, 5, 5)); // 1 linha, colunas conforme necessário, espaçamento 5px
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
		splitPane.setResizeWeight(0.5); 
		splitPane.setDividerLocation(0.5); 
		add(splitPane, BorderLayout.CENTER);

		JPanel painelBusca = new JPanel(new BorderLayout(5, 5));
		painelBusca.setBorder(BorderFactory.createTitledBorder("Buscar Item"));

		JPanel painelEntradaBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel labelBuscar = new JLabel("Nome do Item:");
		campoBuscaItem = new JTextField(20);
		btnBuscarItem = new JButton("Buscar");
		painelEntradaBusca.add(labelBuscar);
		painelEntradaBusca.add(campoBuscaItem);
		painelEntradaBusca.add(btnBuscarItem);

		areaResultadosBusca = new JTextArea(5, 0);
		areaResultadosBusca.setEditable(false);
		areaResultadosBusca.setLineWrap(true);
		areaResultadosBusca.setWrapStyleWord(true);

		painelBusca.add(painelEntradaBusca, BorderLayout.NORTH);
		painelBusca.add(new JScrollPane(areaResultadosBusca), BorderLayout.CENTER);

		add(painelBusca, BorderLayout.SOUTH);

		btnNovaCaixa.addActionListener(e -> acaoNovaCaixa());
		btnEditarCaixa.addActionListener(e -> acaoEditarNomeCaixa());
		btnRemoverCaixa.addActionListener(e -> acaoRemoverCaixa());
		listaCaixas.addListSelectionListener(e -> {
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
		setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            splitPane.setDividerLocation(0.5);
        });
	}

	// Cria e configura a barra de menu da aplicação com opções de Arquivo (Exportar, Importar).
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

	// Gerencia a criação de uma nova caixa, solicitando o nome ao usuário,
	// validando nomes duplicados e atualizando a interface.
	private void acaoNovaCaixa() {
		String nome = JOptionPane.showInputDialog(this, "Nome da caixa:", "Nova Caixa", JOptionPane.PLAIN_MESSAGE);
		if (nome != null && !nome.isBlank()) {
			final String nomeTrimmed = nome.trim();
			boolean nomeExistente = caixas.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nomeTrimmed));
			if (nomeExistente) {
				JOptionPane.showMessageDialog(this, "Já existe uma caixa com este nome.", "Erro",
						JOptionPane.ERROR_MESSAGE);
			} else {
				String id = UUID.randomUUID().toString();
				Caixa caixa = new Caixa(id, nomeTrimmed);
				caixas.add(caixa);
				atualizarVisualizacaoListaCaixas();
				listaCaixas.setSelectedIndex(modeloListaCaixas.getSize() - 1);
			}
		}
	}
	
	// Permite editar o nome de uma caixa selecionada, solicitando o novo nome ao usuário,
	// validando contra duplicidade com outras caixas e atualizando a interface.
	private void acaoEditarNomeCaixa() {
		int indexSelecionado = listaCaixas.getSelectedIndex();
		if (indexSelecionado >= 0) {
			Caixa caixaSelecionada = caixas.get(indexSelecionado);
			String nomeAntigo = caixaSelecionada.getNome();
			String novoNome = JOptionPane.showInputDialog(this, "Digite o novo nome para a caixa:", nomeAntigo);

			if (novoNome != null && !novoNome.isBlank()) {
				final String novoNomeTrimmed = novoNome.trim();
				boolean nomeExistenteEmOutraCaixa = caixas.stream()
											  .filter(c -> c != caixaSelecionada) 
											  .anyMatch(c -> c.getNome().equalsIgnoreCase(novoNomeTrimmed));
				
				if (nomeExistenteEmOutraCaixa) {
					JOptionPane.showMessageDialog(this, "Já existe outra caixa com o nome '" + novoNomeTrimmed + "'.", "Erro", JOptionPane.ERROR_MESSAGE);
				} else {
					caixaSelecionada.setNome(novoNomeTrimmed);
					atualizarVisualizacaoListaCaixas();
					listaCaixas.setSelectedIndex(indexSelecionado); 
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
		}
	}

	// Gerencia a remoção de uma caixa selecionada, após confirmação do usuário,
	// e atualiza a interface.
	private void acaoRemoverCaixa() {
		int indexSelecionado = listaCaixas.getSelectedIndex();
		if (indexSelecionado >= 0) {
			String nomeCaixa = modeloListaCaixas.getElementAt(indexSelecionado);
			int confirmacao = JOptionPane.showConfirmDialog(this,
					"Tem certeza que deseja remover a caixa \"" + nomeCaixa + "\" e todos os seus itens?",
					"Confirmar Remoção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (confirmacao == JOptionPane.YES_OPTION) {
				caixas.remove(indexSelecionado);
				atualizarVisualizacaoListaCaixas();
				modeloListaItens.clear();
				if (modeloListaCaixas.getSize() > 0) {
					listaCaixas.setSelectedIndex(Math.max(0, indexSelecionado - 1));
				} else {
					atualizarListaItens();
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa para remover.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Gerencia a adição de um novo item a uma caixa selecionada, solicitando nome e descrição
	// ao usuário e atualizando a interface.
	private void acaoNovoItem() {
		int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
		if (indexCaixaSelecionada >= 0) {
			JTextField campoNomeItem = new JTextField();
			JTextField campoDescricaoItem = new JTextField();
			Object[] campos = { "Nome do item:", campoNomeItem, "Descrição do item:", campoDescricaoItem };
			int option = JOptionPane.showConfirmDialog(this, campos, "Novo Item", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (option == JOptionPane.OK_OPTION) {
				String nome = campoNomeItem.getText();
				String descricao = campoDescricaoItem.getText();

				if (nome != null && !nome.isBlank()) {
					ItemMudanca item = new ItemMudanca(nome.trim(), (descricao != null ? descricao.trim() : ""));
					caixas.get(indexCaixaSelecionada).adicionarItem(item);
					atualizarListaItens();
					listaItens.setSelectedIndex(modeloListaItens.getSize() - 1);
				} else {
					JOptionPane.showMessageDialog(this, "O nome do item não pode ser vazio.", "Aviso",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro para adicionar um item.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Permite editar o nome e a descrição de um item selecionado, solicitando as novas informações
	// ao usuário e atualizando a interface.
	private void acaoEditarItem() {
		int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
		int indexItemSelecionado = listaItens.getSelectedIndex();

		if (indexCaixaSelecionada < 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (indexItemSelecionado < 0) {
			JOptionPane.showMessageDialog(this, "Selecione um item para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Caixa caixaSelecionada = caixas.get(indexCaixaSelecionada);
		ItemMudanca itemSelecionado = caixaSelecionada.getItens().get(indexItemSelecionado);

		JTextField campoNomeItem = new JTextField(itemSelecionado.getNome());
		JTextField campoDescricaoItem = new JTextField(itemSelecionado.getDescricao());
		Object[] campos = { "Nome do item:", campoNomeItem, "Descrição do item:", campoDescricaoItem };

		int option = JOptionPane.showConfirmDialog(this, campos, "Editar Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			String novoNome = campoNomeItem.getText();
			String novaDescricao = campoDescricaoItem.getText();

			if (novoNome != null && !novoNome.isBlank()) {
				itemSelecionado.setNome(novoNome.trim());
				itemSelecionado.setDescricao(novaDescricao != null ? novaDescricao.trim() : "");
				atualizarListaItens();
				listaItens.setSelectedIndex(indexItemSelecionado);
			} else {
				JOptionPane.showMessageDialog(this, "O nome do item não pode ser vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	// Gerencia a remoção de um item selecionado de uma caixa, após confirmação do usuário,
	// e atualiza a interface.
	private void acaoRemoverItem() {
		int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
		int indexItemSelecionado = listaItens.getSelectedIndex();

		if (indexCaixaSelecionada < 0) {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (indexItemSelecionado < 0) {
			JOptionPane.showMessageDialog(this, "Selecione um item para remover.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Caixa caixaSelecionada = caixas.get(indexCaixaSelecionada);
		ItemMudanca itemParaRemover = caixaSelecionada.getItens().get(indexItemSelecionado);

		int confirmacao = JOptionPane.showConfirmDialog(this,
				"Tem certeza que deseja remover o item \"" + itemParaRemover.getNome() + "\"?", "Confirmar Remoção",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (confirmacao == JOptionPane.YES_OPTION) {
			caixaSelecionada.getItens().remove(indexItemSelecionado);
			atualizarListaItens();
			if (modeloListaItens.getSize() > 0) {
				listaItens.setSelectedIndex(Math.max(0, indexItemSelecionado - 1));
			}
		}
	}

	// Gera um QR Code contendo as informações da caixa selecionada e seus itens,
	// permitindo ao usuário salvar a imagem.
	private void acaoGerarQRCode() {
		int index = listaCaixas.getSelectedIndex();
		if (index >= 0) {
			Caixa caixaSelecionada = caixas.get(index);
			StringBuilder textoQR = new StringBuilder();
			textoQR.append(PREFIXO_CAIXA).append(caixaSelecionada.getNome()).append("\n");
			textoQR.append(INDICADOR_ITENS).append("\n");
			for (ItemMudanca item : caixaSelecionada.getItens()) {
				textoQR.append(PREFIXO_ITEM).append(item.getNome()).append(SEPARADOR_ITEM_DESCRICAO)
						.append(item.getDescricao()).append("\n");
			}

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Salvar QR Code");
			fileChooser.setSelectedFile(
					new File("QRCode_" + caixaSelecionada.getNome().replaceAll("[^a-zA-Z0-9.-]", "_") + ".png"));
			int userSelection = fileChooser.showSaveDialog(this);

			if (userSelection == JFileChooser.APPROVE_OPTION) {
				File arquivo = fileChooser.getSelectedFile();
				try {
					QRCodeUtil.gerarQRCode(textoQR.toString(), arquivo.getAbsolutePath());
					JOptionPane.showMessageDialog(this, "QR Code salvo com sucesso em:\n" + arquivo.getAbsolutePath(),
							"Sucesso", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code: " + ex.getMessage(), "Erro",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "Selecione uma caixa primeiro para gerar o QR Code.", "Aviso",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	// Atualiza a JList de caixas na interface para refletir o conteúdo da lista 'caixas' em memória.
	private void atualizarVisualizacaoListaCaixas() {
		modeloListaCaixas.clear();
		for (Caixa caixa : this.caixas) {
			modeloListaCaixas.addElement(caixa.getNome());
		}
	}

	// Atualiza a JList de itens na interface para exibir os itens da caixa atualmente selecionada.
	private void atualizarListaItens() {
		modeloListaItens.clear();
		int indexCaixaSelecionada = listaCaixas.getSelectedIndex();
		if (indexCaixaSelecionada >= 0 && indexCaixaSelecionada < caixas.size()) {
			Caixa caixaSelecionada = caixas.get(indexCaixaSelecionada);
			for (ItemMudanca item : caixaSelecionada.getItens()) {
				modeloListaItens.addElement(
						item.getNome() + (item.getDescricao().isEmpty() ? "" : " - " + item.getDescricao()));
			}
		}
	}

	// Realiza a busca de itens com base no termo digitado pelo usuário no campo de busca
	// e exibe os resultados na área de texto designada.
	private void acaoBuscarItem() {
		String termoBusca = campoBuscaItem.getText().trim().toLowerCase();
		areaResultadosBusca.setText("");

		if (termoBusca.isEmpty()) {
			areaResultadosBusca.setText("Digite um termo para buscar.");
			return;
		}

		StringBuilder resultados = new StringBuilder();
		boolean encontrado = false;

		for (Caixa caixa : caixas) {
			for (ItemMudanca item : caixa.getItens()) {
				if (item.getNome().toLowerCase().contains(termoBusca)
						|| (!item.getDescricao().isEmpty() && item.getDescricao().toLowerCase().contains(termoBusca))) {
					resultados.append("Item: ").append(item.getNome());
					if (!item.getDescricao().isEmpty()) {
						resultados.append(" (Descrição: ").append(item.getDescricao()).append(")");
					}
					resultados.append(" - Encontrado na Caixa: ").append(caixa.getNome()).append("\n");
					encontrado = true;
				}
			}
		}

		if (encontrado) {
			areaResultadosBusca.setText(resultados.toString());
		} else {
			areaResultadosBusca
					.setText("Nenhum item encontrado com o termo '" + campoBuscaItem.getText().trim() + "'.");
		}
		areaResultadosBusca.setCaretPosition(0);
	}

	// Escreve os dados de todas as caixas e seus itens em um arquivo de texto especificado.
	// Usado tanto para exportação manual quanto para salvar a sessão automaticamente.
	// O parâmetro 'mostrarDialogoSucesso' controla a exibição de mensagens ao usuário.
	private boolean escreverDadosEmArquivo(File arquivo, boolean mostrarDialogoSucesso) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
			for (int i = 0; i < caixas.size(); i++) {
				Caixa caixa = caixas.get(i);
				writer.write(PREFIXO_CAIXA + caixa.getNome());
				writer.newLine();
				writer.write(INDICADOR_ITENS);
				writer.newLine();
				for (ItemMudanca item : caixa.getItens()) {
					writer.write(PREFIXO_ITEM + item.getNome() + SEPARADOR_ITEM_DESCRICAO + item.getDescricao());
					writer.newLine();
				}
				if (i < caixas.size() - 1) {
					writer.write(SEPARADOR_CAIXAS);
					writer.newLine();
				}
			}
			if (mostrarDialogoSucesso) {
				JOptionPane.showMessageDialog(this, "Dados salvos com sucesso em:\n" + arquivo.getAbsolutePath(),
						"Salvo com Sucesso", JOptionPane.INFORMATION_MESSAGE);
			}
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			if (mostrarDialogoSucesso) {
				JOptionPane.showMessageDialog(this, "Erro ao salvar dados: " + ex.getMessage(), "Erro ao Salvar",
						JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
	}

	// Lê os dados de caixas e itens de um arquivo de texto e os carrega na aplicação.
	// Usado tanto para importação manual quanto para carregar a sessão automaticamente.
	// O parâmetro 'mostrarDialogoSucessoEClearExistente' controla a exibição de mensagens
	// e se os dados existentes devem ser limpos antes da importação (caso de importação manual).
	private boolean lerDadosDeArquivo(File arquivo, boolean mostrarDialogoSucessoEClearExistente) {
		List<Caixa> caixasImportadasTemporariamente = new ArrayList<>();
		Caixa caixaAtual = null;

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
			String linha;
			while ((linha = reader.readLine()) != null) {
				linha = linha.trim();
				if (linha.startsWith(PREFIXO_CAIXA)) {
					String nomeCaixa = linha.substring(PREFIXO_CAIXA.length());
					caixaAtual = new Caixa(UUID.randomUUID().toString(), nomeCaixa);
					caixasImportadasTemporariamente.add(caixaAtual);
				} else if (linha.startsWith(PREFIXO_ITEM) && caixaAtual != null) {
					String dadosItem = linha.substring(PREFIXO_ITEM.length());
					String nomeItem;
					String descricaoItem = "";
					int idxSeparador = dadosItem.indexOf(SEPARADOR_ITEM_DESCRICAO);
					if (idxSeparador != -1) {
						nomeItem = dadosItem.substring(0, idxSeparador);
						descricaoItem = dadosItem.substring(idxSeparador + SEPARADOR_ITEM_DESCRICAO.length());
					} else {
						nomeItem = dadosItem;
					}
					caixaAtual.adicionarItem(new ItemMudanca(nomeItem, descricaoItem));
				} else if (linha.equals(SEPARADOR_CAIXAS)) {
					caixaAtual = null;
				}
			}

			if (mostrarDialogoSucessoEClearExistente) {
				this.caixas.clear();
			}

			if (!caixasImportadasTemporariamente.isEmpty() || mostrarDialogoSucessoEClearExistente) {
				this.caixas.clear();
				this.caixas.addAll(caixasImportadasTemporariamente);
			}

			atualizarVisualizacaoListaCaixas();
			modeloListaItens.clear();

			if (!this.caixas.isEmpty()) {
				listaCaixas.setSelectedIndex(0);
				atualizarListaItens();
			} else {
				atualizarListaItens();
			}

			if (mostrarDialogoSucessoEClearExistente) {
				JOptionPane.showMessageDialog(this, "Dados importados com sucesso de:\n" + arquivo.getAbsolutePath(),
						"Importação Concluída", JOptionPane.INFORMATION_MESSAGE);
			}
			return true;

		} catch (IOException ex) {
			ex.printStackTrace();
			if (mostrarDialogoSucessoEClearExistente) {
				JOptionPane.showMessageDialog(this, "Erro ao importar dados: " + ex.getMessage(), "Erro de Importação",
						JOptionPane.ERROR_MESSAGE);
			}
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			if (mostrarDialogoSucessoEClearExistente) {
				JOptionPane.showMessageDialog(this,
						"Erro no formato do arquivo ou ao processar os dados: " + ex.getMessage(), "Erro de Formato",
						JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}
	}

	// Permite ao usuário exportar manualmente todas as caixas e itens para um arquivo de texto.
	private void acaoExportarTudoManual() {
		if (caixas.isEmpty()) {
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
			escreverDadosEmArquivo(arquivoParaSalvar, true);
		}
	}

	// Permite ao usuário importar caixas e itens de um arquivo de texto,
	// substituindo os dados atuais após confirmação.
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
			lerDadosDeArquivo(arquivoParaImportar, true);
		}
	}

	// Verifica se existe um arquivo de sessão anterior e pergunta ao usuário se deseja carregá-lo.
	// Chamado na inicialização da aplicação.
	private void carregarSessaoAnterior() {
		File arquivoSessao = new File(NOME_ARQUIVO_SESSAO);
		if (arquivoSessao.exists() && arquivoSessao.length() > 0) {
			int resposta = JOptionPane.showConfirmDialog(this, "Deseja carregar os dados da última sessão?",
					"Carregar Sessão Anterior", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (resposta == JOptionPane.YES_OPTION) {
				if (!lerDadosDeArquivo(arquivoSessao, false)) {
					JOptionPane.showMessageDialog(this,
							"Não foi possível carregar os dados da sessão anterior. Iniciando uma nova sessão.",
							"Erro ao Carregar Sessão", JOptionPane.ERROR_MESSAGE);
					this.caixas.clear();
					this.atualizarVisualizacaoListaCaixas();
					this.modeloListaItens.clear();
				}
			}
		}
	}

	// Salva o estado atual da aplicação (caixas e itens) no arquivo de sessão
	// e encerra a aplicação. Chamado quando o usuário fecha a janela.
	private void acaoSalvarSessaoAoFechar() {
		escreverDadosEmArquivo(new File(NOME_ARQUIVO_SESSAO), false);
		dispose();
		System.exit(0);
	}
}