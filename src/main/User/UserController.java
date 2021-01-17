package User;

import Config.Message;
import Database.UserDao;
import Logger.LogFactory;
import User.Services.GetUserInfoService;
import User.Services.LoginService;
import com.mongodb.client.MongoDatabase;
import io.javalin.http.Handler;
import org.json.JSONObject;
import org.slf4j.Logger;

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
        String body = ctx.body();
        if (body == null || body.isEmpty()) {
          ctx.result(UserMessage.AUTH_FAILURE.toResponseString());
          return;
        }
        JSONObject req = new JSONObject(body);
        String username = req.has("username") ? req.getString("username") : null;
        String password = req.has("password") ? req.getString("password") : null;
        LoginService loginService = new LoginService(userDao, logger, username, password);
        Message response = loginService.executeAndGetResponse();

        if (response == UserMessage.AUTH_SUCCESS) {
          ctx.sessionAttribute("username", username);
        }

        ctx.result(response.toResponseString());
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
        GetUserInfoService infoService = new GetUserInfoService(userDao, logger, username);

        Message response = infoService.executeAndGetResponse();

        JSONObject responseBody = response.toJSON();
        if (response == UserMessage.SUCCESS) {
          JSONObject user = infoService.getUserFields();
          responseBody = mergeJSON(responseBody, user);
        }

        ctx.result(responseBody.toString());
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
