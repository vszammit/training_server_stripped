package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
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

  @Test
  public void userNotFound() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username2");
    assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void userFoundStatus() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "mohamed");
    assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void userFoundInfo() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withCity("Khartoum")
            .withEmail("fake@fake.com")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "mohamed");
    assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
    JSONObject res = getUserInfoService.getUserFields();
    assertEquals("fake@fake.com", res.get("email"));
    assertEquals("mohamed", res.get("username"));
    assertEquals("Khartoum", res.get("city"));
  }

  @Test
  public void invalidParams() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "~~~");
    assertEquals(UserMessage.INVALID_PARAMETER, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void nullParams() {
    EntityFactory.createUser()
            .withUsername("mohamed")
            .withPasswordToHash("12345678")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(UserMessage.INVALID_PARAMETER, getUserInfoService.executeAndGetResponse());
  }

}
