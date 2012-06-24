package wolfgang.data.mapper;

public class Category {
	public final int id;
	public final String description;
	public final int balance;
	
	@Override
	@Deprecated
	public String toString() {
		StringBuilder sb = new StringBuilder("#Category{");
		sb.append("id=");
		sb.append(id);
		sb.append(", ");
		sb.append("description=");
		sb.append(description);
		sb.append(", ");
		sb.append("balance=");
		sb.append(balance);
		sb.append("}");
		return sb.toString();
	}

	public Category(Category c) {
		super();
		this.id = c.id;
		this.description = c.description;
		this.balance = c.balance;
	}
	public Category(int id, String description, int balance) {
		super();
		this.id = id;
		this.description = description;
		this.balance = balance;
	}
	
	public Category setDescription(String newDescription) {
		if(newDescription == null)
			throw new NullPointerException();
		if(newDescription.equals(description))
			return this;
		return new Category(id, newDescription, balance);
	}
	public Category setBalance(int newBalance) {
		if(newBalance == balance)
			return this;
		return new Category(id, description, newBalance);
	}
	public Category setAccumulator(boolean newAccumulator) {
		return new Category(id, description, balance);
	}
}