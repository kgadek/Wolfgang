package wolfgang.data;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import wolfgang.data.mapper.Category;
import wolfgang.data.mapper.Operation;
import wolfgang.data.mapper.User;

public class DataMaster {
    public static final String DATABASE_FILE = "wolfgang.db";

    private static final String OPERATIONS_TABLENAME = "Operations";
    private static final String OPERATIONS_SCHEMA = "(Balance NUMERIC, Id INTEGER PRIMARY KEY, UserId INTEGER, CategoryId INTEGER, GroupId INTEGER, Repetitions INTEGER, RepetitionDiff INTEGER, " + "Completed INTEGER, DateStart integer)";
    private static final String CATEGORIES_SCHEMA = "(Id INTEGER PRIMARY KEY, Description TEXT, Balance NUMERIC, Accumulator NUMERIC)";
    private static final String CATEGORIES_TABLENAME = "Categories";
    //TODO LastLogin numeric czy jakiś double ?
    private static final String USERS_SCHEMA = "(Id integer PRIMARY KEY, Login text, PasswordHash text, LastLogin numeric)";
    private static final String USERS_TABLENAME = "Users";

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
        conn = DriverManager.getConnection("jdbc:sqlite:".concat(DATABASE_FILE));

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
                ResultSet rs = null;
                try {
                    rs = stat.executeQuery("select * from Users;");
                    while(rs.next()) {
                        User newUser = new User(rs.getInt("Id"), rs.getString("Login"), new Date(rs.getInt("LastLogin")), rs.getString("PasswordHash"));
                            users.put(rs.getInt("Id"), newUser);
                    }
                } finally {
                    if(rs!=null)
                        rs.close();
                }

                rs = null;
                try {
                    rs = stat.executeQuery("select * from Categories;");
                    while(rs.next()) {
                        Category newCat = new Category(rs.getInt("Id"), rs.getString("Description"), rs.getInt("Balance"), rs.getInt("Accumulator") > 0);
                        categories.put(rs.getInt("Id"), newCat);
                    }
                } finally {
                    if(rs!=null)
                        rs.close();
                }

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
                
                    logger.info("Users: "+users.size()+" Categories: "+categories.size()+" Operations: "+operations.size());

            } finally {
                stat.close();
                logger.exiting(this.getClass().getName(), "loadData");
            }
    }

    public static void main(String args[]) {
        try {
            logger.entering("-", "main");
            DataMaster dm = DataMaster.getInstance();

            User u = new User(0, "Konrad", new Date(), "");
            dm.addUser(u.setPassword("konradek"));

            logger.exiting("-", "main");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public User getUserById(User u) throws SQLException {
        ResultSet rs = null;
        try {
            PreparedStatement stat = conn.prepareStatement("select Id from ? where Id=?;");;
            stat.setString(1, USERS_TABLENAME);
            stat.setInt(2, u.id);
            stat.executeBatch();
            rs = stat.getResultSet();
            while(rs.next())
                return u.setId(rs.getInt("Id"));
        } finally {
            if(rs != null)
                rs.close();
        }
        return null;
    }
    public User getUserFromDBByValues(User u) throws SQLException {
        logger.entering(this.getClass().getName(), "getUserFromDBByValues");
        ResultSet rs = null;
        try {
            PreparedStatement stat = conn.prepareStatement("select Id from Users where Login=? and PasswordHash = ?;");
            stat.setString(1, u.login);
            stat.setString(2, bytesToString(u.passwordHash));
            stat.addBatch();
            rs = stat.getResultSet();
            logger.finest("rs = " + rs);
            while(rs.next()) {
                logger.finest("rs.next().getInt(Id) = " + rs.getInt("Id"));
                return u.setId(rs.getInt("Id"));
            }
        } finally {
            if(rs != null)
                rs.close();
        }
        return null;
    }
    public User addUser(User u) throws SQLException {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(u.lastLogin);
        
            PreparedStatement prep = conn.prepareStatement("insert into "+USERS_TABLENAME+
                                                           "(Login, PasswordHash, LastLogin) values (?, ?, ?);");
            prep.setString(1, u.login);
            prep.setString(2, bytesToString(u.passwordHash));
            prep.setDouble(3, cal.getTimeInMillis());
            prep.addBatch();

            conn.setAutoCommit(false);
            prep.executeBatch();
            conn.setAutoCommit(true);

            User ret = getUserFromDBByValues(u);
            users.put(ret.id, ret); // fail if ret == null
            return ret;
    }
    public User modUser(User u) throws SQLException {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(u.lastLogin);

        PreparedStatement prep = conn.prepareStatement("update ? set Login=?, PasswordHash=?, LastLogin=? where Id=?;");
        prep.setString(1, USERS_TABLENAME);
        prep.setString(2, u.login);
        prep.setString(3, bytesToString(u.passwordHash));
        prep.setDouble(4, cal.getTimeInMillis());
        prep.setInt   (5, u.id);
        prep.addBatch();

        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);

        User ret = getUserById(u);
        users.put(ret.id, ret);
        return ret;
    }
    public User delUser(User u) {
        // TODO
        return u;
    }
    public List<User> getUsers() {
        List<User> ret = new ArrayList<User>(users.size());
        for(User i : users.values())
            ret.add(i);
        return ret;
    }
    public User getUser(int id) {
        return null;
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

