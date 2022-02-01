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
  public void userFound() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void incorrectPassword() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger,"username1" , "password345");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);

  }

  @Test
  public void nullUserInput() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);

    LoginService loginService = new LoginService(userDao, logger,"" , "");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);

  }

  @Test
  public void nullUsername() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger,"" , "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);

  }

  @Test
  public void nullPassword() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger,"username1" , "");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);

  }



}
