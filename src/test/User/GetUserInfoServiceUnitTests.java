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
  public void userFound() {
    EntityFactory.createUser()
            .withUsername("username1")
            .buildAndPersist(userDao);
    GetUserInfoService infoService = new GetUserInfoService(userDao, logger, "username1");
    Message message = infoService.executeAndGetResponse();
    assertEquals(message, UserMessage.SUCCESS);
  }

  @Test
  public void nullUsername() {
    EntityFactory.createUser()
            .withUsername("username1")
            .buildAndPersist(userDao);
    GetUserInfoService infoService = new GetUserInfoService(userDao, logger, "");
    Message message = infoService.executeAndGetResponse();
    assertEquals(message, UserMessage.USER_NOT_FOUND);
  }

  @Test
  public void getFirstName() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withFirstName("John")
            .buildAndPersist(userDao);
    GetUserInfoService infoService = new GetUserInfoService(userDao, logger, "username1");
    Message message = infoService.executeAndGetResponse();
    JSONObject userFieldsObject = infoService.getUserFields();
    assertEquals(userFieldsObject.getString("firstName"), "John");
  }

  @Test
  public void getLastName() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withLastName("Smith")
            .buildAndPersist(userDao);
    GetUserInfoService infoService = new GetUserInfoService(userDao, logger, "username1");
    Message message = infoService.executeAndGetResponse();
    JSONObject userFieldsObject = infoService.getUserFields();
    assertEquals(userFieldsObject.getString("lastName"), "Smith");
  }

  @Test
  public void getEmail() {
    EntityFactory.createUser()
            .withUsername("username1")
            .withEmail("johnsmith@email.com")
            .buildAndPersist(userDao);
    GetUserInfoService infoService = new GetUserInfoService(userDao, logger, "username1");
    Message message = infoService.executeAndGetResponse();
    JSONObject userFieldsObject = infoService.getUserFields();
    assertEquals(userFieldsObject.getString("email"), "johnsmith@email.com");
  }




}
