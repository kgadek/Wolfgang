package wolfgang.data.mapper;

public final class Category extends _TableEntry {
	public final int id;
	public final String description;
	public final Integer balance;
	public final Boolean accumulator;

	public Category(Category c) {
		super();
		this.id = c.id;
		this.description = c.description;
		this.balance = c.balance;
		this.accumulator = c.accumulator;
	}
	public Category(int id, String description, Integer balance, Boolean accumulator) {
		super();
		this.id = id;
		this.description = description;
		this.balance = balance;
		this.accumulator = accumulator;
	}
}