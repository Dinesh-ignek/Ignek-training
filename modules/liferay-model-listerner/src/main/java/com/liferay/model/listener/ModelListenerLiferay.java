package com.liferay.model.listener;


import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	    service = ModelListener.class,
	    immediate = true
	)
	public class ModelListenerLiferay extends BaseModelListener<User> {

	@Activate
	protected void activate() {
	    _log.info("ModelListenerLiferay activated");
	}

	public void onBeforeCreate(User userModel) throws ModelListenerException {
		_log.info("in onBeforeCreate method");
		_log.info("userModel : " + userModel);
		super.onBeforeCreate(userModel);
	}

	
	public void onAfterCreate(User userModel) throws ModelListenerException {
		_log.info("in onAfterCreate method");
		_log.info("userModel : " + userModel);
		super.onAfterCreate(userModel);
	}
	public void onAfterRemove(User UserModel) throws ModelListenerException{
		_log.info("removed user" + UserModel.getUserId());
	}
	
	private static final Log _log = LogFactoryUtil.getLog(
			ModelListenerLiferay.class);
}