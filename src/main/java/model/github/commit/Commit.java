package model.github.commit;

import model.github.User;

public class Commit {
    public String sha;
    public CommitInfo commit;
    public String url;
    public User author;
    public User committer;
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
        int result = prime * ((sha == null) ? 0 : sha.hashCode());
        return result;
    }
}

