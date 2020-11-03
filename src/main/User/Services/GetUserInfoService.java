package User.Services;

import Config.Message;
import Config.Service;
import Database.UserDao;
import User.User;
import User.UserMessage;
import Validation.ValidationUtils;
import java.util.Objects;
import org.json.JSONObject;
import org.slf4j.Logger;

public class GetUserInfoService implements Service {
  private UserDao userDao;
  private Logger logger;
  private String username;
  private User user;

  public GetUserInfoService(UserDao userDao, Logger logger, String username) {
    this.userDao = userDao;
    this.logger = logger;
    this.username = username;
  }

  @Override
  public Message executeAndGetResponse() {
    if (!ValidationUtils.isValidUsername(this.username)) {
      return UserMessage.USER_NOT_FOUND;
    }
    // TODO: return the appropriate response here
    return null;
  }

  public JSONObject getUserFields() {
    Objects.requireNonNull(user);
    JSONObject userObject = new JSONObject();
    userObject.put("organization", user.getOrganization());
    userObject.put("firstName", user.getFirstName());
    userObject.put("lastName", user.getLastName());
    userObject.put("birthDate", user.getBirthDate());
    userObject.put("address", user.getAddress());
    userObject.put("city", user.getCity());
    userObject.put("state", user.getState());
    userObject.put("zipcode", user.getZipcode());
    userObject.put("email", user.getEmail());
    userObject.put("phone", user.getPhone());
    userObject.put("twoFactorOn", user.getTwoFactorOn());
    userObject.put("username", user.getUsername());
    return userObject;
  }
}
