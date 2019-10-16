package connect.mantis;

public class StatData {
	private String project;
	private String build;

	private int total_open = 0;
	private int total_closed = 0;
	private int low_severity_open = 0;
	private int low_severityclosed = 0;
	private int high_severity_open = 0;
	private int high_severity_closed = 0;
	
	
	public StatData(String project,String build) {
		this.project = project;
		this.build = build;
	}


	public int getTotal_open() {
		return total_open;
	}


	public void addTotal_open() {
		this.total_open ++;
	}


	public int getTotal_closed() {
		return total_closed;
	}


	public void addTotal_closed() {
		this.total_closed++;
	}


	public int getLow_severity_open() {
		return low_severity_open;
	}


	public void addLow_severity_open() {
		this.low_severity_open++;
	}


	public int getLow_severityclosed() {
		return low_severityclosed;
	}


	public void addLow_severityclosed() {
		this.low_severityclosed++;
	}


	public int getHigh_severity_open() {
		return high_severity_open;
	}


	public void addHigh_severity_open() {
		this.high_severity_open++;
	}


	public int getHigh_severity_closed() {
		return high_severity_closed;
	}


	public void addHigh_severity_closed() {
		this.high_severity_closed++;
	}


	public String getProject() {
		return project;
	}


	public String getBuild() {
		return build;
	}
}
