package com.googlecode.fascinator.transformer.jsonVelocity;

import com.googlecode.fascinator.api.storage.DigitalObject;
import com.googlecode.fascinator.api.storage.StorageException;
import com.googlecode.fascinator.common.JsonSimple;
import com.googlecode.fascinator.common.StorageDataUtil;
import com.googlecode.fascinator.portal.lookup.MintLookupHelper;
import com.googlecode.fascinator.common.JsonSimpleConfig;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.simple.JSONArray;

import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JsonVelocity Transformer
 * 
 * @author Linda Octalina
 * 
 */
public class Util extends StorageDataUtil {

    /** Logger */
    static Logger log = LoggerFactory.getLogger(JsonVelocityTransformer.class);


    /**
     * Getlist method to get the values of key from the sourceMap
     *
     * @param json JsonSimple object to search
     * @param baseKey field to search
     * @return list of value based on baseKey
     */
    public Map<String, Object> getList(JsonSimple json, String baseKey) {
        SortedMap<String, Object> valueMap = new TreeMap<String, Object>();
        Map<String, Object> data;

        if (baseKey == null) {
            log.error("NULL baseKey provided!");
            return valueMap;
        }
        if (!baseKey.endsWith(".")) {
            baseKey = baseKey + ".";
        }

        if (json == null) {
            log.error("NULL JSON object provided!");
            return valueMap;
        }

        // Look through the top level nodes
        for (Object oKey : json.getJsonObject().keySet()) {
            // If the key matches
            String key = (String) oKey;
            if (key.startsWith(baseKey)) {
                // Find our data
                String value = json.getString(null, key);
                String field = baseKey;

                if (key.length() >= baseKey.length()) {
                    field = key.substring(baseKey.length(), key.length());
                }

                String index = field;
                if (field.indexOf(".") > 0) {
                    index = field.substring(0, field.indexOf("."));
                }

                if (valueMap.containsKey(index)) {
                    data = (Map<String, Object>) valueMap.get(index);
                } else {
                    data = new LinkedHashMap<String, Object>();
                    valueMap.put(index, data);
                }

                if (value.length() == 1) {
                    value = String.valueOf(value.charAt(0));
                }

                data.put(field.substring(
                        field.indexOf(".") + 1, field.length()), value);

            }
        }

        //log.info("{}: {}", baseKey, valueMap);
        return valueMap;
    }

    /**
     * Using Mint to look up labels of saved Mint item ids 
     * 
     * @param systemConfig: Json config file
     * @param urlName: Query url defined in config file
     * @param ids: String contains query ids
     * @return ArrayList<String>: Labels of query ids
     */
    public ArrayList<String> getMintLabels(JsonSimpleConfig systemConfig, String urlName, String ids) {
    	Map<String, String> mapIds = new HashMap<String, String>();
    	mapIds.put("id",ids);
    	try {
    		JsonSimple labelsMint = MintLookupHelper.get(systemConfig, urlName, mapIds);
    		ArrayList<String> labels = new ArrayList();
    		JSONArray arr = labelsMint.getJsonArray();
			for (int i = 0; i < arr.size(); i++) {
                JsonSimple labelJson = new JsonSimple(arr.get(i).toString());
                labels.add(labelJson.getString("", "label")); 
    		}
            return labels;
    	} catch (Exception ex) {
    		log.error("PDF transfer - When retrieving Mint labels: ", ex);
    		return null;
    	}
    }
}
