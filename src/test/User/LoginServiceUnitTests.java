package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.LoginService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class LoginServiceUnitTests {
  public UserDao userDao;
  public Logger logger;

  @Before
  public void initialize() {
    this.userDao = UserDaoFactory.create(DeploymentLevel.IN_MEMORY);
    this.logger = new LogFactory().createLogger();
  }

  @After
  public void reset() {
    userDao.clear();
  }

  /**
   * Test LoginService success case
   */
  @Test
  public void success() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    LoginService loginService = new LoginService(userDao, logger, username, password);
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_SUCCESS, message);
  }

  /**
   * Test LoginService failure case with incorrect username
   */
  @Test
  public void incorrect_username() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username2", "password2");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.USER_NOT_FOUND, message);
  }

  /**
   * Test LoginService failure case with incorrect password
   */
  @Test
  public void incorrect_password() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "wrongpassword");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  /**
   * Test LoginService failure case with null username
   */
  @Test
  public void null_username () {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, null, "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  /**
   * Test LoginService failure case with null password
   */
  @Test
  public void null_password () {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", null);
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  /**
   * Test LoginService failure case with username with trailing whitespace
   */
  @Test
  public void trailing_whitespace_username() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1   ", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  /**
   * Test LoginService failure case with username with leading whitespace
   */
  @Test
  public void leading_whitespace_username() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "   username1", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }
}
