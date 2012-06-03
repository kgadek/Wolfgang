package wolfgang.data.mapper;

import java.util.Date;

public class Operation extends _TableEntry {
	public final int id;
	public final User user;
	public final Category category;
	public final Integer groupId;
	public final int balance;
	public final Date dateStart;
	public final int repetitions;
	public final Date repetitionDiff;
	public final int completed;
	
	
	public Operation(Operation o) {
		super();
		this.id = o.id;
		this.user = o.user;
		this.balance = o.balance;
		this.category = o.category;
		this.groupId = o.groupId;
		this.dateStart = o.dateStart;
		this.repetitions = o.repetitions;
		this.repetitionDiff = o.repetitionDiff;
		this.completed = o.completed;
	}
	public Operation(int id, User user, Category category, int balance, Integer groupId,
			Date dateStart, int repetitions, Date repetitionDiff, int completed) {
		super();
		this.id = id;
		this.user = user;
		this.balance = balance;
		this.category = category;
		this.groupId = groupId;
		this.dateStart = dateStart;
		this.repetitions = repetitions;
		this.repetitionDiff = repetitionDiff;
		this.completed = completed;
	}
	
	public Operation setId(int newId) {
		if(newId == id)
			return this;
		Operation ret = new Operation(newId, user, category, balance, groupId, dateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setUser(User newUser) {
		if(newUser == null)
			throw new NullPointerException();
		if(newUser == user)
			return this;
		Operation ret = new Operation(id, newUser, category, balance, groupId, dateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setBalance(int newBalance) {
		if(newBalance == balance)
			return this;
		Operation ret = new Operation(id, user, category, newBalance, groupId, dateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setCategory(Category newCategory) {
		if(newCategory == null)
			throw new NullPointerException();
		if(newCategory == category)
			return this;
		Operation ret = new Operation(id, user, newCategory, balance, groupId, dateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setGroupId(Integer newGroupId) {
		// we allow null
		if(newGroupId == groupId)
			return this;
		Operation ret = new Operation(id, user, category, balance, newGroupId, dateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setDateStart(Date newDateStart) {
		if(newDateStart == null)
			throw new NullPointerException();
		if(newDateStart == dateStart)
			return this;
		Operation ret = new Operation(id, user, category, balance, groupId, newDateStart, repetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setRepetitions(int newRepetitions) {
		if(newRepetitions == repetitions)
			return this;
		Operation ret = new Operation(id, user, category, balance, groupId, dateStart, newRepetitions, repetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setRepetitionDiff(Date newRepetitionDiff) {
		// we allow null iff repetitions == 0
		if(repetitions != 0 && newRepetitionDiff == null)
			throw new NullPointerException();
		if(newRepetitionDiff == repetitionDiff)
			return this;
		Operation ret = new Operation(id, user, category, balance, groupId, dateStart, repetitions, newRepetitionDiff, completed);
		ret.setModified();
		return ret;
	}
	public Operation setCompleted(int newCompleted) {
		if(newCompleted == completed)
			return this;
		Operation ret = new Operation(id, user, category, balance, groupId, dateStart, repetitions, repetitionDiff, newCompleted);
		ret.setModified();
		return ret;
	}
	
}
