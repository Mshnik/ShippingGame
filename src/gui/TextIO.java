package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class TextIO {
	
	/** Write a string to a text file at the given directory.
	 * @param f - directory, a string
	 * @param s - text to write
	 * @throws IOException
	 */
	public static void write(String f, String s) throws IOException {
		write(new File(f), s);
	}

	/** Write a string to a text file at the given directory.
	 * @param f - directory, a file
	 * @param s - text to write
	 * @throws IOException
	 */
	public static void write(File f, String s) throws IOException {
		FileWriter fr= new FileWriter(f);
		BufferedWriter br= new BufferedWriter(fr);

		br.write(s);

		br.flush();
		br.close();
	}
	
	/** Reads File f as a text file.
	 * @param f - the File to read
	 * @return - a String of text
	 * @throws IOException - if the file reading goes bad.
	 */
	public static String read(File f) throws IOException {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String s = "";
		String line = null;
		do{
			line = br.readLine();
			if(line != null){
				s+=line;
			}
		}while(line != null);
		
		br.close();
		return s;
	}
	
	/** Reads File f as a text file.
	 * @param f - the File to read
	 * @return - a String[], each entry of which is a line of text in f
	 * @throws IOException - if the file reading goes bad.
	 */
	public static String[] readToArray(File f) throws IOException {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> s = new ArrayList<String>();
		
		while(br.ready()){
			s.add(br.readLine());
		}
		
		br.close();
		String[] sArray = new String[s.size()];
		sArray = s.toArray(sArray);
		return sArray;
	}
	
	/**
	 * Take in a string and divide it into an array list of strings, broken up by the tab markers
	 * @param line - the string to parse
	 * @return an array of strings. Each string should have no whitespace in it.
	 */
	public static String[] parseToArray(String line){

		ArrayList <String> asArrayList = new ArrayList <String> ();

		//If the first character in the line is the tab character, skip this line. (return an empty array)
		if( line != null ){

			int newBreak = -2;
			while ( newBreak != -1){

				line = line.trim();
				newBreak = line.indexOf('\t');

				if(newBreak != -1)	
				{
					asArrayList.add(line.substring(0, newBreak));
					line = line.substring(newBreak);
				}
				else
					asArrayList.add(line.substring(0,line.length()));
			}
		}

		String[] asArray = new String[asArrayList.size()];
		asArray = asArrayList.toArray(asArray);
		return asArray;
	}	
}
