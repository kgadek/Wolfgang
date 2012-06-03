package wolfgang.data.mapper;

import java.util.Date;

public final class Operation {
	public final int id;
	public final int balance;
	public final Category category;
	public final Integer groupId;
	public final Date dateStart;
	public final int repetitions;
	public final Date repetitionDiff;
	public final int completed;
	
	
	public Operation(Operation o) {
		super();
		this.id = o.id;
		this.balance = o.balance;
		this.category = o.category;
		this.groupId = o.groupId;
		this.dateStart = o.dateStart;
		this.repetitions = o.repetitions;
		this.repetitionDiff = o.repetitionDiff;
		this.completed = o.completed;
	}
	public Operation(int id, int balance, Category category, Integer groupId,
			Date dateStart, int repetitions, Date repetitionDiff, int completed) {
		super();
		this.id = id;
		this.balance = balance;
		this.category = category;
		this.groupId = groupId;
		this.dateStart = dateStart;
		this.repetitions = repetitions;
		this.repetitionDiff = repetitionDiff;
		this.completed = completed;
	}
}
