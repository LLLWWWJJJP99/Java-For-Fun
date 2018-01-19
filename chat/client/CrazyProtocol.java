package chat.client;

public interface CrazyProtocol {
	
	int PROTOCOL_LEN = 2;
	
	// public msg
	String MSG_ROUND = "¡ì¦Ã";
	
	//login protocol
	String USER_ROUND = "¡Ç¡Æ";
	
	String LOGIN_SUCCESS = "1";
	// protocol used to respond for invalid username
	String NAME_REP = "-1";
	
	//private msg protocol
	String PRIVATE_ROUND = "¡ï¡¾";
	
	//split sign used to seperate user and private message
	String SPLIT_SIGN = "¡ù";
}
