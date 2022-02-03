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
  public void nullInput() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("pass")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, null, null);
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.INVALID_PARAMETER);
  }

  @Test
  public void invalidParameter() {
    EntityFactory.createUser()
        .withUsername("mohamed")
        .withPasswordToHash("pass")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "mohammed", "1234");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.INVALID_PARAMETER);
  }

  @Test
  public void userNotFound() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "mohammed", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void userFoundPasswordIncorrect() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "mohamed", "123456789");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void userFoundPasswordCorrect() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "mohamed", "12345678");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_SUCCESS);
  }

}
