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

  // TODO: add more tests
  @Test
  public void userEmpty() {
      EntityFactory.createUser().buildAndPersist(userDao);
      GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "Taha");
      assertEquals(UserMessage.USER_NOT_FOUND, getUserInfoService.executeAndGetResponse());
  }

  @Test
  public void userFound() {
      EntityFactory.createUser().withUsername("Taha").withPasswordToHash("boty").buildAndPersist(userDao);
      GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "Taha");
      assertEquals(UserMessage.SUCCESS, getUserInfoService.executeAndGetResponse());
  }

    @Test
    public void userGetDetails() {
      String username = "tbot";
      String firstName = "Taha";
      String email = "taha@gmail";
        EntityFactory.createUser().withUsername(username).withFirstName(firstName).withPasswordToHash("Password").
                withEmail(email).buildAndPersist(userDao);
        GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "tbot");
        Message response = getUserInfoService.executeAndGetResponse();
        JSONObject user = getUserInfoService.getUserFields();
        assertEquals(email, user.getString("email"));
        assertEquals(username, user.getString("username"));
        assertEquals(firstName, user.getString("firstName"));
    }
}
