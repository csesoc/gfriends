
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class gfred_tutor_options_menu extends Frame implements MouseListener {

    String lab;
    boolean mouse_entered = false;
    gfred parent;
    
    public gfred_tutor_options_menu(int mx, int my, String lab_, gfred parent_) {
	super();
	
	this.setBackground(Color.black);
	this.setUndecorated(true);
	this.setLocation(mx, my);
	this.setSize(160, 80);
	this.addMouseListener(this);
	this.setVisible(true);
	lab = lab_;
	parent = parent_;
    }
    
    public void paint(Graphics g) {
	g.setColor(new Color(255,255,0));
	g.fillRoundRect(6,2,g.getFontMetrics().stringWidth(lab) + 4,70,5,5);
	g.setColor(new Color(0,255,0));
	g.fillRoundRect(2,22,g.getFontMetrics().stringWidth("Add lab to friends list") + 4, 16, 5,5);
	g.fillRoundRect(2,42,g.getFontMetrics().stringWidth("Gen name cheat sheet") + 4, 16, 5,5);
	
	g.setColor(Color.black);
	g.drawString(lab, 8, 15);
	
	g.drawString("Add lab to friends list", 3, 35);
	g.drawString("Gen name cheat sheet", 3, 55);
	
    }

    public boolean inRange(int a, int n, int b) {
	return (a <= n && n <= b);
    }

    public void mouseClicked(MouseEvent e) {
	if (inRange(20, e.getY(), 39)) {
	    parent.dumpLabToFriendsList(lab);
	    this.setVisible(false);
	    parent.rescanLabs();
	}
	if (inRange(40, e.getY(), 59)) {	  
	    parent.genPDFCheatSheet(lab);
	    this.setVisible(false);
	}

    }
    
    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {
	mouse_entered = true;
    }
    
    public void mouseExited(MouseEvent e) {	
	if (mouse_entered) this.setVisible(false);
    }

    
}

