package com.fusionx.lightirc.misc;

import java.io.Serializable;
import java.util.HashMap;

public class ServerObject implements Serializable {
	private static final long serialVersionUID = 1L;
	public String url = "";
	public String[] autoJoinChannels;
	public String userName = "";
	public String nick = "";
	public String serverPassword = "";
	public String title = "";
	
	public HashMap<String, String> toHashMap() {
		HashMap<String, String> nameIcons = new HashMap<String, String>();
		nameIcons.put("url", url);
		nameIcons.put("userName", userName);
		nameIcons.put("nick", nick);
		nameIcons.put("serverPassword", serverPassword);
		nameIcons.put("title", title);
		
		return nameIcons;
	}
}