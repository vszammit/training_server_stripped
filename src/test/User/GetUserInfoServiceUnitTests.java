package User;

import static org.junit.Assert.assertEquals;

import Config.DeploymentLevel;
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
  public void userFound(){
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    GetUserInfoService s = new GetUserInfoService(userDao,logger,"username1");
    assertEquals(s.executeAndGetResponse(),UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void allUserFields(){
    EntityFactory.createUser()
            .withUsername("username1")
            .withPasswordToHash("password123")
            .buildAndPersist(userDao);
    GetUserInfoService s = new GetUserInfoService(userDao,logger,"username1");
    s.executeAndGetResponse();
    JSONObject sInfo = s.getUserFields();
    logger.info(sInfo.toString());
    assertEquals(s.executeAndGetResponse(),UserMessage.AUTH_SUCCESS);
  }

  @Test
  public void incorrectInfo(){
    String username = "username5";
    String password = "password5";
    String phone = "555-555-5555";
    String email = "555@gmail.com";
    String address = "555 fifth street";
    String lastname = "the fifth";
  EntityFactory.createUser()
    .withUsername(username)
    .withPasswordToHash(password)
    .withPhoneNumber(phone)
          .withAddress(address)
          .withEmail(email)
          .buildAndPersist(userDao);
  GetUserInfoService s = new GetUserInfoService(userDao, logger, "username5");
  s.executeAndGetResponse();
  JSONObject sInfo = s.getUserFields();
  assertEquals((sInfo.getString("phone")),phone);
  }
}
