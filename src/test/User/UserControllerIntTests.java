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

  // TODO: add more tests

  // Login tests
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
  public void userNotFound() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username123";
    String incorrectPassword = "badPassword123";
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
  public void userInputEmpty() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username123";
    String incorrectPassword = "badPassword123";
    EntityFactory.createUser()
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", "");
    body.put("password", "");

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_FAILURE");
  }

  @Test
  public void userSuccessfullyAuthenticated() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username123";
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
  public void userPasswordEmpty() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username123";
    String incorrectPassword = "badPassword123";
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

  @Test
  public void usernameEmpty() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username123";
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

  // get user info tests
  @Test
  public void login_successful() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    String email = "zhang98@bu.edu";
    EntityFactory.createUser()
            .withEmail(email)
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON = TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(loginResponseJSON.getString("status")).isEqualTo("AUTH_SUCCESS");
  }

  @Test
  public void userGetEmail() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    String email = "zhang98@bu.edu";
    EntityFactory.createUser()
            .withEmail(email)
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON = TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(infoServiceResponseJSON.getString("email")).isEqualTo(email);
  }

  @Test
  public void userGetFirstName() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    String email = "zhang98@bu.edu";
    EntityFactory.createUser()
            .withEmail(email)
            .withUsername(username)
            .withPasswordToHash(password)
            .withFirstName("Jason")
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON = TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(infoServiceResponseJSON.getString("firstName")).isEqualTo("Jason");
  }

  @Test
  public void userGetLastName() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    String email = "zhang98@bu.edu";
    String lastName = "Zhang";
    EntityFactory.createUser()
            .withEmail(email)
            .withUsername(username)
            .withLastName(lastName)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON = TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(infoServiceResponseJSON.getString("lastName")).isEqualTo(lastName);
  }

  @Test
  public void userGetCity() {
    String username = "username1";
    String password = "password1234";
    String incorrectPassword = "badPassword123";
    String city = "New York";
    EntityFactory.createUser()
            .withCity(city)
            .withUsername(username)
            .withPasswordToHash(password)
            .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON = TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(infoServiceResponseJSON.getString("city")).isEqualTo(city);
  }
}
