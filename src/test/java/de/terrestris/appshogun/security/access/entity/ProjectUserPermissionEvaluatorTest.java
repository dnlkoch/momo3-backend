package de.terrestris.appshogun.security.access.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.terrestris.appshogun.model.ProjectUser;
import de.terrestris.appshogun.util.config.ProjectConfigHolder;
import de.terrestris.appshogun.util.security.ProjectSecurityUtil;
import de.terrestris.shogun2.helper.IdHelper;
import de.terrestris.shogun2.model.Role;
import de.terrestris.shogun2.model.security.Permission;

/**
 *
 * terrestris GmbH & Co. KG
 * @author Andre Henn
 * @date 18.04.2017
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/test-user-group-roles.xml"
})
public class ProjectUserPermissionEvaluatorTest {

	@InjectMocks
	private ProjectUserPermissionEvaluator<ProjectUser> projectUserPermissionEvaluator;

	@Value("${role.defaultUserRoleName:}")
	private String defaultUserRoleName;

	@Value("${role.editorRoleName:}")
	private String editorRoleName;

	@Value("${role.subAdminRoleName:}")
	private String subAdminRoleName;

	@Value("${role.superAdminRoleName:}")
	private String superAdminRoleName;

	/**
	 *
	 */
	private ProjectUser accessUser;

	/**
	 *
	 */
	private ProjectUser testUser;

	@Before
	public void set_up() throws NoSuchFieldException, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		projectUserPermissionEvaluator = new ProjectUserPermissionEvaluator<ProjectUser>();

		ProjectConfigHolder projectConfigHolder = Mockito.mock(ProjectConfigHolder.class);
		when(projectConfigHolder.getDefaultUserRoleName()).thenReturn(defaultUserRoleName);
		when(projectConfigHolder.getEditorRoleName()).thenReturn(editorRoleName);
		when(projectConfigHolder.getSubAdminRoleName()).thenReturn(subAdminRoleName);
		when(projectConfigHolder.getSuperAdminRoleName()).thenReturn(superAdminRoleName);
		ProjectSecurityUtil.configHolder = projectConfigHolder;
		projectUserPermissionEvaluator.setProjectConfigHolder(projectConfigHolder);

		accessUser = new ProjectUser();
		accessUser.setAccountName("Manta");
		IdHelper.setIdOnPersistentObject(accessUser, 1909);

		testUser = new ProjectUser();
		testUser.setAccountName("TestUserToPerformCRUD");
		IdHelper.setIdOnPersistentObject(testUser, 190909);
	}

	@After
	public void clean_up() {
		logoutMockUser();
	}

	/**
	 *
	 * @param userRoles
	 */
	private void loginMockUser(Set<Role> userRoles) {
		Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

		for (Role userRole : userRoles) {
			grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getName()));
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				accessUser, "", grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 *
	 */
	private void logoutMockUser() {
		SecurityContextHolder.clearContext();
	}

	/**
	 * All users, whatever their roles are, don't have the right to
	 * - CREATE new users
	 * - DELETE other users
	 * - UPDATE other users
	 */
	@Test
	public void hasPermission_shouldAlwaysDenyUpdateAndDeleteForOtherUsers() {
		Permission[] permissionsToCheck = new Permission[] {
			Permission.UPDATE,
			Permission.DELETE,
			Permission.CREATE
		};

		final Role defaultUserRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);
		final Role subAdminRole = new Role(subAdminRoleName);
		final Role superAdminRole = new Role(superAdminRoleName);
		ArrayList<Role> userRolesToCheck = new ArrayList<Role>();
		userRolesToCheck.add(defaultUserRole);
		userRolesToCheck.add(editorRole);
		userRolesToCheck.add(subAdminRole);
		userRolesToCheck.add(superAdminRole);

		for (Role role : userRolesToCheck) {
			Set<Role> currentUserRoleSet = new HashSet<Role>();
			currentUserRoleSet.add(role);

			loginMockUser(currentUserRoleSet);

			for (Permission permissionToCheck : permissionsToCheck) {
				boolean permissionResult = projectUserPermissionEvaluator.hasPermission(accessUser, testUser, permissionToCheck);
				assertFalse("Current ROLE: " + role.getName() + " should NOT have " + permissionToCheck.name() + " permission!", permissionResult);
			}

			logoutMockUser();
		}
	}

	/**
	 * User with role DEFAULT_USER (only!) should not be allowed to have any access to other users
	 *
	 */
	@Test
	public void hasPermission_shouldAlwaysDenyCRUDForDefaultUserIfNotHimself() {
		Permission[] permissions = new Permission[] {
			Permission.CREATE,
			Permission.READ,
			Permission.UPDATE,
			Permission.DELETE
		};

		final Role defaultUserRole = new Role(defaultUserRoleName);

		Set<Role> userRoles = new HashSet<Role>();
		userRoles.add(defaultUserRole);

		loginMockUser(userRoles);

		for (Permission permission : permissions) {
			boolean permissionResult = projectUserPermissionEvaluator.hasPermission(accessUser, testUser, permission);
			assertFalse("Current ROLE: " + defaultUserRole.getName() + " should NOT have " + permission.name() + " permission!", permissionResult);
		}
	}

	/**
	 * User should always be allowed to
	 *  - UPDATE
	 *  - READ
	 *  - DELETE
	 * for his own entity
	 */
	@Test
	public void hasPermission_shouldAlwaysAllowReadUpdateDeleteForHimself() {
		Permission[] permissions = new Permission[] {
			Permission.UPDATE,
			Permission.READ,
			Permission.DELETE
		};

		final Role defaultUserRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);
		final Role subAdminRole = new Role(subAdminRoleName);
		final Role superAdminRole = new Role(superAdminRoleName);
		ArrayList<Role> userRolesToCheck = new ArrayList<Role>();
		userRolesToCheck.add(defaultUserRole);
		userRolesToCheck.add(editorRole);
		userRolesToCheck.add(subAdminRole);
		userRolesToCheck.add(superAdminRole);

		for (Role role : userRolesToCheck) {
			Set<Role> currentUserRoleSet = new HashSet<Role>();
			currentUserRoleSet.add(role);

			loginMockUser(currentUserRoleSet);

			for (Permission permissionToCheck : permissions) {
				// check if user is allowed to edit / read his own entity
				boolean permissionResult = projectUserPermissionEvaluator.hasPermission(accessUser, accessUser, permissionToCheck);
				assertTrue("Current ROLE: " + role.getName() + " should have " + permissionToCheck.name() + " permission!", permissionResult);
			}

			logoutMockUser();
		}
	}

	@Test
	public void hasPermission_shouldAlwaysDenyCreateForHimself() {
		Permission createPermission = Permission.CREATE;

		final Role defaultUserRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);
		final Role subAdminRole = new Role(subAdminRoleName);
		final Role superAdminRole = new Role(superAdminRoleName);
		ArrayList<Role> userRolesToCheck = new ArrayList<Role>();
		userRolesToCheck.add(defaultUserRole);
		userRolesToCheck.add(editorRole);
		userRolesToCheck.add(subAdminRole);
		userRolesToCheck.add(superAdminRole);


		for (Role role : userRolesToCheck) {
			Set<Role> currentUserRoleSet = new HashSet<Role>();
			currentUserRoleSet.add(role);

			loginMockUser(currentUserRoleSet);

			// check if user is allowed to edit / read his own entity
			boolean permissionResult = projectUserPermissionEvaluator.hasPermission(accessUser, accessUser, createPermission);
			assertFalse("Current ROLE: " + role.getName() + " should NOT have " + createPermission.name() + " permission!", permissionResult);

			logoutMockUser();
		}
	}

}
