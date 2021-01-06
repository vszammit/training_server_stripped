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

  // TODO: add more tests

  @Test
  public void login_success(){
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
  public void getUserInfoFailure(){
    JSONObject body = new JSONObject();
    body.put("username", "nonexistentUser");
    body.put("password", "password");
    HttpResponse<String> userInfo =
      Unirest.post(TestUtils.getServerUrl() + "/login").body(body.toString()).asString();
    JSONObject userInfoJSON = TestUtils.responseStringToJSON(userInfo.getBody());
    assertThat(userInfoJSON.getString("status")).isEqualTo("USER_NOT_FOUND");
  }

  @Test
  public void getUserInfoSuccess(){
    String username = "username4";
    String password = "password4";
    String email = "four@4mail.com";
    String address = "44 fourth Str.";
    String zip = "44444";
    EntityFactory.createUser()
      .withUsername(username)
      .withPasswordToHash(password)
      .withEmail(email)
      .withAddress(address)
      .withZipcode(zip)
            .buildAndPersist(userDao);

    JSONObject login = new JSONObject();
    login.put("username", username);
    login.put("password", password);
    HttpResponse<String> loginResponse =
            Unirest.post(TestUtils.getServerUrl() + "/login").body(login.toString()).asString();
    JSONObject loginJSON = TestUtils.responseStringToJSON(loginResponse.getBody());
    assertThat(loginJSON.getString("status")).isEqualTo("AUTH_SUCCESS");

    JSONObject getInfo = new JSONObject();
    getInfo.put("username", username);
    HttpResponse<String> getInfoResponse =
            Unirest.post(TestUtils.getServerUrl() + "/get-user-info")
              .body(loginResponse.toString()).asString();
    JSONObject getInfoJSON =
            TestUtils.responseStringToJSON(getInfoResponse.getBody());
    assertThat(getInfoJSON.getString("username")).isEqualTo(username);
    assertThat(getInfoJSON.getString("address")).isEqualTo(address);
    assertThat(getInfoJSON.getString("email")).isEqualTo(email);
  }

}
