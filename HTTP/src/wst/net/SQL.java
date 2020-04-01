package wst.net;
import java.io.*;
import java.sql.*;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
public class SQL {
    
    public String getEmotionWord()            
    {
       
    	StringBuffer sb = new StringBuffer(); 
    	
    	
        String connectionString = "jdbc:sqlserver://jangdeuksms.cafe24.com:1433;" +
            "databaseName=jangdeuksms;user=jangdeuksms;password=Jangduk**";
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
          
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionString);

            String SQL = "SELECT emotionWord, emotionPoint FROM tbSNSEmotionWord";
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL);

            while (rs.next()) {
            	sb.append(rs.getString(1) + "\t" + String.valueOf(rs.getInt(2)) + "\n");
                
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
            if (con != null) try { con.close(); } catch(Exception e) {}
        }
        
        return sb.toString();
    }

}
