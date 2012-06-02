package wolfgang.data;

import java.util.Date;

public final class User extends _TableEntry {
	public final int id;
	public final String login;
	public final Date lastLogin;
	
	
	public User(int id, String login, Date lastLogin) {
		super();
		this.id = id;
		this.login = login;
		this.lastLogin = lastLogin;
	}
}
