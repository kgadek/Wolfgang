package wolfgang.data;

public final class Category extends _TableEntry {
	public final int id;
	public final String description;
	public final Integer balance;
	public final Boolean accumulator;

	public Category(int id, String description, Integer balance, Boolean accumulator) {
		super();
		this.id = id;
		this.description = description;
		this.balance = balance;
		this.accumulator = accumulator;
	}
}