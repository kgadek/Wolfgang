package wolfgang.data.mapper;

public abstract class _TableEntry {
	private boolean isModified = false;
	private boolean isDeleted = false;

	public boolean isModified() {
		return isModified;
	}
	public void setModified() {
		this.isModified = true;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted() {
		this.isDeleted = true;
	}
}
