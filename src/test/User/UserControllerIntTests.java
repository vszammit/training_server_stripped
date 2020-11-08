package User;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  public void login_success() {
    String username = "username1";
    String password = "password1234";
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

  @Test
  public void empty_username() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "");
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }
  @Test
  public void user_not_found() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "username123");
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void invalid_password() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "a");
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }
  @Test
  public void invalid_username() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "!#&@^$%");
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  // Should fail since ctx.sessionattribute("username") is empty without logging in
  @Test
  public void userInfoInvalidUsername() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "username9");
    HttpResponse<String> getInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(getInfoResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void userInfoGetInfo() {
    String username = "username1";
    String password = "password1234";
    String org = "Keep";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName(org)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());

    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");
    assertThat(infoResponseJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(infoResponseJSON.getString("username")).isEqualTo(username);
    assertThat(infoResponseJSON.getString("organization")).isEqualTo(org);
  }
}
