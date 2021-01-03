package User;

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
        JSONObject req = new JSONObject(ctx.body());
        String username = req.getString("username");
        String password = req.getString("password");
        LoginService loginService = new LoginService(userDao, logger, username, password);
        // implement the rest here
        if (loginService.verifyPassword(password, username)) {
            ctx.sessionAttribute("username", username);
        }
        ctx.result(loginService.executeAndGetResponse().toResponseString());
        // is that it?
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
        // implement the rest here
        JSONObject req = infoService.getUserFields();
        JSONObject output = mergeJSON({"status":ERROR}, req);
        //that clearly doesn't work.
        ctx.result(output);
        /*wait so here is the ctx body supposed to provide everything? also, what is a error,
          I don't see a list of errors like for the login service. Also, why is there no password
          mentioned in the code yet? are we getting the info from ctx? or something else?
         */
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
