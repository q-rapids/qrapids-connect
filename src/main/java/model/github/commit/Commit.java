package model.github.commit;

import model.github.User;

import java.util.List;

public class Commit {
    public String sha;
    public CommitInfo commit;
    public String url;
    public User author;
    public User committer;
    public List<ParentData> parents;
    public Stats stats;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Commit u = (Commit) obj;
        return u.sha.equals(this.sha);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * ((sha == null) ? 0 : sha.hashCode());
    }
}

