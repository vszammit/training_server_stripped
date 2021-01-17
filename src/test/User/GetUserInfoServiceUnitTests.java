package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
import User.Services.LoginService;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class GetUserInfoServiceUnitTests {
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
   * Test GetUserInfoService success case
   */
  @Test
  public void success() {
    User user = EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
    assertEquals(GetUserInfoService.getFieldsFromUser(user).toString(), getUserInfoService.getUserFields().toString());
  }

  /**
   * Test GetUserInfo failure case with an incorrect username
   */
  @Test
  public void incorrect_username() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username2");
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  /**
   * Test GetUserInfo failure case with a null username
   */
  @Test
  public void null_username () {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  /**
   * Test GetUserInfo failure case with an undefined username
   */
  @Test
  public void empty_string_username () {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "");
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  /**
   * Test GetUserInfo failure case with a username with trailing whitespace
   */
  @Test
  public void username_with_trailing_whitespace() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1      ");
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  /**
   * Test GetUserInfo failure case with a username with trailing whitespace
   */
  @Test
  public void username_with_leading_whitespace() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "     username1");
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }
}
