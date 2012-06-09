package wolfgang.data.mapper;

public class Category {
    public final int id;
    public final String description;
    public final int balance;
    public final Boolean accumulator;

    public Category(Category c) {
        super();
        this.id = c.id;
        this.description = c.description;
        this.balance = c.balance;
        this.accumulator = c.accumulator;
    }
    public Category(int id, String description, int balance, Boolean accumulator) {
        super();
        this.id = id;
        this.description = description;
        this.balance = balance;
        this.accumulator = accumulator;
    }

    public Category setId(int newId) {
        if(newId == id)
            return this;
        Category ret = new Category(newId, description, balance, accumulator);
        return ret;
    }
    public Category setDescription(String newDescription) {
        if(newDescription == null)
            throw new NullPointerException();
        if(newDescription.equals(description))
            return this;
        Category ret = new Category(id, newDescription, balance, accumulator);
        return ret;
    }
    public Category setBalance(int newBalance) {
        if(newBalance == balance)
            return this;
        Category ret = new Category(id, description, newBalance, accumulator);
        return ret;
    }
    public Category setAccumulator(boolean newAccumulator) {
        Category ret = new Category(id, description, balance, newAccumulator);
        return ret;
    }
}