package User;

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

import static org.junit.Assert.assertEquals;

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

  @Test
  public void userNotFound() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username2", "password2");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void nullUsername() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, null, "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void nullPassword() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", null);
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void notMatchingPassword() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "password1234");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void badPasswordFormat() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "p");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void happyPath() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_SUCCESS);
  }
}
