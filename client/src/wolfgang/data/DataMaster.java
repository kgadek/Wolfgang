package wolfgang.data;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import wolfgang.data.mapper.Category;
import wolfgang.data.mapper.Operation;
import wolfgang.data.mapper.User;

public class DataMaster {
	public static final String DATABASE_FILE = "wolfgang.db";

	private static final String USERS_TABLENAME = "Users";
	private static final String CATEGORIES_TABLENAME = "Categories";
	private static final String OPERATIONS_TABLENAME = "Operations";
	
	private static final String USERS_SCHEMA =
			"(Id integer PRIMARY KEY, Login text, PasswordHash text, LastLogin numeric)";
	private static final String CATEGORIES_SCHEMA =
			"(Id INTEGER PRIMARY KEY, UserId INTEGER NOT NULL, Description TEXT, Balance NUMERIC, " +
			"FOREIGN KEY(UserId) REFERENCES "+USERS_TABLENAME+"(Id))";
	private static final String OPERATIONS_SCHEMA =
			"(Id INTEGER PRIMARY KEY, Balance NUMERIC, UserId INTEGER NOT NULL, CategoryId INTEGER NOT NULL, " +
			"GroupId INTEGER, Repetitions INTEGER, RepetitionDiff INTEGER, Completed INTEGER, DateStart integer, " +
			"Description TEXT, FinalBalance NUMERIC, FOREIGN KEY(UserId) REFERENCES "+USERS_TABLENAME+"(id), " +
			"FOREIGN KEY(CategoryId) REFERENCES "+CATEGORIES_TABLENAME+"(id))";

	public static Logger logger = Logger.getLogger("WolfgangLog");
	private static DataMaster instance = null;
	private Connection conn = null;

	public static String bytesToString(byte[] arr) {
		StringBuilder sb = new StringBuilder();
		for(byte i : arr)
			sb.append(i);
		return sb.toString();
	}

	public static DataMaster getInstance() throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, SecurityException, IOException {
		if(instance == null)
			DataMaster.instance = new DataMaster();
		return DataMaster.instance;
	}

	private DataMaster() throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, SecurityException, IOException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);

		FileHandler fh = new FileHandler("wolfgang.log",true);
		fh.setFormatter(new SimpleFormatter());
		logger.addHandler(fh);
		logger.setLevel(Level.ALL);
		loadData();
	}

	private ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	private ConcurrentHashMap<Integer, Operation> operations = new ConcurrentHashMap<Integer, Operation>();
	private ConcurrentHashMap<Integer, Category> categories = new ConcurrentHashMap<Integer, Category>();

	public void loadData() throws SQLException, NoSuchAlgorithmException {
		logger.entering(this.getClass().getName(), "loadData");
		Statement stat = null;
		
		//conn.setAutoCommit(true);
		
		try {
			
			
			stat = conn.createStatement();
			
			logger.fine("Creating tables if they not exist");
			logger.finer("create table if not exists " + USERS_TABLENAME + " " + USERS_SCHEMA + ";");
			stat.executeUpdate("create table if not exists " + USERS_TABLENAME + " " + USERS_SCHEMA + ";");
			logger.finer("create table if not exists " + CATEGORIES_TABLENAME + " " + CATEGORIES_SCHEMA + ";");
			stat.executeUpdate("create table if not exists " + CATEGORIES_TABLENAME + " " + CATEGORIES_SCHEMA + ";");
			logger.finer("create table if not exists " + OPERATIONS_TABLENAME + " " + OPERATIONS_SCHEMA + ";");
			stat.executeUpdate("create table if not exists " + OPERATIONS_TABLENAME + " " + OPERATIONS_SCHEMA + ";");

			logger.fine("Clearing cache maps");
			users.clear();
			categories.clear();
			operations.clear();

			logger.fine("Reading tables data");
			
			logger.finer("Reading Users data");
			ResultSet rs = null;
			try {
				rs = stat.executeQuery("select * from "+USERS_TABLENAME+";");
				while(rs.next()) {
					User newUser = new User(rs.getInt("Id"), rs.getString("Login"), rs.getDate("LastLogin"), rs.getString("PasswordHash"));
					users.put(newUser.id, newUser);
				}
			} finally {
				if(rs!=null)
					rs.close();
				rs = null;
			}

			logger.finer("Reading Categories data");
			try {
				rs = stat.executeQuery("select * from Categories;");
				while(rs.next()) {
					Category newCat = new Category(rs.getInt("Id"), users.get(rs.getInt("UserId")), rs.getString("Description"), rs.getInt("Balance"));
					categories.put(newCat.id, newCat);
				}
			} finally {
				if(rs!=null)
					rs.close();
				rs = null;
			}

			logger.finer("Reading Operations data");
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
					Operation newOp = new Operation(rs.getInt("Id"), newOpUsr, newOpCat, rs.getInt("Balance"), rs.getInt("FinalBalance"), newOpGId,
							rs.getDate("DateStart"), rs.getInt("Repetitions"), rs.getDate("RepetitionDiff"), rs.getInt("Completed"),
							rs.getString("Description"));
					operations.put(newOp.id, newOp);
					
					newOp.user.balance += newOp.balance;
				}
			} finally {
				if(rs!=null)
					rs.close();
				rs = null;
			}

			logger.info("Users: "+users.size()+" Categories: "+categories.size()+" Operations: "+operations.size());

		} finally {
			stat.close();
			logger.exiting(this.getClass().getName(), "loadData");
		}
	}

	public static void main(String args[]) {
		try {
			DataMaster dm = DataMaster.getInstance();

			System.out.println("Users:");
			for(User u : dm.users.values())
				System.out.println("\t\t"+u);
			System.out.println("Categories:");
			for(Category c : dm.categories.values())
				System.out.println("\t\t"+c);
			System.out.println("Operations:");
			for(Operation o : dm.operations.values())
				System.out.println("\t\t"+o);

			User uKonrad = dm.createOrGetUser("Konrad", new Date(), "ala123");
			Category cZakupy = dm.createOrGetCategory(uKonrad, "Zakupy", 0);
			Category cWyplata = dm.createOrGetCategory(uKonrad, "Wypłata", 0);
			Category cKredyt = dm.createOrGetCategory(uKonrad, "Kredyt", 0);
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			dm.createOperation(uKonrad, cWyplata, 1000000, 1000000, 0, formatter.parse("2012-04-01 00:00"),
					0, null, 1, "");
			dm.createOperation(uKonrad, cZakupy, -900000, 100000, 0, formatter.parse("2012-04-02 13:37"),
					0, null, 1, "Bo plazma musi być");
			dm.createOperation(uKonrad, cKredyt, 800000, 900000, 0, formatter.parse("2012-04-03 14:02"),
					0, null, 1, "Damn, a jednak to była pomyłka...");
			dm.createOperation(uKonrad, cZakupy, -900000, 0, 0, formatter.parse("2012-04-03 14:15"),
					0, null, 1, "I to by było na koniec w tym m-cu");
					
			
//			uKonrad = dm.updateUser(uKonrad.setLogin("Konrad2"));
//			cAuto = dm.updateCategory(cAuto.setBalance(100));
//			op1 = dm.updateOperation(op1.setBalance(1000));
			
//			dm.removeOperation(op1);
//			dm.removeCategory(cAuto);
//			dm.removeUser(uKonrad);

			System.out.println();
			System.out.println("====================");
			System.out.println();

			System.out.println("Users:");
			for(User u : dm.users.values())
				System.out.println("\t\t"+u);
			System.out.println("Categories:");
			for(Category c : dm.categories.values())
				System.out.println("\t\t"+c);
			System.out.println("Operations:");
			for(Operation o : dm.operations.values())
				System.out.println("\t\t"+o);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public User checkLogin(String login, String password) {
		try {
			String passwordHash = User.genPasswordHash(password, login);
			for(User u : users.values()) {
				if(u.login.equals(login) && u.passwordHash.equals(passwordHash))
					return u;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Operation createOperation(User user, Category category, int balance, int finalBalance, Integer groupId, Date dateStart,
			int repetitions, Date repetitionDiff, int completed, String description) throws SQLException {

		Operation ret = null;
		try {
			if(groupId != null && groupId == 0)
				groupId = null;

			PreparedStatement prepInsert = conn.prepareStatement("insert into "+OPERATIONS_TABLENAME+
					" (Balance, UserId, CategoryId, GroupId, Repetitions, RepetitionDiff, Completed," +
					" DateStart, Description, FinalBalance)" +
					" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			PreparedStatement prepSelect = conn.prepareStatement("select last_insert_rowid();");

			prepInsert.setInt(1, balance);
			prepInsert.setInt(2, user.id);
			prepInsert.setInt(3, category.id);
			prepInsert.setInt(4, groupId == null ? 0 : groupId);
			prepInsert.setInt(5, repetitions);
			prepInsert.setLong(6, repetitionDiff == null ? 0 : repetitionDiff.getTime());
			prepInsert.setInt(7, completed);
			prepInsert.setDate(8, dateStart == null ? null : new java.sql.Date(dateStart.getTime()));
			prepInsert.setString(9, description);
			prepInsert.setInt(10, finalBalance);
			prepInsert.addBatch();

			//conn.setAutoCommit(false);
			prepInsert.executeBatch();
			ResultSet rs = prepSelect.executeQuery();
			//conn.setAutoCommit(true);

			int newId = 0;
			while(rs.next())
				newId = rs.getInt(1);
			ret = new Operation(newId, users.get(user.id), categories.get(category.id), balance, finalBalance, groupId,
					dateStart, repetitions, repetitionDiff, completed, description);
			operations.put(newId, ret);
			user.balance += balance;
			//conn.commit();
		} catch (SQLException e) {
			throw e;
		} finally {
			//conn.rollback();
		}

		return ret;

	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private Operation updateOperation(Operation o) throws SQLException {
		try {
			PreparedStatement prepInsert = conn.prepareStatement("update "+OPERATIONS_TABLENAME+
					" set Balance = ? , UserId = ? , CategoryId = ? , GroupId = ? , Repetitions = ? ," +
					" RepetitionDiff= ?, Completed= ?, DateStart= ?, Description= ?, FinalBalance= ? where Id = ?;");

			prepInsert.setInt(1, o.balance);
			prepInsert.setInt(2, o.user.id);
			prepInsert.setInt(3, o.category.id);
			prepInsert.setInt(4, o.groupId == null ? 0 : o.groupId);
			prepInsert.setInt(5, o.repetitions);
			prepInsert.setDate(6, o.repetitionDiff == null ? null : new java.sql.Date(o.repetitionDiff.getTime()));
			prepInsert.setInt(7, o.completed);
			prepInsert.setDate(8, new java.sql.Date(o.dateStart.getTime()));
			prepInsert.setString(9, o.description);
			prepInsert.setInt(10, o.finalBalance);
			prepInsert.setInt(11, o.id);
			prepInsert.addBatch();

			prepInsert.executeBatch();
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}
		return o;
	}

	public Operation removeOperationById(int id) throws SQLException {
		return removeOperation(operations.get(id));
	}
	
	public Operation removeOperation(Operation o) throws SQLException {
		try {
			PreparedStatement prepInsert = conn.prepareStatement("delete from "+OPERATIONS_TABLENAME+" where Id = ? ;");
			prepInsert.setInt(1, o.id);
			prepInsert.addBatch();
			
			prepInsert.executeBatch();
			operations.remove(o.id);
			//conn.commit();
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}
		return o;
	}

	
	public Category createOrGetCategory(User owner, String description, int balance) throws SQLException {
		Category ret = createCategory(owner, description, balance);		
		if(ret == null)
			ret = getCategoryByName(description);
		return ret;
	}

	private Category createCategory(User owner, String description, int balance) throws SQLException {

		Category ret = null;
		try {
			PreparedStatement prepGet = conn.prepareStatement("select Id from "+CATEGORIES_TABLENAME+" where UserId = ? and description = ?;");
			prepGet.setInt(1, owner.id);
			prepGet.setString(2, description);
			
			
			PreparedStatement prepInsert = conn.prepareStatement("insert into "+CATEGORIES_TABLENAME+" (Description, Balance, UserId) values (?, ?, ?);");
			PreparedStatement prepSelect = conn.prepareStatement("select last_insert_rowid();");

			prepInsert.setString(1, description);
			prepInsert.setInt(2, balance);
			prepInsert.setInt(3, owner.id);
			prepInsert.addBatch();

			//conn.setAutoCommit(false);
			ResultSet rsGet = prepGet.executeQuery();
			while(rsGet.next()) {
				rsGet.close();
				//conn.rollback();
				return null;
			}
			rsGet.close();
			prepInsert.executeBatch();
			ResultSet rs = prepSelect.executeQuery();
			

			int newId = 0;
			while(rs.next())
				newId = rs.getInt(1);
			rs.close();
			//conn.commit();
			//conn.setAutoCommit(true);
			
			ret = new Category(newId, owner, description, balance);
			categories.put(newId, ret);
		} catch (SQLException e) {
			//conn.rollback();
			//conn.setAutoCommit(false);
			//conn.rollback();
			throw e;
		} finally {
			//conn.setAutoCommit(true);
		}

		return ret;
	}
	
	@SuppressWarnings("unused")
	private Category updateCategory(Category c) throws SQLException {
		try {
			PreparedStatement prepInsert = conn.prepareStatement("update "+CATEGORIES_TABLENAME+" set Description = ?, Balance = ?, UserId = ? where Id = ?;");
			prepInsert.setString(1, c.description);
			prepInsert.setInt(2, c.balance);
			prepInsert.setInt(3, c.user.id);
			prepInsert.setInt(4, c.id);
			prepInsert.addBatch();

			prepInsert.executeBatch();
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}
		return c;
	}

	@SuppressWarnings("unused")
	private Category removeCategory(Category c) throws SQLException {
		try {
			PreparedStatement prepInsert = conn.prepareStatement("delete from "+CATEGORIES_TABLENAME+" where Id = ? ;");
			prepInsert.setInt(1, c.id );
			prepInsert.addBatch();

			prepInsert.executeBatch();
			categories.remove(c.id);
			//conn.commit();
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}
		return c;
	}
	
	public User createOrGetUser(String login, Date lastLogin, String password) throws SQLException {
		User ret = createUser(login, lastLogin, password);
		if(ret == null)
			for(Entry<Integer, User> i : users.entrySet())
				if(i.getValue().login.equals("Konrad"))
					return i.getValue();
		return ret;
	}
	
	private User createUser(String login, Date lastLogin, String password) throws SQLException {
		
		User ret = null;
		ResultSet rsGet = null;
		PreparedStatement prepInsert = null;
		PreparedStatement prepSelect = null;
		try {
			String passwordHash;
			passwordHash = null;
			try {
				passwordHash = User.genPasswordHash(password, login);
			} catch (NoSuchAlgorithmException e) {
				logger.throwing(this.getClass().getName(), "createUser", e);
				e.printStackTrace();
				return null;
			}
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(lastLogin);

			//conn.setAutoCommit(false);
			
			PreparedStatement prepGet = conn.prepareStatement("select Id from "+USERS_TABLENAME+" where Login = ?;");
			prepGet.setString(1, login);
			
			prepInsert = conn.prepareStatement("insert into "+USERS_TABLENAME+" (Login, PasswordHash, LastLogin) values (?, ?, ?);");
			prepSelect = conn.prepareStatement("select last_insert_rowid();");
			
			prepInsert.setString(1, login);
			prepInsert.setString(2, passwordHash);
			prepInsert.setDouble(3, cal.getTimeInMillis());
			prepInsert.addBatch();
			
			rsGet = prepGet.executeQuery();
			prepGet.close();
			while(rsGet.next())
				return null;
			rsGet.close();
			prepInsert.executeBatch();
			prepInsert.close();
			ResultSet rs = prepSelect.executeQuery();
			
			
			int newId = 0;
			while(rs.next())
				newId = rs.getInt(1);
			rs.close();
			prepSelect.close();
			//conn.commit();
			
			ret = new User(newId, login, lastLogin, passwordHash);
			users.put(newId, ret);
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		} finally {
			if(rsGet!=null)
				rsGet.close();
			if(prepInsert!=null)
				prepInsert.close();
			if(prepSelect!=null)
				prepSelect.close();
			//conn.setAutoCommit(true);
		}

		return ret;
	}
	
	public User updateUser(User u) throws SQLException {

		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(u.lastLogin);

			PreparedStatement prepInsert = conn.prepareStatement("update "+USERS_TABLENAME+" set Login = ?, PasswordHash = ?, LastLogin = ? where Id = ?;");

			prepInsert.setString(1, u.login);
			prepInsert.setString(2, u.passwordHash);
			prepInsert.setDouble(3, cal.getTimeInMillis());
			prepInsert.setInt(4, u.id);
			prepInsert.addBatch();

			prepInsert.executeBatch();
			
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}

		return u;
	}
	
	public User removeUser(User u) throws SQLException {
		try {
			PreparedStatement prepInsert = conn.prepareStatement("delete from "+USERS_TABLENAME+" where Id = ? ;");
			prepInsert.setInt(1, u.id);
			prepInsert.addBatch();

			prepInsert.executeBatch();
			users.remove(u.id);
			//conn.commit();
		} catch (SQLException e) {
			//conn.rollback();
			throw e;
		}
		return u;
	}
	
	public Category getCategoryByName(String name) {
		for(Entry<Integer, Category> i : categories.entrySet())
			if(i.getValue().description.equals(name))
				return i.getValue();
		return null;
	}

	public Collection<User> getUsers() {
		return users.values();
	}
	public Collection<Category> getCategories() {
		return categories.values();
	}	
	public Collection<Operation> getOperations() {
		return operations.values();
	}
}

