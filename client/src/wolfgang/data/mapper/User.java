package wolfgang.data.mapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class User extends _TableEntry {
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
	public User(int id, String login, Date lastLogin, String passwordHash) {
		super();
		this.id = id;
		this.login = login;
		this.lastLogin = lastLogin;
		this.passwordHash = passwordHash.getBytes();
	}
	
	public User setId(int newId) {
		if(newId == id)
			return this;
		User ret = new User(newId, login, lastLogin, passwordHash.toString());
		ret.setModified();
		return ret;
	}
	public User setLogin(String newLogin) {
		if(newLogin == null)
			throw new NullPointerException();
		if(newLogin.equals(login))
			return this;
		User ret = new User(id, newLogin, lastLogin, passwordHash.toString());
		ret.setModified();
		return ret;
	}
	public User setLastLogin(Date newLastLogin) {
		if(newLastLogin == null)
			throw new NullPointerException();
		if(newLastLogin == lastLogin)
			return this;
		User ret = new User(id, login, newLastLogin, passwordHash.toString());
		ret.setModified();
		return ret;
	}
	public User setPassword(String newPassword) throws NoSuchAlgorithmException {
		if(newPassword == null)
			throw new NullPointerException();
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(newPassword.getBytes());
		byte[] newPasswordHash = md.digest();
		// always return new, no matter this is the same pass
		User ret = new User(id, login, lastLogin, newPasswordHash.toString());
		ret.setModified();
		return ret;
	}
}
