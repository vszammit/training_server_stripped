package User;

import Config.Message;
import Database.UserDao;
import Logger.LogFactory;
import Security.SecurityUtils;
import User.Services.GetUserInfoService;
import User.Services.LoginService;
import com.mongodb.client.MongoDatabase;
import io.javalin.http.Handler;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

public class UserController {
  Logger logger;
  MongoDatabase db;
  UserDao userDao;

  public UserController(UserDao userDao) {
    this.userDao = userDao;
    LogFactory l = new LogFactory();
    logger = l.createLogger("UserController");
    logger = (new LogFactory()).createLogger("UserController");
  }

  public Handler loginUser =
      ctx -> {
        ctx.req.getSession().invalidate();
        JSONObject req = new JSONObject(ctx.body());
        String username = req.getString("username");
        String password = req.getString("password");
        LoginService loginService = new LoginService(userDao, logger, username, password);
        // implement the rest here
        Message userMessage = loginService.executeAndGetResponse();
        if (userMessage == UserMessage.AUTH_SUCCESS) {
            ctx.sessionAttribute("username", username);
        }
        ctx.result(userMessage.toResponseString());

      };

  public Handler logout =
      ctx -> {
        ctx.req.getSession().invalidate();
        logger.info("Signed out");
        ctx.result(UserMessage.SUCCESS.toJSON().toString());
      };

  public Handler getUserInfo =
      ctx -> {
        logger.info("Started getUserInfo handler");
        String username = ctx.sessionAttribute("username");
        logger.info(username);
        GetUserInfoService infoService = new GetUserInfoService(userDao, logger, username);
        // implement the rest here
        Message userMessage = infoService.executeAndGetResponse();
        if (userMessage == UserMessage.SUCCESS) {
            JSONObject userFields = infoService.getUserFields();
            JSONObject messageJson = userMessage.toJSON();
            JSONObject finalJson = mergeJSON(messageJson, userFields);
            logger.info("Got User Info");
            ctx.result(finalJson.toString());
        } else {
            logger.info("Could not get User Info");
            ctx.result(userMessage.toResponseString());
        }
      };

  // helper function to merge 2 json objects
  public static JSONObject mergeJSON(JSONObject object1, JSONObject object2) {
    JSONObject merged = new JSONObject(object1, JSONObject.getNames(object1));
    for (String key : JSONObject.getNames(object2)) {
      merged.put(key, object2.get(key));
    }
    return merged;
  }
}
