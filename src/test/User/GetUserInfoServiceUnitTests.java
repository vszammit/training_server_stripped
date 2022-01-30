package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import TestUtils.TestUtils;
import User.Services.GetUserInfoService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
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

  // TODO: add more tests

  @Test
  public void usernameInvalid() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "#**");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.INVALID_PARAMETER);
  }

  @Test
  public void usernameNull() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.INVALID_PARAMETER);
  }

  @Test
  public void userFound() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void userFoundInfo() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .withOrgName("Urban League")
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    Message infoServiceMessage = getUserInfoService.executeAndGetResponse();
    assertEquals(getUserInfoService.getUserFields().getString("username"), "username1");
    assertEquals(getUserInfoService.getUserFields().getString("organization"), "Urban League");
  }
}
