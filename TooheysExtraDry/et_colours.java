import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.*;

public class et_colours {

    ArrayList<String> cols = new ArrayList<String>();

    public et_colours() {
	try {
	    BufferedReader in = new BufferedReader(new FileReader("et_colours.map"));
	    String buffer;
	    while ((buffer = in.readLine()) != null) {
		cols.add(buffer);
	    }
	} catch (IOException ex) {
	    
	}
    }
    
    public int getIndex(char ch) {
	for (int i = 0; i < cols.size(); ++i) {
	    if (cols.get(i).charAt(0) == ch) return i;
	}
	return 6; // cos the 6th element will give white
    }

    public int getRed(char ch) {
	int index = getIndex(ch);
	StringTokenizer st = new StringTokenizer(cols.get(index), " ,");
	st.nextToken();
	return Integer.parseInt(st.nextToken());
    }

    public int getGreen(char ch) {
	int index = getIndex(ch);
	StringTokenizer st = new StringTokenizer(cols.get(index), " ,");
	st.nextToken();
	st.nextToken();
	return Integer.parseInt(st.nextToken());
    }

    public int getBlue(char ch) {
	int index = getIndex(ch);
	StringTokenizer st = new StringTokenizer(cols.get(index), " ,");
	st.nextToken();
	st.nextToken();
	st.nextToken();
	return Integer.parseInt(st.nextToken());
    }

    public Color getColor(char ch) {
	return new Color(getRed(ch), getGreen(ch), getBlue(ch));	
    }

    public String getHash(char ch) {
	int index = getIndex(ch);
	StringTokenizer st = new StringTokenizer(cols.get(index));
	st.nextToken();
	st.nextToken();
	return st.nextToken();
    }

}
