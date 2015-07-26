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

public final class ReplayOpGitCreateRootCommit implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpGitCreateRootCommit.class);
  }

  private final GitRepositorySpecificationType repos;
  private final GitExecutableType              exec;
  private final FossilModelCommitNode          commit;

  public ReplayOpGitCreateRootCommit(
    final GitExecutableType in_exec,
    final GitRepositorySpecificationType in_repos,
    final FossilModelCommitNode in_commit)
  {
    this.exec = NullCheck.notNull(in_exec);
    this.repos = NullCheck.notNull(in_repos);
    this.commit = NullCheck.notNull(in_commit);
  }

  @Override public <A, E extends Exception> A matchOperation(
    final ReplayOperationMatcherType<A, E> m)
    throws E
  {
    return m.onGitCreateRootCommit(this);
  }

  @Override public void execute(
    final DryRun dry_run)
    throws ReplayException
  {
    try {
      ReplayOpGitCreateRootCommit.LOG.info(
        "creating root commit");

      if (dry_run == DryRun.EXECUTE) {
        final FossilCommit c = this.commit.getCommit();
        final String fossil_user = c.getCommitUser();
        final GitIdent git_user = this.repos.getUserNameMapping(fossil_user);

        this.exec.createRootCommit(
          this.repos,
          c.getCommitTime(),
          git_user,
          c.getCommitComment(),
          c.getBranch());
      }
    } catch (IOException e) {
      throw new ReplayException(e);
    }
  }
}
