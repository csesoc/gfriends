import java.io.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.awt.event.*;

public class gfred_help_frame extends Frame implements MouseListener, WindowListener {

    public gfred_help_frame() {
	super("Graphical Friends Help and Features");
	this.setSize(400,430);
	this.setBackground(Color.white);
	this.addMouseListener(this);
	this.addWindowListener(this);
	this.setVisible(true);
	repaint();
    }    

    public void paint(Graphics g) {
	Font old_font = g.getFont();

	g.setFont(new Font(old_font.getName(), Font.BOLD, old_font.getSize() + 4));

	g.setColor(Color.black);
	
	g.drawString("Features in Graphical Friends (gfriends)", 3, 17 + 30);
	g.drawLine(2,19 + 30,298,19 + 30);
	g.setFont(old_font);
	
	g.setColor(Color.red);
	g.drawString("  Click anywhere in this window the close it.", 3, 30 + 30);
	g.drawLine(2, 32 + 30, 298, 32 + 30);
	g.setColor(Color.black);

	g.drawString("~ Move the mouse over a printer to view its status:", 3, 45 + 30);
	g.drawString("   FREE = the printer is free and not in use.", 3, 60 + 30);
	g.drawString("   3 QUEUED JOBS - there are, for example, 3 jobs printing", 3, 75 + 30);
	g.drawString("   STALLED - the printer is stalled. Hunt down the staller", 3, 90 + 30);
	g.drawString("~ Click [show ids] in the key to make random's logins visible", 3, 105 + 30);
	g.drawString("~ Friends from your friends list (~/.friends) are green", 3, 120 + 30);
	g.drawString("~ Random people show up as blue", 3, 135 + 30);
	g.drawString("~ You can have more than these two groups by defining your own.", 3, 150 + 30);
	g.drawString("   Add the following line above a group of entries in your", 3, 165 + 30);
	g.drawString("   friends list file to add them to a specialised group", 3, 180 + 30);
	g.drawString("   \"group:<name of group>:R,G,B\"", 3, 195 + 30);
	g.drawString("   where R,G,B are the comma delimited RGB colour values", 3, 210 + 30);
	g.drawString("   For example, to keep track of my SENG2020 students I had", 3, 225 + 30);
	g.drawString("   \"group:seng2b groups:255,220,220\"", 3, 240 + 30);
	g.drawString("   above the list of entries of my students", 3, 255 + 30);
	g.drawString("   * gfriends automatically assumes that if no group is", 3, 270 + 30);
	g.drawString("     defined, then the standard \"friends\" group is used", 3, 285 + 30);
	g.drawString("   * You must make sure your own definitions are correct,", 3, 300 + 30);
	g.drawString("     as I cbf adding error handling code into gfriends.", 3, 315 + 30);
	g.drawString("~ Click on a person to show their details.", 3, 330 + 30);
	g.drawString("~ Click on a lab name to see if it is booked during the day.", 3, 345 + 30);
	g.drawString("   * Labs booked include classes, closure and special bookings.", 3, 360 + 30);
	g.drawString("~ Right click a person to bring up an options menu.", 3, 375 + 30);
	
	g.setColor(Color.blue);
	g.drawLine(2, 330 + 33 + 45, 298, 330 + 33 + 45);
	g.drawString("Graphical Friends: By scos506 and ijgo605", 3, 345 + 45+30);
	g.setColor(Color.black);
	
	g.setFont(old_font);
    }

    public void mouseClicked(MouseEvent e) {
	this.setVisible(false);
    }

    public void mouseReleased(MouseEvent e) {
	
    }

    public void mousePressed(MouseEvent e) {
	
    }

    public void mouseEntered(MouseEvent e) {
	
    }

    public void mouseExited(MouseEvent e) {
	
    }

    public void windowActivated(WindowEvent e) {
	
    }
    
    public void windowClosed(WindowEvent e) {
	this.setVisible(false);
    }

    public void windowClosing(WindowEvent e) {
	this.setVisible(false);
    }
    
    public void windowDeactivated(WindowEvent e) {
	
    }
    
    public void windowDeiconified(WindowEvent e) {
	
    }

    public void windowIconified(WindowEvent e) {
	
    }
    
    public void windowOpened(WindowEvent e) {
	
    }

}
