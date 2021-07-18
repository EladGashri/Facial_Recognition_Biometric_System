package com.biometricsystem.livefeed;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import com.biometricsystem.image.CapturedFrame;
import org.opencv.core.*;


public class JPanelOpenCV extends JPanel{

    private BufferedImage image;

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    public JPanelOpenCV() {
        image=new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
    }

    public JPanelOpenCV(BufferedImage img) {
        image = img;
    }

    public void window(JFrame jframe, BufferedImage img, String text, int x, int y) {
        JPanelOpenCV secondJPanel=new JPanelOpenCV(img);
        jframe.getContentPane().add(secondJPanel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setTitle(text);
        jframe.setSize(img.getWidth(), img.getHeight() + 30);
        jframe.setLocation(x, y);
        jframe.setVisible(true);
    }

    public BufferedImage MatToBufferedImage(Mat frame) {
        frame.get(0, 0, ((DataBufferByte)image.getRaster().getDataBuffer()).getData());
        return image;
    }


    public BufferedImage loadImage(String file) {
        BufferedImage img;
        try {
            File input = new File(file);
            img = ImageIO.read(input);
            return img;
        } catch (Exception e) {
            System.out.println("erro");
        }
        return null;
    }

    public void saveImage(BufferedImage img) {
        try {
            File outputfile = new File("Images/new.png");
            ImageIO.write(img, "png", outputfile);
        } catch (Exception e) {
            System.out.println("error");
        }
    }

    public BufferedImage grayscale(BufferedImage img) {
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                Color c = new Color(img.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor =
                        new Color(
                                red + green + blue,
                                red + green + blue,
                                red + green + blue);

                img.setRGB(j, i, newColor.getRGB());
            }
        }
        return img;
    }

    public class idNumberDialogBox {
        private final JTextField idField = new JTextField(5);
        private final JTextField numberField = new JTextField(5);
        private int result;

        public void setIdAndNumber(CapturedFrame currentFrame, JFrame jframe) {
            JPanel jPanel=new JPanel();
            jframe.add(jPanel);
            jPanel.add(new JLabel("ID:"));
            jPanel.add(idField);
            jPanel.add(Box.createHorizontalStrut(15)); // a spacer
            jPanel.add(new JLabel("Employee number:"));
            jPanel.add(numberField);
            ImageIcon frame=new ImageIcon( currentFrame.getFaceImageAsByteArray());
            result = JOptionPane.showConfirmDialog(jframe, jPanel,
                    "Please Enter ID and employee number Values", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, frame);
        }

        public boolean wasOkPressed(){
            return result == JOptionPane.OK_OPTION;
        }

        public int getEmployeeId(){
            return Integer.parseInt(idField.getText());
        }

        public int getEmployeeNumber(){
            return Integer.parseInt(idField.getText());
        }

    }

}
