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

  // TODO: add more tests

  @Test
  public void noUsername() {
    EntityFactory.createUser()
            .withUsername("abikmal")
            .withPasswordToHash("anishPassword")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "", "anishPassword");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.INVALID_PARAMETER, message);
  }

  @Test
  public void noPassword() {
    EntityFactory.createUser()
            .withUsername("abikmal")
            .withPasswordToHash("anishPassword")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "abikmal", "");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.INVALID_PARAMETER, message);
  }

  @Test
  public void successfulLogin() {
    EntityFactory.createUser()
            .withUsername("abikmal")
            .withPasswordToHash("anishPassword")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "abikmal", "anishPassword");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_SUCCESS, message);
  }

  @Test
  public void badUsername() {
    EntityFactory.createUser()
            .withUsername("abikmal")
            .withPasswordToHash("anishPassword")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "abik", "anishPassword");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  @Test
  public void badPassword() {
    EntityFactory.createUser()
            .withUsername("abikmal")
            .withPasswordToHash("anishPassword")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "abikmal", "password");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }
}
