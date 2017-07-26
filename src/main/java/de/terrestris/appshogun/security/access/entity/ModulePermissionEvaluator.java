/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.module.Module;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class ModulePermissionEvaluator<E extends Module> extends ProjectPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ModulePermissionEvaluator() {
		this((Class<E>) Module.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ModulePermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E module, Permission permission) {

		// all users but default users and editors are allowed to create Module
		if (permission.equals(Permission.CREATE) && (module == null || module.getId() == null) &&
				! ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! ProjectSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// always allow read on Module...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultProjectPermission(user, module, permission);
	}

}
