package com.liferay.serivce.override;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.liferay.serivce.override.constant.AppConstants;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, property = { "key=login.events.post",
		"service.ranking:Integer=100" }, service = LifecycleAction.class)
public class CustomPostLoginOverride implements LifecycleAction {

	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {
		HttpServletRequest request = lifecycleEvent.getRequest();
		HttpServletResponse response = lifecycleEvent.getResponse();

		try {
			response.sendRedirect(AppConstants.DASHBOARDURL);

		} catch (Exception e) {
			_log.error(e);
			throw new ActionException(e);

		}
	}

	private static final Log _log = LogFactoryUtil.getLog(CustomPostLoginOverride.class);
}
