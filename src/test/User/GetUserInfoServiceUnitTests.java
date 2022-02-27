package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import User.Services.GetUserInfoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import Config.Message;
import TestUtils.EntityFactory;
import org.json.JSONObject;

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
  public void noUsername() {
    EntityFactory.createUser().buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "abikmal");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void userSuccess() {
    EntityFactory.createUser().withUsername("abikmal").withPasswordToHash("anishPassword").buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "abikmal");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void userGetDetails() {
    EntityFactory.createUser().withUsername("abikmal").withFirstName("Anish").withPasswordToHash("anishPassword").
            withEmail("anish@gmail.co").buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "tbot");
    Message response = getUserInfoService.executeAndGetResponse();
    JSONObject user = getUserInfoService.getUserFields();
    assertEquals("anish@gmail.com", user.getString("email"));
    assertEquals("abikmal", user.getString("username"));
    assertEquals("Anish", user.getString("firstName"));
  }
}
