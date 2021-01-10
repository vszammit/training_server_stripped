package User;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import TestUtils.EntityFactory;
import TestUtils.TestUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerIntTests {
  UserDao userDao;

  @Before
  public void configureDatabase() {
    TestUtils.startServer();
    // NEVER USE DEPLOYMENT LEVEL HIGHER THAN TEST
    userDao = UserDaoFactory.create(DeploymentLevel.TEST);
  }

  @After
  public void reset() {
    userDao.clear();
  }

  @Test
  public void login_failure() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", incorrectPassword);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  // TODO: add more tests
  @Test
  public void userNotFound() {
    String username = "username";
    String incorrectUsername = "username_wrong";
    String password = "password";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);
    JSONObject body = new JSONObject();
    body.put("username", incorrectUsername);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void invalidUser() {
    String username = "username";
    String incorrectUsername = "///";
    String password = "password";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);
    JSONObject body = new JSONObject();
    body.put("username", incorrectUsername);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void invalidPassword() {
    String username = "username";
    String password = "password";
    String incorrectPassword = "///";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);
    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", incorrectPassword);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void successfulLogin() {
    String username = "username";
    String password = "password";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);
    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");
  }

  // GET USER INFO INTEGRATION TESTS

  @Test
  public void validUserInfo() {
    String username = "username";
    String password = "password";
    String firstName = "name";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .withFirstName(firstName)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResp = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());

    assertThat(loginResp.getString("status")).isEqualTo("AUTH_SUCCESS");
    assertThat(infoResponseJSON.getString("username")).isEqualTo(username);
    assertThat(infoResponseJSON.getString("firstName")).isEqualTo(firstName);
  }

  @Test
  public void userInfoFailedLogin() {
    String username = "username";
    String password = "password";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);
    JSONObject body = new JSONObject();
    body.put("username", "fake");
    HttpResponse<String> getInfoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(getInfoResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("ERROR");
  }
}
