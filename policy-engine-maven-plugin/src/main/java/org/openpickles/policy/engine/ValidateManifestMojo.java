package org.openpickles.policy.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ValidateManifestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "manifestFile", defaultValue = "policy-manifest.yaml")
    private String manifestFileName;

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Look in src/main/resources by default
        File resourcesDir = new File(project.getBasedir(), "src/main/resources");
        File manifestFile = new File(resourcesDir, manifestFileName);

        if (!manifestFile.exists()) {
            // Also check root if not in resources? For now strict.
            getLog().warn(
                    "No policy-manifest.yaml found at " + manifestFile.getAbsolutePath() + ". Skipping validation.");
            return;
        }

        getLog().info("Validating policy manifest: " + manifestFile.getPath());

        try {
            JsonNode root = mapper.readTree(manifestFile);

            // Validate Structure
            if (!root.has("service")) {
                throw new MojoFailureException("Manifest missing required 'service' block.");
            }
            if (!root.has("policies")) {
                getLog().warn("Manifest has no 'policies' defined.");
            }

            // Validate Referenced Files
            if (root.has("policies")) {
                validatePolicies(root.get("policies"), resourcesDir);
            }

            getLog().info("Manifest validation successful.");

        } catch (IOException e) {
            throw new MojoExecutionException("Error parsing policy-manifest.yaml", e);
        }
    }

    private void validatePolicies(JsonNode policiesNode, File resourcesDir) throws MojoFailureException {
        if (!policiesNode.isArray()) {
            throw new MojoFailureException("'policies' must be an array.");
        }

        Iterator<JsonNode> elements = policiesNode.elements();
        while (elements.hasNext()) {
            JsonNode policy = elements.next();
            if (policy.has("file")) {
                String filePath = policy.get("file").asText();
                File regoFile = new File(resourcesDir, filePath);
                if (!regoFile.exists()) {
                    throw new MojoFailureException("Referenced policy file not found: " + regoFile.getAbsolutePath());
                }
                getLog().info("Verified policy file exists: " + filePath);
            } else if (!policy.has("content")) {
                throw new MojoFailureException("Policy definition must have either 'file' or 'content'. Policy Name: "
                        + policy.path("name").asText("unknown"));
            }
        }
    }
}
