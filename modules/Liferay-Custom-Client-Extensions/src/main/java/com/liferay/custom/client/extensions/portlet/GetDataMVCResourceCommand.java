package com.liferay.custom.client.extensions.portlet;

import com.liferay.custom.client.extensions.constants.LiferayCustomClientExtensionsWebPortletKeys;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    property = {
        "javax.portlet.name=" + LiferayCustomClientExtensionsWebPortletKeys.LIFERAY_CUSTOM_CLIENT_EXTENSIONS_KEY,
        "mvc.command.name=/getdata"
    }, 
    service = MVCResourceCommand.class
)
public class GetDataMVCResourceCommand implements MVCResourceCommand {

    @Reference
    private ObjectDefinitionLocalService objectDefinitionLocalService;
    
    @Reference
    private ListTypeDefinitionLocalService listTypeDefinitionLocalService;
    
    @Reference
    private ObjectFolderLocalService objectFolderLocalService;

    @Override
    public boolean serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws PortletException {
        try {
            String batchType = ParamUtil.getString(resourceRequest, "batchType");
            JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
            Locale locale = resourceRequest.getLocale();

            switch(batchType) {
                case "picklist":
                    List<ListTypeDefinition> listTypeDefinitions = 
                        listTypeDefinitionLocalService.getListTypeDefinitions(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
                    
                    for (ListTypeDefinition ltd : listTypeDefinitions) {
                        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
                        jsonObject.put("id", ltd.getListTypeDefinitionId());
                        jsonObject.put("name", ltd.getName(locale));
                        jsonObject.put("erc", ltd.getExternalReferenceCode());
                        jsonArray.put(jsonObject);
                    }
                    break;
                    
                case "object":
                    List<ObjectDefinition> objectDefinitions = 
                        objectDefinitionLocalService.getObjectDefinitions(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
                    
                    for (ObjectDefinition obj : objectDefinitions) {
                        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
                        jsonObject.put("id", obj.getObjectDefinitionId());
                        jsonObject.put("name", obj.getName());
                        jsonObject.put("erc", obj.getExternalReferenceCode());
                        jsonArray.put(jsonObject);
                    }
                    break;
                    
                case "objectfolder":
                    List<ObjectFolder> objectFolders = 
                        objectFolderLocalService.getObjectFolders(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
                    
                    for (ObjectFolder folder : objectFolders) {
                        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
                        jsonObject.put("id", folder.getObjectFolderId());
                        jsonObject.put("name", folder.getName());
                        jsonObject.put("erc", folder.getExternalReferenceCode());
                        jsonArray.put(jsonObject);
                    }
                    break;
            }
            
            resourceResponse.getWriter().write(jsonArray.toString());

        } catch (IOException e) {
            throw new PortletException("Failed to write JSON response", e);
        }

        return true;
    }
}