package User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
import User.Services.LoginService;
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

  // TODO: add more tests

  @Test
  public void userFound() {
    EntityFactory.createUser().withUsername("username1").withPasswordToHash("abcefghijk").buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void emptyUser() {
    EntityFactory.createUser().withUsername("username1").withPasswordToHash("abcefghijk").buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "");
    assertEquals(UserMessage.INVALID_PARAMETER, getUserInfoService.executeAndGetResponse());
  }

}
