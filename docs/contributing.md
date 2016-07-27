## Welcome

We welcome contributions to the Hyperledger Project in many forms, and there's always plenty to do!

First things first, please review the Hyperledger Project's [Code of Conduct](https://github.com/hyperledger/hyperledger/wiki/Hyperledger-Project-Code-of-Conduct) before participating. It is important that we keep things civil.

## Getting help
If you are looking for something to work on, or need some expert assistance in debugging a problem or working out a fix to an issue, our [community](https://www.hyperledger.org/community) is always eager to help. We hang out on [Slack](https://hyperledgerproject.slack.com/), IRC (#hyperledger on freenode.net) and the [mailing lists](http://lists.hyperledger.org/). Most of us don't bite ;-) and will be glad to help.

## Requirements and Use Cases
We have a [Requirements WG](https://github.com/hyperledger/hyperledger/wiki/Requirements-WG) that is documenting use cases and from those use cases deriving requirements. If you are interested in contributing to this effort, please feel free to join the discussion in [slack](https://hyperledgerproject.slack.com/messages/requirements/).

## Reporting bugs
If you are a user and you find a bug, please submit an [issue](https://github.com/hyperledger/fabric-api/issues). Please try to provide sufficient information for someone else to reproduce the issue. One of the project's maintainers should respond to your issue within 24 hours. If not, please bump the issue and request that it be reviewed.

## Fabric-API
We built Hyperledger Fabric-API to become the foundation of applications served by an [API](api.md). Changes to the API should be preceeded with in-depth discussion and introduced in accordance with the versioning scheme.

## Coding guidelines
We use IntelliJ's default formatting for Java code and Scalariform for scala. Please configure your choice of editor to produce similar layout. Avoid re-formatting code, limit differences to aid code review.

## Versioning
Hyperledger uses major.minor.patch version scheme. Versions within same major and minor but higher patch number should be drop-in backward compatible on API level. A version with higher minor within the same major will add and might remove features, break API, database or network drop-in backward compatibility.

## Pull Requests
Pull requests should be focused: fix a bug, add a feature or refactore code, but not a mixture.  Bug fixes and features should be accompanied with tests. Refactoring with wide scope, such as renaming a frequently used method should be discussed as a feature request and will be applied during dedicated re-factoring windows, that the maintainers of the code base will announce regularly.

## Fixing issues and working stories
Review the [issues list](https://github.com/hyperledger/fabric-api/issues) and find something that interests you. You could also check the ["help wanted"](https://github.com/hyperledger/fabric-api/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22) list. It is wise to start with something relatively straight forward and achievable. Usually there will be a comment in the issue that indicates whether someone has already self-assigned the issue. If no one has already taken it, then add a comment assigning the issue to yourself, eg.: ```I'll work on this issue.```. Please be considerate and rescind the offer in comments if you cannot finish in a reasonable time, or add a comment saying that you are still actively working the issue if you need a little more time.

We are using the [GitHub Flow](https://guides.github.com/introduction/flow/) process to manage code contributions. If you are unfamiliar, please review that link before proceeding.

To work on something, whether a new feature or a bugfix:
  1. Create a [fork](https://help.github.com/articles/fork-a-repo/) (if you haven't already)

  2. Clone it locally
  ```
  git clone https://github.com/yourid/fabric-api.git
  ```
  3. Add the upstream repository as a remote
  ```
  git remote add upstream https://github.com/hyperledger/fabric-api.git
  ```
  4. Create a branch

  Create a descriptively-named branch off of your cloned fork ([more detail here](https://help.github.com/articles/syncing-a-fork/))
  ```
  cd fabric-api
  git checkout -b issue-nnnn
  ```
  5. Commit your code

  Commit to that branch locally, and regularly push your work to the same branch on the server.

  6. Commit messages

  Commit messages must have a short description no longer than 50 characters followed by a blank line and a longer, more descriptive message that includes reference to issue(s) being addressed so that they will be automatically closed on a merge e.g. ```Closes #1234``` or ```Fixes #1234```.

  7. Pull Request (PR)

  When you need feedback or help, or you think the branch is ready for merging, open a pull request (make sure you have first successfully built and tested.

   _Note: if your PR does not merge cleanly, use ```git rebase master``` in your feature branch to update your pull request rather than using ```git merge master```_.

  8. Did we mention tests? All code changes should be accompanied by new or modified tests. Be sure to check the slack #fabric-ci-status channel for status of your build.

  9. Any code changes that affect documentation should be accompanied by corresponding changes (or additions) to the documentation and tests. This will ensure that if the merged PR is reversed, all traces of the change will be reversed as well.

After your pull request has been reviewed and signed off, a maintainer will merge it into the master branch.

## Becoming a maintainer
This project is managed under open governance model as described in our  [charter](https://www.hyperledger.org/about/charter). Projects or sub-projects will be lead by a set of maintainers. New projects can designate an initial set of maintainers that will be approved by the Technical Steering Committee when the project is first approved. The project's maintainers will, from time-to-time, consider adding a new maintainer. An existing maintainer will post a pull request to the [MAINTAINERS.txt](https://github.com/hyperledger/fabric-api/blob/master/MAINTAINERS.txt) file. If a majority of the maintainers concur in the comments, the pull request is then merged and the individual becomes a maintainer.

## Legal stuff
We have tried to make it as easy as possible to make contributions. This applies to how we handle the legal aspects of contribution. We use the same approach&mdash;the [Developer's Certificate of Origin 1.1 (DCO)](http://developercertificate.org/)&mdash;that the Linux&reg; Kernel [community](http://elinux.org/Developer_Certificate_Of_Origin) uses to manage code contributions.
We simply ask that when submitting a pull request, the developer must include a sign-off statement in the pull request description.

Here is an example Signed-off-by line, which indicates that the submitter accepts the DCO:

```
Signed-off-by: John Doe <john.doe@hisdomain.com>
```
