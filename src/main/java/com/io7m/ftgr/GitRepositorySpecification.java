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

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public final class GitRepositorySpecification
  implements GitRepositorySpecificationType
{
  private final File                  directory;
  private final Map<String, GitIdent> name_map;

  private GitRepositorySpecification(
    final File in_directory,
    final Map<String, GitIdent> in_name_map)
  {
    this.directory = NullCheck.notNull(in_directory);
    this.name_map = NullCheck.notNull(in_name_map);
  }

  public static GitRepositorySpecificationBuilderType newBuilder(
    final File directory)
  {
    return new Builder(directory);
  }

  @Override public File getDirectory()
  {
    return this.directory;
  }

  @Override
  public OptionType<GitIdent> getUserNameMappingOptional(final String u)
  {
    if (this.name_map.containsKey(u)) {
      return Option.some(this.name_map.get(u));
    }

    return Option.none();
  }

  @Override public GitIdent getUserNameMapping(final String u)
    throws NoSuchElementException
  {
    if (this.name_map.containsKey(u)) {
      return NullCheck.notNull(this.name_map.get(u));
    }

    throw new NoSuchElementException(u);
  }

  private static class Builder implements GitRepositorySpecificationBuilderType
  {
    private final File                  directory;
    private final Map<String, GitIdent> name_map;

    public Builder(final File in_directory)
    {
      this.directory = NullCheck.notNull(in_directory);
      this.name_map = new HashMap<>();
    }

    @Override public GitRepositorySpecificationType build()
    {
      return new GitRepositorySpecification(this.directory, this.name_map);
    }

    @Override public void addUserNameMapping(
      final String name,
      final GitIdent result)
    {
      NullCheck.notNull(name);
      NullCheck.notNull(result);
      this.name_map.put(name, result);
    }
  }
}
