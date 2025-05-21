package com.projetopoo.OrganizadorDeMudancasSwing.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException; // Importar para tratar a exceção específica
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO; // Para salvar a imagem final
import java.awt.Color;        // Para cores
import java.awt.Font;         // Para fontes
import java.awt.FontMetrics;  // Para medir texto
import java.awt.Graphics2D;   // Para desenhar na imagem
import java.awt.RenderingHints; // Para qualidade do desenho
import java.awt.image.BufferedImage; // Para manipular imagens em memória
import java.io.File;          // Para o arquivo de saída
import java.io.IOException;   // Para exceções de I/O
import java.util.EnumMap;
import java.util.List;        // Para receber as linhas de texto
import java.util.Map;

public class QRCodeUtil {

    private static final int LARGURA_QR_CODE_PADRAO = 200; // Largura do QR Code em si
    private static final int ALTURA_QR_CODE_PADRAO = 200;  // Altura do QR Code em si
    
    // Constantes para layout da etiqueta
    private static final int MARGEM_LATERAL_ETIQUETA = 20;
    private static final int MARGEM_SUPERIOR_ETIQUETA = 20;
    private static final int MARGEM_INFERIOR_ETIQUETA = 20;
    private static final int ESPACO_ENTRE_LINHAS_TEXTO = 5;
    private static final int ESPACO_ENTRE_TEXTO_E_QR = 15;
    
    private static final Font FONTE_CABECALHO = new Font("Arial", Font.BOLD, 16);
    private static final Font FONTE_DETALHES = new Font("Arial", Font.PLAIN, 12);

    // Gera uma etiqueta contendo múltiplas linhas de texto descritivo acima de um QR Code.
    public static void gerarEtiquetaComQRCode(String dadosParaCodigoQR, List<String> linhasTextoDescritivo, String caminhoArquivo)
            throws WriterException, IOException {

        // 1. Gerar a imagem do QR Code em memória
        Map<EncodeHintType, Object> hintsQR = new EnumMap<>(EncodeHintType.class);
        hintsQR.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintsQR.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L); // Nível de correção de erro
        hintsQR.put(EncodeHintType.MARGIN, 1); // Margem interna do QR Code

        BitMatrix bitMatrix = new MultiFormatWriter().encode(dadosParaCodigoQR, BarcodeFormat.QR_CODE, LARGURA_QR_CODE_PADRAO, ALTURA_QR_CODE_PADRAO, hintsQR);
        BufferedImage imagemQRCode = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // 2. Calcular dimensões da imagem final da etiqueta
        // Usa uma imagem temporária para obter métricas da fonte
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dTemp = tempImg.createGraphics();
        
        int alturaTotalTexto = 0;
        int larguraTextoMaxima = 0;

        if (linhasTextoDescritivo != null && !linhasTextoDescritivo.isEmpty()) {
            // Calcula altura e largura para o texto do cabeçalho (primeira linha)
            g2dTemp.setFont(FONTE_CABECALHO);
            FontMetrics fmCabecalho = g2dTemp.getFontMetrics();
            alturaTotalTexto += fmCabecalho.getHeight();
            larguraTextoMaxima = Math.max(larguraTextoMaxima, fmCabecalho.stringWidth(linhasTextoDescritivo.get(0)));

            // Calcula altura e largura para as linhas de detalhes restantes
            if (linhasTextoDescritivo.size() > 1) {
                g2dTemp.setFont(FONTE_DETALHES);
                FontMetrics fmDetalhes = g2dTemp.getFontMetrics();
                for (int i = 1; i < linhasTextoDescritivo.size(); i++) {
                    alturaTotalTexto += ESPACO_ENTRE_LINHAS_TEXTO + fmDetalhes.getHeight();
                    larguraTextoMaxima = Math.max(larguraTextoMaxima, fmDetalhes.stringWidth(linhasTextoDescritivo.get(i)));
                }
            }
        }
        g2dTemp.dispose();

        int larguraEtiqueta = Math.max(larguraTextoMaxima, imagemQRCode.getWidth()) + (2 * MARGEM_LATERAL_ETIQUETA);
        int alturaEtiqueta = MARGEM_SUPERIOR_ETIQUETA + alturaTotalTexto + 
                             (alturaTotalTexto > 0 ? ESPACO_ENTRE_TEXTO_E_QR : 0) + // Adiciona espaço só se houver texto
                             imagemQRCode.getHeight() + MARGEM_INFERIOR_ETIQUETA;
        
        // Garante uma largura mínima para a etiqueta, caso o texto seja muito curto
        larguraEtiqueta = Math.max(larguraEtiqueta, LARGURA_QR_CODE_PADRAO + 2 * MARGEM_LATERAL_ETIQUETA + 40);


        // 3. Criar a imagem final da etiqueta e desenhar os componentes
        BufferedImage imagemEtiquetaFinal = new BufferedImage(larguraEtiqueta, alturaEtiqueta, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dEtiqueta = imagemEtiquetaFinal.createGraphics();

        // Configurações de renderização para alta qualidade
        g2dEtiqueta.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2dEtiqueta.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2dEtiqueta.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2dEtiqueta.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Preenche o fundo da etiqueta com branco
        g2dEtiqueta.setColor(Color.WHITE);
        g2dEtiqueta.fillRect(0, 0, larguraEtiqueta, alturaEtiqueta);
        g2dEtiqueta.setColor(Color.BLACK); // Define a cor para o texto e QR Code

        // Desenhar as linhas de texto descritivo (centralizadas horizontalmente)
        int yAtual = MARGEM_SUPERIOR_ETIQUETA;
        if (linhasTextoDescritivo != null && !linhasTextoDescritivo.isEmpty()) {
            // Primeira linha (cabeçalho)
            g2dEtiqueta.setFont(FONTE_CABECALHO);
            FontMetrics fmCabecalho = g2dEtiqueta.getFontMetrics();
            String linhaCabecalho = linhasTextoDescritivo.get(0);
            int larguraLinhaCabecalho = fmCabecalho.stringWidth(linhaCabecalho);
            g2dEtiqueta.drawString(linhaCabecalho, (larguraEtiqueta - larguraLinhaCabecalho) / 2, yAtual + fmCabecalho.getAscent());
            yAtual += fmCabecalho.getHeight();

            // Linhas de detalhes restantes
            g2dEtiqueta.setFont(FONTE_DETALHES);
            FontMetrics fmDetalhes = g2dEtiqueta.getFontMetrics();
            for (int i = 1; i < linhasTextoDescritivo.size(); i++) {
                yAtual += ESPACO_ENTRE_LINHAS_TEXTO;
                String linhaDetalhe = linhasTextoDescritivo.get(i);
                int larguraLinhaDetalhe = fmDetalhes.stringWidth(linhaDetalhe);
                g2dEtiqueta.drawString(linhaDetalhe, (larguraEtiqueta - larguraLinhaDetalhe) / 2, yAtual + fmDetalhes.getAscent());
                yAtual += fmDetalhes.getHeight();
            }
            yAtual += ESPACO_ENTRE_TEXTO_E_QR; // Espaço antes do QR Code
        } else {
            // Se não houver texto, posiciona o QR Code mais acima
            yAtual = MARGEM_SUPERIOR_ETIQUETA;
        }


        // Desenhar o QR Code centralizado abaixo do texto
        int qrX = (larguraEtiqueta - imagemQRCode.getWidth()) / 2;
        g2dEtiqueta.drawImage(imagemQRCode, qrX, yAtual, null);

        g2dEtiqueta.dispose(); // Libera os recursos gráficos

        // 4. Salvar a imagem final da etiqueta
        File arquivoSaida = new File(caminhoArquivo);
        ImageIO.write(imagemEtiquetaFinal, "PNG", arquivoSaida);
    }
}