/**
 *
 */
package de.terrestris.appshogun.security.access.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.terrestris.appshogun.dao.UserGroupRoleDao;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.model.security.UserGroupRole;
import de.terrestris.appshogun.service.UserGroupRoleService;
import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.security.access.entity.UserGroupPermissionEvaluator;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 */
public class ProjectUserGroupPermissionEvaluator<E extends ProjectUserGroup> extends UserGroupPermissionEvaluator<E> {

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserGroupPermissionEvaluator() {
		this((Class<E>) ProjectUserGroup.class);
	}

	/**
	 * Constructor for subclasses
	 *
	 * @param entityClass
	 */
	protected ProjectUserGroupPermissionEvaluator(Class<E> entityClass) {
		super(entityClass);
	}

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 * Always grants right to READ, UPDATE and CREATE this entity.
	 */
	@Override
	public boolean hasPermission(User user, E userGroup, Permission permission) {

		final String simpleClassName = getEntityClass().getSimpleName();

		String grantMsg = "Granting %s access on secured object \"%s\" with ID %s";
		String restrictMsg = "Restricting %s access on secured object \"%s\" with ID %s";

		// Allow create of groups only for ROLE_SUPERADMIN or ROLE_SUBADMIN
		if (permission.equals(Permission.CREATE)) {
			if (ProjectSecurityUtil.currentUserIsSuperAdmin() || ProjectSecurityUtil.currentUserHasRoleSubAdmin()){
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
				return true;
			}
		}

		// Always grant READ right for this entity.
		if (permission.equals(Permission.READ)) {
			LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
			return true;
		}

		// Grant UPDATE right for this entity, if the user is superadmin or the owner.
		if (permission.equals(Permission.UPDATE)) {
			if (ProjectSecurityUtil.currentUserIsSuperAdmin() || userGroup.getOwner() != null &&
					userGroup.getOwner().getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
				return true;
			}
		}

		// Grant DELETE right for this entity, if the user is superadmin or the owner.
		if (permission.equals(Permission.DELETE)) {
			if (ProjectSecurityUtil.currentUserIsSuperAdmin() || userGroup.getOwner() != null &&
					userGroup.getOwner().getId().equals(user.getId())) {
				LOG.trace(String.format(grantMsg, permission, simpleClassName, userGroup.getId()));
				return true;
			}
		}

		LOG.trace(String.format(restrictMsg, permission, simpleClassName, userGroup.getId()));

		return false;

		// We don't call the parent implementation from SHOGun2 here as we
		// do have an intended override for the getMembers() of the ProjectUserGroup
		// present that will cause the parent method to fail.
		// return super.hasPermission(user, userGroup, permission);
	}

}
