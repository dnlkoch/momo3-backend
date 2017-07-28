package de.terrestris.appshogun.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import de.terrestris.shogun2.dao.RegistrationTokenDao;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.token.RegistrationToken;
import de.terrestris.shogun2.service.AbstractUserTokenService;
import de.terrestris.shogun2.util.application.Shogun2ContextUtil;
import de.terrestris.shogun2.util.mail.MailPublisher;

/**
 *
 * @author Daniel Koch
 * @author Nils Bühner
 *
 */
@Service("projectRegistrationTokenService")
public class ProjectRegistrationTokenService<E extends RegistrationToken, D extends RegistrationTokenDao<E>>
		extends AbstractUserTokenService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public ProjectRegistrationTokenService() {
		this((Class<E>) RegistrationToken.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected ProjectRegistrationTokenService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * The relative path for the SHOGun2 user activation interface.
	 */
	@Value("${login.accountActivationPath}")
	private String accountActivationPath;

	/**
	 * The registration token expiration time in minutes
	 */
	@Value("${login.registrationTokenExpirationTime}")
	private int registrationTokenExpirationTime;

	/**
	 *
	 */
	@Autowired
	private MailPublisher mailPublisher;

	/**
	 *
	 */
	@Autowired
	@Qualifier("registrationMailMessageTemplate-en")
	private SimpleMailMessage registrationMailMessageTemplateEnglish;

	/**
	 *
	 */
	@Autowired
	@Qualifier("registrationMailMessageTemplate-de")
	private SimpleMailMessage registrationMailMessageTemplateGerman;

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("registrationTokenDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	/**
	 * Builds a concrete instance of this class.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected E buildConcreteInstance(User user, Integer expirationTimeInMinutes) {
		if(expirationTimeInMinutes == null) {
			return (E) new RegistrationToken(user);
		}
		return (E) new RegistrationToken(user, expirationTimeInMinutes);
	}

	/**
	 *
	 * @param request
	 * @param user
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public void sendRegistrationActivationMail(HttpServletRequest request,
			User user) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			URISyntaxException, UnsupportedEncodingException {

		// create the reset-password URI that will be send to the user
		URI resetPasswordURI = createRegistrationActivationURI(request,
				user);

		String lang = "en";
		if (user.getLanguage() != null) {
			lang = user.getLanguage().toLanguageTag();
		}

		// create a thread safe "copy" of the template message
		SimpleMailMessage registrationActivationMsg = null;
		if (lang.equals("de")) {
			registrationActivationMsg = new SimpleMailMessage(
					getRegistrationMailMessageTemplateGerman()
			);
		} else {
			registrationActivationMsg = new SimpleMailMessage(
					getRegistrationMailMessageTemplateEnglish()
			);
		}

		// prepare a personalized mail with the given token
		final String email = user.getEmail();
		registrationActivationMsg.setTo(email);
		registrationActivationMsg.setText(
				String.format(
						registrationActivationMsg.getText(),
						UriUtils.decode(resetPasswordURI.toString(), "UTF-8")
				)
		);

		// and send the mail
		mailPublisher.sendMail(registrationActivationMsg);

	}

	/**
	 *
	 * @param registrationToken
	 * @param user
	 * @return
	 * @throws URISyntaxException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public URI createRegistrationActivationURI(HttpServletRequest request,
			User user) throws URISyntaxException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		// generate and save the unique registration token for the user
		RegistrationToken registrationToken = getValidTokenForUser(user,
				registrationTokenExpirationTime);

		// get the webapp URI
		URI appURI = Shogun2ContextUtil.getApplicationURIFromRequest(request);

		// build the registration activation link URI
		URI tokenURI = new URIBuilder(appURI)
				.setPath(appURI.getPath() + accountActivationPath)
				.setParameter("token", registrationToken.getToken())
				.build();

		LOG.trace("Created the following URI for account activation: " + tokenURI);

		return tokenURI;
	}

	/**
	 * This method has no {@link PreAuthorize} annotation and should only be
	 * used after an user account has been activated.
	 *
	 * @param token
	 */
	@SuppressWarnings("unchecked")
	public void deleteTokenAfterActivation(RegistrationToken token) {
		dao.delete((E) token);
		LOG.trace("The registration token has been deleted.");
	}

	/**
	 * @return the accountActivationPath
	 */
	public String getAccountActivationPath() {
		return accountActivationPath;
	}

	/**
	 * @param accountActivationPath the accountActivationPath to set
	 */
	public void setAccountActivationPath(String accountActivationPath) {
		this.accountActivationPath = accountActivationPath;
	}

	/**
	 * @return the registrationTokenExpirationTime
	 */
	public int getRegistrationTokenExpirationTime() {
		return registrationTokenExpirationTime;
	}

	/**
	 * @param registrationTokenExpirationTime the registrationTokenExpirationTime to set
	 */
	public void setRegistrationTokenExpirationTime(
			int registrationTokenExpirationTime) {
		this.registrationTokenExpirationTime = registrationTokenExpirationTime;
	}

	/**
	 * @return the mailPublisher
	 */
	public MailPublisher getMailPublisher() {
		return mailPublisher;
	}

	/**
	 * @param mailPublisher the mailPublisher to set
	 */
	public void setMailPublisher(MailPublisher mailPublisher) {
		this.mailPublisher = mailPublisher;
	}

	/**
	 * @return the registrationMailMessageTemplateEnglish
	 */
	public SimpleMailMessage getRegistrationMailMessageTemplateEnglish() {
		return registrationMailMessageTemplateEnglish;
	}

	/**
	 * @param registrationMailMessageTemplateEnglish the registrationMailMessageTemplateEnglish to set
	 */
	public void setRegistrationMailMessageTemplateEnglish(SimpleMailMessage registrationMailMessageTemplateEnglish) {
		this.registrationMailMessageTemplateEnglish = registrationMailMessageTemplateEnglish;
	}

	/**
	 * @return the registrationMailMessageTemplateGerman
	 */
	public SimpleMailMessage getRegistrationMailMessageTemplateGerman() {
		return registrationMailMessageTemplateGerman;
	}

	/**
	 * @param registrationMailMessageTemplateGerman the registrationMailMessageTemplateGerman to set
	 */
	public void setRegistrationMailMessageTemplateGerman(SimpleMailMessage registrationMailMessageTemplateGerman) {
		this.registrationMailMessageTemplateGerman = registrationMailMessageTemplateGerman;
	}


}
