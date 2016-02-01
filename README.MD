## Repository for integration tests between different modules (jBehave,Cucumber and so on)  

This repo is used for testing different modules of Serenity BDD together.

How build works:
 - build serenity-core and publish artifacts to maven local repository with some predefined version
 - build and test all modules against predefined version

As result all submodules will be tested agains one version to be sure that all works fine. 

This project contains git submodules in last working-well-together version:

```
src/test/resources/projects/serenity-core           git@github.com:serenity-bdd/serenity-core.git
src/test/resources/projects/serenity-cucumber       git@github.com:serenity-bdd/serenity-cucumber.git
src/test/resources/projects/serenity-jbehave        git@github.com:serenity-bdd/serenity-jbehave.git
src/test/resources/projects/serenity-jira           git@github.com:serenity-bdd/serenity-jira.git
src/test/resources/projects/serenity-maven-plugin   git@github.com:serenity-bdd/serenity-maven-plugin.git
src/test/resources/projects/serenity-test-projects  git@github.com:serenity-bdd/serenity-test-projects.git
```

After checkout of this repo, you should init all submodules, and run build all submodules: 

```
git submodule update --init --recursive
./gradlew clean build
```

**IMPORTANT**:
current configuration can use parallel test execution and modules build, as result all modules and all test of modules execute in parallel - it can be resource consuming but will use resources of your PC in much more efficient way and make build much faster on powerful pc (tested with 22+ CPU cores and 20+ GB of RAM). 


## Build all projects using latest version of Serentiy modules

For this you should update all submodules to last version. 

Fore example, your latest version of modules available under remote "origin" repository, then do next to update all repositories to latest version and build all them as well: 

```
git submodule foreach git pull origin master
./gradlew clean build
```
