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

public final class FossilTag
{
  private final String    name;
  private final int       commit_id;
  private final Timestamp creation_time;

  public FossilTag(
    final int in_commit_id,
    final String in_name,
    final Timestamp in_creation_time)
  {
    this.commit_id = in_commit_id;
    this.name = NullCheck.notNull(in_name);
    this.creation_time = NullCheck.notNull(in_creation_time);
  }

  @Override public String toString()
  {
    final StringBuilder sb = new StringBuilder("FossilTag{");
    sb.append("commit_id=").append(this.commit_id);
    sb.append(", name='").append(this.name).append('\'');
    sb.append(", creation_time=").append(this.creation_time);
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

    final FossilTag fossilTag = (FossilTag) o;
    if (this.commit_id != fossilTag.commit_id) {
      return false;
    }
    if (!this.name.equals(fossilTag.name)) {
      return false;
    }
    return this.creation_time.equals(fossilTag.creation_time);
  }

  @Override public int hashCode()
  {
    int result = this.name.hashCode();
    result = 31 * result + this.commit_id;
    result = 31 * result + this.creation_time.hashCode();
    return result;
  }

  public int getCommitID()
  {
    return this.commit_id;
  }

  public Timestamp getCreationTime()
  {
    return this.creation_time;
  }

  public String getName()
  {
    return this.name;
  }
}
