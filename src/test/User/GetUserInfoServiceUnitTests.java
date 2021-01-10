package User;

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

import static org.junit.Assert.assertEquals;

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
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  // TODO: add more tests
  @Test
  public void invalidUsername() {
    EntityFactory.createUser()
        .withUsername("username")
        .withPasswordToHash("password")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "///");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void userFound() {
    EntityFactory.createUser()
        .withUsername("username")
        .withPasswordToHash("password")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void getUserInfo() {
    EntityFactory.createUser()
        .withUsername("username")
        .withPasswordToHash("password")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username");
    getUserInfoService.executeAndGetResponse();
    JSONObject sInfo = getUserInfoService.getUserFields();
    logger.info(sInfo.toString());
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void checkInfo() {
    String firstName = "test";
    EntityFactory.createUser()
        .withUsername("username")
        .withPasswordToHash("password")
        .withFirstName(firstName)
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username");
    getUserInfoService.executeAndGetResponse();
    JSONObject sInfo = getUserInfoService.getUserFields();
    assertEquals(sInfo.get("firstName"), firstName);
  }
}
