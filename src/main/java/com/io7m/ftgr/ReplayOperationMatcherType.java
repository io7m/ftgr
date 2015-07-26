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

public interface ReplayOperationMatcherType<A, E extends Exception>
{
  A onCheckKey(ReplayOpCheckKey r)
    throws E;

  A onGitCreateBranch(ReplayOpGitCreateBranch r)
    throws E;

  A onGitCreateRepository(ReplayOpGitCreateRepository r)
    throws E;

  A onGitCreateRootCommit(ReplayOpGitCreateRootCommit r)
    throws E;

  A onCheckName(ReplayOpCheckName r)
    throws E;

  A onGitMerge(ReplayOpGitMerge r)
    throws E;

  A onGitCommit(ReplayOpGitCommit r)
    throws E;

  A onFossilCheckout(ReplayOpFossilCheckout r)
    throws E;

  A onFossilOpen(ReplayOpFossilOpen r)
    throws E;

  A onGitCheckoutBranch(ReplayOpGitCheckoutBranch r)
    throws E;

  A onGitAddAll(ReplayOpGitAddAll r)
    throws E;
}
