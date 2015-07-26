io7m-ftgr
=========

## What is this?

`ftgr` is a tool made for a very specific purpose: Take a
[Fossil](http://fossil-scm.org) repository that has only had PGP-signed
commits from one person (possibly with multiple keys), and produce
a semantically equivalent [Git](http://git-scm.com) repository. The
resulting `Git` repository's commits will have the same dates and
signatures as the original `Fossil` commits (and the dates inside
those signatures will also match).

## Requirements

+ [Git](http://git-scm.com)
+ [Fossil](http://fossil-scm.org) (tested with `1.32 [715f88811a]`)
+ [libfaketime](https://github.com/wolfcw/libfaketime)
+ [GnuPG2](http://gnupg.org)
+ A JRE supporting Java 7 or greater.

## Building

```
$ mvn clean package
```

## How?

`Fossil` and `Git` use a similar internal model: A directed acyclic
graph of immutable objects representing files, directories, and
commits. Therefore, it's a fairly simple case of transforming one
to the other by literally performing each commit in a `Git` repository
as it was performed in the original `Fossil` repository, with minor
adjustments to account for differences in how merges and branches are
represented.

0. Read all commits from the `Fossil` repository into a directed acyclic
   graph structure, keeping track of the `branch`, `time`, `user`, and
   `comment` of the original commits. The vertices of the graph are the
   commits and the edges of the graph are the parent → child links.
   `Fossil` implicitly marks the first commit of each branch; the
   details of how that happens aren't important here, just the fact
   that it's possible to know unambiguously whether a commit was
   responsible for "creating" the branch or not.

1. Fetch the [manifest](http://fossil-scm.org/index.html/doc/trunk/www/fileformat.wiki#manifest)
   for each commit, and extract the key ID of the PGP used to sign the
   manifest. Maintain a list of all keys used.

2. Check that the running user has the private key for each of the
   listed keys above. Give up if any are missing.

3. Check that a mapping exists from all `Fossil` user names to `Git`
   authors. This is so that the author/committer user names in the
   original commits can be transformed into the conventional
   `An A Author <someone@example.com>` format. Give up if any are
   missing.

4. Create an empty `Git` repository. Create an initial unsigned
   root commit consisting of a single `.gitignore` file that
   hides any relevant `Fossil` metadata files.

5. [Open](http://fossil-scm.org/index.html/help/open) the `Fossil`
   repository into the `Git` repository directory. This allows
   `Fossil` operations to be performed by executing the `fossil`
   command line with that directory as the current working directory.

6. Order the commits by time. Oldest commits come first.

7. For each commit `c`:
  + If `c` was the first commit of a branch, create a new `Git` branch.
  + If `c` has two parents, merge the current branch with whichever of
    the two parents are on the other branch.
  + Otherwise, checkout commit `c` from `Fossil`, replacing all files
    in the current directory (and removing all files that are not part
    of the commit). Add them all to the `Git` index and commit using
    the original message, time, and signing the result with the
    original PGP key.

Internally, `faketime` is used to execute both `git` and `gpg`. This
allows for new commits to have the exact times as specified in the
original commits. Amusingly, it also allows for the use of expired
keys in GnuPG.

