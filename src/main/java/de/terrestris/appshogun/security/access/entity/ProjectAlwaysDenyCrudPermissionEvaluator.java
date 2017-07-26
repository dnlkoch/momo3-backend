package de.terrestris.appshogun.security.access.entity;

import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;

/**
 * @author Nils Bühner
 *
 */
public class ProjectAlwaysDenyCrudPermissionEvaluator<E extends PersistentObject> extends
		ProjectPersistentObjectPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectAlwaysDenyCrudPermissionEvaluator() {
		this((Class<E>) PersistentObject.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ProjectAlwaysDenyCrudPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Override
	public boolean hasPermission(User user, E entity, Permission permission) {
		return false;
	}

}
