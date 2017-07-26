package de.terrestris.appshogun.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.terrestris.appshogun.dao.ProjectApplicationDao;
import de.terrestris.appshogun.dao.ProjectLayerDao;
import de.terrestris.appshogun.dao.ProjectUserDao;
import de.terrestris.appshogun.dao.ProjectUserGroupDao;
import de.terrestris.appshogun.dao.UserGroupRoleDao;
import de.terrestris.appshogun.model.ProjectApplication;
import de.terrestris.appshogun.model.ProjectLayer;
import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.model.ProjectUserGroup;
import de.terrestris.appshogun.model.security.UserGroupRole;
import de.terrestris.shogun2.dao.GenericHibernateDao;
import de.terrestris.shogun2.dao.RegistrationTokenDao;
import de.terrestris.shogun2.dao.RoleDao;
import de.terrestris.shogun2.dao.UserGroupDao;
import de.terrestris.shogun2.model.PersistentObject;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.UserGroup;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.model.security.PermissionCollection;
import de.terrestris.shogun2.model.token.RegistrationToken;
import de.terrestris.shogun2.service.PermissionAwareCrudService;
import de.terrestris.shogun2.service.RoleService;
import de.terrestris.shogun2.service.UserService;
import de.terrestris.shogun2.util.mail.MailPublisher;
import javassist.NotFoundException;

/**
 *
 * @author Johannes Weskamm
 * @author terrestris GmbH & Co. KG
 *
 * @param <E>
 * @param <D>
 */
@Service("projectUserService")
public class ProjectUserService<E extends ProjectUser, D extends ProjectUserDao<E>>
		extends UserService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectUserService() {
		this((Class<E>) ProjectUser.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectUserService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 *
	 */
	@Autowired
	private MailPublisher mailPublisher;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userGroupRoleDao")
	private UserGroupRoleDao<UserGroupRole> userGroupRoleDao;

	@Autowired
	@Qualifier("userGroupRoleService")
	private UserGroupRoleService<UserGroupRole, UserGroupRoleDao<UserGroupRole>> userGroupRoleService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("userGroupDao")
	private UserGroupDao<UserGroup> userGroupDao;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectUserGroupDao")
	private ProjectUserGroupDao<ProjectUserGroup> projectUserGroupDao;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectLayerDao")
	private ProjectLayerDao<ProjectLayer> layerDao;

	/**
	 *
	 */
	@Autowired
	@Qualifier("projectApplicationDao")
	private ProjectApplicationDao<ProjectApplication> applicationDao;

	/**
	 * Role service
	 */
	@Autowired
	@Qualifier("roleService")
	protected RoleService<Role, RoleDao<Role>> roleService;

	@Autowired
	@Qualifier("permissionAwareCrudService")
	private PermissionAwareCrudService<PersistentObject, GenericHibernateDao<PersistentObject, Integer>> permissionAwareCrudService;

	@Autowired
	@Qualifier("projectRegistrationTokenService")
	private ProjectRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> projectRegistrationTokenService;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplate-en")
	private SimpleMailMessage changePermissionsMailMessageTemplate_en;

	/**
	 *
	 */
	@Autowired
	@Qualifier("changePermissionsMailMessageTemplate-de")
	private SimpleMailMessage changePermissionsMailMessageTemplate_de;

	/**
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void activateUser(String tokenValue) throws Exception {

		RegistrationToken token = projectRegistrationTokenService.findByTokenValue(tokenValue);

		LOG.debug("Trying to activate user account with token: " + tokenValue);

		// throws Exception if token is not valid
		projectRegistrationTokenService.validateToken(token);

		// set active=true
		E user = (E) token.getUser();
		user.setActive(true);

		// Add the UserGroupRole for the newly activated user with DEFAULT_USER role.
		if (this.getDefaultUserRole() != null) {
			UserGroupRole userGroupRole = new UserGroupRole();
			userGroupRole.setUser(user);
			userGroupRole.setRole(this.roleService.findByRoleName(
					this.getDefaultUserRole().getName()));

			userGroupRoleDao.saveOrUpdate(userGroupRole);
		}

		// update the user
		dao.saveOrUpdate((E) user);

		// delete the token
		projectRegistrationTokenService.deleteTokenAfterActivation(token);

		LOG.info("The user '" + user.getAccountName()
				+ "' has successfully been activated.");
	}

	/**
	 * Registers a new user. Initially, the user will be inactive. An email with
	 * an activation link will be sent to the user.
	 *
	 * @param user A user with an UNencrypted password (!)
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Override
	public E registerUser(E user, HttpServletRequest request) throws Exception {

		String email = user.getEmail();

		// check if a user with the email already exists
		E existingUser = dao.findByEmail(email);

		if(existingUser != null) {
			final String errorMessage = "User with eMail '" + email + "' already exists.";
			LOG.info(errorMessage);
			throw new Exception(errorMessage);
		}

		user = (E) this.persistNewUser(user, true);

		// create a token for the user and send an email with an "activation" link
		projectRegistrationTokenService.sendRegistrationActivationMail(request, user);

		return user;
	}

	/**
	 *
	 * @param request
	 * @param email
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 * @throws NotFoundException
	 * @throws AlreadyExistsException
	 */
	public void resendRegistrationTokenMail(HttpServletRequest request, String email)
			throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			UnsupportedEncodingException, URISyntaxException, NotFoundException {

		// Get the user by the provided email address
		User user = dao.findByEmail(email);

		if (user == null) {
			String userNotFoundMsg = "Could not find user with email: '" +
					email + "'";
			LOG.warn(userNotFoundMsg);
			throw new UsernameNotFoundException(userNotFoundMsg);
		}

		if (user.isActive()) {
			String userAlreadyActivatedMsg = "The user is already activated.";
			LOG.warn(userAlreadyActivatedMsg);
			throw new SecurityException(userAlreadyActivatedMsg);
		}

		RegistrationToken registrationToken = projectRegistrationTokenService.findByUser(user);

		if (registrationToken == null) {
			String noTokenFoundMsg = "No token found for the requested user.";
			LOG.warn(noTokenFoundMsg);
			throw new NotFoundException(noTokenFoundMsg);
		}

		projectRegistrationTokenService.sendRegistrationActivationMail(request, user);
	}

	/**
	 * Updates the users personal credentials and, if a change in permissions
	 * is made, will contact a subadmin / superadmin to make the appropriate changes
	 *
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param telephone
	 * @param department
	 * @param profileImage
	 * @param language
	 * @param permissions
	 */
	@SuppressWarnings("unchecked")
	public void updateUser(Integer userId, String firstName,String lastName,String email,String telephone,String department,
			String profileImage, String language, HashMap<String,String> permissions) {
		if (userId == null) {
			throw new IllegalArgumentException("No userId to update given");
		}
		E updatingUser = getUserBySession();
		E userToUpdate = findById(userId);

		if (!updatingUser.equals(userToUpdate)) {
			throw new AccessDeniedException("Users can only update themselves");
		}

		userToUpdate.setFirstName(firstName);
		userToUpdate.setLastName(lastName);
		userToUpdate.setEmail(email);
		userToUpdate.setTelephone(telephone);
		userToUpdate.setDepartment(department);
		userToUpdate.setProfileImage(profileImage);
		userToUpdate.setLanguage(new Locale(language));

		dao.saveOrUpdate(userToUpdate);

		// now handle the permission changes
		Role subadminRole = roleService.findByRoleName("ROLE_SUBADMIN");
		Role superadminRole = roleService.findByRoleName("ROLE_ADMIN");
		ProjectUser superadmin = null;
		List<ProjectUser> allUsers = (List<ProjectUser>) dao.findAll();
		for (ProjectUser projectUser : allUsers) {
			Set<Role> roles = userGroupRoleService.findAllUserRoles(projectUser);
			if (roles.contains(superadminRole)) {
				superadmin = projectUser;
			}
		}

		for (Entry<String, String> entry : permissions.entrySet()) {
			Integer groupId = Integer.valueOf(entry.getKey());
			String wantedRole = entry.getValue();

			// 1.) If user wants to become subadmin -> ask the superadmin for permission
			// 2.) If user wants to become editor -> ask the subadmin for permission
			// 3.) If user wants to become user -> ask the subadmin for permission
			// 4.) If user removed all rights in group -> ask the subadmin for permission
			// 4.) If no subadmin found for case 3., 4. and 5. -> ask the superadmin for permission
			ProjectUserGroup group = projectUserGroupDao.findById(groupId);

			if (group != null) {
				ProjectUser subadminForGroup = null;
				Set<ProjectUser> projectUsers = userGroupRoleService.findAllUserGroupMembers(group);
				for (ProjectUser projectUser : projectUsers) {
					if (userGroupRoleService.hasUserRoleInGroup(projectUser, group, subadminRole)) {
						subadminForGroup = projectUser;
					}
				}
				if (wantedRole.equals("ROLE_SUBADMIN")) {
					//sendmail to superadmin
					if (superadmin != null) {
						sendPermissionChangeMail(superadmin, wantedRole, group, userToUpdate);
					}
				} else if (wantedRole.equals("ROLE_EDITOR") ||
						wantedRole.equals("ROLE_USER") ||
						wantedRole.equals("REMOVE")) {
					//send mail to subadmin, or, if not available, to the superadmin
					sendMailToSubadminOrSuperadmin(subadminForGroup, superadmin, wantedRole, group, userToUpdate);
				}
			}
		}
	}

	/**
	 * Deletes a user and changes ownership of its entities to superadmin
	 * @throws UnavailableException
	 *
	 */
	public void deleteUser(Integer userId) throws UnavailableException {

		LOG.info("Trying to delete a user");

		E deletingUser = getUserBySession();
		E userToDelete = findById(userId);
		if (userToDelete == null) {
			throw new RuntimeException("User to delete could not be found");
		}

		// check if current user may delete the user
		Set<Role> rolesOfDeletingUser = userGroupRoleService.findAllUserRoles(deletingUser);
		Set<Role> rolesOfUserToDelete = userGroupRoleService.findAllUserRoles(userToDelete);
		Role adminRole = roleService.findByRoleName("ROLE_ADMIN");
		ProjectUser adminUser = null;
		List<E> allUsers = findAll();
		for (ProjectUser user : allUsers) {
			Set<Role> roles = userGroupRoleService.findAllUserRoles(user);
			if (roles.contains(adminRole)) {
				adminUser = user;
				break;
			}
		}

		if (adminUser == null) {
			throw new UnavailableException("Could not find the superadmin, aborting");
		}

		boolean deletingUserIsAdmin = rolesOfDeletingUser.contains(adminRole);
		boolean userToDeleteIsAdmin = rolesOfUserToDelete.contains(adminRole);
		if (!userToDelete.equals(deletingUser) &&
				!deletingUserIsAdmin) {
			throw new AccessDeniedException("Access is denied");
		}

		if (userToDeleteIsAdmin) {
			throw new AccessDeniedException("The Superadmin may not be deleted");
		}

		List<UserGroupRole> userGroupRoles = userGroupRoleService.findUserGroupRolesBy(userToDelete);
		for (UserGroupRole userGroupRole : userGroupRoles) {
			userGroupRoleService.delete(userGroupRole);
			LOG.debug("Deleted a user group role");
		}

		// Delete all remaining PermissionCollections for this user, e.g. webmap
		Map<PersistentObject, PermissionCollection> entityPermissionCollectionsForUser =
				dao.findAllUserPermissionsOfUser(userToDelete);
		Set<PersistentObject> entitiesWithPermissions = entityPermissionCollectionsForUser.keySet();

		for (PersistentObject persistentObject : entitiesWithPermissions) {
			// INFO: The hashcode of the persistentObject differs here, thats why we cannot use a call like
			// PermissionCollection permissionsOnEntity = entityPermissionCollectionsForUser.get(persistentObject);
			// to get the collection. So we get it by matching classname and id
			Set<Entry<PersistentObject, PermissionCollection>> entries = entityPermissionCollectionsForUser.entrySet();
			for (Entry<PersistentObject, PermissionCollection> entry : entries) {
				if (entry.getKey().getId().equals(persistentObject.getId()) &&
					entry.getKey().getClass().equals(persistentObject.getClass())) {
					PermissionCollection permissionsOnEntity = entry.getValue();
					Set<Permission> permissionsSet = permissionsOnEntity.getPermissions();
					Permission[] permissionsArray = permissionsSet.toArray(new Permission[permissionsSet.size()]);
					permissionAwareCrudService.removeAndSaveUserPermissions(persistentObject, userToDelete, permissionsArray);
					LOG.debug("Removed a permission collection for the user");
					break;
				}
			}
		}

		// remove all registration token entries
		RegistrationToken token = registrationTokenService.findByUser(userToDelete);
		if (token != null) {
			registrationTokenService.deleteTokenAfterActivation(token);
			LOG.debug("Deleted a RegistrationToken of a user");
		}

		// reown all layers of user
		final SimpleExpression isOwner = Restrictions.eq("owner", userToDelete);
		List<ProjectLayer> usersLayers = layerDao.findByCriteria(isOwner);
		for (ProjectLayer projectLayer : usersLayers) {
			projectLayer.setOwner(adminUser);
			LOG.info("A layer ownership has been moved to the superadmin");
		}

		// reown all applications of user
		List<ProjectApplication> usersApplications = applicationDao.findByCriteria(isOwner);
		for (ProjectApplication projectApp : usersApplications) {
			projectApp.setOwner(adminUser);
			LOG.info("An application ownership has been moved to the superadmin");
		}

		// reown all applications of user
		List<ProjectUserGroup> usersGroups = projectUserGroupDao.findByCriteria(isOwner);
		for (ProjectUserGroup projectGroup : usersGroups) {
			projectGroup.setOwner(adminUser);
			LOG.info("A group ownership has been moved to the superadmin");
		}

		dao.delete((E) userToDelete);
	}

	/**
	 * Sends mail to a subadmin of the group and, if not available, to the
	 * superadmin as fallback
	 *
	 * @param subadminForGroup
	 * @param superadmin
	 * @param wantedRole
	 * @param group
	 * @param user
	 */
	public void sendMailToSubadminOrSuperadmin(ProjectUser subadminForGroup, ProjectUser superadmin,
			String wantedRole, ProjectUserGroup group, ProjectUser user) {
		if (subadminForGroup != null) {
			//sendmail to subadmin
			sendPermissionChangeMail(subadminForGroup, wantedRole, group, user);
		} else if (superadmin != null) {
			//sendmail to superadmin
			sendPermissionChangeMail(superadmin, wantedRole, group, user);
		} else {
			throw new RuntimeException("Could neither find a subadmin, nor a superadmin!");
		}
	}

	/**
	 * Sends the final mail to the subadmin or superadmin with the request
	 * for changed permissions. Language of the receiver is taken into account
	 *
	 * @param receiver
	 * @param wantedRole
	 * @param group
	 * @param user
	 */
	public void sendPermissionChangeMail(ProjectUser receiver, String wantedRole,
			ProjectUserGroup group, ProjectUser user) {

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
					getChangePermissionsMailMessageTemplate_de()
			);
		} else {
			changePermissionMailTemplateMsg = new SimpleMailMessage(
					getChangePermissionsMailMessageTemplate_en()
			);
		}

		// Prepare a personalized mail in the correct language
		changePermissionMailTemplateMsg.setTo(email);
		changePermissionMailTemplateMsg.setText(
				String.format(
						changePermissionMailTemplateMsg.getText(),
						receiver.getFirstName(),
						receiver.getLastName(),
						group.getName(),
						wantedRole,
						user.getFirstName(),
						user.getLastName(),
						user.getEmail(),
						user.getDepartment(),
						user.getTelephone()
				)
		);
		// and send the mail
		mailPublisher.sendMail(changePermissionMailTemplateMsg);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("projectUserDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * @return the changePermissionsMailMessageTemplate_en
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplate_en() {
		return changePermissionsMailMessageTemplate_en;
	}

	/**
	 * @return the changePermissionsMailMessageTemplate_de
	 */
	public SimpleMailMessage getChangePermissionsMailMessageTemplate_de() {
		return changePermissionsMailMessageTemplate_de;
	}

	/**
	 * @return the projectRegistrationTokenService
	 */
	public ProjectRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> getProjectRegistrationTokenService() {
		return projectRegistrationTokenService;
	}

	/**
	 * @param projectRegistrationTokenService the projectRegistrationTokenService to set
	 */
	public void setProjectRegistrationTokenService(
			ProjectRegistrationTokenService<RegistrationToken, RegistrationTokenDao<RegistrationToken>> projectRegistrationTokenService) {
		this.projectRegistrationTokenService = projectRegistrationTokenService;
	}

}
