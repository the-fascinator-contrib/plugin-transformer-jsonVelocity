/* 
 * Fascinator - Plugin - Tranformer - jsonVelocity
 * Copyright (C) 2013 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.googlecode.fascinator.transformer.jsonVelocity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.fascinator.common.JsonSimple;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.common.StorageDataUtil;
import com.googlecode.fascinator.portal.lookup.MintLookupHelper;

/**
 * Utility class for JsonVelocity Transformer
 * 
 * @author Linda Octalina
 * @author Andrew Brazzatti
 * @author Jianfeng Li
 * 
 */
public class Util extends StorageDataUtil{

    /** Logger */
    static Logger log = LoggerFactory.getLogger(JsonVelocityTransformer.class);
    
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
