package Liferay.Serivce.Override;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

@Component(
	    immediate = true,
	    property = {
	        "key=login.events.post",
	        "service.ranking:Integer=100"  
	    },
	    service = LifecycleAction.class
	)
	public class CustomPostLoginOverride implements LifecycleAction {

	    @Override
	    public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
	        HttpServletRequest request = lifecycleEvent.getRequest();
	        HttpServletResponse response = lifecycleEvent.getResponse();
	       

	        try {
	           
	            
	                
	                response.sendRedirect("/dashboard");
	           
	        } catch (Exception e) {
	            throw new ActionException(e);
	        }
	    }
	}

