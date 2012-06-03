package wolfgang.data;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import wolfgang.data.mapper.Category;
import wolfgang.data.mapper.Operation;
import wolfgang.data.mapper.User;

public class DataMaster {
	public static final String DATABASE_FILE = "wolfgang.db";
	
	public static Logger logger = Logger.getLogger("WolfgangLog");
	
	private static DataMaster instance = null; 
	public static DataMaster getInstance() throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
		if(instance == null)
			DataMaster.instance = new DataMaster();
		return DataMaster.instance;
	}
	
	private Connection conn = null;
	private DataMaster() throws ClassNotFoundException, SQLException, NoSuchAlgorithmException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:".concat(DATABASE_FILE));
		loadData();
	}
	
	private ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	private ConcurrentHashMap<Integer, Operation> operations = new ConcurrentHashMap<Integer, Operation>();
	private ConcurrentHashMap<Integer, Category> categories = new ConcurrentHashMap<Integer, Category>();
	
	public void loadData() throws SQLException, NoSuchAlgorithmException {
		
		Statement stat = null;
		
		try {
			stat = conn.createStatement();
			
			users.clear();
			categories.clear();
			operations.clear();
			
			ResultSet rs = null;
			try {
				rs = stat.executeQuery("select * from Users;");
				while(rs.next()) {
					User newUser = new User(rs.getInt("Id"), rs.getString("Login"), new Date(rs.getInt("LastLogin")), rs.getString("PasswordHash"));
					users.put(rs.getInt("Id"), newUser);
				}
			} finally { if(rs!=null) rs.close(); }
			
			rs = null;
			try {
				rs = stat.executeQuery("select * from Categories;");
				while(rs.next()) {
					Category newCat = new Category(rs.getInt("Id"), rs.getString("Description"), rs.getInt("Balance"), rs.getInt("Accumulator") > 0);
					categories.put(rs.getInt("Id"), newCat);
				}
			} finally { if(rs!=null) rs.close(); }
			
			rs = null;
			try {
				rs = stat.executeQuery("select * from Operations;");
				while(rs.next()) {
					Category newOpCat = categories.get(rs.getInt("CategoryId"));
					if(newOpCat == null) {
						logger.log(Level.SEVERE, "Database error");
						logger.log(Level.SEVERE, "Operation (id="+rs.getInt("Id")+") has unknown category (catId="+rs.getInt("CategoryId")+")");
						logger.log(Level.SEVERE, "Skipping row");
						continue;
					}
					User newOpUsr = users.get(rs.getInt("UserId"));
					if(newOpUsr == null) {
						logger.log(Level.SEVERE, "Database error");
						logger.log(Level.SEVERE, "Operation (id="+rs.getInt("Id")+") has unknown user (userId="+rs.getInt("UserId")+")");
						logger.log(Level.SEVERE, "Skipping row");
						continue;
					}
					Integer newOpGId = null;
					if(rs.getInt("GroupId") != 0)
						newOpGId = rs.getInt("GroupId");
					Operation newOp = new Operation(rs.getInt("Id"), newOpUsr, newOpCat, rs.getInt("Balance"), newOpGId,
							new Date(rs.getInt("DateStart")), rs.getInt("Repetitions"), new Date(rs.getInt("RepetitionDiff")),
							rs.getInt("Completed"));
					operations.put(rs.getInt("Id"), newOp);
				}
			} finally { if(rs!=null) rs.close(); }
			
			logger.log(Level.INFO, "Users: "+users.size()+" Categories: "+categories.size()+" Operations: "+operations.size());
			
		} finally {
			stat.close();
		}
	}
	
	public void saveData() {
	}
	
	public static void main(String args[]) throws Exception {
		logger.addHandler(new FileHandler("wolfgang.log",true));
		logger.setLevel(Level.ALL);
		
		DataMaster.getInstance();
	}
	
	public User addUser(User u) throws SQLException {
		User ret = new User(u);
		Statement stat = null;
		try {
		} finally { if(stat != null) stat.close(); }
		return ret;
	}
	public User modUser(User u) {
		if(!users.containsKey(u))
			throw new NullPointerException();
		User ret = new User(u);
		ret.setModified();
		users.put(u.id, ret);
		return ret;
	}
	public User delUser(User u) {
		if(!users.containsKey(u))
			throw new NullPointerException();
		User ret = users.get(u.id);
		ret.setDeleted();
		users.put(u.id, ret);
		return ret;
	}
	public List<User> getUsers() {
		List<User> ret = new ArrayList<User>(users.size());
		for(User i : users.values()) 
			if(!i.isDeleted())
				ret.add(i);
		return ret;
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

