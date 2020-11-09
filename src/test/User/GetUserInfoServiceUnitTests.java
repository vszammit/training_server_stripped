package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Config.Message;
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
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void userFound() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void userInfoInvalidUsername() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.AUTH_FAILURE);
  }

  @Test
  public void testUserInfo() {
    String username = "username1";
    String password = "password123";
    String organization = "keep";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName(organization)
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    Message status = getUserInfoService.executeAndGetResponse();

    assertEquals(getUserInfoService.getUserFields().getString("username"), username);
    assertEquals(getUserInfoService.getUserFields().getString("organization"), organization);
  }

}
