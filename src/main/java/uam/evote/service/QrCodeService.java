package uam.evote.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QrCodeService {

    public void genererImagePNG(String contenu, String cheminFichier, int taille) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(contenu, BarcodeFormat.QR_CODE, taille, taille, hints);

            BufferedImage image = new BufferedImage(taille, taille, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < taille; x++) {
                for (int y = 0; y < taille; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }

            ImageIO.write(image, "PNG", new File(cheminFichier));

        } catch (WriterException e) {
            throw new IOException("Erreur lors de la génération du QR code", e);
        }
    }
}