package constants;

public enum Job {

	MAGICIAN(200),
	CLERIC(230),
	PRIEST(231),
	BISHOP(232);
	
	public int jobid;
	
	private Job(int jobid) {
		this.jobid = jobid;
	}
	
	public boolean equals(int jobid) {
		return this.jobid == jobid;
	}
	
	public int getId() {
		return this.jobid;
	}
}
