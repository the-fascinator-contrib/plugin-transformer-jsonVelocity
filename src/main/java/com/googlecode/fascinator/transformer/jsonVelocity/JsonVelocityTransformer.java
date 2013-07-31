/*
 * The Fascinator - Plugin - Transformer - Json Velocity Transformer
 * Copyright (C) 2010-2013 University of Southern Queensland
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.fascinator.api.PluginDescription;
import com.googlecode.fascinator.api.PluginException;
import com.googlecode.fascinator.api.storage.DigitalObject;
import com.googlecode.fascinator.api.storage.Payload;
import com.googlecode.fascinator.api.storage.StorageException;
import com.googlecode.fascinator.api.transformer.Transformer;
import com.googlecode.fascinator.api.transformer.TransformerException;
import com.googlecode.fascinator.common.JsonSimple;
import com.googlecode.fascinator.common.JsonSimpleConfig;
import com.googlecode.fascinator.common.storage.StorageUtils;
import com.googlecode.fascinator.portal.services.PortalManager;
import com.googlecode.fascinator.portal.services.ScriptingServices;
import com.googlecode.fascinator.spring.ApplicationContextProvider;

/**
 * <p>
 * This plugin transform a Json Payload to other formats based on the provided
 * Velocity templates. The transformed formats will then be stored as Payloads.
 * </p>
 * 
 * <h3>Configuration</h3>
 * 
 * <p>
 * Keep in mind that each data source can provide overriding configuration. This
 * transformer currently allows overrides on all fields (except 'id').
 * </p>
 * 
 * <table border="1">
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * <th>Required</th>
 * <th>Default</th>
 * </tr>
 * 
 * <tr>
 * <td>id</td>
 * <td>Id of the transformer</td>
 * <td><b>Yes</b></td>
 * <td>jsonVelocity</td>
 * </tr>
 * 
 * <tr>
 * <td>sourcePayload</td>
 * <td>Source payload from which the object will be transformed. Currently only
 * JSON payloads are supported.</td>
 * <td><b>No</b></td>
 * <td>object.tfpackage</td>
 * </tr>
 * 
 * <tr>
 * <td>templatesPath</td>
 * <td>Velocity template file or directory.</td>
 * <td><b>Yes</b></td>
 * <td>N/A - Must be provided</td>
 * </tr>
 * 
 * <tr>
 * <td>portalId</td>
 * <td>The portal to use when generating external URLs inside the templates. The
 * server's configured URL base will be used as well.</td>
 * <td><b>No</b></td>
 * <td>default</td>
 * </tr>
 * 
 * <tr>
 * <td>checkForTFMETAProperty</td>
 * <td>Whether or not to check for a property in TF-META-OBJ to decide whether
 * to transform the payload or not.</td>
 * <td><b>No</b></td>
 * <td>false</td>
 * </tr>
 * 
 * <tr>
 * <td>TFMETAPropertyName</td>
 * <td>If checkForTFMETAProperty is set to true. What is the property name to
 * chec.k</td>
 * <td><b>No (Yes if checkForTFMETAProperty is true)</b></td>
 * <td>N/A</td>
 * </tr>
 * 
 * <tr>
 * <td>TFMETAPropertyValue</td>
 * <td>If checkForTFMETAProperty is set to true. What is the property value to
 * check.</td>
 * <td><b>No (Yes if checkForTFMETAProperty is true)</b></td>
 * <td>N/A</td>
 * </tr>
 * 
 * <tr>
 * <td>clearPropertyOnTransform</td>
 * <td>If checkForTFMETAProperty is set to true. Should the property be cleared
 * while we are transforming allowing for one time transforms</td>
 * <td><b>No (Yes if checkForTFMETAProperty is true)</b></td>
 * <td>false</td>
 * </tr>
 * 
 * 
 * <tr>
 * <td>transformSource</td>
 * <td>Rather than outputting to a new file, are we transforming the original
 * source file?</td>
 * <td><b>No</b></td>
 * <td>false</td>
 * </tr>
 * 
 * <tr>
 * <td>outputExtension</td>
 * <td>The file extension for the outputted file</td>
 * <td><b>No</b></td>
 * <td>xml</td>
 * </tr>
 * 
 * <h3>Examples</h3>
 * <ol>
 * <li>
 * Adding JsonVelocity Transformer to The Fascinator
 * 
 * <pre>
 * "jsonVelocity": {
 *     "id" : "jsonVelocity",
 *     "sourcePayload" : "object.tfpackage",
 *     "templatesPath" : "src/main/resources/templates"
 * }
 * </pre>
 * 
 * </li>
 * </ol>
 * 
 * <h3>Wiki Link</h3>
 * <p>
 * <a href=
 * "https://fascinator.usq.edu.au/trac/wiki/Fascinator/Documents/Plugins/Transformer/JsonVelocity"
 * > https://fascinator.usq.edu.au/trac/wiki/Fascinator/Documents/Plugins/
 * Transformer/JsonVelocity </a>
 * </p>
 * 
 * @author Linda Octalina
 * @author Andrew Brazzatti
 * @author Jianfeng Li
 */

public class JsonVelocityTransformer implements Transformer {
	/** Default portal */
	protected static String DEFAULT_PORTAL = "default";

	/** Default payload */
	protected static String DEFAULT_PAYLOAD = "object.tfpackage";

	/** Logger */
	private static Logger log = LoggerFactory
			.getLogger(JsonVelocityTransformer.class);

	/** Json config file **/
	protected JsonSimpleConfig systemConfig;

	/** Default Template file or folder **/
	private File systemTemplates;

	/** Source payload to be transformed **/
	private String systemPayload;

	/** Portal ID */
	private String systemPortal;

	/** URL Base */
	private String urlBase;

	/** Utility class for json velocity transformer */
	public Util util;

	/** VelocityEngine **/
	public VelocityEngine velocity;

	/** Used to store last execution */
	private File oldTemplates;

	/** Json config file **/
	protected JsonSimple itemConfig;

	/** Template file or folder **/
	private File itemTemplates;

	private ScriptingServices scriptingServices;

	/**
	 * Overridden method init to initialize
	 * 
	 * @param jsonString
	 *            of configuration for transformer
	 * @throws PluginException
	 *             if fail to parse the config
	 */
	@Override
	public void init(String jsonString) throws PluginException {
		try {
			systemConfig = new JsonSimpleConfig(jsonString);
			reset();
		} catch (IOException e) {
			throw new PluginException(e);
		}
	}

	/**
	 * Overridden method init to initialize
	 * 
	 * @param jsonString
	 *            of configuration for transformer
	 * @throws PluginException
	 *             if fail to parse the config
	 */
	@Override
	public void init(File jsonFile) throws PluginException {
		try {
			systemConfig = new JsonSimpleConfig(jsonFile);
			reset();
		} catch (IOException e) {
			throw new PluginException(e);
		}
	}

	/**
	 * Initialise the plugin, also used during subsequent executions
	 * 
	 * @throws TransformerException
	 *             if errors occur
	 */
	private void reset() throws TransformerException {
		// Load in our scripting services from Spring. Note that it's not a
		// fully initialised Scripting Services yet. Only services accessible
		// from getService are initialised.
		if (scriptingServices == null) {
			if (ApplicationContextProvider.getApplicationContext() != null) {
				scriptingServices = ApplicationContextProvider
						.getApplicationContext().getBean("scriptingServices",
								ScriptingServices.class);
			}
		}
		// Utility Library... and test if this is the first execution
		if (util == null) {
			util = new Util();

			// Find where our templates are
			String templatePath = getTemplatePath();
			if (templatePath != null) {
				systemTemplates = new File(templatePath);
				if (systemTemplates == null || !systemTemplates.exists()) {
					throw new TransformerException("Error finding "
							+ "template path: '" + templatePath + "'");
				}
			}

			// What is our source payload
			systemPayload = getSystemPayload();

			// What portal should be used in URLs
			systemPortal = getSystemPortal();

			// URL Base
			urlBase = getUrlBase();
			if (urlBase == null) {
				throw new TransformerException("No URL base in system config");
			}
		}

		// Purge old transformations
		itemConfig = null;
		itemTemplates = null;
	}

	protected String getUrlBase() {
		return systemConfig.getString(null, "urlBase");
	}

	protected String getSystemPortal() {
		return systemConfig.getString(DEFAULT_PORTAL, "transformerDefaults",
				"jsonVelocity", "portalId");
	}

	protected String getSystemPayload() {
		return systemConfig.getString(DEFAULT_PAYLOAD, "transformerDefaults",
				"jsonVelocity", "sourcePayload");
	}

	protected String getTemplatePath() {
		return systemConfig.getString(null, "transformerDefaults",
				"jsonVelocity", "templatesPath");
	}

	/**
	 * Initialise the Velocity engine, paying attention to whether or not the
	 * template path has changed.
	 * 
	 * @throws TransformerException
	 *             if errors occur
	 */
	private void initVelocityEngine() throws TransformerException {
		// Reset out velocity engine... if the path has changed
		if (velocity == null
				|| oldTemplates == null
				|| !oldTemplates.getAbsolutePath().equals(
						itemTemplates.getAbsolutePath())) {

			log.info("Velocity engine re-initialising. Templates: '{}'",
					itemTemplates.getAbsolutePath());

			try {
				// Store for later
				oldTemplates = itemTemplates;

				velocity = new VelocityEngine();
				velocity.setProperty(Velocity.RESOURCE_LOADER,
						"file, class, url");
				velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE,
						"false");
				velocity.setProperty("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
				velocity.setProperty("url.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.URLResourceLoader");

				velocity.setProperty("directive.set.null.allowed", "true");
				velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
						"com.googlecode.fascinator.transformer.jsonVelocity.LoggingWrapper");

				velocity.setProperty("url.resource.loader.root", "");
				File templateDir = itemTemplates;
				if (itemTemplates.isFile()) {
					templateDir = itemTemplates.getParentFile();
				}
				velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,
						templateDir.getAbsolutePath());
				String portalPath = systemConfig.getString(
						PortalManager.DEFAULT_PORTAL_HOME, "portal", "home");
				File portalLibraryFile = new File(portalPath
						+ "/portal-library.vm");
				if (portalLibraryFile.exists()) {
					velocity.setProperty("velocimacro.library",
							portalLibraryFile.toURI().toURL().toExternalForm()
									.toString());
				}
				velocity.init();
			} catch (Exception ex) {
				velocity = null;
				throw new TransformerException("Error initialising Velocity: ",
						ex);
			}
		}
	}

	/**
	 * Gets plugin Id
	 * 
	 * @return pluginId
	 */
	@Override
	public String getId() {
		return "jsonVelocity";
	}

	/**
	 * Gets plugin name
	 * 
	 * @return pluginName
	 */
	@Override
	public String getName() {
		return "Json Velocity Transformer";
	}

	/**
	 * Gets a PluginDescription object relating to this plugin.
	 * 
	 * @return a PluginDescription
	 */
	@Override
	public PluginDescription getPluginDetails() {
		return new PluginDescription(this);
	}

	/**
	 * Overridden shutdown method
	 * 
	 * @throws PluginException
	 */
	@Override
	public void shutdown() throws PluginException {
		// clean up any resources if required
	}

	/**
	 * Overridden transform method
	 * 
	 * @param in
	 *            : The DigitalObject to be processed/transformer
	 * @param jsonConfig
	 *            : The configuration for this item's harvester
	 * @return processed: The DigitalObject after being transformed
	 * 
	 * @throws TransformerException
	 *             if fail to transform
	 */
	@Override
	public DigitalObject transform(DigitalObject in, String jsonConfig)
			throws TransformerException {
		// Purge previous transformations
		reset();
		try {
			itemConfig = new JsonSimple(jsonConfig);
		} catch (IOException ex) {
			throw new TransformerException("Invalid configuration! '{}'", ex);
		}

		if (okToProcess(in, itemConfig)) {
			// Source payload
			String source = itemConfig
					.getString(systemPayload, "sourcePayload");
			Payload sourcePayload = null;
			try {
				// Sometimes config will be just an extension eg. ".tfpackage"
				for (String payloadId : in.getPayloadIdList()) {
					if (payloadId.endsWith(source)) {
						source = payloadId;
					}
				}
				log.info("Transforming PID '{}' from OID '{}'", source,
						in.getId());
				sourcePayload = in.getPayload(source);
			} catch (StorageException ex) {
				log.error("Error accessing payload in storage: '{}'", ex);
			}

			// Now read the data out of storage
			JsonSimple json = null;
			try {
				json = new JsonSimple(sourcePayload.open());
				sourcePayload.close();
			} catch (Exception ex) {
				throw new TransformerException(
						"Error accessing JSON payload: ", ex);
			}

			// PortalID
			String portalId = itemConfig.getString(systemPortal, "portalId");

			// Find all the templates we are running
			List<File> templates = getListOfTemplates();
			if (templates.isEmpty()) {
				log.info("No templates to execute");
				return in;
			}

			// Initialise our velocity engine
			initVelocityEngine();

			// Bind all the data we want in the template
			VelocityContext vc = new VelocityContext();
			vc.put("systemConfig", systemConfig);
			vc.put("item", json);
			vc.put("util", util);
			// Adding jsonUtil binding here so that we can have a common binding
			// to the one used in CachingDynamicPageService
			vc.put("jsonUtil", util);
	        vc.put("StringUtils", StringUtils.class);
	        vc.put("StringEscapeUtils", StringEscapeUtils.class);
			vc.put("Services", scriptingServices);
			vc.put("oid", in.getId());
			vc.put("object", in);
			vc.put("urlBase", urlBase + portalId);

			// Render each template
			for (File file : templates) {
				String output = null;
				// Find and render the template
				try {
					Template template = velocity.getTemplate(file.getName());
					log.info("Rendering template: '{}'", file.getName());
					output = renderTemplate(template, vc);
				} catch (Exception ex) {
					log.error("Error rendering template: '" + file.getName()
							+ "': ", ex);
				}

				if (output == null) {
					log.error("Unknown error rendering template: '{}'",
							file.getName());
				} else {
					// Store the output
					try {
						String payloadName = payloadName(file.getName());
						if (itemConfig.getBoolean(false, "transformSource")) {
							payloadName = sourcePayload.getId();
						}
						storeData(in, payloadName, output);
					} catch (Exception ex) {
						log.error(
								"Error storing rendered output: '"
										+ file.getName() + "'", ex);
					}
				}
			}
		}
		return in;
	}

	private boolean okToProcess(DigitalObject in, JsonSimple itemConfig)
			throws TransformerException {
		if (itemConfig.getBoolean(false, "checkForTFMETAProperty")) {
			String propertyName = itemConfig.getString(null,
					"TFMETAPropertyName");
			String propertyValue = itemConfig.getString(null,
					"TFMETAPropertyValue");
			if (propertyName != null && propertyValue != null) {
				Properties tfMetadata;
				try {
					tfMetadata = in.getMetadata();
				} catch (StorageException e) {
					throw new TransformerException(e);
				}
				if (!propertyValue.equals(tfMetadata.getProperty(propertyName))) {
					return false;
				}
				if (itemConfig.getBoolean(false, "clearPropertyOnTransform")) {
					ByteArrayInputStream input;
					try {
						tfMetadata.remove(propertyName);
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						tfMetadata.store(output, null);
						input = new ByteArrayInputStream(output.toByteArray());
						StorageUtils.createOrUpdatePayload(in, "TF-OBJ-META",
								input);
					} catch (Exception e) {
						throw new TransformerException(e);
					}

				}
			}

		}
		return true;
	}

	/**
	 * Render a velocity template
	 * 
	 * @param template
	 *            : The template to render
	 * @param context
	 *            : The Velocity context with data
	 * @return String: The rendered output
	 * @throws TransformerException
	 *             if the render fails
	 */
	private String renderTemplate(Template template, VelocityContext context)
			throws TransformerException {
		StringWriter writer = new StringWriter();
		try {
			template.merge(context, writer);
		} catch (IOException ex) {
			throw new TransformerException("Error rendering template: ", ex);
		}
		return writer.toString();
	}

	/**
	 * Store the provided data
	 * 
	 * @param object
	 *            : The object to store the data in
	 * @param pid
	 *            : The payload ID to use in the object
	 * @param data
	 *            : The data to store
	 * @return Payload: The payload object successfully stored
	 * @throws TransformerException
	 *             if storage fails
	 */
	protected Payload storeData(DigitalObject object, String pid, String data)
			throws TransformerException {
		try {
			try {
				return object.createStoredPayload(pid, stream(data));
			} catch (StorageException ex) {
				// Already exists, try an update
				return object.updatePayload(pid, stream(data));
			}
		} catch (UnsupportedEncodingException ex) {
			throw new TransformerException("Error in data encoding: ", ex);
		} catch (StorageException ex) {
			throw new TransformerException("Error storing payload: ", ex);
		}
	}

	/**
	 * Convert the provided String into an InputStream, assuming UTF8 character
	 * encoding.
	 * 
	 * @param string
	 *            : The String to convert
	 * @return InputStream: The InputStream holding the String's data
	 * @throws UnsupportedEncodingException
	 *             if the String does not contain UTF8
	 */
	private InputStream stream(String string)
			throws UnsupportedEncodingException {
		return new ByteArrayInputStream(string.getBytes("UTF8"));
	}

	/**
	 * Find the list of template Files to execute this pass, looking at the item
	 * configuration and the default values.
	 * 
	 * @return List<File>: The list of Files, possibly empty
	 */
	public List<File> getListOfTemplates() {
		List<File> templateList = new ArrayList<File>();

		// Find where our templates are
		String templatePath = itemConfig.getString(null, "templatesPath");
		if (templatePath != null) {
			itemTemplates = new File(templatePath);
			if (itemTemplates == null || !itemTemplates.exists()) {
				log.error("Error finding template path: '{}'", templatePath);
				itemTemplates = systemTemplates;
			}
		} else {
			itemTemplates = systemTemplates;
		}

		// Error case
		if (itemTemplates == null) {
			log.error("No configured or default templates!");
			return templateList;
		}

		// Process directory or individual and return
		if (itemTemplates.isDirectory()) {
			// Make sure we only run velocity tempaltes
			for (File template : itemTemplates.listFiles()) {
				if (template.getName().endsWith(".vm")) {
					templateList.add(template);
				}
			}
		} else {
			if (itemTemplates.isFile()) {
				templateList.add(itemTemplates);
			}
		}
		return templateList;
	}

	/**
	 * Given the name of the provided template, change the extension for use as
	 * a payload ID.
	 * 
	 * @param templateName
	 *            : The name of the template file
	 * @return String: The payload ID to use
	 */
	protected String payloadName(String templateName) {
		String extension = itemConfig.getString("xml", "outputExtension");
		return templateName.substring(0, templateName.lastIndexOf(".")) + "."
				+ extension;
	}

	/**
	 * Like payloadName(String), it returns a name with timestamp
	 */
	protected String getTimestampedPayload(String payloadName) {
		int extensionIndex = payloadName.lastIndexOf(".");
		if (extensionIndex != -1) {
			String extension = payloadName.substring(extensionIndex,
					payloadName.length());
			return payloadName.substring(0, extensionIndex) + "-"
					+ getTimeStamp() + extension;
		}
		return payloadName + "-" + getTimeStamp();
	}

	protected String getTimeStamp() {
		SimpleDateFormat convertor = new SimpleDateFormat("yyyyMMddHHmmss");
		return convertor.format(new Date());
	}
}
