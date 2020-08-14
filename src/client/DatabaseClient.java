package client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import Main.Agent;

public class DatabaseClient {
	public Connection connect = null;
	public DatabaseClient(String database) {
		try {
			
			//This will load the MySQL driver, each DB has its own driver
			 Class.forName("com.mysql.jdbc.Driver"); 
			 //Connect to database
			 connect = DriverManager
				      .getConnection("jdbc:mysql://ltf5469.tam.us.siteprotect.com:3306/"+database, "tkuenzler","Tommy6847");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void close() {
		try {
			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String GetCurrentMonthColumns() {
		String sql = "SELECT MONTH(CURRENT_DATE()),YEAR(CURRENT_DATE())";
		Statement stmt = null;
		ResultSet set = null;
		try {
			stmt = connect.createStatement();
			set = stmt.executeQuery(sql);
			if(set.next()) 
				return set.getString("MONTH(CURRENT_DATE())")+"_"+set.getString("YEAR(CURRENT_DATE())");
			else
				return null;
		} catch(SQLException ex) {
			return "";
		} finally {
			try {
				if(stmt!=null)stmt.close();
				if(set!=null)set.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public boolean DoesColumnExist(String column) {
		String sql = "SHOW COLUMNS FROM `AGENTS`";
		Statement stmt = null;
		ResultSet set = null;
		try {
			stmt = connect.createStatement();
			set = stmt.executeQuery(sql);
			while(set.next()) {
				if(set.getString("Field").equalsIgnoreCase(column))
					return true;
			}
			return false;
		} catch(SQLException ex) {
			return false;
		} finally {
			try {
				if(stmt!=null)stmt.close();
				if(set!=null)set.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int AddColumn(String column) {
		String sql = "ALTER TABLE `AGENTS` ADD `"+column+"` VARCHAR(20) NOT NULL default '00:00:00'";
		Statement stmt = null;
		try {
			stmt = connect.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int SetDispositionLastUpdate(String agent) {
		String sql = "UPDATE `AGENTS` SET `DISPO_LAST_UPDATED` = '"+getCurrentDate("yyyy-MM-dd")+"' WHERE `AGENT` = '"+agent+"'";
		Statement stmt = null;
		try {
			stmt = connect.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int SetHoursLastUpdate(String agent) {
		String sql = "UPDATE `AGENTS` SET `HOURS_LAST_UPDATED` = '"+getCurrentDate("yyyy-MM-dd")+"' WHERE `AGENT` = '"+agent+"'";
		Statement stmt = null;
		try {
			stmt = connect.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int GetLeadsByMonth(String agent) {
		String leads = "SELECT COUNT(*) FROM `Leads` WHERE `agent` = '"+agent+"' AND MONTH(`DATE_ADDED`) = MONTH(CURRENT_DATE()) AND YEAR(`DATE_ADDED`) = YEAR(CURRENT_DATE())";
		String telmed = "SELECT COUNT(*) FROM `TELMED` WHERE `agent` = '"+agent+"' AND MONTH(`DATE_ADDED`) = MONTH(CURRENT_DATE()) AND YEAR(`DATE_ADDED`) = YEAR(CURRENT_DATE())";
		int total = 0;
		Statement stmt = null;
		ResultSet set = null;
		try {
			stmt = connect.createStatement();
			set = stmt.executeQuery(leads);
			if(set.next()) 
				total += set.getInt("COUNT(*)");
			set.close();
			set = stmt.executeQuery(telmed);
			if(set.next()) 
				total += set.getInt("COUNT(*)");
			return total;
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
				if(set!=null)set.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int GetLeadsByDateRange(String agent,String start,String end) {
		String leads = "SELECT COUNT(*) FROM `Leads` WHERE `agent` = '"+agent+"' AND `DATE_ADDED` >= '"+start+"' AND `DATE_ADDED` <= '"+end+"'";
		String telmed = "SELECT COUNT(*) FROM `TELMED` WHERE `agent` = '"+agent+"' AND `DATE_ADDED` >= '"+start+"' AND `DATE_ADDED` <= '"+end+"'";
		int total = 0;
		Statement stmt = null;
		ResultSet set = null;
		try {
			stmt = connect.createStatement();
			set = stmt.executeQuery(leads);
			if(set.next()) 
				total += set.getInt("COUNT(*)");
			set.close();
			set = stmt.executeQuery(telmed);
			if(set.next()) 
				total += set.getInt("COUNT(*)");
			return total;
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
				if(set!=null)set.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int UpdateEnrollmentsPerHour(String column,Agent agent) {
		String sql = "UPDATE `AGENTS` SET `"+column+"` = '"+agent.getEnrollmentsPerHour().toString()+"' WHERE `AGENT` = '"+agent.getName()+"'";
		Statement stmt = null;
		try {
			stmt = connect.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	public int UpdateAgentEnrollmentPercent(String agent,String amount) {
		String sql = "UPDATE `AGENTS` SET `EnrollmentPercent` = '"+amount+"' WHERE `AGENT` = '"+agent+"'";
		Statement stmt = null;
		try {
			stmt = connect.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException ex) {
			return 0;
		} finally {
			try {
				if(stmt!=null)stmt.close();
			} catch(SQLException ex) {
				
			}
		}
	}
	private String getCurrentDate(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format); 
		Date date = new Date(); 
		return formatter.format(date);
	}
}
