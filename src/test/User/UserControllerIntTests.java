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
import org.junit.jupiter.api.Assertions;

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
  public void invalidUsername() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "#**");
    body.put("password", incorrectPassword);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void invalidPassword() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "#**");
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void userNotFound() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "username2");
    body.put("password", incorrectPassword);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void userFound() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
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
  public void userNameBlank() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
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
  public void userInfoNoLogin() {
    String username = "username2";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "username2");
    HttpResponse<String> getInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(getInfoResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void infoServiceUserFound() {
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

    JSONObject body2 = new JSONObject();
    body2.put("username", username);
    HttpResponse<String> infoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body2.toString()).asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("status")).isEqualTo("SUCCESS");
  }

  @Test
  public void infoServiceGetInfo() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName("Org")
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    JSONObject body2 = new JSONObject();
    body2.put("username", username);
    HttpResponse<String> infoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body2.toString()).asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("organization")).isEqualTo("Org");
  }

  @Test
  public void infoServiceWrongLogin() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName("Org")
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "pw");
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");

    JSONObject body2 = new JSONObject();
    body2.put("username", username);
    HttpResponse<String> infoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body2.toString()).asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("status")).isEqualTo("INVALID_PARAMETER");
  }

  @Test
  public void infoServiceGetInfo2() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName("Org")
            .withAddress("Add")
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    JSONObject body2 = new JSONObject();
    body2.put("username", username);
    HttpResponse<String> infoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body2.toString()).asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("organization")).isEqualTo("Org");
    assertThat(infoResponseJSON.getString("address")).isEqualTo("Add");
  }

}
