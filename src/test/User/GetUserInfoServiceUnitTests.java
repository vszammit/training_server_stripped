package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Config.Message;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
import User.Services.LoginService;
import com.mongodb.util.JSON;
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
  public void userSuccessfullyAuthenticated() {
    EntityFactory.createUser()
            .withUsername("username1")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    Message response = getUserInfoService.executeAndGetResponse();
    assertEquals(response, UserMessage.SUCCESS);
  }

  @Test
  public void userNameEmpty() {
    EntityFactory.createUser()
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "");

    // finds the current user in the system if user exists
    Message response = getUserInfoService.executeAndGetResponse();

    assertEquals(response, UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void getUserFirstName() {
    String firstName = "Jason";
    String lastName = "Zhang";
    String email = "zhang98@bu.edu";
    EntityFactory.createUser()
            .withUsername("username1")
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(email)
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");

    // finds the current user in the system if user exists
    Message response = getUserInfoService.executeAndGetResponse();

    // once we confirm user exists in db, we get their fields
    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.getString("firstName"), firstName);
  }

  @Test
  public void getUserEmail() {
    String firstName = "Jason";
    String lastName = "Zhang";
    String email = "zhang98@bu.edu";
    EntityFactory.createUser()
            .withUsername("username1")
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(email)
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");

    // finds the current user in the system if user exists
    Message response = getUserInfoService.executeAndGetResponse();

    // once we confirm user exists in db, we get their fields
    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.getString("email"), email);
  }

  @Test
  public void getUserLastName() {
    EntityFactory.createUser()
            .withUsername("user123")
            .withLastName("Zhang")
            .buildAndPersist(userDao);
    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "user123");

    // finds the current user in the system if user exists
    Message response = getUserInfoService.executeAndGetResponse();

    // once we confirm user exists in db, we get their fields
    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.getString("lastName"), "Zhang");
  }
}
