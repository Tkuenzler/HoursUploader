package Main;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import client.DatabaseClient;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import subframes.FileChooser;

public class Main {
	private static final int AGENT = 0;
	private static final int ACW = 6;
	private static final int NOT_READY = 7;
	private static final int ON_CALL = 8;
	private static final int PREVIEW = 9;
	private static final int READY = 9;
	private static final int RING = 10;
	private static final int TOTAL_CALLS = 24;
	private static int ADD = 0;
	private static DecimalFormat df = new DecimalFormat("#.######");
	public static void main(String[] args) {
		df.setRoundingMode(RoundingMode.CEILING);
		String[] reports = {"Hourly Report","Disposition Report"};
		String report = (String) JOptionPane.showInputDialog(new JFrame(), "Select which report", "Reports:", JOptionPane.QUESTION_MESSAGE, null, reports, reports[0]);
		if(report.equalsIgnoreCase(reports[0]))
			UploadLast30DaysHours();
		else if(report.equalsIgnoreCase(reports[1]))
			UpLoadLast30DaysDispositions();
		System.exit(0);
	}
	public static void UpLoadLast30DaysDispositions() {
		File file = FileChooser.OpenXlsFile("Open Disposition"); 
		Workbook workbook = null;
		DatabaseClient client = new DatabaseClient("MT_MARKETING");
		DatabaseClient info = new DatabaseClient("Info_Table");
		try {
			workbook = Workbook.getWorkbook(file);
			if(workbook==null)
				return;
			Sheet sheet = workbook.getSheet(0);
			String report = sheet.getRow(1)[0].getContents().split("-")[1].trim();
			if(!report.equalsIgnoreCase("Agent Disposition Summary Last 30 Days")) {
				JOptionPane.showMessageDialog(null, "WRONG REPORT");
				return;
			}
			System.out.println(report);
			String group = sheet.getRow(6)[4].getContents();
			String start = ExtractDate(sheet.getRow(4)[4].getContents());
			String end = ExtractDate(sheet.getRow(5)[4].getContents());
			if(start==null || end==null) {
				JOptionPane.showMessageDialog(null, "Start or End time is null");
				return;
			}
			int agentRows = 0;
			for(int row = 6;row<sheet.getRows();row++) {
				Cell[] data = sheet.getRow(row);
				for(Cell cell: data) {
					if(cell.getContents().startsWith("AGENT GROUP")) {
						agentRows = row+1;
						break;
					}
				}
			}
			for(int row = agentRows;row<sheet.getRows();row++) {
				Cell[] data = sheet.getRow(row);
				if(data[0].getContents().equalsIgnoreCase("  "))
					break;
				int count = 0;
				String agent = data[0].getContents();
				double totalCalls = Double.parseDouble(data[data.length-1].getContents());
				double enrollments = (double)client.GetLeadsByDateRange(agent,start,end);
				double enrollmentPercent = enrollments/totalCalls;
				int update = info.UpdateAgentEnrollmentPercent(agent, df.format(enrollmentPercent));
				System.out.println(update);
				if(update==1)
					info.SetDispositionLastUpdate(agent);
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		}finally {
			if(client.connect!=null)client.close();
			if(info.connect!=null)info.close();
			if (workbook != null)workbook.close();
			System.out.println("CLOSED");
		}
		JOptionPane.showMessageDialog(null, "COMPLETED");
		System.exit(0);
	}
	public static void UploadLast30DaysHours() {
		File file = FileChooser.OpenXlsFile("Open Statement"); 
		Workbook workbook = null;
		DatabaseClient client = new DatabaseClient("MT_MARKETING");
		DatabaseClient info = new DatabaseClient("Info_Table");
		try {
			workbook = Workbook.getWorkbook(file);
			if(workbook==null)
				return;
			Sheet sheet = workbook.getSheet(0);
			String report = sheet.getRow(1)[0].getContents().split("-")[1].trim();
			if(!report.equalsIgnoreCase("LAST 30 DAYS HOURS")) {
				JOptionPane.showMessageDialog(null, "WRONG REPORT");
				return;
			}
			System.out.println(report);
			String group = sheet.getRow(6)[4].getContents();
			String start = ExtractDate(sheet.getRow(4)[4].getContents());
			String end = ExtractDate(sheet.getRow(5)[4].getContents());
			if(start==null || end==null) {
				JOptionPane.showMessageDialog(null, "Start or End time is null");
				return;
			}
			int agentRows = 0;
			if(hasPreview(sheet.getRow(19)))
				ADD = 1;
			for(int row = 6;row<sheet.getRows();row++) {
				Cell[] data = sheet.getRow(row);
				for(Cell cell: data) {
					if(cell.getContents().startsWith("AGENT GROUP")) {
						agentRows = row+1;
						break;
					}
				}
			}
			String column = "EPH";
			for(int row = agentRows;row<sheet.getRows();row++) {
				Cell[] data = sheet.getRow(row);
				if(data[0].getContents().equalsIgnoreCase("  "))
					break;
				Agent agent = new Agent(data[AGENT].getContents().trim());
				agent.setACW(data[ACW].getContents().trim());
				agent.setNotReady(data[NOT_READY].getContents().trim());
				agent.setReady(data[READY+ADD].getContents().trim());
				agent.setCall(data[ON_CALL].getContents().trim());
				agent.setRing(data[RING+ADD].getContents().trim());
				if(group.startsWith("Staff Outsourcing")) 
					agent.addTime(agent.getAcw());
				agent.addTime(agent.getCall());
				agent.addTime(agent.getRing());
				agent.addTime(agent.getReady());
				System.out.println(row);
				int enrollments = client.GetLeadsByDateRange(agent.getName(),start,end);
				String paidTime = agent.getPaidTime().toString();
				System.out.println(agent.getName()+" "+enrollments+" "+paidTime);
				if(enrollments==0)
					continue;
				agent.setEnrollmentsPerHour(enrollments);
				int update = info.UpdateEnrollmentsPerHour(column, agent);
				System.out.println(update);
				if(update==1)
					info.SetHoursLastUpdate(agent.getName());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "THERE WAS AN ERROR: "+e.getMessage());
			System.exit(0);
		}  finally {
			client.close();
			info.close();
			if (workbook != null)workbook.close();
				System.out.println("CLOSED");
		}
		JOptionPane.showMessageDialog(null, "COMPLETED");
		System.exit(0);
	}
	public static String ExtractDate(String date) throws ParseException {
		StringBuilder newDate = new StringBuilder();
		String[] data = date.split(" ");
		newDate.append(data[2]+"-");
		switch(data[0]) {
			case "Jan":
				newDate.append("01-");
				break;
			case "Feb":
				newDate.append("02-");
				break;
			case "Mar":
				newDate.append("03-");
				break;
			case "Apr":
				newDate.append("04-");
				break;
			case "May":
				newDate.append("05-");
				break;
			case "Jun":
				newDate.append("06-");
				break;
			case "Jul":
				newDate.append("07-");
				break;
			case "Aug":
				newDate.append("08-");
				break;
			case "Sep":
				newDate.append("09-");
				break;
			case "Oct":
				newDate.append("10-");
				break;
			case "Nov":
				newDate.append("11-");
				break;
			case "Dec":
				newDate.append("12-");
				break;
			default:
				return null;
		}
		newDate.append(data[1].replace(",", ""));
		DateFormat df = new SimpleDateFormat("hh:mm:ss aa");
		DateFormat outputformat = new SimpleDateFormat("HH:mm:ss");
		Date time = null;
	    String output = null;
	    time= df.parse(data[3]+" "+data[4]);
	    output = outputformat.format(time);
	    newDate.append(" "+output);
	    //System.out.println(output);
		return newDate.toString();
	}
	private static boolean hasPreview(Cell[] row) {
		System.out.println("ROW: 22");
		for(Cell cell: row) {
			System.out.println("CONTENTS: "+cell.getContents().trim());
			if(cell.getContents().trim().equalsIgnoreCase("On Preview"))
				return true;
		}
		return false;
	}
}
