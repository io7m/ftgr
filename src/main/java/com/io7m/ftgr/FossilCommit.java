/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.ftgr;

import com.io7m.jnull.NullCheck;

import java.sql.Timestamp;

public final class FossilCommit
{
  private final FossilCommitName commit_blob;
  private final Timestamp        commit_time;
  private final String           commit_comment;
  private final String           commit_user;
  private final String           branch;
  private final boolean          branch_is_new;
  private final int              id;

  public FossilCommit(
    final int in_commit_id,
    final FossilCommitName in_commit_blob,
    final Timestamp in_commit_time,
    final String in_commit_comment,
    final String in_branch,
    final boolean in_branch_is_new,
    final String in_commit_user)
  {
    this.id = in_commit_id;
    this.branch = NullCheck.notNull(in_branch);
    this.commit_blob = NullCheck.notNull(in_commit_blob);
    this.commit_time = NullCheck.notNull(in_commit_time);
    this.commit_comment = NullCheck.notNull(in_commit_comment);
    this.commit_user = NullCheck.notNull(in_commit_user);
    this.branch_is_new = in_branch_is_new;
  }

  public boolean isBranchNew()
  {
    return this.branch_is_new;
  }

  public FossilCommitName getCommitBlob()
  {
    return this.commit_blob;
  }

  public String getCommitComment()
  {
    return this.commit_comment;
  }

  public Timestamp getCommitTime()
  {
    return this.commit_time;
  }

  public String getCommitUser()
  {
    return this.commit_user;
  }

  public int getId()
  {
    return this.id;
  }

  @Override public String toString()
  {
    final StringBuilder sb = new StringBuilder("FossilCommit{");
    sb.append("branch='").append(this.branch).append('\'');
    sb.append(", commit_blob='").append(this.commit_blob).append('\'');
    sb.append(", commit_time=").append(this.commit_time);
    sb.append(", commit_comment='").append(this.commit_comment).append('\'');
    sb.append(", branch_is_new=").append(this.branch_is_new);
    sb.append(", id=").append(this.id);
    sb.append('}');
    return sb.toString();
  }

  @Override public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final FossilCommit that = (FossilCommit) o;

    if (this.branch_is_new != that.branch_is_new) {
      return false;
    }
    if (this.id != that.id) {
      return false;
    }
    if (!this.commit_blob.equals(that.commit_blob)) {
      return false;
    }
    if (!this.commit_time.equals(that.commit_time)) {
      return false;
    }
    if (!this.commit_comment.equals(that.commit_comment)) {
      return false;
    }
    if (!this.commit_user.equals(that.commit_user)) {
      return false;
    }
    return this.branch.equals(that.branch);

  }

  @Override public int hashCode()
  {
    int result = this.commit_blob.hashCode();
    result = 31 * result + this.commit_time.hashCode();
    result = 31 * result + this.commit_comment.hashCode();
    result = 31 * result + this.commit_user.hashCode();
    result = 31 * result + this.branch.hashCode();
    result = 31 * result + (this.branch_is_new ? 1 : 0);
    result = 31 * result + this.id;
    return result;
  }

  public String getBranch()
  {
    return this.branch;
  }
}
