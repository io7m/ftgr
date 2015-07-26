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

import java.util.List;

public final class ReplayExecutor implements ReplayExecutorType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(ReplayExecutor.class);
  }

  private ReplayExecutor()
  {

  }

  public static ReplayExecutorType newExecutor()
  {
    return new ReplayExecutor();
  }

  @Override public void executePlan(
    final List<ReplayOperationType> plan,
    final DryRun dry_run)
    throws ReplayException
  {
    NullCheck.notNull(plan);

    ReplayExecutor.LOG.debug(
      "executing plan of {} operations ({})",
      Integer.valueOf(plan.size()),
      dry_run);

    for (int index = 0; index < plan.size(); ++index) {
      final ReplayOperationType op = NullCheck.notNull(plan.get(index));
      op.execute(dry_run);
    }
  }
}
