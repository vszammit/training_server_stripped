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
  public void userSuccessfullyAuthenticated() {
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
  public void userDoesNotExist() {
    String username = "username1";
    String password = "password1234";
    String incorrectUsername = "username";
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
  public void usernameEmpty() {
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
  public void userGetEmail() {
    String username = "username1";
    String password = "password1234";
    String email = "victoria@gmail";
    String firstName = "victoria";
    EntityFactory.createUser()
        .withUsername(username)
        .withFirstName(firstName)
        .withPasswordToHash(password)
        .withEmail(email)
        .buildAndPersist(userDao);

    JSONObject body = new JSONObject();
    body.put("username", username);
    body.put("password", password);

    HttpResponse<String> loginResponse =
        Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject loginResponseJSON = TestUtils.responseStringToJSON(loginResponse.getBody());

    HttpResponse<String> infoServiceResponse =
        Unirest.post(TestUtils.getServerUrl() + "/get-user-info").body(body.toString()).asString();
    JSONObject infoServiceResponseJSON =
        TestUtils.responseStringToJSON(infoServiceResponse.getBody());

    assertThat(infoServiceResponseJSON.getString("email")).isEqualTo(email);
    assertThat(infoServiceResponseJSON.getString("firstName")).isEqualTo(firstName);
  }

  @Test
  public void getFirstName() {
    String username = "username1";
    String password = "password123";
    String firstName = "victoria";
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
  public void getLastName() {
    String username = "username1";
    String password = "password123";
    String lastName = "zammit";
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
  public void LoggedInAndInfoFound() {
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
    String username = "username";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .withOrgName("Hack4Impact")
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
    assertThat(infoResponseJSON.getString("organization")).isEqualTo("Hack4Impact");
  }

  @Test
  public void infoServiceWrongLogin() {
    String username = "username1";
    String password = "password1234";
    EntityFactory.createUser()
        .withUsername(username)
        .withPasswordToHash(password)
        .withOrgName("Hack4Impact")
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
        .withOrgName("Hack4Impact")
        .withAddress("Lake Drive")
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
    assertThat(infoResponseJSON.getString("organization")).isEqualTo("Hack4Impact");
    assertThat(infoResponseJSON.getString("address")).isEqualTo("Lake Drive");
  }
}
