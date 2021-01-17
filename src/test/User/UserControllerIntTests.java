package User;

import static org.assertj.core.api.Assertions.assertThat;

import Config.DeploymentLevel;
import Database.UserDao;
import Database.UserDaoFactory;
import TestUtils.EntityFactory;
import TestUtils.TestUtils;
import User.Services.GetUserInfoService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

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

  /**
   * Test login success case
   */
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

  /**
   * Test login failure case when incorrect password provided in JSON request body
   */
  @Test
  public void login_failure_incorrect_password() {
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

  /**
   * Test login failure case when no password provided in JSON request body
   */
  @Test
  public void login_failure_no_password_provided() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  /**
   * Test login failure case when empty-string password provided in JSON request body
   */
  @Test
  public void login_failure_empty_password_provided() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", "");
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  /**
   * Test login failure case when incorrect username provided in JSON request body
   */
  @Test
  public void login_failure_invalid_username_provided() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "not-a-real-username");
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  /**
   * Test login failure case when no JSON request body provided
   */
  @Test
  public void login_failure_no_request_body_provided() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  /**
   * Test get-user-info success case
   */
  @Test
  public void get_user_info_success() {
    String username = "username1";
    String password = "password1234";
    User user = EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    // auth via API to fetch session cookie
    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    HttpResponse<String> getUserInfoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").cookie(loginResponse.getCookies()).asString();
    JSONObject getUserInfoResponseJSON = TestUtils.responseStringToJSON(getUserInfoResponse.getBody());
    assertThat(getUserInfoResponseJSON.getString("status")).isEqualTo("SUCCESS");

    JSONObject expectedUserResponse = GetUserInfoService.getFieldsFromUser(user);
    for (Iterator<String> keys = expectedUserResponse.keys(); keys.hasNext(); ) {
      String key = keys.next();
      assertThat(getUserInfoResponseJSON.get(key)).isEqualTo(expectedUserResponse.get(key));
    }
  }

  /**
   * Test get-user-info failure case when no session attached to request
   */
  @Test
  public void get_user_info_no_session() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    HttpResponse<String> getUserInfoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").asString();
    JSONObject getUserInfoResponseJSON = TestUtils.responseStringToJSON(getUserInfoResponse.getBody());
    assertThat(getUserInfoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  /**
   * Test get-user-info failure case when username changes between session creation and get-user-info request
   */
  @Test
  public void get_user_info_modified_username() {
    String username = "username1";
    String password = "password1234";
    User user = EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();

    // delete user and recreate with modified username, simulating a username change
    userDao.delete(user.getUsername());
    user.setUsername("username-updated");
    userDao.save(user);

    HttpResponse<String> getUserInfoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").cookie(loginResponse.getCookies()).asString();
    JSONObject getUserInfoResponseJSON = TestUtils.responseStringToJSON(getUserInfoResponse.getBody());
    assertThat(getUserInfoResponseJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  /**
   * Test get-user-info success case where JSON request body is present & ignored
   */
  @Test
  public void get_user_info_username_in_json_body() {
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

    // The `get-user-info` endpoint should ignore the JSON request body
    JSONObject getUserInfoBody = new JSONObject();
    getUserInfoBody.put("username", "a-fake-username");
    HttpResponse<String> getUserInfoResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").cookie(loginResponse.getCookies()).body(body).asString();
    JSONObject getUserInfoResponseJSON = TestUtils.responseStringToJSON(getUserInfoResponse.getBody());
    assertThat(getUserInfoResponseJSON.getString("status")).isEqualTo("SUCCESS");
  }

  /**
   * Test get-user-info failure case when incorrect HTTP method used
   */
  @Test
  public void get_user_info_incorrect_http_method() {
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

    // A GET to an endpoint that only accepts POST should result in a 404
    HttpResponse<String> getUserInfoResponse =
        Unirest.get(TestUtils.getServerUrl() + "/get-user-info").cookie(loginResponse.getCookies()).asString();
    assertThat(getUserInfoResponse.getStatus()).isEqualTo(404);
  }
}
