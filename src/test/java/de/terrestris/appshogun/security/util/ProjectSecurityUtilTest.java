package de.terrestris.appshogun.security.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

/**
 *
 * terrestris GmbH & Co. KG
 *
 * @author Andre Henn
 * @date 13.04.2017
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/test-user-group-roles.xml" })
public class ProjectSecurityUtilTest {

	@Value("${role.defaultUserRoleName:}")
	private String defaultUserRoleName;

	@Value("${role.editorRoleName:}")
	private String editorRoleName;

	@Value("${role.subAdminRoleName:}")
	private String subAdminRoleName;

	@Value("${role.superAdminRoleName:}")
	private String superAdminRoleName;

	private ProjectUser testUser;

	@Before
	public void setupTest() throws NoSuchFieldException, IllegalAccessException {
		ProjectConfigHolder projectConfigHolder = Mockito.mock(ProjectConfigHolder.class);
		when(projectConfigHolder.getDefaultUserRoleName()).thenReturn(defaultUserRoleName);
		when(projectConfigHolder.getEditorRoleName()).thenReturn(editorRoleName);
		when(projectConfigHolder.getSubAdminRoleName()).thenReturn(subAdminRoleName);
		when(projectConfigHolder.getSuperAdminRoleName()).thenReturn(superAdminRoleName);

		ProjectSecurityUtil.configHolder = projectConfigHolder;

		testUser = new ProjectUser();
		testUser = new ProjectUser();
		testUser.setAccountName("Manta");
		IdHelper.setIdOnPersistentObject(testUser, 1909);
	}

	/**
	 *
	 */
	@After
	public void logoutMockUser() {
		SecurityContextHolder.clearContext();
	}

	/**
	 *
	 * @param userRoles
	 */
	public void loginMockUser(Set<Role> userRoles) {
		Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();

		for (Role userRole : userRoles) {
			grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getName()));
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(testUser, "",
				grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	public void currentUserIsSuperAdmin_shouldReturnTrue() {
		// prepare a user that
		final Role userRole = new Role(superAdminRoleName);
		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		loginMockUser(rolesOfUser);

		assertTrue(ProjectSecurityUtil.currentUserIsSuperAdmin());
	}

	@Test
	public void currentUserIsSuperAdmin_shouldReturnFalse() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);
		final Role subAdminRole = new Role(subAdminRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		rolesOfUser.add(editorRole);
		rolesOfUser.add(subAdminRole);

		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUserIsSuperAdmin());
	}

	@Test
	public void currentUserIsSubAdmin_shouldReturnTrue() {
		// prepare a user that
		final Role subAdminRole = new Role(subAdminRoleName);
		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(subAdminRole);

		loginMockUser(rolesOfUser);

		assertTrue(ProjectSecurityUtil.currentUserHasRoleSubAdmin());
	}

	@Test
	public void currentUserIsSubAdmin_shouldReturnFalse() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		rolesOfUser.add(editorRole);

		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUserHasRoleSubAdmin());
	}

	@Test
	public void currentUserIsEditor_shouldReturnTrue() {
		// prepare a user that
		final Role editorRole = new Role(editorRoleName);
		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(editorRole);

		loginMockUser(rolesOfUser);

		assertTrue(ProjectSecurityUtil.currentUsersHighestRoleIsEditor());
	}

	@Test
	public void currentUserIsEditor_shouldReturnFalse() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUsersHighestRoleIsEditor());
	}

	@Test
	public void currentUserIsDefaultUser_shouldReturnTrue() {
		// prepare a user that
		final Role defaultUserRole = new Role(defaultUserRoleName);
		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(defaultUserRole);

		loginMockUser(rolesOfUser);

		assertTrue(ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser());
	}

	@Test
	public void currentUserIsDefaultUser_shouldReturnFalse() {
		HashSet<Role> rolesOfUser = new HashSet<Role>();
		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser());
	}

	@Test
	public void currentUserIsEditorUserInSingleGroup_ShouldReturnFalse() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);
		final Role subAdminRole = new Role(subAdminRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		rolesOfUser.add(editorRole);
		rolesOfUser.add(subAdminRole);

		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUsersHighestRoleIsEditor());
	}

	@Test
	public void currentUserIsEditorUserInSingleGroup_ShouldReturnTrue() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		rolesOfUser.add(editorRole);

		loginMockUser(rolesOfUser);

		assertTrue(ProjectSecurityUtil.currentUsersHighestRoleIsEditor());
	}

	@Test
	public void currentUserIsDefaultUserInSingleGroup_ShouldReturnFalse() {
		// prepare a user that
		final Role userRole = new Role(defaultUserRoleName);
		final Role editorRole = new Role(editorRoleName);

		HashSet<Role> rolesOfUser = new HashSet<Role>();
		rolesOfUser.add(userRole);
		rolesOfUser.add(editorRole);

		loginMockUser(rolesOfUser);

		assertFalse(ProjectSecurityUtil.currentUsersHighestRoleIsDefaultUser());
	}

}
