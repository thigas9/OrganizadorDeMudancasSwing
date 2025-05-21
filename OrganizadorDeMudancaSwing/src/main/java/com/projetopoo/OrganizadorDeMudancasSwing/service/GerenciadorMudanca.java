package com.projetopoo.OrganizadorDeMudancasSwing.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// import java.util.stream.Collectors; // Import não utilizado, pode ser removido se não for usado em outro lugar.

import com.projetopoo.OrganizadorDeMudancasSwing.model.Caixa;
import com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaComum;
import com.projetopoo.OrganizadorDeMudancasSwing.model.CaixaFragil;
import com.projetopoo.OrganizadorDeMudancasSwing.model.ItemMudanca;
import com.projetopoo.OrganizadorDeMudancasSwing.model.StatusCaixa;

public class GerenciadorMudanca {

	private List<Caixa> caixas;

	public static final String MARCADOR_TIPO_CAIXA = "TipoCaixa: ";
	public static final String PREFIXO_CAIXA = "Caixa: ";
	public static final String MARCADOR_CATEGORIA_CAIXA = "Categoria: ";
	public static final String MARCADOR_STATUS_CAIXA = "Status: ";
	public static final String MARCADOR_INSTRUCOES_CUIDADO = "InstrucoesCuidado: ";
	public static final String INDICADOR_ITENS = "Itens:";
	public static final String PREFIXO_ITEM = "- ";
	public static final String SEPARADOR_ITEM_DESCRICAO = " | Descrição: ";
	public static final String SEPARADOR_CAIXAS = "-----------------";
	public static final String NOME_ARQUIVO_SESSAO = "ultima_sessao.txt";

	// Construtor: Inicializa o gerenciador com uma lista vazia de caixas.
	public GerenciadorMudanca() {
		this.caixas = new ArrayList<>();
	}

	// Retorna uma cópia da lista de todas as caixas gerenciadas, para proteger a lista interna.
	public List<Caixa> getCaixas() {
		return new ArrayList<>(this.caixas);
	}

	// Adiciona uma nova caixa à lista, validando o nome e o tipo.
	// Retorna true se adicionada com sucesso, false caso contrário (ex: nome duplicado ou inválido).
	public boolean adicionarCaixa(String nome, String tipo, String categoria, StatusCaixa status,
			String instrucoesCuidado) {
		if (nome == null || nome.isBlank()) {
			return false;
		}
		final String nomeTrimmed = nome.trim();
		boolean nomeExistente = caixas.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nomeTrimmed));
		if (nomeExistente) {
			return false;
		}

		Caixa novaCaixa;
		String id = UUID.randomUUID().toString();
		if ("Frágil".equals(tipo)) {
			novaCaixa = new CaixaFragil(id, nomeTrimmed, categoria, status, instrucoesCuidado);
		} else {
			novaCaixa = new CaixaComum(id, nomeTrimmed, categoria, status);
		}
		this.caixas.add(novaCaixa);
		return true;
	}

	// Edita os dados de uma caixa existente.
	// Retorna true se a edição foi bem-sucedida, false caso contrário (ex: nome duplicado em outra caixa ou nome inválido).
	public boolean editarCaixa(Caixa caixaParaEditar, String novoNome, String novaCategoria, StatusCaixa novoStatus,
			String novasInstrucoes) {
		if (caixaParaEditar == null || novoNome == null || novoNome.isBlank()) {
			return false;
		}
		final String novoNomeTrimmed = novoNome.trim();
		boolean nomeExistenteEmOutraCaixa = caixas.stream().filter(c -> c != caixaParaEditar)
				.anyMatch(c -> c.getNome().equalsIgnoreCase(novoNomeTrimmed));
		if (nomeExistenteEmOutraCaixa) {
			return false;
		}

		caixaParaEditar.setNome(novoNomeTrimmed);
		caixaParaEditar.setCategoria(novaCategoria);
		caixaParaEditar.setStatus(novoStatus);
		if (caixaParaEditar instanceof CaixaFragil) {
			((CaixaFragil) caixaParaEditar).setInstrucoesCuidado(novasInstrucoes);
		}
		return true;
	}

	// Remove uma caixa específica da lista de caixas.
	// Retorna true se a caixa foi encontrada e removida, false caso contrário.
	public boolean removerCaixa(Caixa caixa) {
		return this.caixas.remove(caixa);
	}

	// Remove uma caixa da lista pelo seu índice.
	// Retorna a caixa removida ou null se o índice for inválido.
	public Caixa removerCaixa(int index) {
		if (index >= 0 && index < this.caixas.size()) {
			return this.caixas.remove(index);
		}
		return null;
	}

	// Adiciona um novo item a uma caixa especificada.
	public void adicionarItemEmCaixa(Caixa caixa, String nomeItem, String descricaoItem) {
		if (caixa != null && nomeItem != null && !nomeItem.isBlank()) {
			ItemMudanca item = new ItemMudanca(nomeItem.trim(), (descricaoItem != null ? descricaoItem.trim() : ""));
			caixa.adicionarItem(item);
		}
	}

	// Edita os dados de um item existente.
	// Retorna true se a edição foi bem-sucedida, false caso contrário (ex: nome do item inválido).
	public boolean editarItemEmCaixa(ItemMudanca itemParaEditar, String novoNome, String novaDescricao) {
		if (itemParaEditar != null && novoNome != null && !novoNome.isBlank()) {
			itemParaEditar.setNome(novoNome.trim());
			itemParaEditar.setDescricao(novaDescricao != null ? novaDescricao.trim() : "");
			return true;
		}
		return false;
	}

	// Remove um item de uma caixa específica, usando o índice do item.
	public void removerItemDeCaixa(Caixa caixa, int itemIndex) {
		if (caixa != null) {
			caixa.removerItem(itemIndex);
		}
	}

	// Busca itens com base em um termo de pesquisa (no nome ou descrição do item) e, opcionalmente, filtra por categoria da caixa.
	// Retorna uma lista de strings formatadas descrevendo os itens encontrados e suas localizações.
	public List<String> buscarItens(String termoBuscaOriginal, String categoriaFiltro) {
		List<String> resultadosFormatados = new ArrayList<>();
		if (termoBuscaOriginal == null || termoBuscaOriginal.isBlank()) {
			resultadosFormatados.add("Digite um termo para buscar.");
			return resultadosFormatados;
		}
		String termoBuscaLower = termoBuscaOriginal.trim().toLowerCase();
		boolean filtroCategoriaAtivo = categoriaFiltro != null
				&& !categoriaFiltro.equalsIgnoreCase("Todas as Categorias") && !categoriaFiltro.isBlank();
		boolean encontrado = false;

		for (Caixa caixa : caixas) {
			if (filtroCategoriaAtivo && !caixa.getCategoria().equalsIgnoreCase(categoriaFiltro)) {
				continue;
			}

			for (ItemMudanca item : caixa.getItens()) {
				if (item.getNome().toLowerCase().contains(termoBuscaLower) || (!item.getDescricao().isEmpty()
						&& item.getDescricao().toLowerCase().contains(termoBuscaLower))) {
					StringBuilder sb = new StringBuilder();
					sb.append("Item: ").append(item.getNome());
					if (!item.getDescricao().isEmpty()) {
						sb.append(" (Descrição: ").append(item.getDescricao()).append(")");
					}
					sb.append(" - Caixa: ").append(caixa.getNome()).append(" (Tipo: ").append(caixa.getTipoCaixa())
							.append(", Cat: ").append(caixa.getCategoria()).append(", Status: ")
							.append(caixa.getStatus().toString()).append(")");
					resultadosFormatados.add(sb.toString());
					encontrado = true;
				}
			}
		}

		if (!encontrado) {
			resultadosFormatados.add("Nenhum item encontrado com o termo '" + termoBuscaOriginal.trim() + "' "
					+ (filtroCategoriaAtivo ? "na categoria '" + categoriaFiltro + "'" : "") + ".");
		}
		return resultadosFormatados;
	}

	// Salva o estado atual das caixas no arquivo de sessão padrão.
	// Retorna true se a operação for bem-sucedida, false caso contrário.
	public boolean salvarSessao() {
		return escreverDadosEmArquivo(new File(NOME_ARQUIVO_SESSAO));
	}

	// Carrega o estado das caixas a partir do arquivo de sessão padrão.
	// Se o arquivo não existir ou estiver vazio, inicia com uma lista limpa.
	// Retorna true se a operação for bem-sucedida (incluindo carregar uma sessão vazia), false se ocorrer um erro de leitura.
	public boolean carregarSessao() {
		File arquivoSessao = new File(NOME_ARQUIVO_SESSAO);
		if (arquivoSessao.exists() && arquivoSessao.length() > 0) {
			return lerDadosDeArquivo(arquivoSessao);
		}
		this.caixas.clear();
		return true;
	}

	// Exporta os dados atuais das caixas para um arquivo especificado pelo usuário.
	// Retorna true se a operação for bem-sucedida, false caso contrário.
	public boolean exportarDados(File arquivo) {
		return escreverDadosEmArquivo(arquivo);
	}

	// Importa dados de caixas de um arquivo especificado pelo usuário, substituindo os dados atuais.
	// Retorna true se a operação for bem-sucedida, false caso contrário.
	public boolean importarDados(File arquivo) {
		return lerDadosDeArquivo(arquivo);
	}

	// Método privado auxiliar para escrever os dados da lista de caixas em um arquivo.
	// Retorna true se a escrita for bem-sucedida, false caso contrário.
	private boolean escreverDadosEmArquivo(File arquivo) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
			for (int i = 0; i < caixas.size(); i++) {
				Caixa caixa = caixas.get(i);
				writer.write(MARCADOR_TIPO_CAIXA + caixa.getTipoCaixa());
				writer.newLine();
				writer.write(PREFIXO_CAIXA + caixa.getNome());
				writer.newLine();
				writer.write(MARCADOR_CATEGORIA_CAIXA + caixa.getCategoria());
				writer.newLine();
				writer.write(MARCADOR_STATUS_CAIXA + caixa.getStatus().name());
				writer.newLine();

				String detalhesEspecificos = caixa.getDetalhesEspecificosParaSalvar();
				if (detalhesEspecificos != null && !detalhesEspecificos.isEmpty()) {
					writer.write(detalhesEspecificos);
					writer.newLine();
				}

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
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// Método privado auxiliar para ler os dados de caixas de um arquivo e popular a lista interna.
	// Retorna true se a leitura for bem-sucedida, false caso contrário.
	private boolean lerDadosDeArquivo(File arquivo) {
		List<Caixa> caixasLidas = new ArrayList<>();
		Caixa caixaAtual = null;
		String tipoCaixaAtual = null;
		String nomeCaixaAtual = null;
		String categoriaCaixaAtual = "Geral";
		StatusCaixa statusCaixaAtual = StatusCaixa.A_EMBALAR;
		String instrucoesCaixaFragil = null;
		String idCaixaAtual = null;

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
			String linha;
			while ((linha = reader.readLine()) != null) {
				linha = linha.trim();

				if (linha.startsWith(MARCADOR_TIPO_CAIXA)) {
					tipoCaixaAtual = linha.substring(MARCADOR_TIPO_CAIXA.length());
					idCaixaAtual = UUID.randomUUID().toString();
					instrucoesCaixaFragil = null;
					nomeCaixaAtual = null;
					categoriaCaixaAtual = "Geral";
					statusCaixaAtual = StatusCaixa.A_EMBALAR;
					caixaAtual = null;
				} else if (linha.startsWith(PREFIXO_CAIXA)) {
					nomeCaixaAtual = linha.substring(PREFIXO_CAIXA.length());
				} else if (linha.startsWith(MARCADOR_CATEGORIA_CAIXA)) {
					categoriaCaixaAtual = linha.substring(MARCADOR_CATEGORIA_CAIXA.length());
				} else if (linha.startsWith(MARCADOR_STATUS_CAIXA)) {
					statusCaixaAtual = StatusCaixa.fromString(linha.substring(MARCADOR_STATUS_CAIXA.length()));
				} else if (linha.startsWith(MARCADOR_INSTRUCOES_CUIDADO)) {
					if ("Fragil".equals(tipoCaixaAtual)) {
						instrucoesCaixaFragil = linha.substring(MARCADOR_INSTRUCOES_CUIDADO.length());
					}
				} else if (linha.equals(INDICADOR_ITENS)) {
					if (tipoCaixaAtual != null && nomeCaixaAtual != null) {
						if ("Fragil".equals(tipoCaixaAtual)) {
							caixaAtual = new CaixaFragil(idCaixaAtual, nomeCaixaAtual, categoriaCaixaAtual,
									statusCaixaAtual, instrucoesCaixaFragil);
						} else {
							caixaAtual = new CaixaComum(idCaixaAtual, nomeCaixaAtual, categoriaCaixaAtual,
									statusCaixaAtual);
						}
						caixasLidas.add(caixaAtual);
					}
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
					tipoCaixaAtual = null;
					nomeCaixaAtual = null;
					categoriaCaixaAtual = "Geral";
					statusCaixaAtual = StatusCaixa.A_EMBALAR;
					instrucoesCaixaFragil = null;
				}
			}

			this.caixas.clear();
			this.caixas.addAll(caixasLidas);
			return true;

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
}