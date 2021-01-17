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

    Optional<User> user = userDao.get(username);

    // Indicates username does match any existing user in db
    if (user.isEmpty()) {
      return UserMessage.USER_NOT_FOUND;
    }

    // Verify passed password matches stored password hash
    if (verifyPassword(this.password, user.get().getPassword())) {
      return UserMessage.AUTH_SUCCESS;
    }

    return UserMessage.AUTH_FAILURE;
  }

  /**
   * Hash user-provided candidate inputPassword and compare with passwordHash
   * @param inputPassword The user-provided candidate password
   * @param passwordHash  The password hash to compare the input against
   * @return Boolean indicating whether inputPassword successfully matches passwordHash
   */
  public boolean verifyPassword(String inputPassword, String passwordHash) {
    SecurityUtils.PassHashEnum verifyPasswordStatus =
        SecurityUtils.verifyPassword(inputPassword, passwordHash);
    switch (verifyPasswordStatus) {
      case SUCCESS:
        return true;

      case ERROR:
        logger.error("Failed to hash password");
        return false;

      case FAILURE:
        logger.info("Incorrect password");
        return false;
    }
    return false;
  }
}
