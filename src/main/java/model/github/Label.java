package model.github;

import com.google.gson.annotations.SerializedName;

public class Label {
	public String id;
	public String node_id;
	public String url;
	public String name;
	public String color;
	
	@SerializedName("default") // default is keyword
	public Boolean defaultState;
}
