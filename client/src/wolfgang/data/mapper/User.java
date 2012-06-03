package wolfgang.data.mapper;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

public final class User extends _TableEntry {
	public final int id;
	public final String login;
	public final Date lastLogin;
	public final byte[] passwordHash;
	
	public User(User u) {
		super();
		this.id = u.id;
		this.login = u.login;
		this.lastLogin = u.lastLogin;
		this.passwordHash = u.passwordHash;
	}
	public User(int id, String login, Date lastLogin, String passwordHash) throws NoSuchAlgorithmException {
		super();
		this.id = id;
		this.login = login;
		this.lastLogin = lastLogin;
//		MessageDigest md = MessageDigest.getInstance("SHA-256");
//		md.update(passwordHash.getBytes());
//		this.passwordHash = md.digest();
		this.passwordHash = passwordHash.getBytes();
	}
}
