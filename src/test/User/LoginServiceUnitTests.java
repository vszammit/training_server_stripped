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
        .withUsername("newusername")
        .withPasswordToHash("password1234")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "otherusername", "otherpassword");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.USER_NOT_FOUND);
  }

  // TODO: add more tests

  @Test
  public void emptyUser() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "", "123456789");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  @Test
  public void emptyPassword() {
    EntityFactory.createUser()
        .withUsername("victoriazammmit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "victoriazammit", "");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  @Test
  public void correctLogin() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "victoriazammit", "123456789");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_SUCCESS, message);
  }

  @Test
  public void incorrectPassword() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "victoriazammit", "12345666");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }

  @Test
  public void incorrectLoginShortPassword() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123")
        .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "victoriazammit", "123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(UserMessage.AUTH_FAILURE, message);
  }
}
