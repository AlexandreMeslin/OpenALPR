package br.com.meslin.lac;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.stream.JsonReader;

/**
 * Created by luis on 1/07/15.
 * POJO that represents a query message
 * to the MEPA service
 */
@SuppressWarnings("serial")
public class MEPAQuery extends QueryMessage implements Serializable {
    /** DEBUG */
    public static final String TAG = "MEPAQuery";

    private ACTION type;
    private ITEM object;
    private ROUTE target;
    private String label;
    private String rule;
    private String actuation;
    private Map<String, Object> event;

    /** Setters **/
    public void setType(ACTION type) {
        this.type = type;
    }

    public void setObject(ITEM object) {
        this.object = object;
    }

    public void setTarget(ROUTE target) {
        this.target = target;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setActuation(String actuation) {
        this.actuation = actuation;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }


    /** Getters */
    public ACTION getType() {
        return this.type;
    }

    public ITEM getObject() {
        return this.object;
    }

    public ROUTE getTarget() {
        return this.target;
    }

    public String getLabel() {
        return this.label;
    }

    public String getRule() {
        return this.rule;
    }

    public String getActuation() {
        return this.actuation;
    }

    public Map<String, Object> getEvent() {
        return this.event;
    }


    public void fromJSON(JsonReader reader) throws IOException, IllegalArgumentException {
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            switch(name) {
                case TYPE:
                    type = ACTION.fromString(reader.nextString());
                    break;

                case OBJECT:
                    object = ITEM.fromString(reader.nextString());
                    break;

                case TARGET:
                    target = ROUTE.fromString(reader.nextString());
                    break;

                case LABEL:
                    label = reader.nextString();
                    break;

                case RULE:
                    rule = reader.nextString();
                    break;

                case EVENT:
                    event = new HashMap<>();

                    reader.beginArray();
                    while(reader.hasNext()) {
                        reader.beginArray();
                        String var  = reader.nextString();
                        String type = reader.nextString();
                        event.put(var, type);
                        reader.endArray();
                    }
                    reader.endArray();
                    break;

                case ACTUATION:
                    actuation = reader.nextString();
                    break;

                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    /**
     * Converts a MEPAQuery to a json string to be sent in a message
     * @return a json string representation of the query
     */
	public String toJsonString(){
        //Build a json string using the properties of the query
		// A documentação do LAC no DokuWiki está errada. Está faltando informar que precisa dos [] no início e no fim da string JSON
        String json = "[{\"MEPAQuery\": {" +
                "\"type\":\"" + this.getType() + "\"";
        if (this.getObject() != null)
            json += ",\"object\":\"" + this.getObject() + "\"";
        if (this.getLabel() != null)
            json += ",\"label\":\"" + this.getLabel() + "\"";
        if (this.getRule() != null)
            json += ",\"rule\":\"" + this.getRule() + "\"";
        if (this.getTarget() != null)
            json += ",\"target\":\"" + this.getTarget() + "\"";
        json += "}}]";
        return json;
    }

    private String notNullString(Object obj){
        return (obj == null) ? "null" : obj.toString();
    }

    @Override
    public String toString() {
        return TAG + " [type=" + type + ", label=" + notNullString(label)
                + ", rule=" + notNullString(rule) + "]";
    }
}