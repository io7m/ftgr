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

public final class FossilModelCommitNode
{
  private final int          id;
  private final FossilCommit commit;

  public FossilModelCommitNode(
    final FossilCommit in_commit,
    final int in_id)
  {
    this.commit = NullCheck.notNull(in_commit);
    this.id = in_id;
  }

  public FossilCommit getCommit()
  {
    return this.commit;
  }

  @Override public String toString()
  {
    final StringBuilder sb = new StringBuilder("FossilModelCommitNode{");
    sb.append("commit=").append(this.commit);
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

    final FossilModelCommitNode that = (FossilModelCommitNode) o;
    return this.id == that.id;
  }

  @Override public int hashCode()
  {
    return this.id;
  }
}
