package wolfgang.data.mapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import wolfgang.data.DataMaster;

public class User {
	public final int id; // ustalane przez applikację, zero gdy nie wiadomo
	public final String login;
	public final Date lastLogin;
	public final String passwordHash;

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
		this.passwordHash = passwordHash;
	}

	public User setId(int newId) {
		if(newId == id)
			return this;
		User ret = new User(newId, login, lastLogin, passwordHash.toString());
		return ret;
	}
	public User setLogin(String newLogin) {
		if(newLogin == null)
			throw new NullPointerException();
		if(newLogin.equals(login))
			return this;
		User ret = new User(id, newLogin, lastLogin, passwordHash);
		return ret;
	}
	public User setLastLogin(Date newLastLogin) {
		if(newLastLogin == null)
			throw new NullPointerException();
		if(newLastLogin == lastLogin)
			return this;
		User ret = new User(id, login, newLastLogin, passwordHash.toString());
		return ret;
	}
	public User setPassword(String newPassword) throws NoSuchAlgorithmException {
		DataMaster.logger.entering(this.getClass().getName(), "setPassword");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(newPassword.getBytes());
		byte[] newPasswordHash = md.digest();
		DataMaster.logger.finest("SHA256(...) = "+DataMaster.bytesToString(newPasswordHash));
		// always return new, no matter this is the same pass
		User ret = new User(id, login, lastLogin, DataMaster.bytesToString(newPasswordHash));
		DataMaster.logger.exiting(this.getClass().getName(), "setPassword");
		return ret;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("#User{");
		sb.append("id=");
		sb.append(id);
		sb.append(", ");
		sb.append("login=");
		sb.append(login);
		sb.append(", ");
		sb.append("lastLogin=");
		sb.append(lastLogin);
		sb.append("}");
		return sb.toString();
	}
}
