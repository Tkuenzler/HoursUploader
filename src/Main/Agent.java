package Main;

import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class Agent {
	SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	String name;
	TimePart acw,ready,not_ready,call,ring,preview;
	TimePart paidTime,enrollmentsPerHour;
	public Agent(String name) {
		this.name = name;
		 df.setTimeZone(TimeZone.getTimeZone("UTC"));
		 paidTime = TimePart.parse("00:00:00");
	}
	public void setACW(String acw) {
		if(acw.equalsIgnoreCase("-"))
			this.acw = TimePart.parse("00:00:00");
		else
			this.acw = TimePart.parse(acw);
	}
	public void setReady(String ready) {
		if(ready.equalsIgnoreCase("-"))
			this.ready = TimePart.parse("00:00:00");
		else
			this.ready =TimePart.parse(ready);
	}
	public void setNotReady(String not_ready) {
		if(not_ready.equalsIgnoreCase("-"))
			this.not_ready = TimePart.parse("00:00:00");
		else
			this.not_ready = TimePart.parse(not_ready);
	}
	public void setCall(String call) {
		if(call.equalsIgnoreCase("-"))
			this.call = TimePart.parse("00:00:00");
		else
			this.call = TimePart.parse(call);
	}
	public void setRing(String ring) {
		if(ring.equalsIgnoreCase("-"))
			this.ring = TimePart.parse("00:00:00");
		else
			this.ring = TimePart.parse(ring);
	}
	public void setPreview(String preview) {
		if(preview.equalsIgnoreCase("-"))
			this.preview = TimePart.parse("00:00:00");
		else
			this.preview = TimePart.parse(preview);
	}
	public SimpleDateFormat getDf() {
		return df;
	}
	public String getName() {
		return name;
	}
	public TimePart getAcw() {
		return acw;
	}
	public TimePart getReady() {
		return ready;
	}
	public TimePart getNot_ready() {
		return not_ready;
	}
	public TimePart getCall() {
		return call;
	}
	public TimePart getRing() {
		return ring;
	}
	public TimePart getPaidTime() {
		return paidTime;
	}
	public TimePart getEnrollmentsPerHour() {
		return enrollmentsPerHour;
	}
	public void addTime(TimePart date) {
       paidTime.add(date);
	}
	public void setEnrollmentsPerHour(int enrollments) {
		int seconds = paidTime.seconds+(paidTime.minutes*60)+((paidTime.hours*60)*60);
		double ratio = Math.round(seconds/enrollments);
		int hour = (int) Math.floor(ratio/3600);
		int minute = (int) Math.floor((ratio%3600)/60);
		int second = (int) (ratio-((hour*3600)+(minute*60)));
		enrollmentsPerHour = TimePart.parse(hour+":"+minute+":"+second);
	}
	public static class TimePart {
	    int hours = 0;
	    int minutes = 0;
	    int seconds = 0;

	    static TimePart parse(String in) {
	        if (in != null) {
	            String[] arr = in.split(":");
	            TimePart tp = new TimePart();
	            tp.hours = ((arr.length >= 1) ? Integer.parseInt(arr[0]) : 0);
	            tp.minutes = ((arr.length >= 2) ? Integer.parseInt(arr[1]) : 0);
	            tp.seconds = ((arr.length >= 3) ? Integer.parseInt(arr[2]) : 0);
	            return tp;
	        }
	        return null;
	    }

	    public TimePart add(TimePart a) {
	        this.seconds += a.seconds;
	        int of = 0;
	        while (this.seconds >= 60) {
	            of++;
	            this.seconds -= 60;
	        }
	        this.minutes += a.minutes + of;
	        of = 0;
	        while (this.minutes >= 60) {
	            of++;
	            this.minutes -= 60;
	        }
	        this.hours += a.hours + of;
	        return this;
	    }

	    @Override
	    public String toString() {
	        return String.format("%02d:%02d:%02d", hours, minutes,
	                seconds);
	    }
	}
}
