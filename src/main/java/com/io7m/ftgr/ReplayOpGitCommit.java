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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ReplayOpGitCommit implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpGitCommit.class);
  }

  private final GitExecutableType              git;
  private final GitRepositorySpecificationType repos;
  private final FossilCommit                   commit;
  private final long                           key;

  public ReplayOpGitCommit(
    final GitExecutableType in_git,
    final GitRepositorySpecificationType in_repos,
    final FossilCommit in_commit,
    final long key_id)
  {
    this.git = NullCheck.notNull(in_git);
    this.repos = NullCheck.notNull(in_repos);
    this.commit = NullCheck.notNull(in_commit);
    this.key = key_id;
  }

  @Override public <A, E extends Exception> A matchOperation(
    final ReplayOperationMatcherType<A, E> m)
    throws E
  {
    return m.onGitCommit(this);
  }

  @Override public void execute(final DryRun dry_run)
    throws ReplayException
  {
    ReplayOpGitCommit.LOG.info(
      "commit {} on branch {} ({})",
      this.commit.getCommitBlob(),
      this.commit.getBranch(),
      this.commit.getCommitComment());

    try {
      final GitIdent ident = this.repos.getUserNameMapping(
        this.commit.getCommitUser());
      this.git.createCommit(
        this.repos,
        this.commit.getCommitTime(),
        ident,
        this.commit.getCommitComment(),
        this.commit.getBranch(),
        this.key);
    } catch (final IOException e) {
      throw new ReplayException(e);
    }
  }
}
