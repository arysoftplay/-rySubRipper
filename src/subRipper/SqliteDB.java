package subRipper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;  
import java.sql.SQLException; 
import java.sql.Statement;

import org.sqlite.Function;

import java.sql.PreparedStatement;
import java.sql.ResultSet;  

public class SqliteDB {
		public final static char CR  = (char) 0x0D;
		public final static char LF  = (char) 0x0A;		
		public final static int compMode = 0;	// compares full string
	
//****************************************************** GENERAL DB  *******************************************		
		
        public static Connection dbConnect(String FileName) {  

        	try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
            Connection conn = null;
            String url = "jdbc:sqlite:" + FileName;  

            if (!SubRipper.fileExists(FileName)) {
            	SubRipper.msgBox("File not found: " + FileName);
            }
            else {
	            try {  
	                // db parameters  
	                // create a connection to the database  
	                conn = DriverManager.getConnection(url);  
	                  
	                prndeb(10,"Connection to SQLite has been established.");  
	                  
	            } catch (SQLException e) {  
	            	SubRipper.msgBox("Error connecting to " + url);
	                prndeb(1,e.getMessage());  
	            } finally {  
	            	/*
	                try {  
	                    if (conn != null) {  
	                        conn.close();  
	                    }  
	                } catch (SQLException ex) {  
	                    System.out.println(ex.getMessage());  
	                } 
	                */ 
	            }  
            }
            return conn;
        }  
 
        public static void dbCreateNewDatabase(String fileName) {  
       	/* To create a new database in SQLite using java programming language.          	   
       	 */
            String url = "jdbc:sqlite:" + fileName;  
       
            try {  
                Connection conn = DriverManager.getConnection(url);  
                if (conn != null) {  
                    DatabaseMetaData meta = conn.getMetaData();  
                    System.out.println("The driver name is " + meta.getDriverName());  
                    System.out.println("A new database has been created.");
                    
                    conn.close();
                }  
       
            } catch (SQLException e) {  
                System.out.println(e.getMessage());  
            }  
        }          

        public static void dbCreateNewTable(String FileName, String sql) {  
            // SQLite connection string  
            String url = "jdbc:sqlite:" + FileName;  
              
            // SQL statement for creating a new table  
            /*String sql = "CREATE TABLE IF NOT EXISTS employees (\n"  
                    + " id integer PRIMARY KEY,\n"  
                    + " name text NOT NULL,\n"  
                    + " capacity real\n"  
                    + ");";*/  
              
            try{  
                Connection conn = DriverManager.getConnection(url);  
                Statement stmt = conn.createStatement();  
                stmt.execute(sql);  
                
                conn.close();
            } catch (SQLException e) {  
                System.out.println(e.getMessage());  
            }  
        }  
               
        public static void dbInsert(Connection conn, String query) {  						// executes a sql statement
            //String sql = "INSERT INTO employees(name, capacity) VALUES(?,?)";  
       
            try{  
                //Connection conn = this.connect();  
                Statement stmt = conn.createStatement();  
                stmt.execute(query);  
            } catch (SQLException e) {  
                System.out.println(e.getMessage());  
            }  
        }        

//********************************************************* OCR  ***********************************************        
               
        public static String dbExistStr(Connection conn, int w, int h, String str){  							// DEPRECATED
            
        	String query = "SELECT * FROM ocr WHERE width=" + w + " AND height=" + h + " AND char='" + str +"'";  
            String tmp=null;
            
            try {  
                //Connection conn = this.connect();  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                // loop through the result set  
                if (rs.next()) {  
                	tmp = rs.getString("char");  
                }  
            } catch (SQLException e) {  
                prndeb(1,e.getMessage());  
            }
            
            return tmp;
        }
        
        public static String[] dbExistStrTol(Connection conn, int w, int h, String str, double tolerance, int minSpacePix, int yPos){  		// retrieves best OCR match for a string
            
        	String []rslt = new String[6];
        	
        	String query = "SELECT o.*, ifnull(c.tolerance," + tolerance + ") as tol FROM ocr o ";
        	query= query + " LEFT JOIN char_tol c ON o.char=c.char ";
        	query= query + " WHERE o.width=" + w + " AND o.height=" + h;
        	
            String svdStr, curStr, debugStr; 
            double svdMatch=0, curMatch, debugMatch=0;    
            String svdChar=null, curChar, debugChar=null;
            double svdTol=0, curTol, debugTol=0;
            int id, svdId=0, debugId=0, width;
            double curYPos, charYPos;
            
            try {  
                //Connection conn = this.connect();  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                // loop through the result set  
                while (rs.next()) {  
                	curStr = rs.getString("string");
                	curChar = rs.getString("char");  
                	id = rs.getInt("id");
                	curTol = rs.getDouble("tol");
                	width = rs.getInt("width");
                	charYPos = (double) rs.getInt("y_pos");
                	curYPos = (double) yPos;
                	
                	if(curChar.length()==0 && width>=minSpacePix) {
                		curChar=" ";
                	}
                	
                	curMatch=compOCR(curStr,str, compMode);	// calculates matching % between the 2 strings                	
                	curMatch=curMatch-Math.abs(curYPos-charYPos)/100;	
                	
                	prndeb(8,"comparing against rec id" + id + ", char=" + curChar + ", match="+ curMatch +  "curYPos vs charYPos: " + curYPos + "," + charYPos +" adj=" + (Math.abs(curYPos-charYPos)/100));

                	if(curMatch>debugMatch) {                		
                		debugMatch=curMatch;
                		debugStr=curStr;
                		debugChar=curChar;
                		debugTol=curTol;
                		debugId=id;
                	}                	
                	
                	if(curMatch>curTol && curMatch>svdMatch) {                		
                		svdMatch=curMatch;
                		svdStr=curStr;
                		svdChar=curChar;
                		svdTol=curTol;
                		svdId=id;
                	}
                }  
                
                rs.close();
                
            } catch (SQLException e) {  
                prndeb(1,e.getMessage());  
            }
            
            prndeb(6,"best match: '" + debugChar + "' tol=" + debugTol + ", %="+ debugMatch);            
            prndeb(6,"accepted match: '" + svdChar + "' tol=" + debugTol + ", %=" + svdMatch + " id=" + svdId);
            
            rslt[0]= svdChar;
            rslt[1]= debugChar+"";
            rslt[2]= svdMatch+"";
            rslt[3]= debugMatch+"";
            rslt[4]= debugTol+"";
            rslt[5]= debugId+"";
            return rslt;
        }

        public static String[] dbExistStrTolV2(Connection conn, int w, int h, String str, double tolerance, int minSpacePix, int yPos){  	// retrieves best OCR match for a string
            
        	String []rslt = new String[7];
        	
        	String query = "SELECT * FROM (";
        	query= query + "SELECT binComp(o.string,'" + str + "','" + yPos +"', ifnull(o.y_pos,0)) as cmp, o.*, ifnull(c.tolerance," + tolerance + ") as tol FROM ocr o ";
        	query= query + " LEFT JOIN char_tol c ON o.char=c.char ";
        	query= query + " WHERE o.width=" + w + " AND o.height=" + h;
        	query= query + ") ORDER BY cmp DESC";
        	        	        	
            String svdStr, curStr, debugStr; 
            double svdMatch=0, curMatch, debugMatch=0;    
            String svdChar=null, curChar, debugChar=null;
            double svdTol=0, curTol, debugTol=0;
            int id, svdId=0, debugId=0, width;
            double curYPos, charYPos;
            
            try {  
                //Connection conn = this.connect();  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                // loop through the result set  
                if (rs.next()) {  
                	curStr = rs.getString("string");
                	curChar = rs.getString("char");  
                	id = rs.getInt("id");
                	curTol = rs.getDouble("tol");
                	width = rs.getInt("width");
                	charYPos = (double) rs.getInt("y_pos");
                	curYPos = (double) yPos;
                	
                	if(curChar.length()==0 && width>=minSpacePix) {
                		curChar=" ";
                	}
                	
                	curMatch=SubRipper.toDouble(rs.getString("cmp"));
                	
                	prndeb(8,"comparing against rec id" + id + ", char=" + curChar + ", match="+ curMatch +  "curYPos vs charYPos: " + curYPos + "," + charYPos +" adj=" + (Math.abs(curYPos-charYPos)/100));

                	if(curMatch>debugMatch) {                		
                		debugMatch=curMatch;
                		debugStr=curStr;
                		debugChar=curChar;
                		debugTol=curTol;
                		debugId=id;
                	}                	
                	
                	if(curMatch>curTol && curMatch>svdMatch) {                		
                		svdMatch=curMatch;
                		svdStr=curStr;
                		svdChar=curChar;
                		svdTol=curTol;
                		svdId=id;
                	}
                	

                }
                
            	rs.close();
            	
            } catch (SQLException e) {  
				prndeb(1,"ERROR dbExistStrTolV2: " + e.getMessage());
            }
            
            prndeb(6,"best match: '" + debugChar + "' tol=" + debugTol + ", %="+ debugMatch);            
            prndeb(6,"accepted match: '" + svdChar + "' tol=" + debugTol + ", %=" + svdMatch + " id=" + svdId);            
            
            rslt[0]= svdChar;
            rslt[1]= debugChar+"";
            rslt[2]= svdMatch+"";
            rslt[3]= debugMatch+"";
            rslt[4]= debugTol+"";
            rslt[5]= debugId+"";
            rslt[6]= yPos+"";
            return rslt;
        }
   
        public static void dbInsertOCR(Connection conn, int w, int h, String imgStr, String chr, double tol, int yPos) {  // writes OCR char entry to DB
            prndeb(7,"enter dbInsertOCR"); 
            
            String query = "INSERT INTO ocr(width, height, string, char, y_pos) VALUES(?,?,?,?,?)";  
       
            try{  
                //Connection conn = this.connect();  
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, w);
                pstmt.setInt(2, h);  
                pstmt.setString(3, imgStr);  
                pstmt.setString(4, chr);
                pstmt.setInt(5, yPos);  
                pstmt.executeUpdate();  
                
                prndeb(9,"adding " + chr + " to OCR");  
                
            } catch (SQLException e) {  
                prndeb(0,e.getMessage());  
            }  
            
            query="INSERT INTO char_tol(char, tolerance) VALUES(?,?)";
            try{  
                //Connection conn = this.connect();  
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, chr);
                pstmt.setDouble(2, tol);  
                pstmt.executeUpdate();                
                prndeb(9,"adding " + chr + " to OCR");                  
            } catch (SQLException e) {
            	// can fail if char already exists in the table
                prndeb(10,e.getMessage());  
            }  
            
            prndeb(7,"out dbInsertOCR");
        } 
        
        public static double compOCR(String str1, String str2, int mode){													// compares two OCR character strings pixel by pixel and returns matching percentage        	        	
        	// mode = 0 => compare full string
        	// mode = 1 => compare only whites
        	
        	double cnt=0, tot=0;
        	//System.out.println("compOCR");
        	//System.out.println("str1="+str1); 
        	//System.out.println("str2="+str2);        
        	
        	for (int i=0;i<str1.length();i++) {
        		
        		if(str1.charAt(i)=="X".charAt(0) || str2.charAt(i)=="X".charAt(0) || mode==0) {	        		
	        		tot++;
	        		if(str1.charAt(i)==str2.charAt(i))
	        			cnt++;
        		}
        	}
        	
        	//System.out.println("tot="+tot+", cnt="+cnt+", match=" + (cnt/tot)); 
        	if (tot!=0)
        		return cnt/tot;
        	else
        		return 0;
        }

        public static void dbInsertStat(Connection conn, String chrOcr, String chrReal, double match) {  			// insert OCR matching statistics 
       
            if (chrOcr!=null) {		// inserts into the ocr if there was a proposed char by the ocr
            
	            String query = "INSERT INTO ocr_stats(char_ocr, char_real, match) VALUES(?,?,?)";
	            	
	            try{  
	                //Connection conn = this.connect();  
	                PreparedStatement pstmt = conn.prepareStatement(query);
	                pstmt.setString(1, chrOcr);
	                pstmt.setString(2, chrReal);
	                pstmt.setDouble(3,Math.round(match*1000)/1000.0d);    
	                pstmt.executeUpdate();  
	                                
	            } catch (SQLException e) {  
	                prndeb(1,"ERROR Insert Stat: " + e.getMessage());  
	            }
            }
        } 
        
        public static void dbPurgeStat(String dbFile) {  			// insert OCR matching statistics
            prndeb(5,"enter dbPurgeStat"); 
            
        	Connection conn = dbConnect(dbFile);
            String query = "DELETE FROM ocr_stats";  
       
            try{  
                //Connection conn = this.connect();  
                PreparedStatement pstmt = conn.prepareStatement(query);   
                pstmt.executeUpdate();  
                                
            } catch (SQLException e) {  
                prndeb(1,"ERROR Purge Stat: " + e.getMessage());  
            }  
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR Purge Stat close DB: " + e.getMessage());  
			}
            
            prndeb(5,"out dbPurgeStat"); 
        } 
        
        public static void dbUpdateCharTol (String dbFile, String chr, double val) {
            prndeb(5,"enter dbUpdateCharTol"); 
        	
        	Connection conn = dbConnect(dbFile);
            String query = "UPDATE char_tol SET tolerance=? WHERE char=?";  
       
            try{  
                //Connection conn = this.connect();  
                PreparedStatement pstmt = conn.prepareStatement(query);   
                pstmt.setDouble(1, val);
                pstmt.setString(2, chr);
                pstmt.executeUpdate();  
                                
            } catch (SQLException e) {  
                prndeb(1,"ERROR Update Char Tol: " + e.getMessage());  
            }  
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR Update Char Tol Stat close DB: " + e.getMessage());  
			}
            
            prndeb(5,"out dbUpdateCharTol");      	
        }
        
        public static Object[][] GetCharTolTable(String dbFile) {
        	prndeb(5,"enter GetCharTolTable");
        	
        	Object[][] data = null;
        	            
            try {
            	String query = "SELECT count(1) as num_rec FROM char_tol";  

            	Connection conn = dbConnect(dbFile);  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                int numRec = rs.getInt("num_rec");  
                int cnt=0;
                
                if (numRec>0) {
                	data = new Object[numRec][2];
                	
                	query = "SELECT * FROM char_tol ORDER BY char";  
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);  

                    while (rs.next()) {  
                    	data[cnt][0] = rs.getString("char");  
                    	data[cnt][1] = rs.getString("tolerance");
                    	cnt++;
                    }  
                }
            	
                conn.close();
            } catch (SQLException e) {  
            	prndeb(1,"ERROR GetCharTolTable: " + e.getMessage());  
            }        	        	
        	            
        	prndeb(5,"exit GetCharTolTable");
        	return data;
        }

        public static Object[][] GetCharStatsTable(String dbFile) {
        	prndeb(5,"enter GetCharStatsTable");
        	
        	Object[][] data = null;
        	            
            try {
            	String query = "SELECT count(1) as num_rec FROM ocr_stats";  

            	Connection conn = dbConnect(dbFile);  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                int numRec = rs.getInt("num_rec");  
                int cnt=0;
                
                if (numRec>0) {
                	data = new Object[numRec][3];
                	
                	query = "SELECT * FROM ocr_stats ORDER BY char_real";  
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);  

                    while (rs.next()) {  
                    	data[cnt][0] = rs.getString("char_real");  
                    	data[cnt][1] = rs.getString("char_ocr");
                    	data[cnt][2] = rs.getDouble("match");
                    	cnt++;
                    }  
                }
            	
                conn.close();
            } catch (SQLException e) {  
            	prndeb(1,"ERROR GetCharStatsTable: " + e.getMessage());  
            }        	        	
        	            
        	prndeb(5,"exit GetCharStatsTable");
        	return data;
        }
        
        //************************************************** SUBTITLES POST TREATMENT *********************************************        
        
        public static void dbCleanSubs(String dbFile, String vidFile) { 				// removes "noise" chars or lines from subs DB  
            prndeb(5,"enter dbCleanSubs"); 

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;            		
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(3,"Unknown File: " + vidFile);  
            }
            else {
	            try{  		// cleans subs
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
                    prndeb(4,"# records before cleaning=" + rs.getInt("cnt"));
	            	
	                query = "UPDATE subs SET text=TRIM(text) WHERE file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"subs trimmed 1");  	
	                
	                query = "UPDATE subs SET text = substr(text,1,length(text)-2) WHERE fixed_deleted!=1 AND  text like '%'||char(13)||char(10) AND file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"removed ending CR");  
	
	                query = "UPDATE subs SET text = substr(text,-(length(text)-2)) WHERE fixed_deleted!=1 AND  text like char(13)||char(10)||'%' AND file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"removed leading CR"); 
	                
	                query = "UPDATE subs SET text=replace(text,'  ',' ') WHERE fixed_deleted!=1 AND  file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"removed double spaces");  

	                query = "UPDATE subs SET text=TRIM(text) WHERE fixed_deleted!=1 AND  file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"subs trimmed 2");  	
	                
	                query = "UPDATE subs SET fixed_deleted=1 WHERE (length(text)=0 or text='.' or text=',' or text='-' or text = char(39) or text=char(10) or text=char(13) or text=' ') AND file_id=" + id;  
	            	pstmt = conn.prepareStatement(query);
	                pstmt.executeUpdate();  	                
	                prndeb(7,"removed empty and some one char subs");  
	                
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
                    prndeb(4,"# records after cleaning=" + rs.getInt("cnt"));
	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(1,"ERROR Cleaning subs: " + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
            
            prndeb(5,"out dbCleanSubs");
        } 
 
        public static void dbRemoveDoubleSubs(String dbFile, String vidFile) {  		// consolidates equal consecutive subs in one record 
            prndeb(5,"enter dbRemoveDoubleSubs");
            
            Statement stmt;
            PreparedStatement pstmt;
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;            		
            int posFrom, posTo, recId;
            int posFromSvd=0, posToSvd=0, recIdSvd=0;
            int countDouble=0, countEmpty=0;
            String sub, subSvd="", tmp1, tmp2;
            
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(1,"ERROR File unknown:" + vidFile);  
            }
            else {
	            try{  		// removes doubles
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
                    prndeb(4,"# records before removing doubles=" + rs.getInt("cnt"));
	            	
	            	query="SELECT * FROM subs WHERE file_id=" + id + " AND fixed_deleted!=1 ORDER BY pos_from ASC, pos_to ASC";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);  

	                // loop through the result set  
	                while (rs.next()) {  
	                	recId = rs.getInt("rec_id");  
	                	
	                	posFrom = rs.getInt("fixed_pos_from");
	                	if(posFrom==0) {	                		
	                		posFrom = rs.getInt("pos_from");
	                	}
	                	posTo = rs.getInt("fixed_pos_to");
	                	if(posTo==0) {	                		
	                		posTo = rs.getInt("pos_to");
	                	}

	                	sub = rs.getString("text");	                	  
	                	
	                	tmp1=removePonctChars(sub);
	                	tmp2=removePonctChars(subSvd);
	                	
	                	if(tmp1==null) {
	                		// if empty string then delete the current sub
	    	                query = "UPDATE subs SET fixed_deleted=1 WHERE rec_id="+recId;  
	    	            	pstmt = conn.prepareStatement(query);
	    	                pstmt.executeUpdate();  

	    	                prndeb(10,"removed empty record " + recId);
                			countEmpty++;
	                	}
	                	else {
		                	if(tmp1.contentEquals(tmp2) || tmp1.length()==0) {	// analyzes text without any additional point, space, LF or other ponctuation chars
		                		
		                		if(tmp1.contentEquals(tmp2) && (posTo - posToSvd) < 2000) { 	// if double with previous one and time between end and start < 2 seconds 
		                																		//then update end time of previous sub
				                		prndeb(9,"found double sub at pos " + posFrom + " svd_rec=" + recIdSvd + " - cur rec=" + recId);
				                		prndeb(10,"str1=" + tmp1);  	
				                		prndeb(10,"str2=" + tmp2);
		                		
				    	                query = "UPDATE subs SET fixed_pos_to=" + posTo + " WHERE rec_id="+recIdSvd;  
				    	            	pstmt = conn.prepareStatement(query);
				    	                pstmt.executeUpdate();  
				    	                
				    	                prndeb(10,"set new end time to rec " + recIdSvd);  	
		                		
					                	posToSvd = posTo;		
				                		countDouble++;	
		                		}
		                		else {
		                			countEmpty++;
		                		}
		                		
		                		// if double of empty string then delete the current sub
		    	                query = "UPDATE subs SET fixed_deleted=1 WHERE rec_id="+recId;  
		    	            	pstmt = conn.prepareStatement(query);
		    	                pstmt.executeUpdate();  
	
		    	                prndeb(10,"delete rec " + recId);
		    	                    	                
		                	}
		                	else {                		
			                	recIdSvd = recId;  
			                	posFromSvd = posFrom;
			                	posToSvd = posTo;
			                	subSvd = sub;
		                	}
		                }  
	                }
	                
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);
	                prndeb(4,"# records after removing doubles=" + rs.getInt("cnt"));
                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(1,"ERROR removing double subs: " + e.getMessage());  
	            }  
	            
	            prndeb(4,"removed " + countDouble + " double subs and " + countEmpty + " empty subs");  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
            
            prndeb(5,"out dbRemoveDoubleSubs");
        }         
        
        public static void dbFixSubsOverlaps(String dbFile, String vidFile) {  			// corrects overlapping between subs 
            prndeb(5,"enter dbFixSubsOverlaps");
            
            Statement stmt;
            PreparedStatement pstmt;
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;            		
            int posFrom, posTo, recId;
            int posFromSvd=0, posToSvd=0, recIdSvd=0;
            int count=0;           
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(1,"ERROR File unknown:" + vidFile);  
            }
            else {
	            try{  		// removes doubles
	            	
	            	query="SELECT * FROM subs WHERE fixed_deleted!=1 AND  file_id=" + id + " ORDER BY pos_from ASC, pos_to ASC";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);  

	                // loop through the result set  
	                while (rs.next()) {  
	                	recId = rs.getInt("rec_id");  
	                	
	                	posFrom = rs.getInt("fixed_pos_from");
	                	if(posFrom==0) {	                		
	                		posFrom = rs.getInt("pos_from");
	                	}
	                	posTo = rs.getInt("fixed_pos_to");
	                	if(posTo==0) {	                		
	                		posTo = rs.getInt("pos_to");
	                	}
	                	
	                	if(posFrom<posToSvd) {
	                		prndeb(7,"found inconsistent start at pos " + posFrom + " svd_rec=" + recIdSvd + " - cur rec=" + recId);  	
	                		
	                		// calculate intermediary position for both subs
	                		int intPos=posFrom + ((posToSvd-posFrom)/2);
	                		
	    	                query = "UPDATE subs SET fixed_pos_to=" + intPos + " WHERE fixed_deleted!=1 AND  rec_id="+recIdSvd;  
	    	            	pstmt = conn.prepareStatement(query);
	    	                pstmt.executeUpdate();  

	    	                query = "UPDATE subs SET fixed_pos_from=" + (intPos+1) + " WHERE fixed_deleted!=1 AND rec_id="+recId;  
	    	            	pstmt = conn.prepareStatement(query);
	    	                pstmt.executeUpdate();  	    	                
	    	                
	    	                prndeb(7,"update start/end on recs " + recIdSvd + " and " + recId);  		                		
	    	                
	                		count++;	    	                
	                	}
                		
	                	recIdSvd = recId;  
	                	posFromSvd = posFrom;
	                	posToSvd = posTo;

	                }  
	                
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);
                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(1,"ERROR fixed overlaps: " + e.getMessage());  
	            }  
	            
	            prndeb(4,"fixed " + count + " overlaps");  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
            
            prndeb(5,"out dbFixSubsOverlaps");
        }   
        
        public static void dbFixSubWrongTimes(String dbFile, String vidFile) {  		// DEPRECATED
            prndeb(5,"enter dbFixSubWrongTimes");
            
            Statement stmt;
            PreparedStatement pstmt;
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;            		
            int posFrom, posTo, recId;
            int posFromSvd=0, posToSvd=0, recIdSvd=0;
            int count=0;           
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(1,"ERROR File unknown:" + vidFile);  
            }
            else {
	            try{  		// removes doubles
	            	
	            	query="SELECT * FROM subs WHERE fixed_deleted!=1 AND  file_id=" + id + " ORDER BY rec_id ASC";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);  

	                // loop through the result set  
	                while (rs.next()) {  
	                	recId = rs.getInt("rec_id");  
	                	
	                	posFrom = rs.getInt("fixed_pos_from");
	                	if(posFrom==0) {	                		
	                		posFrom = rs.getInt("pos_from");
	                	}
	                	posTo = rs.getInt("fixed_pos_to");
	                	if(posTo==0) {	                		
	                		posTo = rs.getInt("pos_to");
	                	}
	                	
	                	if(posFrom<posToSvd) {
	                		prndeb(7,"found inconsistent start at pos " + posFrom + " svd_rec=" + recIdSvd + " - cur rec=" + recId);  	
	                		
	                		// calculate intermediary position for both subs
	                		int intPos=posFrom + ((posToSvd-posFrom)/2);
	                		
	    	                query = "UPDATE subs SET pos_from=" + (posToSvd+1) + " WHERE fixed_deleted!=1 AND rec_id="+recId;  
	    	            	pstmt = conn.prepareStatement(query);
	    	                pstmt.executeUpdate();  	    	                
	    	                
	    	                prndeb(7,"update start/end on recs " + recIdSvd + " and " + recId);  		                		
	    	                
		                	posToSvd = posTo;
	                		count++;	    	                
	                	}         		
	                	recIdSvd = recId;  
	                	posFromSvd = posFrom;
	                	posToSvd = posTo;	                	
	                }  
	                
	            	query = "SELECT count(1) as cnt FROM subs WHERE fixed_deleted!=1 AND file_id=" + id;  	                
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);
                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(1,"ERROR Wrong times: " + e.getMessage());  
	            }  
	            
	            prndeb(4,"fixed " + count + " wrong times");  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
            
            prndeb(5,"out dbFixSubWrongTimes");
        }         
 
        public static void dbInverseSubNumbers(String dbFile, String vidFile) {  		// Inverse consecutive (when direction is R->L)
            prndeb(5,"enter dbInverseSubNumbers");
            
            Statement stmt;
            PreparedStatement pstmt;
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;            		
            int recId;
            int count=0;
            String text, tmp;                    
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(1,"ERROR File unknown: " + vidFile);  
            }
            else {
	            try{  		// removes doubles
	            	
	            	//query="SELECT * FROM subs WHERE text REGEXP '[0-9]' AND fixed_deleted!=1 AND  file_id=" + id + " ORDER BY rec_id ASC";
	            	query="SELECT * FROM subs WHERE (text LIKE '%0%' OR text LIKE '%1%' OR text LIKE '%2%' OR text LIKE '%3%' OR text LIKE '%4%' "
	            			+ " OR text LIKE '%5%' OR text LIKE '%6%' OR text LIKE '%7%' OR text LIKE '%8%' OR text LIKE '%9%')"
	            			+ " AND fixed_deleted!=1 AND fixed_numbers!=1 AND  file_id=" + id + " ORDER BY rec_id ASC";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);  

	                // loop through the result set  
	                while (rs.next()) {  
	                	recId = rs.getInt("rec_id"); 	                	
	                	text = rs.getString("text");
	                	
	                	prndeb(9, "working on " + text + " (id#" + recId + ")");
	                	
	                	tmp=inverseNumbers(text);

	    				if (!tmp.contentEquals(text)) {
	    					prndeb(7, "Remplacing " + text + " by " + tmp + " on rec_id " + recId);
	    					count++;
	    				}
	                	
	    				tmp = tmp.replace("'", "''");	//escape quote for sql
	    				
            			query="UPDATE subs SET text='" + tmp + "', fixed_numbers=1 where rec_id=" + recId;
    	            	pstmt = conn.prepareStatement(query);
    	                pstmt.executeUpdate();
    	                
	                }
	                
	            } catch (SQLException e) {
	                prndeb(1,"ERROR dbInverseSubNumbers: " + e.getMessage());  
	            }  	            
	            
	            prndeb(4,"inversed " + count + " numbers");  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
            
            prndeb(5,"out dbInverseSubNumbers");        
        	
        }	
        	
        public static String removePonctChars(String txt) {								// removes all chars but letters from a string
        	String[] charList= {"."," ",",","-","!",":","'",CR+"",LF+""};
        	
        	for(int i=0;i<charList.length; i++) {
        		if(txt != null)	{
        			txt=txt.replace(charList[i], "");
        		}

        	}
        	return txt;
        }
  
        public static String inverseNumbers(String txt) {
        	prndeb(7,"enter inverseNumbers"); 
        	
        	String tmp=txt;
        	
        	int pos=0, first=0, sep=0;
        	
        	for (int i=0;i<tmp.length();i++) {
        		int ascii=(int)tmp.charAt(i);
        		if(ascii>=48 && ascii<=57) {
        			if(pos==0) first=i;
        			pos++;
        			
        			prndeb(9, "found digit " + tmp.charAt(i) + " on pos " + i);
        		}
        		else { 
        			if(pos>1 && (ascii==44 || ascii==46 || ascii==58)) {	// if it's a numeric separator we continue with the number (,.:)
            			prndeb(9, "found separator " + tmp.charAt(i) + " on pos " + i);

        				pos++;
        				sep=pos;
        			}
        			else {
	        			if(pos>1) {					//more than one consecutive number need to inverse
	        				if (pos==sep) pos--;	// if the last number was the separator, then not a separator
	        				tmp=inverseText(tmp,first,pos);
	        			}
	        			pos=0; 
	        			first=0;	
	        			sep=0;
        			}
        		}
        	}
        	
			if(pos>1) {		// if the last chars of the text where numbers
				tmp=inverseText(tmp,first,pos);
			}
					
			
			return tmp;
        }
        
        public static String inverseText(String txt, int first, int len) {				// inverses a substring inside a text
            prndeb(5,"enter inverseText");
            
            prndeb(9, "entered with" + first +", "+ len + ", " + txt);
            
            int last=first+len-1;
            String tmp="";
            
            if (first>0) {
            	tmp=txt.substring(0,first);
            }
                       
            for (int i=last; i>=first;i--) {
            	//prndeb(9, "inversing pos " + i +", char "+ txt.charAt(i));
            	tmp=tmp+txt.charAt(i);
            }
                        
            if(txt.length()>last) {
            	tmp=tmp + txt.substring(last+1);
            }

            prndeb(9, txt + "inversed into " + tmp);
            
            prndeb(5,"enter inverseText");            
            return tmp;
        }

        //****************************************************** SUBTITLES EDITION *******************************************        
        
        public static int dbInsertSub(String dbFile, String vidFile, int posFrom, int posTo, String text, String typ, int recId) {  	// writes sub entry to DB 
      		// typ values => N=New, L=copy last, E=edit current
        	
            prndeb(7,"enter dbInsertSub"); 

            Connection conn=null;
            int id=0, subId=0;
            Statement stmt;
            PreparedStatement pstmt=null;  
            ResultSet rs=null;
            
            if(recId==0) typ="N";
            		
            String query = "INSERT INTO file(name) VALUES(?)";  
            
            try{  		// tries to create video file id (if doesn't already exists)
            	
            	conn = dbConnect(dbFile);  
            	if (conn==null) return 0;
            	
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, vidFile);
                pstmt.executeUpdate();  
                
                prndeb(7,"adding video file" + vidFile + " to DB");  
                
            } catch (SQLException e) {
            	// can fail if the video already exists
                prndeb(7,"ERROR Add video File:" + e.getMessage());  
            }  

            id=getVideoFileId(conn, vidFile);

            
            try{  		// inserts sub line

          	    switch (typ) {      	    		
	      	    	case "N":	      	    		
	      	            query = "INSERT INTO subs(file_id, pos_from, pos_to, text) VALUES(?,?,?,?)";  
	      	            
	                	pstmt = conn.prepareStatement(query);
	                    pstmt.setInt(1, id);
	                    pstmt.setInt(2, posFrom);
	                    pstmt.setInt(3, posTo);
	                    pstmt.setString(4, text);
	                    
	      	    		break;
	      	    		
	      	    	case "L":	
	      	            query = "UPDATE subs SET pos_to = ? WHERE rec_id = ?";  
	      	            
	                	pstmt = conn.prepareStatement(query);
	                    pstmt.setInt(1, posTo);
	                    pstmt.setInt(2, recId);                  
	                    	      	    		
	      	    		break;
	      	    		
	      	    	case "E":
	      	            query = "UPDATE subs SET file_id=?, pos_from=?, pos_to=?, text=? WHERE rec_id=?";  
	      	            
	                	pstmt = conn.prepareStatement(query);
	                    pstmt.setInt(1, id);
	                    pstmt.setInt(2, posFrom);
	                    pstmt.setInt(3, posTo);
	                    pstmt.setString(4, text);
	                    pstmt.setInt(5, recId);   
	                    	      	    		
	      	    		break;  	    		
          	    }

                pstmt.executeUpdate();

                if (typ.contentEquals("N")) {
	                query="SELECT max(rec_id) as rec_id from subs";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);                
	                
	                if (rs.next()) {	
	                	// retrieve new record id
	                	subId=rs.getInt("rec_id");
	                }
	                prndeb(3,"adding sub id#" + subId + " on pos " + posFrom + "-" + posTo + ": " + text);
	                
	                rs.close();
                }
	            else {
	            	subId=recId;
	                prndeb(3,"updating sub id#" + subId + " on pos " + posFrom + "-" + posTo + ": " + text);

	            }
                                                  
            } catch (SQLException e) {
            	// can fail if the video already exists
                prndeb(1,"ERROR Add sub: " + e.getMessage());  
            }  
            
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                        
                        
            prndeb(7,"out dbInsertSub");
            
            return subId;
        } 
        
        public static String[] GetSubData(String dbFile, String vidFile, int recId) {  // returns contents of a record
        	String[] rslt = {"0","0",""};
        	
            prndeb(7,"enter GetSubData");             

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            Connection conn=null;
            String query;
            int id=0;     
            Integer posFrom=0;
            Integer posTo=0;
      
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0 || recId==0) {
            	prndeb(3,"Unknown File: " + vidFile + " or Sub: " + recId);  
            }
            else {
        		try {
	            	query = "SELECT * FROM subs WHERE rec_id=" + recId;	            	

	            	stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
	            	
	                if (!rs.next()) {	
	                	//SubRipper.msgBox("No subtitles for this file");
	                }
	                else {
	                	// retrieve information 
	                	posFrom=rs.getInt("pos_from");
	                	posTo=rs.getInt("pos_to");
	                	
	                	rslt[0] = posFrom.toString();
	                	rslt[1] = posTo.toString();
	                	rslt[2] = rs.getString("text");
	                }
                    
	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(7,"Edit GetSubData " + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                   
            
            prndeb(7,"out GetSubData");
            
            return rslt;        	
        }
        
        public static int EditSub(String dbFile, String vidFile, int recId) {  // retrieves subtitle data and sets it for edition on the frame
        	
        	        	
        	int[] rslt = {0,0};
        	String[] rslt2;
        	
            prndeb(7,"enter EditSub");             

            rslt2 = GetSubData(dbFile, vidFile, recId);
                
            int posFrom=Integer.parseInt(rslt2[0]);
            int posTo=Integer.parseInt(rslt2[1]);
            
            if (posFrom==0 && posTo==0) {
            	prndeb(3,"Unknown File: " + vidFile + " or Sub: " + recId);  
            }
            else {

                	SubRipper.subText.setText(rslt2[2]);
                	SubRipper.subRecId.setText(recId+"");
                	SubRipper.subStartPos.setText(posFrom+"");
                	SubRipper.subEndPos.setText(posTo+"");

	       }
            
            prndeb(7,"out EditSub");
            
            return (int) posFrom+(posTo-posFrom)/2;
        } 

        public static String[] FindSub(String dbFile, String vidFile, int searchType, String moveType, int curRecId) {	//
      		//moveType: F => First, P => previous, N => Next, L => Last, PE => previous error, NE => next error        	
            //searchType:  0 => search last time, 1 => search last rec_id
        	
        	prndeb(7,"enter GoToSub"); 

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            Connection conn=null;
            String query="";
            int id=0;     
            String rslt[]= {"0","0","0","","E"};
            String msg="E";
            
            String delQuery = " AND fixed_deleted = 0 ";
            
            conn = dbConnect(dbFile);
            if(conn==null) return rslt;	// if cannot open db then gets out
            	
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(3,"Unknown File: " + vidFile);  
            }
            else {
	            try{  		// retrieve last sub
	            	
	            	switch (moveType) {
	            		case "F":				//FIRST
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;
				            	query = query + " AND pos_from=(SELECT min(pos_from) FROM subs WHERE file_id=" + id; 
				            	query = query + delQuery;		
				            	query = query + ")";
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT min(rec_id) FROM subs WHERE file_id=" + id;	            		
				            	query = query + delQuery;		
				            	query = query + ")";
			            	}	            			
	            			break;			
	            	
	            		case "P":				//PREVIOUS
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND pos_from=(SELECT max(pos_from) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;					            	
				            	query = query + " AND rec_id<" + curRecId + ")";
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT max(rec_id) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;					            	
				            	query = query + " AND rec_id<" + curRecId + ")";
			            	}	            			
	            			break;
	            			
	            		case "N":				// NEXT
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND pos_from=(SELECT min(pos_from) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;					            	
				            	query = query + " AND rec_id>" + curRecId + ")";
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT min(rec_id) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;					            	
				            	query = query + " AND rec_id>" + curRecId + ")";
			            	}	            				            			
	            			break;
	            			
	            		case "L":				// LAST
	            			msg="L";
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND pos_from=(SELECT max(pos_from) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;
				            	query = query + ")";				            	
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT max(rec_id) FROM subs WHERE file_id=" + id;
				            	query = query + delQuery;
				            	query = query + ")";				            	
			            	}
	            			break;
	            			
	            		case "PE":				//PREVIOUS ERROR
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND pos_from=(SELECT max(pos_from) FROM subs WHERE file_id=" + id;
				            	query = query + " AND rec_id<" + curRecId;
				            	query = query + delQuery;					            	
				            	query = query + " AND text like '%@%')";
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT max(rec_id) FROM subs WHERE file_id=" + id;				            					            	
				            	query = query + " AND rec_id<" + curRecId;
				            	query = query + delQuery;					            	
				            	query = query + " AND text like '%@%')";
			            	}	            			
	            			break;	
	            			
	            		case "NE":				// NEXT ERROR
	            			if(searchType==0) {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND pos_from=(SELECT min(pos_from) FROM subs WHERE file_id=" + id;
				            	query = query + " AND rec_id>" + curRecId;
				            	query = query + delQuery;					            	
				            	query = query + " AND text like '%@%')";			            	  				            	
			            	}
			            	else {
				            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
				            	query = query + " AND rec_id=(SELECT min(rec_id) FROM subs WHERE file_id=" + id;
				            	query = query + " AND rec_id>" + curRecId;
				            	query = query + delQuery;					            	
				            	query = query + " AND text like '%@%')";				            	
			            	}	            				            			
	            			break;
	            	}
	            	
	            	prndeb(4,query);
	            	
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);
	            	
	                if (!rs.next()) {	
	                	//SubRipper.msgBox("No subtitle found");
	                }
	                else {
	                	// retrieve information and fill frame
	                	rslt[0]=Integer.toString(rs.getInt("rec_id"));
	                	rslt[1]=Integer.toString(rs.getInt("pos_from"));
	                	rslt[2]=Integer.toString(rs.getInt("pos_to"));
	                	rslt[3]=rs.getString("text");
	                	rslt[4]=msg;
	                }
	                	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(7,"Go to sub: " + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                   
            
            prndeb(7,"out GoToSub");
            
            return rslt;
        	
        }
        
        public static String[] GoToSub(String dbFile, String vidFile, int searchType, String moveType, int curRecId) {  // searches for a subtitle and sets it for edition on the frame
      		//moveType: F => First, P => previous, N => Next, L => Last, PE => previous error, NE => next error        	
            //searchType:  0 => search last time, 1 => search last rec_id
        	
        	prndeb(7,"enter GoToSub"); 

            String rslt[]= {"0","0","0","",""};
        	
            rslt = FindSub(dbFile, vidFile, searchType, moveType, curRecId);
	            	
            if (rslt[0].contentEquals("0")) {	
            	SubRipper.msgBox("No subtitle found");
            }
         	                
           	SubRipper.PaintStatOCR(Integer.valueOf(rslt[1]),Integer.valueOf(rslt[2]), rslt[3], Integer.valueOf(rslt[0]), rslt[4]);
              
            prndeb(7,"out GoToSub");
            
            return rslt;
        } 
                
        public static String[] GetLastSub(String dbFile, String vidFile, int searchType) {  // retrieves text of last subtitle 
            //type 0: search last time
            //type 1: search last rec_id
        	
        	String[] rslt = {"","","",""};
        	
            prndeb(7,"enter GetLastSub");             

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            Connection conn=null;
            String query;
            int id=0;     
            int posFrom=0;
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(3,"Unknown File: " + vidFile);  
            }
            else {
	            try{  		// retrieve last sub
	            	if(searchType==0) {
		            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
		            	query = query + " AND pos_from=(SELECT max(pos_from) FROM subs WHERE file_id=" + id + ")";
	            	}
	            	else {
		            	query = "SELECT * FROM subs WHERE file_id=" + id;	            	
		            	query = query + " AND rec_id=(SELECT max(rec_id) FROM subs WHERE file_id=" + id + ")";	            		
	            	}
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
	            	
	                if (!rs.next()) {	
	                	SubRipper.msgBox("No subtitles for this file");
	                }
	                else {
	                	// retrieve information and fill return array
	                	rslt[0] = rs.getInt("rec_id")+"";	                	
	                	rslt[1] = rs.getInt("pos_from")+"";
	                	rslt[2] = rs.getInt("pos_to")+"";	                	
	                	rslt[3] = rs.getString("text");	                	                    
	                }
                    
	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(7,"GetLastSub" + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                   
            
            prndeb(7,"out GetLastSub");
            
            return rslt;
        } 
        
        public static void DeleteSub(String dbFile, String vidFile, int recId) {			// deletes subtitle
      	
            prndeb(7,"enter DeleteSub");             

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            Connection conn=null;
            String query;
            int id;            
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0 || recId==0) {
            	prndeb(3,"Unknown File: " + vidFile + " or Sub: " + recId);  
            }
            else {
        		try {
	            	query = "UPDATE subs SET fixed_deleted=1 WHERE rec_id=" + recId;	            	

	            	stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);
	            	
                	prndeb(7,"Marked as deleted rec_id=" + recId);  
                 
	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(7,"DeleteSub " + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                                   
            
            prndeb(7,"out DeleteSub");        	
        }
        
        public static void MergeSub(String dbFile, String vidFile, int recId, int searchType, String mode) {	//merges 2 consecutive subtitles
            //mode P: Previous Sub : extends previous sub and deletes curr sub
            //mode N: Next Sub : extends next sub and deletes curr sub
            //searchType:  0 => search last time, 1 => search last rec_id
        	
        	String[] rsltCur = {"","",""};
        	String[] rsltMerge = {"","","","",""};
        	int posFrom, posTo, recId2;
        	String text;
        	
        	//gets current sub info
        	rsltCur = GetSubData(dbFile, vidFile, recId);
                              
        	//gets previous/next sub info
        	rsltMerge = FindSub(dbFile, vidFile, searchType, mode, recId);
       	
    		prndeb(1,"Current record found = " + recId + ", " + rsltCur[0] + ", " + rsltCur[1] + ", " + rsltCur[2]);
    		prndeb(1,"Merge record found = " + rsltMerge[0] + ", " + rsltMerge[1] + ", " + rsltMerge[2] + ", " + rsltMerge[3] + ", ");

        	if(!rsltCur[0].contentEquals("0") && !rsltMerge[0].contentEquals("0")) {
        		if (mode.contentEquals("P")) {
                	recId2 = Integer.valueOf(rsltMerge[0]);
                	posFrom= Integer.valueOf(rsltMerge[1]);
                	posTo =  Integer.valueOf(rsltCur[1]);
                	text =   rsltMerge[3];        			

        			prndeb(1,"Merge with previous, set record id " + recId2 + " to " + posFrom + ", " + posTo + ", " + text);
                	
        			dbInsertSub(dbFile, vidFile, posFrom, posTo, text, "E", recId2);	// update merged destination record
        			DeleteSub(dbFile, vidFile, recId);								// delete merged origin record
        		}
        		else {
                	recId2 = Integer.valueOf(rsltMerge[0]);
                	posFrom= Integer.valueOf(rsltCur[0]);
                	posTo =  Integer.valueOf(rsltMerge[2]);
                	text =   rsltMerge[3];          			
        			
        			prndeb(1,"Merge with previous, set record id " + recId2 + " to " + posFrom + ", " + posTo + ", " + text);
        			
        			dbInsertSub(dbFile, vidFile, posFrom, posTo, text, "E", recId2);	// update merged destination record
        			DeleteSub(dbFile, vidFile, recId);								// delete merged origin record
        		}
        		prndeb(1,"Delete record id " + recId);        		
        	}
        	else {
        		prndeb(1,"Merge error");
        	}
        	
            prndeb(7,"out MergeSub");
                    	
        }
        
        //********************************************************** PARAMETERS *************************************************        
        
        public static void saveParam(String dbFile, String param, String val) {  		// get parameter from DB
            String query = "UPDATE params SET value = ? WHERE name = ?";  
       
            try{  
                Connection conn = dbConnect(dbFile);  
                if (conn!=null) {
	                PreparedStatement pstmt = conn.prepareStatement(query);
	                pstmt.setString(1, val);
	                pstmt.setString(2, param);  
	                pstmt.executeUpdate();  
	                
	                conn.close();
	                prndeb(7,"saving param " + param + " value " + val);  
                }
            } catch (SQLException e) {  
            	prndeb(1,e.getMessage());  
            }  
        } 

        public static String getParam(String dbFile, String param){  					// save parameter to DB
            
        	String query = "SELECT * FROM params WHERE name='" + param + "'";  
            String tmp="";
            
            try {  
            	Connection conn = dbConnect(dbFile);  
            	if (conn!=null) {
	                Statement stmt  = conn.createStatement();  
	                ResultSet rs    = stmt.executeQuery(query);  
	                  
	                // loop through the result set  
	                if (rs.next()) {  
	                	tmp = rs.getString("value");  
	                }  
	                else {
	                	prndeb(1,"ERROR: Parameter " + param + " missing on the database");
	                }
	                conn.close();
            	}
            } catch (SQLException e) {  
            	prndeb(1,e.getMessage());  
            }
            
            return tmp;
        }
              
        public static Object[][] GetParamsTable(String dbFile) {
        	prndeb(5,"enter GetParamsTable");
        	
        	Object[][] data = null;
        	            
            try {
            	String query = "SELECT count(1) as num_rec FROM params";  

            	Connection conn = dbConnect(dbFile);  
                Statement stmt  = conn.createStatement();  
                ResultSet rs    = stmt.executeQuery(query);  
                  
                int numRec = rs.getInt("num_rec");  
                int cnt=0;
                
                if (numRec>0) {
                	data = new Object[numRec][3];
                	
                	query = "SELECT * FROM params ORDER BY name";  
                    stmt  = conn.createStatement();  
                    rs    = stmt.executeQuery(query);  

                    while (rs.next()) {  
                    	data[cnt][0] = rs.getString("name");  
                    	data[cnt][1] = rs.getString("descr");
                    	data[cnt][2] = rs.getString("value");
                    	cnt++;
                    }  
                }
            	
                conn.close();
            } catch (SQLException e) {  
            	prndeb(1,"ERROR GetParamsTable: " + e.getMessage());  
            }        	        	
        	            
        	prndeb(5,"exit GetParamsTable");
        	return data;
        }

        //************************************************************ SRT FILE ***********************************************        
        
        public static void writeSubsToSrt(String dbFile, String vidFile, String srtFile) {  	// writes srt file from db for current vidFile
            prndeb(7,"enter writeSubsToSrt"); 

            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            String query;
            Connection conn=null;
            int id=0;   
            int cnt=0;
            int posFrom=0, posTo=0, recId;
            String sub;
            
            conn = dbConnect(dbFile);
            
            id=getVideoFileId(conn, vidFile);
            
            if (id==0) {
            	prndeb(3,"Unknown File: " + vidFile);  
            }
            else {
	            try{  		// write subs to srt

	            	//
	            	if (TextFileWriter.createFile(srtFile))	            			
	        			prndeb(7,"File " + srtFile + " created");
	            	else
	            		prndeb(1,"Error creating file " + srtFile);
	            	
	            	query="SELECT * FROM subs WHERE fixed_deleted!=1 AND file_id=" + id + " ORDER BY pos_from ASC";
	                stmt  = conn.createStatement();  
	                rs    = stmt.executeQuery(query);  

	                // loop through the result set  
	                while (rs.next()) {  
	                	recId = rs.getInt("rec_id");  
	                	posFrom = rs.getInt("fixed_pos_from");
	                	if (posFrom==0) posFrom = rs.getInt("pos_from");
	                	posTo = rs.getInt("fixed_pos_to");
	                	if (posTo==0) posTo = rs.getInt("pos_to");
	                	sub = rs.getString("text");	   
	                	cnt++;
	                	
	                	WriteSubEntry(srtFile, cnt, posFrom, posTo, sub);	                	
	                }
	                
	                if (posTo>0) {
	                	posFrom = posTo + 1000;
	                	posTo = posTo + 4000;
	                	sub = "Ripped with @rySubRipper " + SubRipper.appVersion + "\r\nhttps://github.com/arysoftplay/-rySubRipper";
	                	WriteSubEntry(srtFile, cnt+1, posFrom, posTo, sub);
	                }
	                
	                
	                prndeb(4,"SRT written to " + srtFile);
	                SubRipper.msgBox("SRT written to " + srtFile);
	                
	            } catch (SQLException e) {
	            	// can fail if the video already exists
	                prndeb(1,"ERROR Writing srt: " + e.getMessage());  
	            }  
            }
            
            try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                 
                        
            prndeb(7,"out writeSubsToSrt");
        } 
        
      	public static void WriteSubEntry(String srtFile, int subNum, int subPosFrom, int subPosTo, String sub) {	// writes a sub entry on the srt file
      	    prndeb(10,"enter WriteSubEntry");
      		      	    
      		String tmp="";
      		
      		if (subNum>0)
      			tmp="" + CR + LF;
      		
      		prndeb(10,"Sub id=" + subNum + " - Sub from" + SubRipper.FormatTime(subPosFrom) + " to " + SubRipper.FormatTime(subPosTo) + "\n" + sub);
    		if (!TextFileWriter.append(srtFile, tmp + subNum + CR + LF + SubRipper.FormatTime(subPosFrom) + " --> " + SubRipper.FormatTime(subPosTo) + CR + LF + sub + CR + LF))
    			prndeb(1,"Error writing file");		    	      	   
    		
    	    prndeb(10,"exit WriteSubEntry");		
      	}        
    	
//************************************************************* TOOLS ***************************************      	
      	
      	public static int getVideoFileId(Connection conn, String vidFile) {				// retrieves subs video_id from video file name
            prndeb(10,"enter getVideoFileId");
            
            Statement stmt; 
            ResultSet rs;
            int id=0;    
      		
        	String query = "SELECT id FROM file WHERE name='" + vidFile + "'";  
            
            try {  	// retrieves video file id
                stmt  = conn.createStatement();  
                rs    = stmt.executeQuery(query);  
                  
                // loop through the result set  
                if (rs.next()) {  
                	id = rs.getInt("id");  
                }  
                
                prndeb(7,"retrieved video file id " + id);  

            } catch (SQLException e) {  
            	prndeb(0,"Select video File:" + e.getMessage());            	
            }
            
            prndeb(10,"out getVideoFileId");
            
      		return id;
      	}
      	
      	public static void prndeb(int lvl, String txt) {								// writes debug text to console2
    		SubRipper.prndeb(lvl, txt);
      	}

        public static String dbGetPath() {												// gets current application path
 	    	String curDir = System.getProperty("user.dir");
 	    	prndeb(9,"Current User Dir = " + curDir);
         	return curDir;
         }  
        
        public static void addCustomFunction(Connection conn) {

            try {
				Function.create(conn, "binComp", new Function() {
				    @Override
				    protected void xFunc() throws SQLException {
				        //System.out.println("myFunc called!");
				        String arg1="", arg2="";
				        double curYPos=0,charYPos=0;
				        double rslt;
				        try {
				            arg1 = value_text(0);
				            arg2 = value_text(1);
				            curYPos = SubRipper.toDouble(value_text(2));
				            charYPos = SubRipper.toDouble(value_text(3));
				            //System.out.println("function arg1:"+arg1 + " & arg2:"+arg2);
				            rslt=compOCR(arg1,arg2, compMode);

				            //System.out.println("function arg1:"+arg1 + " & arg2:"+arg2);

				            rslt=rslt-Math.abs(curYPos-charYPos)*SubRipper.vertPixMatchCoef;
				            result(rslt);
				        } catch (SQLException e) {
							prndeb(1,"ERROR addCustomFunction Calculation: " + e.getMessage());
				        }
				    }
				},  4, Function.FLAG_DETERMINISTIC);		// the number 4 is the number of expected arguments
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				prndeb(1,"ERROR addCustomFunction Create: " + e.getMessage());
			}
        	
        }
   
        public static String testCustom(String dbFile) {
            Statement stmt;
            PreparedStatement pstmt;  
            ResultSet rs;
            Connection conn=null;
            String query="";
        	
        	conn = dbConnect(dbFile);
        	addCustomFunction(conn);
        	
        	query = "select * from ("
        			+ "Select name, replace(name,'a','e') as nam2, binComp(name,replace(name,'a','e')) as rslt from params"
        			+ ") ORDER BY rslt desc";
        	
            try {
				stmt  = conn.createStatement();
				
		           rs    = stmt.executeQuery(query);
		        	
		            while (rs.next()) {	
		            	prndeb(0,rs.getString("name") + " vs " + rs.getString("nam2")  + " = " + rs.getString("rslt"));
		            }        
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
 	
        	try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	return "";
        }
                
}

