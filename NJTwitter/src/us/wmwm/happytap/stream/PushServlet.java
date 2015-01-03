package us.wmwm.happytap.stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by gravener on 1/3/15.
 */
public class PushServlet extends HttpServlet {

    MongoClient client;

    Gson gson = new Gson();

    public PushServlet(MongoClient client) {
        this.client = client;
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pushId = req.getParameter("push_id");
        String screenname = req.getParameter("screen_name");
        if(screenname==null) {
            screenname = req.getParameter("service");
        }
        if(screenname==null) {
            screenname="nj_rails";
        }
        String dayString = req.getParameter("day");
        String hourString = req.getParameter("hour");
        int day,hour;
        Calendar cal = Calendar.getInstance();
        if(dayString==null) {
            day = cal.get(Calendar.DAY_OF_WEEK);
        } else {
            day = Integer.parseInt(dayString);
        }
        if(hourString==null) {
            hour = cal.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = Integer.parseInt(hourString);
        }
        DBCursor cursor = HappyStream.findUsersForService(HappyStream.ServiceType.DYNAMIC_SERVICES,screenname,day,hour);
        List<String> pushIds = new ArrayList<String>();
        while(cursor.hasNext()) {
            pushIds.add((String)cursor.next().get("push_id"));
        }
        resp.getWriter().write(gson.toJson(pushIds));
    }
}
