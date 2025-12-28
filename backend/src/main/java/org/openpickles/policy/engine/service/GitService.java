package org.openpickles.policy.engine.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class GitService {

    public String fetchFileContent(String repoUrl, String branch, String filePath) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("policy-engine-git");

            // Clone the repository
            try (Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(tempDir.toFile())
                    .setBranch(branch)
                    .setDepth(1) // Shallow clone for speed
                    .call()) {

                Repository repository = git.getRepository();

                // Read the file content
                try (RevWalk revWalk = new RevWalk(repository)) {
                    ObjectId lastCommitId = repository.resolve(branch);
                    if (lastCommitId == null) {
                        // Maybe branch is HEAD but resolve needs exact ref, try HEAD if branch is
                        // 'main' or 'master' didn't work purely by name?
                        // Actually clone w/ branch sets HEAD to that branch.
                        lastCommitId = repository.resolve("HEAD");
                    }

                    RevCommit commit = revWalk.parseCommit(lastCommitId);
                    RevTree tree = commit.getTree();

                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        treeWalk.setFilter(PathFilter.create(filePath));

                        if (!treeWalk.next()) {
                            throw new org.openpickles.policy.engine.exception.FunctionalException(
                                    "File not found in repository: " + filePath, "FUNC_006");
                        }

                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);

                        return new String(loader.getBytes());
                    }
                }
            }
        } catch (org.openpickles.policy.engine.exception.FunctionalException e) {
            throw e;
        } catch (Exception e) {
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "Failed to fetch file from Git: " + e.getMessage(), "TECH_002", e);
        } finally {
            if (tempDir != null) {
                try {
                    FileSystemUtils.deleteRecursively(tempDir);
                } catch (IOException e) {
                    // Ignore cleanup error
                    System.err.println("Failed to clean up temp dir: " + e.getMessage());
                }
            }
        }
    }

    public void pushFileContent(String repoUrl, String branch, String filePath, String content, String message) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("policy-engine-git-push");

            // Full clone needed for push? Shallow clone usually fine if depth sufficient or
            // just pushing top.
            // But usually for push we want to be careful.
            try (Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(tempDir.toFile())
                    .setBranch(branch)
                    .call()) {

                // Write content to file
                File file = new File(tempDir.toFile(), filePath);
                // Ensure parent dirs exist
                file.getParentFile().mkdirs();
                Files.write(file.toPath(), content.getBytes());

                // Add
                git.add().addFilepattern(filePath).call();

                // Commit
                // TODO: Author/Committer identification
                git.commit().setMessage(message).call();

                // Push
                // Note: Auth is tricky here without credentials.
                // Assuming SSH agent or .netrc or HTTP creds if URL has them.
                // If the user provided credentials in URL (https://user:pass@...), it works.
                // Otherwise JGit needs CredentialsProvider.
                // For now, relying on environment or URL embedding as per plan.
                git.push().call();
            }
        } catch (Exception e) {
            throw new org.openpickles.policy.engine.exception.TechnicalException(
                    "Failed to push file to Git: " + e.getMessage(), "TECH_003", e);
        } finally {
            if (tempDir != null) {
                try {
                    FileSystemUtils.deleteRecursively(tempDir);
                } catch (IOException e) {
                    System.err.println("Failed to clean up temp dir: " + e.getMessage());
                }
            }
        }
    }
}
