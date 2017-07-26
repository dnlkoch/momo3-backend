package de.terrestris.appshogun.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.shogun2.dao.UserGroupDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("projectUserGroupDao")
public class ProjectUserGroupDao<E extends ProjectUserGroup> extends UserGroupDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserGroupDao() {
		super((Class<E>) ProjectUserGroup.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ProjectUserGroupDao(Class<E> clazz) {
		super(clazz);
	}
}
