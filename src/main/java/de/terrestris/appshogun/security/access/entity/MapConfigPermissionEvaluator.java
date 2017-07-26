/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.map.MapConfig;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MapConfigPermissionEvaluator<E extends MapConfig> extends ProjectPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MapConfigPermissionEvaluator() {
		this((Class<E>) MapConfig.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MapConfigPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E mapConfig, Permission permission) {

		// all users but default users and editors are allowed to create MapConfig
		if (permission.equals(Permission.CREATE) && (mapConfig == null || mapConfig.getId() == null) &&
				! ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! ProjectSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// always allow read on mapConfig...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultProjectPermission(user, mapConfig, permission);
	}

}
