package de.terrestris.appshogun.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import de.terrestris.appshogun.dao.ProjectUserDao;
import de.terrestris.appshogun.dao.ProjectUserGroupDao;
import de.terrestris.appshogun.dao.UserGroupRoleDao;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.model.security.UserGroupRole;
import de.terrestris.appshogun.util.config.ProjectConfigHolder;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.service.RoleService;
import de.terrestris.shogun2.service.UserGroupService;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("projectUserGroupService")
public class ProjectUserGroupService<E extends ProjectUserGroup, D extends ProjectUserGroupDao<E>>
		extends UserGroupService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserGroupService() {
		this((Class<E>) ProjectUserGroup.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectUserGroupService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectUserGroupDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	@Autowired
	@Qualifier("projectUserService")
	private ProjectUserService<ProjectUser, ProjectUserDao<ProjectUser>> projectUserService;

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 * Role service
	 */
	@Autowired
	@Qualifier("roleService")
	protected RoleService<Role, RoleDao<Role>> roleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectConfigHolder")
	private ProjectConfigHolder projectConfigHolder;

	/**
	 * Override in order to set the owner and correct class..
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())"
			+ " or (#e.id == null and hasPermission(#e, 'CREATE'))"
			+ " or (#e.id != null and hasPermission(#e, 'UPDATE'))")
	public void saveOrUpdate(E e) {
		// set owner in create mode
		if (e.getId() == null) {
			ProjectUser user = projectUserService.getUserBySession();
			if (user != null) {
				e.setOwner(user);
				// also grant creator the subadmin role to this group
				// so that he will be able to edit it afterwards
				String subAdminRoleName = projectConfigHolder.getSubAdminRoleName();
				Role subadminRole = roleService.findByRoleName(subAdminRoleName);
				UserGroupRole userGroupRole = new UserGroupRole(user, e, subadminRole);
				userGroupRoleService.saveOrUpdate(userGroupRole);
			}
		}
		dao.saveOrUpdate(e);
	}

	/**
	 * Override in order to handle deletion correctly...
	 * TODO: what we really need to delete?
	 */
	@Override
	@PreAuthorize("hasRole(@configHolder.getSuperAdminRoleName())"
			+ " or (#e.id != null and hasPermission(#e, 'DELETE'))")
	public void delete(E e) {
		Set<ProjectUser> members = userGroupRoleService.findAllUserGroupMembers(e);
		for (ProjectUser projectUser : members) {
			userGroupRoleService.removeUserPermissionsFromGroup(projectUser, e);
			userGroupRoleService.removeUserFromGroup(projectUser, e);
		}

		dao.delete(e);
	}
}
