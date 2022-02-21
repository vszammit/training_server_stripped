package User.Services;

import Config.Message;
import Config.Service;
import Database.UserDao;
import User.User;
import User.UserMessage;
import Validation.ValidationUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class GetUserInfoService implements Service {
  private UserDao userDao;
  private Logger logger;
  private String username;
  private User user;
  private Optional<User> userOption;

  public GetUserInfoService(UserDao userDao, Logger logger, String username) {
    this.userDao = userDao;
    this.logger = logger;
    this.username = username;
  }

  @Override
  public Message executeAndGetResponse() {
    if (!ValidationUtils.isValidUsername(this.username)) {
      return UserMessage.INVALID_PARAMETER;
    }
    Optional<User> optionalUser = userDao.get(this.username);
    if (optionalUser.isEmpty()) {
      logger.info("User not found");
      return UserMessage.USER_NOT_FOUND;
    }
    logger.info("User found");
    this.user = optionalUser.get();

    return UserMessage.SUCCESS;
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
