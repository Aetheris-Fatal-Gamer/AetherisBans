package br.com.aetherismc.bans.discord;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Message {

	private String _username;
	private String _iconUrl;
	private String _text;
	private ArrayList<Attachment> _attachments;

	public Message() {
		this((String)null, (String)null, (String)null);
	}

	public Message(String username) {
		this(username, (String)null, (String)null);
	}

	public Message(String username, String iconUrl) {
		this(username, iconUrl, (String)null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Message(String username, String iconUrl, String text) {
		this._username = username;
		this._iconUrl = iconUrl;
		this._text = text;
		this._attachments = new ArrayList();
	}

	public void setText(String text) {
		this._text = text;
	}

	public void setUsername(String username) {
		this._username = username;
	}

	public void setIconUrl(String iconUrl) {
		this._iconUrl = iconUrl;
	}

   public void pushAttachment(Attachment attachment) {
      this._attachments.add(attachment);
   }

   @SuppressWarnings(value = {"unchecked"})
   public JSONObject toJson() {
	   JSONObject result = new JSONObject();
	   result.put("username", this._username);
	   result.put("icon_url", this._iconUrl);
	   result.put("text", this._text);
	   if (!this._attachments.isEmpty()) {
		   JSONArray array = new JSONArray();

           for (Attachment attachment : this._attachments) {
               array.add(attachment.toJson());
           }
		   result.put("attachments", array);
	   }
	   return result;
	}
}
