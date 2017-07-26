/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.module.Map;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Johannes Weskamm
 * @param <E>
 *
 */
public class MapPermissionEvaluator<E extends Map> extends ProjectPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public MapPermissionEvaluator() {
		this((Class<E>) Map.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected MapPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E map, Permission permission) {

		// all users but default users and editors are allowed to create Map
		if (permission.equals(Permission.CREATE) && (map == null || map.getId() == null) &&
				! ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser() && ! ProjectSecurityUtil.currentUsersHighestRoleIsEditor()) {
			return true;
		}

		// always allow read on map...
		if (permission.equals(Permission.READ)) {
			return true;
		}

		/**
		 * by default look for granted rights
		 */
		return hasDefaultProjectPermission(user, map, permission);
	}

}
