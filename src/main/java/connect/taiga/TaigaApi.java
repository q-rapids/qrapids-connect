package connect.taiga;

import com.google.gson.Gson;
import model.github.GithubIssues;
import rest.RESTInvoker;
import model.taiga.*;

import java.util.*;

public class TaigaApi {

    private static Gson  gson = new Gson();

    public static Issue[] getIssuesByProjectId(String url, String projectId, String token) {
        //Request of the project's issues

        RESTInvoker ri = new RESTInvoker(url+"/issues?project="+projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Issue[] iss = gson.fromJson(json, model.taiga.Issue[].class);
        /*for(model.taiga.Issue i : iss) {
            System.out.println(i.subject);
            System.out.println(i.status_extra_info.name);
            if(i.assigned_to_extra_info!=null) System.out.println(i.assigned_to_extra_info.username);
        }*/
        return iss;
    }

    public static UserStory[] getUserStroriesByProjectId(String url, String projectId, String token) {
        //Request of the project's user stories

        RESTInvoker ri = new RESTInvoker(url+"/userstories?project="+projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.UserStory[] us = gson.fromJson(json, model.taiga.UserStory[].class);
        /*for(model.taiga.UserStory u : us) {
            System.out.println(u.subject);
            if(u.epics!=null) {
                for (Epic e : u.epics) {
                    System.out.println(e.id);
                    System.out.println(e.subject);
                }
            }
            else System.out.println("HI HA UN NULL????");
        }*/
        return us;
    }

    public static UserStory getUserStroryById(String url, String Id, String token) {
        //Request of the project's user stories

        RESTInvoker ri = new RESTInvoker(url+"/userstories/"+Id, token);
        String json = ri.getDataFromServer("");
        model.taiga.UserStory us = gson.fromJson(json, model.taiga.UserStory.class);
        return us;
    }

    public static Milestone[] getMilestonesByProjectId(String url, String projectId, String token) {
        //Request of the project's epics

        RESTInvoker ri = new RESTInvoker(url+"/milestones?project="+projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Milestone[] mil = gson.fromJson(json, model.taiga.Milestone[].class);
        /*for(model.taiga.Milestone m : mil) {
            System.out.println(m.name);
            System.out.println(m.slug);
            System.out.println(m.id);
            System.out.println(m.total_points);
            System.out.println(m.closed_points);
            System.out.println(m.closed);
            System.out.println(m.estimated_finish);
            System.out.println(m.estimated_start);
            System.out.println(m.created_date);
            System.out.println(m.modified_date);
        }*/
        return mil;
    }

    public static Task[] getTasks(String url, String projectId, String token) {
        //Request of the project's tasks. Tasks can be from a project, milestone or user story

        RESTInvoker ri = new RESTInvoker(url+"/tasks?project="+projectId, token);
        String json = ri.getDataFromServer("");
        model.taiga.Task[] task = gson.fromJson(json, model.taiga.Task[].class);
        /*for(model.taiga.Task t : task) {
            System.out.println(t.subject);
            System.out.println(t.id);
            System.out.println(t.milestone_slug);
            System.out.println(t.status_extra_info.name);
            if(t.assigned_to_extra_info!=null) System.out.println(t.assigned_to_extra_info.username);
        }*/
        return task;
    }

    public static Epic[] getEpicsByProjectID(String url, String projectId, String token) {
        //Request of the project's epics

        RESTInvoker ri = new RESTInvoker(url+"/epics?project="+projectId,token);
        String json = ri.getDataFromServer("");
        model.taiga.Epic[] ep = gson.fromJson(json, model.taiga.Epic[].class);
        /*for(model.taiga.Epic e : ep) {
            System.out.println(e.id);
            System.out.println(e.subject);
        }*/
        return ep;
    }

    public static Integer getProjectId(String url, String slug, String token) {
        RESTInvoker ri = new RESTInvoker(url + "/projects/by_slug?slug=" + slug,token);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        return pr.id;
    }

    public static Project getProject(String url, String id, String token) {

        RESTInvoker ri = new RESTInvoker(url+"/projects/"+id, token);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        //Request of other project stats
        RESTInvoker ri2 = new RESTInvoker(url+"/projects/"+id+"/stats", token);
        String json2 = ri2.getDataFromServer("");
        model.taiga.Project pr2 = gson.fromJson(json2, model.taiga.Project.class);
        pr.closed_points=pr2.closed_points;
        pr.defined_points=pr2.defined_points;
        pr.total_points=pr2.total_points;
        return pr;

    }

    public static String Login(String url, String name, String password) {
        RESTInvoker ri = new RESTInvoker(url+"/auth", name, password);
        String json=ri.restlogin(url+"/auth", name, password);
        model.taiga.User u = gson.fromJson(json, model.taiga.User.class);
        return u.auth_token;
    }

    public static Map<Integer,String> getCustomAttributesIDs(String url, String type, String projectID, String token, String[] customAttributes) {

        Map<Integer, String> WantedIDs = new HashMap<>();
        if(type=="task") {
            RESTInvoker ri = new RESTInvoker(url + "/task-custom-attributes?project=" + projectID, token);

            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributes[] attributes = gson.fromJson(json, model.taiga.CustomAttributes[].class);

            for (int i = 0; i < attributes.length; ++i) {
                if(Arrays.asList(customAttributes).contains(attributes[i].name))  {
                    WantedIDs.put(attributes[i].id, attributes[i].name);
                }
            }
        }
        if(type=="userstory") {
            RESTInvoker ri2 = new RESTInvoker(url + "/userstory-custom-attributes?project=" + projectID, token);

            String json2 = ri2.getDataFromServer("");
            model.taiga.CustomAttributes[] attributes = gson.fromJson(json2, model.taiga.CustomAttributes[].class);
            for (int i = 0; i < attributes.length; ++i) {
                if(Arrays.asList(customAttributes).contains(attributes[i].name))  {
                    WantedIDs.put(attributes[i].id, attributes[i].name);
                }
            }
        }
        return WantedIDs;

    }


    public static Map<String, String> getCustomAttributes(Integer ID, String type, String url, String token, Map<Integer,String> WantedIDs) {
        Map<String, String> temp = new HashMap<>();
        if(type=="task") {
            RESTInvoker ri = new RESTInvoker(url + "/tasks/custom-attributes-values/" + ID.toString(), token);
            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributesValues values = gson.fromJson(json, model.taiga.CustomAttributesValues.class);
            WantedIDs.forEach((k, v) -> temp.put(v, values.attributes_values.get(k.toString())));
        }
        else if(type=="userstory") {
            RESTInvoker ri = new RESTInvoker(url + "/userstories/custom-attributes-values/" + ID.toString(), token);
            String json = ri.getDataFromServer("");
            model.taiga.CustomAttributesValues values = gson.fromJson(json, model.taiga.CustomAttributesValues.class);
            WantedIDs.forEach((k, v) -> temp.put(v, values.attributes_values.get(k.toString())));
        }
        return temp;

    }

    public static void main(String[] args) {



        RESTInvoker ri = new RESTInvoker("https://api.taiga.io/api/v1/auth", "aleix.linares@estudiantat.upc.edu", "rfc.185,ws");
        String json = ri.restlogin("https://api.taiga.io/api/v1/auth", "aleix.linares@estudiantat.upc.edu", "rfc.185,ws");
        model.taiga.User u = gson.fromJson(json, model.taiga.User.class);
        System.out.println(u.username);
        System.out.println(u.auth_token);
        Integer piD = getProjectId("https://api.taiga.io/api/v1","aleixlinares-test", u.auth_token);
        System.out.println(piD);
        String projectId= piD.toString();



        String test = "Estimated Effort,Actual Effort";
        String testarray[] = test.split(",");
        System.out.println(getCustomAttributesIDs("https://api.taiga.io/api/v1", "task", projectId, u.auth_token, testarray));

        //UserStory[] us = getUserStroriesByProjectId("https://api.taiga.io/api/v1", projectId, u.auth_token);

        Task[] tasks = getTasks("https://api.taiga.io/api/v1", projectId, u.auth_token);
        Map<Integer,String> WantedIDs =  getCustomAttributesIDs("https://api.taiga.io/api/v1","task",  projectId, u.auth_token, testarray);
        System.out.println(WantedIDs);
        for(int i=0; i<tasks.length; ++i) {
            Map<String,String> m = getCustomAttributes(tasks[i].id,"task", "https://api.taiga.io/api/v1", u.auth_token, WantedIDs);
            System.out.println(m.get("Actual Effort"));
        }



       /* RESTInvoker ri2= new RESTInvoker("https://api.taiga.io/api/v1/task-custom-attributes?project=" +projectId , u.auth_token);
        String json2 = ri2.getDataFromServer("");
        model.taiga.TaskCustomAttributes[] t = gson.fromJson(json2,model.taiga.TaskCustomAttributes[].class);
        System.out.println(t[0].name);
        System.out.println(t[0].id);
        System.out.println(t[1].name);
        System.out.println(t[1].id);
        System.out.println(t[2].name);
        System.out.println(t[2].id);


        for (Integer i = 0; i < tasks.length; i++) {
            RESTInvoker ri3 = new RESTInvoker("https://api.taiga.io/api/v1/tasks/custom-attributes-values/" + tasks[i].id.toString(), u.auth_token);
            String json3 = ri3.getDataFromServer("");
            model.taiga.TaskCustomAttributesValues t2 = gson.fromJson(json3, model.taiga.TaskCustomAttributesValues.class);
            System.out.println("CUSTOM ATTRIBUTES "+ t2.task.toString());
            System.out.println(t2.version);
            t2.attributes_values.forEach((k,v)-> System.out.println("KEY: "+ k.toString() + " VALUE: " + v.toString()));
            System.out.println("ESTIMATED EFFORT? --> " + t2.attributes_values.get("19129"));
        }

        //Request of a project
       /* System.out.println("Projecte Eventic");
        RESTInvoker ri = new RESTInvoker("https://api.taiga.io/api/v1/projects/by_slug?slug=csansoon-eventic", "Aleix Linares", "rfc.185,ws");
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        //Request of other project stats
        RESTInvoker ri2 = new RESTInvoker("https://api.taiga.io/api/v1/projects/399143/stats", "Aleix Linares", "rfc.185,ws");
        String json2 = ri2.getDataFromServer("");
        model.taiga.Project pr2 = gson.fromJson(json2, model.taiga.Project.class);
        pr.closed_points=pr2.closed_points;
        pr.defined_points=pr2.defined_points;
        pr.total_points=pr2.total_points;
        */
        //pr.issues = getIssuesByProjectId("https://api.taiga.io/api/v1/issues", "399143", "Taiga Username", "Taiga Password");
        //pr.userStories = getUserStroriesByProjectId("https://api.taiga.io/api/v1/userstories", "399143", "Aleix Linares", "rfc.185,ws");
        //pr.milestones = getMilestonesByProjectId("https://api.taiga.io/api/v1/milestones", "399143", "Taiga Username", "Taiga Password");
        //Task[] tasks = getTasks("https://api.taiga.io/api/v1", projectId, u.auth_token);
        //pr.epics = getEpicsByProjectID("https://api.taiga.io/api/v1/epics", "399143", "Aleix Linares", "rfc.185,ws");
    }


}
