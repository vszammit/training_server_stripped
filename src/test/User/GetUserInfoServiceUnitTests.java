package User;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
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

  @Test
  public void userNotFoundNPEThrown() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username2");
    getUserInfoService.executeAndGetResponse();
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              getUserInfoService.getUserFields();
            });
  }

  @Test
  public void userNull() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void userNullNPEThrown() {
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    getUserInfoService.executeAndGetResponse();
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              getUserInfoService.getUserFields();
            });
  }

  @Test
  public void happyPathInfoPopulated() {
    String username = "username1";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, username);
    getUserInfoService.executeAndGetResponse();
    assertEquals(getUserInfoService.getUserFields().get("username"), username);
  }

  @Test
  public void happyPath() {
    EntityFactory.createUser()
        .withUsername("username1")
        .withPasswordToHash("password123")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }
}
