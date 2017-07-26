package de.terrestris.appshogun.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.terrestris.appshogun.dao.ProjectUserGroupDao;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.service.ProjectUserGroupService;
import de.terrestris.shogun2.rest.UserGroupRestController;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 * @param <S>
 */
@RestController
@RequestMapping("/projectusergroups")
public class ProjectUserGroupRestController<E extends ProjectUserGroup, D extends ProjectUserGroupDao<E>, S extends ProjectUserGroupService<E, D>>
		extends UserGroupRestController<E, D, S> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserGroupRestController() {
		this((Class<E>) ProjectUserGroup.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the controller.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectUserGroupRestController(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct service here.
	 * Otherwise, spring can not decide which service has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectUserGroupService")
	public void setService(S service) {
		this.service = service;
	}

}
