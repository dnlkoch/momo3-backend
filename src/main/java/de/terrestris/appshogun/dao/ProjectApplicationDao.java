package de.terrestris.appshogun.dao;

import org.springframework.stereotype.Repository;

import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.shogun2.dao.ApplicationDao;

/**
 *
 * @author Nils Bühner
 *
 * @param <E>
 */
@Repository("projectApplicationDao")
public class ProjectApplicationDao<E extends ProjectApplication> extends ApplicationDao<E> {

	/**
	 * Public default constructor for this DAO.
	 */
	@SuppressWarnings("unchecked")
	public ProjectApplicationDao() {
		super((Class<E>) ProjectApplication.class);
	}

	/**
	 * Constructor that has to be called by subclasses.
	 *
	 * @param clazz
	 */
	protected ProjectApplicationDao(Class<E> clazz) {
		super(clazz);
	}

}
