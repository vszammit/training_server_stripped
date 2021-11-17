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
  public void successful_login() {
    String username = "username1";
    String password = "password123";
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
  public void invalid_password() {
    String username = "username1";
    String password = "password123";
    String incorrectPassword = "invalidPassword";
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
  public void invalid_user() {
    String username = "username1";
    String password = "password123";
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
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void empty_password() {
    String username = "username1";
    String password = "password123";
    String incorrectPassword = "";
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
  public void empty_username() {
    String username = "username1";
    String password = "password123";
    String incorrectUsername = "";
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

  // User Info Integration Tests

  @Test
  public void successful_login_and_request() {
    String username = "username1";
    String password = "password123";
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

    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("SUCCESS");
  }

  @Test
  public void get_email() {
    String username = "username1";
    String password = "password123";
    String email = "user@email.com";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withEmail(email)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(userInfoJSON.getString("email")).isEqualTo(email);
  }


  @Test
  public void get_first_name() {
    String username = "username1";
    String password = "password123";
    String firstName = "Tirtha";
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
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(userInfoJSON.getString("firstName")).isEqualTo(firstName);
  }

  @Test
  public void get_last_name() {
    String username = "username1";
    String password = "password123";
    String lastName = "Kharel";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withLastName(lastName)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(userInfoJSON.getString("lastName")).isEqualTo(lastName);
  }

  @Test
  public void get_organization() {
    String username = "username1";
    String password = "password123";
    String organization = "Penn";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .withOrgName(organization)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    HttpResponse<String> userInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfoResponse.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("SUCCESS");
    assertThat(userInfoJSON.getString("organization")).isEqualTo(organization);
  }
}
