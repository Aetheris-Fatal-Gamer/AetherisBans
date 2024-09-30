package br.com.aetherismc.bans.discord;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class Attachment {
	   
	private String _authorName;
	private String _authorIcon;
	private String _color;
	private ArrayList<Field> _fields;

	public Attachment() {
		this((String)null, (String)null, (String)null);
	}

	public Attachment(String authorName) {
		this(authorName, (String)null, (String)null);
	}

	public Attachment(String authorName, String authorIcon) {
		this(authorName, authorIcon, (String)null);
	}

	@SuppressWarnings("rawtypes")
	public Attachment(String authorName, String authorIcon, String color) {
		this._authorName = authorName;
		this._authorIcon = authorIcon;
		this._color = color;
		this._fields = new ArrayList();
	}

	public void setAuthorName(String authorName) {
		this._authorName = authorName;
	}	
	
	public void setAuthorIcon(String authorIcon) {
		this._authorIcon = authorIcon;
	}

	public void setColor(String color) {
		this._color = color;
	}

	public void pushField(Field field) {
		this._fields.add(field);
	}

	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		result.put("author_icon", this._authorIcon);
		result.put("author_name", this._authorName);
		result.put("color", this._color);
		if (!this._fields.isEmpty()) {
			JSONArray array = new JSONArray();

            for (Field field : this._fields) {
                array.add(field.toJson());
            }
			result.put("fields", array);
		}
		return result;
	}
}