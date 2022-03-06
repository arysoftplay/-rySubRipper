package subRipper;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class TextFileWriter {
	
	public static boolean createFile(String fileName) {

	    BufferedWriter writer;
		try {
			//writer = new BufferedWriter(new FileWriter(fileName));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
			writer.close();
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}								
	}
	
	public static boolean append(String fileName, String text) {

	    BufferedWriter writer;
		try {
			//writer = new BufferedWriter(new FileWriter(fileName, true));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), StandardCharsets.UTF_8));

			writer.append(text);
			writer.close();
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}								
	}

}
