/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class ProjectApplicationPermissionEvaluator<E extends ProjectApplication> extends ProjectPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectApplicationPermissionEvaluator() {
		this((Class<E>) ProjectApplication.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ProjectApplicationPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E application, Permission permission) {

		// all users but default users and editors are allowed to create applications
		if (permission.equals(Permission.CREATE) && (application == null || application.getId() == null) &&
				! ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! ProjectSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultProjectPermission(user, application, permission);
	}

}
