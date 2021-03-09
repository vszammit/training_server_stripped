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
  public void success() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password1234")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void nullUsername() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void blankUsername() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "    ");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void invalidUsername() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username%");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void validContent() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password1234")
            .withCity("Chicago")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    Message message = getUserInfoService.executeAndGetResponse();
    JSONObject info = getUserInfoService.getUserFields();
    assertEquals(info.getString("city"), "Chicago");
  }
}
