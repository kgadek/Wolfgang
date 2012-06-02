package wolfgang.data;

public abstract class _TableEntry {
	private boolean isModified = false;

	public boolean isModified() {
		return isModified;
	}

	public void setModified() {
		this.isModified = true;
	}
	
}
