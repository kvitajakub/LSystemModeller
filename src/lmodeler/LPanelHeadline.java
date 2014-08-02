package lmodeler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Trida pro hlavicku vsech panelu.
 * @author Kvita Jakub
 */
public class LPanelHeadline extends JPanel {

    private static final long serialVersionUID = 1L;

    JTabbedPane pane;
    JLabel label;
	
    /**
     * Inicializace, prida jmeno a tlacitko zavreni panelu.
     * @param name Jmeno panelu
     * @param pane TabbedPane ve kterem jsou vsechny panely.
     */
    public LPanelHeadline(String name,JTabbedPane pane) {
            super();

            this.pane=pane;

            setOpaque(false);

            label = new JLabel(name); 
            add(label);

        JButton button = new TabButton();
        add(button);

    }
	
    @Override
    /**
     * Nastavi text zobrazujici se v hlavicce.
     * @param name Zobrazeny text.
     */
    public void setName(String name){
            this.label.setText(name);
    }
	
    private class TabButton extends JButton implements ActionListener {
		private static final long serialVersionUID = 1L;

		public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }
 
        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(LPanelHeadline.this);
            if (i != -1) {
                pane.remove(i);
            }
        }
 
        //we don't want to update UI for this button
        @Override
        public void updateUI() {
        }
 
        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
 
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
 
        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}
