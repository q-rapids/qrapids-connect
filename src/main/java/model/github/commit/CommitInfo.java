package model.github.commit;

public class CommitInfo {
    public CommitPerson author;
    public CommitPerson committer;
    public String message;
    public String url;
    public String comment_count;
    public CommitVerification verification;
}
