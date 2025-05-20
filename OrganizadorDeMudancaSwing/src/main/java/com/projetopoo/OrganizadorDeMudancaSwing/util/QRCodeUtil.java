package com.projetopoo.OrganizadorDeMudancaSwing.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.EncodeHintType;
import java.util.EnumMap;
import java.util.Map;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeUtil {

    public static void gerarQRCode(String texto, String caminhoArquivo) throws Exception {
        int largura = 300;
        int altura = 300;
        String formato = "UTF-8";
        
        // Seta o formato UTF-8
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, formato);

        BitMatrix matrix = new MultiFormatWriter().encode(texto, BarcodeFormat.QR_CODE, largura, altura, hints);
        Path caminho = FileSystems.getDefault().getPath(caminhoArquivo);
        MatrixToImageWriter.writeToPath(matrix, "PNG", caminho);
    }
}
