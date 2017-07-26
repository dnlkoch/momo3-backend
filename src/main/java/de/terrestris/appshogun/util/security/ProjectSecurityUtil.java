package de.terrestris.appshogun.util.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.util.config.ProjectConfigHolder;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 06.04.2017
 *
 */
@Component
public class ProjectSecurityUtil {

	public static ProjectConfigHolder configHolder;

	/**
	 *
	 * @return
	 */
	public static boolean currentUserIsSuperAdmin(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof ProjectUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getSuperAdminRoleName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUserHasRoleSubAdmin(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof ProjectUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getSubAdminRoleName())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUsersHighestRoleIsEditor(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof ProjectUser) {
			for (GrantedAuthority authority : authorities) {
				if(authority.getAuthority().equals(configHolder.getEditorRoleName())) {
					return !ProjectSecurityUtil.currentUserHasRoleSubAdmin();
				}
			}
		}

		return false;
	}

	/**
	 *
	 * @return
	 */
	public static boolean currentUsersHighestRoleIsDefaultUser(){
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		final Object principal = authentication.getPrincipal();

		if (principal instanceof ProjectUser) {
			for (GrantedAuthority authority : authorities) {
				// if user's highest role is ROLE_USER, the list of authorities will always have one single element
				if(authority.getAuthority().equals(configHolder.getDefaultUserRoleName()) && authorities.size() == 1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @param configHolder the configHolder to set
	 */
	@Autowired
	@Qualifier("projectConfigHolder")
	public void setConfigHolder(ProjectConfigHolder configHolder) {
		ProjectSecurityUtil.configHolder = configHolder;
	}

}
