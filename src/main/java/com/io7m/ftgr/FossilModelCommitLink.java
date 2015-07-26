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

public final class FossilModelCommitLink
{
  private final FossilModelCommitNode source;
  private final FossilModelCommitNode target;

  public FossilModelCommitLink(
    final FossilModelCommitNode in_source,
    final FossilModelCommitNode in_target)
  {
    this.source = NullCheck.notNull(in_source);
    this.target = NullCheck.notNull(in_target);
  }

  @Override public String toString()
  {
    final StringBuilder sb = new StringBuilder("FossilModelCommitLink{");
    sb.append("source=").append(this.source);
    sb.append(", target=").append(this.target);
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

    final FossilModelCommitLink that = (FossilModelCommitLink) o;
    if (!this.source.equals(that.source)) {
      return false;
    }
    return this.target.equals(that.target);
  }

  @Override public int hashCode()
  {
    int result = this.source.hashCode();
    result = 31 * result + this.target.hashCode();
    return result;
  }
}
