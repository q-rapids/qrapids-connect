package connect.taiga;

import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import model.taiga.CustomAttributes;
import model.taiga.CustomAttributesValues;
import model.taiga.Epic;
import model.taiga.Issue;
import model.taiga.Milestone;
import model.taiga.Project;
import model.taiga.Task;
import model.taiga.User;
import model.taiga.UserStory;
import rest.RESTInvoker;

public class TaigaApi {
    private static Gson gson = new Gson();
    private static DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    private static DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

    public TaigaApi() {
    }

    public static Issue[] getIssuesByProjectId(String url, String projectId, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/issues?project=" + projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Issue[] iss = gson.fromJson(json, model.taiga.Issue[].class);
        return iss;
    }

    public static UserStory[] getUserStoriesByProjectId(String url, String projectId, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/userstories?project=" + projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.UserStory[] us = gson.fromJson(json, model.taiga.UserStory[].class);
        return us;
    }

    public static UserStory getUserStoryById(String url, String Id, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/userstories/" + Id, token);
        String json = ri.getDataFromServer("");
        model.taiga.UserStory us = gson.fromJson(json, model.taiga.UserStory.class);
        return us;
    }

    public static Map<Integer, Milestone> getMilestonesByProjectId(String url, String projectId, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/milestones?project=" + projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Milestone[] mil = gson.fromJson(json, model.taiga.Milestone[].class);
        Map<Integer, Milestone> m = new HashMap();
        for (int i = 0; i < mil.length; ++i) {
            m.put(mil[i].id, mil[i]);
        }
        return m;
    }

    public static Task[] getTasks(String url, String projectId, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/tasks?project=" + projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Task[] task = gson.fromJson(json, model.taiga.Task[].class);
        return task;
    }

    public static Epic[] getEpicsByProjectID(String url, String projectId, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/epics?project=" + projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Epic[] ep = gson.fromJson(json, model.taiga.Epic[].class);
        return ep;
    }

    public static Integer getProjectId(String url, String slug, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/projects/by_slug?slug=" + slug, token);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        return pr.id;
    }

    public static Project getProject(String url, String id, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/projects/" + id, token);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        RESTInvoker ri2 = new RESTInvoker(url + "/projects/" + id + "/stats", token);
        String json2 = ri2.getDataFromServer("");
        model.taiga.Project pr2 = gson.fromJson(json2, model.taiga.Project.class);
        pr.closed_points = pr2.closed_points;
        pr.defined_points = pr2.defined_points;
        pr.total_points = pr2.total_points;
        return pr;
    }

    public static String Login(String url, String name, String password) {
        RESTInvoker ri = new RESTInvoker(url + "/auth", name, password);
        String json = ri.restlogin(url + "/auth", name, password);
        model.taiga.User u = gson.fromJson(json, model.taiga.User.class);
        return u.auth_token;
    }

    public static Map<Integer, String> getCustomAttributesIDs(String url, String type, String projectID, String token) {
        Map<Integer, String> WantedIDs = new HashMap();
        int i;
        if (type == "task") {
            RESTInvoker ri = new RESTInvoker(url + "/task-custom-attributes?project=" + projectID, token);
            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributes[] attributes = gson.fromJson(json, model.taiga.CustomAttributes[].class);

            for(i = 0; i < attributes.length; ++i) {
                WantedIDs.put(attributes[i].id, attributes[i].name.toUpperCase());
            }
        }

        if (type == "userstory") {
            RESTInvoker ri2 = new RESTInvoker(url + "/userstory-custom-attributes?project=" + projectID, token);
            String json2 = ri2.getDataFromServer("");
            model.taiga.CustomAttributes[] attributes = gson.fromJson(json2, model.taiga.CustomAttributes[].class);

            for(i = 0; i < attributes.length; ++i) {
                WantedIDs.put(attributes[i].id, attributes[i].name.toUpperCase());
            }
        }

        return WantedIDs;
    }

    public static Map<String, String> getCustomAttributes(Integer ID, String type, String url, String token, Map<Integer, String> WantedIDs) {
        Map<String, String> temp = new HashMap();
        if (type == "task") {
            RESTInvoker ri = new RESTInvoker(url + "/tasks/custom-attributes-values/" + ID.toString(), token);
            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributesValues values = gson.fromJson(json, model.taiga.CustomAttributesValues.class);
            WantedIDs.forEach((k, v) -> {
                temp.put(v, values.attributes_values.get(k.toString()));
            });
        } else if (type == "userstory") {
            RESTInvoker ri = new RESTInvoker(url + "/userstories/custom-attributes-values/" + ID.toString(), token);
            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributesValues values = gson.fromJson(json, model.taiga.CustomAttributesValues.class);
            WantedIDs.forEach((k, v) -> {
                temp.put(v, values.attributes_values.get(k.toString()));
            });
        }
        return temp;
    }

    public static void main(String[] args) throws InterruptedException {
        Integer piD = getProjectId("https://api.taiga.io/api/v1", "aleixlinares-test", null);
        System.out.println(piD);
        String projectId = piD.toString();
        UserStory[] t = getUserStoriesByProjectId("https://api.taiga.io/api/v1", projectId, null);
        Map<Integer, String> m = getCustomAttributesIDs("https://api.taiga.io/api/v1", "userstory", projectId, null);
        System.out.println(m);

        for(int i = 0; i < t.length; ++i) {
            Map<String, String> ms = getCustomAttributes(t[i].id, "userstory", "https://api.taiga.io/api/v1", null, m);
            System.out.println(t[i].subject);
            System.out.println(ms.get("PRIORIDAD"));
        }

    }
}
