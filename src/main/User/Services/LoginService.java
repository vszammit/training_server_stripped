package User.Services;

import Config.Message;
import Config.Service;
import Database.UserDao;
import Security.SecurityUtils;
import User.User;
import User.UserMessage;
import Validation.ValidationUtils;
import org.slf4j.Logger;

import java.util.Optional;

public class LoginService implements Service {
  private Logger logger;
  private UserDao userDao;
  private String username;
  private String password;
  private User user;

  public LoginService(UserDao userDao, Logger logger, String username, String password) {
    this.userDao = userDao;
    this.logger = logger;
    this.username = username;
    this.password = password;
  }

  public Message executeAndGetResponse() {
    if (!ValidationUtils.isValidUsername(this.username)
        || !ValidationUtils.isValidPassword(this.password)) {
      logger.info("Invalid username and/or password");
      return UserMessage.AUTH_FAILURE;
    }
    // TODO: see UserMessage for appropriate return types
    Optional<User> ou = userDao.get(this.username);
    if(ou.isEmpty()){
      logger.info("User does not exist");
      return UserMessage.USER_NOT_FOUND;
    }
    this.user = ou.get();
    if (verifyPassword(this.password, user.getPassword())){
      logger.info("Successful login");
      return UserMessage.AUTH_SUCCESS;
    }
    else {
      logger.info("incorrect password");
      return UserMessage.AUTH_FAILURE;
    }
  }

  public boolean verifyPassword(String inputPassword, String userHash) {
    SecurityUtils.PassHashEnum verifyPasswordStatus =
        SecurityUtils.verifyPassword(inputPassword, userHash);
    switch (verifyPasswordStatus) {
      case SUCCESS:
        return true;
      case ERROR:
        {
          logger.error("Failed to hash password");
          return false;
        }
      case FAILURE:
        {
          logger.info("Incorrect password");
          return false;
        }
    }
    return false;
  }
}
