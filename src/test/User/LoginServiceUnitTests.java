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
  public void emptyUser() {
      EntityFactory.createUser().withUsername("taha").withPasswordToHash("boty").buildAndPersist(userDao);
      LoginService loginService = new LoginService(userDao, logger, "", "boty");
      Message message = loginService.executeAndGetResponse();
      assertEquals(UserMessage.INVALID_PARAMETER, message);
  }

    @Test
    public void emptyPassword() {
        EntityFactory.createUser().withUsername("taha").withPasswordToHash("boty").buildAndPersist(userDao);
        LoginService loginService = new LoginService(userDao, logger, "taha", "");
        Message message = loginService.executeAndGetResponse();
        assertEquals(UserMessage.INVALID_PARAMETER, message);
    }

    @Test
    public void correctLogin() {
        EntityFactory.createUser().withUsername("taha").withPasswordToHash("boty12341").buildAndPersist(userDao);
        LoginService loginService = new LoginService(userDao, logger, "taha", "boty12341");
        Message message = loginService.executeAndGetResponse();
        assertEquals(UserMessage.AUTH_SUCCESS, message);
    }

    @Test
    public void incorrectPassword() {
        EntityFactory.createUser().withUsername("taha").withPasswordToHash("boty12341").buildAndPersist(userDao);
        LoginService loginService = new LoginService(userDao, logger, "taha", "boty1234");
        Message message = loginService.executeAndGetResponse();
        assertEquals(UserMessage.AUTH_FAILURE, message);
    }

    @Test
    public void incorrectLoginShortPassword() {
        EntityFactory.createUser().withUsername("taha").withPasswordToHash("boty").buildAndPersist(userDao);
        LoginService loginService = new LoginService(userDao, logger, "taha", "boty");
        Message message = loginService.executeAndGetResponse();
        assertEquals(UserMessage.AUTH_FAILURE, message);
    }

}
