package subRipper;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.media.ui.DefaultControlPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;
import java.beans.EventHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

//import org.omg.CORBA.LongHolder;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.player.base.SnapshotApi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Scanner;
import java.util.Timer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.ini4j.Wini;

public class SubRipper extends JFrame {
	static final String appVersion = "1.5";
	static final String appBuild = "20230408";
	static final String appCopyright = "© 2021-2023 @rySoft";
	static final String appNextVersion1 = "1.4";
	static final String appNextVersion2 = "2.0";
	
	//frame obj
	static JFrame Main;
	static JLayeredPane controlPane;
	static uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent playerCmpt;
	static JLabel lblStatus, lblStatus2, charBox;
	static JLabel pickColor;
	static JTextPane lblStatOCR;
	static BufferedImage bufImg, captImg; 
	static JLayeredPane imagePanel1;
	static ImagePanel image1;
	static JLabel[] captureBox;
	static JLabel[] captBox;
	static ImagePanel picker;
	static JLabel seekStatus;
	static JSlider vidSlid;
	static JTextArea boxX, boxY[], boxW, boxH;
	static JTextArea tolerance, toleranceG, toleranceB, goToPos, goToSub, minSpacePix, tolOCR, vFile, dbFileTxt, dbSubsTxt;
	static JTextArea subText, subStartPos, subEndPos, subRecId;	
	static JButton btnMute, btnReadDirection, btnDeleteCurSub;
	// pop
	static JLabel popl2, popStat;
	static JLabel popStat2, popStat3;
	static JDialog pop;
	static boolean popOn;	
	static JTextField popTA;	
	static String popReadChar, popAction;
	static JCheckBox stopAfter, stopAfterF;
	static JCheckBox chkInteractive, chkShowDel, chkIsDel;
	static int yPos=0;
	// paramPop
	static JDialog paramPop;
	static JTable paramTable;
	// charTolPop
	static JDialog charTolPop;
	static JTable charTolTable;
	// charStatsPop
	static JDialog charStatsPop;
	static JTable charStatsTable;
	// aboutPop
	static JDialog aboutPop;
	// cancelPop
	static JDialog cancelPop;
	//sqlitedb
	static Connection dbcn;
	static String dbSubs="";	
	static String dbFile="";	
	static String srtFile="";
	static String vidFile="";
	static String newFileType="";
	
	// global variables 
	static String lastReadBox[]={"0","","",""}; 
	static String actMode="Manual";
	static int playerStatus;	//0=not playing, 1=playing	
	public final static char CR  = (char) 0x0D;
	public final static char LF  = (char) 0x0A;
	static String logFile="run.log";
	static int curSubId=0, curSubStartPos=0, curSubEndPos=0;	
	static boolean stopAfterRead=false;
	static boolean isConsoleOn = false;
	static Console console;
	static String prevId="", prevSub="";
			
	// parameters
	public static int debugLevel =4; // 0 = no debug, 1 - errors, 3 - very important info, 4, important info, 
									 // 5 - process level, 6 - very basic debug, 7 - basic debug, 9 - additional debug, 10 - total debug	
	static int readDir=0;	//reading direction 0=L->R, 1=R->L
	static int step = 500;	//number of ms between two frames for OCR
	static int adjStep = 100;	// part of the step to try to adjust backwards when a frame changes after big step on SeekNextSub
	static int adjSub = 100;	// part of the step to adjust the subtitles position when running in auto-mode and no adjStep
	static int typeLastSub=0;	// last sub search type : 0 => last time, 1 => last rec_id
	static int step1=100, step5=1000;	// values for the player buttons bck/fwd slow and fast 
	static boolean generateOcrStats=false;
	static boolean writeLog=true;
	static double frameTolPct = .99;		// maximum frame difference tolerance in percentage (in combination with frameTolPix)
	static int maxSpacePix = 60;			// maximum espace between pixels (to detect real line contents stating from the middle)
	static int frameTolPix = 100;			// maximum frame difference tolerance in pixels (in combination with frameTolPct)
	static int minFramePix = 100;			// minimum number of pixels to consider a frame not empty
	static int seekAfterManual = 0;			// For SeekSub/Run Auto 0: doesn't come back to adjust position after step jump, 1: backs by adjStep until frame dif 
	static double charPropTol = .10;		// will show Character proposal in OCR pop up when difference between tolerance and match < charPropTol 
	static double vertPixMatchCoef = .010;	// coefficient of a vertical pixel difference in the matching result (i.e. if 0.01 => one vert pixel will reduce the match of 0.01)
	static String vlcPath = "C:\\Program Files\\VLC";
	static int bgMode = 0;					// run auto in background mode 0: off, 1: on
	static boolean interactiveMode=true;	// if true, run auto in mode "interactive/learning". If false unknown chars are marked with "errorChar" in db
	static String errorChar = "@";
	static int vlcTimeout = 2;				// number of seconds for vlc snapshot timeout
	static int vlcRetries = 2;				// number of retries in case of vlc snapshot timeout
	static boolean chkNewVersion = true;	// check if there a new @rySubripper version


	public static void main(String[] args) {
        new SubRipper();
    }
    
    public SubRipper() {			// MAIN START INITIALIZATION FUNCTION
    	
    	 logFile = SqliteDB.dbGetPath() + "\\" + logFile;
    	 writeToLog(logFile,"Starting @rySubRipper","N");
    	
    	 int statusBarY = 330;
    	
    	 /*
    	 this.addMouseListener(new MouseAdapter() { 
    		     public void mousePressed(MouseEvent me) { 
    		      //System.out.println(me); 
    		     } 
    		    }); 
    	*/
    	 
    	 this.setTitle("@rySubRipper");
    	 this.setResizable(false);		// set frame not resizable

		//******************************* Create panes *******************************
	    JPanel playerPanel = new JPanel(new BorderLayout());		 
		
	    controlPane = new JLayeredPane();
        controlPane.setPreferredSize(new Dimension(1600, 400));
        controlPane.setLayout(null);
	    
        imagePanel1 = new JLayeredPane();
        imagePanel1.setPreferredSize(new Dimension(800, 600));
        imagePanel1.setLayout(null);
        
        getContentPane().add(controlPane, BorderLayout.SOUTH);
        getContentPane().add(playerPanel, BorderLayout.WEST);
        getContentPane().add(imagePanel1, BorderLayout.EAST); 
               
        getContentPane().setBackground(Color.decode("#888888"));
        
        Main = this;

	    //******************************* Player Pane *******************************
		try {
			playerCmpt = new uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent();
		} catch (Exception e) {
			prndeb(1,"ERROR loading vlcj library: " + e.getMessage());
		}
		
        playerCmpt.setPreferredSize(new Dimension(800, 600));

        playerPanel.add(playerCmpt);
        //playerPanel.setLayout(null);
                      
		//******************************* Image Pane *******************************
              
        image1 = new ImagePanel();
        image1.setBounds(0, 0,800,600);
        image1.setForeground(Color.BLACK);
        image1.setBackground(Color.DARK_GRAY);
        image1.setOpaque(true);
        
        captureBox = new JLabel[2];
        for(int i = 0; i < captureBox.length; i++)
        {
        	captureBox[i] = new JLabel("");
        }
        
        captureBox[0].setBounds(80, 401,640,32);
        captureBox[0].setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));

        captureBox[1].setBounds(80, 443,640,32);
        captureBox[1].setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
               
    	seekStatus = new JLabel("xxx");
    	seekStatus.setBounds(0, 568,200,32);
    	seekStatus.setForeground(Color.YELLOW);
    	seekStatus.setBackground(Color.DARK_GRAY);
    	seekStatus.setOpaque(true);
    	seekStatus.setVisible(false);
        
        imagePanel1.add(image1,1);   
        imagePanel1.add(captureBox[0],0);    
        imagePanel1.add(captureBox[1],0);
        imagePanel1.add(seekStatus,0);
        
		//******************************* Control Pane Left *******************************
                    
        // ------------- PLAYER TOOLBAR --------------
        //Line 1
        vidSlid = new JSlider(JSlider.HORIZONTAL,1, 100, 1);
        vidSlid.setBounds(1,0,800,10);
    
        //Line 20
        JButton btnPlay = new JButton();
        btnPlay.setText("Play");
        btnPlay.setBounds(10,20,80,30);

        JButton btnStop = new JButton();
        btnStop.setText("Stop");
        btnStop.setBounds(100,20,80,30);

        JButton btnPause = new JButton();
        btnPause.setText("Pause");
        btnPause.setBounds(190,20,80,30); 

        JButton btnGoToPos = new JButton();
        btnGoToPos.setText("Go To Pos");
        btnGoToPos.setBounds(280,20,80,30);        
        btnGoToPos.setMargin(new Insets(2, 2, 2, 2));

        goToPos  = new JTextArea("30");
        goToPos.setBounds(370,25,50,20);         

        JButton btnBack5 = new JButton();
        btnBack5.setText("<<");
        btnBack5.setBounds(430,20,40,30);
        btnBack5.setMargin(new Insets(2, 2, 2, 2));
        btnBack5.setToolTipText("backs " + step5 + "ms");
        
        JButton btnBack1 = new JButton();
        btnBack1.setText("<");
        btnBack1.setBounds(470,20,40,30);         
        btnBack1.setMargin(new Insets(2, 2, 2, 2));
        btnBack1.setToolTipText("backs " + step1 + "ms");
        
        JButton btnFwd1 = new JButton();
        btnFwd1.setText(">");
        btnFwd1.setBounds(510,20,40,30);    
        btnFwd1.setMargin(new Insets(2, 2, 2, 2));
        btnFwd1.setToolTipText("advances " + step1 + "ms");
        
        JButton btnFwd5 = new JButton();
        btnFwd5.setText(">>");
        btnFwd5.setBounds(550,20,40,30);         
        btnFwd5.setMargin(new Insets(2, 2, 2, 2));
        btnFwd5.setToolTipText("advances " + step5 + "ms");

        btnMute = new JButton();
        btnMute.setText("Mute");
        btnMute.setBounds(600,20,80,30);         
        btnMute.setMargin(new Insets(2, 2, 2, 2));        

        // ------------- OCR TUNING TOOLBAR --------------
        //Line 60
        JButton btnPickColor = new JButton();
        btnPickColor.setText("Pick Ink Color");
        btnPickColor.setBounds(10,60,110,30);  
        btnPickColor.setMargin(new Insets(2, 2, 2, 2));
        btnPickColor.setToolTipText("toggle mode 'pick color' to capture OCR ink color by clicking the still image on the right");
        
        JButton btnSetCaptureBox = new JButton();
        btnSetCaptureBox.setText("Set Capture Box");
        btnSetCaptureBox.setBounds(130,60,140,30);        
        btnSetCaptureBox.setToolTipText("sets capture line boxes based on set up coordinates (L1 (x,y,w,h) and L2(y)");

        JButton btnSnapshot = new JButton();
        btnSnapshot.setText("Snapshot");
        btnSnapshot.setBounds(280,60,100,30);    
        btnSnapshot.setToolTipText("copies current video frame into the OCR image on the right");
        
        JButton btnReadBox = new JButton();
        btnReadBox.setText("Capture Box");
        btnReadBox.setBounds(390,60,110,30);    
        btnReadBox.setToolTipText("takes video current frame snapshot and then captures pick color pixels on the OCR image into the black/white captured box");

        JButton btnTreatBox = new JButton();
        btnTreatBox.setText("Read Sub");
        btnTreatBox.setBounds(510,60,100,30);       
        btnTreatBox.setToolTipText("executes manual OCR recognition of the captured box and writes it on the subtitle text box");
        
        btnReadDirection = new JButton();
        btnReadDirection.setText("L → R");
        btnReadDirection.setBounds(620,60,100,30);
        //btnReadDirection.setIcon(new ImageIcon(SetButtonIcon(10,30,"L-R.png")));
        btnReadDirection.setToolTipText("determines the text reading direction (Left to Right or Right to Left)");

        //Line 100
        boxY = new JTextArea[2];
        
        JLabel lbl2 = new JLabel("L1 (x,y,w,h): ");
        lbl2.setBounds(10,100,80,20);              
        boxX  = new JTextArea(captureBox[0].getBounds().x +"");
        boxX.setBounds(100,100,30,20);    
        boxY[0]  = new JTextArea(captureBox[0].getBounds().y +"");
        boxY[0].setBounds(135,100,30,20);
        boxW  = new JTextArea(captureBox[0].getBounds().width +"");
        boxW.setBounds(170,100,30,20);    
        boxH  = new JTextArea(captureBox[0].getBounds().height +"");
        boxH.setBounds(205,100,30,20);

        JLabel lbl21 = new JLabel("L2 (y): ");
        lbl21.setBounds(260,100,80,20);              
        boxY[1]  = new JTextArea(captureBox[1].getBounds().y +"");
        boxY[1].setBounds(300,100,30,20);    
        
        //Line 130
        JLabel lbl3 = new JLabel("Ink Tol (RGB): ");
        lbl3.setBounds(10,130,90,20);        
        tolerance  = new JTextArea("30");
        tolerance.setBounds(100,130,30,20);    
        toleranceG  = new JTextArea("30");
        toleranceG.setBounds(140,130,30,20);    
        toleranceB  = new JTextArea("30");
        toleranceB.setBounds(180,130,30,20);    

        JLabel lbl4 = new JLabel("Min Spc Pix: ");
        lbl4.setBounds(220,130,80,20);        
        minSpacePix  = new JTextArea("6");
        minSpacePix.setBounds(300,130,30,20);    

        //Line 160
        JLabel lbl5 = new JLabel("OCR Tolerance: ");
        lbl5.setBounds(10,160,90,20);        
        tolOCR  = new JTextArea(".94");
        tolOCR.setBounds(100,160,40,20);         
                
        // ------------- FILES TOOLBAR --------------
        //Line 190
        JLabel lbl6 = new JLabel("Video File: ");
        lbl6.setBounds(10,190,90,20);        
        vFile  = new JTextArea("");
        vFile.setBounds(100,190,600,20);  
        vFile.setEditable(false);
        
        JButton btnBrowse = new JButton();
        btnBrowse.setBounds(710,190,20,20);        
        btnBrowse.setMargin(new Insets(2, 2, 2, 2));
        btnBrowse.setIcon(new ImageIcon(SetButtonIcon(20,20,"search.png")));
        btnBrowse.setToolTipText("opens browser to select an existing video file");

        //Line 220
        JLabel lbl8 = new JLabel("OCR DB: ");
        lbl8.setBounds(10,220,90,20);        
        dbFileTxt  = new JTextArea("");
        dbFileTxt.setBounds(100,220,600,20);  
        dbFileTxt.setEditable(false);
        
        JButton btnBrowseDBF = new JButton();
        btnBrowseDBF.setBounds(710,220,20,20);        
        btnBrowseDBF.setMargin(new Insets(2, 2, 2, 2));
        btnBrowseDBF.setIcon(new ImageIcon(SetButtonIcon(20,20,"search.png")));
        btnBrowseDBF.setToolTipText("opens browser to select an existing ocr database");
        JButton btnAddDBF = new JButton();
        btnAddDBF.setBounds(740,220,20,20);        
        btnAddDBF.setMargin(new Insets(2, 2, 2, 2));
        btnAddDBF.setIcon(new ImageIcon(SetButtonIcon(20,20,"add.png")));
        btnAddDBF.setToolTipText("opens window to create a new ocr database");       
        
        //Line 250
        JLabel lbl9 = new JLabel("Subs DB: ");
        lbl9.setBounds(10,250,90,20);        
        dbSubsTxt  = new JTextArea("");
        dbSubsTxt.setBounds(100,250,600,20); 
        dbSubsTxt.setEditable(false);
        
        JButton btnBrowseDBS = new JButton();
        btnBrowseDBS.setBounds(710,250,20,20);        
        btnBrowseDBS.setMargin(new Insets(2, 2, 2, 2));
        btnBrowseDBS.setIcon(new ImageIcon(SetButtonIcon(20,20,"search.png")));
        btnBrowseDBS.setToolTipText("opens browser to select an existing subtitles database");      
        JButton btnAddDBS = new JButton();
        btnAddDBS.setBounds(740,250,20,20);        
        btnAddDBS.setMargin(new Insets(2, 2, 2, 2));
        btnAddDBS.setIcon(new ImageIcon(SetButtonIcon(20,20,"add.png")));
        btnAddDBS.setToolTipText("opens window to create a new subtitles database");

        // ------------- ADDITIONAL CONTROLS TOOLBAR --------------
        // line 280        
        JButton btnSaveDBF = new JButton();
        //btnSaveDBF.setBounds(1360,220,30,30);        
        btnSaveDBF.setBounds(10,280,30,30);
        btnSaveDBF.setMargin(new Insets(2, 2, 2, 2));
        btnSaveDBF.setIcon(new ImageIcon(SetButtonIcon(27,27,"save.png")));
        btnSaveDBF.setToolTipText("saves parameters into current subtitles database");
        
        JButton btnParams = new JButton();
        //btnParams.setBounds(1400,220,30,30);         
        btnParams.setBounds(50,280,30,30);
        btnParams.setMargin(new Insets(2, 2, 2, 2));
        btnParams.setToolTipText("advanced parameters");    
        btnParams.setIcon(new ImageIcon(SetButtonIcon(22,22,"settings.png")));
        
        JButton btnCharTol = new JButton();
        //btnCharTol.setBounds(1440,220,30,30);   
        btnCharTol.setBounds(90,280,30,30);
        //btnCharTol.setText("abc");
        btnCharTol.setMargin(new Insets(2, 2, 2, 2));
        btnCharTol.setToolTipText("Edit character tolerance");    
        btnCharTol.setIcon(new ImageIcon(SetButtonIcon(25,25,"abc.png")));

        JButton btnCharStats = new JButton();
        //btnCharStats.setBounds(1480,220,30,30);   
        btnCharStats.setBounds(130,280,30,30);
        //btnCharStats.setText("Stat");
        btnCharStats.setMargin(new Insets(2, 2, 2, 2));
        btnCharStats.setToolTipText("Review ocr stats");    
        btnCharStats.setIcon(new ImageIcon(SetButtonIcon(29,29,"stats.png")));

        JButton btnConsole = new JButton();
        //btnConsole.setBounds(1520,220,30,30);   
        btnConsole.setBounds(170,280,30,30);
        //btnConsole.setText("Stat");
        btnConsole.setMargin(new Insets(2, 2, 2, 2));
        btnConsole.setToolTipText("Opens Console");    
        btnConsole.setIcon(new ImageIcon(SetButtonIcon(26,26,"console.png")));
        
        JButton btnAbout = new JButton();
        //btnAbout.setBounds(1560,220,30,30);   
        btnAbout.setBounds(210,280,30,30);
        //btnHelp.setText("?");
        btnAbout.setMargin(new Insets(2, 2, 2, 2));
        btnAbout.setToolTipText("About");    
        btnAbout.setIcon(new ImageIcon(SetButtonIcon(32,32,"about.png")));        

        // ------------- STATUS TOOLBAR --------------        
        // line 330
        lblStatus = new JLabel("Mode: ");
        lblStatus.setBounds(10, 380,100,20);
        lblStatus.setForeground(Color.BLACK);
        lblStatus.setBackground(Color.WHITE);
        lblStatus.setOpaque(true);
        
        JLabel lbl1 = new JLabel("Ink Color: ");
        lbl1.setBounds(110, 380,80,20);

        pickColor = new JLabel();
        pickColor.setBounds(180, 380+2,15,15);
        pickColor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        pickColor.setOpaque(true);
        
        lblStatus2 = new JLabel("stat2");
        lblStatus2.setBounds(210, 380,500,20);        
        
		//******************************* Control Pane Right *******************************   
        // ------------- OCR CAPTURED BOX --------------            
        //Line 20
        captBox = new JLabel[2];	// box where we extract and treat as image the lines from the screenshot
        captBox[0] = new JLabel();
        captBox[0].setBounds(800, 10,450,32);
        captBox[0].setBackground(Color.BLACK);
        captBox[0].setOpaque(true);                  
        captBox[1] = new JLabel();
        captBox[1].setBounds(800, 42,450,32);
        captBox[1].setBackground(Color.BLACK);
        captBox[1].setOpaque(true);                  
        
        charBox = new JLabel();		// rectangle to highlight the selected char
        charBox.setBounds(800, 20,50,32);
        charBox.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        charBox.setVisible(false); 
 
        // ------------- SUBTITLE RIPPING TOOLBAR --------------    
        //Line 120
        subText  = new JTextArea("");	
        subText.setBounds(800,120,400,55);   
        subText.setFont(subText.getFont().deriveFont((float)22));

        JLabel lbl12 = new JLabel("Rec Id: ");
        lbl12.setBounds(1210,120,50,20);        
        subRecId  = new JTextArea("");
        subRecId.setBounds(1280,120,90,20);
        subRecId.setEditable(false);
        subRecId.setOpaque(false);

        stopAfterF = new JCheckBox("Stop after read");	// checkbox to stop
        stopAfterF.setBounds(1400,120,150,20); 

        JLabel lbl7 = new JLabel("From pos: ");
        lbl7.setBounds(1210,150,80,20);        
        subStartPos  = new JTextArea("");
        subStartPos.setBounds(1280,150,90,20);        
        

        JLabel lbl10 = new JLabel("To pos: ");
        lbl10.setBounds(1400,150,50,20);        
        subEndPos  = new JTextArea("");
        subEndPos.setBounds(1450,150,90,20);        
        
        // ------------- SUBTITLE EDITION TOOLBAR --------------  
        //Line 180
        JButton btnRunAuto = new JButton();
        btnRunAuto.setText("Run Auto");
        btnRunAuto.setBounds(800,180,100,30);          
        btnRunAuto.setMargin(new Insets(2, 2, 2, 2));
        btnRunAuto.setToolTipText("Starts automatic OCR process from the current position");
        
        JButton btnSeekNextSub = new JButton();
        btnSeekNextSub.setText("Seek Sub >");
        btnSeekNextSub.setBounds(910,180,100,30); 
        btnSeekNextSub.setMargin(new Insets(2, 2, 2, 2));
        btnSeekNextSub.setToolTipText("Searches for the next subtitle in the video from the current position");
        
        JButton btnSetSubStart = new JButton();
        btnSetSubStart.setText("Set Sub Start");
        btnSetSubStart.setBounds(1020,180,100,30); 
        btnSetSubStart.setMargin(new Insets(2, 2, 2, 2));
        btnSetSubStart.setToolTipText("Sets current position as start subtitle position");

        JButton btnSetSubEnd = new JButton();
        btnSetSubEnd.setText("Set Sub End");
        btnSetSubEnd.setBounds(1130,180,100,30); 
        btnSetSubEnd.setMargin(new Insets(2, 2, 2, 2));        
        btnSetSubEnd.setToolTipText("Sets current position as end position and saves the subtitle");

        chkInteractive = new JCheckBox("Interactive");	// checkbox to set interactive
        chkInteractive.setBounds(1250,185,100,20);
        chkInteractive.setSelected(interactiveMode);        
        
        JButton btnWriteSub = new JButton();
        btnWriteSub.setText("Write SRT");
        btnWriteSub.setBounds(1360,180,110,30); 
        btnWriteSub.setMargin(new Insets(2, 2, 2, 2));        
        btnWriteSub.setToolTipText("Process database stored subtitles for the current video and generates the SRT file");

        //line 220
        JButton btnGoToFirstSub = new JButton();
        btnGoToFirstSub.setText("<<");
        btnGoToFirstSub.setBounds(800,220,40,30);
        btnGoToFirstSub.setMargin(new Insets(2, 2, 2, 2));
        btnGoToFirstSub.setToolTipText("Moves to First Saved Sub");
        
        JButton btnGoToPrevSub = new JButton();
        btnGoToPrevSub.setText("<");
        btnGoToPrevSub.setBounds(840,220,40,30);         
        btnGoToPrevSub.setMargin(new Insets(2, 2, 2, 2));
        btnGoToPrevSub.setToolTipText("Moves to Previous Saved Sub");
        
        JButton btnGoToNextSub = new JButton();
        btnGoToNextSub.setText(">");
        btnGoToNextSub.setBounds(880,220,40,30);    
        btnGoToNextSub.setMargin(new Insets(2, 2, 2, 2));
        btnGoToNextSub.setToolTipText("Moves to Next Saved Sub");
        
        JButton btnGoToLastSub = new JButton();
        btnGoToLastSub.setText(">>");
        btnGoToLastSub.setBounds(920,220,40,30);         
        btnGoToLastSub.setMargin(new Insets(2, 2, 2, 2));
        btnGoToLastSub.setToolTipText("Moves to Last Saved Sub");        
        
        JButton btnGoToPrevSubErr = new JButton();
        btnGoToPrevSubErr.setText("<@");
        btnGoToPrevSubErr.setBounds(970,220,40,30);         
        btnGoToPrevSubErr.setMargin(new Insets(2, 2, 2, 2));
        btnGoToPrevSubErr.setToolTipText("Moves to Previous Saved Sub with Error");
        
        JButton btnGoToNextSubErr = new JButton();
        btnGoToNextSubErr.setText("@>");
        btnGoToNextSubErr.setBounds(1010,220,40,30);    
        btnGoToNextSubErr.setMargin(new Insets(2, 2, 2, 2));
        btnGoToNextSubErr.setToolTipText("Moves to Next Saved Sub with Error");                
        
        JButton btnEditCurSub = new JButton();
        btnEditCurSub.setText("Edit");
        btnEditCurSub.setBounds(1060,220,50,30); 
        btnEditCurSub.setMargin(new Insets(2, 2, 2, 2));  
        btnEditCurSub.setToolTipText("Edits Current Saved Sub");        

        JButton btnSaveCurSub = new JButton();
        btnSaveCurSub.setText("Save");
        btnSaveCurSub.setBounds(1120,220,50,30); 
        btnSaveCurSub.setMargin(new Insets(2, 2, 2, 2));  
        btnSaveCurSub.setToolTipText("Saves Current Sub");           

        btnDeleteCurSub = new JButton();
        btnDeleteCurSub.setText("Delete");
        btnDeleteCurSub.setBounds(1180,220,50,30); 
        btnDeleteCurSub.setMargin(new Insets(2, 2, 2, 2));
        btnDeleteCurSub.setBackground(Color.RED);
        btnDeleteCurSub.setForeground(Color.WHITE);
        btnDeleteCurSub.setToolTipText("Deletes Current Sub");   
        
        JButton btnMergeCurSubPrev = new JButton();
        btnMergeCurSubPrev.setText("<Mrg");
        btnMergeCurSubPrev.setBounds(1240,220,50,30); 
        btnMergeCurSubPrev.setMargin(new Insets(2, 2, 2, 2));  
        btnMergeCurSubPrev.setToolTipText("Merges Current Sub with Previous one - current sub text is lost");   

        JButton btnMergeCurSubNext = new JButton();
        btnMergeCurSubNext.setText("Mrg>");
        btnMergeCurSubNext.setBounds(1290,220,50,30); 
        btnMergeCurSubNext.setMargin(new Insets(2, 2, 2, 2));  
        btnMergeCurSubNext.setToolTipText("Merges Current Sub with Next one - current sub text is lost");   

        JButton btnGoToSub = new JButton();
        btnGoToSub.setText("Go To Sub");
        btnGoToSub.setBounds(1350,220,80,30);        
        btnGoToSub.setMargin(new Insets(2, 2, 2, 2));

        goToSub  = new JTextArea("1");
        goToSub.setBounds(1440,225,30,20);    
        goToSub.setAlignmentX(CENTER_ALIGNMENT);

        chkShowDel = new JCheckBox("Show Deleted");	// checkbox to define if deleted records are shown in navigation buttons
        chkShowDel.setBounds(1475,225,130,20);
        chkShowDel.setSelected(false);        
        
        // ------------- SUBTITLE STATUS BOX --------------  
        //line 260
        lblStatOCR = new JTextPane();
        lblStatOCR.setBounds(800,260,800,140);
        lblStatOCR.setContentType("text/html");
        lblStatOCR.setText("<html><font size='4'>OCR stat:</font></html>");
        lblStatOCR.setEditable(false);
        lblStatOCR.setOpaque(true);
        lblStatOCR.setFont(subText.getFont().deriveFont((float)17));
        lblStatOCR.setBackground(Color.LIGHT_GRAY);
        
        //------- ADD controls to pane
        
        //left
        // -- player ctrl
        controlPane.add(vidSlid,5);
        controlPane.add(btnPlay,5);
        controlPane.add(btnStop,5);
        controlPane.add(btnPause,5);
        controlPane.add(btnGoToPos,5);   
        controlPane.add(goToPos,5);
        controlPane.add(btnBack5,5);   
        controlPane.add(btnBack1,5);
        controlPane.add(btnFwd1,5);
        controlPane.add(btnFwd5,5);
        controlPane.add(btnMute,5);
        // -- actions
        controlPane.add(btnPickColor,5);
        controlPane.add(btnSetCaptureBox,5);
        controlPane.add(btnSnapshot,5);
        controlPane.add(btnReadBox,5);
        controlPane.add(btnTreatBox,5);
        controlPane.add(btnReadDirection,5);
        // -- settings
        controlPane.add(lbl2);
        controlPane.add(lbl21);
        controlPane.add(boxX);
        controlPane.add(boxW);
        controlPane.add(boxH);
        controlPane.add(boxY[0]);        
        controlPane.add(boxY[1]);
        controlPane.add(lbl3);
        controlPane.add(tolerance);
        controlPane.add(toleranceG);
        controlPane.add(toleranceB);
        controlPane.add(lbl4);
        controlPane.add(minSpacePix);
        controlPane.add(lbl5);
        controlPane.add(tolOCR);
        controlPane.add(lbl6);
        controlPane.add(vFile);
        controlPane.add(btnBrowse);
        controlPane.add(lbl8);
        controlPane.add(dbFileTxt);
        controlPane.add(btnBrowseDBF);
        controlPane.add(btnAddDBF);
        controlPane.add(btnSaveDBF);
        controlPane.add(lbl9);
        controlPane.add(dbSubsTxt);
        controlPane.add(btnBrowseDBS);
        controlPane.add(btnAddDBS);
        
        // right
        controlPane.add(captBox[0],5);
        controlPane.add(captBox[1],5);
        controlPane.add(charBox,0);
        controlPane.add(subText,0);
        controlPane.add(lbl7);              
        controlPane.add(subStartPos,0);
        controlPane.add(stopAfterF,0);
        controlPane.add(lbl10);    
        controlPane.add(subEndPos,0);
        controlPane.add(lbl12,0);
        controlPane.add(subRecId,0);  
        controlPane.add(btnRunAuto);
        controlPane.add(btnSeekNextSub);
        controlPane.add(btnSetSubStart);
        controlPane.add(btnSetSubEnd);
        controlPane.add(chkInteractive);
        controlPane.add(btnWriteSub);
        controlPane.add(btnGoToFirstSub);
        controlPane.add(btnGoToPrevSub);
        controlPane.add(btnGoToNextSub);
        controlPane.add(btnGoToLastSub);
        controlPane.add(btnEditCurSub);
        controlPane.add(btnSaveCurSub);
        controlPane.add(btnGoToPrevSubErr);
        controlPane.add(btnGoToNextSubErr);
        controlPane.add(btnMergeCurSubPrev);        
        controlPane.add(btnMergeCurSubNext);        
        controlPane.add(btnDeleteCurSub);
        controlPane.add(btnGoToSub);
        controlPane.add(goToSub);
        controlPane.add(chkShowDel);
        controlPane.add(btnParams);
        controlPane.add(btnCharTol);
        controlPane.add(btnCharStats);
        controlPane.add(btnConsole);
        controlPane.add(btnAbout);
        controlPane.add(lblStatOCR);  
        //status        
        controlPane.add(lblStatus);
        controlPane.add(lblStatus2);
        controlPane.add(lbl1);        
        controlPane.add(pickColor);
        
        //******************************* init variables *******************************
        InitVariables();
              
        //******************************* declare Events *******************************                      
                
        btnPlay.addActionListener(new ActionBtnPlay());
        btnStop.addActionListener(new ActionBtnStop());
        btnPause.addActionListener(new ActionBtnPause());
        btnGoToPos.addActionListener(new ActionBtnGoToPos());
        btnBack5.addActionListener(new ActionBtnBack5());
        btnBack1.addActionListener(new ActionBtnBack1());
        btnFwd1.addActionListener(new ActionBtnFwd1());
        btnFwd5.addActionListener(new ActionBtnFwd5());
        btnMute.addActionListener(new ActionBtnMute());
        btnPickColor.addActionListener(new ActionBtnPickColor());
        btnSetCaptureBox.addActionListener(new ActionBtnSetCaptureBox());
        btnSnapshot.addActionListener(new ActionBtnSnapshot());
        btnReadBox.addActionListener(new ActionBtnReadBox());
        btnTreatBox.addActionListener(new ActionBtnTreatBox());
        btnReadDirection.addActionListener(new ActionBtnReadDirection());
        btnBrowse.addActionListener(new ActionBtnBrowse());
        btnBrowseDBF.addActionListener(new ActionBtnBrowseDBF());        
        btnBrowseDBS.addActionListener(new ActionBtnBrowseDBS());
        btnAddDBF.addActionListener(new ActionBtnAddDBF());
        btnAddDBS.addActionListener(new ActionBtnAddDBS());      
        btnRunAuto.addActionListener(new ActionBtnRunAuto());
        btnSeekNextSub.addActionListener(new ActionBtnSeekNextSub());
        btnSetSubStart.addActionListener(new ActionBtnSetSubStart());
        btnSetSubEnd.addActionListener(new ActionBtnSetSubEnd());
        btnWriteSub.addActionListener(new ActionBtnWriteSub());
        btnEditCurSub.addActionListener(new ActionBtnEditCurSub());
        btnSaveCurSub.addActionListener(new ActionBtnSaveCurSub());
        btnGoToLastSub.addActionListener(new ActionBtnGoToLastSub());
        btnGoToFirstSub.addActionListener(new ActionBtnGoToFirstSub());
        btnGoToNextSub.addActionListener(new ActionBtnGoToNextSub());
        btnGoToPrevSub.addActionListener(new ActionBtnGoToPrevSub());
        btnGoToNextSubErr.addActionListener(new ActionBtnGoToNextSubErr());
        btnGoToPrevSubErr.addActionListener(new ActionBtnGoToPrevSubErr());
        btnSaveDBF.addActionListener(new ActionBtnSaveDBF());
        btnMergeCurSubPrev.addActionListener(new ActionBtnMergeCurSubPrev());
        btnMergeCurSubNext.addActionListener(new ActionBtnMergeCurSubNext());
        btnDeleteCurSub.addActionListener(new ActionBtnDeleteCurSub());
        btnGoToSub.addActionListener(new ActionBtnGoToSub());
        btnParams.addActionListener(new ActionBtnParams());        
        btnCharTol.addActionListener(new ActionBtnCharTol());
        btnCharStats.addActionListener(new ActionBtnCharStats());
        btnConsole.addActionListener(new ActionBtnConsole());
        btnAbout.addActionListener(new ActionBtnAbout());

        playerCmpt.videoSurfaceComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               //lblStatus.setText(playerCmpt.getMousePosition().x + "," + playerCmpt.getMousePosition().y);              
               
               CopyFrame();
               ReadBoxV2();
            }
         });        
        
        // Add change listener to the slider
        vidSlid.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				// TODO Auto-generated method stub
				//JOptionPane.showMessageDialog(null, "My Goodness, this is so concise");
				/*
				playerCmpt.mediaPlayer().media().getFPS();
				playerCmpt.mediaPlayer().status().time();
				playerCmpt.mediaPlayer().status().length();
				playerCmpt.mediaPlayer().status().position();
				playerCmpt.mediaPlayer().media().info().duration();
				playerCmpt.mediaPlayer().controls().skipPosition(float); //Skip forward or backward by a change in position.
				playerCmpt.mediaPlayer().controls().skipTime(long);		 //Skip forward or backward by a period of time.
				playerCmpt.mediaPlayer().controls().setPosition​(float position) 	//Jump to a specific position.
				playerCmpt.mediaPlayer().controls().setTime​(long time) 	//Jump to a specific moment.
				*/
				
		        vidSlid.setMaximum((int) (playerCmpt.mediaPlayer().status().length()));				
		    	 
		        if (Math.abs(playerCmpt.mediaPlayer().status().time()-vidSlid.getValue())>2000 || playerStatus==0) {
		        //only updates if the slider moved more than what advances in a second to avoid collision with slider playing video update 
		        	playerCmpt.mediaPlayer().controls().setTime(vidSlid.getValue());
		        	PaintStatusBar();
		        }
			}
        });
        
        AddKeyListener();
        
		CheckVariables();
		PaintStatusBar();
        
        //******************************* Print panes *******************************		
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
        this.repaint();                      
        
        //check new version
        if (chkNewVersion) {
        	checkNewVersion(appNextVersion1);
        	checkNewVersion(appNextVersion2);
        }
        
        //playerCmpt.mediaPlayer().media().prepare("D:\\NewsLeecher\\downloads\\Grabit\\Zaguri Imperia\\Zaguri Imperia - S01E03 vosthe.mp4");
        //playerCmpt.mediaPlayer().media().startPaused("D:\\NewsLeecher\\downloads\\Grabit\\Zaguri Imperia\\Zaguri Imperia - S01E03 vosthe.mp4");
        //playerCmpt.mediaPlayer().media().play("D:\\NewsLeecher\\downloads\\Grabit\\Zaguri Imperia\\Zaguri Imperia - S01E03 vosthe.mp4");
        //playerCmpt.mediaPlayer().controls().pause();
                
    }	// close SubRipper
       
    //******************************* MAIN FRAME ACTION BUTTONS ************************************
    
    //------------------------------- ACTIONS LEFT PANE --------------------------------------
    
	//******************************* PLAYER TOOLBAR ************************************
	public static class ActionBtnPlay implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	  		PlayerPlay();
	  		setTimer();
	  	}
	}
	
	public static class ActionBtnStop implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	  		PlayerStop();
	  	}
	}
	
	public static class ActionBtnPause implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	  		PlayerPause();
	  	}
	}

	public static class ActionBtnBack5 implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnBack5");
	        
	        long pos= playerCmpt.mediaPlayer().status().time()-step5;
	        if (pos<0) pos = 0;
	        playerCmpt.mediaPlayer().controls().setTime(pos);
	        PaintStatusBar();
	        
	        prndeb(5,"exit ActionBtnBack5");   
	  	}
	}
	
	public static class ActionBtnBack1 implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnBack1");

	        long pos= playerCmpt.mediaPlayer().status().time()-step1;
	        if (pos<0) pos = 0;
	        playerCmpt.mediaPlayer().controls().setTime(pos);
	        PaintStatusBar();
	        
	        prndeb(5,"exit ActionBtnBack1");   
	  	}
	}

	public static class ActionBtnFwd5 implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnFwd5");

	        long pos= playerCmpt.mediaPlayer().status().time()+step5;
	        if (pos>PlayerGetEndPos()) pos = PlayerGetEndPos();
	        playerCmpt.mediaPlayer().controls().setTime(pos);
	        PaintStatusBar();
	        
	        prndeb(5,"exit ActionBtnFwd5");   
	  	}
	}

	public static class ActionBtnFwd1 implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnFwd1");

	        long pos= playerCmpt.mediaPlayer().status().time()+step1;
	        if (pos>PlayerGetEndPos()) pos = PlayerGetEndPos();
	        playerCmpt.mediaPlayer().controls().setTime(pos);
	        PaintStatusBar();
	        
	        prndeb(5,"exit ActionBtnFwd1");   
	  	}
	}
	
	public static class ActionBtnMute implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnMute");

	        if (playerCmpt.mediaPlayer().audio().isMute()) {
		  		playerCmpt.mediaPlayer().audio().setMute(false);
		  		btnMute.setText("Mute");
	        }
	        else {
	        	playerCmpt.mediaPlayer().audio().setMute(true);
	        	btnMute.setText("Unmute");
	        }
	        
	        prndeb(5,"exit ActionBtnMute");   
	  	}
	}
		
	public static class ActionBtnGoToPos implements ActionListener {
	  	public void actionPerformed(ActionEvent e) {
	  		int pos;
	  		
	  		if(isNumeric(goToPos.getText())) {
	  			pos = toInt(goToPos.getText());
	  			PlayerGoTo(pos);
	  		}
	  	}
	}
	
	//******************************* OCR TUNING TOOLBAR ************************************
	
	public static class ActionBtnPickColor implements ActionListener {				// sets mode pick color
	  	public void actionPerformed(ActionEvent e) {
	  		SetPickColor();
	  	}
	}

	public static class ActionBtnSetCaptureBox implements ActionListener {			// SET CAPTURE BOX
	  	public void actionPerformed(ActionEvent e) {
	  		SetCaptureBox();
	  	}
	}

	public static class ActionBtnSnapshot implements ActionListener {				// TAKE SNAPSHOT OF CURRENT VIDEO FRAME
	  	public void actionPerformed(ActionEvent e) {
	  		CopyFrame();
	  	}		
	}
	
	public static class ActionBtnReadBox implements ActionListener {				// CAPTURE BOX
	  	public void actionPerformed(ActionEvent e) {
            CopyFrame();
	  		ReadBoxV2();
	  	}
	}

	public static class ActionBtnTreatBox implements ActionListener {				// READ SUB
	  	public void actionPerformed(ActionEvent e) {
	  		ReadSub();
	  		//InitPopDlg();
	  		//ShowPopDlg(null);
	  	} // close actionPerformed
	  	
	}	//close ActionBtnTreatBox class
	
	public static class ActionBtnReadDirection implements ActionListener {			// L <-> R
	  	public void actionPerformed(ActionEvent e) {
	  		if (readDir==1) {
	  			readDir=0;
	  		}
	  		else {
	  			readDir=1;
	  		}
  			PaintReadDirButton();
	  	} // close actionPerformed
	  	
	}	//close ActionBtnTreatBox class	

	//******************************* FILES TOOLBAR ************************************
	
	public static class ActionBtnBrowse implements ActionListener {					// browse Video File
	  	public void actionPerformed(ActionEvent e) {
	  		BrowseFile("V");
	  	}
	}
	
	public static class ActionBtnBrowseDBF implements ActionListener {				// browse OCR File
	  	public void actionPerformed(ActionEvent e) {
	  		BrowseFile("O");
	  	}
	}
	
	public static class ActionBtnBrowseDBS implements ActionListener {				// browse Subtitle File
	  	public void actionPerformed(ActionEvent e) {
	  		BrowseFile("S");
	  	}
	}

	public static class ActionBtnAddDBS implements ActionListener {					// Adds New Subtitle File
	  	public void actionPerformed(ActionEvent e) {
	  		PopNewFile("S");
	  	}
	}
	
	public static class ActionBtnAddDBF implements ActionListener {					// Adds New OCR File
	  	public void actionPerformed(ActionEvent e) {
	  		PopNewFile("O");
	  	}
	}

    //------------------------------- ACTIONS RIGHT PANE --------------------------------------
	
	//******************************* SUBTITLE RIPPING TOOLBAR ************************************

	public static class ActionBtnRunAuto implements ActionListener {					// READ AUTO
	  	public void actionPerformed(ActionEvent e) {
	  		if(bgMode==1) {
	        SwingWorker sw2 = new SwingWorker()  
		        {        	
					@Override
					protected Object doInBackground() throws Exception {
		  				RunAutoV1();
						return null;
		  			}
		  		};
		  		
		  		sw2.execute();
	  		}
	  		else   		
	  			RunAutoV1();
	  		
	  	} // close actionPerformed
	  	
	}	//close ActionBtnTreatBox class	

	public static class ActionBtnSeekNextSub implements ActionListener {				// SEEK NEXT SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		SeekNextSubV1(1);
	  	} // close actionPerformed
	  	
	}	//close ActionBtnSeekNextSub class	
	
	public static class ActionBtnSetSubStart implements ActionListener {				// SET SUB START
	  	public void actionPerformed(ActionEvent e) {  		
	  		SetSubStart();
	  	} // close actionPerformed	  		
	}	//close ActionBtnSetSubStart class	
	
	public static class ActionBtnSetSubEnd implements ActionListener {					// SET SUB END
	  	public void actionPerformed(ActionEvent e) {  		
	  		SetSubEnd();
	  	} // close actionPerformed
	  	
	}	//close ActionBtnSetSubEnd class		
	
	public static class ActionBtnWriteSub implements ActionListener {					// SET WRITE SRT
	  	public void actionPerformed(ActionEvent e) {  
	  		WriteSubtitles();
	  	} // close actionPerformed
	  	
	}	//close ActionBtnWriteSub class	
	
	public static class ActionBtnEditCurSub implements ActionListener {					// EDIT CURRENT SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("L", 1);
	  	} // close actionPerformed

	}	//close ActionBtnEditCurSub class	

	public static class ActionBtnSaveCurSub implements ActionListener {					// SAVE CURRENT SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		SaveCurSub();
	  	} // close actionPerformed

	}	//close ActionBtnSaveCurSub class	
	
	//******************************* SUBTITLE EDITION TOOLBAR ************************************
	
	public static class ActionBtnGoToFirstSub implements ActionListener {				// GO TO FIRST SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("F",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToFirstSub class		
	
	public static class ActionBtnGoToLastSub implements ActionListener {				// GO TO LAST SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("L",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToLastSub class	
	
	public static class ActionBtnGoToPrevSub implements ActionListener {				// GO TO PREVIOUS SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("P",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToPrevSub class	
	
	public static class ActionBtnGoToNextSub implements ActionListener {				// GO TO NEXT SUB WITH ERROR
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("N",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToNextSub class	
	
	public static class ActionBtnGoToPrevSubErr implements ActionListener {				// GO TO PREVIOUS SUB WITH ERROR
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("PE",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToPrevSubErr class	
	
	public static class ActionBtnGoToNextSubErr implements ActionListener {				// GO TO NEXT SUB
	  	public void actionPerformed(ActionEvent e) {  		
	  		GoToSub("NE",0);
	  	} // close actionPerformed

	}	//close ActionBtnGoToNextSubErr class	
	
	public static class ActionBtnSaveDBF implements ActionListener {					// SAVES PARAMETERS TO DB
	  	public void actionPerformed(ActionEvent e) {
	  		SaveVariables();
	  	}
	}	//close ActionBtnSaveDBF class	
	
	public static class ActionBtnMergeCurSubPrev implements ActionListener {			// MERGES SUB INTO PREVIOUS
	  	public void actionPerformed(ActionEvent e) {
	  		MergeCurSub("P");
	  	}
	}	//close ActionBtnMergeCurSubPrev class	

	public static class ActionBtnMergeCurSubNext implements ActionListener {			// MERGES SUB INTO NEXT
	  	public void actionPerformed(ActionEvent e) {
	  		MergeCurSub("N");
	  	}
	}	//close ActionBtnMergeCurSubNext class	
	
	public static class ActionBtnDeleteCurSub implements ActionListener {				// MARKS SUB AS DELETED		
	  	public void actionPerformed(ActionEvent e) {
	  		DeleteCurSub();
	  	}
	}	//close ActionBtnDeleteCurSub class		
		
	public static class ActionBtnGoToSub implements ActionListener {					// MOVES TO SUB #	
	  	public void actionPerformed(ActionEvent e) {
	  		String pos;
	  		
	  		if(isNumeric(goToSub.getText())) {
	  			pos = goToSub.getText();
		  		GoToSub(pos,0);
	  		}
	  	}
	}

	//******************************* ADDITIONAL TOOLBAR ************************************
	
	public static class ActionBtnParams implements ActionListener {						// OPENS PARAMETERS WINDOW
	  	public void actionPerformed(ActionEvent e) {
	        prndeb(5,"enter ActionBtnParams");
	        
	  		SaveVariables();	// saves the variables to the db
	        InitParamsDlg(); 	// retrieves all variables from db
	        
	        prndeb(5,"exit ActionBtnParams");   
	  	}
	}	//close ActionBtnParams class
	
	public static class ActionBtnCharTol implements ActionListener {					// OPENS CHAR TOLERANCE POP UP
	  	public void actionPerformed(ActionEvent e) {
	  		InitCharTolDlg();
	  	} // close actionPerformed
	}

	public static class ActionBtnCharStats implements ActionListener {					// OPENS OCR STATS POP UP

	  	public void actionPerformed(ActionEvent e) {  
	  		InitCharStatsDlg();
	  	} // close actionPerformed
	}

	public static class ActionBtnConsole implements ActionListener {					// OPENS CONSOLE WINDOW

	  	public void actionPerformed(ActionEvent e) {  
	  		if(!isConsoleOn) {
	  			Console cons = new Console();
	  		}
	  	} // close actionPerformed
	}

	public static class ActionBtnAbout implements ActionListener {						// OPENS ABOUT POP UP
	  	public void actionPerformed(ActionEvent e) {
	  		InitAboutDlg();
	  	} // close actionPerformed
	}
	
    //******************************* SETTING VARIABLES ************************************

    public static void InitVariables() {
    	prndeb(5,"enter InitVariables");
    	
    	prndeb(4,"initializing variables");
    	
    	String tmp="";
    	
    	tmp = GetIniEntry("LastSaved","ocr");
    	if (tmp.length()==0) {
    		if(fileExists(SqliteDB.dbGetPath() + "\\" + "default_ocr.db")) {
    			tmp=SqliteDB.dbGetPath() + "\\" + "default_ocr.db";
    		}
    	}
    	if(tmp.length()!=0) {
    		dbFile=tmp;
    		dbFileTxt.setText(tmp);
    	}
    	   	
    	tmp = GetIniEntry("LastSaved","subs");
    	if (tmp.length()==0) {
    		if(fileExists(SqliteDB.dbGetPath() + "\\" + "default_subs.db")) {
    			tmp=SqliteDB.dbGetPath() + "\\" + "default_subs.db";
    		}
    	}
    	if(tmp.length()!=0) {
    		dbSubs=tmp;
    		dbSubsTxt.setText(tmp);
    	}
    	
        srtFile = SqliteDB.dbGetPath() + "\\" + "generated.srt";
    	
    	tmp = GetIniEntry("Path","vlc");
    	if(tmp.length()!=0) {
    		vlcPath=tmp;
    	}
              	
    	if (vlcPath!=null) {
    		if (dirExists(vlcPath)) {
    			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
    		} else {
    			prndeb(1,"vlc path incorrect in config.ini");
    		}
    	} else {
    		prndeb(1,"vlc path not defined in config.ini");
    	}

	    
    	Connection conn=null;
    	conn = SqliteDB.dbConnect(dbFile);
    	
    	if (dbFile.length()==0 || !fileExists(dbFile) || conn==null) {	
    		prndeb(1,"Parameters database not defined in config.ini or path incorrect. Default paramters loaded.");    		
    	}
    	else {				//if  configuration file exits then retrieve parameters
    		try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
    		
    		SetPickColor(toInt(SqliteDB.getParam(dbFile,"pick")));    	
    		tolerance.setText(SqliteDB.getParam(dbFile,"toleranceR"));
    		toleranceG.setText(SqliteDB.getParam(dbFile,"toleranceG"));
    		toleranceB.setText(SqliteDB.getParam(dbFile,"toleranceB"));
    		minSpacePix.setText(SqliteDB.getParam(dbFile,"min_spc_pix"));
    		tolOCR.setText(SqliteDB.getParam(dbFile,"char_tol"));
    		boxX.setText(SqliteDB.getParam(dbFile,"line_x"));
    		boxH.setText(SqliteDB.getParam(dbFile,"line_h"));
    		boxW.setText(SqliteDB.getParam(dbFile,"line_w"));
    		boxY[0].setText(SqliteDB.getParam(dbFile,"line_y_1"));
    		boxY[1].setText(SqliteDB.getParam(dbFile,"line_y_2"));

    		step = toInt(SqliteDB.getParam(dbFile,"step_frame"));	
    		adjStep = toInt(SqliteDB.getParam(dbFile,"step_adjustment"));
    		adjSub = toInt(SqliteDB.getParam(dbFile,"sub_adjustment"));
    		typeLastSub=toInt(SqliteDB.getParam(dbFile,"type_last_sub"));
    		step1=toInt(SqliteDB.getParam(dbFile,"step_slow"));
    		step5=toInt(SqliteDB.getParam(dbFile,"step_fast"));
    		frameTolPix=toInt(SqliteDB.getParam(dbFile,"frame_tol_pix"));
    		frameTolPct=toDouble(SqliteDB.getParam(dbFile,"frame_tol_pct"));
    		minFramePix=toInt(SqliteDB.getParam(dbFile,"min_frame_pix"));
    		seekAfterManual=toInt(SqliteDB.getParam(dbFile,"seek_after_manual"));
    		charPropTol=toDouble(SqliteDB.getParam(dbFile,"char_prop_tol"));
    		vertPixMatchCoef=toDouble(SqliteDB.getParam(dbFile,"vert_pix_match_coef"));
    		maxSpacePix=toInt(SqliteDB.getParam(dbFile,"max_space_pix"));
    		bgMode=toInt(SqliteDB.getParam(dbFile,"background_mode"));
    		vlcTimeout=toInt(SqliteDB.getParam(dbFile,"vlc_timeout"));
    		vlcRetries=toInt(SqliteDB.getParam(dbFile,"vlc_retries"));
    		
    		if (toInt(SqliteDB.getParam(dbFile,"gen_ocr_stats"))==0) {    			
    			generateOcrStats=false;
    		}
    		else {
    			generateOcrStats=true;
    		}

    		if (toInt(SqliteDB.getParam(dbFile,"check_new_version"))==0) {    			
    			chkNewVersion=false;
    		}
    		else {
    			chkNewVersion=true;
    		}
    		
    		readDir=toInt(SqliteDB.getParam(dbFile,"read_dir"));
    		PaintReadDirButton();
    		
    		SetCaptureBox();
    		
    		// this two parameters are retrieved at the end just in case there's a problem with the rest of the parameters in the database
    		debugLevel=toInt(SqliteDB.getParam(dbFile,"debug_level"));    		
    		if (toInt(SqliteDB.getParam(dbFile,"write_log"))==0) {    			
    			writeLog=false;
    		}
    		else {
    			writeLog=true;
    		}
    	}
    	
    	prndeb(5,"exit InitVariables");
    }

    public static void SaveVariables() {
    	prndeb(5,"enter SaveVariables");
    	
    	String tmp="";
    	
    	if(dbFileTxt.getText().length()==0) {
    		msgBox("You must define an OCR database");    		
    	}    	
    	else {
    		dbFile=dbFileTxt.getText();
    	
    		SqliteDB.saveParam(dbFile,"toleranceR",tolerance.getText().trim());
    		SqliteDB.saveParam(dbFile,"toleranceB",toleranceB.getText().trim());
    		SqliteDB.saveParam(dbFile,"toleranceG",toleranceG.getText().trim());
    		SqliteDB.saveParam(dbFile,"min_spc_pix", minSpacePix.getText().trim());
    		SqliteDB.saveParam(dbFile,"char_tol", tolOCR.getText().trim());
    		SqliteDB.saveParam(dbFile,"line_x", boxX.getText().trim());
    		SqliteDB.saveParam(dbFile,"line_h", boxH.getText().trim());
    		SqliteDB.saveParam(dbFile,"line_w", boxW.getText().trim());
    		SqliteDB.saveParam(dbFile,"line_y_1", boxY[0].getText().trim());
    		SqliteDB.saveParam(dbFile,"line_y_2", boxY[1].getText().trim());
    		SqliteDB.saveParam(dbFile,"read_dir", readDir+"");    		
    		SqliteDB.saveParam(dbFile,"step_frame", step+"");    	
    		SqliteDB.saveParam(dbFile,"step_adjustment", adjStep+"");    	
    		SqliteDB.saveParam(dbFile,"sub_adjustment", adjSub+"");
    		SqliteDB.saveParam(dbFile,"type_last_sub", typeLastSub+"");    	
    		SqliteDB.saveParam(dbFile,"step_slow", step1+"");    	
    		SqliteDB.saveParam(dbFile,"step_fast", step5+"");    	
    		SqliteDB.saveParam(dbFile,"read_dir", readDir+"");
    		SqliteDB.saveParam(dbFile,"frame_tol_pix", frameTolPix+"");
    		SqliteDB.saveParam(dbFile,"frame_tol_pct", frameTolPct+"");
    		SqliteDB.saveParam(dbFile,"min_frame_pix", minFramePix+"");  
    		SqliteDB.saveParam(dbFile,"seek_after_manual", seekAfterManual+"");
    		SqliteDB.saveParam(dbFile,"char_prop_tol", charPropTol+"");
    		SqliteDB.saveParam(dbFile,"vert_pix_match_coef", vertPixMatchCoef+"");    		
    		SqliteDB.saveParam(dbFile,"debug_level", debugLevel+"");
    		SqliteDB.saveParam(dbFile,"max_space_pix", maxSpacePix+"");
    		SqliteDB.saveParam(dbFile,"background_mode", bgMode+"");
    		SqliteDB.saveParam(dbFile,"vlc_timeout", vlcTimeout+""); 
    		SqliteDB.saveParam(dbFile,"vlc_retries", vlcRetries+""); 

    		if (generateOcrStats) {
        		SqliteDB.saveParam(dbFile,"gen_ocr_stats", "1");      			
    		}
    		else {
        		SqliteDB.saveParam(dbFile,"gen_ocr_stats", "0");  
    		}

    		if (chkNewVersion) {
        		SqliteDB.saveParam(dbFile,"check_new_version", "1");      			
    		}
    		else {
        		SqliteDB.saveParam(dbFile,"check_new_version", "0");  
    		}
    		
    		if (writeLog) {
        		SqliteDB.saveParam(dbFile,"write_log", "1");      			
    		}
    		else {
        		SqliteDB.saveParam(dbFile,"write_log", "0");  
    		}

    		
    		// pick color is saved when picked
    		//SqliteDB.saveParam(dbFile,"pick", pixel+"");
    	}
    	prndeb(5,"exit SaveVariables");
    }
    
    public static boolean CheckVariables() {
    	prndeb(5,"enter CheckVariables");
    	
    	String tmp="", msg="";
    	boolean ok=true;
    	
    	tmp=tolerance.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nInk Tolerance R should be a number";
    		ok=false;
    	}

    	tmp=toleranceB.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nInk Tolerance B should be a number";
    		ok=false;
    	}
    	
    	tmp=toleranceG.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nInk Tolerance G should be a number";
    		ok=false;
    	}    	
    	
    	tmp=minSpacePix.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nMin. Spc Pix should be a number";
    		ok=false;
    	}
    	
    	tmp=maxSpacePix+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nMax. Spc Pix should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<=toInt(minSpacePix.getText().trim()))  {
	    		msg=msg + "\n Max. Space Pix should be higher than Min. Space Pix";
	    		ok=false;
	    	}
    	}

    	tmp=tolOCR.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nOCR Tolerance should be a number";
    		ok=false;
    	}
    	else {
	    	if(toDouble(tmp)<0 || toDouble(tmp)>1)  {
	    		msg=msg + "\n OCR Tolerance value should be between 0 and 1";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxX.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL1 x (line one left position) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)>800)  {
	    		msg=msg + "\nL1 x (line left position) value should be between 0 and 800";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxY[0].getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL1 y (line 1 top position) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)>600)  {
	    		msg=msg + "\nL1 y (line 1 top position) value should be between 0 and 600";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxY[1].getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL2 y (line 2 top position) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)>600)  {
	    		msg=msg + "\nL2 y (line 2 top position) value should be between 0 and 600";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxY[1].getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL2 y (line 2 top position) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)>600)  {
	    		msg=msg + "\nL2 y (line 2 top position) value should be between 0 and 600";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxW.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL1 w (line width) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)+toInt(boxX.getText().trim())>800)  {
	    		msg=msg + "\n line left+width should be lower than 800";
	    		ok=false;
	    	}
    	}
    	
    	tmp=boxH.getText().trim();
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nL1 h (line height) should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)+toInt(boxY[0].getText().trim())>600)  {
	    		msg=msg + "\n line 1 top+height should be lower than 600";
	    		ok=false;
	    	}
	    	if(toInt(tmp)<0 || toInt(tmp)+toInt(boxY[1].getText().trim())>600)  {
	    		msg=msg + "\n line 2 top+height should be lower than 600";
	    		ok=false;
	    	}
    	}
    	
    	tmp=readDir+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nReading direction should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)!=0 && toInt(tmp)!=1)  {
	    		msg=msg + "\nAllowed reading direction values are 0 and 1";
	    		ok=false;
	    	}
    	}

    	tmp=bgMode+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nBackground Mode should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)!=0 && toInt(tmp)!=1)  {
	    		msg=msg + "\nAllowed background mode values are 0 and 1";
	    		ok=false;
	    	}
    	}

    	tmp=vlcTimeout+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nInk vlc_timeout should be a number";
    		ok=false;
    	}       	

    	tmp=vlcRetries+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nInk vlc_retries should be a number";
    		ok=false;
    	}  	
    	
    	/*
    	tmp=generateOcrStats+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nGenerate OCR Stats should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)!=0 && toInt(tmp)!=1)  {
	    		msg=msg + "\nAllowed generate OCR Stats values are 0 and 1";
	    		ok=false;
	    	}
    	}
    	*/

    	tmp=typeLastSub+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nLast sub type should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)!=0 && toInt(tmp)!=1)  {
	    		msg=msg + "\nAllowed last sub type values are 0 and 1";
	    		ok=false;
	    	}
    	}
    	
    	tmp=seekAfterManual+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nSeek after manual should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)!=0 && toInt(tmp)!=1)  {
	    		msg=msg + "\nAllowed seek after manual values are 0 and 1";
	    		ok=false;
	    	}
    	}
    	
    	tmp=step+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nStep frame should be a number";
    		ok=false;
    	}
    	
    	tmp=adjStep+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nAdjusted Step should be a number";
    		ok=false;
    	}
    	
    	tmp=adjSub+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nSub adjustment should be a number";
    		ok=false;
    	}
    	
    	tmp=step1+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nSlow step should be a number";
    		ok=false;
    	}
    	tmp=step5+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nFast step should be a number";
    		ok=false;
    	}
    	tmp=frameTolPix+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nFrame_tol_pix should be a number";
    		ok=false;
    	}
    	tmp=minFramePix+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nmin_frame_pix should be a number";
    		ok=false;
    	}
    	
    	tmp=frameTolPct+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\frame_tol_pct should be a number";
    		ok=false;
    	}
    	else {
	    	if(toDouble(tmp)<0 || toDouble(tmp)>1)  {
	    		msg=msg + "\nAllowed frame_tol_pct values are between 0 and 1";
	    		ok=false;
	    	}
    	}
    	
    	tmp=charPropTol+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nchar_prop_tol should be a number";
    		ok=false;
    	}
    	else {
	    	if(toDouble(tmp)<0 || toDouble(tmp)>1)  {
	    		msg=msg + "\nAllowed char_prop_tol values are between 0 and 1";
	    		ok=false;
	    	}
    	}
   
    	tmp=vertPixMatchCoef+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nvert_pix_match_coef should be a number";
    		ok=false;
    	}
    	else {
	    	if(toDouble(tmp)<0 || toDouble(tmp)>1)  {
	    		msg=msg + "\nAllowed vert_pix_match_coef values are between 0 and 1";
	    		ok=false;
	    	}
    	}    	
    	
    	tmp=debugLevel+"";
    	if(!isNumeric(tmp))  {
    		msg=msg + "\nDebug Level should be a number";
    		ok=false;
    	}
    	else {
	    	if(toInt(tmp)<0 || toInt(tmp)>10)  {
	    		msg=msg + "\nAllowed debug level values are 0 to 10";
	    		ok=false;
	    	}
    	}

    	if (!ok) {
    		msgBox(msg);
    	}
    	
    	prndeb(5,"exit CheckVariables");
    	
    	return ok;
    }
    
	public static void InitParamsDlg() {
		  prndeb(5,"enter InitParamsDlg");	
		  
	      // create a dialog Box 
	      paramPop = new JDialog(Main, "Advanced Settings",true);
	      paramPop.setModal(true);
	      //paramPop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	      paramPop.setPreferredSize(new Dimension(800, 800));
	      paramPop.setLocation(100,50);

	      JLayeredPane pane = new JLayeredPane();
	      pane.setLayout(null);
	      paramPop.add(pane);
	      pane.setSize(800, 800);     

	      String[] columnNames = {"Name", "Description", "Value"};	      	      	            
	      Object[][] data = SqliteDB.GetParamsTable(dbFile);
	      
	      TableModel model = new DefaultTableModel(data, columnNames)
	      {
	        public boolean isCellEditable(int row, int column)
	        {
	        	if (column!=2)
	        		return false;	//This causes all cells to be not editable
	        	else
	        		return true;	//This causes all cells to be editable
	        }
	      };
	      
	      paramTable = new JTable(model);
	      TableColumn column = null;
          column = paramTable.getColumnModel().getColumn(0);
          column.setPreferredWidth(160); 
          column = paramTable.getColumnModel().getColumn(1);
          column.setPreferredWidth(500); 
          column = paramTable.getColumnModel().getColumn(2);
          column.setPreferredWidth(100); 
	      
	      JScrollPane scrollPane = new JScrollPane(paramTable);
	      paramTable.setFillsViewportHeight(true);    
	      scrollPane.setBounds(10,30,760,600);
	      
	      pane.add(scrollPane);

	      JButton btnOk = new JButton();
	      btnOk.setText("Save");
	      btnOk.setBounds(10,700,80,30);
	      btnOk.setMargin(new Insets(2, 2, 2, 2));	          
	      
	      JButton btnCancel = new JButton();
	      btnCancel.setText("Cancel");
	      btnCancel.setBounds(100,700,80,30);
	      btnCancel.setMargin(new Insets(2, 2, 2, 2));          


	      pane.add(btnOk,0);
	      pane.add(btnCancel,0);     
	      
	      btnOk.addActionListener(new ActionBtnParamsOK());
	      btnCancel.addActionListener(new ActionBtnParamsCancel());
	      
	      // set visibility of dialog 
	      paramPop.pack();
	      paramPop.setVisible(true); 

	      prndeb(5,"exit InitParamsDlg");
	}	
	    
	public static class ActionBtnParamsOK implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {
	  		for (int i=0; i<paramTable.getRowCount();i++) {
	  			//prndeb(7,"value for " + paramTable.getModel().getValueAt(i,0)+": " + paramTable.getModel().getValueAt(i,2));
	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("pick")) {
	  				SetPickColor(toInt(paramTable.getModel().getValueAt(i,2).toString())); 
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("toleranceR")) {
	  				tolerance.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("toleranceG")) {
	  				toleranceG.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("toleranceB")) {
	  				toleranceB.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("min_spc_pix")) {
	  				minSpacePix.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("char_tol")) {
	  				tolOCR.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("line_x")) {
	  				boxX.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("line_h")) {
	  				boxH.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("line_w")) {
	  				boxW.setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("line_y_1")) {
	  				boxY[0].setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("line_y_2")) {
	  				boxY[1].setText(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("step_frame")) {
	  				step=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("step_adjustment")) {
	  				adjStep=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("sub_adjustment")) {
	  				adjSub=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("type_last_sub")) {
	  				typeLastSub=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("step_slow")) {
	  				step1=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("step_fast")) {
	  				step5=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("frame_tol_pix")) {
	  				frameTolPix=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("frame_tol_pct")) {
	  				frameTolPct=toDouble(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("char_prop_tol")) {
	  				charPropTol=toDouble(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("min_frame_pix")) {
	  				minFramePix=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("seek_after_manual")) {
	  				seekAfterManual=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("read_dir")) {
	  				readDir=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("vert_pix_match_coef")) {
	  				vertPixMatchCoef=toDouble(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("debug_level")) {
	  				debugLevel=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("max_space_pix")) {
	  				maxSpacePix=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("background_mode")) {
	  				bgMode=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}

	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("vlc_timeout")) {
	  				vlcTimeout=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}	  			

	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("vlc_retries")) {
	  				vlcRetries=toInt(paramTable.getModel().getValueAt(i,2).toString());
	  			}	  		  			
	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("gen_ocr_stats")) {
	  	    		if (toInt(paramTable.getModel().getValueAt(i,2).toString())==0) {    			
	  	    			generateOcrStats=false;
	  	    		}
	  	    		else {
	  	    			generateOcrStats=true;
	  	    		}
	  			}

	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("check_new_version")) {
	  	    		if (toInt(paramTable.getModel().getValueAt(i,2).toString())==0) {    			
	  	    			chkNewVersion=false;
	  	    		}
	  	    		else {
	  	    			chkNewVersion=true;
	  	    		}
	  			}	  			
	  			
	  			if(paramTable.getModel().getValueAt(i,0).toString().contentEquals("writeLog")) {
	  	    		if (toInt(paramTable.getModel().getValueAt(i,2).toString())==0) {    			
	  	    			writeLog=false;
	  	    		}
	  	    		else {
	  	    			writeLog=true;
	  	    		}
	  			}	  				    			    		

	  		}	// end for (int i=0; i<paramTable.getRowCount();i++)	  		

    		if (CheckVariables()) {
    			SaveVariables();
        		PaintReadDirButton();	    		
        		SetCaptureBox();    			
    			paramPop.dispose();
    		}
	  		
	  	} // close actionPerformed
	  	
	}	//close ActionBtnParamsOK 	
	
	public static class ActionBtnParamsCancel implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
    		if (CheckVariables()) {
    			paramPop.dispose();
    		}
	  	} // close actionPerformed
	  	
	}	//close ActionBtnParamsCancel 	
	
	public static void SetPickColor() {
  		
  		if(actMode=="Pick") {
  			actMode="Manual";
  		}
  		else {  			
  			//captureBox[0].setVisible(false);
  			//captureBox[1].setVisible(false);
  			 			
  			int x,y,w,h;
  			int xFrom,yFrom,wFrom,hFrom, yFromOffset;
  			int size=2;
  			
  			xFrom=captureBox[0].getX();
  			yFrom=captureBox[0].getY();
  			wFrom=captureBox[0].getWidth();
  			hFrom=captureBox[1].getY()-captureBox[0].getY()+captureBox[1].getHeight();  			 			
  			
  			w=captureBox[0].getWidth()*size;
  			x=(1600-w)/2;
  			y=20;  			
  			h=(captureBox[1].getY()-captureBox[0].getY()+captureBox[1].getHeight())*size;
  			
  			// create a dialog Box 
  			JDialog popPicker = new JDialog(Main, "Ink Picker",true);
  			popPicker.setModal(true);
  			//popPicker.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
  			popPicker.setPreferredSize(new Dimension(w+10, h+40));
  			popPicker.setLocation(x,400);
  			
  			if(image1.getImage()==null) {
  				actMode="Manual";
  				msgBox("You need an image on the OCR Panel");
  			}
  			else {  			
  				actMode="Pick";
  		  		PaintStatusBar();
  		  		
  		  		yFromOffset = (600 - image1.getImage().getHeight()) / 2;	// image height correction when image proportions not 800x600
  		  		
  		  		//prndeb(1, "image w,h=" + image1.getImage().getWidth() + "," + image1.getImage().getHeight() + " , " + yFromOffset);
  		  		//prndeb(1, "w,h=" + w + "," + h);
  		  		//prndeb(1, "xFrom, yFrom, wFrom, hFrom=" + xFrom + "," + yFrom + "," + wFrom + "," + hFrom);
  		  		
	  	        //------- PICKER ----------
	  	        picker = new ImagePanel();
	  	        picker.setBounds(5,5,w,h);	        
	  			picker.loadBuffer(image1.getImage().getSubimage(xFrom, yFrom-yFromOffset, wFrom, hFrom), w, h);
	  			picker.setVisible(true);
	  			
	  			popPicker.add(picker);
	  			
	  			popPicker.addWindowListener(new WindowAdapter() {	// capture dialog closing event
				    @Override
				    public void windowClosing(WindowEvent e) {
				    	actMode="Manual";
				  		PaintStatusBar();
				    }
				});
	  			
	  	        picker.addMouseListener(new MouseAdapter() {		// capture ink selection event
	  	            @Override
	  	            public void mousePressed(MouseEvent e) {
	  	            	
	  	            	prndeb(7,"Picker mouse Event at " + picker.getMousePosition().x + "," + picker.getMousePosition().y);
	  	            	
	  	            	switch(actMode) {
	  	            	  case "Pick":
	  	            		  
	  	            		  // gets selected color
	  	            		  int pixel = picker.getPixelColor(picker.getMousePosition().x,picker.getMousePosition().y);
	  	            		  
	  	            		  SetPickColor(pixel);      // stores selected color
	  	            		  
	  	            		  ReadBoxV2();				// repaint captured box
	  	            		  
	  	            	    break;
	  	            	  case "Box":
	  	            	    // code block
	  	            	    break;
	  	            	  default:
	  	            	    // code block
	  	                    //int x = image1.getPixelColor(image1.getMousePosition().x,image1.getMousePosition().y);
	  	            		  
	  	            	}
	  	            }
	  	        });
	  			
	  			popPicker.pack();
	  			popPicker.setVisible(true);
  			}
  		}
  		
  		PaintStatusBar();
	}
	
  	//***************************************** PLAYER FUNCTIONS *****************************************************    

  	public static void setTimer() {
  	    TimerTask task = new TimerTask() {
  	        public void run() {
  	            //System.out.println("Task performed on: " + new Date() + "n" + "Thread's name: " + Thread.currentThread().getName());
  	        	vidSlid.setValue((int) playerCmpt.mediaPlayer().status().time());  					
		  	    PaintStatusBar();
		  	    
		  	    if(playerStatus == 1)
		  	    	setTimer();								
  	        }
  	    };
  	    Timer timer = new Timer("Timer");
  	    
  	    long delay = 1000L;
  	    timer.schedule(task, delay); 	  	    
  	}

  	public static void PlayerOpenFile(String file) {
  		
  		playerCmpt.mediaPlayer().media().startPaused(file);
		PlayerPause();

		// wait until player starts
			try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			prndeb(1,"ERROR: sleep" + e.getMessage());
		}
			
		PlayerPause();
  		vidFile=file;
  	}
  	
  	public static void PlayerPlay() {
		playerCmpt.mediaPlayer().controls().play();
		playerCmpt.mediaPlayer().audio().setVolume(5);
		playerStatus=1;
  	}

  	public static void PlayerStop() {
		playerCmpt.mediaPlayer().controls().stop();
		playerStatus=0;
  	}

  	public static void PlayerPause() {
  		playerCmpt.mediaPlayer().controls().pause();
  		if (playerStatus==1)
	  		playerStatus=0;
  		else {
	  		playerStatus=1;
	  		setTimer();
  		}
  	}
  	
  	public static void PlayerGoTo (int pos) {
        prndeb(5,"enter PlayerGoTo: " + pos);

        if (pos>=0 && pos<=PlayerGetEndPos()) {
        	playerCmpt.mediaPlayer().controls().setTime(pos);
        	PaintStatusBar();
        }
        
        prndeb(5,"exit PlayerGoTo");        
	}
  	
  	public static int PlayerGetCurPos() {
        prndeb(5,"enter PlayerGetCurPos");
        
        int tmp= (int) playerCmpt.mediaPlayer().status().time();
        
        prndeb(5,"exit PlayerGetCurPos");     		
        return tmp;
  	}

  	public static int PlayerGetEndPos() {
        prndeb(5,"enter PlayerGetEndPos");
        
        int tmp= (int) playerCmpt.mediaPlayer().status().length();
        
        prndeb(5,"exit PlayerGetEndPos");     		
  		return tmp;
  	}

  	//***************************************** OCR FUNCTIONS *****************************************************    

  	public static String getImgStr(BufferedImage image) {				// converts image into string of pixels
  		String tmp="";
  		
    	int w=image.getWidth();
    	int h=image.getHeight();
    	  	
    	for(int j = 0; j < h; j++) {
       	    for(int i = 0; i < w; i++) {
   	    		if(image.getRGB(i,j)==Color.WHITE.getRGB()) 
   	    			tmp=tmp + "X";
   	    		else
   	    			tmp=tmp + "-";
    	    }
    	}
   	    
  		return tmp;
  	}

  	public static BufferedImage imgTrim(BufferedImage image) {			// removes upper and lower black lines from image
  		//removes black lines on top and bottom of the image  		
    	int w=image.getWidth();
    	int h=image.getHeight();    	    	
    	
    	int i, first=0, last=0;
    	
    	int[] hasWhite = new int[h];
    	
    	hasWhite = DetectLines(image);
    	
    	for(i=0;i<h;i++) {
    		if(hasWhite[i]!=0 && first==0)
    			first=i;
    	}

    	for(i=h-1;i>=0;i--) {
    		if(hasWhite[i]!=0 && last==0)
    			last=i;
    	}
    	
    	image=image.getSubimage(0, first, w, last-first+1);
    	yPos = first;		// returns y position for the ocr database  	
    	
  		return image;
  	}
  	
    public static BufferedImage initImage(int w, int h) {
    	
    	//System.out.println("init img");
    	
    	//int w = captureBox.getBounds().width;
    	//int h = captureBox.getBounds().height;
    	int type = BufferedImage.TYPE_INT_ARGB;

    	BufferedImage image = new BufferedImage(w, h, type);

    	int color = Color.CYAN.getRGB(); // RGBA value, each component in a byte

    	for(int x = 0; x < w; x++) {
    	    for(int y = 0; y < h; y++) {
    	        image.setRGB(x, y, 0);
    	    }
    	}

    	//System.out.println("init img out");
    	
    	return image;
    }
    
    public static int[] DetectColumns(BufferedImage image) {
    	    	
    	int w=image.getWidth();
    	int h=image.getHeight();
    	
    	int first=0;
    	
    	int[] hasWhite = new int[w];
    	
    	
   	    for(int i = 0; i < w; i++) {
    		hasWhite[i]=0;
   	    	for(int j = 0; j < h; j++) {    	    	
   	    		if(image.getRGB(i,j)==Color.WHITE.getRGB()) {
   	   	    		//prndeb(10, w + "," + h + "," + j + "," + i + "," + image.getRGB(i,j) + "," + Color.WHITE.getRGB());
   	   	    		
   	   	    		if (first==0) {
   	   	    			first=1;
   	   	    			prndeb(10, "Detect Column : found fist white pixel - " + w + "," + h + "," + j + "," + i + "," + image.getRGB(i,j) + "," + Color.WHITE.getRGB());
   	   	    		}

   	   	    		hasWhite[i]=hasWhite[i]+1;
   	    		}   	    		
    	    }
    	}
   	    
   	    for(int j = 0; j < w; j++) {
   	    	//System.out.println("line "+ j + "=" +hasWhite[j]);   	    	    	    
    	}
   	    
   	    return hasWhite;
    }
    
    public static int[] DetectLines(BufferedImage image) {
    	
    	int w=image.getWidth();
    	int h=image.getHeight();
    	
    	int first=0;
    	
    	int[] hasWhite = new int[h];
    	
   	    for(int j = 0; j < h; j++) {
    		hasWhite[j]=0;
   	    	for(int i = 0; i < w; i++) {    	    	
   	    		if(image.getRGB(i,j)==Color.WHITE.getRGB()) {
   	   	    		//System.out.println(j + "," + i + "," + image.getRGB(i,j) + "," + Color.WHITE.getRGB());
   	   	    		
   	    			if (first==0) {
   	    				first=1;
   	    				prndeb(7, "Detect Line : found fist white pixel - " + w + "," + h + "," + j + "," + i + "," + image.getRGB(i,j) + "," + Color.WHITE.getRGB());
   	    			}
   	    			
   	    			hasWhite[j]=hasWhite[j]+1;
   	    		}   	    		
    	    }
    	}
   	 /*   
   	    for(int j = 0; j < h; j++) {
   	    	System.out.println("line "+ j + "=" +hasWhite[j]);   	    	    	    
    	}
    */
   	    return hasWhite;
    }
  	
  	public static void SetCaptureBox() {			// sets box based on param coordinates and paints it
  		/*
  		if(actMode.compareTo("Box")==0) {
  			actMode="";
  			captureBox[0].setVisible(false);
  			captureBox[1].setVisible(false);
  		}
  		else {
  			actMode="Box";
  			//System.out.println("box dim=" + toInt(boxX[.getText()) +","+ toInt(boxY.getText()) +","+toInt(boxW.getText())+","+toInt(boxH.getText()));
  			 * 
  			 */
  		
  		if (CheckVariables()) {
  			for (int i=0;i<2;i++) {
	  			captureBox[i].setBounds(toInt(boxX.getText()) , toInt(boxY[i].getText()) ,toInt(boxW.getText()),toInt(boxH.getText()));
		  		captureBox[i].setVisible(true);
		  		captureBox[i].repaint();
  			}
  		}
  			/*
  		}
  		lblStatus.setText("Mode: " + actMode);
  		*/
  	}
  	
  	public static boolean CopyFrame() {				// copies the frame image from the player to the right pane
        prndeb(5,"enter CopyFrame");
        
        bufImg = null;
        
        boolean rslt=true;
        
        int timeOut = 2;
        int numRetry = 2;
        int cont = 1;
        
  		//bufImg = playerCmpt.mediaPlayer().snapshots().get();
        
        while (bufImg==null && cont<=numRetry) {
	        //New executor to prevent api from blocking
	        ExecutorService executor = Executors.newCachedThreadPool();
	        Callable<BufferedImage> task = new Callable<BufferedImage>() {
	           public BufferedImage call() {
	        	   return playerCmpt.mediaPlayer().snapshots().get();
	           }
	        };
	        Future<BufferedImage> future = executor.submit(task);
	        try {
	            prndeb(5,"calling future, try " + cont);
	
	        	bufImg = future.get(2, TimeUnit.SECONDS); 
	        }
	        catch (TimeoutException ex) {
	           // handle the timeout
	           prndeb(1,"CopyFrame Time Out at Pos: " + playerCmpt.mediaPlayer().status().time() + " Time: " + FormatTime((int) playerCmpt.mediaPlayer().status().time()));           
	           rslt=false;
	        } 
	        catch (InterruptedException e) {
	           // handle the interrupts
	        }
	        catch (ExecutionException e) {
	           // handle other exceptions
	        }
	        finally {
	           future.cancel(true); // may or may not desire this
	        }
	        // end New executor
	        
	        cont++;
	  		prndeb(7,"after snapshot");
        }  		
  		
  		if(bufImg!=null) {
	  		image1.loadBuffer(bufImg, 800, 600);
	  		image1.repaint();
	  		prndeb(10,"snapshot copyed to buffer");
  		}
  		else {
  			rslt = false;
  			prndeb(1,"CopyFrame: snapshot image is null");
  		}
  		
        prndeb(5,"exit CopyFrame");
        return rslt;
  	}

  	public static int ReadBoxV1() {					// DEPRECATED - captures lines from frame in black&white - returns only number of pixels
        prndeb(5,"enter ReadBox");
        
  		int offset = image1.getBounds().y;
  		int x = captureBox[0].getBounds().x;
  		int y[] = new int[2];
  		y[0] = captureBox[0].getBounds().y;
  		y[1] = captureBox[1].getBounds().y;
    	int w = captureBox[0].getBounds().width;
    	int h = captureBox[0].getBounds().height;
    	int countWhites=0;
    	String imgStr="";

  		prndeb(7,"readbox: x=" + x + ", y0=" + y[0] + ", w=" + w + ", h=" + h + ", off=" + offset);	    	
    	
    	captBox[0].setBounds(x+800,captBox[0].getBounds().y,w+2,h+2);
    	captBox[1].setBounds(x+800,captBox[0].getBounds().y+h+2,w+2,h+2);
	
    	for (int l = 0; l < 2; l++) {			// for each image line
    		
	    	captImg=initImage(w+2,h+2);

	    	for(int j = 0; j < h+2; j++) {	// add first and last black columns
	    		captImg.setRGB(0, j, Color.BLACK.getRGB());	    
	    		captImg.setRGB(w+1, j, Color.BLACK.getRGB());
	    	}
	    	
	    	for(int i = 0; i < w+2; i++) {	// add first and last black lines
	    		captImg.setRGB(i, 0, Color.BLACK.getRGB());
	    		captImg.setRGB(i, h+1, Color.BLACK.getRGB());	    
	    	}

	    	for(int i = 0; i < w; i++) {		// for each pixel in width
	    	    for(int j = 0; j < h; j++) {	// for each pixel in height
	    	  		prndeb(10,"readbox: " + x + ',' + y[l] + ',' + w + "," + h + "," + offset + " => " + i + "," + j);	    	
	    	    	int pixel = image1.getPixelColor(x+i,y[l]+j-offset);

	    	    	int red = (pixel >> 16) & 0xff;
	    	    	int green = (pixel >> 8) & 0xff;
	    	    	int blue = (pixel) & 0xff;

	    	    	int tolR= toInt(tolerance.getText());
	    	    	int tolG= toInt(toleranceG.getText());
	    	    	int tolB= toInt(toleranceB.getText());
	    	    	
	    	    	Color pck = pickColor.getBackground();
	    	    	
	    	    	if (red>pck.getRed()-tolR && red<pck.getRed()+tolR && 
	    	    		blue>pck.getBlue()-tolB && blue<pck.getBlue()+tolB &&	
	    	    		green>pck.getGreen()-tolG && green<pck.getGreen()+tolG
	    	    			) {   	    	
	    	    		//captImg.setRGB(i, j, Color.RED.getRGB());
	    	    		captImg.setRGB(i+1, j+1, Color.WHITE.getRGB());
		    	  		prndeb(10,"OK colors: " + red + ',' + green + ',' + blue + " - " + pck.getRed() + "," +  pck.getGreen() + "," +  pck.getBlue());	    		    	    	
	    	    		countWhites++;
	    	    		imgStr = imgStr + "X";
	    	    	}
	    	    	else {
	    	    		captImg.setRGB(i+1, j+1, Color.BLACK.getRGB());
	    	    		//captImg.setRGB(i, j, pixel);
		    	  		prndeb(10,"KO colors: " + red + ',' + green + ',' + blue + " - " + pck.getRed() + "," +  pck.getGreen() + "," +  pck.getBlue());
	    	    		imgStr = imgStr + "-";
	    	    	}
	    	    }
	    	}

	    	captBox[l].setIcon(new ImageIcon(captImg));
	    	captBox[l].repaint();
    	}
    	
    	prndeb(9,"White Pixels: " + countWhites);
    	
  		CleanBox();
    	
        prndeb(5,"exit ReadBox");
        
    	return countWhites;    	
  	}

  	public static String[] ReadBoxV2() {			// captures lines from frame in black&white- returns number of pixels and image string
        prndeb(5,"enter ReadBox");
                
  		int offset = image1.getBounds().y;
  		int x = captureBox[0].getBounds().x;
  		int y[] = new int[2];
  		y[0] = captureBox[0].getBounds().y;
  		y[1] = captureBox[1].getBounds().y;
    	int w = captureBox[0].getBounds().width;
    	int h = captureBox[0].getBounds().height;
    	int countWhites=0;
    	String imgStr="";
    	String imgStrLine[]={"",""};
    	String rslt[]={"0","","",""};		// result => pos 0 is number of whites, pos1 is the img string, pos 2 is the image string line 1, pos 3 is the image string line 2 

  		
        if(image1.getPixelColor(0,0)==-9999) {		// if no image loaded
        	prndeb(4,"Readbox: no image to read");
        	msgBox("No image loaded");
        }
        else {	
	    	prndeb(7,"readbox: x=" + x + ", y0=" + y[0] + ", w=" + w + ", h=" + h + ", off=" + offset);	    	
	    	
	    	captBox[0].setBounds(800+x,captBox[0].getBounds().y,w+2,h+2);
	    	captBox[1].setBounds(800+x,captBox[0].getBounds().y+h+2,w+2,h+2);
		
	    	for (int l = 0; l < 2; l++) {			// for each image line
	    		
		    	captImg=initImage(w+2,h+2);
	
		    	for(int j = 0; j < h+2; j++) {	// add first and last black columns
		    		captImg.setRGB(0, j, Color.BLACK.getRGB());	    
		    		captImg.setRGB(w+1, j, Color.BLACK.getRGB());
		    	}
		    	
		    	for(int i = 0; i < w+2; i++) {	// add first and last black lines
		    		captImg.setRGB(i, 0, Color.BLACK.getRGB());
		    		captImg.setRGB(i, h+1, Color.BLACK.getRGB());	    
		    	}

	    	    for(int j = 0; j < h; j++) {	// for each pixel in height
	    	    	for(int i = 0; i < w; i++) {		// for each pixel in width
		    	  		prndeb(10,"readbox: " + x + ',' + y[l] + ',' + w + "," + h + "," + offset + " => " + i + "," + j);	    	
		    	    	int pixel = image1.getPixelColor(x+i,y[l]+j-offset);
	
		    	    	int red = (pixel >> 16) & 0xff;
		    	    	int green = (pixel >> 8) & 0xff;
		    	    	int blue = (pixel) & 0xff;
	
		    	    	int tolR= toInt(tolerance.getText());		    	    	
		    	    	int tolG= toInt(toleranceG.getText());
		    	    	int tolB= toInt(toleranceB.getText());
		    	    	
		    	    	Color pck = pickColor.getBackground();
		    	    	
		    	    	if (red>pck.getRed()-tolR && red<pck.getRed()+tolR && 
		    	    		blue>pck.getBlue()-tolB && blue<pck.getBlue()+tolB &&	
		    	    		green>pck.getGreen()-tolG && green<pck.getGreen()+tolG
		    	    	
		    	    			) {   	    	
		    	    		//captImg.setRGB(i, j, Color.RED.getRGB());
		    	    		captImg.setRGB(i+1, j+1, Color.WHITE.getRGB());
			    	  		prndeb(10,"OK colors: " + red + ',' + green + ',' + blue + " - " + pck.getRed() + "," +  pck.getGreen() + "," +  pck.getBlue());	    		    	    	
		    	    		countWhites++;
		    	    		imgStr = imgStr + "X";
		    	    		imgStrLine[l] = imgStrLine[l] + "X";
		    	    	}
		    	    	else {
		    	    		captImg.setRGB(i+1, j+1, Color.BLACK.getRGB());
		    	    		//captImg.setRGB(i, j, pixel);
			    	  		prndeb(10,"KO colors: " + red + ',' + green + ',' + blue + " - " + pck.getRed() + "," +  pck.getGreen() + "," +  pck.getBlue());
		    	    		imgStr = imgStr + "-";
		    	    		imgStrLine[l] = imgStrLine[l] + "-";
		    	    	}
		    	    }
		    	}
	
		    	captBox[l].setIcon(new ImageIcon(captImg));
		    	captBox[l].repaint();
	    	}
	    	
	        rslt[0]=countWhites+"";
	        rslt[1]=imgStr;
	        rslt[2]=imgStrLine[0];
	        rslt[3]=imgStrLine[1];	        
	        
	    	prndeb(9,"White Pixels: " + countWhites);
	    	prndeb(10,"Image String: " + countWhites);
	    	
	  		CleanBox();
        }
        
        prndeb(5,"exit ReadBox");
        
        lastReadBox=rslt;
        
    	return rslt;    	
  	}
  	
  	public static void CleanBox() {					// removes noise from the captured box
        prndeb(5,"enter cleanBox");
        
        BufferedImage clnImg;
        int cnt = 0;
        int chkX1, chkX2, chkY1, chkY2;
        
    	for (int l = 0; l < 2; l++) {			// for each captured image line
      		int w = captBox[l].getBounds().width;
      		int h = captBox[l].getBounds().height;
      		
	    	ImageIcon icon = (ImageIcon)captBox[l].getIcon();
    		clnImg =  ((BufferedImage) icon.getImage());
    		
	    	for(int i = 0; i < w; i++) {		// for each pixel in width
	    	    for(int j = 0; j < h; j++) {	// for each pixel in height    	

	    	    	int pixel = clnImg.getRGB(i,j);
	    	    	//prndeb(10,"pixel on " + i + "," + j + " - col " + pixel + " vs white " + Color.WHITE.getRGB());
	    	    
	    	    	if(pixel==Color.WHITE.getRGB()) {	// when white point, then erase if isolated
	    	    		cnt=0;
	    	    		chkX1=i-1;
	    	    		chkX2=i+1;
	    	    		chkY1=j-1;
	    	    		chkY2=j+1;
	    	    		
	    	    		if (chkX1<0) chkX1=0;
	    	    		if (chkY1<0) chkY1=0;
	    	    		if (chkX2>w-1) chkX2=w-1;
	    	    		if (chkY2>h-1) chkY2=h-1;
	    	    		
	    		    	for(int i1 = chkX1; i1 <= chkX2; i1++) {		// for each pixel in width
	    		    	    for(int j1 = chkY1; j1 <= chkY2; j1++) {	// for each pixel in height   
	    		    	    	int pixel2 = ((BufferedImage) icon.getImage()).getRGB(i1,j1);
	    		    	    	if(pixel2==Color.WHITE.getRGB()) cnt++;	    		    	    	
	    		    	    }
	    		    	}
	    		    	
	    		    	prndeb(10,"count for pixel on " + i + "," + j + ": " + cnt);
	    		    	
	    		    	if (cnt==1) {	// if only one white pixel then clean it	
	    		    		clnImg.setRGB(i,j,Color.BLACK.getRGB());
	    		    		prndeb(9,"found pixel to clean on " + i + "," + j);
	    		    	}
	    	    	}
	    	    	
	    	    }	// end for j
	    	} // end for i
	    	
	    	captBox[l].setIcon(new ImageIcon(clnImg));
	    	captBox[l].repaint();
	    	
    	}	// end for l
        prndeb(5,"exit cleanBox");
  	}
  	
  	public static String ReadSub() {				// reads subtitle on current position
        prndeb(5,"enter ReadSub");
        
  		String chr, imgStr, readSub="";
  		String txtFile = srtFile;
  		String ocrRslt[];
  		int lFrom, lTo;
  		
  		if(!actMode.contentEquals("Auto")) {		// if not in auto mode then opens db connection
  			dbcn = SqliteDB.dbConnect(dbFile);	//create DB connection
  			SqliteDB.addCustomFunction(dbcn);	// add custom function for binary comparison 
  		}
  		
  		
  readingFrame: for (int l=0; l<2; l++) {										// for each subtitle line
	  
	  		if (GetSampleLine(lastReadBox[1],l,5)[1].contentEquals("-1")) {		// if the line is empty	  			
	  			prndeb(4,"Read sub, skipping line " + l + " that is empty");
	  		}
	  		else { 																// if the line is not empty

	  		   // initializes line variables
	  		   int[] lines, columns;		  	   
		  	   int firstPoint=0;
		  	   int secondPoint=0;	   
		  	   int topPoint=0;
		  	   int bottomPoint=0;
		  	   int prevPoint=0;
		  	   int lastValidChar=0;
		  	   
		  	   int minPix = toInt(minSpacePix.getText());
		  	   if (minPix==0) 
		  		   minPix=6;
		  	   
		  	   double tol = toDouble(tolOCR.getText());
		  	   if (tol==0) 
		  		 tol=.95;
		  	   
		  	   //initializes pop up dialog
		  	   popAction="";
		  	   InitPopDlg();
		  	   
		  	   //Retrieve line image
		 	   ImageIcon icon = (ImageIcon)captBox[l].getIcon();
		 	   if(icon==null) {
		 		   prndeb(1,"ERROR: No image available for reading sub");
		 		   break;
		 	   }
			   BufferedImage img = (BufferedImage)((Image) icon.getImage());
			   
		  	   seekStatus.setText("Reading subtitle ");
		  	   seekStatus.setVisible(true);
		  	   seekStatus.paintImmediately(seekStatus.getVisibleRect());		  	   		  	   		 	   
			  	    		  	   
		  	   lines = DetectLines(img);
		  	   columns = DetectColumns(img);
	
		  	   //calculates real text line start/end
		  	   String strLine = GetSampleLine(lastReadBox[1],l,1)[0];	//gets string sample line
		  	   String curChar="";
		  	   int rightStart=strLine.length()-1, leftStart=0;
		  	   int lastWhite=strLine.length()/2;
		  	   for(int i=strLine.length()/2; i<strLine.length();i++) {		// gets the end of the line from the middle to the right
		  		   curChar=strLine.charAt(i)+"";
		  		   if(curChar.contentEquals("X")) {
		  			   lastWhite=i;
		  		   }
		  		   else {
		  			   if(i-lastWhite>maxSpacePix) {
		  				   rightStart=i;	// uses i and not lastWhite to ensure not missing chars not having white points in the middle of the line (dot, comma...)
		  				   break;
		  			   }
		  		   }
		  	   }
		  	   
		  	   lastWhite=strLine.length()/2;
		  	   for(int i=strLine.length()/2; i>=0;i--) {		// gets the end of the line from the middle to the right
		  		   curChar=strLine.charAt(i)+"";
		  		   if(curChar.contentEquals("X")) {
		  			   lastWhite=i;
		  		   }
		  		   else {
		  			   if(lastWhite-i>maxSpacePix) {
		  				   leftStart=i;	// uses i and not lastWhite to ensure not missing chars not having white points in the middle of the line (dot, comma...)
		  				   break;
		  			   }
		  		   }
		  	   }

		  	   prndeb(4, "Line " + l + ": found left start at " + leftStart + " and right start at " + rightStart);
		  	   		  	   
		  	   // define topPoint
		  	   for (int i=0; i<lines.length;i++) {
		  		   if (lines[i]!=0 & topPoint==0){
		  			   topPoint=i;
		  		   } 
		  	   }		  	   
	
		  	   // define bottomPoint
		  	   for (int i=lines.length-1;i>=0;i--) {
		  		   if (lines[i]!=0 & bottomPoint==0){
		  			   bottomPoint=i;
		  		   } 
		  	   }		  	   	  	   	  	   		  	   	  	   
	
		  	  boolean iCond=true;
		  	  int i, iEnd, iStep;
		  	  if(readDir==0) {		// if Left -> Right
		  		//i=0;
		  		//iEnd=columns.length-2;
		  		iStep = 1;		
		  		
		  		i=leftStart;
		  		iEnd=rightStart;
		  	  }
		  	  else {
		  		//i=columns.length-1;
		  		//iEnd=0;
		  		iStep = -1;	  	
		  		
		  		i=rightStart;
		  		iEnd=leftStart;
		  	  }
		  	  
		// readingLine: for (int i=columns.length-1; i>=0;i--) {
		  	 readingLine: while (iCond) {
		  		 
		  		   if (columns[i]!=0 && firstPoint==0){	
		  			   //when finds first white point
		  			   firstPoint=i;
		  			   
		  			   prndeb(9, "first point found at " +i);
		  		   } 
		  		   else {									
		  			   if ((columns[i]==0 || i==iEnd) && firstPoint!=0 && secondPoint==0) {
		  				   //when finds the end of the character
		  				   secondPoint=i;
		  				
		  				   prndeb(9, "second point found at " +i);
		  				   /*
		  				   if(prevPoint-firstPoint>minPix) {
		  					   // if more than x pixels between previous char then add space
		  					   readSub=readSub + " ";
		  				   }
		  			   		*/
		  				   
		  				   BufferedImage chrImg;
		  				   if (readDir==0) {	// if left to right
		  					   chrImg = img.getSubimage(firstPoint, topPoint-1, secondPoint-firstPoint+1, bottomPoint-topPoint+2);
	
		  					   // set red rectangle on the current character
			  				   charBox.setBounds(captBox[l].getX()+firstPoint-1,captBox[l].getY()+topPoint-1,secondPoint-firstPoint+3, bottomPoint-topPoint+2);
		  				   }
		  				   else {
		  					   chrImg = img.getSubimage(secondPoint, topPoint-1, firstPoint-secondPoint+1, bottomPoint-topPoint+2);
		  					   
			  				   // set red rectangle on the current character
			  				   charBox.setBounds(captBox[l].getX()+secondPoint-1,captBox[l].getY()+topPoint-1,firstPoint-secondPoint+3, bottomPoint-topPoint+2);
		  					   
		  				   }
		  				   
		  				   charBox.setVisible(true);
		  				   charBox.repaint();
		  				   
		  				   prndeb(7,"found char (x,y,h,w): " + secondPoint + "," + topPoint + "," + (firstPoint-secondPoint+1) + "," + (bottomPoint-topPoint+1));
			  						  				
		  				   chrImg=imgTrim(chrImg);		//removes black lines on top and bottom (and sets yPos as the top pixel)
			  				
		  				   imgStr=getImgStr(chrImg);	//transforms img into str
			  				
		  				   prndeb(7,"yPos = " + yPos + ", topPoint=" + topPoint + ", line=" + l);
			  		   	   yPos=topPoint+yPos;
	
			  		   	   ocrRslt = SqliteDB.dbExistStrTolV2(dbcn, chrImg.getWidth(), chrImg.getHeight(), imgStr, tol, minPix, yPos);	// search for char in DB with tolerance of 95%
			  		   	   chr=ocrRslt[0];	
			  				
			  		   	   if (chr!=null) {	// if found in DB then use the char
			  					//readSub=readSub + chr;
			  		   	   }
			  		   	   else {				// if not found in DB then 
			  		   		   
			  		   		   if (interactiveMode || !actMode.contentEquals("Auto")) {		//when manual or interactive ask the user for the new char		  		   		   
				  		   		   
				  					ShowPopDlg(chrImg, ocrRslt);
				  				
					  	            while (popOn) {
					  	                // waiting
					  	                try {
											Thread.sleep(1000);
					  	                	} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
					  	          			prndeb(1,"ERROR: sleep" + e1.getMessage());
					  	                	}   // you could remove this as well
					  	            }
			  		   		   }
			  		   		   else {					// if not interactive mode, set the char as error
			  		   			   popAction = "";
			  		   			   popReadChar = errorChar; 
			  		   		   }
				  	            
				  	            if (popAction.contentEquals("*--out of the loop--*") || popAction.contentEquals("*--skip frame--*")) {
				  	            	break readingFrame;
				  	            }
				  	            
				  	            if (popAction.contentEquals("*--skip line--*")) {
				  	            	break readingLine;
				  	            }
				  	            
				  	            if (popAction.contentEquals("*--copy frame--*")) {
				  	            	String[] rst = SqliteDB.GetLastSub(dbSubs, vidFile, 1);
				  	            	readSub = rst[3];
				  	            	break readingFrame;
				  	            }			  	            
				  	          			  	            
				  				//readSub=readSub + popReadChar;
				  	            chr = popReadChar;
	
				  				//create ocr stats
				  				if (generateOcrStats) SqliteDB.dbInsertStat(dbcn, ocrRslt[1]+"", popReadChar, toDouble(ocrRslt[3]));
	
			  	           }
			  				
			  		   	   if (chr.length()==0)	{	// if is a null char
			  					// do nothing
			  		   	   }
			  		   	   else {						// if is a valid char
			  					
			  				   if(lastValidChar!=0 && Math.abs(lastValidChar-firstPoint)>minPix) {
			  					   // if more than x pixels between previous char then add space
			  					   readSub=readSub + " ";
			  				   }		  					
			  					
			  					readSub = readSub + chr;
			  					lastValidChar = secondPoint;
			  		   	   }
			  				  				
			  		   	   prevPoint=secondPoint;
			  	           firstPoint=0;
			  	           secondPoint=0;
			  	           charBox.setVisible(false);
			  	            
		  			  }	 //end when finds second white point  			  
		  		  }	// close else
		  		   
		  		  subText.setText(readSub);
		  		  	  		   
		  		  i=i + iStep;
		 	  	  if(readDir==0) {		// if Left -> Right
		 	  		  if(i>iEnd) iCond=false;
	 		  	  }
	 		  	  else {
	 		  		  if(i<iEnd) iCond=false;
	 		  	  }
		  		   
		  	   }		// close for i
		  	   
		  	   //add LF
		  	   if (readSub.trim().length()>0 && l==0) {
		  		   readSub = readSub + CR + LF;
		  	   }
		  	   
  			}		// close if (GetSampleLine(lastReadBox[1],l)[0].contentEquals("-1")) 
  	   }			// close for l
  	   
  	   if(readDir==1) {				// if R->L direction then inverse numbers if there are any
  		   if(containsDigit(readSub) && !popAction.contentEquals("*--copy frame--*")) {
  			   readSub=SqliteDB.inverseNumbers(readSub);
  		   }
			   
  	   }
  
	   subText.setText(readSub);
	   subText.paintImmediately(subText.getVisibleRect());
  
	   if(!actMode.contentEquals("Auto")) {		// if not in auto mode then opens db connection
	  	   //close DB
	  	   try {
			dbcn.close();
	  	   		} catch (SQLException e1) {
			// TODO Auto-generated catch block
	  			prndeb(1,"ERROR: db close" + e1.getMessage());
	  	   }
	   }
	  		   
  	   prndeb(4,"Read subtitle = " + readSub);
  	   
  	   if (pop!=null) ClosePopDlg();	  
	   
  	   seekStatus.setText("");
  	   seekStatus.setVisible(false);
	   
       prndeb(5,"exit ReadSub");
	   return readSub;
  	}	// end TreatBox

  	public static void RunAuto_OLD() {				// DEPRECATED - runs automatic OCR on the film from current position
        prndeb(5,"enter RunAuto");
        
  		int subCount=0;
  		double numWhites, whitesSvdSub=0;
  		int subPosFrom=0, subPosTo=0; 
  		String sub="";
  		stopAfterRead=stopAfterF.isSelected();

 		int posFrom=PlayerGetCurPos();
  		int posTo = PlayerGetEndPos();
  		String readRslt[]= {"",""};
  		boolean copyOK;
  		
  		actMode="Auto";

	  	dbcn = SqliteDB.dbConnect(dbFile);	// opens DB connection
  		
 //autoProc: for (int i=posFrom/step; i<posTo/step; i++) {
  		autoProc: for (int i=posFrom; i<posTo; i+=step) {
  			// visit a frame every second

  			//prndeb(3, "Moving to position" + (i*step) + " of " + posTo); 
  			prndeb(4, "Moving to position " + (i) + " of " + posTo);

  			//PlayerGoTo(i*step);		
  			PlayerGoTo(i);
  			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR: sleep" + e.getMessage());
			} 
  			
  			copyOK = CopyFrame();
  			
  			if (copyOK) {			// if vlc screenshot corretly taken
  				readRslt= ReadBoxV2();
  				numWhites=toInt(readRslt[0]);
  			} 
  			else {						// if vlc screenshot failed
  				numWhites = minFramePix; // forces entering in subtitle treatment
  			}
  			
  			if (numWhites<minFramePix) { 				// if there's no subtitle in this position
  				if (subPosFrom!=0 && subPosTo==0 && i>1) {
  					// print subtitle
  					//subPosTo = (i*step)-step/2;
  					subPosTo = i-adjStep;
  					PrintSub(subCount, subPosFrom, subPosTo, sub, "N");
  					
  					// reset positions
  					subPosFrom=0;
  					subPosTo=0;
  				}
  			}
  			else { 								// if there's a subtitle in this position
  				if (subPosFrom==0) { 			// if previous position had no subtitle
  					
  					if (copyOK)
  						sub = ReadSub();
  					else {
  						sub = "@";	// if screenshot failed forces subtitle text
  						prndeb(3,"CopyFrame error, forcing @ into subtitle " + subCount);
  					}
  						
  					if (!actMode.contentEquals("Auto")) {	// if button cancel was pressed during pop up
  						subStartPos.setText((i-adjStep)+"");
  						break autoProc;
  					}  					
  					  					
  					if (sub.length()>0) {		// if valid subtitle
  						subCount++;  					
  	  					//subPosFrom=i*step-step/2;
  						subPosFrom=i-adjStep;
  	  					subPosTo=0;
  						whitesSvdSub=numWhites;
  					}
  				}
  				else { 																	// if previous position had a subtitle
  					if(isInsideFrameTol(whitesSvdSub,numWhites)) {			// if previous position had the same subtitle
  						// do nothing
  					}
  					else {						// if previous position had a different subtitle
  						// treat previous subtitle
  	  					//subPosTo = i*step-step/2;
  						subPosTo = i-adjStep;
  	  					PrintSub(subCount, subPosFrom, subPosTo, sub, "N");  	  					
  	  					
  	  					//initialize new subtitle
  	  					if (copyOK)
  	  						sub = ReadSub();
  	  					else {
  	  						sub = "@";	// if screenshot failed forces subtitle text
  	  						prndeb(3,"CopyFrame error, forcing @ into subtitle " + subCount);
  	  					}
  	  					
  	  					if (!actMode.contentEquals("Auto")) {	// if button cancel/switch to manual was pressed during pop up
  	  						subStartPos.setText((subPosTo+1)+"");
  	  						break autoProc;
  	  					}
  	  					
  	  					if (sub.length()>0) {	// if valid subtitle
  	  	  					//subPosFrom=i*step-step/2;
  	  						subPosFrom=subPosTo+1;
  	  	  					subPosTo=0;
  	  	  					subCount++;  	  					
  	  	  					whitesSvdSub=numWhites;  	  					  	  						
  	  					}
  	  					else {
  	    					// reset positions
  	    					subPosFrom=0;
  	    					subPosTo=0;						
  	  					}
  	  					
  					}	// end if(whitesSvdSub/numWhites>.95 && whitesSvdSub/numWhites<1.05)
  				}		// end if (subPosFrom==0) 
  			}			// end if (numWhites==0)	
  					
			if (stopAfterRead) {	// if button cancel was pressed during pop up
					prndeb(7,"stopAfterRead => manual");
					actMode="Manual";
					subStartPos.setText((i-adjStep)+"");
					break autoProc;
			}
	  		
  		} // end for (int i=posFrom; i<posTo/step; i++)  		
  		
  		//close DB
  		try {
  			dbcn.close();
  	   	} catch (SQLException e1) {
  	   		// TODO Auto-generated catch block
			prndeb(1,"ERROR: db close" + e1.getMessage());
  	   	}
  	   
        prndeb(5,"exit RunAuto");
  	}	//

  	public static void RunAutoV1() {				// runs automatic OCR on the film from current position  - using SeekNextSubV1 
        prndeb(5,"enter RunAuto");
        
  		int subCount=0, i;
  		double numWhites;
  		boolean first=true;
  		int subPosFrom=0, subPosTo=0; 
  		String sub="";
  		String[] imgStr = {"",""};
  		stopAfterRead=stopAfterF.isSelected();
  		interactiveMode=chkInteractive.isSelected();
  		
 		int posFrom=PlayerGetCurPos();
  		int posTo = PlayerGetEndPos();
  		boolean copyOK;
  		int seekRslt[];
  		
  		actMode="Auto";
  		PaintStatusBar();

	  	dbcn = SqliteDB.dbConnect(dbFile);	// opens DB connection
	  	if(dbcn==null) return;				// if cannot open ocr db then gets out
	  	
	  	//tests subs db
	  	Connection conn =SqliteDB.dbConnect(dbSubs);
	  	if(conn==null) return;
	  	else {
	  		try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
	  	}
	  	
	  	SqliteDB.addCustomFunction(dbcn);	// adds custom function for binary comparison

		//InitCancelRunDlg(); 	
		togleGUI(controlPane,false);
	  	
	  	//positions on the current frame to start the process
		copyOK = CopyFrame();

		if (copyOK) {			// if vlc screenshot corretly taken
			imgStr=ReadBoxV2();
			numWhites=toInt(imgStr[0]);
		} 
		else {						// if vlc screenshot failed
			numWhites = minFramePix; // forces entering in subtitle treatment
		}
	  			
  		autoProc: while (actMode.contentEquals("Auto") && PlayerGetCurPos()<PlayerGetEndPos()-step) {
		  			
  			i=PlayerGetCurPos();
  			
  			if (numWhites<minFramePix) { 							// if there's no subtitle in this position
  				if (subPosFrom!=0 && subPosTo==0 && i>1) {	//if there was a subtitle in the previous position
  					
  					subPosTo = i-adjSub;				
  					 					
  					// print subtitle
  					if (popAction.contentEquals("*--copy frame--*")) {
  						PrintSub(subCount, subPosFrom, subPosTo, sub, "L");
  						popAction="";
  					}
  					else {
  						PrintSub(subCount, subPosFrom, subPosTo, sub, "N");
  						popAction="";
  					}
  					
  					// reset positions
  					subPosFrom=0;
  					subPosTo=0;

  					// cleans the subtext box
  					subText.setText("");
  					subText.paintImmediately(subText.getVisibleRect());

  				}
  			}
  			else { 											// if there's a subtitle in this position
  				if (subPosFrom==0) { 						// if previous position had no subtitle

  					if (copyOK) {
  						sub = ReadSub();
  					}
  					else {
  						sub = "@vlc_err";	// if screenshot failed forces error subtitle text
  						prndeb(1,"CopyFrame error, forcing @ into subtitle " + subCount);
  					}
  					
  					if (!actMode.contentEquals("Auto")) {	// if button cancel was pressed during pop up
  						subStartPos.setText((i)+"");
  						break autoProc;
  					}  					
  					  					
  					if (sub.length()>0) {					// if valid subtitle
  						subCount++;  			
  						
  						if (first) subPosFrom=i;			// if first loop of auto run then keeps current position
  						else subPosFrom=i-adjSub;			// else, adjusts starting position
  						
  	  					subPosTo=0;
  					}
  				}
  				else { 										// if previous position had a subtitle
					// treat previous subtitle
					subPosTo = i-adjSub;
									
  					if (popAction.contentEquals("*--copy frame--*")) {
  						PrintSub(subCount, subPosFrom, subPosTo, sub, "L");
  						popAction="";
  					}
  					else {
  						PrintSub(subCount, subPosFrom, subPosTo, sub, "N");
  						popAction="";
  					}
  					
  					//initialize new subtitle

  					if (copyOK) {
  						sub = ReadSub();
  					}
  					else {
  						sub = "@vlc_err";	// if screenshot failed forces error subtitle text
  						prndeb(1,"CopyFrame error, forcing @ into subtitle " + subCount);
  					}
  					
  					if (!actMode.contentEquals("Auto")) {	// if button cancel/switch to manual was pressed during pop up
  						subStartPos.setText((subPosTo)+"");
  						break autoProc;
  					}
  					
  					if (sub.length()>0) {	// if valid subtitle
  	  					//subPosFrom=i*step-step/2;
  						subPosFrom=subPosTo+1;
  	  					subPosTo=0;
  	  					subCount++;  	  					
  					}
  					else {
    					// reset positions
    					subPosFrom=0;
    					subPosTo=0;						
  					}
  					
  				}		// end if (subPosFrom==0) 
  			}			// end if (numWhites==0)	
  			
  			first=false;
  			
			if (stopAfterRead) {	// if button cancel was pressed during pop up
					actMode="Manual";
					subStartPos.setText((i)+"");
					break autoProc;
			}

			// seek next subtitle
			seekRslt=SeekNextSubV1(0);
			if (seekRslt[1]==1) 
				copyOK = true;
			else
				copyOK = false;
			
			if (copyOK) {			// if vlc screenshot corretly taken
				numWhites=seekRslt[0];
			} 
			else {						// if vlc screenshot failed
				numWhites = minFramePix; // forces entering in subtitle treatment
			}
			
			
  		} // end while (actMode.contentEquals("Auto") && PlayerGetCurPos()<PlayerGetEndPos()-step)
  		
  		togleGUI(controlPane,true);
  		//cancelPop.dispose();
  		
  		//close DB
  		try {
  			dbcn.close();
  	   	} catch (SQLException e1) {
  	   		// TODO Auto-generated catch block
  	   		prndeb(1, "ERROR RunAutoV1 Closing DB: " + e1.getMessage());
  	   	}
  	   
  		PaintStatusBar();
  		
        prndeb(5,"exit RunAuto");
  	}	//
  	
  	public static void RunAutoV2() {				// DEPRECATED - runs automatic OCR on the film from current position  - using SeekNextSubV2 // TEST NOT USED
        prndeb(5,"enter RunAuto");
        
  		int subCount=0, i;
  		double numWhites;
  		int subPosFrom=0, subPosTo=0; 
  		String sub="";
  		String imgStr[]= {"",""};
  		stopAfterRead=stopAfterF.isSelected();

  		int seekRslt[];
  		int posFrom=PlayerGetCurPos();
  		int posTo = PlayerGetEndPos();
  		
  		actMode="Auto";

	  	dbcn = SqliteDB.dbConnect(dbFile);	// opens DB connection

	  	//positions in the current frame to start the process
		CopyFrame();
		imgStr=ReadBoxV2();
		numWhites=toInt(imgStr[0]);
	  	
  		autoProc: while (actMode.contentEquals("Auto") && PlayerGetCurPos()<PlayerGetEndPos()-step) {
		  			
  			i=PlayerGetCurPos();
  			
  			if (numWhites<minFramePix) { 							// if there's no subtitle in this position
  				if (subPosFrom!=0 && subPosTo==0 && i>1) {	//if there was a subtitle in the previous position
  					
  					subPosTo = i-adjStep;				
  					
  					// print subtitle
  					PrintSub(subCount, subPosFrom, subPosTo, sub, "N");
  					
  					// reset positions
  					subPosFrom=0;
  					subPosTo=0;
  				}
  			}
  			else { 											// if there's a subtitle in this position
  				if (subPosFrom==0) { 						// if previous position had no subtitle
  					sub = ReadSub();
  					if (!actMode.contentEquals("Auto")) {	// if button cancel was pressed during pop up
  						subStartPos.setText((i)+"");
  						break autoProc;
  					}  					
  					  					
  					if (sub.length()>0) {		// if valid subtitle
  						subCount++;  					
  						subPosFrom=i;
  	  					subPosTo=0;
  					}
  				}
  				else { 										// if previous position had a subtitle
					// treat previous subtitle
					subPosTo = i-adjStep;
  					PrintSub(subCount, subPosFrom, subPosTo, sub, "N");  	  					
  					
  					//initialize new subtitle
  					sub = ReadSub();
  					if (!actMode.contentEquals("Auto")) {	// if button cancel/switch to manual was pressed during pop up
  						subStartPos.setText((subPosTo)+"");
  						break autoProc;
  					}
  					
  					if (sub.length()>0) {	// if valid subtitle
  	  					//subPosFrom=i*step-step/2;
  						subPosFrom=subPosTo;
  	  					subPosTo=0;
  	  					subCount++;  	  					
  					}
  					else {
    					// reset positions
    					subPosFrom=0;
    					subPosTo=0;						
  					}
  					
  				}		// end if (subPosFrom==0) 
  			}			// end if (numWhites==0)	
  					
			if (stopAfterRead) {	// if button cancel was pressed during pop up
					actMode="Manual";
					subStartPos.setText((i)+"");
					break autoProc;
			}

			seekRslt=SeekNextSubV2(0);
			numWhites=seekRslt[0];
			
  		} // end while (actMode.contentEquals("Auto") && PlayerGetCurPos()<PlayerGetEndPos()-step)
  		
  		//close DB
  		try {
  			dbcn.close();
  	   	} catch (SQLException e1) {
  	   		// TODO Auto-generated catch block
			prndeb(1,"ERROR: db close" + e1.getMessage());
  	   	}
  	   
        prndeb(5,"exit RunAuto");
  	}	//
  	
  	public static void PrintSub(int subNum, int subPosFrom, int subPosTo, String sub, String typ) {		// writes subtitle to db
  		// typ values => N=New, L=copy last, E=edit current
  		
  	    prndeb(5,"enter PrintSub"); 	    

  	    prndeb(7,"Writing sub typ = " + typ + " - curSubId=" + curSubId);
  	    
	    if (SqliteDB.removePonctChars(sub).length()>0) {	  
	    	switch (typ) {
	    		case "N":
		    		curSubStartPos=subPosFrom;
		    		curSubEndPos=subPosTo;
		    		curSubId = SqliteDB.dbInsertSub(dbSubs, vidFile, subPosFrom, subPosTo, sub, typ, 0);
		    		break;
		    		
	    		case "L":
		    		curSubEndPos=subPosTo;
		    		curSubId = SqliteDB.dbInsertSub(dbSubs, vidFile, subPosFrom, subPosTo, sub, typ, curSubId);
	    			break;
	    			
	    		case "E":
		    		curSubStartPos=subPosFrom;
		    		curSubEndPos=subPosTo;
		    		curSubId = SqliteDB.dbInsertSub(dbSubs, vidFile, subPosFrom, subPosTo, sub, typ, curSubId);
	    			break;	    			
	    	}
	    	
	    	PaintStatOCR(curSubStartPos, curSubEndPos, sub, curSubId, typ);
	    }
  	    
	    CleanSubEdit();
	    	    
	    prndeb(5,"exit PrintSub");		
  	}
  	
  	public static int[] SeekNextSubV1(int mode) {		// difference between subs is done by counting of pixels (less accurate but faster)
  	/// mode=1 => search next sub, mode=0 => seek next change (sub or empty)
        prndeb(5,"enter SeekNextSub");
  		                
  		int posFrom=PlayerGetCurPos();
  		int posTo=PlayerGetEndPos();  	
  		int curPos, svdPos;
  		boolean found=false, foundFrameDif=false;
  		double numWhites, numWhites2, whitesSvdSub;
  		String[] readRslt = {"",""};
  		String svdString="", curString="";
  		double compStr1=0, compStr2=0;

		boolean copyOK, firstTry;
		int rslt[] = {0,0};  		
  		
		firstTry=true;	// to avoid that seeker remains in previous position if first copy fails
		
		copyOK = CopyFrame();
		
		readRslt= ReadBoxV2();
		whitesSvdSub=toInt(readRslt[0]);
		svdString=readRslt[1];
		
		numWhites=whitesSvdSub;
		curPos=posFrom;				
		
		prndeb(7, "Seeking next sub from position" + posFrom + " - with Whites=" + whitesSvdSub); 
		
		seekStatus.setVisible(true);
		
		while ((curPos+step<PlayerGetEndPos()-10) && !found && (copyOK || firstTry)) {
			
			prndeb(7, "New loop " + (curPos+step) + " / " + PlayerGetEndPos());
			
			firstTry=false;
			
			seekStatus.setText("Seeking position " + (curPos+step));
			seekStatus.paintImmediately(seekStatus.getVisibleRect());
			
			svdPos=curPos;
			curPos=curPos+step;
			PlayerGoTo(curPos);			 	
			
			try {	// set time to give vlcj time to change pos. With 100ms or less sometimes it freezes while taking screenshot. 
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR: sleep" + e.getMessage());
			} 
			
			copyOK = CopyFrame();
	
			readRslt= ReadBoxV2();
			numWhites=toInt(readRslt[0]);
			curString=readRslt[1];

			// for checking empty lines we use a 5th of the line
			String sample1[]=GetSampleLine(curString,0,5); 
			String sample2[]=GetSampleLine(curString,1,5);
						
			if(sample1[1].contentEquals("-1") && sample2[1].contentEquals("-1")) {	// if both sample strings are empty (no points in the lines)
				prndeb(4, "Found empty lines on pos " + curPos + " with numWhites=" + numWhites);
				//prndeb(10, "line1=" + readRslt[2]);
				//prndeb(10, "line2=" + readRslt[3]);
				numWhites=0;
			} 
			else {
				prndeb(4, "Line 1 =" + sample1[1] + ", line 2=" + sample2[1] + "on pos " + curPos + " with numWhites=" + numWhites);
			}
			
			// for comparing lines use a 3rd of the line			
			compStr1=SqliteDB.compOCR(GetSampleLine(curString,0,3)[0],GetSampleLine(svdString,0,3)[0],0);						
			compStr2=SqliteDB.compOCR(GetSampleLine(curString,1,3)[0],GetSampleLine(svdString,1,3)[0],0);
			
			prndeb(7, "Position " + curPos + " (" + FormatTime(curPos) + "): " + numWhites + " whites, compared samples %: " + compStr1 + "&" + compStr2);			 		
			
			svdString = curString;
			
			if (!isInsideFrameTol(whitesSvdSub,numWhites) || compStr1<.96 || compStr2<.96) { 				// if current sub different from starting one
				numWhites2=numWhites;	//stores new number of whites

				prndeb(7, "Found different sub at position " + curPos + " (" + FormatTime(curPos) + "): " + numWhites + " whites");	
				
				if (mode==0) {									// if looking for change then found
					found=true;
				}
				else {											// if looking for next subtitle
					if (numWhites>=minFramePix) {				// and there's a subtitle in the position
						found=true;	
					}
				}
				
				if (found) {											// if found

					if (adjStep>0) {	// if step adjustment set up
			
						while(isInsideFrameTol(numWhites2,numWhites) && curPos-adjStep>svdPos) {		// tries to adjust to the right position
							
							seekStatus.setText("Adjusting position " + (curPos-adjStep));
							seekStatus.paintImmediately(seekStatus.getVisibleRect());
							
							curPos=curPos-adjStep;
							PlayerGoTo(curPos);	
							copyOK = CopyFrame();						
							
							readRslt= ReadBoxV2();
							numWhites=toInt(readRslt[0]);												
							
							prndeb(7, "Testing previous position " + curPos + " (" + FormatTime(curPos) + "): " + numWhites + " whites");						
						}
	
						curPos=curPos+adjStep;
						PlayerGoTo(curPos);	
						copyOK = CopyFrame();
												
						readRslt = ReadBoxV2();
						numWhites = toInt(readRslt[0]);

						prndeb(7, "Final adjusted position " + curPos + " (" + FormatTime(curPos) + "): " + numWhites + " whites");

					}	// end if (adjStep>0) 
					
				}		// end if (found) 
			}			// if (!isInsideFrameTol(whitesSvdSub,numWhites) || compStr1<.96 || compStr2<.96)
		}				// while ((curPos+step<PlayerGetEndPos()) && !found)	

		seekStatus.setVisible(false);
		seekStatus.paintImmediately(0, 0, 200, 32);
		
		if(curPos+step>=PlayerGetEndPos()-10) {	// reached the end of the video
			PlayerGoTo((int)playerCmpt.mediaPlayer().status().time()-10);
			msgBox("Process Completed");
		}
		
        prndeb(5,"exit SeekNextSub");
        
        rslt[0] = (int) numWhites;
        if (copyOK) 
        	rslt[1] = 1;	// true
        else 
        	rslt[1] = 0;	// false
        
		return rslt ;
  	}
  	  	
  	public static int[] SeekNextSubV2(int mode) {		// TEST NOT USED - difference between subs is done by comparing pixels position (more accurate but slower)	
  	// mode=1 => search next sub, mode=0 => seek next change (sub or empty)
        prndeb(5,"enter SeekNextSub");
  		                
  		int posFrom=PlayerGetCurPos();
  		int posTo=PlayerGetEndPos();  	
  		int curPos;
  		String imgStr[]= {"",""};
  		String imgStrSvd;
  		boolean notFound=true;
  		double numWhites, numWhites2, whitesSvdSub;
		double frameDif;	
		
		boolean CopyOk;
		int rslt[] = {0,0};
		
		CopyOk = CopyFrame();
		
		imgStr=ReadBoxV2();
		whitesSvdSub=toInt(imgStr[0]);
		imgStrSvd=imgStr[1];
		numWhites=whitesSvdSub;
		curPos=posFrom;
		
		prndeb(7, "Seeking next sub from position" + posFrom + " - with Whites=" + whitesSvdSub); 		
		
		while ((curPos+step<PlayerGetEndPos()-10) && notFound) {
			
			prndeb(7, "New loop " + (curPos+step) + " / " + PlayerGetEndPos());
			
			curPos=curPos+step;
			PlayerGoTo(curPos);			 	
			
			try {	// set time to give vlcj time to change pos. With 100ms or less sometimes it freezes while taking screenshot. 
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR: sleep" + e.getMessage());
			} 
			
			CopyOk = CopyFrame();
	
			imgStr=ReadBoxV2();
			numWhites=toInt(imgStr[0]);

			frameDif=SqliteDB.compOCR(imgStrSvd,imgStr[1],0);
			
			prndeb(10,"Sav String: " + imgStrSvd);
			prndeb(10,"Cur String: " + imgStr[1]);
			prndeb(7, "Position " + curPos + " (" + FormatTime(curPos) + ": " + numWhites + " whites, frame difference: " + frameDif);			 					
			
			if (frameDif<frameTolPix) { 					// if current sub different from starting one
				numWhites2=numWhites;	//stores new number of whites
				imgStrSvd=imgStr[1];	//stores new image string

				prndeb(7, "Found different sub at position " + curPos + " (" + FormatTime(curPos) + ": " + numWhites + " whites");	
				
				if (mode==0) {							// if looking for change then found
					notFound=false;
				}
				else {										// if looking for next subtitle
					if (numWhites>=minFramePix) {			// and there's a subtitle in the position
						notFound=false;	
					}
				}
				
				if (!notFound) {						// if found
					
					frameDif=frameTolPix;
					
					while(frameDif<frameTolPix) {		// tries to adjust to the right position
						curPos=curPos-adjStep;
						PlayerGoTo(curPos);	
						CopyOk = CopyFrame();
						
						imgStr=ReadBoxV2();
						numWhites=toInt(imgStr[0]);						
						frameDif=SqliteDB.compOCR(imgStrSvd,imgStr[1],0);
						
						imgStrSvd=imgStr[1];

						prndeb(7, "Testing previous position " + curPos + " (" + FormatTime(curPos) + ": " + numWhites + " whites, frame Difference: " + frameDif);						
					}

					curPos=curPos+adjStep;
					PlayerGoTo(curPos);	
					CopyOk = CopyFrame();					

					imgStr=ReadBoxV2();
					numWhites=toInt(imgStr[0]);						
					
					prndeb(7, "Final adjusted position " + curPos + " (" + FormatTime(curPos) + ": " + numWhites + " whites");
				}
			}
		}
			
		if(notFound) {	// reached the end of the video
			PlayerGoTo((int)playerCmpt.mediaPlayer().status().time()-10);
			msgBox("Process Completed");
		}
		
        prndeb(5,"exit SeekNextSub");
        
        rslt[0] = (int) numWhites;
        
		return rslt ;
  	}
 
  	public static void GoToSub(String typ, int mode) {	// moves to a saved subtitle start position in mode move or edit
  		// typ: F => First, P => previous, N => Next, L => Last, PE => previous error, NE => next error, numeric value => sub #
  		// mode: 0 => go to, move:1 => edit
  		
  		String rslt[];
  		int pos;
  		
  		if (mode==0) {			// search mod
  			rslt = SqliteDB.GoToSub(dbSubs, vidFile, typeLastSub, typ, curSubId, chkShowDel.isSelected());
  			curSubId = Integer.valueOf(rslt[0]);
  			curSubStartPos = Integer.valueOf(rslt[1]);
  			curSubEndPos = Integer.valueOf(rslt[2]);
  			
  			pos=curSubStartPos+ (curSubEndPos-curSubStartPos)/2;		// move to the end position of the subtitle
  		}
  		else {				// edition mode (fills manual edition fields and deletes sub from DB)
  			if (actMode.contentEquals("Edit")) {
  				actMode="Manual";
  				CleanSubEdit();
  				pos=PlayerGetCurPos();  				
  				PaintStatusBar();
  			}
  			else {
  				if(curSubId!=0) {
	  				actMode="Edit";
	  				pos = SqliteDB.EditSub(dbSubs, vidFile, curSubId);
  				} 
  				else {
  	  				pos=PlayerGetCurPos();
  				}  				
  			}
  		}
  		
  		prndeb(7,"Moved to Sub Pos=" + pos + " with type: " + typ);
  		
  		if(pos>PlayerGetEndPos()-100) pos = PlayerGetEndPos()-100;
  		
  		if (pos>=0) {
  			PlayerGoTo(pos);
  			
  			CopyFrame();
  			ReadBoxV2();
  			
  			// wait until player is moves
  			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR: sleep" + e.getMessage());
			}  			
  		}
  	}

  	public static void SaveCurSub() {				// saves current subtitle in EDIT MODE
        prndeb(5,"enter SaveCurSub");
        
  		String txt=subText.getText();
  		int posFrom=toInt(subStartPos.getText());
  		int posTo=toInt(subEndPos.getText());
  		int recId=toInt(subRecId.getText());
  		  		
  		if (!actMode.contentEquals("Edit") || recId==0 || posFrom==0 || posTo==0 || txt.length()==0) {
  			msgBox("Cannot save subtitle.\nPlease review data");  			
  		}
  		else {
  			curSubId=recId;
  			curSubStartPos=posFrom;
  			curSubEndPos=posTo;
  			
  			actMode="Manual";

  			PrintSub(0, posFrom, posTo, txt, "E");  
  			PaintStatusBar();
  		}
  		
        prndeb(5,"exit SaveCurSub");
  	}
  	
   	public static void SetSubStart() {				// reads text on current position and sets it as start  
  		if (subText.getText().length()==0) {
  			ReadSub();
  		}
  		subStartPos.setText(PlayerGetCurPos()+""); 
  		
  		if(seekAfterManual==1) 	SeekNextSubV1(0);	// seek next change on sub
  	}  	
   	
  	public static void SetSubEnd() {				// sets current position to end and writes sub to db
  		
        prndeb(5,"enter SetSubEnd");
  		
  		String sub=subText.getText();  		
  		int startPos = toInt(subStartPos.getText());
  		int recId = toInt(subRecId.getText());
  		int endPos = PlayerGetCurPos();
  		boolean ok=true;
  		int minSubLen = 500;

  		if (startPos==0 || sub.length()==0) {
  			msgBox("You should first Read Subs and set Start Position");
  			ok=false;
  		}
  		if (endPos-startPos<minSubLen) {
  			msgBox("Sub duration cannot be less that " + minSubLen + "ms");
  			ok=false;  			
  		}
		
  		if (ok) {
  			if (actMode.contentEquals("Edit")) {
  				curSubId = SqliteDB.dbInsertSub(dbSubs, vidFile, startPos, endPos, sub, "E", recId);
  			}
  			else {
  	  			curSubId = SqliteDB.dbInsertSub(dbSubs, vidFile, startPos, endPos, sub, "N", 0);  				
  			}
  			actMode="Manual";
  			
  			CleanSubEdit();
  			PaintStatOCR(startPos, endPos, sub, curSubId, "E");

  	  		if(seekAfterManual==1) 	SeekNextSubV1(0);	// seek next change on sub
  		}
  		
        prndeb(5,"exit SetSubEnd");
  	}

  	public static void MergeCurSub(String mode) {			// merges current sub 
  		// mode "P" => into previous, mode "N" => into next 
  		
  		SqliteDB.MergeSub(dbSubs, vidFile,  curSubId, typeLastSub, mode);
  		
  		GoToSub("N", 0);	//moves to the next sub
  	}
  	
  	public static void DeleteCurSub() {				// marks current sub as deleted 
  		
  		SqliteDB.DeleteSub(dbSubs, vidFile,  curSubId);
  		
  		GoToSub("N", 0);	//moves to the next sub
  	}
  	
  	public static void WriteSubtitles() {			// cleans the subtitles database then writes the SRT file
  		//SqliteDB.dbFixSubWrongTimes(dbSubs, vidFile);

  		
  		// deprecated, now done during ocr reading
  		//if(readDir==1) SqliteDB.dbInverseSubNumbers(dbSubs, vidFile);	// if reading right to left then invert numbers
  		
  		SqliteDB.dbCleanSubs(dbSubs, vidFile);
  		SqliteDB.dbRemoveDoubleSubs(dbSubs, vidFile);
  		SqliteDB.dbFixSubsOverlaps(dbSubs, vidFile);
  		SqliteDB.writeSubsToSrt(dbSubs, vidFile, srtFile);
  		
  		
  		//SqliteDB.testCustom(dbFile);
  	}

  	public static boolean isInsideFrameTol(double numPix1, double numPix2) {	// returns true if compared number of pixels inside the frame tolerance
  		
  		boolean rslt=true;
  		
  		// checks tolerance in pixels
		if(Math.abs(numPix1-numPix2)>frameTolPix) rslt=false;		 
		
		// checks tolerance in percentage
		if(numPix1>numPix2) {
			if (numPix2/numPix1<frameTolPct && Math.abs(numPix1-numPix2)>minFramePix) rslt=false; 							
		} 
		else {
			if (numPix1/numPix2<frameTolPct && Math.abs(numPix1-numPix2)>minFramePix) rslt=false; 			
		}			

		if (!rslt) {
			if (numPix1>numPix2)
				prndeb(7,"Frame diff in % btw " + (int)numPix1 + "," + (int)numPix2 + " is " + (numPix2/numPix1) + " vs tol " + frameTolPct + " - pix DIF=" + (int)(numPix1-numPix2));
			else
				prndeb(7,"Frame diff in % btw " + (int)numPix1 + "," + (int)numPix2 + " is " + (numPix1/numPix2) + " vs tol " + frameTolPct + " - pix DIF=" + (int)(numPix2-numPix1));
		}
		
		return rslt;
  	}
  	
  	public static String[] GetSampleLine(String imgStr, int lineNb, int part) {

  		int sampleFrom1, sampleTo1, sampleFrom2, sampleTo2;	
  		String sample[]= {"",""};
  		//int lineSamplePart=3;
  		int lineSamplePart=part;
  		  		
  		// defines points for sample (half a line) in the middle of each line
  		sampleFrom1=(captureBox[0].getWidth()*Math.round(captureBox[0].getHeight()/2))+1+Math.round((captureBox[0].getWidth()-captureBox[0].getWidth()/lineSamplePart)/2);
  		sampleTo1=sampleFrom1+Math.round(captureBox[0].getWidth()/lineSamplePart);
  		
  		sampleFrom2=sampleFrom1 + (captureBox[0].getWidth()*captureBox[0].getHeight());
  		sampleTo2=sampleTo1 + (captureBox[0].getWidth()*captureBox[0].getHeight());  		
  		
  		if (lineNb==0)	
  			sample[0]=imgStr.substring(sampleFrom1, sampleTo1);
  		else			
  			sample[0]=imgStr.substring(sampleFrom2, sampleTo2);
  		
  		sample[1]=sample[0].indexOf("X")+"";
  				
  		return sample;
  	}
  	
  //***************************************** CANCEL POP UP (for cancelling auto mode) *****************************************************
  	
  	public static void InitCancelRunDlg() {												// DEPRECATED - button cancel pop up for auto mode
  		
		  prndeb(2,"enter InitCancelRunDlg");	
		  
	        SwingUtilities.invokeLater(new Runnable() {

	            public void run() {
	      	      // create a dialog Box 
	      		  cancelPop = new JDialog(Main, "Cancel Run",true);
	      		  cancelPop.setModal(false);
	      	      //paramPop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	      		  cancelPop.setPreferredSize(new Dimension(150, 90));
	      		  cancelPop.setLocation(1450,100);

	      	      JLayeredPane pane = new JLayeredPane();
	      	      pane.setLayout(null);
	      	      cancelPop.add(pane);
	      	      pane.setSize(120, 50);  	      
	            
	      	      JButton btnCancel = new JButton();

	      	      btnCancel.setText("OK");
	      	      btnCancel.setBounds(30,10,80,30);
	      	      btnCancel.setMargin(new Insets(2, 2, 2, 2));	          
	      	      

	      	      pane.add(btnCancel,0);

	      	      btnCancel.addActionListener(new ActionBtnCancelPop());
	      	      
	      	      // set visibility of dialog 
	      	      cancelPop.pack();
	      	      cancelPop.setVisible(true); 
	            }
	      });
		  
				      
	      prndeb(5,"exit InitCancelRunDlg");
	}	
	
	public static class ActionBtnCancelPop implements ActionListener {					// DEPRECATED - cancels auto mode, pass to manual mode
	  	public void actionPerformed(ActionEvent e) {
	  		
	        prndeb(5,"enter ActionBtnCancelPop");
	  		
	  		actMode="Manual";
	  		
	        prndeb(5,"exit ActionBtnCancelPop");
	  	}
	}
  	
  	//***************************************** OCR POP UP (capture new char) *****************************************************
	
 	public static void InitPopDlg() {
 	  popOn=false;
	
      // create a dialog Box 
      pop = new JDialog(Main, "Enter char value",true);
      pop.setModal(true);
      pop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      pop.setPreferredSize(new Dimension(420, 400));
      pop.setLocation(370,400);

      JLayeredPane pane = new JLayeredPane();
      pane.setLayout(null);
      pop.add(pane);
      pane.setSize(420, 400);      

      popStat = new JLabel(""); 		// time & position
      popStat.setBounds(10,10,300,20); 
      pane.add(popStat,0);
            
      JLabel l1 = new JLabel("");		// char background
      l1.setBounds(10,40,380,toInt(boxH.getText())+2);
	  l1.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
      l1.setBackground(Color.BLACK);
      l1.setOpaque(true);
      pane.add(l1,1);
      
      popl2 = new JLabel("");			// char image
      popl2.setBounds(15,41,380,toInt(boxH.getText()));   
      pane.add(popl2,0);

      popStat2 = new JLabel(); 		// OCR stats
      popStat2.setBounds(10,130,350,30);
      popStat2.setFont(popStat2.getFont().deriveFont((float)13)); 
      pane.add(popStat2,1);  

      popStat3 = new JLabel(); 		// OCR stats
      popStat3.setBounds(110,130,30,30);
      popStat3.setFont(popStat3.getFont().deriveFont((float)14));  
      popStat3.setFont(popStat3.getFont().deriveFont(popStat3.getFont().getStyle() | Font.BOLD));
      pane.add(popStat3,0);  

      
      popTA = new JTextField("");		// capture of the new char value
      popTA.setBounds(15,165,200,30);
      popTA.setFont(popStat3.getFont().deriveFont((float)16));
      popTA.setFont(popTA.getFont().deriveFont(popTA.getFont().getStyle() | Font.BOLD));      
      pane.add(popTA,0);  
      popTA.setHorizontalAlignment(JLabel.CENTER);
      if (readDir==1) {
    	  popTA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      }
      else {
    	  popTA.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
      }      
      
      stopAfter = new JCheckBox("Stop after read");	// checkbox to stop
      stopAfter.setBounds(250,165,120,20); 
      pane.add(stopAfter,0);
      
      JButton btnOk = new JButton();
      btnOk.setText("Char -> Sub+OCR");
      btnOk.setBounds(10,200,120,30);
      btnOk.setMargin(new Insets(2, 2, 2, 2));
      
      JButton btnSkip = new JButton();
      btnSkip.setText("Char -> Sub");
      btnSkip.setBounds(140,200,120,30);
      btnSkip.setMargin(new Insets(2, 2, 2, 2));

      JButton btnSkipL = new JButton();
      btnSkipL.setText("End Line");
      btnSkipL.setBounds(10,240,120,30);
      btnSkipL.setMargin(new Insets(2, 2, 2, 2));             
      
      JButton btnSkipF = new JButton();
      btnSkipF.setText("Skip Frame");
      btnSkipF.setBounds(140,240,120,30);
      btnSkipF.setMargin(new Insets(2, 2, 2, 2));          

      JButton btnEqualPrev = new JButton();
      btnEqualPrev.setText("Copy Previous");
      btnEqualPrev.setBounds(270,240,120,30);
      btnEqualPrev.setMargin(new Insets(2, 2, 2, 2));   
      
      JButton btnCancel = new JButton();
      btnCancel.setText("Switch to Manual");
      btnCancel.setBounds(100,300,150,30);
      btnCancel.setMargin(new Insets(2, 2, 2, 2));          


      pane.add(btnOk,0);
      pane.add(btnCancel,0);
      pane.add(btnSkip,0);
      pane.add(btnSkipL,0);     
      pane.add(btnSkipF,0);
      pane.add(btnEqualPrev,0);    
      
      btnOk.addActionListener(new ActionBtnPopOk());
      btnCancel.addActionListener(new ActionBtnPopCancel());
      btnSkip.addActionListener(new ActionBtnPopSkip());
      btnSkipL.addActionListener(new ActionBtnPopSkipLine());
      btnSkipF.addActionListener(new ActionBtnPopSkipFrame());
      btnEqualPrev.addActionListener(new ActionBtnPopEqualPrev());
      
      // set visibility of dialog 
      pop.pack();
      pop.setVisible(false); 

}
	
	public static void ShowPopDlg(BufferedImage img, String[] ocrRslt) { 
 	  	popOn=true;
 	  	
 	  	popStat.setText("Time:" + FormatTime((int) playerCmpt.mediaPlayer().status().time()));
 	  	
 	    popl2.setBounds(15,40+toInt(ocrRslt[6]),380,img.getHeight());   
      
 	  	popStat2.setText("Best match: «             » - " + (String.format("%.1f", toDouble(ocrRslt[3])*100)) + "% (tol: " + ocrRslt[4] +") id#" + ocrRslt[5]);   	  	
 	  	
 	  	popStat3.setText(ocrRslt[1]);   	
 	  	
 	  	if (img!=null) popl2.setIcon(new ImageIcon(img));

 	  	popl2.repaint();
 	  	
 	  	stopAfter.setSelected(stopAfterRead); 	  
 	  	
 	  	if (ocrRslt[1].contentEquals("null") || Math.abs(toDouble(ocrRslt[3])-toDouble(ocrRslt[4]))>charPropTol) {		// if no proposal or matching beyond tolerance show no proposal
 	 	  	popTA.setText("");
 	  	}
 	  	else {
 	 	  	popTA.setText(ocrRslt[1]);	
 	  	}
 	  	popTA.requestFocus();
 	  	pop.setVisible(true);
  }
    
	public static class ActionBtnPopCancel implements ActionListener {					// cancels auto mode, pass to manual mode
	  	public void actionPerformed(ActionEvent e) {
	  		
	        prndeb(5,"enter ActionBtnPopCancel");
	  		
	  		popAction = "*--out of the loop--*";
	  		popReadChar="";
	  		actMode="Manual";
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  		
	        prndeb(5,"exit ActionBtnPopCancel");
	  	}
	}

	public static class ActionBtnPopOk implements ActionListener { 						// captured char goes to text and OCR DB
	  	public void actionPerformed(ActionEvent e) {
	  		
	    	//getCharImage
	    	ImageIcon icon = (ImageIcon)popl2.getIcon();
	    	BufferedImage img = (BufferedImage)((Image) icon.getImage());
	    	
	    	String str=getImgStr(img);
	    	popReadChar=popTA.getText();
	    	if (popReadChar==null) popReadChar="";
	    	
	    	if(img.getWidth()>=toInt(minSpacePix.getText()) && popReadChar.length()==0) popReadChar=" ";	    		
	    	
	  	   	double tol = toDouble(tolOCR.getText());
	  	   	if (tol==0) 
	  	   		tol=.95;
	    	
	    	SqliteDB.dbInsertOCR(dbcn, img.getWidth(),img.getHeight(), str, popReadChar, tol, yPos);
	  		
	 	  	stopAfterRead=stopAfter.isSelected();
	 	  	stopAfterF.setSelected(stopAfter.isSelected());
	    	
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  	}
	}
 
	public static class ActionBtnPopSkip implements ActionListener { 					// captured char goes to text
	  	public void actionPerformed(ActionEvent e) {
	  		
	    	popReadChar=popTA.getText();
	    	if (popReadChar==null) popReadChar="";
	  		
	 	  	stopAfterRead=stopAfter.isSelected();
	 	  	stopAfterF.setSelected(stopAfter.isSelected());	 	  		 	  	
	 	  	
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  	}
	}
	
	public static class ActionBtnPopSkipLine implements ActionListener { 				// skips the current line
	  	public void actionPerformed(ActionEvent e) {
	  		
	  		popAction="*--skip line--*";
	  		popReadChar="";
	  				
	 	  	stopAfterRead=stopAfter.isSelected();
	 	  	stopAfterF.setSelected(stopAfter.isSelected());
	 	  	
	 	  	prndeb(4,"User skips line");
	 	  	
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  	}
	}  	
	
	public static class ActionBtnPopSkipFrame implements ActionListener { 				// skips the current frame
	  	public void actionPerformed(ActionEvent e) {
	  		
	  		popAction="*--skip frame--*";
	  		popReadChar="";
	  		
	 	  	stopAfterRead=stopAfter.isSelected();
	 	  	stopAfterF.setSelected(stopAfter.isSelected());
	  		
	 	  	prndeb(4,"User skips frame");
	 	  	
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  	}
	}
	
	public static class ActionBtnPopEqualPrev implements ActionListener { 				// current frame continues the previous one
	  	public void actionPerformed(ActionEvent e) {
	  		
	  		popAction="*--copy frame--*";
	  		popReadChar="";
	  		
	 	  	stopAfterRead=stopAfter.isSelected();
	 	  	stopAfterF.setSelected(stopAfter.isSelected());
	  		
	 	  	prndeb(4,"User copies previous frame");
	 	  	
	  		popl2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 0));
	  		pop.setVisible(false);
	  		popOn=false;
	  	}
	}
	
	public static void ClosePopDlg() {    	
  	
      pop.dispose();
  }  		  	

    //******************************* OCR CHARACTER TOLERANCES POP UP ************************************

	public static void InitCharTolDlg() {
		  prndeb(5,"enter InitCharTolDlg");	
		  
	      // create a dialog Box 
	      charTolPop = new JDialog(Main, "Character Tolerance",true);
	      charTolPop.setModal(true);
	      //paramPop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	      charTolPop.setPreferredSize(new Dimension(400, 800));
	      charTolPop.setLocation(100,50);

	      JLayeredPane pane = new JLayeredPane();
	      pane.setLayout(null);
	      charTolPop.add(pane);
	      pane.setSize(400, 800);     

	      String[] columnNames = {"Character", "Tolerance"};	      	      	            
	      Object[][] data = SqliteDB.GetCharTolTable(dbFile);
	      
	      TableModel model = new DefaultTableModel(data, columnNames)
	      {
	        public boolean isCellEditable(int row, int column)
	        {
	        	if (column!=1)
	        		return false;	//This causes all cells to be not editable
	        	else
	        		return true;	//This causes all cells to be editable
	        }
	      };
	      
	      charTolTable = new JTable(model);
	      TableColumn column = null;
	      column = charTolTable.getColumnModel().getColumn(0);
	      column.setPreferredWidth(80); 
	      column = charTolTable.getColumnModel().getColumn(1);
	      column.setPreferredWidth(80); 
	      
	      JScrollPane scrollPane = new JScrollPane(charTolTable);
	      charTolTable.setFillsViewportHeight(true);    
	      scrollPane.setBounds(10,30,360,600);
	      
	      pane.add(scrollPane);

	      JButton btnOk = new JButton();
	      btnOk.setText("Save");
	      btnOk.setBounds(10,700,80,30);
	      btnOk.setMargin(new Insets(2, 2, 2, 2));	          
	      
	      JButton btnCancel = new JButton();
	      btnCancel.setText("Cancel");
	      btnCancel.setBounds(100,700,80,30);
	      btnCancel.setMargin(new Insets(2, 2, 2, 2));          


	      pane.add(btnOk,0);
	      pane.add(btnCancel,0);     
	      
	      btnOk.addActionListener(new ActionBtnCharTolOK());
	      btnCancel.addActionListener(new ActionBtnCharTolCancel());
	      
	      // set visibility of dialog 
	      charTolPop.pack();
	      charTolPop.setVisible(true); 

	      prndeb(5,"exit InitCharTolDlg");
	}	
	    
	public static class ActionBtnCharTolOK implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {
	  		boolean ok=true;
	  		double val;
	  		
	  		for (int i=0; i<charTolTable.getRowCount();i++) {
	  			//prndeb(7,"value for " + paramTable.getModel().getValueAt(i,0)+": " + paramTable.getModel().getValueAt(i,2));
	  			val=toDouble(charTolTable.getModel().getValueAt(i,1).toString());
	  			if(val<=0 || val>1) ok=false; 				    			    		
	  		}	// end for (int i=0; i<paramTable.getRowCount();i++)	  		

  		if (ok) {
	  		for (int i=0; i<charTolTable.getRowCount();i++) {
	  			SqliteDB.dbUpdateCharTol(dbFile, charTolTable.getModel().getValueAt(i,0).toString(),toDouble(charTolTable.getModel().getValueAt(i,1).toString()));
	  		}
      		charTolPop.dispose();
  		}
  		else {
  			msgBox("Character Tolerance must be higher than 0 and equal or less to 1");
  		}
	  		
	  	} // close actionPerformed
	  	
	}	//close ActionBtnParamsOK 	
	
	public static class ActionBtnCharTolCancel implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
	  		charTolPop.dispose();
	  	} // close actionPerformed
	  	
	}	//close ActionBtnParamsCancel 	
	
	public static boolean SaveCharTol() {
		return true;
	}
	
    //******************************* OCR CHARACTER STATS POP UP ************************************

	public static void InitCharStatsDlg() {
		  prndeb(5,"enter InitCharStatsDlg");	
		  
	      // create a dialog Box 
	      charStatsPop = new JDialog(Main, "OCR matching statistics",true);
	      charStatsPop.setModal(true);
	      //paramPop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	      charStatsPop.setPreferredSize(new Dimension(400, 800));
	      charStatsPop.setLocation(100,50);

	      JLayeredPane pane = new JLayeredPane();
	      pane.setLayout(null);
	      charStatsPop.add(pane);
	      pane.setSize(400, 800);     

	      String[] columnNames = {"Real Character", "OCR matching character", "OCR matching coef"};	      	      	            
	      Object[][] data = SqliteDB.GetCharStatsTable(dbFile);
	      
	      TableModel model = new DefaultTableModel(data, columnNames)
	      {
	        public boolean isCellEditable(int row, int column)
	        {
        		return false;	//This causes all cells to be non editable
	        }
	      };
	      
	      charStatsTable = new JTable(model);
	      TableColumn column = null;
	      column = charStatsTable.getColumnModel().getColumn(0);
	      column.setPreferredWidth(80); 
	      column = charStatsTable.getColumnModel().getColumn(1);
	      column.setPreferredWidth(80); 
	      column = charStatsTable.getColumnModel().getColumn(2);
	      column.setPreferredWidth(80); 
	      
	      JScrollPane scrollPane = new JScrollPane(charStatsTable);
	      charStatsTable.setFillsViewportHeight(true);    
	      scrollPane.setBounds(10,30,360,600);
	      
	      pane.add(scrollPane);

	      JButton btnOk = new JButton();
	      btnOk.setText("OK");
	      btnOk.setBounds(10,700,100,30);
	      btnOk.setMargin(new Insets(2, 2, 2, 2));	          
	      
	      JButton btnPurge = new JButton();
	      btnPurge.setText("Purge Table");
	      btnPurge.setBounds(120,700,100,30);
	      btnPurge.setMargin(new Insets(2, 2, 2, 2));          


	      pane.add(btnOk,0);
	      pane.add(btnPurge,0);     
	      
	      btnOk.addActionListener(new ActionBtnCharStatsOK());
	      btnPurge.addActionListener(new ActionBtnCharStatsPurge());
	      
	      // set visibility of dialog 
	      charStatsPop.pack();
	      charStatsPop.setVisible(true); 

	      prndeb(5,"exit InitCharStatsDlg");
	}	
	
	public static class ActionBtnCharStatsOK implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
	  		charStatsPop.dispose();
	  	} // close actionPerformed
	  	
	}	//close ActionBtnCharstatsOk 	
	
	public static class ActionBtnCharStatsPurge implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {
	  		SqliteDB.dbPurgeStat(dbFile);
  			charStatsPop.dispose();
	  		
	  	} // close actionPerformed
	  	
	}	//close ActionBtnCharStatsPurge 	
	
    //******************************* ABOUT POP UP ************************************
	
	public static void InitAboutDlg() {
		  prndeb(5,"enter InitAboutDlg");	
		  	  
	      // create a dialog Box 
	      aboutPop = new JDialog(Main, "About",true);
	      aboutPop.setModal(true);
	      //paramPop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	      aboutPop.setPreferredSize(new Dimension(300, 300));
	      aboutPop.setLocation(650,200);

	      JLayeredPane pane = new JLayeredPane();
	      pane.setLayout(null);
	      aboutPop.add(pane);
	      pane.setSize(300, 300);  
	      
	      //out.println("\tSpec Title/Version: " + pkg.getSpecificationTitle() + " " + pkg.getSpecificationVersion());
	      //out.println("\tSpec Vendor: " +  pkg.getSpecificationVendor());
	      
	      Package pkg = Package.getPackage("subRipper");
	      
	      String txt = "<html><center>";
	      txt =  txt + "<font size='7'>@rySubRipper</font><br/>";
	      txt =  txt + "<font size='3'>Version: " + appVersion + "</font><br/>";
	      txt =  txt + "<font size='3'>Build  : " + appBuild + "</font><p/>";	      
	      txt =  txt + "<font size='4'>" + appCopyright + "</font><br/>";
	      txt =  txt + "<font size='4'><a href='https://github.com/arysoftplay/-rySubRipper'>https://github.com/arysoftplay/-rySubRipper</a></font>";	   
	      txt =  txt + "</center></html>";
	      
	      JTextPane lblAbout = new JTextPane();
	      lblAbout.setBounds(10,10,260,180);
	      lblAbout.setContentType("text/html");
	      lblAbout.setText(txt);
	      lblAbout.setEditable(false);
	      lblAbout.setOpaque(true);
	      lblAbout.setFont(subText.getFont().deriveFont((float)17));
	      lblAbout.setBackground(Color.LIGHT_GRAY);

	      lblAbout.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent event) {
	    	        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    	            String url = event.getURL().toString();
	    	            try {
							Desktop.getDesktop().browse(URI.create(url));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							prndeb(1,"ERROR opening HyperLink: "  + e.getMessage());
						}
	    	        }
				}
	    	});
	      
	      JButton btnOk = new JButton();
	      btnOk.setText("OK");
	      btnOk.setBounds(10,220,80,30);
	      btnOk.setMargin(new Insets(2, 2, 2, 2));	          
	      
	      JButton btnDoc = new JButton();
	      btnDoc.setText("Help");
	      btnDoc.setBounds(100,220,80,30);
	      btnDoc.setMargin(new Insets(2, 2, 2, 2));          

	      JButton btnLic = new JButton();
	      btnLic.setText("License");
	      btnLic.setBounds(190,220,80,30);
	      btnLic.setMargin(new Insets(2, 2, 2, 2));   
	      
   	      pane.add(lblAbout,0);
	      pane.add(btnOk,0);
	      pane.add(btnDoc,0);     
	      pane.add(btnLic,0);  

	      btnOk.addActionListener(new ActionBtnAboutOK());
	      btnDoc.addActionListener(new ActionBtnAboutDoc());
	      btnLic.addActionListener(new ActionBtnAboutLic());
	      
	      // set visibility of dialog 
	      aboutPop.pack();
	      aboutPop.setVisible(true); 

	      prndeb(5,"exit InitAboutDlg");
	}	
	
	public static class ActionBtnAboutOK implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
	  		aboutPop.dispose();
	  	} // close actionPerformed	  	
	}	//close ActionBtnAboutOK
	
	public static class ActionBtnAboutDoc implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
	  		openPDF("@rySubRipper_Doc.pdf",SqliteDB.dbGetPath());  		  	
	  	} // close actionPerformed	  	
	}	//close ActionBtnAboutDoc
	
	public static class ActionBtnAboutLic implements ActionListener {				
	  	public void actionPerformed(ActionEvent e) {  
	  		openPDF("@rySubRipper_Lic.pdf",SqliteDB.dbGetPath());	  	
	  	} // close actionPerformed	  	
	}	//close ActionBtnAboutLic
	
  	//***************************************** BROWSE FILE FUNCTIONS *****************************************************    

  	public static void BrowseFile(String typ) {
  		
  		JFileChooser jfc=null;
  		FileNameExtensionFilter filter=null;
  		String folder="";
	  	//Create a file chooser
        //JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
  		
        switch( typ ) {
    		case "V":  		
		  		jfc = new JFileChooser(GetIniEntry("LastSaved","videoDir"));
		        filter = new FileNameExtensionFilter("Video Files", "avi", "wmv", "mp4", "mpg", "mpeg", "mkv", "mov", "flv");
		        break;
    		case "O":  		
    			folder=GetIniEntry("LastSaved","ocr");
    			if(folder.length()==0) {
    				folder=SqliteDB.dbGetPath();
    			}
    			else {
    				folder=GetFileDir(folder);
    			}
    			
		  		jfc = new JFileChooser(folder);
		  		filter = new FileNameExtensionFilter("Database", "db");
		        break;
    		case "S":  	
    			folder=GetIniEntry("LastSaved","subs");
    			if(folder.length()==0) {
    				folder=SqliteDB.dbGetPath();
    			}
    			else {
    				folder=GetFileDir(folder);
    			}
    			    			
		  		jfc = new JFileChooser(folder);
		        filter = new FileNameExtensionFilter("Database", "db");
		        break;
        }
        
        jfc.addChoosableFileFilter(filter);	     
        jfc.setFileFilter(filter);

        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            prndeb(9,"selected file: " + selectedFile.getAbsolutePath());
            
            switch( typ ) {
            	case "V":
		            vFile.setText(selectedFile.getAbsolutePath());
		  			SetIniEntry("LastSaved","videoDir",GetFileDir(selectedFile.getAbsolutePath()));
			        PlayerOpenFile(selectedFile.getAbsolutePath());
			        break;
            	case "O":
		            dbFileTxt.setText(selectedFile.getAbsolutePath());
		  			SetIniEntry("LastSaved","ocr",selectedFile.getAbsolutePath());
		  			dbFile = selectedFile.getAbsolutePath();
		  			InitVariables();
			        break;
			        
            	case "S":
		            dbSubsTxt.setText(selectedFile.getAbsolutePath());
		  			SetIniEntry("LastSaved","subs",selectedFile.getAbsolutePath());
		  			dbSubs = selectedFile.getAbsolutePath();
			        break;            		
            }
        }

  	}
  	
    public static void PopNewFile(String typ) {   	
        // create a dialog Box 
  	  String tit = "";
  	  
  	  newFileType = typ;
  	  
  	  if(typ.contentEquals("O")) {
  		  tit = "OCR";    		  
  	  }
  	  else {
  		  tit = "Subtitles";    		  
  	  }
  	
        pop = new JDialog(Main, "Choose New " + tit + " File Name",true);
        pop.setModal(true);
        pop.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pop.setPreferredSize(new Dimension(300, 250));
        pop.setLocation(650,400);

        JLayeredPane pane = new JLayeredPane();
        pane.setLayout(null);
        pop.add(pane);

        // setsize of dialog 
        pane.setSize(300, 250);
                 
        popl2 = new JLabel("File Name");
        popl2.setBounds(15,45,180,20);   
        pane.add(popl2,0);

        popTA = new JTextField("");
        popTA.setBounds(15,80,250,20);
        pane.add(popTA,0);
        
        JButton btnOk = new JButton();
        btnOk.setText("OK");
        btnOk.setBounds(10,120,80,30);
        btnOk.setMargin(new Insets(2, 2, 2, 2));             
        
        JButton btnCancel = new JButton();
        btnCancel.setText("Cancel");
        btnCancel.setBounds(100,120,80,30);
        btnCancel.setMargin(new Insets(2, 2, 2, 2));          


        pane.add(btnOk,0);
        pane.add(btnCancel,0);
        
        btnOk.addActionListener(new ActionBtnPopNewFileOk());
        btnCancel.addActionListener(new ActionBtnPopNewFileCancel());
        
        // set visibility of dialog 
        pop.pack();
        pop.setVisible(true); 

  }
  
	public static class ActionBtnPopNewFileOk implements ActionListener { 		
	  	public void actionPerformed(ActionEvent e) {
	  		
	  		File source, dest;
	  		
	  		if(newFileType.contains("O")) {		// OCR DB
	  			source = new File(SqliteDB.dbGetPath() + "\\res\\_ocr.db");
	  		}
	  		else {								// Subs DB
	  			source = new File(SqliteDB.dbGetPath() + "\\res\\_subs.db");	  			
	  		}
	  		
	  		dest = new File(SqliteDB.dbGetPath() + "\\" + popTA.getText());
	  		
	  		try {
				FileUtils.copyFile(source, dest);
				
		  		if(newFileType.contains("O")) {		// OCR DB
		  			dbFileTxt.setText(SqliteDB.dbGetPath() + "\\" + popTA.getText());
		  			SetIniEntry("LastSaved","ocr",SqliteDB.dbGetPath() + "\\" + popTA.getText());
		  			dbFile = SqliteDB.dbGetPath() + "\\" + popTA.getText();
		  		}
		  		else {								// Subs DB
		  			dbSubsTxt.setText(SqliteDB.dbGetPath() + "\\" + popTA.getText());  			
		  			SetIniEntry("LastSaved","subs",SqliteDB.dbGetPath() + "\\" + popTA.getText());
		  			dbSubs = SqliteDB.dbGetPath() + "\\" + popTA.getText();
		  		}		  				
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				prndeb(1, "Create new " + newFileType + " Db File: " + e1.getMessage());
				
				msgBox("Cannot create new file");

			}
	  		//Files.copy(source.toPath(), dest.toPath());
	  		
	  		pop.dispose();
	  	}
	}
 
	public static class ActionBtnPopNewFileCancel implements ActionListener { 		
	  	public void actionPerformed(ActionEvent e) {

	  		pop.dispose();
	  	}
	}
	
  	//***************************************** GUI FUNCTIONS *****************************************************    
    public static void AddKeyListener() {

    	controlPane.addKeyListener(new KeyListener(){
	        @Override
	           public void keyPressed(KeyEvent e) {
	               if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
	                   System.out.println("Hi");
	               }
	           }
	
	           @Override
	           public void keyTyped(KeyEvent e) {
	               throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	           }
	
	           @Override
	           public void keyReleased(KeyEvent e) {
	               throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	           }

	   });
    	
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = controlPane.getInputMap(condition);
        ActionMap actionMap = controlPane.getActionMap();

        String down = "down";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), down);
        actionMap.put(down, new AbstractAction() {

           @Override
           public void actionPerformed(ActionEvent arg0) {
        	  if (actMode.contentEquals("Auto")) {
	              prndeb(4,"Auto Run cancelled by the user");
	              actMode="Manual";
        	  }
           }
        });

	}		

    public static void togleGUI(JLayeredPane pane, boolean isEnabled) {
    	
    	if (bgMode==1) {	// only needed if running in background mode
	    	
	        Component[] components = pane.getComponents();
	
	        for (Component component : components) {
	            /*if (component instanceof JPanel) {
	                togleGUI((JPanel) component, isEnabled);
	            }
	            */
	        	
	        	// if component is not a text area
	        	if (!(component instanceof JTextArea || component instanceof JTextField)) {
	        		component.setEnabled(isEnabled);	        		
	        	}       	
	        }
	        
	        // set text fields
	    	boxX.setEditable(isEnabled);
	    	boxY[0].setEditable(isEnabled);
	    	boxY[1].setEditable(isEnabled);
	    	boxW.setEditable(isEnabled);
	    	boxH.setEditable(isEnabled);
	    	tolerance.setEditable(isEnabled);
	    	toleranceG.setEditable(isEnabled);
	    	toleranceB.setEditable(isEnabled);
	    	goToPos.setEditable(isEnabled);
	    	minSpacePix.setEditable(isEnabled);
	    	tolOCR.setEditable(isEnabled);
	    	subText.setEditable(isEnabled);
	    	subStartPos.setEditable(isEnabled);
	    	subEndPos.setEditable(isEnabled);
	    	
	        // enables the needed elements
	        captBox[0].setEnabled(true);
	        captBox[1].setEnabled(true);
	        lblStatOCR.setEnabled(true);
	        lblStatus.setEnabled(true);
	        lblStatus2.setEnabled(true);
	        
	        pane.paintImmediately(pane.getVisibleRect());
    	}
    }
	
  	//***************************************** TOOL FUNCTIONS *****************************************************    

  	public static void msgBox(String text){		// pops alert message box
  	    Toolkit.getDefaultToolkit().beep();
  	    JOptionPane optionPane = new JOptionPane(text,JOptionPane.WARNING_MESSAGE);	//WARNING_MESSAGE or INFORMATION_MESSAGE or ERROR_MESSAGE
  	    JDialog dialog = optionPane.createDialog("Warning!");
  	    dialog.setAlwaysOnTop(true);
  	    dialog.setVisible(true);
  	}
  	
  	public static String FormatTime(int sec) {	// formats position to srt time format
  		int h, m, s, ms;
  		String tmp ="";
  		
  		h = (int) (sec/3600000);
  		m = (int) ((sec-(h*3600000))/60000);
  		s = (int) ((sec - h * 3600000 - m * 60000)/1000);
  		ms = sec - h * 3600000 - m * 60000 - s * 1000;
  		
  		if (h<10)
  			tmp=tmp + "0" + h + ":";
  		else
  			tmp=tmp + h + ":";
  		
  		if (m<10)
  			tmp=tmp + "0" + m + ":";
  		else
  			tmp=tmp + m + ":";

  		if (s<10)
  			tmp=tmp + "0" + s + ",";
  		else
  			tmp=tmp + s + ",";

  		if (ms<10)
  			tmp=tmp + "00" + ms;
  		else
  	  		if (ms<100)
  	  			tmp=tmp + "0" + ms;
  	  		else
  	  			tmp=tmp + ms;
  		
  		return tmp;
  	}

  	public static void writeToLog(String file,String txt, String typ) {
  		
  		if (writeLog) {
	  		if(typ.contentEquals("N")) {	// if new file
	  			TextFileWriter.createFile(file);
	  		}
	  		
	  		String ts = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Timestamp(System.currentTimeMillis()));
	  		
	  		TextFileWriter.append(file,ts + " : " + txt + CR + LF);
  		}
  	}
  	
  	public static void prndeb(int lvl, String txt) {	// prints debug text to console2
  		if (lvl<= debugLevel) {
  			if (txt.length()<500) {
  				System.out.println(txt);
  			}
  			
  			writeToLog(logFile,txt,"A");	//appends txt to log file
  			
  		}
  	}
  	
  	public static void PaintStatusBar() {				// updates info on status bar
  		if (lblStatus.getText().contentEquals("Mode: Auto") && actMode.contentEquals("Manual"))
  			prndeb(4, "Mode switched to Manual");
  		
  		lblStatus.setText("Mode: " + actMode);
  		
    	lblStatus2.setText("Pos: " + playerCmpt.mediaPlayer().status().time() + " Time: " + FormatTime((int) playerCmpt.mediaPlayer().status().time()) + " End: " + playerCmpt.mediaPlayer().status().length() + " - " + FormatTime((int) playerCmpt.mediaPlayer().status().length()));
    	lblStatus2.paintImmediately(lblStatus2.getVisibleRect());    	
  	}

  	public static void PaintStatOCR(int startPos, int endPos, String sub, int subId, String typ) {
  		// typ: L = Last, E = edited
  		
  		String dir=" DIR='LTR' ";
  		String tmp="";
  		
  		if (readDir==1) dir=" DIR='RTL' ";
  		
  		if (subId==0) {
  			lblStatOCR.setText("<html><font size='5'>OCR stat: An error occurred. Please check the log file</font></html>");
  			prevId="";
  			prevSub = "";
  		}
  		else {
	  		if (typ.contentEquals("L")) {
	  			tmp="<html dir='rtl'><table width='100%' cellpadding='0' cellspacing='0'><tr><td><font size='5'>OCR stat (last saved #";	  			
	  		}
	  		else {
	  			tmp="<html><table width='100%' cellpadding='0' cellspacing='0'><tr><td><font size='5'>OCR stat (current: #";  			
	  		}
	  		tmp=tmp + subId + ") pos: " + startPos + "-" + endPos + "</font></td>";
  			tmp=tmp + "<td><center><font size='6'><p " + dir + ">" + sub.replace(LF+"", "<br />") + "</p></font></center></td></tr>";
  			tmp=tmp + "<tr height=1><td colspan=2><hr></td></tr>";
  			tmp=tmp + "<tr><td><font size='5'>Prev Sub #" + prevId + "</font></td>";
  			tmp=tmp + "<td><center><font size='6'><p " + dir + ">" + prevSub.replace(LF+"", "<br />") + "</p></font></center></td></tr>";
  			tmp=tmp + "</table></html>";
  			
  			prevId=subId+"";
  			prevSub = sub;
  			
	  		lblStatOCR.setText(tmp);
  		}
  		
  		lblStatOCR.paintImmediately(lblStatOCR.getVisibleRect());
  	}

  	public static void SetPickColor(int pixel) {		// shows selected "ink" color in the ink box on the status bar 
	      int red = (pixel >> 16) & 0xff;
	      int green = (pixel >> 8) & 0xff;
	      int blue = (pixel) & 0xff;
		  pickColor.setBackground(new Color(red,green,blue));
		  SqliteDB.saveParam(dbFile,"pick", pixel+"");
	}

  	public static void CleanSubEdit() {
  		subText.setText("");
  		subRecId.setText("");
  		subStartPos.setText("");
  		subEndPos.setText("");
  	}
  	
  	public static void PaintReadDirButton() {
  		if (readDir==1) {
  			btnReadDirection.setText("L ← R");
  	        //btnReadDirection.setIcon(new ImageIcon(SetButtonIcon(60,20,"R-L.png")));
  			//scrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
  			subText.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

  		}
  		else {
  			readDir=0;
  			btnReadDirection.setText("L → R");
  	        //btnReadDirection.setIcon(new ImageIcon(SetButtonIcon(60,20,"L-R.png")));
  			subText.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

  		}
  	}
  	
    public static String GetIniEntry(String section, String entry){						// READ CONFIG.INI ENTRY
    	String value="";
    	
        try{
            Wini ini = new Wini(new File(SqliteDB.dbGetPath() + "\\config.ini"));
            value  = ini.get(section, entry, String.class);
        
            prndeb(7,"retrieved " + entry + ": " + value);
        // To catch basically any error related to finding the file e.g
        // (The system cannot find the file specified)
        }catch(Exception e){
        	prndeb(1,"Get Ini Entry (" + section + "." + entry + ": "+ e.getMessage());
        }
        
        if(value==null) value="";
        
        return value;
    }

    public static void SetIniEntry(String section, String entry, String value){				// WRITE CONFIG.INI ENTRY
        try{
            Wini ini = new Wini(new File(SqliteDB.dbGetPath() + "\\config.ini"));
            
            ini.put(section, entry, value);
            ini.store();
        // To catch basically any error related to writing to the file
        // (The system cannot find the file specified)
        }catch(Exception e){
        	prndeb(1,"Set Ini Entry (" + section + "." + entry + ": "+ e.getMessage());
        }
    }

    public static String GetFileDir(String file) {    	
    	
        prndeb(10,"GetFileDir " + file.substring(0,file.lastIndexOf("\\")));
        
    	return file.substring(0,file.lastIndexOf("\\"));
    }
    
    public static Image SetButtonIcon(int w, int h, String imgFile) {
    	
    	Image img=null;
    	
    	try {
    		    //img = ImageIO.read(getClass().getResource(SqliteDB.dbGetPath()+"\\res\\" + imgFile));
    		    img = Toolkit.getDefaultToolkit().getImage(SqliteDB.dbGetPath()+"\\res\\" + imgFile);
    		    img = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH ) ;
    		    prndeb(9, "SetButtonIcon: " + w + "x" + h + " - " + imgFile);	
    	} catch (Exception ex) {
    		    prndeb(1, "ERROR SetButtonIcon: " + ex);
    	}
    	
    	return img;
    }
 
    public static int toInt(String text) {
    	//System.out.println("toInt" + text.trim());
    	if(isNumeric(text.trim())) {
    		return Integer.parseInt(text.trim());
    	}
    	else {
    		return 0;
    	}
    }
    
    public static double toDouble(String text) {
    	//System.out.println("toInt" + text.trim());
    	if(isNumeric(text.trim())) {
    		return Double.parseDouble(text.trim());
    	}
    	else {
    		return 0;
    	}
    }
        
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void openPDF(String fil, String path) {     	
    	
  		if (Desktop.isDesktopSupported()) {
  		    try {
  		        File myFile = new File(path + "\\" + fil);
  		        Desktop.getDesktop().open(myFile);
  		    } catch (IOException ex) {
  		        // no application registered for PDFs
  		    	if (path.contentEquals(SqliteDB.dbGetPath())) path = "the application folder";
  		    	msgBox("No application registered for PDF.\nPlease open " + fil + " on " + path);
  		    }
  		}
    }

    public static boolean fileExists(String fileName) {
    	boolean rslt=false;
    	File f = new File(fileName);
    	if(f.exists() && !f.isDirectory()) { 
    	    rslt=true;
    	}
    	return rslt;
    }

    public static boolean dirExists(String fileName) {
    	boolean rslt=false;
    	File f = new File(fileName);
    	if(f.exists() && f.isDirectory()) { 
    	    rslt=true;
    	}
    	return rslt;
    }

    public final static boolean containsDigit(String s) {
        boolean containsDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (containsDigit = Character.isDigit(c)) {
                    break;
                }
            }
        }

        return containsDigit;
    }

    public final static boolean checkNewVersion(String version) {
        
    	boolean rslt = false;
        
    	int respCode;
    	
        prndeb(7,"check for new version " + version);
        
        String strUrl = "https://github.com/arysoftplay/-rySubRipper/releases/tag/v" + version;
        
        try {
            URL url = new URL(strUrl);
            HttpURLConnection  urlc = (HttpURLConnection) url.openConnection();
            urlc.connect();//<--- throws UnknownHostException when unable to connect!!
            respCode = urlc.getResponseCode();
            urlc.disconnect();
            if(respCode == HttpURLConnection.HTTP_OK) {
            //System.out.println("URL exists");            
            	prndeb(7,"new version " + version + " found");
            	msgBox("New version available at " + strUrl);
            	rslt=true;
            }
            else {
            	  prndeb(7,"http response code = " + respCode);
            }
        } catch(UnknownHostException e) {
            prndeb(7,"URL either doesn't exist or unable to connect at this moment - no new version found");
            //System.out.println("URL either doesn't exist or unable to connect at this moment");
        } catch(IOException e) {
        	// e.printStackTrace();
      	  prndeb(7,"IO exception: " + e);

        }
		
        return rslt;
    }
    
}