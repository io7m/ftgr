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

public final class ReplayOpCheckKey implements ReplayOperationType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayOpCheckKey.class);
  }

  private final GPGExecutableType gpg;
  private final long              key_id;

  public ReplayOpCheckKey(
    GPGExecutableType in_gpg,
    long in_key_id)
  {
    this.gpg = NullCheck.notNull(in_gpg);
    this.key_id = in_key_id;
  }

  @Override public <A, E extends Exception> A matchOperation(
    final ReplayOperationMatcherType<A, E> m)
    throws E
  {
    return m.onCheckKey(this);
  }

  @Override public void execute(final DryRun dry_run)
    throws ReplayException
  {
    try {
      ReplayOpCheckKey.LOG.info(
        "checking key: {}", Long.toHexString(this.key_id));

      if (dry_run == DryRun.EXECUTE) {
        if (this.gpg.hasSecretKey(this.key_id) == false) {
          throw new ReplayExceptionKeyNonexistent(this.key_id);
        }
      }
    } catch (IOException e) {
      throw new ReplayException(e);
    }
  }
}
