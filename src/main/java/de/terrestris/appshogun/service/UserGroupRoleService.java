package de.terrestris.appshogun.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jboss.elasticsearch.tools.content.InvalidDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.mail.SimpleMailMessage;
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
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import de.terrestris.shogun2.service.RoleService;
import de.terrestris.shogun2.util.mail.MailPublisher;

/**
 *
 * @author Daniel Koch
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("userGroupRoleService")
public class UserGroupRoleService<E extends UserGroupRole, D extends UserGroupRoleDao<E>>
		extends PermissionAwareCrudService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public UserGroupRoleService() {
		this((Class<E>) UserGroupRole.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected UserGroupRoleService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("userGroupRoleDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * user service
	 */
	@Autowired
	@Qualifier("projectUserService")
	private ProjectUserService<ProjectUser, ProjectUserDao<ProjectUser>> projectUserService;

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
	@Qualifier("projectUserGroupService")
	private ProjectUserGroupService<ProjectUserGroup, ProjectUserGroupDao<ProjectUserGroup>> projectUserGroupService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectConfigHolder")
	private ProjectConfigHolder projectConfigHolder;

	/**
	 *
	 */
	@Autowired
	private MailPublisher mailPublisher;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplateForUser-en")
	private SimpleMailMessage changePermissionsMailMessageTemplateForUser_en;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplateForUser-de")
	private SimpleMailMessage changePermissionsMailMessageTemplateForUser_de;

	/**
	 *
	 * @param user
	 * @return
	 */
	public Set<Role> findAllUserRoles(ProjectUser user) {
		Set<Role> allUserRoles = new HashSet<Role>();

		if (user != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				Role userRole = userGroupRole.getRole();
				allUserRoles.add(userRole);
			}
		}

		return allUserRoles;
	}

	/**
	 *
	 * @param user
	 * @return
	 */
	public Set<ProjectUserGroup> findAllUserGroups(ProjectUser user) {
		Set<ProjectUserGroup> allUserGroups = new HashSet<ProjectUserGroup>();

		if (user != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				ProjectUserGroup userGroup = userGroupRole.getGroup();
				allUserGroups.add(userGroup);
			}
		}

		return allUserGroups;
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	public Set<Role> findAllUserGroupRoles(ProjectUserGroup userGroup) {
		Set<Role> allUserGroupRoles = new HashSet<Role>();

		if (userGroup != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(userGroup);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				Role groupRole = userGroupRole.getRole();
				allUserGroupRoles.add(groupRole);
			}
		}

		return allUserGroupRoles;
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	public Set<ProjectUser> findAllUserGroupMembers(ProjectUserGroup userGroup) {
		Set<ProjectUser> allUserGroupMembers = new HashSet<ProjectUser>();

		if (userGroup != null) {
			List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(userGroup);

			for (UserGroupRole userGroupRole : userGroupRoles) {
				ProjectUser userGroupMember = userGroupRole.getUser();
				allUserGroupMembers.add(userGroupMember);
			}
		}

		return allUserGroupMembers;
	}

	/**
	 *
	 * @param user
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(ProjectUser user) {
		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"user", user);
	}

	/**
	 *
	 * @param userGroup
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(ProjectUserGroup userGroup) {
		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"group", userGroup);
	}

	/**
	 *
	 * @param role
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(Role role) {
		return (List<UserGroupRole>) this.dao.findAllWhereFieldEquals(
				"role", role);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(ProjectUser user, ProjectUserGroup userGroup) {

		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup)
		);

		return (List<UserGroupRole>) this.dao.findByCriteria(and);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @param role
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGroupRole> findUserGroupRolesBy(ProjectUser user, ProjectUserGroup userGroup, Role role) {
		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup),
				Restrictions.eq("role", role)
		);

		return (List<UserGroupRole>) this.dao.findByCriteria(and);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @return
	 */
	public boolean isUserMemberInUserGroup(ProjectUser user, ProjectUserGroup userGroup) {
		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup)
		);

		return (this.dao.getTotalCount(and).longValue() > 0);
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 * @param role
	 * @return
	 */
	public boolean hasUserRoleInGroup(ProjectUser user, ProjectUserGroup userGroup, Role role) {
		final Criterion and = Restrictions.and(
				Restrictions.eq("user", user),
				Restrictions.eq("group", userGroup),
				Restrictions.eq("role", role)
		);
		return (this.dao.getTotalCount(and).longValue() > 0);
	}

	/**
	 * TODO Check if correct security annotation
	 * TODO Add role for new user
	 *
	 * @param user
	 * @param userGroup
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("isAuthenticated()")
	public void addUserToGroup(ProjectUser user, ProjectUserGroup userGroup, Role role) {
		// TODO add isnull check
		if (this.isUserMemberInUserGroup(user, userGroup)) {
			LOG.trace("User with ID " + user.getId() + " is already a member of "
					+ "group with ID " + userGroup.getId());
			return;
		}

		UserGroupRole userGroupRole = new UserGroupRole(user, userGroup);

		this.dao.saveOrUpdate((E) userGroupRole);
	}

	/**
	 * TODO Check if correct security annotation
	 *
	 * @param user
	 * @param userGroup
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("isAuthenticated()")
	public void removeUserFromGroup(ProjectUser user, ProjectUserGroup userGroup) {
		// TODO add isnull check
		if (this.isUserMemberInUserGroup(user, userGroup) == false) {
			LOG.trace("User with ID " + user.getId() + " is not a member of "
					+ "group with ID " + userGroup.getId());
			return;
		}

		List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user, userGroup);

		for (UserGroupRole userGroupRole : userGroupRoles) {
			this.dao.delete((E) userGroupRole);
		}
	}

	/**
	 *
	 * @param user
	 * @param userGroup
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("isAuthenticated()")
	public void removeUserPermissionsFromGroup(ProjectUser user, ProjectUserGroup userGroup) {
		if (this.isUserMemberInUserGroup(user, userGroup) == false) {
			LOG.trace("User with ID " + user.getId() + " is not a member of "
					+ "group with ID " + userGroup.getId());
			return;
		}

		List<UserGroupRole> userGroupRoles = this.findUserGroupRolesBy(user, userGroup);

		for (UserGroupRole userGroupRole : userGroupRoles) {
			removeAndSaveUserPermissions((E) userGroupRole, (User) user, Permission.ADMIN);
		}
	}

	/**
	 * TODO test if this is transactional
	 * @param permissions
	 */
	@SuppressWarnings("unchecked")
	@PreAuthorize("hasRole(@projectConfigHolder.getSuperAdminRoleName())"
			+ " or hasRole(@projectConfigHolder.getSubAdminRoleName())")
	public void updateUserGroupRole(List<Map<String, String>> permissions) {
		ProjectUser currentUser = projectUserService.getUserBySession();
		Set<Role> allRoles = findAllUserRoles(currentUser);

		String subAdminRoleName = projectConfigHolder.getSubAdminRoleName();
		String superAdminRoleName = projectConfigHolder.getSuperAdminRoleName();
		Role subadminRole = roleService.findByRoleName(subAdminRoleName);
		Role superadminRole = roleService.findByRoleName(superAdminRoleName);
		boolean isSuperAdmin = allRoles.contains(superadminRole);
		boolean isSubAdmin = allRoles.contains(subadminRole);

		if (!isSuperAdmin && !isSubAdmin) {
			throw new PermissionDeniedDataAccessException(
					"Current user has no permission to edit userGroupRoles", null);
		}

		for (Map<String, String> permissionsMap : permissions) {

			LOG.debug("Changing a userGroupRole for a user...");
			String groupIdString = permissionsMap.get("group");
			String userGroupRoleIdString = permissionsMap.get("id");
			String roleString = permissionsMap.get("role");
			String userIdString = permissionsMap.get("user");

			// check for valid data
			if (groupIdString == null || roleString == null || userIdString == null) {
				throw new InvalidDataException(
						"Could not update userGroupPermissions due to invalid data");
			}
			Integer userId = Integer.parseInt(userIdString);
			Integer groupId = Integer.parseInt(groupIdString);
			Integer userGroupRoleId = null;

			if (!StringUtils.isEmpty(userGroupRoleIdString)) {
				userGroupRoleId = Integer.parseInt(userGroupRoleIdString);
			}

			// check for restricted role
			if (roleString.equalsIgnoreCase("ROLE_ADMIN")) {
				throw new PermissionDeniedDataAccessException(
						"Requested a ROLE_ADMIN, this is forbidden!", null);
			}

			UserGroupRole userGroupRole = null;
			ProjectUserGroup userGroup = projectUserGroupService.findById(groupId);
			if (userGroupRoleId != null) {
				userGroupRole = findById(userGroupRoleId);
			}
			Role wantedRole = roleService.findByRoleName(roleString);
			ProjectUser user = projectUserService.findById(userId);

			// check for valid data
			if (userGroup == null || user == null) {
				throw new InvalidDataException(
						"Could not update userGroupPermissions due to invalid data");
			}

			// check if the updating person has the right for the current group
			boolean hasSubadminRoleInGroup = hasUserRoleInGroup(currentUser, userGroup, subadminRole);
			boolean isOwner = userGroup.getOwner().equals(currentUser);
			if (!hasSubadminRoleInGroup && !isSuperAdmin && !isOwner) {
				throw new PermissionDeniedDataAccessException(
						"Current user has no permission to edit userGroupRoles", null);
			}

			// check for current usergrouproles of this user
			List<UserGroupRole> currentUserGroupRoles = findUserGroupRolesBy(user);
			for (UserGroupRole currentUserGroupRole : currentUserGroupRoles) {
				ProjectUserGroup currentUserGroupRoleGroup = currentUserGroupRole.getGroup();
				if (currentUserGroupRoleGroup != null && currentUserGroupRoleGroup.equals(userGroup)) {
					// user already has a usergrouprole for this group. we
					// just need to modify this role
					LOG.debug("Updating an already exisiting userGroupRole for current group");
					userGroupRole = currentUserGroupRole;
				}
			}

			// check if we can edit an existing permission or if we have
			// to create a new one
			if (userGroupRole == null) {
				LOG.debug("Creating a new userGroupRole for current group and user");
				userGroupRole = new UserGroupRole();
			}

			// check if user has to be removed from a group (removed all permissions)
			if (wantedRole == null && roleString.equalsIgnoreCase("remove")) {
				LOG.debug("Deleting a userGroupRole, as the user shall have no permissions anymore");
				removeUserFromGroup(user, userGroup);
				removeUserPermissionsFromGroup(user, userGroup);
			} else {
				userGroupRole.setRole(wantedRole);
				userGroupRole.setGroup(userGroup);
				userGroupRole.setUser(user);

				dao.saveOrUpdate((E) userGroupRole);
				addAndSaveUserPermissions((E) userGroupRole, (User) user, Permission.READ);

			}
			// send a mail to the user
			sendPermissionChangeMail(user, roleString, userGroup);
		}
	}

	/**
	 * Sends a mail to the user containing the changed permissions.
	 * Language of the receiver is taken into account
	 *
	 * @param receiver
	 * @param wantedRole
	 * @param group
	 * @param user
	 */
	public void sendPermissionChangeMail(ProjectUser receiver, String givenRole,
			ProjectUserGroup group) {

		String lang = "en";
		if (receiver.getLanguage() != null) {
			lang = receiver.getLanguage().toLanguageTag();
		}
		String email = receiver.getEmail();
		if (email == null) {
			throw new RuntimeException("User has no mailadress attached, cancelled sending of mail");
		}

		SimpleMailMessage changePermissionMailTemplateMsg = null;
		// Create a thread safe "copy" of the template message, depending on the users language
		if (lang.equals("de")) {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplateForUser_de()
			);
		} else {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplateForUser_en()
			);
		}

		// Prepare a personalized mail in the correct language
		changePermissionMailTemplateMsg.setTo(email);
		changePermissionMailTemplateMsg.setText(
				String.format(
						changePermissionMailTemplateMsg.getText(),
						receiver.getFirstName(),
						receiver.getLastName(),
						givenRole,
						group.getName()
				)
		);
		// and send the mail
		mailPublisher.sendMail(changePermissionMailTemplateMsg);
	}

	/**
	 * @return the changePermissionsMailMessageTemplateForUser_en
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplateForUser_en() {
		return changePermissionsMailMessageTemplateForUser_en;
	}

	/**
	 * @return the changePermissionsMailMessageTemplateForUser_de
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplateForUser_de() {
		return changePermissionsMailMessageTemplateForUser_de;
	}

}
