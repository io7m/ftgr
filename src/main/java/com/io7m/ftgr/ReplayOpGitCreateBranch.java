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

public final class ReplayOpGitCreateBranch implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpGitCreateBranch.class);
  }

  private final String                         branch;
  private final GitExecutableType              exec;
  private final GitRepositorySpecificationType repos;

  public ReplayOpGitCreateBranch(
    final GitExecutableType in_exec,
    final GitRepositorySpecificationType in_repos,
    final String in_branch)
  {
    this.exec = NullCheck.notNull(in_exec);
    this.repos = NullCheck.notNull(in_repos);
    this.branch = NullCheck.notNull(in_branch);
  }

  @Override public void execute(
    final DryRun dry_run)
    throws ReplayException
  {
    try {
      ReplayOpGitCreateBranch.LOG.info("creating branch {}", this.branch);

      if (dry_run == DryRun.EXECUTE) {
        this.exec.createBranch(this.repos, this.branch);
      }
    } catch (final IOException e) {
      throw new ReplayException(e);
    }
  }
}
