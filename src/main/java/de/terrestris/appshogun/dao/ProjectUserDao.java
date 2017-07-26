package de.terrestris.appshogun.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.shogun2.dao.UserDao;

/**
 *
 * @author Nils Bühner
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
@Repository("projectUserDao")
public class ProjectUserDao<E extends ProjectUser> extends UserDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserDao() {
		super((Class<E>) ProjectUser.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ProjectUserDao(Class<E> clazz) {
		super(clazz);
	}
}
