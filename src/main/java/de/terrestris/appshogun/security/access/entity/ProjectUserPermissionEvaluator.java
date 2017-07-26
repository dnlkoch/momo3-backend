/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.util.config.ProjectConfigHolder;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.security.access.entity.UserPermissionEvaluator;


/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 05.04.2017
 *
 * @param <E>
 */
public class ProjectUserPermissionEvaluator<E extends ProjectUser> extends UserPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserPermissionEvaluator() {
		this((Class<E>) ProjectUser.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ProjectUserPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectConfigHolder")
	private ProjectConfigHolder projectConfigHolder;

	/**
	 * Always grants right to READ, UPDATE and DELETE an user.
	 */
	@Override
	public boolean hasPermission(User user, E projectUser, Permission permission) {

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		// Always restrict CREATE right for this entity. Users can only be created by themselves via email.
		if (permission.equals(Permission.CREATE)) {
			LOG.trace(String.format(restrictMsg, permission, simpleClassName, projectUser.getId()));
			return false;
		}

		// Always grant READ right for this entity.
		if (permission.equals(Permission.READ)) {
			// each user can read its own props
			if (user.equals(projectUser)) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, projectUser.getId()));
				return true;
			}

			// always allow to read all users for an user that has at least the following role: ROLE_EDITOR
			Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
					.getAuthentication().getAuthorities();
			for (GrantedAuthority grantedAuthority : authorities) {
				if (grantedAuthority.getAuthority().equalsIgnoreCase(projectConfigHolder.getEditorRoleName()) ||
						grantedAuthority.getAuthority().equalsIgnoreCase(projectConfigHolder.getSubAdminRoleName())) {
					LOG.trace(String.format(grantMsg, permission, simpleClassName, projectUser.getId()));
					return true;
				}
			}
		}

		// Grant UPDATE right for this entity only for user.
		if (permission.equals(Permission.UPDATE)) {
			if (projectUser.getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, projectUser.getId()));
				return true;
			}
		}

		// Grant DELETE right for this entity
		// - user want's to remove his account
		if (permission.equals(Permission.DELETE)) {
			if (projectUser.getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, projectUser.getId()));
				return true;
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, projectUser.getId()));
		return false;

		// We don't call the parent implementation from SHOGun2 here as we
		// do have an intended override for the getMembers() of the ProjectUserGroup
		// present that will cause the parent method to fail.
		// return super.hasPermission(user, userGroup, permission);
	}

	/**
	 * @param projectConfigHolder the projectConfigHolder to set
	 */
	public void setProjectConfigHolder(ProjectConfigHolder projectConfigHolder) {
		this.projectConfigHolder = projectConfigHolder;
	}

}
