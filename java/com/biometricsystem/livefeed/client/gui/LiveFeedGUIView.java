package com.biometricsystem.livefeed.client.gui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class LiveFeedGUIView extends JFrame {

    private LiveFeedStandGUIPanel panel;
    private JTextField identifiedIdField;
    private  JTextField identifiedEmployeeNumberField;
    private JButton okButton;
    private JButton cancelButton;
    private final static Image LOGO=new ImageIcon("src\\main\\resources\\static\\images\\logos\\amdocs.jpg").getImage();
    public final static Dimension DIMENSION=new Dimension(500,500);
    public final static String TITLE="Live Feed";

    public LiveFeedGUIView() {
        panel = new LiveFeedStandGUIPanel();
        add(panel);
        setIconImage(LOGO);
        setTitle(TITLE);
        setSize(DIMENSION);
        panel.setSize(DIMENSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        identifiedEmployeeNumberField = new JTextField("Employee number",10);
        identifiedEmployeeNumberField.setVisible(true);
        panel.add(identifiedEmployeeNumberField);
        identifiedIdField = new JTextField("ID",10);
        identifiedIdField.setVisible(true);
        panel.add(identifiedIdField);
        okButton=new JButton("OK");
        panel.add(okButton);
        cancelButton=new JButton("Cancel");
        panel.add(cancelButton);
        panel.setVisible(true);
        setVisible(true);
        hideIdentificationComponents();
    }

    public String getIdentifiedId(){
        return identifiedIdField.getText();
    }

    public String getIdentifiedEmployeeNumber(){
        return identifiedEmployeeNumberField.getText();
    }

    public void setCancelButtonActionListener(ActionListener listener){
        cancelButton.addActionListener(listener);
    }

    public void setOKButtonActionListener(ActionListener listener){
        okButton.addActionListener(listener);
    }

    public void setPanelIcon(ImageIcon icon){
        panel.setIcon(icon);
        panel.repaint();
    }

    public void clearPanel(){
        setPanelIcon(null);
    }

    public void showIdentificationComponents() {
        identifiedIdField.setVisible(true);
        identifiedEmployeeNumberField.setVisible(true);
        okButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    public void makeCancelButtonVisible(){
        cancelButton.setVisible(true);
    }

    public void hideIdentificationComponents() {
        identifiedIdField.setVisible(false);
        identifiedEmployeeNumberField.setVisible(false);
        okButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    public void resetLabels(){
        identifiedIdField.setText("ID");
        identifiedEmployeeNumberField.setText("Employee number");
    }

    public class LiveFeedStandGUIPanel extends JPanel {

        private ImageIcon icon;

        public LiveFeedStandGUIPanel() {
            icon=null;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (icon!=null) {
                g.drawImage(icon.getImage(),0,0,LiveFeedGUIView.DIMENSION.width,LiveFeedGUIView.DIMENSION.height,null);
            }else{
                g.setColor(Color.white);
                g.fillRect(0,0, LiveFeedGUIView.DIMENSION.width,LiveFeedGUIView.DIMENSION.height);
            }
        }

        public void setIcon(ImageIcon icon){
            this.icon=icon;
        }

    }

}