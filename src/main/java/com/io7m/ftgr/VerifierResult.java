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

public final class VerifierResult
{
  private final FossilCommitName fossil_commit;
  private final GitCommitName    git_commit;
  private final String           message;
  private final boolean          ok;

  public VerifierResult(
    final FossilCommitName in_fossil_commit,
    final GitCommitName in_git_commit,
    final String in_message,
    final boolean in_ok)
  {
    this.fossil_commit = NullCheck.notNull(in_fossil_commit);
    this.git_commit = NullCheck.notNull(in_git_commit);
    this.message = NullCheck.notNull(in_message);
    this.ok = in_ok;
  }

  public FossilCommitName getFossilCommit()
  {
    return this.fossil_commit;
  }

  public GitCommitName getGitCommit()
  {
    return this.git_commit;
  }

  public String getMessage()
  {
    return this.message;
  }

  public boolean isOk()
  {
    return this.ok;
  }
}
