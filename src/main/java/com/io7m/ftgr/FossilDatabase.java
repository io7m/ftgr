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
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class FossilDatabase implements FossilDatabaseType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FossilDatabase.class);
  }

  private final SQLiteDataSource data;
  private final String           query_all_commits;
  private final String           query_parent_links;

  private FossilDatabase(
    final SQLiteDataSource in_ds)
    throws IOException
  {
    this.data = NullCheck.notNull(in_ds);

    final Properties queries = new Properties();
    queries.loadFromXML(
      FossilDatabase.class.getResourceAsStream(
        "queries.xml"));

    this.query_all_commits =
      NullCheck.notNull(queries.getProperty("query_all_commits"));
    this.query_parent_links =
      NullCheck.notNull(queries.getProperty("query_parent_links"));
  }

  public static FossilDatabaseType openDatabase(
    final File file,
    final File executable)
    throws FossilDatabaseException
  {
    NullCheck.notNull(file);
    NullCheck.notNull(executable);

    FossilDatabase.LOG.debug("opening {}", file);

    try {
      if (file.isFile() == false) {
        throw new FossilDatabaseException(
          String.format(
            "%s is not a regular file", file));
      }

      final SQLiteDataSource ds = new SQLiteDataSource();
      ds.setReadOnly(true);
      ds.setUrl("jdbc:sqlite:" + file);
      return new FossilDatabase(ds);
    } catch (final IOException e) {
      throw new FossilDatabaseException(e);
    }
  }

  @Override public FossilDatabaseTransactionType newTransaction()
    throws FossilDatabaseException
  {
    try {
      return new Transaction(this.data.getConnection());
    } catch (final SQLException e) {
      throw new FossilDatabaseException(e);
    }
  }

  private final class Transaction implements FossilDatabaseTransactionType
  {
    private final Connection conn;

    Transaction(final Connection c)
    {
      this.conn = NullCheck.notNull(c);
    }

    @Override public void close()
      throws FossilDatabaseException
    {
      try {
        this.conn.close();
      } catch (final SQLException e) {
        throw new FossilDatabaseException(e);
      }
    }

    @Override public Map<Integer, FossilCommit> getAllCommits()
      throws FossilDatabaseException
    {
      final Map<Integer, FossilCommit> xs = new HashMap<>(100);

      try (final PreparedStatement st = this.conn.prepareStatement(
        FossilDatabase.this.query_all_commits)) {

        try (final ResultSet rs = st.executeQuery()) {
          while (rs.next()) {
            final int id = rs.getInt("commit_id");
            final String blob = NullCheck.notNull(rs.getString("commit_blob"));
            final String comment =
              NullCheck.notNull(rs.getString("commit_comment"));
            final String time_raw =
              NullCheck.notNull(rs.getString("commit_mtime"));
            final Timestamp time = Timestamp.valueOf(time_raw);
            final String branch = NullCheck.notNull(rs.getString("branch"));
            final boolean branch_is_new = rs.getBoolean("branch_is_new");
            final String user = NullCheck.notNull(rs.getString("commit_user"));

            final FossilCommit fc = new FossilCommit(
              id, blob, time, comment, branch, branch_is_new, user);
            xs.put(Integer.valueOf(id), fc);
          }
        }
      } catch (final SQLException e) {
        throw new FossilDatabaseException(e);
      }

      return xs;
    }

    @Override public List<FossilParentLink> getParentLinks()
      throws FossilDatabaseException
    {
      final List<FossilParentLink> xs = new ArrayList<>(100);

      try (final PreparedStatement st = this.conn.prepareStatement(
        FossilDatabase.this.query_parent_links)) {

        try (final ResultSet rs = st.executeQuery()) {
          while (rs.next()) {
            final int parent_id = rs.getInt("parent_id");
            final int child_id = rs.getInt("child_id");
            xs.add(new FossilParentLink(parent_id, child_id));
          }
        }
      } catch (final SQLException e) {
        throw new FossilDatabaseException(e);
      }

      return xs;
    }

  }
}
