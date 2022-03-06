package subRipper;

//
//A simple Java Console for your application (Swing version)
//Requires Java 1.1.5 or higher
//
//Disclaimer the use of this source is at your own risk. 
//
//Permision to use and distribute into your own applications
//
//RJHM van den Bergh , rvdb@comweb.nl

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.TextArea;
import java.awt.event.*;
import javax.swing.*;

public class Console extends WindowAdapter implements WindowListener, ActionListener, Runnable
{
	private JFrame frame;
	//private JTextArea textArea;
	private TextArea textArea;
	private JScrollPane scrollPane;
	private Thread reader;
	private Thread reader2;
	private boolean quit;
	private Console console;
					
	private final PipedInputStream pin=new PipedInputStream(); 
	private final PipedInputStream pin2=new PipedInputStream(); 

	Thread errorThrower; // just for testing (Throws an Exception at this Console
	
	public Console()
	{				
		
		console=this;
	
		// create all components and add them        
        frame=new JFrame("Java Console");
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
		int x=(int)(frameSize.width/2);
		int y=(int)(frameSize.height/2);
		frame.setBounds(x,y,frameSize.width,frameSize.height);		
		
		//textArea=new JTextArea();
		textArea=new TextArea("", 3 , 100 , TextArea.SCROLLBARS_VERTICAL_ONLY);
		textArea.setEditable(false);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
		JButton button=new JButton("clear");
		
		//scrollPane = new JScrollPane(textArea);
		
		frame.getContentPane().setLayout(new BorderLayout());
		//frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
		//frame.getContentPane().add(scrollPane,BorderLayout.CENTER);
		frame.getContentPane().add(textArea,BorderLayout.CENTER);
		frame.getContentPane().add(button,BorderLayout.SOUTH);
		frame.setVisible(true);		
		
		frame.addWindowListener(this);		
		button.addActionListener(this);		
		
		try
		{
			PipedOutputStream pout=new PipedOutputStream(this.pin);
			System.setOut(new PrintStream(pout,true, "UTF-8")); 
		} 
		catch (java.io.IOException io)
		{
			printToConsole("Couldn't redirect STDOUT to this console\n"+io.getMessage());			
		}
		catch (SecurityException se)
		{
			printToConsole("Couldn't redirect STDOUT to this console\n"+se.getMessage());
	    } 
		
		try 
		{
			PipedOutputStream pout2=new PipedOutputStream(this.pin2);
			System.setErr(new PrintStream(pout2,true));
		} 
		catch (java.io.IOException io)
		{
			printToConsole("Couldn't redirect STDERR to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			printToConsole("Couldn't redirect STDERR to this console\n"+se.getMessage());
	    } 		
			
		quit=false; // signals the Threads that they should exit
		
		// Starting two seperate threads to read from the PipedInputStreams				
		//
		reader=new Thread(this);
		reader.setDaemon(true);	
		reader.start();	
		//
		reader2=new Thread(this);	
		reader2.setDaemon(true);	
		reader2.start();
				
		SubRipper.isConsoleOn=true;
		
		/*
		// testing part
		// you may omit this part for your application
		//
		System.out.println("Hello World 2");
		System.out.println("All fonts available to Graphic2D:\n");
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames=ge.getAvailableFontFamilyNames();
		for(int n=0;n<fontNames.length;n++)  System.out.println(fontNames[n]);		
		// Testing part: simple an error thrown anywhere in this JVM will be printed on the Console
		// We do it with a seperate Thread becasue we don't wan't to break a Thread used by the Console.
		System.out.println("\nLets throw an error on this console");	
		errorThrower=new Thread(this);
		errorThrower.setDaemon(true);
		errorThrower.start();
		*/					
		//printToConsole(x+","+y+","+frameSize.width+","+frameSize.height+"\n");
	}
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		this.notifyAll(); // stop all threads
		try { reader.join(1000);pin.close();   } catch (Exception e){}		
		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
		//System.exit(0);
		SubRipper.isConsoleOn=false;
		
		try {
		    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		    throw new InternalError("VM does not support mandatory encoding UTF-8");
		}
	}		
		
	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behaviour of JFrame	
		frame.dispose();
	}
	
	public synchronized void actionPerformed(ActionEvent evt)
	{
		textArea.setText("");
	}

	public synchronized void run()
	{
  	  		try
  			{			
  				while (Thread.currentThread()==reader)
  				{
  					try { this.wait(100);}catch(InterruptedException ie) {}
  					if (pin.available()!=0)
  					{
  						String input=console.readLine(pin);
  						printToConsole(input);
  					}
  					if (quit) return;
  				}
  			
  				while (Thread.currentThread()==reader2)
  				{
  					try { this.wait(100);}catch(InterruptedException ie) {}
  					if (pin2.available()!=0)
  					{
  						String input=console.readLine(pin2);
  						printToConsole(input);
  					}
  					if (quit) return;
  				}			
  			} catch (Exception e)
  			{				
  				printToConsole("\nConsole reports an Internal error.\nThe error is: "+e);
  			}
  			
  			// just for testing (Throw a Nullpointer after 1 second)
  			if (Thread.currentThread()==errorThrower)
  			{
  				try { this.wait(1000); }catch(InterruptedException ie){}
  				throw new NullPointerException("Application test: throwing an NullPointerException It should arrive at the console");
  			}
  			
  	}

	private synchronized void printToConsole(String txt) {
				
        SwingWorker sw1 = new SwingWorker()  
        {        	
			@Override
			protected Object doInBackground() throws Exception {
				
				String tmp=txt;
				
				// TODO Auto-generated method stub
				int len;
				//int maxLen=Integer.MAX_VALUE-1000;
				int maxLen=10000;
				len=textArea.getText().length()+tmp.length();
				
				if (tmp.length()>maxLen) {
					textArea.setText("");
					tmp=tmp.substring(tmp.length()-maxLen);
				}
				else {					
					if(len>maxLen) {
						textArea.setText(textArea.getText().substring(len-maxLen));
					}
				}
			
				textArea.append(tmp);	
				//textArea.setCaretPosition(textArea.getDocument().getLength());
				//scrollPane.paintImmediately(scrollPane.getVisibleRect());
				//scrollPane.paintImmediately(textArea.getVisibleRect());
				//textArea.paint.paintImmediately(((JComponent) textArea).getVisibleRect());
				textArea.repaint();
				return null;
			} 
        	
        };
        
        sw1.execute();
	}
	
	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input="";
		do
		{
			int available=in.available();
			if (available==0) break;
			byte b[]=new byte[available];
			in.read(b);
			input=input+new String(b,0,b.length, "UTF-8");														
		}while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}	
		
	public static void main(String[] arg)
	{
		new Console(); // create console with not reference	
	}			
}