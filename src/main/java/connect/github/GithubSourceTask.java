package connect.github;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;
import java.text.ParseException;
import java.util.logging.Logger;

import model.github.branch.Branch;
import model.github.branch.GitHubBranches;
import model.github.commit.Commit;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import model.github.*;
import org.elasticsearch.action.search.SearchResponse;
import rest.RESTInvoker;

import static connect.elasticsearch.ElasticsearchApi.getTaskReference;


public class GithubSourceTask extends SourceTask {

	private static final TimeZone tzUTC = TimeZone.getTimeZone("UTC");
	private static final DateFormat dfZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	static { dfZULU.setTimeZone(tzUTC); }

	private List<String> githubOrgUrl;
	private List<String[]> githubUrls;
	private String githubSecret;
	private String githubUser;
	private String githubPass;
	private List<String> issue_topic;
	private List<String> commit_topic;
	private List<String> taiga_topic;

	private String githubInterval;
	private Integer interval;
	private String githubTeamsNum;
	private Integer teamsNum;
	private String githubTeamsInterval;
	private Integer teamsInterval;

	private String createdSince;
	private Date defaultDate;
	private List< Map< String, Map<String, Date> > > commitMostRecentUpdate;

	private static final DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");

	private long lastPoll = 0;
	private List<Boolean> firstPoll;
	private List< Map<String, Boolean> > firstPollInRepo;
	private int currentTaskID;

	private final Logger log = Logger.getLogger(GithubSourceTask.class.getName());

	private static String getTaskNumber(String message) {
		List<String> words = new ArrayList<>(Arrays.asList(message.split(" ")));
		words.removeAll(Arrays.asList("", null));
		int i = 0;
		while(i < words.size()){
			if((words.get(i).equalsIgnoreCase("task") || words.get(i).equalsIgnoreCase("tasca") || words.get(i).equalsIgnoreCase("tarea")) && (i+1 < words.size())) {
				StringBuilder num = new StringBuilder();
				String aux = words.get(i + 1);
				int j = aux.charAt(0) == '#' ? 1 : 0; //if the first char is '#' we ignore it
				while (j < aux.length()) {
					if (!Character.isDigit(aux.charAt(j))) break;
					num.append(aux.charAt(j));
					++j;
				}
				if(num.length() == 0) return null;
				else return num.toString().replaceFirst("^0+(?!$)", ""); //we need to delete all leading zeros
			}
			++i;
		}
		return null;
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<>();
		log.info("lastPollDeltaMillis:" + (System.currentTimeMillis() - lastPoll) + " interval:" + interval );

		if ( lastPoll != 0L &&
			System.currentTimeMillis() < ( lastPoll + (long) (interval * 1000) )
			&& currentTaskID == 0) {

			log.info("------- exit polling, " + ( System.currentTimeMillis() - lastPoll ) / 1000 + " secs since last poll.");
			Thread.sleep(1000);
			return records;

		}

		else {
			if (currentTaskID == 0) lastPoll = System.currentTimeMillis();
			Thread.sleep(teamsInterval * 1000L); // Wait between teams
			log.info("\n\n**********");

			if (firstPoll.get(currentTaskID)) {

				try {

					if (currentTaskID == 0) defaultDate = onlyDate.parse(createdSince);
					firstPollInRepo.add(new HashMap<>());
					commitMostRecentUpdate.add(new HashMap<>());

					for (String url : githubUrls.get(currentTaskID)) {
						firstPollInRepo.get(currentTaskID).put(url, true);
						Map<String, Date> aux = new HashMap<>();
						commitMostRecentUpdate.get(currentTaskID).put(url, aux);
					}

				} catch (ParseException e) {
					log.info("unable to parse " + createdSince);
					throw new InterruptedException();
				}

			}
			else {

				githubUrls.remove(currentTaskID); //Update URLs in case of new repositories
				githubUrls.add(currentTaskID, getGithubUrlsFromOrg(githubOrgUrl.get(currentTaskID)));

				for ( String url : githubUrls.get(currentTaskID) ) {
					if (!firstPollInRepo.get(currentTaskID).containsKey(url)) {
						firstPollInRepo.get(currentTaskID).put(url, true);
						Map<String, Date> aux = new HashMap<>();
						commitMostRecentUpdate.get(currentTaskID).put(url, aux);
					}
				}

				//log.info("Issue info updated since: " + issueMostRecentUpdate);
				log.info("Is it the repo's first poll: " + firstPollInRepo.get(currentTaskID));
				log.info("Commit info updated since: " + commitMostRecentUpdate.get(currentTaskID) + "\n\n");
			}
		}

		log.info("Start executing task " + currentTaskID + " with Github organization " + githubOrgUrl.get(currentTaskID));

		//issues

		/*
		int offset = 1;
		GithubIssues redmineIssues;
		Date issue_maxUpdatedOn = null;

		do {
			redmineIssues = GithubApi.getIssues(githubUrl, githubSecret, createdSince, GithubApi.State.ALL, offset);
			log.info("ISSUES: Obtained " + redmineIssues.total_count + " issues from page " + offset);

			if (issue_maxUpdatedOn == null &&  redmineIssues.issues.length > 0 ) {
                issue_maxUpdatedOn=redmineIssues.issues[0].updated_at;
			}

			// download up to that moment
			if(redmineIssues.issues.length == 0 || issueMostRecentUpdate.compareTo(redmineIssues.issues[0].updated_at) >= 0)break;
			records.addAll( getIssueSourceRecords(redmineIssues, issueMostRecentUpdate) );
			offset += 1;
		} while (true);

        issueMostRecentUpdate = issue_maxUpdatedOn;

 		*/

		//commits
		try {
			for( String url : githubUrls.get(currentTaskID) ) {
				Repository repo = GithubApi.getRepository(url, githubSecret);
				log.info("Obtaining commits from " + url);
				Map<String, Date> mostRecentBranchUpdates = commitMostRecentUpdate.get(currentTaskID).get(url);

				int offset = 1;
				List<Branch> branches = new ArrayList<>();
				GitHubBranches auxBranch;
				//get a list of all repository branches
				do{
					auxBranch = GithubApi.getBranches(url, githubSecret, offset++);
					branches.addAll(Arrays.asList(auxBranch.branches));
				}while(auxBranch.total_count == 100);
				log.info("BRANCHES: Obtained " + branches.size() + " different branches");


				//fetch collaborator list
				offset = 1;
				List<User> collaborators = new ArrayList<>();
				GithubUsers auxUser;
				do{
					auxUser = GithubApi.getCollaborators(url, githubSecret, offset++);
					collaborators.addAll(Arrays.asList(auxUser.users));
				}while(auxUser.total_count == 100);

				if (collaborators.size() == 0){
					log.info("COLLABORATORS: No collaborators detected, skipping to next URL");
					continue;
				}

				//one collaborator is the professor
				log.info("COLLABORATORS: Obtained " + (collaborators.size() - 1) + " different collaborators");

				//getting a set of all new commits in every branch
				Set<Commit> commitsSet = new HashSet<>();

				for (Branch b : branches) {

					Date branch_maxUpdatedOn = null;

					Date mostRecentUpdate = mostRecentBranchUpdates.get(b.name);
					if(mostRecentUpdate == null) mostRecentUpdate = defaultDate;

					int commit_offset = 1;
					GitHubCommits commit;

					do {
						String branchName = b.name;

						commit = GithubApi.getCommits(url, githubSecret, branchName, commit_offset++);
						log.info("COMMITS: Obtained " + commit.total_count + " commits from branch " + b.name + " with page " + (commit_offset - 1));

						if (branch_maxUpdatedOn == null && commit.total_count > 0) {
							branch_maxUpdatedOn = commit.commits[0].commit.author.date;
						}

						// download up to that moment
						if (commit.commits.length == 0 || mostRecentUpdate.compareTo(commit.commits[0].commit.author.date) >= 0)
							break;

						// only new commits are saved
						int commitSetSize = commitsSet.size();
						for (Commit auxComm : commit.commits) {
							if (mostRecentUpdate.compareTo(auxComm.commit.author.date) < 0) commitsSet.add(auxComm);
						}
						if(commitSetSize != commitsSet.size()) log.info("COMMITS: Added " + (commitsSet.size() - commitSetSize) + " new commits from branch " + b.name);

					} while (commit.total_count == 100);

					if (branch_maxUpdatedOn != null) mostRecentBranchUpdates.put(b.name, branch_maxUpdatedOn);
				}

				//obtaining the stats (number of code additions and deletions) for each commit
				//all commits that are also merges are removed (all github merges have multiple parents)

				log.info("COMMITS: Obtaining commit stats for " + commitsSet.size() + " commits " + (commitsSet.size() > 15 ? "(this may take a little while)":""));
				int cont = 0;
				int merges = 0;
				long poll = System.currentTimeMillis();

				for (Iterator<Commit> i = commitsSet.iterator(); i.hasNext();) {
					Commit c = i.next();
					++cont;
					if((System.currentTimeMillis() - poll) >= 5000) {
						log.info("COMMITS: Obtained commit stats for " + cont + " commits");
						log.info("COMMITS: "+ merges + " commits were detected as merges and removed");
						poll = System.currentTimeMillis();
					}

					if(c.parents.size() > 1) {
						i.remove();
						++merges;
						continue;
					}
					c.stats = GithubApi.getCommitInfo(url, githubSecret, c.sha).stats;
				}

				log.info("COMMITS: Commit stats for repo" + url + "successfully obtained");

				if (firstPoll.get(currentTaskID) && commitsSet.size() != 0) commitsSet = removeLargestOldCommit(commitsSet);
				else if (firstPollInRepo.get(currentTaskID).get(url) && commitsSet.size() != 0) commitsSet = removeLargestOldCommit(commitsSet);

				if (commitsSet.size() != 0) records.addAll(getCommitSourceRecords(commitsSet, collaborators, repo));

				commitMostRecentUpdate.get(currentTaskID).put(url, mostRecentBranchUpdates);
				if (firstPollInRepo.get(currentTaskID).get(url)) firstPollInRepo.get(currentTaskID).put(url, false);
			}

			firstPoll.remove(currentTaskID);
			firstPoll.add(currentTaskID, false);
			log.info("Finished executing task " + currentTaskID + " with Github organization " + githubOrgUrl.get(currentTaskID));
			log.info("**********\n\n\n");

			++currentTaskID;
			if (currentTaskID == teamsNum) currentTaskID = 0;

		} catch (RuntimeException e){
			if (RESTInvoker.HTTP_STATUS_FORBIDDEN.equals(e.getMessage()))
				log.info("ERROR: GitHub API rate limit exceeded. The data will not be updated until the next poll");
			else throw new RuntimeException(e);
		}
		return records;
	}

	//Gets the 10 oldest commits (or all the commits if there are not 10) and removes the one with most line changes
	//The idea of this method is to remove the commit generated automatically by the used framework
	private Set<Commit> removeLargestOldCommit(Set<Commit> commitsSet) {
		ArrayList<Commit> a = new ArrayList<>(commitsSet);
		a.sort(Comparator.comparing(o -> o.commit.author.date));

		int largestCommitIndex = 0;
		int n = Math.min(commitsSet.size(), 10);

		for(int i = 1; i < n; ++i){
			Commit c = a.get(i);
			if (c.stats.total > (a.get(largestCommitIndex)).stats.total) largestCommitIndex = i;
		}

		log.info("COMMIT: Removed a commit with " + a.get(largestCommitIndex).stats.total + " lines modified");
		a.remove(largestCommitIndex);

		return new HashSet<>(a);
	}


	private List<SourceRecord> getCommitSourceRecords(Set<Commit> commitsList, List<User> collaborators, Repository repo) throws RuntimeException {
        List<SourceRecord> result = new ArrayList<>();

        for (Commit i : commitsList){

			User user = null;

			//if the commit author is not associated with GitHub, a default user is created
			if(i.author != null) {
				for (User u : collaborators) {
					if (u.login.equals(i.author.login)) {
						user = u;
						break;
					}
				}
			}

			//if the commit author is not associated with GitHub, a default user is created
			if(user == null) user = new User("anonymous", 0, "", "", false);

            log.info("COMMIT URL: " + i.url);

            Struct commit = new Struct(GithubSchema.githubCommit);
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_SHA, i.sha);
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_URL, i.url);
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_DATE, dfZULU.format(i.commit.author.date));
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_MESSAGE, i.commit.message);
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_VERIFIED, i.commit.verification.verified);
            commit.put(GithubSchema.FIELD_GITHUB_COMMIT_REASON, i.commit.verification.reason);
			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_REPO, repo.full_name);
			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_REASON, i.commit.verification.reason);
			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_MESSAGE_CHARCOUNT, (long) i.commit.message.length());
			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_MESSAGE_WORDCOUNT, (long) i.commit.message.split(" ").length);

			boolean task = i.commit.message.toLowerCase().contains("task") || i.commit.message.toLowerCase().contains("tasca") || i.commit.message.toLowerCase().contains("tarea");
			String NULL_STRING = "null";
			if (task) {
				String num = getTaskNumber(i.commit.message);
				if(num != null) {
					try {
						SearchResponse response = getTaskReference(taiga_topic.get(currentTaskID), Integer.parseInt(num));
						if(response.getHits().totalHits == 1) {
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, true);
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, num);
						} else if(response.getHits().totalHits == 0) {
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, false);
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, NULL_STRING);
						} else {
							log.info("ERROR: obtained multiple tasks with reference " + num );
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, false);
							commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, NULL_STRING);
						}
					} catch (IOException e) {
						log.info("ERROR: Error on fetch of task reference " + num );
						e.printStackTrace();
						commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, true);
						commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, NULL_STRING);
					}
					commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, num);
				} else {
					commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, false);
					commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, NULL_STRING);
				}
			} else {
				commit.put(GithubSchema.FIELD_GITHUB_COMMIT_CONTAINS_TASK, false);
				commit.put(GithubSchema.FIELD_GITHUB_COMMIT_TASK_REF, NULL_STRING);
			}

			Struct stats = new Struct(GithubSchema.githubStats);
			stats.put(GithubSchema.FIELD_GITHUB_STATS_TOTAL, i.stats.total);
			stats.put(GithubSchema.FIELD_GITHUB_STATS_ADD, i.stats.additions);
			stats.put(GithubSchema.FIELD_GITHUB_STATS_DEL, i.stats.deletions);

			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_STATS, stats);

			Struct userSchema = new Struct(GithubSchema.userSchema);
			userSchema.put( GithubSchema.FIELD_GITHUB_USER_LOGIN, user.login );
			userSchema.put( GithubSchema.FIELD_GITHUB_USER_ID, Long.toString(user.id) );
			userSchema.put( GithubSchema.FIELD_GITHUB_USER_URL, user.url );
			userSchema.put( GithubSchema.FIELD_GITHUB_USER_TYPE, user.type );
			userSchema.put( GithubSchema.FIELD_GITHUB_USER_ADMIN, String.valueOf(user.site_admin) );

			commit.put(GithubSchema.FIELD_GITHUB_COMMIT_USER, userSchema);

			Map<String,String> sourcePartition = new HashMap<>();
			sourcePartition.put( "githubUrl", repo.url );

			Map<String,String> sourceOffset = new HashMap<>();
			sourceOffset.put( "updated", dfZULU.format( new Date(System.currentTimeMillis()) ) );

			// we use the GitHub commit id (i.sha) as key in the elasticsearch index (_id)
			SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, commit_topic.get(currentTaskID), Schema.STRING_SCHEMA, i.sha, GithubSchema.githubCommit , commit);
			result.add(sr);
        }

        return result;
    }

	private String[] getGithubUrlsFromOrg(String githubOrgUrl) {
		Repository[] repos = GithubApi.getReposFromOrganization(githubOrgUrl, githubSecret);
		String[] repoUrls = new String[repos.length];
		for (int i = 0; i < repos.length; ++i) repoUrls[i] = repos[i].url;
		return repoUrls;
	}

/*
	private List<SourceRecord> getIssueSourceRecords(GithubIssues redmineIssues, Date updatedSince) {

		List<SourceRecord> result = new ArrayList<>();

		for ( Issue i : redmineIssues.issues ) {

			log.info("ISSUE ID: " + i.number);
			log.info("ISSUE URL: " + githubUrl+"/issues/"+i.number);

			Struct struct = new Struct( GithubSchema.githubIssue);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_URL, i.url);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_HTML_URL, i.html_url);
			// Issue IDs
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_NUMBER, i.number); // id for the project
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_ID, i.id); // global ID
			// Issue Info
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_TITLE, i.title);
			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_BODY, i.body);
			// Dates
			if (i.created_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_CREATED_AT, dfZULU.format(i.created_at));
			if (i.updated_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_UPDATED_AT, dfZULU.format(i.updated_at));
			if (i.closed_at != null)
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_CLOSED_AT, dfZULU.format(i.closed_at));

			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_STATE, i.state);

			// labels
			Vector<Struct> labels = new Vector<Struct>();
			for(int labelid = 0; labelid < i.labels.length; ++labelid){
				Struct label = new Struct(GithubSchema.labelsSchema);
				label.put("name", i.labels[labelid].name);
				labels.add(label);
			}

			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_LABELS, labels);

			//user
			Struct user = new Struct(GithubSchema.userSchema);
			user.put( GithubSchema.FIELD_GITHUB_USER_LOGIN, i.user.login );
			user.put( GithubSchema.FIELD_GITHUB_USER_ID, Long.toString(i.user.id) );
			user.put( GithubSchema.FIELD_GITHUB_USER_URL, i.user.url );
			user.put( GithubSchema.FIELD_GITHUB_USER_TYPE, i.user.type );
			user.put( GithubSchema.FIELD_GITHUB_USER_ADMIN, String.valueOf(i.user.site_admin) );

			struct.put( GithubSchema.FIELD_GITHUB_ISSUE_USER, user);

			//assignees
			if(i.assignees != null){
				Vector<Struct> assignees = new Vector<Struct>();
				for (User a: i.assignees) {
					Struct assignee = new Struct(GithubSchema.userSchema);
					assignee.put( GithubSchema.FIELD_GITHUB_USER_LOGIN, a.login );
					assignee.put( GithubSchema.FIELD_GITHUB_USER_ID, Long.toString(a.id) );
					assignee.put( GithubSchema.FIELD_GITHUB_USER_URL, a.url );
					assignee.put( GithubSchema.FIELD_GITHUB_USER_TYPE, a.type );
					assignee.put( GithubSchema.FIELD_GITHUB_USER_ADMIN, String.valueOf(a.site_admin) );

					assignees.add(assignee);
				}
				struct.put( GithubSchema.FIELD_GITHUB_ISSUE_ASSIGNEES, assignees);
			}

			Map<String,String> sourcePartition = new HashMap<>();
			sourcePartition.put( "githubUrl", githubUrl );

			Map<String,String> sourceOffset = new HashMap<>();
			sourceOffset.put( "updated",  dfZULU.format( i.updated_at ) );

			// we use the github id (i.id) as key in the elasticsearch index (_id)
			SourceRecord sr = new SourceRecord(sourcePartition, sourceOffset, issue_topic, Schema.STRING_SCHEMA, i.number.toString(), GithubSchema.githubIssue , struct);
			result.add(sr);

		}

		log.info("Found " + result.size() + " issues.");

		return result;
	}

 */
	
	@Override
	public void start(Map<String, String> props) {

		commitMostRecentUpdate = new ArrayList<>();
		firstPollInRepo = new ArrayList<>();
		log.info(props.toString());

		githubSecret = props.get( GithubSourceConfig.GITHUB_SECRET_CONFIG );
		githubUser = props.get( GithubSourceConfig.GITHUB_USER_CONFIG );
		githubPass = props.get( GithubSourceConfig.GITHUB_PASS_CONFIG );
		createdSince = props.get( GithubSourceConfig.GITHUB_CREATED_SINCE_CONFIG);
		githubInterval = props.get( GithubSourceConfig.GITHUB_INTERVAL_SECONDS_CONFIG );
		githubTeamsNum = props.get( GithubSourceConfig.GITHUB_TEAMS_NUMBER_CONFIG );
		githubTeamsInterval	= props.get( GithubSourceConfig.GITHUB_TEAMS_INTERVAL_CONFIG );

		teamsNum = Integer.parseInt(githubTeamsNum);
		firstPoll = new ArrayList<>( Collections.nCopies(teamsNum, true) );
		currentTaskID = 0;

		githubOrgUrl = new ArrayList<>();
		githubUrls = new ArrayList<>();
		issue_topic = new ArrayList<>();
		commit_topic = new ArrayList<>();
		taiga_topic = new ArrayList<>();

		for (int i = 0; i < teamsNum; ++i) {
			githubOrgUrl.add( props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_URL_CONFIG ) );
			issue_topic.add( props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_ISSUES_TOPIC_CONFIG ) );
			commit_topic.add( props.get( "tasks." + i + "." + GithubSourceConfig.GITHUB_COMMIT_TOPIC_CONFIG ) );
			taiga_topic.add( props.get( "tasks." + i + "." + GithubSourceConfig.TAIGA_TASK_TOPIC_CONFIG ) );
			githubUrls.add( getGithubUrlsFromOrg(githubOrgUrl.get(i)) );
		}

		for (int i = 0; i < githubOrgUrl.size(); ++i) {
			log.info("Organization URL: " + githubOrgUrl.get(i));
			String[] reposUrl = githubUrls.get(i);
			for (String s : reposUrl) log.info("\tRepository URL: " + s);
		}
		
		if ( (githubInterval == null || githubInterval.isEmpty()) ) interval = 3600;
		else interval = Integer.parseInt(githubInterval);
		if ( (githubTeamsInterval == null || githubTeamsInterval.isEmpty()) ) teamsInterval = 120;
		else teamsInterval = Integer.parseInt(githubTeamsInterval);
	}

	@Override
	public void stop() {

	}

	public String version() {
		return "0.0.1";
	}

}
