package User;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import TestUtils.EntityFactory;
import TestUtils.TestUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
    TestUtils.logout();
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
  public void login_null_username() {
    String username = "username1";
    String password = "password1234";
    String nullUsername = null;
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", nullUsername);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    assertThatExceptionOfType(JSONException.class)
        .isThrownBy(
            () -> {
              JSONObject loginResponseJSON =
                  TestUtils.responseStringToJSON(loginResponse.getBody());
            });
  }

  @Test
  public void login_null_password() {
    String username = "username1";
    String password = "password1234";
    String nullPassword = null;
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", nullPassword);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    assertThatExceptionOfType(JSONException.class)
        .isThrownBy(
            () -> {
              JSONObject loginResponseJSON =
                  TestUtils.responseStringToJSON(loginResponse.getBody());
            });
  }

  @Test
  public void login_incorrect_username() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username2";
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
  public void login_bad_password_format() {
    String username = "username1";
    String password = "password1234";
    String badPassword = "p";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", badPassword);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void login_happy_path() {
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
  public void get_info_user_not_logged_in() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    HttpResponse<String> infoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void get_info_bad_login_username() {
    String username = "username1";
    String password = "password1234";
    String badUsername = "username2";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", badUsername);
    body.put("password", password);

    // To set session
    Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void get_info_bad_login_password() {
    String username = "username1";
    String password = "password1234";
    String badPassword = "password4321";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", badPassword);

    // To set session
    Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void get_info_happy_path() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    // To set session
    Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    HttpResponse<String> infoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();

    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(infoResponseJSON.getString("username")).isEqualTo(username);
    assertThat(infoResponseJSON.getString("address")).isEqualTo("123 Test St Av");
    // ...and so on
  }

  @Test
  public void get_info_happy_path_multiple_users_in_db() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    // Populate database with a bunch of users
    String usernameBase = "username12";
    String passwordBase = "password12345";
    for (int i = 0; i < 10; i++) {
      EntityFactory.createUser()
          .withUsername(usernameBase.concat(String.valueOf(i)))
          .withPasswordToHash(passwordBase.concat(String.valueOf(i)))
          .buildAndPersist(userDao);
    }

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    // To set session
    Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    HttpResponse<String> infoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();

    JSONObject infoResponseJSON = TestUtils.responseStringToJSON(infoResponse.getBody());
    assertThat(infoResponseJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(infoResponseJSON.getString("username")).isEqualTo(username);
    assertThat(infoResponseJSON.getString("address")).isEqualTo("123 Test St Av");
    // ...and so on
  }
}
