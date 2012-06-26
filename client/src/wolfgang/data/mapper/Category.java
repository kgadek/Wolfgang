package wolfgang.data.mapper;

public class Category {
	public final int id;
	public final String description;
	public final int balance;
	public final User user;
	
	@Override
	@Deprecated
	public String toString() {
		StringBuilder sb = new StringBuilder("#Category{");
		sb.append("id=");
		sb.append(id);
		sb.append(", ");
		sb.append("user#User.id=");
		sb.append(user.id);
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
		this.user = c.user;
	}
	public Category(int id, User user, String description, int balance) {
		super();
		this.id = id;
		this.description = description;
		this.balance = balance;
		this.user = user;
	}
	
	public Category setDescription(String newDescription) {
		if(newDescription == null)
			throw new NullPointerException();
		if(newDescription.equals(description))
			return this;
		return new Category(id, user, newDescription, balance);
	}
	public Category setBalance(int newBalance) {
		if(newBalance == balance)
			return this;
		return new Category(id, user, description, newBalance);
	}
	public Category setUser(User newUser) {
		if(user == newUser)
			return this;
		if(newUser == null)
			throw new NullPointerException();
		return new Category(id, newUser, description, balance);
	}
}