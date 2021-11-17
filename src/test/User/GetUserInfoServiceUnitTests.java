package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import Logger.LogFactory;
import TestUtils.EntityFactory;
import User.Services.GetUserInfoService;
import org.json.JSONException;
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
  public void userSuccessfullyFound() {
    EntityFactory.createUser()
            .withUsername("username1")
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);
  }

  @Test
  public void getUserEmail() {
    String email = "username1@email.com";
    EntityFactory.createUser()
            .withUsername("username1")
            .withEmail(email)
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);

    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.get("email"), email);
  }

  @Test
  public void getUserFirstName() {
    String firstName = "Tirtha";
    EntityFactory.createUser()
            .withUsername("username1")
            .withFirstName(firstName)
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);

    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.get("firstName"), firstName);
  }

  @Test
  public void getUserLastName() {
    String lastName = "Kharel";
    EntityFactory.createUser()
            .withUsername("username1")
            .withLastName(lastName)
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);

    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    assertEquals(userFieldsObject.get("lastName"), lastName);
  }

  @Test(expected = JSONException.class)
  public void getNonExistingKey() {
    EntityFactory.createUser()
            .withUsername("username1")
            .buildAndPersist(userDao);

    GetUserInfoService getUserInfoService = new GetUserInfoService(userDao, logger, "username1");
    assertEquals(getUserInfoService.executeAndGetResponse(), UserMessage.SUCCESS);

    JSONObject userFieldsObject = getUserInfoService.getUserFields();
    userFieldsObject.get("nonExistingKey");
  }
}
