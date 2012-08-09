//Graphics Friends List - based on the friends script written by trents@cse
//By Stephen Cossell (scos506) and Ian Goldsmith (ijgo605)

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;

/*
--new definition of gfred.gfred--

+- [0-1]("gr" flag)
|  [2-$](name),(red),(green),(blue)
+- [0-3](labname)
   +- [4]  (n=lNormal,x=lDown,c=lClass
   |  [5-8](##.#=temperature)
   |  [9-$](classname(if_exist))
   +- [4-5](machine_number)
      [6]  (o=occupied,d=down,b=booked,a=available)
      [7]  (u=unoccupied,m=murder,s=senger,r=random,f=fred)
      [8-$](username)=(nick)=(group_index)
*/

class CSEMachine {
    int x;
    int y;
    int orient;

    String buffer = ""; //soon to be deprecated
    
    char status = 'z';      // (o,d,b,a)
    char user_type;         // (u,m,s,r,f)
    String username = "";
    String nickname = "";
    int group_index = -1;

    boolean highlighted = false;

    // used as a cache for results of the getUserInfo() function, so you don't 
    //   have to call the function multiple times on the same person
    String user_info = "";

    float alpha = 1.0f;
}

class CSEGroup {
    String name;
    Color colour;
    boolean show_names;
}

class CSELab {
    boolean visible;

    int id;

    int[][] coords = new int[2][6];
    int x,y; // coords of the top left corner
    int width, height; // max width and height of lab

    String buffer = ""; //soon to be depreciated
    
    char status = 'z';
    float therm;
    String course = "";

    String name = "";
    int name_x, name_y;
    
    float alpha = 1.0f;

    int [] zoomed_close_button_coords = {-1, -1};
    int [] zoomed_options_button_coords = {-1, -1};

    
    public boolean mouseInLab(int x_, int y_) {
	if (x <= x_ && x_ <= (x + width) &&
	    y <= y_ && y_ <= (y + height)) {
	    return true;
	} else {
	    return false;
	}
    }


}

class CSEPrinter {
    boolean visible;
    
    String name;
    int x, y; // of the upper left corner, and all printers are 20x20
    String details = "";

    float alpha = 1.0f;
}

class CSEServer {
    String name;
    int x,y;
    String details = "";

    float alpha = 0.8f;
}

class CSEAdBanner {
    int x = 425;
    int y = 520;
    int w = 340;
    int h = 150;
    
    String image_filename = "";
    String link = "";
    
    BufferedImage img = null;

    public CSEAdBanner() {
	String config_filename = "banner.txt";	

	try {
	    BufferedReader in = new BufferedReader(new FileReader(config_filename));
	    String buffer = "";
	    while ((buffer = in.readLine()) != null) {
		if (buffer.startsWith("#")) continue; // comment line
		if (buffer.startsWith("image:")) image_filename = buffer.substring(6);
		if (buffer.startsWith("link:")) link = buffer.substring(5);		
	    }
	    in.close();

	    if (!image_filename.equals("null")) {
		img = ImageIO.read(new File(image_filename));
	    }
	} catch (IOException ex) {
	    System.out.println("OMG");
	    ex.printStackTrace();
	}		
    }

    public void paint(Graphics g) {
	if (img != null) {
	    g.drawImage(img, x, y, w, h, null);
	}
	g.setColor(Color.black);
	g.drawRect(x,y,w,h);

	g.setColor(Color.black);
	g.drawString("Click the banner for more details...", x+5, y+h+10);
	
	
	//g.drawString("CSESoc Soccer wed 1-3pm Village Green", x+5, y-50);
	
	//g.setColor(new Color(0,255,0));
	//g.fillRect(x, y-45, 200, 5);
	
    }

    public boolean mouseClicked(int mx, int my) {
	
	if (x <= mx && mx <= (x+w) &&
	    y <= my && my <= (y+h)) {
	    //System.out.println("DEBUG: AdBanner Clicked");
	    try {
		String params[] = {"firefox", link};
		Process pinger = Runtime.getRuntime().exec(params);
	    } catch (IOException ex) {
		System.out.println("ERROR: Could not open banner link");
	    }
	    //System.out.println("DEBUG: Firefox called");
	    return true;
	} else {
	    return false;
	}

    }
}

class InfoWindow {
    String name;
    String classes;
    String teachings = "";
    int x,y;
    boolean visible = false;
    
    float alpha = 0.75f;
    
    public void paintWin(Graphics g) {
	if (!visible) return;
	if (name == null) return;

	if (name.startsWith("506")) {
	    if (classes.equals("")) classes = "Not today";
	    
	    int half_name_width = (int)Math.round(g.getFontMetrics().stringWidth(name.substring(3)) / 2.0);
	    	   		    
	    g.setColor(new Color(1.0f,0.0f,0.0f, alpha));
	    g.fillRoundRect(x - half_name_width - 3, y, 2 * half_name_width + 6, 15, 5, 5);
	    g.setColor(Color.white);
	    g.drawString(name.substring(3), x - half_name_width, y + 12);

	    int overlap_offset = 0;
	    int classes_width = g.getFontMetrics().stringWidth("Lab booked: " + classes);
	    
	    if (classes_width + x > 1194) overlap_offset = x + classes_width - 1194;
	    overlap_offset += half_name_width;
	    
	    g.setColor(new Color(220/255.0f,0.0f,0.0f, alpha));
	    g.fillRoundRect(x - overlap_offset - 3, y+15, g.getFontMetrics().stringWidth("Lab booked: " + classes) + 6, 15, 5, 5);
	    g.setColor(Color.white);
	    g.drawString("Lab booked: " + classes, x - overlap_offset, y + 27);
	} else {
	    if (classes.equals("")) classes = "None";
	    int overlap_offset = 0;
	    
	    int name_width = g.getFontMetrics().stringWidth(name);
	    int classes_width = g.getFontMetrics().stringWidth("Classes:" + classes);
	    
	    if (name_width + x > 1194) overlap_offset = x + name_width - 1194;
	    if (classes_width + x > 1194) overlap_offset = Math.max(overlap_offset, x + classes_width - 1194);

	    g.setColor(new Color(1.0f, 0.86f, 0.20f, alpha));
	    g.fillRoundRect(x - overlap_offset,y, name_width + 6,20,5,5);
	    g.setColor(new Color(1.0f, 0.86f, 0.0f, alpha));
	    g.fillRoundRect(x - overlap_offset,y+15, classes_width + 6, 15, 5, 5);
	    if (!teachings.equals("")) {
		g.setColor(new Color(1.0f, 1.0f, 0.0f, alpha));
		g.fillRoundRect(x - overlap_offset, y+30, 
				g.getFontMetrics().stringWidth("Tutoring: " + teachings) + 6, 15, 5, 5);
	    }
	    
	    g.setColor(Color.black);
	    g.drawString(name, x+3-overlap_offset, y + 12);
	    g.drawString("Classes:" + classes, x+3-overlap_offset, y + 12 + 15);
	    if (!teachings.equals("")) {
		g.drawString("Tutoring: " + teachings, x+3-overlap_offset, y + 12 + 30);
	    }
	}
    }
}











//------------------------------------------------------------------->
public class gfred extends JPanel implements MouseListener, MouseMotionListener, ActionListener, KeyListener /*, Runnable*/ {

    String keychain;

    String current_fortune;

    CSELab zoomed_lab = null;

    boolean user_using_laptop = false;
    boolean sc_in_office = false;
    String user_username = "";
    
    InfoWindow info_window;

    boolean paint_random_people_logins = false;

    int selected_printer = -1;

    String cur_printer_info = null;

    int mouseX, mouseY;

    JScrollPane scrollpane;
    Frame window;
    Label status;
    Button closer;
    Button refresher;
    Button helper;
    Button searcher;
    TextField search_text;
    
    boolean DEBUG = false;

    int NUMBER_OF_LABS = 21;
    int MAX_NUMBER_OF_MACHINES_IN_LAB = 29;
    int NUMBER_OF_COORDINATES = 2;
    int VERTICES_IN_LAB = 6;
    int MAX_NUMBER_OF_GROUPS = 8;
    int NUMBER_OF_PRINTERS = 10;

    int HARP = 0; int TUBA = 1; int DRUM = 2;
    int BONG = 3; int MOOG = 4; int BELL = 5; int LEAF = 6; int SPOO = 7;
    int BUGL = 8; int PIPE = 9;
    int BANJ = 10;int OUDX = 11;
    int CLAV = 12; int PIAN = 13; int ORGA = 14;
    int WEIL = 15; int WAGN = 16; int WILL = 17;
    int OBOE = 18;
    int SANH = 19; int ERHU = 20;

    int PSHOOTER = 0, PITCH = 1, // EE printers
	PLIGHT = 2, POUCH = 3, // dungeon printers
	PKABOO = 4, PLUMP = 5, // mech eng printers
	PLUCK = 6, PANTS = 7, // eatery printers
	PROOF = 8, PRUDE = 9; // banjo/oud
    

    int X = 0, Y = 1;

    int X_COORD = 0; int Y_COORD = 1; int ORIENT = 2;

    //machine location constatnts
    CSEMachine[][] machines = new CSEMachine[NUMBER_OF_LABS][MAX_NUMBER_OF_MACHINES_IN_LAB];

    CSELab[] labs = new CSELab[NUMBER_OF_LABS];

    CSEPrinter[] printers = new CSEPrinter[NUMBER_OF_PRINTERS];

    ArrayList<CSEGroup> groups = new ArrayList<CSEGroup>();

    Color cLabDown = new Color(65, 65, 65);
    Color cLabClass = new Color(180, 0, 0);
    Color cLabNormal = new Color(70, 130, 200);
    Color cLabServer = new Color(0, 125, 0);

    Color cMachineOccupied = new Color(0, 0, 255);
    Color cMachineBooked = new Color(255, 0, 0);
    Color cMachineDown = new Color(0, 0, 0);
    Color cMachineAvailable = new Color(255, 255, 255);

    Color cUserMurder = new Color(255, 0, 0);
    Color cUserFred = new Color(0, 255, 0);
    Color cUserSeng = new Color(255, 100, 180);
    Color cUserRandom = new Color(0, 0, 255);

    Color cPrinter = new Color(255, 200, 50);

    CSEAdBanner ad_banner = new CSEAdBanner();

    /**
       Sets up all the constants and the layout and loads lab info.
    */
    public void init() {
	keychain = "";

	/*
	try {
	    String params[] = {"./run_alias_update.pl"};
	    Process pinger = Runtime.getRuntime().exec(params);
	} catch (Exception ex) {
	    // if something fk's up then don't worry about it
	}
	*/

	try {
	    String params[] = {"./get_fortune.sh"};
	    Process pinger = Runtime.getRuntime().exec(params);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pinger.getInputStream()));
	    
	    this.current_fortune = in.readLine();
	} catch (Exception ex) {
	    this.current_fortune = "";
	}

	Dimension dim = null;
	try {
	    dim = Toolkit.getDefaultToolkit().getScreenSize();
	} catch (HeadlessException exer) {
	    System.out.println("----------------------------------------\nYou don't have an X display to show the interface.\nIf you are using PuTTy try using vncviewer or Xmanager (or apparently even Xming).\nIf you used ssh to connect then try adding -X to the ssh command.\n----------------------------------------\n");
	    System.exit(1);
	}
	
	if (dim.height < 950 || dim.width < 1200) {
	    user_using_laptop = true;
	}

	info_window = new InfoWindow();

        //load the background
	this.addMouseListener(this);
	this.addMouseMotionListener(this);
	this.addKeyListener(this);

	Cursor cur = new Cursor(Cursor.CROSSHAIR_CURSOR);
        setBackground(Color.white);
	this.setCursor(cur);

	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    labs[ll] = new CSELab();
	}

  	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    for (int nn = 0; nn < MAX_NUMBER_OF_MACHINES_IN_LAB; nn++) {
		machines[ll][nn] = new CSEMachine();
	    }
	}
	
	for (int pp = 0; pp < NUMBER_OF_PRINTERS; pp++) {
	    printers[pp] = new CSEPrinter();
	}

        setHarpConstants();
        setTubaConstants();
        setDrumConstants();
	setOboeConstants();
        setBongConstants();
        setMoogConstants();
        setBellConstants();
        setLeafConstants();
        setSpooConstants();
        setBuglConstants();
        setPipeConstants();
        setBanjConstants();
        setOudxConstants();
        setClavConstants();
        setPianConstants();
        setOrgaConstants();
	setSanhConstants();
	setErhuConstants();
	setServerConstants();

	setPrinterConstants();

	loadGfredFile();

	//this.start();    

	try {
	    window = new Frame("Graphical Lab Monitor (By Steve (scos506) and Ian (ijgo605))");
	} catch (HeadlessException exer) {
	    System.out.println("You don't have an X display to show the interface. If you are using PuTTy try using vncviewer or Xmanager.");
	    System.exit(1);
	}

	window.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    }
				 );
	
	status = new Label("The window is active");
	this.setSize(1200, 950);
	
	Container topPanel = new Container();
	topPanel.setLayout(new BorderLayout());
      
	closer = new Button("Close Window");
	closer.addActionListener(this);
	closer.setBackground(new Color(200,200,200));

	refresher = new Button("Rescan Labs");
	refresher.addActionListener(this);
	refresher.setBackground(new Color(200,200,200));
	
	helper = new Button("Help");
	helper.addActionListener(this);
	helper.setBackground(new Color(200,200,200));

	searcher = new Button("RegExp Search");
	searcher.addActionListener(this);
	searcher.setBackground(new Color(255, 200, 0));
	
	search_text = new TextField();
	search_text.setBackground(new Color(255, 255, 225));
	
	Container topRightGrid = new Container();
	topRightGrid.setLayout(new GridLayout(1,4));
	
	topRightGrid.add(helper);
	topRightGrid.add(refresher);

	topRightGrid.add(search_text);
	topRightGrid.add(searcher);

	//DRAW CREDIT LINE
	topPanel.setBackground(Color.white);
	topPanel.add("Center", new Label("Created by (scos506), developed by (ijgo605) and (scossell), refined by (stevec) (under constant beta testing)."));
	topPanel.add("West", closer);
	topPanel.add("East", topRightGrid);

	window.setLayout(new BorderLayout());
	
	window.add("North", topPanel);
	if (user_using_laptop) {
	    this.setPreferredSize(new Dimension(1200, 950));
	    scrollpane = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	    window.add("Center", scrollpane);
	} else {
	    window.add("Center", this);
	}
	window.add("South", status);
	
	//System.out.println("DEBUG::Everything is init'd ready for display");

	window.pack();

	//System.out.println("DEBUG::Window is packed");

	if (user_using_laptop) {
	    window.setSize(Math.min(1200, dim.width), Math.min(950+20, dim.height));
	} else {
	    window.setSize(1200, 950+20); // adds 20 for winow title (thx ijgo);
	}

	//System.out.println("DEBUG::Window about to be made visible");

	window.setVisible(true);

	//System.out.println("DEBUG::Everything should be showing by now");
	
	this.requestFocus();
    }

    public void setHarpConstants() {

    	if (DEBUG) System.out.println("setHarpConstants");

	labs[HARP].visible = false;
	labs[HARP].id = HARP;

	labs[HARP].x = 55;
	labs[HARP].y = 15;
	// everything from here on is in local coords, offset by the above values

	labs[HARP].width = 180;
	labs[HARP].height = 270;

	labs[HARP].name = "HARP";
	labs[HARP].name_x = 65;
	labs[HARP].name_y = 70;
    
        labs[HARP].coords[X][0] = 0;
        labs[HARP].coords[X][1] = 180;
        labs[HARP].coords[X][2] = 180;
        labs[HARP].coords[X][3] = 60;
        labs[HARP].coords[X][4] = 60;
        labs[HARP].coords[X][5] = 0;

        labs[HARP].coords[Y][0] = 0;
        labs[HARP].coords[Y][1] = 0;
        labs[HARP].coords[Y][2] = 270;
        labs[HARP].coords[Y][3] = 270;
        labs[HARP].coords[Y][4] = 90;
        labs[HARP].coords[Y][5] = 90;

        /*HARP MACHINES POSITION CONSTANTS DEFINITIONS*/ /*D O N E*/
        machines[HARP][0].x = 165;machines[HARP][0].y = 165;machines[HARP][0].orient = 2;
        machines[HARP][1].x = 165;machines[HARP][1].y = 135;machines[HARP][1].orient = 1;
        machines[HARP][2].x = 135;machines[HARP][2].y = 135;machines[HARP][2].orient = 0;
        machines[HARP][3].x = 135;machines[HARP][3].y = 105;machines[HARP][3].orient = 3;
        machines[HARP][4].x = 165;machines[HARP][4].y = 105;machines[HARP][4].orient = 2;
        machines[HARP][5].x = 165;machines[HARP][5].y = 75;machines[HARP][5].orient = 1;
        machines[HARP][6].x = 165;machines[HARP][6].y = 45;machines[HARP][6].orient = 2;
        machines[HARP][7].x = 165;machines[HARP][7].y = 15;machines[HARP][7].orient = 1;
        machines[HARP][8].x = 135;machines[HARP][8].y = 15;machines[HARP][8].orient = 0;
        machines[HARP][9].x = 105; machines[HARP][9].y = 15;machines[HARP][9].orient = 1;
        machines[HARP][10].x = 75;machines[HARP][10].y = 15;machines[HARP][10].orient = 0;
        machines[HARP][11].x = 45;machines[HARP][11].y = 15;machines[HARP][11].orient = 1;
        machines[HARP][12].x = 25;machines[HARP][12].y = 75;machines[HARP][12].orient = 3;
        machines[HARP][13].x = 75;machines[HARP][13].y = 105;machines[HARP][13].orient = 0;
        machines[HARP][14].x = 75;machines[HARP][14].y = 135;machines[HARP][14].orient = 3;
        machines[HARP][15].x = 75;machines[HARP][15].y = 165;machines[HARP][15].orient = 0;
        machines[HARP][16].x = 75;machines[HARP][16].y = 195;machines[HARP][16].orient = 3;
        machines[HARP][17].x = 75;machines[HARP][17].y = 255;machines[HARP][17].orient = 3;
        machines[HARP][18].x = 105;machines[HARP][18].y = 255;machines[HARP][18].orient = 2;
        machines[HARP][19].x = 135;machines[HARP][19].y = 255;machines[HARP][19].orient = 3;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {	
	    machines[HARP][i].x = 0;machines[HARP][i].y = 0;machines[HARP][i].orient = 4;
	}
    }

    public void setTubaConstants() {

    	if (DEBUG) System.out.println("setTubaConstants");

	labs[TUBA].visible = false;
	labs[TUBA].id = TUBA;

	labs[TUBA].x = 235;
	labs[TUBA].y = 15;
	// everything following this is in local coords offset by the above values

	labs[TUBA].width = 240;
	labs[TUBA].height = 180;

	labs[TUBA].name = "TUBA";
	labs[TUBA].name_x = 85;
	labs[TUBA].name_y = 115;

        labs[TUBA].coords[X][0] = 0;
        labs[TUBA].coords[X][1] = 120;
        labs[TUBA].coords[X][2] = 240;
        labs[TUBA].coords[X][3] = 240;
        labs[TUBA].coords[X][4] = 120;
        labs[TUBA].coords[X][5] = 0;

        labs[TUBA].coords[Y][0] = 0;
        labs[TUBA].coords[Y][1] = 0;
        labs[TUBA].coords[Y][2] = 0;
        labs[TUBA].coords[Y][3] = 180;
        labs[TUBA].coords[Y][4] = 180;
        labs[TUBA].coords[Y][5] = 180;

        /*TUBA MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[TUBA][0].x = 225;machines[TUBA][0].y = 165;machines[TUBA][0].orient = 2;
        machines[TUBA][1].x = 225;machines[TUBA][1].y = 135;machines[TUBA][1].orient = 1;
        machines[TUBA][2].x = 225;machines[TUBA][2].y = 45;machines[TUBA][2].orient = 2;
        machines[TUBA][3].x = 225;machines[TUBA][3].y = 15;machines[TUBA][3].orient = 1;
        machines[TUBA][4].x = 135;machines[TUBA][4].y = 15;machines[TUBA][4].orient = 0;
        machines[TUBA][5].x = 135;machines[TUBA][5].y = 45;machines[TUBA][5].orient = 3;
        machines[TUBA][6].x = 105;machines[TUBA][6].y = 45;machines[TUBA][6].orient = 2;
        machines[TUBA][7].x = 105;machines[TUBA][7].y = 15;machines[TUBA][7].orient = 1;
        machines[TUBA][22].x = 105;machines[TUBA][22].y = 15;machines[TUBA][22].orient = 1;
        machines[TUBA][23].x = 45;machines[TUBA][23].y = 15;machines[TUBA][23].orient = 1;
        machines[TUBA][8].x = 15;machines[TUBA][8].y = 15;machines[TUBA][8].orient = 0;
        machines[TUBA][9].x = 15;machines[TUBA][9].y = 45;machines[TUBA][9].orient = 3;
        machines[TUBA][10].x = 15;machines[TUBA][10].y = 75;machines[TUBA][10].orient = 0;
        machines[TUBA][11].x = 15;machines[TUBA][11].y = 105;machines[TUBA][11].orient = 3;
        machines[TUBA][12].x = 15;machines[TUBA][12].y = 135;machines[TUBA][12].orient = 0;
        machines[TUBA][13].x = 15;machines[TUBA][13].y = 165;machines[TUBA][13].orient = 3;
        machines[TUBA][14].x = 45;machines[TUBA][14].y = 165;machines[TUBA][14].orient = 2;
        machines[TUBA][15].x = 75;machines[TUBA][15].y = 165;machines[TUBA][15].orient = 3;
        machines[TUBA][16].x = 105;machines[TUBA][16].y = 165;machines[TUBA][16].orient = 2;
        machines[TUBA][17].x = 105;machines[TUBA][17].y = 135;machines[TUBA][17].orient = 1;
        machines[TUBA][18].x = 135;machines[TUBA][18].y = 135;machines[TUBA][18].orient = 0;
        machines[TUBA][19].x = 135;machines[TUBA][19].y = 165;machines[TUBA][19].orient = 3;
        machines[TUBA][20].x = 165;machines[TUBA][20].y = 165;machines[TUBA][20].orient = 2;
	for (int i = 21; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[TUBA][i].x = 0;machines[TUBA][i].y = 0;machines[TUBA][i].orient = 4;
	}
    }

    public void setDrumConstants() {

    	if (DEBUG) System.out.println("setDrumConstants");

	labs[DRUM].visible = false;
	labs[DRUM].id = DRUM;

	labs[DRUM].x = 235;
	labs[DRUM].y = 195;
	// everything from here down is in local coord, offset by the above values

	labs[DRUM].width = 240;
	labs[DRUM].height = 180;

	labs[DRUM].name = "DRUM";
	labs[DRUM].name_x = 85;
	labs[DRUM].name_y = 75;
    
        labs[DRUM].coords[X][0] = 0;
        labs[DRUM].coords[X][1] = 240;
        labs[DRUM].coords[X][2] = 240;
        labs[DRUM].coords[X][3] = 120;
        labs[DRUM].coords[X][4] = 120;
        labs[DRUM].coords[X][5] = 0;

        labs[DRUM].coords[Y][0] = 0;
        labs[DRUM].coords[Y][1] = 0;
        labs[DRUM].coords[Y][2] = 180;
        labs[DRUM].coords[Y][3] = 180;
        labs[DRUM].coords[Y][4] = 120;
        labs[DRUM].coords[Y][5] = 120;

        /*DRUM MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[DRUM][0].x = 225;machines[DRUM][0].y = 165;machines[DRUM][0].orient = 2;
        machines[DRUM][1].x = 225;machines[DRUM][1].y = 135;machines[DRUM][1].orient = 1;
        machines[DRUM][2].x = 195;machines[DRUM][2].y = 135;machines[DRUM][2].orient = 0;
        machines[DRUM][3].x = 195;machines[DRUM][3].y = 105;machines[DRUM][3].orient = 3;
        machines[DRUM][4].x = 225;machines[DRUM][4].y = 105;machines[DRUM][4].orient = 2;
        machines[DRUM][5].x = 225;machines[DRUM][5].y = 75;machines[DRUM][5].orient = 1;
        machines[DRUM][6].x = 225;machines[DRUM][6].y = 45;machines[DRUM][6].orient = 2;
        machines[DRUM][7].x = 225;machines[DRUM][7].y = 15;machines[DRUM][7].orient = 1;
        machines[DRUM][8].x = 165;machines[DRUM][8].y = 15;machines[DRUM][8].orient = 1;
        machines[DRUM][9].x = 135;machines[DRUM][9].y = 15;machines[DRUM][9].orient = 0;
        machines[DRUM][10].x = 135;machines[DRUM][10].y = 45;machines[DRUM][10].orient = 3;
        machines[DRUM][11].x = 105;machines[DRUM][11].y = 45;machines[DRUM][11].orient = 2;
        machines[DRUM][12].x = 105;machines[DRUM][12].y = 15;machines[DRUM][12].orient = 1;
        machines[DRUM][13].x = 75;machines[DRUM][13].y = 15;machines[DRUM][13].orient = 0;
        machines[DRUM][14].x = 45;machines[DRUM][14].y = 15;machines[DRUM][14].orient = 1;
        machines[DRUM][15].x = 15;machines[DRUM][15].y = 75;machines[DRUM][15].orient = 0;
        machines[DRUM][16].x = 15;machines[DRUM][16].y = 105;machines[DRUM][16].orient = 3;
        machines[DRUM][17].x = 45;machines[DRUM][17].y = 105;machines[DRUM][17].orient = 2;
        machines[DRUM][18].x = 75;machines[DRUM][18].y = 105;machines[DRUM][18].orient = 3;
        machines[DRUM][19].x = 105;machines[DRUM][19].y = 105;machines[DRUM][19].orient = 2;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[DRUM][i].x = 0;machines[DRUM][i].y = 0;machines[DRUM][i].orient = 4;
	}

    }

    public void setOboeConstants() {
	
	if (DEBUG) System.out.println("setOboeConstants");

	labs[OBOE].visible = false;
	labs[OBOE].id = OBOE;

	labs[OBOE].x = 15;
	labs[OBOE].y = 135;
	// all values below are in local coords, offset by the above values

	labs[OBOE].width = 220;
	labs[OBOE].height = 240;
	
	labs[OBOE].name = "OBOE";
	labs[OBOE].name_x = 95;
	labs[OBOE].name_y = 195;

	labs[OBOE].coords[X][0] = 0;
	labs[OBOE].coords[X][1] = 100;
	labs[OBOE].coords[X][2] = 100;
	labs[OBOE].coords[X][3] = 220;
	labs[OBOE].coords[X][4] = 220;
	labs[OBOE].coords[X][5] = 0;

	labs[OBOE].coords[Y][0] = 0;
	labs[OBOE].coords[Y][1] = 0;
	labs[OBOE].coords[Y][2] = 150;
	labs[OBOE].coords[Y][3] = 150;
	labs[OBOE].coords[Y][4] = 240;
	labs[OBOE].coords[Y][5] = 240;

	/*OBOE MACHINE POSITION CONSTANTS DEFINITIONS*/
	machines[OBOE][0].x = 205;machines[OBOE][0].y = 225;machines[OBOE][0].orient = 3;
	machines[OBOE][1].x = 205;machines[OBOE][1].y = 165;machines[OBOE][1].orient = 1;
	machines[OBOE][2].x = 175;machines[OBOE][2].y = 165;machines[OBOE][2].orient = 0;
	machines[OBOE][3].x = 145;machines[OBOE][3].y = 165;machines[OBOE][3].orient = 1;
	machines[OBOE][4].x = 115;machines[OBOE][4].y = 165;machines[OBOE][4].orient = 0;
	machines[OBOE][5].x = 85;machines[OBOE][5].y = 165;machines[OBOE][5].orient = 1;
	machines[OBOE][6].x = 85;machines[OBOE][6].y = 135;machines[OBOE][6].orient = 0;
	machines[OBOE][7].x = 45;machines[OBOE][7].y = 15;machines[OBOE][7].orient = 1;
	machines[OBOE][8].x = 15;machines[OBOE][8].y = 15;machines[OBOE][8].orient = 0;
	machines[OBOE][9].x = 15;machines[OBOE][9].y = 45;machines[OBOE][9].orient = 3;
	machines[OBOE][10].x = 15;machines[OBOE][10].y = 75;machines[OBOE][10].orient = 0;
	machines[OBOE][11].x = 15;machines[OBOE][11].y = 105;machines[OBOE][11].orient = 3;
	machines[OBOE][12].x = 15;machines[OBOE][12].y = 135;machines[OBOE][12].orient = 0;
	machines[OBOE][13].x = 15;machines[OBOE][13].y = 165;machines[OBOE][13].orient = 3;
	machines[OBOE][14].x = 15;machines[OBOE][14].y = 195;machines[OBOE][14].orient = 0;
	machines[OBOE][15].x = 15;machines[OBOE][15].y = 225;machines[OBOE][15].orient = 3;
	machines[OBOE][16].x = 45;machines[OBOE][16].y = 225;machines[OBOE][16].orient = 2;
	machines[OBOE][17].x = 75;machines[OBOE][17].y = 225;machines[OBOE][17].orient = 3;
	machines[OBOE][18].x = 105;machines[OBOE][18].y = 225;machines[OBOE][18].orient = 2;
	machines[OBOE][19].x = 135;machines[OBOE][19].y = 225;machines[OBOE][19].orient = 3;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {	    
	    machines[OBOE][i].x = 0;machines[OBOE][i].y = 0;machines[OBOE][i].orient = 4;
	}
		   
    }

    public void setBongConstants() {

    	if (DEBUG) System.out.println("setBongConstants");
	
	int exists = 0;

	labs[BONG].visible = false;
	labs[BONG].id = BONG;

	labs[BONG].x = 555 * exists;
	labs[BONG].y = 15 * exists;
	// below values are in local coords, with the offset of the above values

	labs[BONG].width = 180 * exists;
	labs[BONG].height = 180 * exists;

	labs[BONG].name = "BONGO";
	labs[BONG].name_x = 75 * exists;
	labs[BONG].name_y = 105 * exists;

	labs[BONG].coords[X][0] = 0 * exists;
	labs[BONG].coords[X][1] = 90 * exists;
	labs[BONG].coords[X][2] = 180 * exists;
	labs[BONG].coords[X][3] = 180 * exists;
	labs[BONG].coords[X][4] = 90 * exists;
	labs[BONG].coords[X][5] = 0 * exists;

	labs[BONG].coords[Y][0] = 0 * exists;
	labs[BONG].coords[Y][1] = 0 * exists;
	labs[BONG].coords[Y][2] = 0 * exists;
	labs[BONG].coords[Y][3] = 180 * exists;
	labs[BONG].coords[Y][4] = 180 * exists;
	labs[BONG].coords[Y][5] = 180 * exists;

    
        /*BONG MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[BONG][0].x = 165 * exists;machines[BONG][0].y = 135 * exists;machines[BONG][0].orient = 1;
        machines[BONG][1].x = 165 * exists;machines[BONG][1].y = 105 * exists;machines[BONG][1].orient = 2;
        machines[BONG][2].x = 165 * exists;machines[BONG][2].y = 75 * exists;machines[BONG][2].orient = 1;
        machines[BONG][3].x = 165 * exists;machines[BONG][3].y = 45 * exists;machines[BONG][3].orient = 2;
        machines[BONG][4].x = 165 * exists;machines[BONG][4].y = 15 * exists;machines[BONG][4].orient = 1;
        machines[BONG][5].x = 105 * exists;machines[BONG][5].y = 15 * exists;machines[BONG][5].orient = 0;
        machines[BONG][6].x = 75 * exists;machines[BONG][6].y = 15 * exists;machines[BONG][6].orient = 1;
        machines[BONG][7].x = 15 * exists;machines[BONG][7].y = 15 * exists;machines[BONG][7].orient = 0;
        machines[BONG][8].x = 15 * exists;machines[BONG][8].y = 45 * exists;machines[BONG][8].orient = 3;
        machines[BONG][9].x = 45 * exists;machines[BONG][9].y = 60 * exists;machines[BONG][9].orient = 3;
        machines[BONG][10].x = 15 * exists;machines[BONG][10].y = 75 * exists;machines[BONG][10].orient = 0;
        machines[BONG][11].x = 15 * exists;machines[BONG][11].y = 105 * exists;machines[BONG][11].orient = 3;
        machines[BONG][12].x = 45 * exists;machines[BONG][12].y = 120 * exists;machines[BONG][12].orient = 0;
        machines[BONG][13].x = 15 * exists;machines[BONG][13].y = 135 * exists;machines[BONG][13].orient = 0;
        machines[BONG][14].x = 15 * exists;machines[BONG][14].y = 165 * exists;machines[BONG][14].orient = 3;
	for (int i = 15; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {	
	    machines[BONG][i].x = 10 * exists;machines[BONG][i].y = 10 * exists;machines[BONG][i].orient = 4;
	}

    }

    public void setMoogConstants() {

    	if (DEBUG) System.out.println("setMoogConstants");

	labs[MOOG].visible = true;
	labs[MOOG].id = MOOG;

	labs[MOOG].x = 795;
	labs[MOOG].y = 15;
	// below values are in local coords, offset by above values

	labs[MOOG].width = 180;
	labs[MOOG].height = 180;

	labs[MOOG].name = "MOOG";
	labs[MOOG].name_x = 50;
	labs[MOOG].name_y = 115;
    
        labs[MOOG].coords[X][0] = 0;
        labs[MOOG].coords[X][1] = 90;
        labs[MOOG].coords[X][2] = 180;
        labs[MOOG].coords[X][3] = 180;
        labs[MOOG].coords[X][4] = 90;
        labs[MOOG].coords[X][5] = 0;

        labs[MOOG].coords[Y][0] = 0;
        labs[MOOG].coords[Y][1] = 0;
        labs[MOOG].coords[Y][2] = 0;
        labs[MOOG].coords[Y][3] = 180;
        labs[MOOG].coords[Y][4] = 180;
        labs[MOOG].coords[Y][5] = 180;

        /*MOOG MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[MOOG][0].x = 15;machines[MOOG][0].y = 135;machines[MOOG][0].orient = 0;
        machines[MOOG][1].x = 15;machines[MOOG][1].y = 165;machines[MOOG][1].orient = 3;
        machines[MOOG][2].x = 75;machines[MOOG][2].y = 165;machines[MOOG][2].orient = 2;
        machines[MOOG][3].x = 75;machines[MOOG][3].y = 135;machines[MOOG][3].orient = 1;
        machines[MOOG][4].x = 105;machines[MOOG][4].y = 135;machines[MOOG][4].orient = 0;
        machines[MOOG][5].x = 105;machines[MOOG][5].y = 165;machines[MOOG][5].orient = 3;
        machines[MOOG][6].x = 165;machines[MOOG][6].y = 165;machines[MOOG][6].orient = 2;
        machines[MOOG][7].x = 165;machines[MOOG][7].y = 135;machines[MOOG][7].orient = 1;
        machines[MOOG][8].x = 165;machines[MOOG][8].y = 75;machines[MOOG][8].orient = 1;
        machines[MOOG][9].x = 165;machines[MOOG][9].y = 45;machines[MOOG][9].orient = 2;
        machines[MOOG][10].x = 165;machines[MOOG][10].y = 15;machines[MOOG][10].orient = 1;
        machines[MOOG][11].x = 105;machines[MOOG][11].y = 15;machines[MOOG][11].orient = 0;
        machines[MOOG][12].x = 105;machines[MOOG][12].y = 45;machines[MOOG][12].orient = 3;
        machines[MOOG][13].x = 105;machines[MOOG][13].y = 75;machines[MOOG][13].orient = 0;
        machines[MOOG][14].x = 75;machines[MOOG][14].y = 75;machines[MOOG][14].orient = 1;
        machines[MOOG][15].x = 75;machines[MOOG][15].y = 45;machines[MOOG][15].orient = 2;
        machines[MOOG][16].x = 75;machines[MOOG][16].y = 15;machines[MOOG][16].orient = 1;
        machines[MOOG][17].x = 15;machines[MOOG][17].y = 15;machines[MOOG][17].orient = 0;
        machines[MOOG][18].x = 15;machines[MOOG][18].y = 45;machines[MOOG][18].orient = 3;
        machines[MOOG][19].x = 15;machines[MOOG][19].y = 75;machines[MOOG][19].orient = 0;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[MOOG][i].x = 10;machines[MOOG][i].y = 10;machines[MOOG][i].orient = 4;
	}

    }

    public void setBellConstants() {

    	if (DEBUG) System.out.println("setBellConstants");

	labs[BELL].visible = true;
	labs[BELL].id = BELL;

	labs[BELL].x = 975;
	labs[BELL].y = 15;
	// below values are in local coords, offset by above values

	labs[BELL].width = 180;
	labs[BELL].height = 180;

	labs[BELL].name = "BELL";
	labs[BELL].name_x = 50;
	labs[BELL].name_y = 115;
        
	labs[BELL].coords[X][0] = 0;
        labs[BELL].coords[X][1] = 90;
        labs[BELL].coords[X][2] = 180;
        labs[BELL].coords[X][3] = 180;
        labs[BELL].coords[X][4] = 90;
        labs[BELL].coords[X][5] = 0;

        labs[BELL].coords[Y][0] = 0;
        labs[BELL].coords[Y][1] = 0;
        labs[BELL].coords[Y][2] = 0;
        labs[BELL].coords[Y][3] = 180;
        labs[BELL].coords[Y][4] = 180;
        labs[BELL].coords[Y][5] = 180;
	
        /*BELL MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[BELL][0].x = 15;machines[BELL][0].y = 135;machines[BELL][0].orient = 0;
        machines[BELL][1].x = 15;machines[BELL][1].y = 165;machines[BELL][1].orient = 3;
        machines[BELL][2].x = 75;machines[BELL][2].y = 135;machines[BELL][2].orient = 0;
        machines[BELL][3].x = 75;machines[BELL][3].y = 165;machines[BELL][3].orient = 3;
        machines[BELL][4].x = 105;machines[BELL][4].y = 165;machines[BELL][4].orient = 2;
        machines[BELL][5].x = 135;machines[BELL][5].y = 165;machines[BELL][5].orient = 3;
        machines[BELL][6].x = 165;machines[BELL][6].y = 165;machines[BELL][6].orient = 2;
        machines[BELL][7].x = 165;machines[BELL][7].y = 135;machines[BELL][7].orient = 1;
        machines[BELL][8].x = 165;machines[BELL][8].y = 105;machines[BELL][8].orient = 2;
        machines[BELL][9].x = 165;machines[BELL][9].y = 75;machines[BELL][9].orient = 1;
        machines[BELL][10].x = 165;machines[BELL][10].y = 45;machines[BELL][10].orient = 2;
        machines[BELL][11].x = 105;machines[BELL][11].y = 15;machines[BELL][11].orient = 0;
        machines[BELL][12].x = 105;machines[BELL][12].y = 45;machines[BELL][12].orient = 3;
        machines[BELL][13].x = 105;machines[BELL][13].y = 75;machines[BELL][13].orient = 0;
        machines[BELL][14].x = 75;machines[BELL][14].y = 75;machines[BELL][14].orient = 1;
        machines[BELL][15].x = 75;machines[BELL][15].y = 45;machines[BELL][15].orient = 2;
        machines[BELL][16].x = 75;machines[BELL][16].y = 15;machines[BELL][16].orient = 1;
        machines[BELL][17].x = 15;machines[BELL][17].y = 15;machines[BELL][17].orient = 0;
        machines[BELL][18].x = 15;machines[BELL][18].y = 45;machines[BELL][18].orient = 3;
        machines[BELL][19].x = 15;machines[BELL][19].y = 75;machines[BELL][19].orient = 0;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[BELL][i].x = 10;machines[BELL][i].y = 10;machines[BELL][i].orient = 4;
	}
    }

    public void setLeafConstants() {

    	if (DEBUG) System.out.println("setLeafConstants");

	labs[LEAF].visible = true;
	labs[LEAF].id = LEAF;

	labs[LEAF].x = 795;
	labs[LEAF].y = 195;
	// below values are in local coords, offset by above values

	labs[LEAF].width = 180;
	labs[LEAF].height = 180;

	labs[LEAF].name = "LEAF";
	labs[LEAF].name_x = 50;
	labs[LEAF].name_y = 85;
    
	labs[LEAF].coords[X][0] = 0;
        labs[LEAF].coords[X][1] = 90;
        labs[LEAF].coords[X][2] = 180;
        labs[LEAF].coords[X][3] = 180;
        labs[LEAF].coords[X][4] = 90;
        labs[LEAF].coords[X][5] = 0;

        labs[LEAF].coords[Y][0] = 0;
        labs[LEAF].coords[Y][1] = 0;
        labs[LEAF].coords[Y][2] = 0;
        labs[LEAF].coords[Y][3] = 180;
        labs[LEAF].coords[Y][4] = 180;
        labs[LEAF].coords[Y][5] = 180;

        /*LEAF MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[LEAF][0].x = 15;machines[LEAF][0].y = 135;machines[LEAF][0].orient = 0;
        machines[LEAF][1].x = 15;machines[LEAF][1].y = 165;machines[LEAF][1].orient = 3;
        machines[LEAF][2].x = 75;machines[LEAF][2].y = 165;machines[LEAF][2].orient = 2;
        machines[LEAF][3].x = 75;machines[LEAF][3].y = 135;machines[LEAF][3].orient = 1;
        machines[LEAF][4].x = 75;machines[LEAF][4].y = 105;machines[LEAF][4].orient = 2;
        machines[LEAF][5].x = 105;machines[LEAF][5].y = 105;machines[LEAF][5].orient = 3;
        machines[LEAF][6].x = 105;machines[LEAF][6].y = 135;machines[LEAF][6].orient = 0;
        machines[LEAF][7].x = 105;machines[LEAF][7].y = 165;machines[LEAF][7].orient = 3;
        machines[LEAF][8].x = 165;machines[LEAF][8].y = 165;machines[LEAF][8].orient = 2;
        machines[LEAF][9].x = 165;machines[LEAF][9].y = 135;machines[LEAF][9].orient = 1;
        machines[LEAF][10].x = 165;machines[LEAF][10].y = 105;machines[LEAF][10].orient = 2;
        machines[LEAF][11].x = 165;machines[LEAF][11].y = 45;machines[LEAF][11].orient = 2;
        machines[LEAF][12].x = 165;machines[LEAF][12].y = 15;machines[LEAF][12].orient = 1;
        machines[LEAF][13].x = 105;machines[LEAF][13].y = 15;machines[LEAF][13].orient = 0;
        machines[LEAF][14].x = 105;machines[LEAF][14].y = 45;machines[LEAF][14].orient = 3;
        machines[LEAF][15].x = 75;machines[LEAF][15].y = 45;machines[LEAF][15].orient = 2;
        machines[LEAF][16].x = 75;machines[LEAF][16].y = 15;machines[LEAF][16].orient = 1;
        machines[LEAF][17].x = 15;machines[LEAF][17].y = 15;machines[LEAF][17].orient = 0;
        machines[LEAF][18].x = 15;machines[LEAF][18].y = 45;machines[LEAF][18].orient = 3;
	for (int i = 19; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[LEAF][i].x = 10;machines[LEAF][i].y = 10;machines[LEAF][i].orient = 4;
	}

    }

    public void setSpooConstants() {

    	if (DEBUG) System.out.println("setSpooConstants");

	labs[SPOO].visible = true;
	labs[SPOO].id = SPOO;

	labs[SPOO].x = 975;
	labs[SPOO].y = 195;
	// below values are in local coords, offset by above values

	labs[SPOO].width = 180;
	labs[SPOO].height = 180;

	labs[SPOO].name = "SPOONS";
	labs[SPOO].name_x = 80;
	labs[SPOO].name_y = 145;

	labs[SPOO].coords[X][0] = 0;
        labs[SPOO].coords[X][1] = 90;
        labs[SPOO].coords[X][2] = 180;
        labs[SPOO].coords[X][3] = 180;
        labs[SPOO].coords[X][4] = 90;
        labs[SPOO].coords[X][5] = 0;

        labs[SPOO].coords[Y][0] = 0;
        labs[SPOO].coords[Y][1] = 0;
        labs[SPOO].coords[Y][2] = 0;
        labs[SPOO].coords[Y][3] = 180;
        labs[SPOO].coords[Y][4] = 180;
        labs[SPOO].coords[Y][5] = 180;
    
        /*SPOO MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[SPOO][0].x = 15;machines[SPOO][0].y = 105;machines[SPOO][0].orient = 3;
        machines[SPOO][1].x = 15;machines[SPOO][1].y = 135;machines[SPOO][1].orient = 0;
        machines[SPOO][2].x = 15;machines[SPOO][2].y = 165;machines[SPOO][2].orient = 3;
        machines[SPOO][3].x = 45;machines[SPOO][3].y = 165;machines[SPOO][3].orient = 2;
        machines[SPOO][4].x = 75;machines[SPOO][4].y = 165;machines[SPOO][4].orient = 3;
        machines[SPOO][5].x = 135;machines[SPOO][5].y = 165;machines[SPOO][5].orient = 3;
        machines[SPOO][6].x = 165;machines[SPOO][6].y = 165;machines[SPOO][6].orient = 2;
        machines[SPOO][7].x = 165;machines[SPOO][7].y = 105;machines[SPOO][7].orient = 1;
        machines[SPOO][8].x = 135;machines[SPOO][8].y = 105;machines[SPOO][8].orient = 0;
        machines[SPOO][9].x = 105;machines[SPOO][9].y = 105;machines[SPOO][9].orient = 1;
        machines[SPOO][10].x = 105;machines[SPOO][10].y = 75;machines[SPOO][10].orient = 2;
        machines[SPOO][11].x = 135;machines[SPOO][11].y = 75;machines[SPOO][11].orient = 3;
        machines[SPOO][12].x = 165;machines[SPOO][12].y = 75;machines[SPOO][12].orient = 2;
        machines[SPOO][13].x = 165;machines[SPOO][13].y = 15;machines[SPOO][13].orient = 1;
        machines[SPOO][14].x = 135;machines[SPOO][14].y = 15;machines[SPOO][14].orient = 0;
        machines[SPOO][15].x = 105;machines[SPOO][15].y = 15;machines[SPOO][15].orient = 1;
        machines[SPOO][16].x = 75;machines[SPOO][16].y = 15;machines[SPOO][16].orient = 0;
        machines[SPOO][17].x = 45;machines[SPOO][17].y = 15;machines[SPOO][17].orient = 1;
        machines[SPOO][18].x = 15;machines[SPOO][18].y = 15;machines[SPOO][18].orient = 0;
        machines[SPOO][19].x = 15;machines[SPOO][19].y = 45;machines[SPOO][19].orient = 3;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[SPOO][i].x = 10;machines[SPOO][i].y = 10;machines[SPOO][i].orient = 4;
	}

    }

    public void setBuglConstants() {

    	if (DEBUG) System.out.println("setBuglConstants");

	labs[BUGL].visible = true;
	labs[BUGL].id = BUGL;

	labs[BUGL].x = 795;
	labs[BUGL].y = 405;
	// below values are in local coords, offset by above values

	labs[BUGL].width = 210;
	labs[BUGL].height = 180;

	labs[BUGL].name = "BUGLE";
	labs[BUGL].name_x = 85;
	labs[BUGL].name_y = 145;

	labs[BUGL].coords[X][0] = 0;
	labs[BUGL].coords[X][1] = 210;
	labs[BUGL].coords[X][2] = 210;
	labs[BUGL].coords[X][3] = 30;
	labs[BUGL].coords[X][4] = 30;
	labs[BUGL].coords[X][5] = 0;

	labs[BUGL].coords[Y][0] = 0;
	labs[BUGL].coords[Y][1] = 0;
	labs[BUGL].coords[Y][2] = 180;
	labs[BUGL].coords[Y][3] = 180;
	labs[BUGL].coords[Y][4] = 90;
	labs[BUGL].coords[Y][5] = 90;
    
        /*BUGL MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[BUGL][0].x = 75;machines[BUGL][0].y = 165;machines[BUGL][0].orient = 3;
        machines[BUGL][1].x = 105;machines[BUGL][1].y = 165;machines[BUGL][1].orient = 2;
        machines[BUGL][2].x = 135;machines[BUGL][2].y = 165;machines[BUGL][2].orient = 3;
        machines[BUGL][3].x = 195;machines[BUGL][3].y = 165;machines[BUGL][3].orient = 2;
        machines[BUGL][4].x = 195;machines[BUGL][4].y = 135;machines[BUGL][4].orient = 1;
        machines[BUGL][5].x = 195;machines[BUGL][5].y = 105;machines[BUGL][5].orient = 2;
        machines[BUGL][6].x = 195;machines[BUGL][6].y = 75;machines[BUGL][6].orient = 1;
        machines[BUGL][7].x = 195;machines[BUGL][7].y = 45;machines[BUGL][7].orient = 2;
        machines[BUGL][8].x = 195;machines[BUGL][8].y = 15;machines[BUGL][8].orient = 1;
        machines[BUGL][9].x = 135;machines[BUGL][9].y = 15;machines[BUGL][9].orient = 0;
        machines[BUGL][10].x = 135;machines[BUGL][10].y = 45;machines[BUGL][10].orient = 3;
        machines[BUGL][11].x = 135;machines[BUGL][11].y = 75;machines[BUGL][11].orient = 0;
        machines[BUGL][12].x = 135;machines[BUGL][12].y = 105;machines[BUGL][12].orient = 3;
        machines[BUGL][13].x = 105;machines[BUGL][13].y = 105;machines[BUGL][13].orient = 2;
        machines[BUGL][14].x = 105;machines[BUGL][14].y = 75;machines[BUGL][14].orient = 1;
        machines[BUGL][15].x = 105;machines[BUGL][15].y = 45;machines[BUGL][15].orient = 2;
        machines[BUGL][16].x = 105;machines[BUGL][16].y = 15;machines[BUGL][16].orient = 1;
        machines[BUGL][17].x = 45;machines[BUGL][17].y = 15;machines[BUGL][17].orient = 1;
        machines[BUGL][18].x = 15;machines[BUGL][18].y = 15;machines[BUGL][18].orient = 0;
        machines[BUGL][19].x = 15;machines[BUGL][19].y = 45;machines[BUGL][19].orient = 3;	
        machines[BUGL][20].x = 15;machines[BUGL][20].y = 75;machines[BUGL][20].orient = 3;
	for (int i = 21; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[BUGL][i].x = 10;machines[BUGL][i].y = 10;machines[BUGL][i].orient = 4;
	}

    }

    public void setPipeConstants() {

    	if (DEBUG) System.out.println("setPipeConstants");

	labs[PIPE].visible = true;
	labs[PIPE].id = PIPE;

	labs[PIPE].x = 795;
	labs[PIPE].y = 585;
	// below values are in local coords, offset by abov value

	labs[PIPE].width = 210;
	labs[PIPE].height = 180;
	
	labs[PIPE].name = "PIPE";
	labs[PIPE].name_x = 85;
	labs[PIPE].name_y = 145;

	labs[PIPE].coords[X][0] = 0;
	labs[PIPE].coords[X][1] = 210;
	labs[PIPE].coords[X][2] = 210;
	labs[PIPE].coords[X][3] = 30;
	labs[PIPE].coords[X][4] = 30;
	labs[PIPE].coords[X][5] = 0;

	labs[PIPE].coords[Y][0] = 0;
	labs[PIPE].coords[Y][1] = 0;
	labs[PIPE].coords[Y][2] = 180;
	labs[PIPE].coords[Y][3] = 180;
	labs[PIPE].coords[Y][4] = 120;
	labs[PIPE].coords[Y][5] = 120;
    
        /*PIPE MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[PIPE][0].x = 15;machines[PIPE][0].y = 75;machines[PIPE][0].orient = 0;
        machines[PIPE][1].x = 15;machines[PIPE][1].y = 105;machines[PIPE][1].orient = 3;
        machines[PIPE][2].x = 45;machines[PIPE][2].y = 135;machines[PIPE][2].orient = 0;
        machines[PIPE][3].x = 45;machines[PIPE][3].y = 165;machines[PIPE][3].orient = 3;
        machines[PIPE][4].x = 75;machines[PIPE][4].y = 165;machines[PIPE][4].orient = 2;
        machines[PIPE][5].x = 135;machines[PIPE][5].y = 165;machines[PIPE][5].orient = 2;
        machines[PIPE][6].x = 165;machines[PIPE][6].y = 165;machines[PIPE][6].orient = 3;
        machines[PIPE][7].x = 195;machines[PIPE][7].y = 165;machines[PIPE][7].orient = 2;
        machines[PIPE][8].x = 195;machines[PIPE][8].y = 135;machines[PIPE][8].orient = 1;
        machines[PIPE][9].x = 195;machines[PIPE][9].y = 105;machines[PIPE][9].orient = 2;
        machines[PIPE][10].x = 195;machines[PIPE][10].y = 75;machines[PIPE][10].orient = 1;
        machines[PIPE][11].x = 195;machines[PIPE][11].y = 45;machines[PIPE][11].orient = 2;
        machines[PIPE][12].x = 195;machines[PIPE][12].y = 15;machines[PIPE][12].orient = 1;
        machines[PIPE][13].x = 165;machines[PIPE][13].y = 15;machines[PIPE][13].orient = 1;
        machines[PIPE][14].x = 135;machines[PIPE][14].y = 15;machines[PIPE][14].orient = 0;
        machines[PIPE][15].x = 135;machines[PIPE][15].y = 45;machines[PIPE][15].orient = 3;
        machines[PIPE][16].x = 135;machines[PIPE][16].y = 75;machines[PIPE][16].orient = 0;
        machines[PIPE][17].x = 135;machines[PIPE][17].y = 105;machines[PIPE][17].orient = 3;
        machines[PIPE][18].x = 105;machines[PIPE][18].y = 75;machines[PIPE][18].orient = 1;
        machines[PIPE][19].x = 105;machines[PIPE][19].y = 45;machines[PIPE][19].orient = 2;
        machines[PIPE][20].x = 105;machines[PIPE][20].y = 15;machines[PIPE][20].orient = 1;
	for (int i = 21; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[PIPE][i].x = 10;machines[PIPE][i].y = 10;machines[PIPE][i].orient = 4;
	}
    }

    public void setBanjConstants() {

    	if (DEBUG) System.out.println("setBanjConstants");

	labs[BANJ].visible = true;
	labs[BANJ].id = BANJ;

	labs[BANJ].x = 585;
	labs[BANJ].y = 705;
	// below values are in local coords, offset by above values

	labs[BANJ].width = 180;
	labs[BANJ].height = 180;
	
	labs[BANJ].name = "BANJO";
	labs[BANJ].name_x = 90;
	labs[BANJ].name_y = 15;
	
	labs[BANJ].coords[X][0] = 0;
	labs[BANJ].coords[X][1] = 90;
	labs[BANJ].coords[X][2] = 180;
	labs[BANJ].coords[X][3] = 180;
	labs[BANJ].coords[X][4] = 90;
	labs[BANJ].coords[X][5] = 0;	
	
	labs[BANJ].coords[Y][0] = 0;
	labs[BANJ].coords[Y][1] = 0;
	labs[BANJ].coords[Y][2] = 0;
	labs[BANJ].coords[Y][3] = 180;
	labs[BANJ].coords[Y][4] = 180;
	labs[BANJ].coords[Y][5] = 180;
		
        /*BANJ MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[BANJ][0].x = 145;machines[BANJ][0].y = 40;machines[BANJ][0].orient = 0;
        machines[BANJ][1].x = 145;machines[BANJ][1].y = 70;machines[BANJ][1].orient = 0;
        machines[BANJ][2].x = 145;machines[BANJ][2].y = 100;machines[BANJ][2].orient = 0;
        machines[BANJ][3].x = 145;machines[BANJ][3].y = 130;machines[BANJ][3].orient = 0;
        machines[BANJ][4].x = 130;machines[BANJ][4].y = 160;machines[BANJ][4].orient = 0;
        machines[BANJ][5].x = 100;machines[BANJ][5].y = 160;machines[BANJ][5].orient = 0;
        machines[BANJ][6].x =  70;machines[BANJ][6].y = 160;machines[BANJ][6].orient = 1;
        machines[BANJ][7].x =  40;machines[BANJ][7].y = 160;machines[BANJ][7].orient = 1;
        machines[BANJ][8].x =  25;machines[BANJ][8].y = 130;machines[BANJ][8].orient = 1;
        machines[BANJ][9].x =  25;machines[BANJ][9].y = 100;machines[BANJ][9].orient = 1;
        machines[BANJ][10].x = 25;machines[BANJ][10].y = 70;machines[BANJ][10].orient = 1;
        machines[BANJ][11].x = 70;machines[BANJ][11].y = 40;machines[BANJ][11].orient = 1;
        machines[BANJ][12].x = 70;machines[BANJ][12].y = 70;machines[BANJ][12].orient = 1;
        machines[BANJ][13].x = 70;machines[BANJ][13].y = 100;machines[BANJ][13].orient = 1;
        machines[BANJ][14].x =100;machines[BANJ][14].y = 100;machines[BANJ][14].orient = 0;
        machines[BANJ][15].x =100;machines[BANJ][15].y =  70;machines[BANJ][15].orient = 0;
        machines[BANJ][16].x =100;machines[BANJ][16].y =  40;machines[BANJ][16].orient = 0;
	for (int i = 17; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[BANJ][i].x = 10;machines[BANJ][i].y = 100;machines[BANJ][i].orient = 4;
	}

    }

    public void setOudxConstants() {

    	if (DEBUG) System.out.println("setOudxConstants");

	labs[OUDX].visible = true;
	labs[OUDX].id = OUDX;

	labs[OUDX].x = 405;
	labs[OUDX].y = 705;
	// below values are in local coords, offset by above values

	labs[OUDX].width = 180;
	labs[OUDX].height = 180;

	labs[OUDX].name = "OUD";
	labs[OUDX].name_x = 100;
	labs[OUDX].name_y = 15;
	
	labs[OUDX].coords[X][0] = 0;
	labs[OUDX].coords[X][1] = 90;
	labs[OUDX].coords[X][2] = 180;
	labs[OUDX].coords[X][3] = 180;
	labs[OUDX].coords[X][4] = 90;
	labs[OUDX].coords[X][5] = 0;
	
	labs[OUDX].coords[Y][0] = 0;
	labs[OUDX].coords[Y][1] = 0;
	labs[OUDX].coords[Y][2] = 0;
	labs[OUDX].coords[Y][3] = 180;
	labs[OUDX].coords[Y][4] = 180;
	labs[OUDX].coords[Y][5] = 180;
    
        /*OUDX MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[OUDX][0].x = 165;machines[OUDX][0].y = 45;machines[OUDX][0].orient = 2;
        machines[OUDX][1].x = 165;machines[OUDX][1].y = 75;machines[OUDX][1].orient = 1;
        machines[OUDX][2].x = 165;machines[OUDX][2].y = 105;machines[OUDX][2].orient = 2;
        machines[OUDX][3].x = 165;machines[OUDX][3].y = 135;machines[OUDX][3].orient = 1;
        machines[OUDX][4].x = 165;machines[OUDX][4].y = 165;machines[OUDX][4].orient = 2;
        machines[OUDX][5].x = 105;machines[OUDX][5].y = 165;machines[OUDX][5].orient = 3;
        machines[OUDX][6].x = 105;machines[OUDX][6].y = 135;machines[OUDX][6].orient = 0;
        machines[OUDX][7].x = 105;machines[OUDX][7].y = 105;machines[OUDX][7].orient = 3;
        machines[OUDX][8].x = 105;machines[OUDX][8].y = 75;machines[OUDX][8].orient = 0;
        machines[OUDX][9].x = 105;machines[OUDX][9].y = 45;machines[OUDX][9].orient = 3;
        machines[OUDX][10].x = 75;machines[OUDX][10].y = 45;machines[OUDX][10].orient = 2;
        machines[OUDX][11].x = 75;machines[OUDX][11].y = 75;machines[OUDX][11].orient = 1;
        machines[OUDX][12].x = 75;machines[OUDX][12].y = 105;machines[OUDX][12].orient = 2;
        machines[OUDX][13].x = 75;machines[OUDX][13].y = 135;machines[OUDX][13].orient = 1;
        machines[OUDX][14].x = 75;machines[OUDX][14].y = 165;machines[OUDX][14].orient = 2;
        machines[OUDX][15].x = 15;machines[OUDX][15].y = 165;machines[OUDX][15].orient = 3;
        machines[OUDX][16].x = 15;machines[OUDX][16].y = 135;machines[OUDX][16].orient = 0;
        machines[OUDX][17].x = 15;machines[OUDX][17].y = 105;machines[OUDX][17].orient = 3;
        machines[OUDX][18].x = 15;machines[OUDX][18].y = 75;machines[OUDX][18].orient = 0;
	for (int i = 19; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[OUDX][i].x = 10;machines[OUDX][i].y = 10;machines[OUDX][i].orient = 4;
	}

    }

    public void setClavConstants() {

    	if (DEBUG) System.out.println("setClavConstants");

	labs[CLAV].visible = true;
	labs[CLAV].id = CLAV;

	labs[CLAV].x = 105;
	labs[CLAV].y = 585;
	// below values are in local coords, offset by above values

	labs[CLAV].width = 300;
	labs[CLAV].height = 90;

	labs[CLAV].name = "CLAVIER";
	labs[CLAV].name_x = 15;
	labs[CLAV].name_y = 55;

	labs[CLAV].coords[X][0] = 0;
        labs[CLAV].coords[X][1] = 150;
        labs[CLAV].coords[X][2] = 300;
        labs[CLAV].coords[X][3] = 300;
        labs[CLAV].coords[X][4] = 150;
        labs[CLAV].coords[X][5] = 0;

        labs[CLAV].coords[Y][0] = 0;
        labs[CLAV].coords[Y][1] = 0;
        labs[CLAV].coords[Y][2] = 0;
        labs[CLAV].coords[Y][3] = 90;
        labs[CLAV].coords[Y][4] = 90;
        labs[CLAV].coords[Y][5] = 90;
    
        /*CLAV MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[CLAV][19].x = 15;machines[CLAV][19].y = 75;machines[CLAV][19].orient = 3;
        machines[CLAV][18].x = 45;machines[CLAV][18].y = 75;machines[CLAV][18].orient = 2;
        machines[CLAV][17].x = 75;machines[CLAV][17].y = 75;machines[CLAV][17].orient = 3;
        machines[CLAV][16].x = 105;machines[CLAV][16].y = 75;machines[CLAV][16].orient = 2;
        machines[CLAV][15].x = 135;machines[CLAV][15].y = 75;machines[CLAV][15].orient = 3;
        machines[CLAV][14].x = 165;machines[CLAV][14].y = 75;machines[CLAV][14].orient = 2;
        machines[CLAV][13].x = 195;machines[CLAV][13].y = 75;machines[CLAV][13].orient = 3;
        machines[CLAV][12].x = 225;machines[CLAV][12].y = 75;machines[CLAV][12].orient = 2;
        machines[CLAV][11].x = 255;machines[CLAV][11].y = 75;machines[CLAV][11].orient = 3;
        machines[CLAV][10].x = 285;machines[CLAV][10].y = 75;machines[CLAV][10].orient = 2;
        machines[CLAV][9].x = 285;machines[CLAV][9].y = 15;machines[CLAV][9].orient = 1;
        machines[CLAV][8].x = 255;machines[CLAV][8].y = 15;machines[CLAV][8].orient = 0;
        machines[CLAV][7].x = 225;machines[CLAV][7].y = 15;machines[CLAV][7].orient = 1;
        machines[CLAV][6].x = 195;machines[CLAV][6].y = 15;machines[CLAV][6].orient = 0;
        machines[CLAV][5].x = 165;machines[CLAV][5].y = 15;machines[CLAV][5].orient = 1;
        machines[CLAV][4].x = 135;machines[CLAV][4].y = 15;machines[CLAV][4].orient = 0;
        machines[CLAV][3].x = 105;machines[CLAV][3].y = 15;machines[CLAV][3].orient = 1;
        machines[CLAV][2].x = 75;machines[CLAV][2].y = 15;machines[CLAV][2].orient = 0;
        machines[CLAV][1].x = 45;machines[CLAV][1].y = 15;machines[CLAV][1].orient = 1;
        machines[CLAV][0].x = 15;machines[CLAV][0].y = 15;machines[CLAV][0].orient = 0;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[CLAV][i].x = 10;machines[CLAV][i].y = 10;machines[CLAV][i].orient = 4;
	}

    }

    public void setPianConstants() {

    	if (DEBUG) System.out.println("setPianConstants");

	labs[PIAN].visible = true;
	labs[PIAN].id = PIAN;

	labs[PIAN].x = 15;
	labs[PIAN].y = 405;
	// below values are in local coords, offset by above values

	labs[PIAN].width = 360;
	labs[PIAN].height = 90;
	
	labs[PIAN].name = "PIANO";
	labs[PIAN].name_x = 18;
	labs[PIAN].name_y = 50;

	labs[PIAN].coords[X][0] = 0;
        labs[PIAN].coords[X][1] = 180;
        labs[PIAN].coords[X][2] = 360;
        labs[PIAN].coords[X][3] = 360;
        labs[PIAN].coords[X][4] = 180;
        labs[PIAN].coords[X][5] = 0;

        labs[PIAN].coords[Y][0] = 0;
        labs[PIAN].coords[Y][1] = 0;
        labs[PIAN].coords[Y][2] = 0;
        labs[PIAN].coords[Y][3] = 90;
        labs[PIAN].coords[Y][4] = 90;
        labs[PIAN].coords[Y][5] = 90;
    
        /*PIAN MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[PIAN][17].x = 105;machines[PIAN][17].y = 75;machines[PIAN][17].orient = 2;
        machines[PIAN][16].x = 135;machines[PIAN][16].y = 75;machines[PIAN][16].orient = 3;
        machines[PIAN][15].x = 165;machines[PIAN][15].y = 75;machines[PIAN][15].orient = 2;
        machines[PIAN][14].x = 195;machines[PIAN][14].y = 75;machines[PIAN][14].orient = 3;
        machines[PIAN][13].x = 225;machines[PIAN][13].y = 75;machines[PIAN][13].orient = 2;
        machines[PIAN][12].x = 255;machines[PIAN][12].y = 75;machines[PIAN][12].orient = 3;
        machines[PIAN][11].x = 285;machines[PIAN][11].y = 75;machines[PIAN][11].orient = 2;
        machines[PIAN][10].x = 315;machines[PIAN][10].y = 75;machines[PIAN][10].orient = 3;
        machines[PIAN][9].x = 345;machines[PIAN][9].y = 75;machines[PIAN][9].orient = 2;
        machines[PIAN][8].x = 345;machines[PIAN][8].y = 15;machines[PIAN][8].orient = 1;
        machines[PIAN][7].x = 315;machines[PIAN][7].y = 15;machines[PIAN][7].orient = 0;
        machines[PIAN][6].x = 285;machines[PIAN][6].y = 15;machines[PIAN][6].orient = 1;
        machines[PIAN][5].x = 255;machines[PIAN][5].y = 15;machines[PIAN][5].orient = 0;
        machines[PIAN][4].x = 225;machines[PIAN][4].y = 15;machines[PIAN][4].orient = 1;
        machines[PIAN][3].x = 195;machines[PIAN][3].y = 15;machines[PIAN][3].orient = 0;
        machines[PIAN][2].x = 165;machines[PIAN][2].y = 15;machines[PIAN][2].orient = 1;
        machines[PIAN][1].x = 135;machines[PIAN][1].y = 15;machines[PIAN][1].orient = 0;
        machines[PIAN][0].x = 105;machines[PIAN][0].y = 15;machines[PIAN][0].orient = 1;
	for (int i = 18; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[PIAN][i].x = 10;machines[PIAN][i].y = 10;machines[PIAN][i].orient = 4;
	}

    }

    public void setOrgaConstants() {

    	if (DEBUG) System.out.println("setOrgaConstants");

	labs[ORGA].visible = true;
	labs[ORGA].id = ORGA;

	labs[ORGA].x = 105;
	labs[ORGA].y = 495;
	// below values are in local coords, offset by above values

	labs[ORGA].width = 300;
	labs[ORGA].height = 90;

	labs[ORGA].name = "ORGAN";
	labs[ORGA].name_x = 15;
	labs[ORGA].name_y = 55;

	labs[ORGA].coords[X][0] = 0;
        labs[ORGA].coords[X][1] = 135;
        labs[ORGA].coords[X][2] = 300;
        labs[ORGA].coords[X][3] = 300;
        labs[ORGA].coords[X][4] = 135;
        labs[ORGA].coords[X][5] = 0;

        labs[ORGA].coords[Y][0] = 0;
        labs[ORGA].coords[Y][1] = 0;
        labs[ORGA].coords[Y][2] = 0;
        labs[ORGA].coords[Y][3] = 90;
        labs[ORGA].coords[Y][4] = 90;
        labs[ORGA].coords[Y][5] = 90;
    
        /*ORGA MACHINES POSITION CONSTANTS DEFINITIONS*/
        machines[ORGA][19].x = 15;machines[ORGA][19].y = 75;machines[ORGA][19].orient = 3;
        machines[ORGA][18].x = 45;machines[ORGA][18].y = 75;machines[ORGA][18].orient = 2;
        machines[ORGA][17].x = 75;machines[ORGA][17].y = 75;machines[ORGA][17].orient = 3;
        machines[ORGA][16].x = 105;machines[ORGA][16].y = 75;machines[ORGA][16].orient = 2;
        machines[ORGA][15].x = 135;machines[ORGA][15].y = 75;machines[ORGA][15].orient = 3;
        machines[ORGA][14].x = 165;machines[ORGA][14].y = 75;machines[ORGA][14].orient = 2;
        machines[ORGA][13].x = 195;machines[ORGA][13].y = 75;machines[ORGA][13].orient = 3;
        machines[ORGA][12].x = 225;machines[ORGA][12].y = 75;machines[ORGA][12].orient = 2;
        machines[ORGA][11].x = 255;machines[ORGA][11].y = 75;machines[ORGA][11].orient = 3;
        machines[ORGA][10].x = 285;machines[ORGA][10].y = 75;machines[ORGA][10].orient = 2;
        machines[ORGA][9].x = 285;machines[ORGA][9].y = 15;machines[ORGA][9].orient = 1;
        machines[ORGA][8].x = 255;machines[ORGA][8].y = 15;machines[ORGA][8].orient = 0;
        machines[ORGA][7].x = 225;machines[ORGA][7].y = 15;machines[ORGA][7].orient = 1;
        machines[ORGA][6].x = 195;machines[ORGA][6].y = 15;machines[ORGA][6].orient = 0;
        machines[ORGA][5].x = 165;machines[ORGA][5].y = 15;machines[ORGA][5].orient = 1;
        machines[ORGA][4].x = 135;machines[ORGA][4].y = 15;machines[ORGA][4].orient = 0;
        machines[ORGA][3].x = 105;machines[ORGA][3].y = 15;machines[ORGA][3].orient = 1;
        machines[ORGA][2].x = 75;machines[ORGA][2].y = 15;machines[ORGA][2].orient = 0;
        machines[ORGA][1].x = 45;machines[ORGA][1].y = 15;machines[ORGA][1].orient = 1;
        machines[ORGA][0].x = 15;machines[ORGA][0].y = 15;machines[ORGA][0].orient = 0;
	for (int i = 20; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[ORGA][i].x = 10;machines[ORGA][i].y = 10;machines[ORGA][i].orient = 4;
	}

    }

    public void setSanhConstants() {
	if (DEBUG) System.out.println("setSanhConstants");
	
	labs[SANH].visible = true;
	labs[SANH].id = SANH;
	
	labs[SANH].x = 400;
	labs[SANH].y = 15;
	
	labs[SANH].width = 270;
	labs[SANH].height = 210;
	
	labs[SANH].name = "SANHU";
	labs[SANH].name_x = 65;
	labs[SANH].name_y = 170;
	
	labs[SANH].coords[X][0] = 0;
	labs[SANH].coords[X][1] = 120;
	labs[SANH].coords[X][2] = 270;
	labs[SANH].coords[X][3] = 270;
	labs[SANH].coords[X][4] = 120;
	labs[SANH].coords[X][5] = 0;
	
	labs[SANH].coords[Y][0] = 0;
	labs[SANH].coords[Y][1] = 0;
	labs[SANH].coords[Y][2] = 0;
	labs[SANH].coords[Y][3] = 210;
	labs[SANH].coords[Y][4] = 210;
	labs[SANH].coords[Y][5] = 210;

	/*SANHU MACHINES POSITON CONSTANTS DEFINITIONS*/
	machines[SANH][0].x = 15; machines[SANH][0].y = 75; machines[SANH][0].orient = 0;
	machines[SANH][1].x = 15; machines[SANH][1].y = 45; machines[SANH][1].orient = 3;
	machines[SANH][2].x = 15; machines[SANH][2].y = 15; machines[SANH][2].orient = 0;
	machines[SANH][3].x = 75; machines[SANH][3].y = 15; machines[SANH][3].orient = 1;
	machines[SANH][4].x = 75; machines[SANH][4].y = 45; machines[SANH][4].orient = 2;
	machines[SANH][5].x = 75; machines[SANH][5].y = 75; machines[SANH][5].orient = 1;
	machines[SANH][6].x = 75; machines[SANH][6].y = 105; machines[SANH][6].orient = 2;
	machines[SANH][7].x = 75; machines[SANH][7].y = 135; machines[SANH][7].orient = 1;
	machines[SANH][8].x = 105; machines[SANH][8].y = 135; machines[SANH][8].orient = 0;
	machines[SANH][9].x = 105; machines[SANH][9].y = 105; machines[SANH][9].orient = 3;
	machines[SANH][10].x = 105; machines[SANH][10].y = 75; machines[SANH][10].orient = 0;
	machines[SANH][11].x = 105; machines[SANH][11].y = 45; machines[SANH][11].orient = 3;
	machines[SANH][12].x = 105; machines[SANH][12].y = 15; machines[SANH][12].orient = 0;
	machines[SANH][13].x = 165; machines[SANH][13].y = 15; machines[SANH][13].orient = 1;
	machines[SANH][14].x = 165; machines[SANH][14].y = 45; machines[SANH][14].orient = 2;
	machines[SANH][15].x = 165; machines[SANH][15].y = 75; machines[SANH][15].orient = 1;
	machines[SANH][16].x = 165; machines[SANH][16].y = 105; machines[SANH][16].orient = 2;
	machines[SANH][17].x = 165; machines[SANH][17].y = 135; machines[SANH][17].orient = 1;
	machines[SANH][18].x = 195; machines[SANH][18].y = 135; machines[SANH][18].orient = 0;
	machines[SANH][19].x = 195; machines[SANH][19].y = 105; machines[SANH][19].orient = 3;
	machines[SANH][20].x = 195; machines[SANH][20].y = 75; machines[SANH][20].orient = 0;
	machines[SANH][21].x = 195; machines[SANH][21].y = 45; machines[SANH][21].orient = 3;
	machines[SANH][22].x = 195; machines[SANH][22].y = 15; machines[SANH][22].orient = 0;
	machines[SANH][23].x = 255; machines[SANH][23].y = 15; machines[SANH][23].orient = 1;
	machines[SANH][24].x = 255; machines[SANH][24].y = 45; machines[SANH][24].orient = 2;
	machines[SANH][25].x = 255; machines[SANH][25].y = 75; machines[SANH][25].orient = 1;
	machines[SANH][26].x = 255; machines[SANH][26].y = 105; machines[SANH][26].orient = 2;
	machines[SANH][27].x = 255; machines[SANH][27].y = 135; machines[SANH][27].orient = 1;
	machines[SANH][28].x = 255; machines[SANH][28].y = 175; machines[SANH][28].orient = 2;
	
	for (int i = 29; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[SANH][i].x = 0; machines[SANH][i].y = 0; machines[SANH][i].orient = 4;
	}
	
    }

    public void	setErhuConstants() {
	if (DEBUG) System.out.println("setErhuConstants");
	
	labs[ERHU].visible = true;
	labs[ERHU].id = ERHU;
	
	labs[ERHU].x = 250;
	labs[ERHU].y = 15;
	
	labs[ERHU].width = 150;
	labs[ERHU].height = 150;
	
	labs[ERHU].name = "ERHU";
	labs[ERHU].name_x = 30;
	labs[ERHU].name_y = 110;
	
	labs[ERHU].coords[X][0] = 0;
	labs[ERHU].coords[X][1] = 60;
	labs[ERHU].coords[X][2] = 150;
	labs[ERHU].coords[X][3] = 150;
	labs[ERHU].coords[X][4] = 60;
	labs[ERHU].coords[X][5] = 0;

	labs[ERHU].coords[Y][0] = 0;
	labs[ERHU].coords[Y][1] = 0;
	labs[ERHU].coords[Y][2] = 0;
	labs[ERHU].coords[Y][3] = 150;
	labs[ERHU].coords[Y][4] = 150;
	labs[ERHU].coords[Y][5] = 150;

	machines[ERHU][0].x = 105; machines[ERHU][0].y = 135; machines[ERHU][0].orient = 2;
	machines[ERHU][1].x = 75; machines[ERHU][1].y = 135; machines[ERHU][1].orient = 3;
	machines[ERHU][2].x = 45; machines[ERHU][2].y = 135; machines[ERHU][2].orient = 2;
	machines[ERHU][3].x = 15; machines[ERHU][3].y = 75; machines[ERHU][3].orient = 0;
	machines[ERHU][4].x = 15; machines[ERHU][4].y = 45; machines[ERHU][4].orient = 3;
	machines[ERHU][5].x = 15; machines[ERHU][5].y = 15; machines[ERHU][5].orient = 0;
	machines[ERHU][6].x = 75; machines[ERHU][6].y = 15; machines[ERHU][6].orient = 1;
	machines[ERHU][7].x = 105; machines[ERHU][7].y = 15; machines[ERHU][7].orient = 0;
	machines[ERHU][8].x = 135; machines[ERHU][8].y = 15; machines[ERHU][8].orient = 1;
	machines[ERHU][9].x = 135; machines[ERHU][9].y = 45; machines[ERHU][9].orient = 2;
	machines[ERHU][10].x = 135; machines[ERHU][10].y = 75; machines[ERHU][10].orient = 1;
	for (int i = 11; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
	    machines[ERHU][i].x = 0; machines[ERHU][i].y = 0; machines[ERHU][i].orient = 4;
	}
	
    }

    public void setServerConstants() {
	if (DEBUG) System.out.println("setServerConstants");

	int exists = 0;

	labs[WEIL].coords[X][0] = 30 * exists;
	labs[WEIL].coords[X][1] = 300 * exists;
	labs[WEIL].coords[X][2] = 1150 * exists;
	labs[WEIL].coords[X][3] = 1150 * exists;
	labs[WEIL].coords[X][4] = 300 * exists;
	labs[WEIL].coords[X][5] = 30 * exists;
	
	labs[WEIL].coords[Y][0] = 780 * exists;
	labs[WEIL].coords[Y][1] = 780 * exists;
	labs[WEIL].coords[Y][2] = 780 * exists;
	labs[WEIL].coords[Y][3] = 800 * exists;
	labs[WEIL].coords[Y][4] = 800 * exists;
	labs[WEIL].coords[Y][5] = 800 * exists;

	labs[WAGN].coords[X][0] = 30 * exists;
	labs[WAGN].coords[X][1] = 300 * exists;
	labs[WAGN].coords[X][2] = 1150 * exists;
	labs[WAGN].coords[X][3] = 1150 * exists;
	labs[WAGN].coords[X][4] = 300 * exists;
	labs[WAGN].coords[X][5] = 30 * exists;

	labs[WAGN].coords[Y][0] = 810 * exists;
	labs[WAGN].coords[Y][1] = 810 * exists;
	labs[WAGN].coords[Y][2] = 810 * exists;
	labs[WAGN].coords[Y][3] = 830 * exists;
	labs[WAGN].coords[Y][4] = 830 * exists;
	labs[WAGN].coords[Y][5] = 830 * exists;

	labs[WILL].coords[X][0] = 30 * exists;
	labs[WILL].coords[X][1] = 300 * exists;
	labs[WILL].coords[X][2] = 1150 * exists;
	labs[WILL].coords[X][3] = 1150 * exists;
	labs[WILL].coords[X][4] = 300 * exists;	
	labs[WILL].coords[X][5] = 30 * exists;
	
	labs[WILL].coords[Y][0] = 840 * exists;
	labs[WILL].coords[Y][1] = 840 * exists;
	labs[WILL].coords[Y][2] = 840 * exists;
	labs[WILL].coords[Y][3] = 860 * exists;
	labs[WILL].coords[Y][4] = 860 * exists;
	labs[WILL].coords[Y][5] = 860 * exists;
    }

    public void setPrinterConstants() {
	
	printers[PSHOOTER].name = "shooter";
	printers[PSHOOTER].x = 470;
	printers[PSHOOTER].y = 190;
	printers[PSHOOTER].visible = true;

	printers[PITCH].name = "itch";
	printers[PITCH].x = 300;
	printers[PITCH].y = 320;
	printers[PITCH].visible = false;

	printers[PKABOO].name = "kaboo";
	printers[PKABOO].x = 770;
	printers[PKABOO].y = 170;
	printers[PKABOO].visible = true;

	printers[PLUMP].name = "lump";
	printers[PLUMP].x = 770;
	printers[PLUMP].y = 200;
	printers[PLUMP].visible = true;

	printers[PLUCK].name = "luck";
	printers[PLUCK].x = 830;
	printers[PLUCK].y = 500;
	printers[PLUCK].visible = true;

	printers[PANTS].name = "ants";
	printers[PANTS].x = 800;
	printers[PANTS].y = 621;
	printers[PANTS].visible = true;

	printers[PLIGHT].name = "light";
	printers[PLIGHT].x = 50;
	printers[PLIGHT].y = 650;
	printers[PLIGHT].visible = true;

	printers[POUCH].name = "ouch";
	printers[POUCH].x = 80;
	printers[POUCH].y = 650;
	printers[POUCH].visible = true;

	printers[PROOF].name = "roof";
	printers[PROOF].x = 615;
	printers[PROOF].y = 710;
	printers[PROOF].visible = true;
	
	printers[PRUDE].name = "rude";
	printers[PRUDE].x = 435;
	printers[PRUDE].y = 710;
	printers[PRUDE].visible = true;

    }

    /*
    public void start() {
	thread = new Thread(this);
	thread.start();
    }
    */


    /*
    public void run() {
	repaint();
	try {
	    thread.sleep(300);
	} catch (InterruptedException e) {
	    System.out.println("Execution Interupted");
	}
	System.exit(0);
    }
    */

    // replaces "~/" with "/home/logname/" because ~/ is bash specific
    //   and wont work if you syscall from java, which uses sh (not bash)
    private String substituteTilde(String command_line) {
	String logname = System.getenv("LOGNAME");
	if (logname != null) {
	    command_line = command_line.replaceAll("~/", "/home/" + logname + "/");
	}
	return command_line.replaceAll("~", "/home/");
    }


    /**
       Loads lab info into all the structures.
    */
    public void loadGfredFile() {

	if (groups.size() > 0) groups.clear();

	try {
	    String filename = substituteTilde("~/.gfriends.d/gfred.gfred");


	    BufferedReader in = new BufferedReader(new FileReader(filename));

	    String buffer = "";
	    
	    while ((buffer = in.readLine()) != null) {

		if (buffer.startsWith("gr")) {	
		    CSEGroup new_group = new CSEGroup();
		    StringTokenizer st_group = new StringTokenizer(buffer.substring(2), ",");
		    new_group.name = st_group.nextToken();
		    new_group.colour = new Color(Integer.parseInt(st_group.nextToken()), Integer.parseInt(st_group.nextToken()), Integer.parseInt(st_group.nextToken()));
		    new_group.show_names = false;
		    if (new_group.name.equals("friends")) new_group.show_names = true;
		    groups.add(new_group);
		    continue;
		}

		int lab;
		int machine_number;
		char status;
		char user_type;
		float therm;
		String tail_of_buffer;
		
		lab = mapLabStringToInt(buffer.substring(0,4));
		if (buffer.substring(4,5).equals("n") || 
		    buffer.substring(4,5).equals("x") ||
		    buffer.substring(4,5).equals("c") ||
		    buffer.substring(4,5).equals("w")) {
		    labs[lab].status = buffer.charAt(4);
		    try {
			int therm_str_end_index = 9;
			if (buffer.length() < therm_str_end_index) therm_str_end_index = buffer.length();
			labs[lab].therm = Float.parseFloat(buffer.substring(5,therm_str_end_index));
		    } catch (StringIndexOutOfBoundsException ex123) {
			labs[lab].therm = 23.0f;
		    } catch (Exception ex456) {
			labs[lab].therm = 23.0f;
		    }
		    if (buffer.length() >= 9) {
			labs[lab].course = buffer.substring(9);
		    } else {
			labs[lab].course = "";
		    }
		} else {
		    try {
			machine_number = Integer.parseInt(buffer.substring(4,6));
			machines[lab][machine_number].status = buffer.charAt(6);
			machines[lab][machine_number].user_type = buffer.charAt(7);
			if (!buffer.substring(8).equals("")) {
			    StringTokenizer st_user = new StringTokenizer(buffer.substring(8), "=");
			    
			    
			    if (st_user.hasMoreTokens()) machines[lab][machine_number].username = st_user.nextToken();
			    if (st_user.hasMoreTokens()) machines[lab][machine_number].nickname = st_user.nextToken();
			    if (st_user.hasMoreTokens()) machines[lab][machine_number].group_index = Integer.parseInt(st_user.nextToken());
			} else {
			    machines[lab][machine_number].username = "";
			    machines[lab][machine_number].nickname = "";
			    machines[lab][machine_number].group_index = -1;
			}
		    } catch (StringIndexOutOfBoundsException ex) {
			System.out.println("Error:buffer=" + buffer);
		    } catch (NumberFormatException ex2) {
			System.out.println("Glen's Exclusion Princliple:Only one equals sign can occupy one line at any single time for a particular energy eigenstate acquaintance. Yay!");
			System.out.println("For the non-physicist, in your ~/.friends file you should only have one equals sign per line. That is, the line should be of the standard format:");
			System.out.println("name = <login>, <nickname>");
			System.out.println("One major cause of this is accidentally putting two entries on the one line. For example:");
			System.out.println("name = stevec, Stevename = ijgo605, Ian");
			System.out.println("would break the script. Obviously a new line is required between 'Steve' and 'name'");
			
			System.out.println("---------------------------------------");
			System.out.println("Try looking at the entry in your ~/.friends list");
			System.out.println("with this person's details:");
			System.out.println("  " + buffer.substring(8));
			System.out.println("---------------------------------------");
			
			System.exit(0);
		    }
		}
		
		
	    }
	    
	    in.close();
	} catch (IOException ex) {
	    System.out.println("file load fkd up");
	}

    }

    /**
       Takes in the 4 char name of the lab and returns a constant for it.
    */
    public int mapLabStringToInt(String slab) {
	
	slab = slab.toLowerCase();
	
	if (slab.equals("harp")) return HARP;
	if (slab.equals("tuba")) return TUBA;
	if (slab.equals("drum")) return DRUM;
	if (slab.equals("bong")) return BONG;
	if (slab.equals("moog")) return MOOG;
	if (slab.equals("bell")) return BELL;
	if (slab.equals("leaf")) return LEAF;
	if (slab.equals("spoo") || slab.equals("spoons")) return SPOO;
	if (slab.equals("bugl") || slab.equals("bugle")) return BUGL;
	if (slab.equals("pipe")) return PIPE;
	if (slab.equals("banj") || slab.equals("banjo")) return BANJ;	
	if (slab.equals("oudx") || slab.equals("oud")) return OUDX;
	if (slab.equals("cong")) return BONG; // since cong is really bong 
	if (slab.equals("clav") || slab.equals("clavier")) return CLAV;
	if (slab.equals("pian") || slab.equals("piano")) return PIAN;
	if (slab.equals("orga") || slab.equals("organ")) return ORGA;
	if (slab.equals("weil")) return WEIL;
	if (slab.equals("wagn")) return WAGN;
	if (slab.equals("will")) return WILL;
	if (slab.equals("oboe")) return OBOE;
	if (slab.equals("sanh") || slab.equals("sanhu")) return SANH;
	if (slab.equals("erhu")) return ERHU;

	return 0;
    }   

    public String mapLabIntToString(int ilab) {
	if (ilab == HARP) return "harp";
	if (ilab == TUBA) return "tuba";
	if (ilab == DRUM) return "drum";
	if (ilab == BONG) return "bongo";
	if (ilab == MOOG) return "moog";
	if (ilab == BELL) return "bell";
	if (ilab == LEAF) return "leaf";
	if (ilab == SPOO) return "spoons";
	if (ilab == BUGL) return "bugle";
	if (ilab == PIPE) return "pipe";
	if (ilab == BANJ) return "banjo";
	if (ilab == OUDX) return "oud";
	if (ilab == CLAV) return "clavier";
	if (ilab == PIAN) return "piano";
	if (ilab == ORGA) return "organ";
	if (ilab == WEIL) return "weill";
	if (ilab == WAGN) return "wagner";
	if (ilab == WILL) return "williams";
	if (ilab == OBOE) return "oboe";
	if (ilab == SANH) return "sanhu";
	if (ilab == ERHU) return "erhu";
	
	return "";
    }

    /**
       THE MASTER PAINT METHOD.
    */
    public void paint(Graphics g) {

	// clear buffer (b/c we're using JFrame, which uses dbl buffering)
	g.setColor(Color.white);
	g.fillRect(0,0,1200,950);	
	
	ad_banner.paint(g);
	
	g.setColor(Color.black);
	//DRAW KEY
	paintKey(g);
		 
	//g.setColor(Color.red);
	//g.drawLine(300, 550, 400, 550);
	//g.drawLine(300, 570, 400, 570);
	//g.drawLine(380, 530, 420, 560);
	//g.drawLine(380, 590, 420, 560);
	//g.drawString("Some of the labs and most of the machines", 430, 570);
	//g.drawString("are under maintenance. You'll just have to be patient.", 430, 590);

	
	// window size is 1200x950
	/*
	if (user_using_laptop) {
	    g.setColor(new Color(0,255,0));
	    g.fillRoundRect(425, 400, 280, 100, 5, 5);
	    g.setColor(Color.black);
	    g.drawString("Hello low screen res user", 430, 415);
	    g.drawString("There are now scrollbars to", 430, 435);
	    g.drawString("make your life easier", 430, 455);
	    
	}
	*/
	

	// GOLD g.setColor(new Color(255,180,0));

	/*
	g.setColor(new Color(0,255,0));
	g.fillRoundRect(400+40, 550, 350, 80, 5, 5);
	g.setColor(Color.black);
	g.drawString("CSEsoc", 405+40, 565);
	g.drawString("http://www.csesoc.unsw.edu.au", 405+40, 585);
	*/

	//	g.drawString("", 405+40, 605);
	//	g.drawString("(www.csesoc.cse.unsw.edu.au)", 405+40, 625);

	
	/* // the "why gfriends is so slow - frozen computers" message
	g.setColor(Color.red);
	g.drawString("Why do gfriends or friends take so long sometimes ???", 15, 815);
	g.setColor(Color.black);
	g.drawString("It is because people who's computers have frozen,", 15, 830);
	g.drawString("don't turn their computers back on after turning", 15, 845);
	g.drawString("them off. Asside from the fact that it's against", 15, 860);
	g.drawString("Yellow Form, it slows gfriends and friends down", 15, 875);
	g.drawString("for everyone. Stop being a n00b and do the right thing.", 15, 890);
	*/

	//g.drawString("Heaps of prizes and FREE pizza and drinks", 480, 590);
	//g.drawString("Register teams of 8 at sesoc.trivia@cse", 480, 610);
	
	/*
	Toolkit tk = Toolkit.getDefaultToolkit();
	Image compsoc = tk.getImage("quake3.jpg");
	g.drawImage(compsoc, 480, 430, 369, 123, null);
	*/

	
	//-- lines pointing from above text to different labs (used for murder@CSE advertising)

	g.setColor(new Color(0, 255, 0));
	g.fillRoundRect(450, 410, 300, 100, 5, 5);
	g.setColor(new Color(0, 0, 0));
	g.drawString("Want to stalk people more???", 460, 430);
	g.drawString("/home/stevec/bin/scmess", 460, 450);
	g.drawString("A messenger style pop-up tracker monitoring", 460, 470);
	g.drawString("all users in CSE. (feedback to stevec@cse)", 460, 490);

	/*
	g.setColor(new Color(0,0,0));
	//g.fillRoundRect(425, 435, 170, 40, 5, 5);
	//g.fillRoundRect(425, 435, 190, 40, 5, 5);
	g.fillRoundRect(425, 435, 255, 40, 5, 5);
	g.setColor(Color.white);
	g.drawString("murder@CSE", 430, 450);
	g.setColor(new Color(255,0,0));
	//g.drawString("Register this week 12-1pm outside bugle", 430, 470);
	g.drawString("Where is your victim now ???", 430, 470);

	g.drawLine(685, 455, 750, 455);
	g.drawLine(750, 455, 740, 445);
	g.drawLine(750, 455, 740, 465);
	*/
	/*
	// lines pointing to labs from the "where is your victim now" thing above
	g.setColor(new Color(255,0,0));
	g.drawLine(490, 420, 440, 390); // pointing to EE
	g.drawLine(440, 390, 440, 400);
	g.drawLine(440, 390, 450, 390); 

	g.drawLine(610, 420, 660, 370); // pointing to ME
	g.drawLine(660, 370, 650, 370);
	g.drawLine(660, 370, 660, 380);

	g.drawLine(630, 460, 750, 530); // pointing to Eatery
	g.drawLine(750, 530, 740, 530);
	g.drawLine(750, 530, 750, 520);

	g.drawLine(520, 491, 415, 580);
	g.drawLine(415, 580, 425, 580);
	g.drawLine(415, 580, 415, 570);
	*/

	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    if (labs[ll].status == 'z') {
		paintLabOutline(labs[ll], g);
	    } else {
		paintLab(labs[ll], g);
	    }
	}

	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    for (int mm = 0; mm < MAX_NUMBER_OF_MACHINES_IN_LAB; mm++) {
		if (machines[ll][mm].status == 'z') continue;
		paintMachine(ll, machines[ll][mm], g);
	    }
	}

	paintStructures(g);
	paintPrinters(g);

	//drawIcon("q3", 500, 500, g, Color.white);
	//drawIcon("et", 532, 500, g, Color.white);
	//drawIcon("trem", 564, 500, g, Color.white);

	if (sc_in_office) {
	    g.setColor(cLabNormal);
	    g.fillRect(800, 800, 100, 50);

	    g.setColor(Color.black);
	    g.drawRect(800, 800, 100, 50);

	    g.setColor(cUserFred);
	    g.fillOval(890, 835, 10, 10);
	    
	    g.drawString("Steve", 850, 840);
	    
	    g.setColor(Color.white);
	    g.drawString("ME L205", 805, 820);
	}

	// the check server buttons
	int server_button_x = 920;
	int server_button_y = 800;       

	g.setColor(Color.green);
	g.fillRoundRect(server_button_x, server_button_y +  0, 90, 15, 5, 5);
	g.fillRoundRect(server_button_x, server_button_y + 20, 90, 15, 5, 5);
	g.fillRoundRect(server_button_x, server_button_y + 40, 90, 15, 5, 5);
	
	g.setColor(Color.black);
	g.drawString("Scan wagner", server_button_x + 5, server_button_y + 12);
	g.drawString("Scan weill", server_button_x + 5, server_button_y + 32);
	g.drawString("Scan williams", server_button_x + 5, server_button_y + 52);

	//---------------------------------------
	// zoomed in lab
	if (zoomed_lab != null) {
	    //System.out.println("Selected lab number " + cur_zoomed_lab);
	    paintZoomedLab(g);	   	    
	    
	}
	
	info_window.paintWin(g);       
    }


    /**
       draws a zoom/resize icon centered at a given location
       (looks like 4 arrows pointing up,down,left,right)
    */
    public void paintZoomIcon(int x, int y, Graphics g) {
		
	int neck_size = 7; // length of an arm of the arrows
	int head_size = 2; // length of the arms of the arrow head

	g.setColor(Color.black);	
	g.fillRoundRect(x - (neck_size + 1), y - (neck_size + 1), 2*(neck_size + 1), 2*(neck_size + 1), 5, 5);	
	
	g.setColor(Color.white);	
	g.drawRoundRect(x - (neck_size + 1), y - (neck_size + 1), 2*(neck_size + 1), 2*(neck_size + 1), 5, 5);
	
	g.setColor(Color.white);

	g.drawLine(x - neck_size, y, x + neck_size, y); // horizontal line
	g.drawLine(x, y - neck_size, x, y + neck_size); // vertical line
	

	for (int i = -1; i < 2; i+=2) {
	    g.drawLine(x - neck_size, y, x - neck_size + head_size, y + i*head_size); //west head
	    g.drawLine(x + neck_size, y, x + neck_size - head_size, y + i*head_size); //east head


	    g.drawLine(x, y - neck_size, x + i*head_size, y - neck_size + head_size); //north head
	    g.drawLine(x, y + neck_size, x + i*head_size, y + neck_size - head_size); //south head

	}
    }

    /**
       paints the key/legend on the right side of the screen
    */
    public void paintKey(Graphics g) {
	int key_box_x = 1025; //the upper left corner of the legend/key
	int key_box_y = 420;  //the upper left corner of the legend/key
	
	int cur_y_draw_coord = key_box_y;

	g.drawRoundRect(key_box_x, key_box_y, 160, 430, 10, 10);

	g.drawString("--Lab Status--", key_box_x + 10, cur_y_draw_coord += 20);
	g.drawString("Normal Lab", key_box_x + 40, cur_y_draw_coord += 20); 
	g.drawString("Full/Class Lab", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("Closed Lab", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("CSE Server", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("--Computer Status--", key_box_x + 10, cur_y_draw_coord += 20);
	g.drawString("Down Computer", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("Occupied Computer", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("Booked Computer", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("Free Computer", key_box_x + 40, cur_y_draw_coord += 20);
	g.drawString("--User Status--", key_box_x + 10, cur_y_draw_coord += 20);
	g.drawString("Friend", key_box_x + 40, cur_y_draw_coord += 20);
	if (paint_random_people_logins) {
	    g.drawString("Random [hide ids]", key_box_x + 40, cur_y_draw_coord += 20);
	} else {
	    g.drawString("Random [show ids]", key_box_x + 40, cur_y_draw_coord += 20);
	}       

	int custom_groups_painted_to_key = 0;
       	for(int gr_i = 0; gr_i < groups.size(); gr_i++) {
	    if (groups.get(gr_i).name.equals("friends")) continue;
	    custom_groups_painted_to_key++;
	    if (custom_groups_painted_to_key > 4) break;
	    g.setColor(groups.get(gr_i).colour);
	    g.fillOval(key_box_x + 18, key_box_y + 250 + 20*custom_groups_painted_to_key, 10, 10);
	    g.setColor(Color.black);
	    g.drawString(groups.get(gr_i).name, key_box_x + 40, cur_y_draw_coord += 20);
	}
	g.drawString("--Other--", key_box_x + 10, cur_y_draw_coord += 20);
	g.drawString("Printer", key_box_x + 40, cur_y_draw_coord += 20);

	int friendliest_lab_index = getFriendliestLab();
	if (friendliest_lab_index >= 0) {
	    g.drawString("Friendliest Lab: " + mapLabIntToString(friendliest_lab_index), key_box_x + 5, cur_y_draw_coord += 20);
	}

	int emptiest_lab_index = getEmptiestLab();
	if (emptiest_lab_index >= 0) {
	    g.drawString("Emptiest Lab: " + mapLabIntToString(emptiest_lab_index), key_box_x + 5, cur_y_draw_coord += 20);
	}
	int noobest_lab_index = getNoobestLab();
	if (noobest_lab_index >= 0) {
	    g.drawString("n00b-est Lab: " + mapLabIntToString(noobest_lab_index), key_box_x + 5, cur_y_draw_coord += 20);
	}
	int leetest_lab_index = getLeetestLab();
	if (leetest_lab_index >= 0) {
	    g.drawString("1337-est Lab: " + mapLabIntToString(leetest_lab_index), key_box_x + 5, cur_y_draw_coord += 20);
	}
	
	cur_y_draw_coord = key_box_y + 10;
	
	g.setColor(cLabNormal); g.fillRect(key_box_x + 10, cur_y_draw_coord += 20, 25, 10);
	g.setColor(cLabClass);  g.fillRect(key_box_x + 10, cur_y_draw_coord += 20, 25, 10);
	g.setColor(cLabDown);   g.fillRect(key_box_x + 10, cur_y_draw_coord += 20, 25, 10);
	g.setColor(cLabServer); g.fillRect(key_box_x + 10, cur_y_draw_coord += 20, 25, 10);
	g.setColor(cLabNormal); g.fillRect(key_box_x + 10, cur_y_draw_coord += 40, 25, 10);
	g.setColor(cMachineDown); 
	g.drawLine(key_box_x + 13, key_box_y + 134, key_box_x + 28, key_box_y + 134);
	g.drawLine(key_box_x + 14, key_box_y + 135, key_box_x + 27, key_box_y + 135);
	g.drawLine(key_box_x + 15, key_box_y + 136, key_box_x + 26, key_box_y + 136);
	g.setColor(cLabNormal); g.fillRect(key_box_x + 10, key_box_y + 150, 25, 10);
	g.setColor(cMachineOccupied);
	g.drawLine(key_box_x + 13, key_box_y + 154, key_box_x + 28, key_box_y + 154);
	g.drawLine(key_box_x + 14, key_box_y + 155, key_box_x + 27, key_box_y + 155);
	g.drawLine(key_box_x + 15, key_box_y + 156, key_box_x + 26, key_box_y + 156);
	g.setColor(cLabNormal); g.fillRect(key_box_x + 10, key_box_y + 170, 25, 10);
	g.setColor(cMachineBooked);
	g.drawLine(key_box_x + 13, key_box_y + 174, key_box_x + 28, key_box_y + 174);
	g.drawLine(key_box_x + 14, key_box_y + 175, key_box_x + 27, key_box_y + 175);
	g.drawLine(key_box_x + 15, key_box_y + 176, key_box_x + 26, key_box_y + 176);
	g.setColor(cLabNormal); g.fillRect(key_box_x + 10, key_box_y + 190, 25, 10);
	g.setColor(cMachineAvailable);
	g.drawLine(key_box_x + 13, key_box_y + 194, key_box_x + 28, key_box_y + 194);
	g.drawLine(key_box_x + 14, key_box_y + 195, key_box_x + 27, key_box_y + 195);
	g.drawLine(key_box_x + 15, key_box_y + 196, key_box_x + 26, key_box_y + 196);
	g.setColor(cUserFred); g.fillOval(key_box_x + 18, key_box_y + 230, 10, 10);
	g.setColor(cUserRandom); g.fillOval(key_box_x + 18, key_box_y + 250, 10, 10);
	g.setColor(cPrinter); g.fillRect(key_box_x + 10, key_box_y + 250 + 20*groups.size(), 25, 10);

    }

    /**
       Paints a given lab.
    */
    public void paintLab(CSELab lab, Graphics g) {

	if (lab.visible == false) return;

	if (lab.status == 'n') g.setColor(new Color(cLabNormal.getRed()/255.0f,
						    cLabNormal.getGreen()/255.0f,
						    cLabNormal.getBlue()/255.0f,
						    lab.alpha));
	if (lab.status == 'x') g.setColor(new Color(cLabDown.getRed()/255.0f,
						    cLabDown.getGreen()/255.0f,
						    cLabDown.getBlue()/255.0f,
						    lab.alpha));
	if (lab.status == 'c') g.setColor(new Color(cLabClass.getRed()/255.0f,
						    cLabClass.getGreen()/255.0f,
						    cLabClass.getBlue()/255.0f,
						    lab.alpha));
	if (lab.status == 'w') g.setColor(new Color(cLabServer.getRed()/255.0f,
						    cLabServer.getGreen()/255.0f,
						    cLabServer.getBlue()/255.0f,
						    lab.alpha));

	int[][] local_coords = new int[2][6];
	for (int i = 0; i < 6; i++) {
	    local_coords[X][i] = lab.x + lab.coords[X][i];
	    local_coords[Y][i] = lab.y + lab.coords[Y][i];
	}	  

	g.fillPolygon(local_coords[X], local_coords[Y], 6);

	g.setColor(new Color(0.0f, 0.0f, 0.0f, lab.alpha));
	for (int i = 0; i < 6; i++) {
	    g.drawLine(local_coords[X][i], local_coords[Y][i], local_coords[X][(i+1) % 6], local_coords[Y][(i+1) % 6]);
	}

    }

    /**
       Paints the outline of a lab... called when the lab couldn't be scanned for data.
     */
    public void paintLabOutline(CSELab lab, Graphics g) {
		
	if (lab.visible == false) return;

	int[][] local_coords = new int[2][6];
	for (int i = 0; i < 6; i++) {
	    local_coords[X][i] = lab.x + lab.coords[X][i];
	    local_coords[Y][i] = lab.y + lab.coords[Y][i];
	}	  

	g.setColor(new Color(0.0f, 0.0f, 0.0f, lab.alpha));
	for (int i = 0; i < 6; i++) {
	    g.drawLine(local_coords[X][i], local_coords[Y][i], local_coords[X][(i+1) % 6], local_coords[Y][(i+1) % 6]);
	}
	
    }

    /**
       Paints a given machine in a given lab.
    */
    public void paintMachine(int lab, CSEMachine mach, Graphics g) {

	// private request list of people not wanting to be found by Vlad
	//  (whether I agree or not I'm providing it as a free service)
	if (user_username.equals("vapa389")) {
	    if (mach.username.startsWith("hmeh692") || 
		mach.username.startsWith("pdut154") ||
		mach.username.startsWith("pauld000") ||
		mach.username.startsWith("harij666")) {
		mach.status = 'a';
		mach.user_type = 'u';
	    }
	}

	// Hide me from a guy who literally started stalking me
	if (user_username.equals("edle337")) {
	    if (mach.username.startsWith("stevec") ||
		mach.username.startsWith("scos506") ||
		mach.username.startsWith("scossell")) {
		mach.status = 'a';
		mach.user_type = 'u';
	    }
	}
	

	// hide me when murder is on
	/*
	if (mach.username.startsWith("scos506") ||
	    mach.username.startsWith("scossell") ||
	    mach.username.startsWith("stevec")) {
	    mach.status = 'a';
	    mach.user_type = 'u';
	}
	*/

	if (mach.status == 'o') g.setColor(new Color(cMachineOccupied.getRed()/255.0f,
						     cMachineOccupied.getGreen()/255.0f,
						     cMachineOccupied.getBlue()/255.0f,
						     mach.alpha));
	if (mach.status == 'd') g.setColor(new Color(cMachineDown.getRed()/255.0f,
						     cMachineDown.getGreen()/255.0f,
						     cMachineDown.getBlue()/255.0f,
						     mach.alpha));
	if (mach.status == 'b') g.setColor(new Color(cMachineBooked.getRed()/255.0f,
						     cMachineBooked.getGreen()/255.0f,
						     cMachineBooked.getBlue()/255.0f,
						     mach.alpha));
	if (mach.status == 'a') g.setColor(new Color(cMachineAvailable.getRed()/255.0f,
						     cMachineAvailable.getGreen()/255.0f,
						     cMachineAvailable.getBlue()/255.0f,
						     mach.alpha));

	if (mach.orient == 4) return;
	if (mach.orient == 0) {
	    g.drawLine(labs[lab].x + mach.x + 5, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x - 5, labs[lab].y + mach.y + 5); 
	    g.drawLine(labs[lab].x + mach.x + 4, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x - 5, labs[lab].y + mach.y + 4);
	    g.drawLine(labs[lab].x + mach.x + 3, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x - 5, labs[lab].y + mach.y + 3);
	}
	if (mach.orient == 1) {
	    g.drawLine(labs[lab].x + mach.x - 5, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x + 5, labs[lab].y + mach.y + 5);
	    g.drawLine(labs[lab].x + mach.x - 4, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x + 5, labs[lab].y + mach.y + 4);
	    g.drawLine(labs[lab].x + mach.x - 3, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x + 5, labs[lab].y + mach.y + 3);
	}
	if (mach.orient == 2) {
	    g.drawLine(labs[lab].x + mach.x + 5, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x - 5, labs[lab].y + mach.y + 5);
	    g.drawLine(labs[lab].x + mach.x + 5, labs[lab].y + mach.y - 4, 
		       labs[lab].x + mach.x - 4, labs[lab].y + mach.y + 5);
	    g.drawLine(labs[lab].x + mach.x + 5, labs[lab].y + mach.y - 3, 
		       labs[lab].x + mach.x - 3, labs[lab].y + mach.y + 5);
	}
	if (mach.orient == 3) {
	    g.drawLine(labs[lab].x + mach.x - 5, labs[lab].y + mach.y - 5, 
		       labs[lab].x + mach.x + 5, labs[lab].y + mach.y + 5);
	    g.drawLine(labs[lab].x + mach.x - 5, labs[lab].y + mach.y - 4, 
		       labs[lab].x + mach.x + 4, labs[lab].y + mach.y + 5);
	    g.drawLine(labs[lab].x + mach.x - 5, labs[lab].y + mach.y - 3, 
		       labs[lab].x + mach.x + 3, labs[lab].y + mach.y + 5);
	}

	if (mach.user_type == 'u') return;
	//if (mach.user_type == 'm') g.setColor(cUserMurder);
	//if (mach.user_type == 's') g.setColor(cUserSeng);
	//if (mach.user_type == 'r') g.setColor(cUserRandom);	       

	if (mach.group_index == -1) {
	    g.setColor(new Color(cUserRandom.getRed()/255.0f, 
				 cUserRandom.getGreen()/255.0f,
				 cUserRandom.getBlue()/255.0f,
				 mach.alpha));
	    if (paint_random_people_logins) g.drawString(mach.username, 
							 labs[lab].x + mach.x - 5, 
							 labs[lab].y + mach.y + 15);	    
	} else {
	    Color group_colour = new Color(groups.get(mach.group_index).colour.getRed()/255.0f,
					   groups.get(mach.group_index).colour.getGreen()/255.0f,
					   groups.get(mach.group_index).colour.getBlue()/255.0f,
					   mach.alpha);
	    g.setColor(group_colour);
	}	

	if (mach.orient == 0) g.fillOval(labs[lab].x + mach.x, labs[lab].y + mach.y, 10, 10);
	if (mach.orient == 1) g.fillOval(labs[lab].x + mach.x - 10, labs[lab].y + mach.y, 10, 10);
	if (mach.orient == 2) g.fillOval(labs[lab].x + mach.x - 10, labs[lab].y + mach.y - 10, 10, 10);
	if (mach.orient == 3) g.fillOval(labs[lab].x + mach.x, labs[lab].y + mach.y - 10, 10, 10);
	
	if (!mach.nickname.equals("")) {	    
	    paintNickName(mach.nickname, labs[lab].x + mach.x - 5, labs[lab].y + mach.y + 15, g);
	    //g.drawString(mach.nickname, mach.x - 5, mach.y + 15);
	}

	if (mach.highlighted) {
	    g.setColor(new Color(255,255,0));
	    
	    int sqr_size = 27;
	    
	    g.drawRoundRect(labs[lab].x + mach.x - (sqr_size/2), labs[lab].y + mach.y - (sqr_size/2), sqr_size, sqr_size, 6, 6);
	}

	    
    }

    /**
       Paints a nickname with the correct ET/Q3 style colouring.
    */
    public void paintNickName(String nickname, int x, int y, Graphics g) {
	//FontMetrics fm = new FontMetrics(g.getFont());
	int font_width = 0;

	et_colours cols = new et_colours();

	for (int i = 0; i < nickname.length(); i++) {
	    if (nickname.charAt(i) == '^') {
		if (i + 1 == nickname.length()) return;
		//if (nickname.charAt(i+1) == '7') g.setColor(Color.white);
		Color tmp = cols.getColor(nickname.charAt(i+1));
		// include some alpha stuff here in the future
		g.setColor(tmp);
		i++;
	    } else {
		g.drawString("" + nickname.charAt(i), x + font_width, y);
		font_width += g.getFontMetrics().charWidth(nickname.charAt(i));
	    }
	}
    }

    /**
       paints a zoomed in lab (get rid of this)
    */
    public void paintZoomedLab(Graphics g) {
	
	/* // darkening the background
	g.setColor(new Color(0.1f, 0.1f, 0.1f, 0.4f));
	g.fillRect(0,0,1200,950);
	*/

	CSELab cur_lab = zoomed_lab;
	
	int scale = 3;

	int left = (int)Math.round((1200-cur_lab.width*scale)/2.0);
	int top  = (int)Math.round((850-cur_lab.height*scale)/2.0);

	/* border (looks crap)
	g.setColor(Color.black);
	g.fillRoundRect(left - 5, top - 5, cur_lab.width*scale + 10, cur_lab.height*scale + 10, 10, 10);	
	*/

	if (cur_lab.status == 'n') g.setColor(cLabNormal);
	if (cur_lab.status == 'x') g.setColor(cLabDown);
	if (cur_lab.status == 'c') g.setColor(cLabClass);
	if (cur_lab.status == 'w') g.setColor(cLabServer);
	
	int[][] local_coords = new int[2][6];
	for (int i = 0; i < 6; i++) {
	    local_coords[X][i] = left + cur_lab.coords[X][i]*scale;
	    local_coords[Y][i] = top  + cur_lab.coords[Y][i]*scale;
	}	  
	
	g.fillPolygon(local_coords[X], local_coords[Y], 6);
	
	g.setColor(Color.black);
	for (int i = 0; i < 6; i++) {
	    g.drawLine(local_coords[X][i]-1,local_coords[Y][i]-1,local_coords[X][(i+1) % 6]-1,local_coords[Y][(i+1) % 6]-1);	    
	    g.drawLine(local_coords[X][i],  local_coords[Y][i],  local_coords[X][(i+1) % 6],  local_coords[Y][(i+1) % 6]);
	    g.drawLine(local_coords[X][i]+1,local_coords[Y][i]+1,local_coords[X][(i+1) % 6]+1,local_coords[Y][(i+1) % 6]+1);	    
	}
	
	for (int m = 0; m < MAX_NUMBER_OF_MACHINES_IN_LAB; m++) {
	    if (machines[cur_lab.id][m].status == 'z') continue;

	    //System.out.println("zoomed:" + m);

	    CSEMachine mach = machines[cur_lab.id][m];
	    
	    if (mach.status == 'o') g.setColor(cMachineOccupied);
	    if (mach.status == 'd') g.setColor(cMachineDown);
	    if (mach.status == 'b') g.setColor(cMachineBooked);
	    if (mach.status == 'a') g.setColor(cMachineAvailable);

	    if (mach.orient == 4) continue;
	    if (mach.orient == 0) {
		g.drawLine(left + scale*(mach.x + 5), top + scale*(mach.y - 5), 
			   left + scale*(mach.x - 5), top + scale*(mach.y + 5)); 
		g.drawLine(left + scale*(mach.x + 4), top + scale*(mach.y - 5), 
			   left + scale*(mach.x - 5), top + scale*(mach.y + 4));
		g.drawLine(left + scale*(mach.x + 3), top + scale*(mach.y - 5), 
			   left + scale*(mach.x - 5), top + scale*(mach.y + 3));
	    }
	    if (mach.orient == 1) {
		g.drawLine(left + scale*(mach.x - 5), top + scale*(mach.y - 5), 
			   left + scale*(mach.x + 5), top + scale*(mach.y + 5));
		g.drawLine(left + scale*(mach.x - 4), top + scale*(mach.y - 5), 
			   left + scale*(mach.x + 5), top + scale*(mach.y + 4));
		g.drawLine(left + scale*(mach.x - 3), top + scale*(mach.y - 5), 
			   left + scale*(mach.x + 5), top + scale*(mach.y + 3));
	    }
	    if (mach.orient == 2) {
		g.drawLine(left + scale*(mach.x + 5), top + scale*(mach.y - 5), 
			   left + scale*(mach.x - 5), top + scale*(mach.y + 5));
		g.drawLine(left + scale*(mach.x + 5), top + scale*(mach.y - 4), 
			   left + scale*(mach.x - 4), top + scale*(mach.y + 5));
		g.drawLine(left + scale*(mach.x + 5), top + scale*(mach.y - 3), 
			   left + scale*(mach.x - 3), top + scale*(mach.y + 5));
	    }
	    if (mach.orient == 3) {
		g.drawLine(left + scale*(mach.x - 5), top + scale*(mach.y - 5), 
			   left + scale*(mach.x + 5), top + scale*(mach.y + 5));
		g.drawLine(left + scale*(mach.x - 5), top + scale*(mach.y - 4), 
			   left + scale*(mach.x + 4), top + scale*(mach.y + 5));
		g.drawLine(left + scale*(mach.x - 5), top + scale*(mach.y - 3), 
			   left + scale*(mach.x + 3), top + scale*(mach.y + 5));
	    }

	    if (mach.user_type == 'u') continue;
	    //if (mach.user_type == 'm') g.setColor(cUserMurder);
	    //if (mach.user_type == 's') g.setColor(cUserSeng);
	    //if (mach.user_type == 'r') g.setColor(cUserRandom);	       

	    if (mach.group_index == -1) {
		g.setColor(new Color(0,100,255));
	    } else {
		// make slightly lighter
		g.setColor(lighten(groups.get(mach.group_index).colour));		
	    }

	    if (mach.orient == 0) g.fillOval(left + scale*mach.x, top + scale*mach.y, scale*10, scale*10);
	    if (mach.orient == 1) g.fillOval(left + scale*(mach.x - 10), top + scale*mach.y, scale*10, scale*10);
	    if (mach.orient == 2) g.fillOval(left + scale*(mach.x - 10), top + scale*(mach.y - 10), scale*10, scale*10);
	    if (mach.orient == 3) g.fillOval(left + scale*mach.x, top + scale*(mach.y - 10), scale*10, scale*10);
	  
	    if (mach.user_info.equals("")) {
		mach.user_info = getUserInfo(mach.username);
	    } 

	    String current_user_info_string = mach.user_info;

	    // if I'm painting the machine of a random
	    if (mach.group_index == -1) {
		//g.setColor(cUserRandom);
		
		// make slightly darker
		g.setColor(darken(cUserRandom));
		
		Font normal_font = g.getFont();
		g.setFont(new Font(normal_font.getFontName(), normal_font.getStyle(), (int)Math.round(normal_font.getSize() * scale * 0.5)));
		g.drawString(mach.username, left + scale*(mach.x) - 20, top + scale*(mach.y) + ((mach.orient < 2) ? 25 : 0));
		g.setFont(normal_font);

		String random_persons_name;
		try {
		    random_persons_name = current_user_info_string.split(":")[0];
		} catch (ArrayIndexOutOfBoundsException ex3) {
		    random_persons_name = "";
		}
		if (!random_persons_name.equals("...")) {
		    g.drawString(random_persons_name, left + scale*(mach.x) - 20, top + scale*(mach.y) + 13 + ((mach.orient < 2) ? 25 : 0));
		}
	    } else {
		
		// else, set the colour to the group colour of the person you're about the print the name of
		//g.setColor(groups.get(mach.group_index).colour);

		g.setColor(darken(groups.get(mach.group_index).colour));
	    }	
	    
	    
	    if (!mach.nickname.equals("")) {
		Font normal_font = g.getFont();
		g.setFont(new Font(normal_font.getFontName(), normal_font.getStyle(), (int)Math.round(normal_font.getSize() * scale * 0.5)));
		paintNickName(mach.nickname, left + scale*(mach.x) - 20, top + scale*(mach.y) + ((mach.orient < 2) ? 25 : 0), g);
		g.setFont(normal_font);
		//g.drawString(mach.nickname, mach.x - 5, mach.y + 15);
	    }

	    // print out their classes and tutorings
	    if (!current_user_info_string.equals("...")) {
		String [] user_info_tokens = current_user_info_string.split(":");

		Font normal_font = g.getFont();
		g.setFont(new Font(normal_font.getFontName(), normal_font.getStyle(), (int)Math.round(normal_font.getSize() * 0.75)));
		
		if (user_info_tokens.length >= 2) {
		    String [] classes_tokens = user_info_tokens[1].split(",");

		    if (classes_tokens.length == 1) {
			g.drawString(classes_tokens[0], left + scale*(mach.x) - 20, top + scale*(mach.y) + 25 + ((mach.orient < 2) ? 25 : 0));
		    } else if (classes_tokens.length >= 2) {
			g.drawString(classes_tokens[0] + ", " + classes_tokens[1], left + scale*(mach.x) - 20, top + scale*(mach.y) + 25 + ((mach.orient < 2) ? 25 : 0));
		    }
		    
		    if (classes_tokens.length == 3) {
			g.drawString(classes_tokens[2], left + scale*(mach.x) - 20, top + scale*(mach.y) + 38 + ((mach.orient < 2) ? 25 : 0));
		    } else if (classes_tokens.length >= 4) {
			g.drawString(classes_tokens[2] + ", " + classes_tokens[3], left + scale*(mach.x) - 20, top + scale*(mach.y) + 38 + ((mach.orient < 2) ? 25 : 0));
		    }
		}

		if (user_info_tokens.length >= 3) {
		    g.drawString("Tutoring:" + user_info_tokens[2], left + scale*(mach.x) - 20, top + scale*(mach.y) + (user_info_tokens[1].split(",").length > 2 ? 51 : 38) + ((mach.orient < 2) ? 25 : 0));
		}

		g.setFont(normal_font);
	    }
	
	}

	// a little close button on the top
	int close_button_left = left + cur_lab.width*scale - 19;
	int close_button_top  = top + 3;
	
	// because oboe is a funny shape
	if (cur_lab.name.equals("OBOE")) {
	    close_button_left -= (cur_lab.width*scale - 22);
	}

	cur_lab.zoomed_close_button_coords[X] = close_button_left;
	cur_lab.zoomed_close_button_coords[Y] = close_button_top;

	g.setColor(Color.black);
	g.fillRect(close_button_left, close_button_top, 16, 16);
	g.setColor(Color.white);
	g.drawLine(close_button_left +  2, close_button_top + 2, 
		   close_button_left + 14, close_button_top + 14);
	g.drawLine(close_button_left + 14, close_button_top + 2, 
		   close_button_left +  2, close_button_top + 14);
	// end close button code

	// a tutor options button at the top next to the close button
	cur_lab.zoomed_options_button_coords[X] = close_button_left - 90;
	cur_lab.zoomed_options_button_coords[Y] = close_button_top;	

	g.setColor(Color.black);
	g.fillRoundRect(close_button_left - 90, close_button_top, 85, 16, 5, 5);
	
	g.setColor(Color.white);
	g.drawString("Tutor Options", close_button_left - 88, close_button_top + 13);	

    }

    /* --print server code
       if (buffer.charAt(4) == 'w') { //Server printing
       g.setColor(cLabServer);
       g.fillPolygon(labs[lab].coords[X], labs[lab].coords[Y], 6);
       String servFreds = buffer.substring(5);
       g.setColor(Color.black);
       g.drawLine(labs[lab].coords[X][0], labs[lab].coords[Y][0], labs[lab].coords[X][1], labs[lab].coords[Y][1]);
       g.drawLine(labs[lab].coords[X][1], labs[lab].coords[Y][1], labs[lab].coords[X][2], labs[lab].coords[Y][2]);
       g.drawLine(labs[lab].coords[X][2], labs[lab].coords[Y][2], labs[lab].coords[X][3], labs[lab].coords[Y][3]);
       g.drawLine(labs[lab].coords[X][3], labs[lab].coords[Y][3], labs[lab].coords[X][4], labs[lab].coords[Y][4]);
       g.drawLine(labs[lab].coords[X][4], labs[lab].coords[Y][4], labs[lab].coords[X][5], labs[lab].coords[Y][5]);
       g.drawLine(labs[lab].coords[X][5], labs[lab].coords[Y][5], labs[lab].coords[X][0], labs[lab].coords[Y][0]);
       g.setColor(Color.white);
       if (lab == WEIL) g.drawString("WEILL->" + servFreds, labs[lab].coords[X][0] + 5, labs[lab].coords[Y][0] + 15);
       if (lab == WAGN) g.drawString("WAGNER->" + servFreds, labs[lab].coords[X][0] + 5, labs[lab].coords[Y][0] + 15);
       if (lab == WILL) g.drawString("WILLIAMS->" + servFreds, labs[lab].coords[X][0] + 5, labs[lab].coords[Y][0] + 15);  
       return;
       } */
		

    public Color darken(Color c) {
	return new Color(Math.max(c.getRed() - 25, 0),
			 Math.max(c.getGreen() - 25, 0),
			 Math.max(c.getBlue() - 25, 0));
    }

    public Color lighten(Color c) {
	return new Color(Math.min(c.getRed() + 25, 255),
			 Math.min(c.getGreen() + 25, 255),
			 Math.min(c.getBlue() + 25, 255));
    }

	
    /**
       Hmmmm, let me think... takes in a number and returns number times number.
    */
    public int sqr(int n) {
	return n * n;
    }
	
    /**
       Paints all the extra little things like doors and corridors.
    */
    public void paintStructures(Graphics g) {

	//DRAW HELP DESK

	boolean help_desk_open = false;
	
	Calendar rightNow = Calendar.getInstance();

	if (rightNow.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
	    rightNow.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
	    
	} else {
	    

	    if (10 < rightNow.get(Calendar.HOUR_OF_DAY) && 
		rightNow.get(Calendar.HOUR_OF_DAY) < 13) {
		help_desk_open = true;
	    } 
	    if (14 < rightNow.get(Calendar.HOUR_OF_DAY) &&
		rightNow.get(Calendar.HOUR_OF_DAY) < 16) {
		help_desk_open = true;
		
	    } 
		
	    if (rightNow.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
		if (16 <= rightNow.get(Calendar.HOUR_OF_DAY) &&
		    rightNow.get(Calendar.HOUR_OF_DAY) < 18) {
		    help_desk_open = true;
		}
	    }
	}
	
	/*
	if (help_desk_open) {
	    g.setColor(cLabNormal);
	    g.fillRect(645-30, 255, 735-615, 375-255);

	    g.setColor(Color.green);
	    g.fillOval(620, 280, 10, 10);
	    
	    g.drawString("Loc", 620, 305);
	} else {
	    g.setColor(cLabDown);
	    g.fillRect(645-30, 255, 735-615, 375-255);
	}


	g.setColor(Color.black);
	g.drawRect(645-30, 255, 735-615, 375-255);

	g.setColor(new Color(180,200,255));
	g.fillRect(640, 256, 20, 60); 
	
	g.setColor(Color.blue);
	g.drawLine(652, 275, 652, 275+16);
	g.drawLine(653, 276, 653, 276+14);
	g.drawLine(654, 277, 654, 277+12);
	*/

	//DRAW PRINTER ROOM in EE
	g.setColor(Color.black);
	//g.drawRect(235, 315, 120, 60); //drum printer room border
	g.drawRect(15, 495, 90, 180); // dungeon printer room
	g.drawRect(735, 15, 60, 360); // mech eng priter room

	//DRAW COUCH OUTSIDE BUGLE/PIPE
	//g.setColor(Color.black);
	//g.fillRect(785, 405, 20, 90);
	//g.fillOval(780, 405, 11, 90);

	//DRAW IN DOORS
	g.setColor(Color.black);
	//-->Elec Eng Undercroft<--\\
	//if (labs[HARP].status != 'z') g.fillRect(55, 13, 30, 5);   //harp->external	
	//if (labs[HARP].status != 'z' &&
	//    labs[DRUM].status != 'z') g.fillRect(233, 225, 5, 30); //harp->drum
	//if (labs[TUBA].status != 'z') g.fillRect(415, 13, 30, 5);  //tuba->external	
	//if (labs[TUBA].status != 'z' &&
	//    labs[DRUM].status != 'z') g.fillRect(415, 193, 30, 5); //tuba->drum
	//g.fillRect(355, 373, 60, 5); //drum->external (dbl doors)
	//g.fillRect(353, 315, 5, 30); //drum->printer_room
	//g.fillRect(160, 373, 50, 5); //oboe->external
	//g.fillRect(233, 315, 5, 30); //printer_room->oboe
	//g.fillRect(113, 225, 5, 30); //oboe->harp

	//-->OMB Undercroft<--\\
	g.fillRect(13, 585, 5, 60); //lobby->external
	if (labs[PIAN].status != 'z') g.fillRect(45, 493, 30, 5); //piano->lobby
	if (labs[ORGA].status != 'z') g.fillRect(103, 525, 5, 30); //organ->lobby
	if (labs[CLAV].status != 'z') g.fillRect(103, 615, 5, 30); //clavier->lobby

	//-->MechEng Undercroft<--\\
	g.fillRect(793, 105, 5, 30); //hall->moog
	g.fillRect(793, 255, 5, 30); //hall->leaf
	g.fillRect(973, 105, 5, 30); //moog->bell
	g.fillRect(973, 255, 5, 30); //leaf->spoons
	g.fillRect(1095, 13, 30, 5);  //bell->external
	g.fillRect(1065, 373, 30, 5); //spoons->external
	g.fillRect(733, 165, 5, 30); //bongo->hall

	//-->Help Desk Door<--\\
	g.fillRect(733, 255, 5, 60);

	//-->MechEng Eatery<--\\
	g.fillRect(583 + 240, 525, 5, 30);
	g.fillRect(555 + 240, 583, 30, 5);

	//-->Oud Door<--\\
	if (labs[OUDX].status != 'z') g.fillRect(525, 703, 30, 5);

	//-->Banjo Door<--\\
	if (labs[BANJ].status != 'z') g.fillRect(700, 703, 30, 5);

	//DRAW IN LAB NAMES
	g.setColor(Color.white);
	for (int lll = 0; lll < NUMBER_OF_LABS; lll++) {
	    if (labs[lll].status == 'z') continue;
	    g.drawString(labs[lll].name + "[" + labs[lll].therm + " C]", 
			 labs[lll].x + labs[lll].name_x, 
			 labs[lll].y + labs[lll].name_y);
	    g.drawOval(labs[lll].x + labs[lll].name_x + 
		       g.getFontMetrics().stringWidth(labs[lll].name + "[" + labs[lll].therm),
		       labs[lll].y + labs[lll].name_y - 12, 4, 4);
	    paintZoomIcon(labs[lll].x + labs[lll].name_x - 9, labs[lll].y + labs[lll].name_y - 5, g);
	}


	/*
       	g.drawString("HARP[" + labs[HARP].therm + " C]", 120, 85);
	g.drawString("TUBA[" + labs[TUBA].therm + " C]", 320, 130);
	g.drawString("DRUM[" + labs[DRUM].therm + " C]", 320, 270);
	g.drawString("OBOE[" + labs[OBOE].therm + " C]", 110, 330);
	//g.drawString("BONGO", 630, 110);
	g.drawString("MOOG[" + labs[MOOG].therm + " C]", 845, 130);
	g.drawString("BELL[" + labs[BELL].therm + " C]", 1025, 130);
	g.drawString("LEAF[" + labs[LEAF].therm + " C]", 845, 280);
	g.drawString("SPOONS[" + labs[SPOO].therm + " C]", 985, 280);
	g.drawString("CLAVIER[" + labs[CLAV].therm + " C]", 110, 670);
	g.drawString("PIANO[" + labs[PIAN].therm + " C]", 30, 485);
	g.drawString("ORGAN[" + labs[ORGA].therm + " C]", 110, 580);
	g.drawString("BUGLE[" + labs[BUGL].therm + " C]", 640 + 240, 550);
	g.drawString("PIPE[" + labs[PIPE].therm + " C]", 640 + 240, 730);
	*/

    }

    /**
       Paints all the printers (object, name, and status).
    */
    public void paintPrinters(Graphics g) {

	g.setColor(cPrinter);
	
	for (int i = 0; i < NUMBER_OF_PRINTERS; ++i) {
	    if (printers[i].visible == false) continue;
	    
	    g.fillRect(printers[i].x, printers[i].y, 20, 20);
	}

	//DRAW IN PRINTER NAMES
	g.setColor(Color.black);
	//	g.drawString("itch", 300, 350); //drum
	g.drawString("shooter", 480, 200); //<del>drum</del> sanhu
	g.drawString("kaboo", 740, 180); //moog
	g.drawString("lump", 740, 210); //moog
	g.drawString("light", 30, 660); //clavier
	g.drawString("ouch", 70, 650); //clavier

	g.setColor(Color.white);
	g.drawString("luck", 590 + 240, 535); //bugle
	g.drawString("ants", 585 + 240, 635); //pipe
	g.drawString("rude", 407, 720); // oud
	g.drawString("roof", 589, 720); // banjo

	if (selected_printer > -1) {
	    if (printers[selected_printer].details.startsWith("FRE")) g.setColor(Color.green);
	    else if (printers[selected_printer].details.startsWith("STA")) g.setColor(new Color(255,50,50));
	    else g.setColor(Color.yellow);
	    g.fillRoundRect(printers[selected_printer].x - 10, 
		       printers[selected_printer].y + 20, 
		       g.getFontMetrics().stringWidth(printers[selected_printer].details) + 6, 
		       15, 5, 5);
	    g.setColor(Color.black);
	    g.drawString(printers[selected_printer].details, printers[selected_printer].x - 7, printers[selected_printer].y + 32);
	}

    }

    /**
       Given a string removes shell escape characters.
    */
    public String removeEscapeCharCrap(String name) {
	
	String clean_str = "";
	boolean finding_m = false;

	for (int i = 0; i < name.length() - 2; i++) {
	    if (finding_m) {
		if (name.charAt(i) == 'm') finding_m = false;
		continue;
	    }
	    if (name.substring(i, i+2).equals("\\e")) {
		finding_m = true;
	    } else {
		clean_str = clean_str + name.charAt(i);
	    }
	}

	return clean_str;
    }

    public void rescanLabs() {
	try {
	    
	    String params[] = {"./rescanlabs.sh"};
	    Process pinger = Runtime.getRuntime().exec(params);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pinger.getInputStream()));
	    String buffer;
	    
	    while ((buffer = in.readLine()) != null) {}
	    
	    loadGfredFile();
	    repaint();
	} catch (IOException ex) {
	    status.setText("Syscall to rescan labs failed...");
	}	
    }

    /**
       Action listener for the buttons.
    */
    public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(closer)) {
	    System.exit(0);
	}
	if (e.getSource().equals(helper)) {
	    try {
		String params[] = {"firefox", "http://www.cse.unsw.edu.au/~stevec/gfriends.html"};
		Process pinger = Runtime.getRuntime().exec(params);
	    } catch (IOException ex) {
		try {
		    String params[] = {"/home/csesoc/bin/firefox", "http://www.cse.unsw.edu.au/~stevec/gfriends.html"};
		    Process pinger = Runtime.getRuntime().exec(params);
		} catch (IOException ex2) {
		    status.setText("Could not launch firefox to show help file");
		}
	    }
	}
	if (e.getSource().equals(refresher)) {
	    
	    this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

	    status.setText("Scanning Labs...");
	    Button b = (Button)e.getSource();
	    b.setLabel("Scanning...");
	    rescanLabs();

	    b.setLabel("Rescan Labs");
	    status.setText("Labs Scanned");

	    this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	if (e.getSource().equals(searcher)) {
	    //System.out.println("Performing search on '" + search_text.getText());

	    ArrayList<CSEMachine> found = findPeople(search_text.getText());
	    
	    if (found.size() == 0) System.out.println("[none]");
	    
	    for (CSEMachine m : found) {
		m.highlighted = true;
		//System.out.println(m.username + "(" + m.x + "," + m.y + ")" + m.user_info);
	    }
	    repaint();
	}
    }

    /**
       mouse clicked.
    */
    public void mouseClicked(MouseEvent e) {
	
	if (zoomed_lab != null) {
	    // if (clicked the zoomed lab's close button) ...
	    if (inRange(zoomed_lab.zoomed_close_button_coords[X], e.getX(), zoomed_lab.zoomed_close_button_coords[X] + 16) &&
		inRange(zoomed_lab.zoomed_close_button_coords[Y], e.getY(), zoomed_lab.zoomed_close_button_coords[Y] + 16)) {
		// close the zoomed in lab window
		zoomed_lab = null;
		repaint();
		return;
	    }
	    
	    // if (clicked the zoomed lab's "tutor options" button) ...
	    if (inRange(zoomed_lab.zoomed_options_button_coords[X], e.getX(), zoomed_lab.zoomed_options_button_coords[X] + 85) &&
		inRange(zoomed_lab.zoomed_options_button_coords[Y], e.getY(), zoomed_lab.zoomed_options_button_coords[Y] + 16)) {
		
		gfred_tutor_options_menu t_o_menu = new gfred_tutor_options_menu(e.getX(), e.getY(), zoomed_lab.name, this);
		return;
	    }
	}

	if (ad_banner.mouseClicked(e.getX(), e.getY())) return;

	if (e.getButton() == e.BUTTON1) {
	    if (inRange(920, e.getX(), 920+80) && inRange(800, e.getY(), 820)) {
		// run: ssh wagner who
		try {
		    String params[] = {"./showxterm_who.pl","wagner"};
		    Process pinger = Runtime.getRuntime().exec(params);
		} catch (IOException ex) {
		    status.setText("Could not scan wagner...");
		}
	    }
	    
	    if (inRange(920, e.getX(), 920+80) && inRange(820, e.getY(), 840)) {
		// run: ssh weill who
		try {
		    String params[] = {"./showxterm_who.pl","weill"};
		    Process pinger = Runtime.getRuntime().exec(params);
		} catch (IOException ex) {
		    status.setText("Could not scan weill...");
		}
	    }

	    if (inRange(920, e.getX(), 920+80) && inRange(840, e.getY(), 860)) {
		// run: ssh wagner williams
		try {
		    String params[] = {"./showxterm_who.pl","williams"};
		    Process pinger = Runtime.getRuntime().exec(params);
		} catch (IOException ex) {
		    status.setText("Could not scan williams...");
		}
	    }
	    
	    if (inRange(1119, e.getX(), 1170) && inRange(670, e.getY(), 681)) {
		paint_random_people_logins = !paint_random_people_logins;		
	    }
	    	    
	    String current_user_and_host = findSelectedUser(e.getX(), e.getY());

	    //System.out.println("DEBUG:" + current_user_and_host);
	    
	    String current_user;
	    try {
		current_user = current_user_and_host.split("@")[0];
	    } catch (ArrayIndexOutOfBoundsException ex) {
		current_user = "";
	    }

	    String current_host;
	    try {
		current_host = current_user_and_host.split("@")[1];
	    } catch (ArrayIndexOutOfBoundsException ex) {
		current_host = "";
	    }


	    if (!current_user.equals("")) {
		if (current_user.startsWith("605")) {
		    // clicked on the zoom lab thing
		    info_window.name = null;

		    //System.out.println("About to zoom lab " + current_user.substring(3).toLowerCase());

		    zoomed_lab = labs[mapLabStringToInt(current_user.substring(3).toLowerCase())];
		} else if (current_user.startsWith("506")) {
		    
		    info_window.visible = false;
		    repaint();

		    info_window.name = current_user;
		    info_window.classes = getLabInfo(current_user.substring(3).toLowerCase());
		    
		    info_window.x = e.getX();
		    info_window.y = e.getY();
		    info_window.visible = true;
		    
		} else {

		    info_window.visible = false;
		    repaint();

		    String info = getUserInfo(current_user);
		    String []info_tokens = info.split(":");
		    
		    try {
			info_window.name = info_tokens[0];
		    } catch (ArrayIndexOutOfBoundsException ex) {
			//System.out.println("error: current_user=" + current_user + ",info=" + info);
			//System.out.println("     : raw=" + current_user_and_host);
			info_window.name = "Unnamed";
		    }

		    try {
			info_window.classes = info_tokens[1];
		    } catch (ArrayIndexOutOfBoundsException ex) {
			info_window.classes = "";
		    }
		    
		    try {
			info_window.teachings = info_tokens[2];
		    } catch (ArrayIndexOutOfBoundsException ex) {
			info_window.teachings = "";
		    }
		}
		
		info_window.x = e.getX();
		info_window.y = e.getY();	
		
		info_window.visible = true;
	    } else {
		
		for (int pr = 0; pr < NUMBER_OF_PRINTERS; pr++) {
		    if (printers[pr].x < e.getX() && e.getX() < printers[pr].x + 20 &&
			printers[pr].y < e.getY() && e.getY() < printers[pr].y + 20) {
			// printers have .name
			try {
			    String params[] = {"./showxterm_lpq.pl", "-P"+printers[pr].name};
			    Process pinger = Runtime.getRuntime().exec(params);
			} catch (IOException ex) {
			    status.setText("Could not show printer list");
			}
		    }
		}
	    }

	    repaint();
	} else { // for right click
	    String current_user_and_host = findSelectedUser(e.getX(), e.getY());
	    
	    String current_user;
	    try {
		current_user = current_user_and_host.split("@")[0];
	    } catch (ArrayIndexOutOfBoundsException ex) {
		current_user = "";
	    }
	    
	    String current_host;
	    try {
		current_host = current_user_and_host.split("@")[1];
	    } catch (ArrayIndexOutOfBoundsException ex) {
		current_host = "";
	    }


	    if (!current_user.equals("")) {
		if (current_user.startsWith("506")) {
		    
		} else {
		    gfred_menu quick_menu = new gfred_menu(e.getX(), e.getY(), current_user, current_host);
		}
	    }
	}
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {
	
	if (info_window.visible) {
	    if (inRange(info_window.x - 20, e.getX(), info_window.x + 20) &&
		inRange(info_window.y - 20, e.getY(), info_window.y + 20)) {
		// do nothing anymore
	    } else {
		info_window.visible = false;
		repaint();
		return;
	    }
	}

	if (e.getX() == 652 && e.getY() == 384) {
	    status.setText("(" + e.getX() + "," + e.getY() + ") - the ET map's true secret is #0000ff disguised 8-bit ASCII");
	} else if (e.getX() == 308 && e.getY() == 394) {
	    status.setText("(" + e.getX() + "," + e.getY() + ") - ");
	} else {
	    status.setText("(" + e.getX() + "," + e.getY() + ") - bringing usefulness to CSE since Feb 2005" + " ... Fortune: " + this.current_fortune);
	}
	mouseX = e.getX();
	mouseY = e.getY();
	for (int pr = 0; pr < NUMBER_OF_PRINTERS; pr++) {
	    if (printers[pr].x < mouseX && mouseX < printers[pr].x + 20 &&
		printers[pr].y < mouseY && mouseY < printers[pr].y + 20) {
		
		if (selected_printer != pr) {
		    selected_printer = pr;
		    printers[pr].details = getPrinterInfo(printers[pr].name);
		}
		repaint();
		return;
	    }
	}
	
	if (selected_printer != -1) {
	    selected_printer = -1;	
	}

	/*
	for (int i = 0; i < NUMBER_OF_LABS; i++) {
	    if (labs[i].mouseInLab(mouseX, mouseY)) {
		labs[i].alpha = 1.0f;
	    } else {
		labs[i].alpha = 0.9f;
	    }
	}
	*/
	
    }

    public static void main(String args[]) {
	AnimationThread anim_thread;
	
	gfred da_window;

	/*
	String args_switches = "";
	for (int i = 0; i < args.length; i++) {
	    if (args[i].startsWith("laptop")) args_switches += "l";
	    if (args[i].startsWith("sc")) args_switches += "s";
	    if (args[i].startsWith("v")) args_switches += "v";
	    if (args[i].startsWith("-u")) args_switches.add += "|" + args[i].subtring(2) + "|";
	}
	*/

	// from here down works
	da_window = new gfred(args);

	da_window.init();

	anim_thread = new AnimationThread(da_window);

	anim_thread.start();
    }

    public gfred() {
	
    }

    public gfred(String args[]) {
	
	for (int i = 0; i < args.length; i++) {
	    //	    System.out.println("DEBUG::args[" + i + "]=" + args[i]);
	    if (args[i].startsWith("laptop")) user_using_laptop = true;
	    if (args[i].startsWith("-u")) {
		user_username = args[i].substring(2);
	    }
	    if (args[i].startsWith("sc")) {
		try {
		    BufferedReader in = new BufferedReader(new FileReader("./scoffice"));
		    String buffer = "";
		    
		    if ((buffer = in.readLine()) != null) {
			if (buffer.startsWith("yes")) {
			    sc_in_office = true;
			}
		    }
		} catch (IOException ex) {
		    
		}
	    }
	}

	//	System.out.println("DEBUG::username=" + user_username);

    }

    /**
       Given a printer name returns the quick summary String (FREE, STALLED or n QUEUED JOBS)
    */
    public String getPrinterInfo(String printer) {
	String return_details = "+";

	try {
	    String params[] = {"lpq", "-P" + printer};
	    Process pinger = Runtime.getRuntime().exec(params);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pinger.getInputStream()));
	    String buffer = "";
	    
	    while ((buffer = in.readLine()) != null) {
		if (buffer.startsWith("stalled") || buffer.startsWith(" Stalled")) {
		    return "STALLED";
		}
		if (buffer.startsWith(" Queue:")) {
		    StringTokenizer st = new StringTokenizer(buffer);
		    st.nextToken();
		    String queue_len_str = st.nextToken();
		    if (queue_len_str.equals("no")) {
			return "FREE";
		    } else {
			return_details =  Integer.parseInt(queue_len_str) + " QUEUED JOBS";
		    }		    
		}		    
	    }
	} catch (Exception ex) {
	    System.out.println("Could not stat printer " + printer);
	    return "-";
	}
	
	return return_details;
    }

    /**
       Gets the busy times for a given lab.
    */
    public String getLabInfo(String labname) {

	String busy_times = "";

	try {
	    String params[] = {"./lab_outlook.pl",labname};
	    Process pinger = Runtime.getRuntime().exec(params);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pinger.getInputStream()));
	    String buffer;
	    
	    while ((buffer = in.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(buffer, " ");
		try {
		    busy_times += st.nextToken() + "-" + st.nextToken() + " [" + st.nextToken() + "],";
		} catch (NoSuchElementException exex) {
		    continue;
		}
	    }	       
	} catch (Exception ex) {
	    //ex.printStackTrace();
	    return "...";
	}
	return busy_times;
	
    }

    /**
       Given a username, returns "Name:classes:tutorings"
       
       eg "Stephen Cossell" is doing {MATH3411,COMP9444} and tutoring {COMP1921}
       "Stephen Cossell: MATH3411, COMP9444: COMP1921"
    */
    public String getUserInfo(String login) {
	
	String name = "", classes = "", teachings = "";

	if (login.equals("stevec") || login.equals("scossell")) return "Steve: MTRNPhD, GPGPU, ~stevec/qw: ENGG1811, COMP2911";
	

	try {
	    String params[] = {"pp", login, "|", "grep", "Name"};
	    Process pinger = Runtime.getRuntime().exec(params);
	    BufferedReader in = new BufferedReader(new InputStreamReader(pinger.getInputStream()));	    
	    String buffer = "";
	    
	    while ((buffer = in.readLine()) != null) {
		
		if (buffer.equals("")) continue;
		StringTokenizer st = new StringTokenizer(buffer, " :");
		String key = st.nextToken();
		if (key.equals("Name")) {
		    while (st.hasMoreTokens()) {
			    name += st.nextToken() + " ";
		    }
		}
	    }
	    in.close();

	    String params2[] = {"./classes", login};
	    Process pinger2 = Runtime.getRuntime().exec(params2);
	    in = new BufferedReader(new InputStreamReader(pinger2.getInputStream()));
	    
	    while ((buffer = in.readLine()) != null) {
		classes += buffer + ",";
	    }
	    in.close();

	    String params3[] = {"./teachings", login};
	    Process pinger3 = Runtime.getRuntime().exec(params3);
	    in = new BufferedReader(new InputStreamReader(pinger3.getInputStream()));
	    
	    while ((buffer = in.readLine()) != null) {
		teachings += buffer + ",";
	    }
	    in.close();

	} catch (Exception ex) {
	    //System.out.println("-----DEBUG-----");
	    //ex.printStackTrace();
	    //System.out.println("-----DEBUG-----");
	    return "...";
	}
	return name + ":" + classes + ":" + teachings;
    }

    /**
       Returns true is left <= mid <= right
    */
    public boolean inRange(int left, int mid, int right) {
	if (left <= mid && mid <= right) return true;
	return false;
    }

    // str, int . int  gfx, col
    public void drawIcon(String game, int x, int y, Graphics g, Color bgcolor) {
	BufferedImage img = null;
	try {
	    if (game.equals("q3")) img = ImageIO.read(new File("q3icon.gif"));
	    if (game.equals("et")) img = ImageIO.read(new File("eticon.png"));
	    if (game.equals("trem")) img = ImageIO.read(new File("tricon.png"));
	} catch (IOException ex) {
	    return;
	}
	if (img != null) g.drawImage(img, x, y, 16, 16, bgcolor, null);
    }

    /**
       returns (if exists) "username@hostname", or if its a lab name "506labname"
    */
    public String findSelectedUser(int x, int y) {
	
	int tol = 10; // tolerance

	for (int i = 0; i < NUMBER_OF_LABS; i++) {
	    if (labs[i].status == 'z') continue;
	    if (labs[i].visible == false) continue;
	    if (inRange(labs[i].x + labs[i].name_x, x, labs[i].x + labs[i].name_x + 48) &&
		inRange(labs[i].y + labs[i].name_y - 12, y, labs[i].y + labs[i].name_y)) 
		return "506" + labs[i].name;
	    if (inRange(labs[i].x + labs[i].name_x - 15, x, labs[i].x + labs[i].name_x) &&
		inRange(labs[i].y + labs[i].name_y - 12, y, labs[i].y + labs[i].name_y)) 
		return "605" + labs[i].name;
	}             

	int lab_in_vicinity_of_mouse = -1;
	for (int i = 0; i < NUMBER_OF_LABS; i++) {
	    if (labs[i].mouseInLab(x, y)) {
		lab_in_vicinity_of_mouse = i;
		break;
	    }
	}

	if (lab_in_vicinity_of_mouse == -1) {

	} else {
	    for (int i = 0; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
		if (Math.abs(labs[lab_in_vicinity_of_mouse].x + machines[lab_in_vicinity_of_mouse][i].x - x) < tol &&
		    Math.abs(labs[lab_in_vicinity_of_mouse].y + machines[lab_in_vicinity_of_mouse][i].y - y) < tol) {
		    return machines[lab_in_vicinity_of_mouse][i].username + "@" + mapLabIntToString(lab_in_vicinity_of_mouse) + (i<10?"0"+i:""+i);	    
		}
	    }       
	}

	return "";
    }

    public void keyPressed(KeyEvent e) {
	if (e.getKeyCode() == e.VK_BACK_SPACE) {
	    keychain = "";
	    for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
		for (int nn = 0; nn < MAX_NUMBER_OF_MACHINES_IN_LAB; nn++) {
		    machines[ll][nn].highlighted = false;
		}
	    }
	    repaint();
	    return;
	} else if (Character.isLetter(e.getKeyChar()) ||
		   Character.isDigit(e.getKeyChar())) {
	    keychain += e.getKeyChar();
	}
	
	//	System.out.println("keychain='" + keychain + "'");

	if (keychain.equals("")){ 

	} else {
	    if (keychain.charAt(0) == 'f') {
		for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
		    for (int nn = 0; nn < MAX_NUMBER_OF_MACHINES_IN_LAB; nn++) {
			if (machines[ll][nn].user_type == 'f') {
			    machines[ll][nn].highlighted = true;
			}
		    }
		}
		repaint();
		keychain = "";
	    }	
	}
    }
    
    public void keyReleased(KeyEvent e) {
	
    }
    
    public void keyTyped(KeyEvent e) {
	
    }

    public void dumpLabToFriendsList(String lab_) {
	int lab_i = mapLabStringToInt(lab_);

	//	System.out.println("DEBUG:: lab = " + lab_ + "(" + lab_i + ")");

	try {
	    PrintWriter out = new PrintWriter(new FileWriter("/home/" + user_username + "/.friends", true));
	    
	    out.println("");
	    out.println("group:Students:200,255,0");
	    out.println("");
	    
	    for (int i = 0; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
		//System.out.println("DEBUG:: " + i + "(" + machines[lab_i][i].status + ")");
		if (machines[lab_i][i].orient == 4) continue;      // if the machine doesn't exist
		if (machines[lab_i][i].status != 'o') continue;    // if the machine isn't 'o'ccupied
		if (machines[lab_i][i].username.equals(user_username)) continue; // if it's me
		
		String tmp_username = machines[lab_i][i].username;
		String tmp_name = machines[lab_i][i].user_info.split(":")[0];

		//System.out.println("DEBUG: Adding record (" + tmp_username + ":" + tmp_name + ")");

		out.println("name = " + tmp_username + ", " + tmp_name);
	    }
	    out.close();
	} catch (Exception ex) {
	    System.out.println("Exception: can't append to /home/" + user_username + "/.friends list");
	}       
	
    }
    
    public void genPDFCheatSheet(String lab_) {
	int lab_i = mapLabStringToInt(lab_);
	
	try {
	    PrintWriter out = new PrintWriter(new FileWriter("/tmp/cheatsheet.tex"));
	    
	    out.println("\\documentclass[12pt]{article}");
	    out.println("\\parindent 0mm");
	    out.println("\\oddsidemargin 0mm");
	    out.println("\\evensidemargin 0mm");

	    out.println("\\begin{document}");
	    
	    out.println("\\begin{tabular}{|c|c|c|c|}");
	    out.println("\\hline");
	    
	    out.println("\\textbf{Machine} & \\textbf{Login} & \\textbf{Name} & \\textbf{.friends Entry} \\\\");
	    out.println("\\hline");
	    
	    for (int i = 0; i < MAX_NUMBER_OF_MACHINES_IN_LAB; i++) {
		if (machines[lab_i][i].orient == 4) continue;

		String tmp_machine = mapLabIntToString(lab_i) + " " + (i < 10 ? "0" : "") + i;
		String tmp_login = machines[lab_i][i].username;
		String tmp_name = machines[lab_i][i].user_info.split(":")[0];
		String tmp_nick = machines[lab_i][i].nickname;

		tmp_name = tmp_name.replaceAll("\\^.", "");
		tmp_name = tmp_name.replaceAll("\\x26", "and");
		
		tmp_nick = tmp_nick.replaceAll("\\^.", "");
		tmp_nick = tmp_nick.replaceAll("\\x26", "and");

		out.println("\\texttt{" + tmp_machine + "} & \\texttt{" + tmp_login + "} & " + tmp_name + " & " + tmp_nick + " \\\\");
		
		out.println("\\hline");
	    }
	    
	    out.println("\\end{tabular}");	    
	    out.println("\\end{document}");
	    
	    out.close();

	    // finished writing the tex file... now making the PDF
	    String params[] = {"./compile_cheatsheet.sh"};
	    Process pinger = Runtime.getRuntime().exec(params);	    

	} catch (Exception ex) {
	    System.out.println("Exception: can't make the cheat sheet");
	}
	
    }

    int getFriendliestLab() {
	
	int friendliest_lab = -1;
	int friendliest_val = 0;

	for (int l = 0; l < NUMBER_OF_LABS; l++) {
	    int cur_friends_count = 0;
	    
	    for (int m = 0; m < MAX_NUMBER_OF_MACHINES_IN_LAB; m++) {
		if (machines[l][m].orient == 4) continue; // if the machine doesn't exist then ignore it
		
		if (machines[l][m].user_type == 'f') {
		    cur_friends_count++;
		}
		
	    }
	    
	    if (cur_friends_count > friendliest_val) {
		friendliest_lab = l;
		friendliest_val = cur_friends_count;
	    }
	}
	
	return friendliest_lab;
	
    }

    // find the lab index with the most free spots
    int getEmptiestLab() {
	
	int emptiest_lab = -1;
	float emptiest_val = 1.0f;

	for (int l = 0; l < NUMBER_OF_LABS; l++) {
	    int cur_empty_count = 0;
	    int cur_machs_count = 0;
	    
	    for (int m = 0; m < MAX_NUMBER_OF_MACHINES_IN_LAB; m++) {
		if (machines[l][m].orient == 4) continue; // if the machine doesn't exist then ignore it
		
		cur_machs_count++;
		if (machines[l][m].status == 'a') {
		    cur_empty_count++;
		}
	    }
		    
	    if (cur_machs_count == 0) continue;
	    float tmp_full_ratio = 1.0f - cur_empty_count / (float)cur_machs_count;

	    if (tmp_full_ratio < emptiest_val) {
		emptiest_lab = l;
		emptiest_val = tmp_full_ratio;;
	    }
	    
	}

	return emptiest_lab;	
    }

    // find the lab with the most n00bs
    int getNoobestLab() {
	
	int noobest_lab = -1;
	int noobest_val = 0;
	
	for (int l = 0; l < NUMBER_OF_LABS; l++) {
	    
	    int cur_noob_count = 0;

	    for (int m = 0; m < MAX_NUMBER_OF_MACHINES_IN_LAB; m++) {		
		if (machines[l][m].orient == 4) continue;

		if (machines[l][m].username.equals("stevec")) {
		    cur_noob_count = -MAX_NUMBER_OF_MACHINES_IN_LAB;
		} 
		
		String tmp_username = machines[l][m].username;
		if (tmp_username.length() > 0) {
		    if (Character.isDigit(tmp_username.charAt(tmp_username.length() - 1))) {
			cur_noob_count++;
		    }
		}
		
	    }
	    
	    if (cur_noob_count > noobest_val) {
		noobest_lab = l;
		noobest_val = cur_noob_count;
	    }
	    
	}

	return noobest_lab;	
    }

    // get the lab with the most cool people
    int getLeetestLab() {
	
	int leetest_lab = -1;
	int leetest_val = 0;
	
	for (int l = 0; l < NUMBER_OF_LABS; l++) {
	    
	    int cur_leet_count = 0;
	    
	    for (int m = 0; m < MAX_NUMBER_OF_MACHINES_IN_LAB; m++) {
		if (machines[l][m].orient == 4) continue;
		
		if (machines[l][m].username.equals("stevec")) {
		    cur_leet_count += MAX_NUMBER_OF_MACHINES_IN_LAB;
		}

		String tmp_username = machines[l][m].username;
		if (tmp_username.length() > 0) {
		    if (Character.isDigit(tmp_username.charAt(tmp_username.length() - 1))) {
			
		    } else {
			cur_leet_count++;
		    }
		}
		
	    }
	    
	    if (cur_leet_count > leetest_val) {
		leetest_lab = l;
		leetest_val = cur_leet_count;
	    }
	}

	return leetest_lab;
    }

    public ArrayList<CSEMachine> findPeople(String search) {       

	ArrayList<CSEMachine> found = new ArrayList<CSEMachine>();

	if (search.equals("") || search.equals(".*")) {
	    unhighlightEveryone();
	    return found;
	}

	search = ".*" + search + ".*";

	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    for (int mm = 0; mm < MAX_NUMBER_OF_MACHINES_IN_LAB; mm++) {
		CSEMachine m = machines[ll][mm];
		m.highlighted = false;

		if (m.status == 'z') continue;
		
		if (m.username.matches(search)) {
		    found.add(m);
		    continue;
		} else if (m.nickname.matches(search)) {
		    found.add(m);
		    continue;
		} else if (m.user_info.matches(search)) {
		    found.add(m);
		    continue;
		}		  		
		
	    }
	}			
	
	return found;	
    }

    public void unhighlightEveryone() {
	for (int ll = 0; ll < NUMBER_OF_LABS; ll++) {
	    for (int mm = 0; mm < MAX_NUMBER_OF_MACHINES_IN_LAB; mm++) {
		machines[ll][mm].highlighted = false;
	    }
	}
    }

}

class AnimationThread extends Thread {
    gfred parent;
    
    public AnimationThread(gfred parent_) {
	super();
	parent = parent_;
    }

    public void run() {
	while (true) {
	    parent.repaint(); //commented out cos it only enables mouse movement
	    try {
		sleep(20000);
	    } catch (InterruptedException ex) {
		System.out.print("Interrupted Exception in Animation Thread");
		System.exit(0);
	    }
	}
    }
}

