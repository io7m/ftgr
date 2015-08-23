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
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

final class FTGRConfiguration
{
  private final File                  fossil_exec;
  private final File                  git_exec;
  private final File                  gpg_exec;
  private final File                  faketime_exec;
  private final Map<String, GitIdent> name_map;
  private final Map<BigInteger, BigInteger>       key_map;
  private final File                  git_repos;
  private final File                  fossil_repos;
  private final DryRun                dry_run;
  private final File                  commit_mapping_file;
  private final boolean               verification;

  private FTGRConfiguration(
    final File in_fossil_exec,
    final File in_git_exec,
    final File in_gpg_exec,
    final File in_faketime_exec,
    final Map<String, GitIdent> in_name_map,
    final Map<BigInteger, BigInteger> in_key_map,
    final File in_git_repos,
    final File in_fossil_repos,
    final DryRun in_dry_run,
    final File in_commit_mapping_file,
    final boolean in_verification)
  {
    this.fossil_exec = NullCheck.notNull(in_fossil_exec);
    this.git_exec = NullCheck.notNull(in_git_exec);
    this.gpg_exec = NullCheck.notNull(in_gpg_exec);
    this.faketime_exec = NullCheck.notNull(in_faketime_exec);
    this.name_map = NullCheck.notNull(in_name_map);
    this.key_map = NullCheck.notNull(in_key_map);
    this.git_repos = NullCheck.notNull(in_git_repos);
    this.fossil_repos = NullCheck.notNull(in_fossil_repos);
    this.dry_run = NullCheck.notNull(in_dry_run);
    this.commit_mapping_file = NullCheck.notNull(in_commit_mapping_file);
    this.verification = in_verification;
  }

  public static FTGRConfiguration fromProperties(
    final Properties p)
    throws JPropertyException
  {
    NullCheck.notNull(p);

    final File fossil_exec =
      new File(JProperties.getString(p, "com.io7m.ftgr.fossil_executable"));
    final File git_exec =
      new File(JProperties.getString(p, "com.io7m.ftgr.git_executable"));
    final File gpg_exec =
      new File(JProperties.getString(p, "com.io7m.ftgr.gpg_executable"));
    final File faketime_exec =
      new File(JProperties.getString(p, "com.io7m.ftgr.faketime_executable"));

    final Map<String, GitIdent> name_map = new HashMap<>(8);
    for (final Object k : p.keySet()) {
      final String ks = NullCheck.notNull((String) k);
      if (ks.startsWith("com.io7m.ftgr.name_map.")) {
        final String name = NullCheck.notNull(
          ks.replace("com.io7m.ftgr.name_map.", ""));
        final String v = NullCheck.notNull(p.getProperty(ks));
        final String[] parts = v.split("\\|");
        if (parts.length != 2) {
          throw new JPropertyException(
            String.format(
              "Invalid value for key %s (%s): Must be of the form: name|email",
              k,
              v));
        }
        final GitIdent ident = new GitIdent(parts[0].trim(), parts[1].trim());
        name_map.put(name, ident);
      }
    }

    final Map<BigInteger, BigInteger> key_map = new HashMap<>(8);
    for (final Object k : p.keySet()) {
      final String ks = NullCheck.notNull((String) k);
      if (ks.startsWith("com.io7m.ftgr.key_map.")) {
        final String name = NullCheck.notNull(
          ks.replace("com.io7m.ftgr.key_map.", ""));
        final String v = NullCheck.notNull(p.getProperty(ks));
        final BigInteger from = new BigInteger(name, 16);
        final BigInteger to = new BigInteger(v, 16);
        key_map.put(from, to);
      }
    }

    final File repos_git =
      new File(JProperties.getString(p, "com.io7m.ftgr.git_repository"));
    final File repos_fossil =
      new File(JProperties.getString(p, "com.io7m.ftgr.fossil_repository"));
    final File commit_map =
      new File(JProperties.getString(p, "com.io7m.ftgr.commit_map"));

    final DryRun dry_run;
    if (JProperties.getBoolean(p, "com.io7m.ftgr.dry_run")) {
      dry_run = DryRun.EXECUTE_DRY_RUN;
    } else {
      dry_run = DryRun.EXECUTE;
    }

    final boolean verify = JProperties.getBoolean(p, "com.io7m.ftgr.verify");

    return new FTGRConfiguration(
      fossil_exec,
      git_exec,
      gpg_exec,
      faketime_exec,
      name_map,
      key_map,
      repos_git,
      repos_fossil,
      dry_run,
      commit_map,
      verify);
  }

  public Map<BigInteger, BigInteger> getKeyMap()
  {
    return this.key_map;
  }

  public boolean wantVerification()
  {
    return this.verification;
  }

  public File getGitRepository()
  {
    return this.git_repos;
  }

  public File getFossilRepository()
  {
    return this.fossil_repos;
  }

  public File getFaketimeExecutable()
  {
    return this.faketime_exec;
  }

  public File getFossilExecutable()
  {
    return this.fossil_exec;
  }

  public File getGitExecutable()
  {
    return this.git_exec;
  }

  public File getGPGExecutable()
  {
    return this.gpg_exec;
  }

  public Map<String, GitIdent> getNameMap()
  {
    return this.name_map;
  }

  public DryRun getDryRun()
  {
    return this.dry_run;
  }

  public File getCommitMappingFile()
  {
    return this.commit_mapping_file;
  }
}
