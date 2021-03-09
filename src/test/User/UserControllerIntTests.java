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

  // GetUserInfoService tests
  @Test
  public void user_info_success() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject loginRequest = new JSONObject();
    loginRequest.put("username", username);
    loginRequest.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(loginRequest.toString()).asString();

    JSONObject userInfoRequest = new JSONObject();
    userInfoRequest.put("username", username);
    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(userInfoRequest.toString()).asString();
    JSONObject userInfoResponseJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoResponseJSON.getString("status")).isEqualTo("SUCCESS");
  }

  @Test
  public void user_info_valid_content() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withCity("Chicago")
            .buildAndPersist(userDao);

    JSONObject loginRequest = new JSONObject();
    loginRequest.put("username", username);
    loginRequest.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(loginRequest.toString()).asString();

    JSONObject userInfoRequest = new JSONObject();
    userInfoRequest.put("username", username);
    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(userInfoRequest.toString()).asString();
    JSONObject userInfoResponseJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoResponseJSON.getString("city")).isEqualTo("Chicago");
  }

  @Test
  public void user_info_user_not_found() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject loginRequest = new JSONObject();
    loginRequest.put("username", username);
    loginRequest.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(loginRequest.toString()).asString();

    JSONObject userInfoRequest = new JSONObject();
    userInfoRequest.put("username", "username123");
    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(userInfoRequest.toString()).asString();
    JSONObject userInfoResponseJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void user_info_invalid_username() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject loginRequest = new JSONObject();
    loginRequest.put("username", username);
    loginRequest.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(loginRequest.toString()).asString();

    JSONObject userInfoRequest = new JSONObject();
    userInfoRequest.put("username", "username%");
    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(userInfoRequest.toString()).asString();
    JSONObject userInfoResponseJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void user_info_blank_username() {
    String username = "username1";
    String password = "password123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject loginRequest = new JSONObject();
    loginRequest.put("username", username);
    loginRequest.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(loginRequest.toString()).asString();

    JSONObject userInfoRequest = new JSONObject();
    userInfoRequest.put("username", "  ");
    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(userInfoRequest.toString()).asString();
    JSONObject userInfoResponseJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  // LoginService tests
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
    String correctPassword = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", correctPassword);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");
  }

  @Test
  public void login_user_not_found() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username1234";
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
  public void login_blank_username() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", " ");
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void login_invalid_username() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "username13%");
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void login_blank_password() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "  ");
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void login_invalid_password() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "pass");
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }
}
