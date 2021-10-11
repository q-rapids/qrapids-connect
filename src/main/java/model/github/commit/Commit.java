package model.github.commit;

import model.github.User;

public class Commit {
    public String sha;
    public CommitInfo commit;
    public String url;
    public User author;
    public User committer;
}

