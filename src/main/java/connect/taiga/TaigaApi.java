package connect.taiga;

import com.google.gson.Gson;
import model.github.GithubIssues;
import rest.RESTInvoker;
import model.taiga.*;

public class TaigaApi {

    private static Gson  gson = new Gson();

    public static Issue[] getIssuesByProjectId(String url, String projectId, String name, String password) {
        //Request of the project's issues

        RESTInvoker ri = new RESTInvoker(url+"/issues?project="+projectId, name, password);
        String json = ri.getDataFromServer("");
        model.taiga.Issue[] iss = gson.fromJson(json, model.taiga.Issue[].class);
        /*for(model.taiga.Issue i : iss) {
            System.out.println(i.subject);
            System.out.println(i.status_extra_info.name);
            if(i.assigned_to_extra_info!=null) System.out.println(i.assigned_to_extra_info.username);
        }*/
        return iss;
    }

    public static UserStory[] getUserStroriesByProjectId(String url, String projectId, String name, String password) {
        //Request of the project's user stories

        RESTInvoker ri = new RESTInvoker(url+"/userstories?project="+projectId, name, password);
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
    public static Milestone[] getMilestonesByProjectId(String url, String projectId, String name, String password) {
        //Request of the project's epics

        RESTInvoker ri = new RESTInvoker(url+"/milestones?project="+projectId, name, password);
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

    public static Task[] getTasks(String url, String projectId, String name, String password) {
        //Request of the project's tasks. Tasks can be from a project, milestone or user story

        RESTInvoker ri = new RESTInvoker(url+"/tasks?project="+projectId, name, password);
        String json = ri.getDataFromServer("");
        model.taiga.Task[] task = gson.fromJson(json, model.taiga.Task[].class);
        /*for(model.taiga.Task t : task) {
            System.out.println(t.subject);
            System.out.println(t.milestone_slug);
            System.out.println(t.status_extra_info.name);
            if(t.assigned_to_extra_info!=null) System.out.println(t.assigned_to_extra_info.username);
        }*/
        return task;
    }

    public static Epic[] getEpicsByProjectID(String url, String projectId, String name, String password) {
        //Request of the project's epics

        RESTInvoker ri = new RESTInvoker(url+"/epics?project="+projectId, name, password);
        String json = ri.getDataFromServer("");
        model.taiga.Epic[] ep = gson.fromJson(json, model.taiga.Epic[].class);
        /*for(model.taiga.Epic e : ep) {
            System.out.println(e.id);
            System.out.println(e.subject);


        }*/
        return ep;
    }

    public static Integer getProjectId(String url, String slug, String name, String password) {
        RESTInvoker ri = new RESTInvoker(url + "/projects/by_slug?slug=" + slug, name, password);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        return pr.id;
    }

    public static Project getProject(String url, String id, String name, String password) {

        RESTInvoker ri = new RESTInvoker(url+"/projects/"+id, name, password);
        String json = ri.getDataFromServer("");
        model.taiga.Project pr = gson.fromJson(json, model.taiga.Project.class);
        //Request of other project stats
        RESTInvoker ri2 = new RESTInvoker(url+"/projects/"+id+"/stats", name, password);
        String json2 = ri2.getDataFromServer("");
        model.taiga.Project pr2 = gson.fromJson(json2, model.taiga.Project.class);
        pr.closed_points=pr2.closed_points;
        pr.defined_points=pr2.defined_points;
        pr.total_points=pr2.total_points;
        return pr;

    }

    public static void main(String[] args) {
        //Request of a project
        System.out.println("Projecte Eventic");
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

        //pr.issues = getIssuesByProjectId("https://api.taiga.io/api/v1/issues", "399143", "Taiga Username", "Taiga Password");
        pr.userStories = getUserStroriesByProjectId("https://api.taiga.io/api/v1/userstories", "399143", "Aleix Linares", "rfc.185,ws");
        //pr.milestones = getMilestonesByProjectId("https://api.taiga.io/api/v1/milestones", "399143", "Taiga Username", "Taiga Password");
        //getTasks("https://api.taiga.io/api/v1/tasks", "399143", "Taiga Username", "Taiga Password");
        pr.epics = getEpicsByProjectID("https://api.taiga.io/api/v1/epics", "399143", "Aleix Linares", "rfc.185,ws");
    }


}
