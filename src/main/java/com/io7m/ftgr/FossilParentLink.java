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

public class FossilParentLink
{
  private final int parent;
  private final int child;

  public FossilParentLink(
    final int in_child,
    final int in_parent)
  {
    this.child = in_child;
    this.parent = in_parent;
  }

  public int getChild()
  {
    return this.child;
  }

  public int getParent()
  {
    return this.parent;
  }

  @Override public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }

    final FossilParentLink that = (FossilParentLink) o;

    if (this.parent != that.parent) {
      return false;
    }
    return this.child == that.child;
  }

  @Override public String toString()
  {
    final StringBuilder sb = new StringBuilder("FossilParentLink{");
    sb.append("child=").append(this.child);
    sb.append(", parent=").append(this.parent);
    sb.append('}');
    return sb.toString();
  }

  @Override public int hashCode()
  {
    int result = this.parent;
    result = 31 * result + this.child;
    return result;
  }
}
