package com.liferay.custom.client.extensions.portlet;

import com.liferay.custom.client.extensions.constants.LiferayCustomClientExtensionsWebPortletKeys;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author dell
 */
@Component(
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=false",
		"javax.portlet.display-name=LiferayCustomClientExtensions",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/generate-client-extension.jsp",
		"javax.portlet.name=" + LiferayCustomClientExtensionsWebPortletKeys.LIFERAY_CUSTOM_CLIENT_EXTENSIONS_KEY,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)public class LiferayCustomClientExtensionsWebPortlet extends MVCPortlet {
    private static final Log _log = LogFactoryUtil.getLog(LiferayCustomClientExtensionsWebPortlet.class);
 // Add this new reference at the top with other @Reference annotations
    @Reference
    private ObjectFolderLocalService objectFolderLocalService;

    public void generateExtension(ActionRequest request, ActionResponse response) 
    	    throws IOException, PortletException {

    	    try {
    	        String extensionName = ParamUtil.getString(request, "extensionName");
    	        String extensionType = ParamUtil.getString(request, "extensionType");
    	        String[] batchTypes = ParamUtil.getParameterValues(request, "batchType");

    	        // Validate inputs
    	        if (Validator.isBlank(extensionName)) {
    	            throw new PortletException("Extension name cannot be empty");
    	        }

    	        // Create temp directory
    	        File tempDir = Files.createTempDirectory(extensionName).toFile();

    	        try {
    	            // 1. Create client-extension.yaml
    	            File yamlFile = new File(tempDir, "client-extension.yaml");
    	            createClientExtensionYaml(yamlFile, extensionName, extensionType, request);

    	          
    	            if ("batch".equalsIgnoreCase(extensionType) && batchTypes != null && batchTypes.length > 0) {
    	                File batchDir = new File(tempDir, "batch");
    	                batchDir.mkdir();

    	                // Create separate JSON files for each batch type
    	                for (String batchType : batchTypes) {
    	                    String[] selectedItems = ParamUtil.getParameterValues(
    	                        request, "selectedItems_" + batchType);
    	                    
    	                    if (selectedItems != null && selectedItems.length > 0) {
    	                        String batchFileName = getBatchFileName(batchType);
    	                        File configFile = new File(batchDir, batchFileName);
    	                        createBatchConfigurationJson(configFile, batchType, selectedItems, request);
    	                    }
    	                }
    	            }

    	            // 3. Create ZIP file and download
    	            File zipFile = File.createTempFile(extensionName, ".zip");
    	            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
    	                zipDirectory(tempDir, zos, "");
    	            }

    	            downloadZipFile(response, zipFile, extensionName);

    	        } finally {
    	            FileUtil.delete(tempDir);
    	        }
    	    } catch (Exception e) {
    	        SessionErrors.add(request, e.getClass().getName(), e);
    	        throw new PortletException(e);
    	    }
    	}

    	// Update createClientExtensionYaml to handle multiple batch types
    private void createClientExtensionYaml(File yamlFile, String extensionName, 
    	    String extensionType, ActionRequest request) 
    	    throws IOException, PortalException {
    	    
    	    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
    	    String portalURL = themeDisplay.getPortalURL();
    	    String serviceAddress = portalURL.replace("https://", "").replace("http://", "");
    	    String serviceScheme = portalURL.startsWith("https://") ? "https" : "http";

    	    try (BufferedWriter writer = Files.newBufferedWriter(yamlFile.toPath())) {
    	        writer.write("assemble:\n");
    	        writer.write("  - from: batch\n");
    	        writer.write("    into: batch\n\n");
    	        
    	        writer.write(extensionName + ":\n");
    	        writer.write("  name: " + extensionName + "\n");
    	        
    	        
    	        
    	        writer.write("\n" + extensionName + "-oauth-application-headless-server:\n");
    	        writer.write("  .serviceAddress: " + serviceAddress + "\n");
    	        writer.write("  .serviceScheme: " + serviceScheme + "\n");
    	        writer.write("  name: Liferay OAuth Application Headless Server\n");
    	        writer.write("  scopes:\n");
    	        writer.write("    - Liferay.Headless.Admin.Workflow.everything\n");
    	        writer.write("    - Liferay.Headless.Batch.Engine.everything\n");
    	        writer.write("    - Liferay.Object.Admin.REST.everything\n");
    	        writer.write("  type: oAuthApplicationHeadlessServer\n");
    	    }
    	}
    private String getBatchFileName(String batchType) {
        switch(batchType) {
            case "picklist":
                return "00-list-type-definition.batch-engine-data.json";
            case "object":
                return "01-object-definition.batch-engine-data.json";
            case "objectfolder":
                return "00-object-folder.batch-engine-data.json";
            default:
                return "00-config.batch-engine-data.json";
        }
    }

    // Update createBatchConfigurationJson to handle different batch types
    private void createBatchConfigurationJson(File configFile, String batchType, 
    	    String[] selectedItems, ActionRequest request) throws IOException, PortalException {

    	    try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath())) {
    	        JSONObject config = JSONFactoryUtil.createJSONObject();

    	        // Configuration section
    	        JSONObject configuration = JSONFactoryUtil.createJSONObject();
    	        
    	        switch(batchType) {
    	            case "picklist":
    	                configuration.put("className", 
    	                    "com.liferay.headless.admin.list.type.dto.v1_0.ListTypeDefinition");
    	                break;
    	            case "object":
    	                configuration.put("className", 
    	                    "com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition");
    	                break;
    	            case "objectfolder":
    	                configuration.put("className", 
    	                    "com.liferay.object.admin.rest.dto.v1_0.ObjectFolder");
    	                break;
    	        }

    	        JSONObject parameters = JSONFactoryUtil.createJSONObject();
    	        parameters.put("containsHeaders", "false");
    	        parameters.put("createStrategy", "UPSERT");
    	        parameters.put("importStrategy", "ON_ERROR_FAIL");
    	        parameters.put("updateStrategy", "UPDATE");

    	        configuration.put("parameters", parameters);
    	        configuration.put("taskItemDelegateName", "DEFAULT");
    	        config.put("configuration", configuration);

    	        // Items array with complete data
    	        JSONArray items = JSONFactoryUtil.createJSONArray();

    	        if (selectedItems != null && selectedItems.length > 0) {
    	            for (String itemERC : selectedItems) {
    	                JSONObject itemDefinition = getItemDefinition(batchType, itemERC, request);
    	                if (itemDefinition != null) {
    	                    items.put(itemDefinition);
    	                }
    	            }
    	        }

    	        config.put("items", items);
    	        writer.write(config.toString());
    	    }
    	}

    // New method to handle different item types
    private JSONObject getItemDefinition(String batchType, String itemERC, ActionRequest request) 
        throws IOException, PortalException {

        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        String portalURL = themeDisplay.getPortalURL();
        String emailAddress = themeDisplay.getUser().getEmailAddress();
        String password = "Test"; // Handle password securely in production
        
        String authString = emailAddress + ":" + password;
        String authEncoded = Base64.encode(authString.getBytes());

        String apiUrl;
        
        switch(batchType) {
            case "picklist":
                apiUrl = portalURL + "/o/headless-admin-list-type/v1.0/list-type-definitions/by-external-reference-code/" + itemERC;
                break;
            case "object":
                apiUrl = portalURL + "/o/object-admin/v1.0/object-definitions/by-external-reference-code/" + itemERC;
                break;
            case "objectfolder":
                apiUrl = portalURL + "/o/object-admin/v1.0/object-folders/by-external-reference-code/" + itemERC;
                break;
            default:
                return null;
        }

        Http.Options options = new Http.Options();
        options.setLocation(apiUrl);
        options.addHeader("Accept", "application/json");
        options.addHeader("Authorization", "Basic " + authEncoded);

        try {
            String jsonResponse = HttpUtil.URLtoString(options);
            JSONObject jsonObject = JSONFactoryUtil.createJSONObject(jsonResponse);

            // Create new JSON object excluding actions
            JSONObject result = JSONFactoryUtil.createJSONObject();

            // Copy all fields except "actions"
            for (String key : jsonObject.keySet()) {
                if (!"actions".equals(key)) {
                    result.put(key, jsonObject.get(key));
                }
            }

            return result;

        } catch (Exception e) {
            _log.error("Error fetching item definition: " + e.getMessage(), e);
            return null;
        }
    }

	/*
	 * // Update createClientExtensionYaml to handle different batch types private
	 * void createClientExtensionYaml(File yamlFile, String extensionName, String
	 * extensionType, String batchType, String[] selectedItems, ActionRequest
	 * request) throws IOException, PortalException {
	 * 
	 * try (BufferedWriter writer = Files.newBufferedWriter(yamlFile.toPath())) {
	 * writer.write("assemble:\n"); writer.write("  - from: batch\n");
	 * writer.write("    into: batch\n\n");
	 * 
	 * writer.write(extensionName + ":\n"); writer.write("  name: " + extensionName
	 * + "\n");
	 * 
	 * if ("batch".equalsIgnoreCase(extensionType)) {
	 * writer.write("  oAuthApplicationHeadlessServer: " + extensionName +
	 * "-oauth-application\n"); writer.write("  type: batch\n");
	 * writer.write("  resourcePath: /batch/" + extensionName.toLowerCase() + "\n");
	 * 
	 * if (selectedItems != null && selectedItems.length > 0) {
	 * writer.write("  parameters:\n"); for (String itemERC : selectedItems) {
	 * JSONObject item = getItemDefinition(batchType, itemERC, request); if (item !=
	 * null) { writer.write("    - name: " +
	 * item.getString("name").toLowerCase().replace(" ", "-") + "\n");
	 * writer.write("      type: string\n"); } } } } else {
	 * writer.write("  type: microservice\n"); }
	 * 
	 * writer.write("\n" + extensionName + "-oauth-application:\n");
	 * writer.write("  name: " + extensionName + " OAuth Application\n");
	 * writer.write("  scopes:\n");
	 * writer.write("    - Liferay.Headless.Batch.Engine.everything\n");
	 * writer.write("    - Liferay.Object.Admin.REST.everything\n");
	 * writer.write("  type: oAuthApplicationHeadlessServer\n"); } }
	 */
    private void zipDirectory(File dir, ZipOutputStream zos, String path) throws IOException {
	    for (File file : dir.listFiles()) {
	        String entryPath = path + file.getName();
	        
	        if (file.isDirectory()) {
	            zipDirectory(file, zos, entryPath + "/");
	            continue;
	        }
	        
	        zos.putNextEntry(new ZipEntry(entryPath));
	        Files.copy(file.toPath(), zos);
	        zos.closeEntry();
	    }
	}

	private void downloadZipFile(ActionResponse response, File zipFile, String extensionName) 
	    throws IOException {
	    
	    // Set response headers for file download
	    HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
	    httpResponse.setContentType("application/zip");
	    httpResponse.setHeader("Content-Disposition", 
	        "attachment; filename=\"" + extensionName + ".zip\"");
	    httpResponse.setContentLength((int) zipFile.length());
	    httpResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
	    
	    // Stream file to response
	    try (InputStream is = new FileInputStream(zipFile);
	         OutputStream os = httpResponse.getOutputStream()) {
	        byte[] buffer = new byte[4096];
	        int bytesRead;
	        while ((bytesRead = is.read(buffer)) != -1) {
	            os.write(buffer, 0, bytesRead);
	        }
	        os.flush();
	    } finally {
	        // Delete the temp zip file after streaming
	        FileUtil.delete(zipFile);
	    }
	}
}