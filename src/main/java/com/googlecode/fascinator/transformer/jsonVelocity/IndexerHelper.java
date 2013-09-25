/**
 * 
 */
package com.googlecode.fascinator.transformer.jsonVelocity;

import java.io.IOException;

import com.googlecode.fascinator.api.PluginException;
import com.googlecode.fascinator.api.PluginManager;
import com.googlecode.fascinator.api.indexer.Indexer;
import com.googlecode.fascinator.api.storage.Storage;
import com.googlecode.fascinator.common.FascinatorHome;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.indexer.SolrIndexer;
import com.googlecode.fascinator.portal.services.ByteRangeRequestCache;
import com.googlecode.fascinator.portal.services.DatabaseServices;
import com.googlecode.fascinator.portal.services.DynamicPageService;
import com.googlecode.fascinator.portal.services.FascinatorService;
import com.googlecode.fascinator.portal.services.HarvestManager;
import com.googlecode.fascinator.portal.services.HouseKeepingManager;
import com.googlecode.fascinator.portal.services.PortalManager;
import com.googlecode.fascinator.portal.services.ScriptingServices;
import com.googlecode.fascinator.portal.services.VelocityService;

/**
 * 
 * This is a helper class for 1.6, PLEASE migrate away from this class when moving to 1.7
 * 
 * @author Shilo Banihit
 *
 */
public class IndexerHelper implements ScriptingServices {

	@Override
	public ByteRangeRequestCache getByteRangeCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatabaseServices getDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HarvestManager getHarvestManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HouseKeepingManager getHouseKeepingManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Indexer getIndexer() {
		Indexer indexer = new SolrIndexer();
		try {
			indexer.init(JsonSimpleConfig.getSystemFile());
		} catch (Exception e) {			
		}
		return indexer;
	}

	@Override
	public DynamicPageService getPageService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortalManager getPortalManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FascinatorService getService(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Storage getStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VelocityService getVelocityService() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
