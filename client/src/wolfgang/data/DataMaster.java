package wolfgang.data;

import java.util.HashMap;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DataMaster {
	private static DataMaster instance = null; 
	public static DataMaster getInstance() {
		if(instance == null)
			DataMaster.instance = new DataMaster();
		return DataMaster.instance;
	}
	private DataMaster() {
	}
	
	private HashMap<Integer, User> users = new HashMap<Integer, User>();
	private HashMap<Integer, Category> categories = new HashMap<Integer, Category>();
	private HashMap<Integer, Operation> operations = new HashMap<Integer, Operation>();
	
	public User addUser(User u) {
		throw new NotImplementedException();
	}
	public User modUser(User u) {
		throw new NotImplementedException();
	}
	public User delUser(User u) {
		throw new NotImplementedException();
	}
	public User[] getUsers() {
		return (User[]) users.values().toArray();
	}
	
	public Category addCategory(Category c) {
		throw new NotImplementedException();
	}
	public Category modCategory(Category c) {
		throw new NotImplementedException();
	}
	public Category delCategory(Category c) {
		throw new NotImplementedException();
	}
	public Category[] getCategories() {
		return (Category[]) categories.values().toArray();
	}
	
	public Operation addOperation(Operation o) {
		throw new NotImplementedException();
	}
	public Operation modOperation(Operation o) {
		throw new NotImplementedException();
	}
	public Operation delOperation(Operation o) {
		throw new NotImplementedException();
	}
	public Operation[] getOperations() {
		return (Operation[]) operations.values().toArray();
	}
}

