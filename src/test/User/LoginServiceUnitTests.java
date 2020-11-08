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
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void loginValid(){
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username1", "password123");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void passwordInvalid(){
    EntityFactory.createUser()
            .withUsername("username2")
            .withPasswordToHash("a")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username2", "a");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }

  @Test
  public void passwordValid(){
    EntityFactory.createUser()
            .withUsername("username3")
            .withPasswordToHash("adsafhdskjfha")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username3", "adsafhdskjfha");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void usernameInvalid(){
    EntityFactory.createUser()
            .withUsername("username4")
            .withPasswordToHash("adsafhdskjfha")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "u!!@#$!", "adsafhdskjfha");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }
  @Test
  public void passwordWrong(){
    EntityFactory.createUser()
            .withUsername("username5")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    LoginService loginService = new LoginService(userDao, logger, "username5", "password321");
    Message message = loginService.executeAndGetResponse();
    assertEquals(message, UserMessage.AUTH_FAILURE);
  }
}
