package User;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
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
  public void userFoundStatus() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService =
        new GetUserInfoService(userDao, logger, "victoriazammit");
    assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void invalidParams() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "~~~");
    assertEquals(UserMessage.INVALID_PARAMETER, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void nullParams() {
    EntityFactory.createUser()
        .withUsername("victoriazammit")
        .withPasswordToHash("123456789")
        .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, null);
    assertEquals(UserMessage.INVALID_PARAMETER, getUserInfoService.executeAndGetResponse());
  }

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
        .withUsername("newusername")
        .withPasswordToHash("password1234")
        .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "newusername");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void userFoundInfo() {
    EntityFactory.createUser()
        .withUsername("newusername")
        .withPasswordToHash("password1234")
        .withOrgName("Hack4Impact")
        .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "newusername");
    Message infoServiceMessage = getUserInfoService.executeAndGetResponse();
    assertEquals(getUserInfoService.getUserFields().getString("username"), "newusername");
    assertEquals(getUserInfoService.getUserFields().getString("organization"), "Hack4Impact");
  }
}
