package wolfgang.data.mapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import wolfgang.data.DataMaster;

public final class User {
	public final int id;
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
	@Deprecated
	public User setLogin(String newLogin) {
		// UWAGA. Psuje hasło :P
		if(newLogin == null)
			throw new NullPointerException();
		if(newLogin.equals(login))
			return this;
		return new User(id, newLogin, lastLogin, passwordHash);
	}
	public User setLastLogin(Date newLastLogin) {
		if(newLastLogin == null)
			throw new NullPointerException();
		if(newLastLogin == lastLogin)
			return this;
		return new User(id, login, newLastLogin, passwordHash);
	}
	public User setPassword(String newPassword) throws NoSuchAlgorithmException {
		return new User(id, login, lastLogin, genPasswordHash(newPassword, login));
	}
	public static String genPasswordHash(String pass, String login) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(pass.getBytes());
		md.update("### SÓL SOLI SOLĄ NIE ZASOLI ###".getBytes()); // salting of hash
		md.update(login.getBytes());
		return DataMaster.bytesToString(md.digest());
	}

	@Override
	@Deprecated
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
