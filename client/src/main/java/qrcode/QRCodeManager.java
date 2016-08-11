package qrcode;

import net.glxn.qrgen.javase.QRCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by alexanderweiss
 * Class for representing a manager for maintaining qr codes
 */
public class QRCodeManager {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(QRCodeManager.class);

    /**
     * Default constructor
     */
    public QRCodeManager(){
    }

    /**
     * Generate an QR code with an given string
     * @param string
     * @return
     * @throws IOException
     */
    public static BufferedImage generateQRCode(String string, int width, int height) throws IOException{
        logger.info("Create QR-Code image");
        return ImageIO.read(new ByteArrayInputStream(QRCode.from(string).withSize(width, height).stream().toByteArray()));
    }

    /**
     * Generate an QR Code with an text underneath the qr code
     * NOT USED IN CURRENT SCOPE
     * @param string
     * @param id
     * @param key
     * @return
     * @throws IOException
     */
    public static BufferedImage generateQRCode(String string, String id , String key, int width, int height) throws IOException{
        System.out.println("QR WITH TEXT");
        BufferedImage qrcode =  ImageIO.read(new ByteArrayInputStream(QRCode.from(string).withSize(width, height).stream().toByteArray()));
        Graphics g = qrcode.getGraphics();
        float fontSize = (float) 0.05*height;
        g.setFont(g.getFont().deriveFont(fontSize));
        g.setColor(Color.BLUE);

        g.drawString("ID: " +id, 0 , height);
        g.drawString("Key: " + key, 0 , height -g.getFontMetrics().getHeight());
        g.dispose();
        //ImageIO.write(qrcode, "png", new File("test.png"));
        return qrcode;
    }
}
