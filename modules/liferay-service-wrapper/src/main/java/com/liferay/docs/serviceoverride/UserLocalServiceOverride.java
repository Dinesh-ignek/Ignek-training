package com.liferay.docs.serviceoverride;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroupRole;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceWrapper;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author dell
 */
@Component(property = {}, service = ServiceWrapper.class)
public class UserLocalServiceOverride extends UserLocalServiceWrapper {

	public UserLocalServiceOverride() {
		super(null);
	}

	@Override
	public int authenticateByEmailAddress(long companyId, String emailAddress, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap, Map<String, Object> resultsMap)
			throws PortalException {

		System.out.println("Authenticating user by email address " + emailAddress);
		return super.authenticateByEmailAddress(companyId, emailAddress, password, headerMap, parameterMap, resultsMap);
	}

	@Override
	public User getUser(long userId) throws PortalException {
		System.out.println("Getting user by id " + userId);
		return super.getUser(userId);
	}

	@Override
	public User updateUser(long userId, String oldPassword, String newPassword1, String newPassword2,
			boolean passwordReset, String reminderQueryQuestion, String reminderQueryAnswer, String screenName,
			String emailAddress, boolean hasPortrait, byte[] portraitBytes, String languageId, String timeZoneId,
			String greeting, String comments, String firstName, String middleName, String lastName,
			long prefixListTypeId, long suffixListTypeId, boolean male, int birthdayMonth, int birthdayDay,
			int birthdayYear, String smsSn, String facebookSn, String jabberSn, String skypeSn, String twitterSn,
			String jobTitle, long[] groupIds, long[] organizationIds, long[] roleIds,
			List<UserGroupRole> userGroupRoles, long[] userGroupIds, ServiceContext serviceContext)
			throws PortalException {

		System.out.println("user updated" + userId);
		return super.updateUser(userId, oldPassword, newPassword1, newPassword2, passwordReset, reminderQueryQuestion,
				reminderQueryAnswer, screenName, emailAddress, hasPortrait, portraitBytes, languageId, timeZoneId,
				greeting, comments, firstName, middleName, lastName, prefixListTypeId, suffixListTypeId, male,
				birthdayMonth, birthdayDay, birthdayYear, smsSn, facebookSn, jabberSn, skypeSn, twitterSn, jobTitle,
				groupIds, organizationIds, roleIds, userGroupRoles, userGroupIds, serviceContext);
	}

	;

	@Reference(unbind = "-")
	private void serviceSetter(UserLocalService userLocalService) {
		setWrappedService(userLocalService);
	}
}