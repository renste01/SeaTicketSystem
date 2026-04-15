package dk.easv.seaticketsystem.GUI.Util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class QRGenerator {

    public static Image generateQRCode(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            com.google.zxing.common.BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);

        } catch (WriterException e) {
            throw new RuntimeException("Kunne ikke generere QR kode", e);
        }
    }
}
